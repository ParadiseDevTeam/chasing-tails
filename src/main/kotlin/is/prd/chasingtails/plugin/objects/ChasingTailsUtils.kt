/*
 * Copyright (C) 2024 Paradise Dev Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 */

package `is`.prd.chasingtails.plugin.objects

import `is`.prd.chasingtails.plugin.ChasingtailsPlugin
import `is`.prd.chasingtails.plugin.config.GamePlayerData
import `is`.prd.chasingtails.plugin.managers.ChasingTailsGameManager.gamePlayers
import `is`.prd.chasingtails.plugin.managers.ChasingTailsResumptionManager.joinedGameMasters
import `is`.prd.chasingtails.plugin.managers.ChasingTailsResumptionManager.joinedGamePlayers
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.scoreboard.Scoreboard

/**
 * @author aroxu, DytroC, ContentManager
 */

object ChasingTailsUtils {
    val plugin = ChasingtailsPlugin.instance
    val server = plugin.server

    val scoreboard = server.scoreboardManager.mainScoreboard.apply {
        reinitializeScoreboard()
    }

    private val mappedColors = hashMapOf<NamedTextColor, String>(
        Pair(NamedTextColor.LIGHT_PURPLE, "핑크색"),
        Pair(NamedTextColor.RED, "빨간색"),
        Pair(NamedTextColor.GOLD, "주황색"),
        Pair(NamedTextColor.YELLOW, "노란색"),
        Pair(NamedTextColor.GREEN, "초록색"),
        Pair(NamedTextColor.BLUE, "파란색"),
        Pair(NamedTextColor.DARK_BLUE, "남색"),
        Pair(NamedTextColor.DARK_PURPLE, "보라색")
    )

    fun Scoreboard.reinitializeScoreboard() {
        val colorList = listOf(
            NamedTextColor.LIGHT_PURPLE,
            NamedTextColor.RED,
            NamedTextColor.GOLD,
            NamedTextColor.YELLOW,
            NamedTextColor.GREEN,
            NamedTextColor.BLUE,
            NamedTextColor.DARK_BLUE,
            NamedTextColor.DARK_PURPLE,
        ).reversed()

        colorList.forEachIndexed { index, color ->
            getTeam("team$index")?.unregister()

            registerNewTeam("team$index").apply {
                color(color)
            }
        }
    }

    fun manageWorld(world: World) = world.apply {
        setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
        setGameRule(GameRule.KEEP_INVENTORY, true)
        setGameRule(GameRule.SHOW_DEATH_MESSAGES, false)
        setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
        difficulty = Difficulty.EASY
    }

    val Player.gamePlayerData
        get() = gamePlayers.find { it.uuid == uniqueId }

    var OfflinePlayer.lastLocation: Location?
        get() = plugin.config.getLocation("last_location.${uniqueId}")
        set(value) {
            plugin.config.set("last_location.${uniqueId}", value)
        }

    var initEndSpawn: Location? = null

    val admins = hashSetOf(
        "762dea11-9c45-4b18-95fc-a86aab3b39ee",
        "5082c832-7f7c-4b04-b0c7-2825062b7638",
        "5ca88187-c8af-48b8-b4e8-6243b16f924a"
    )

    val Player.color: TextColor
        get() = this@ChasingTailsUtils.scoreboard.getPlayerTeam(this)?.color()
            ?: NamedTextColor.WHITE

    private val Player.koreanColor
        get() = mappedColors[color] ?: "하얀색"

    fun GamePlayer.notifyTeam() {
        sendMessage(
            text("당신의 팀: ", NamedTextColor.YELLOW)
                .append(text(player.koreanColor, player.color))
        )

        sendMessage(
            text("당신의 타겟은 ", NamedTextColor.YELLOW).append(
                text(
                    target
                        .player.koreanColor, target.player.color
                )
            ).append(text("입니다.", NamedTextColor.YELLOW))
        )
    }

    fun Player.formatUsername(): String {
        return when (uniqueId.toString()) {
            "389c4c9b-6342-42fc-beb3-922a7d7a72f9" -> "코마"
            "dc339a98-af07-49ed-a9d7-c3b95d3d2000" -> "행크"
            "a14ea0bf-77ea-4c49-ad42-c4cee010b10c" -> "쪼만"
            "a6d46d92-f701-49e5-a6bd-14a2e3ba76d6" -> "우융"
            "ae2314d7-ed7e-49b6-ad44-58d93e33f4bb" -> "파이브"
            "814e180c-22ab-4d92-b17d-4808800927c4" -> "플래그"
            "a8d09337-1fe0-48a8-9b47-857b4033e717" -> "티푸"
            "b1b54589-95f9-44d3-9626-edb9288fa4f6" -> "옝"
            else -> name
        }
    }

    fun checkPlayers(): Boolean {
        val configGamePlayers =
            plugin.config.getStringList("gamePlayers")
        val configMainMasters =
            plugin.config.getStringList("mainMasters")

        return configGamePlayers.size != joinedGamePlayers.size
                || configMainMasters.size != joinedGameMasters.size
    }

    fun Player.restoreGamePlayer() {
        val data = plugin.config.get("chasingtails.${uniqueId}") as GamePlayerData

        gamePlayerData?.let { gamePlayer ->
            gamePlayer.master = gamePlayers.find { it.uuid.toString() == data.master }
            gamePlayer.deathTimer = server.worlds
                .flatMap { world -> world.entities }
                .firstOrNull { it.uniqueId.toString() == data.deathTimer } as? TextDisplay
            gamePlayer.temporaryDeathDuration = data.temporaryDeathDuration

            gamePlayer.deathTimer?.let { deathTimer ->
                gamePlayer.player.addPassenger(deathTimer)
            }

            gamePlayer.master?.let {
                getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 10.0
            }
        }

        val team = this@ChasingTailsUtils.scoreboard.getTeam(data.teamName)
            ?: this@ChasingTailsUtils.scoreboard.registerNewTeam(data.teamName)

        try {
            team.color()
        } catch (e: IllegalStateException) {
            team.color(NamedTextColor.namedColor(data.teamColor))
        }

        team.addPlayer(this)

        if (gamePlayerData?.master != null) gamePlayerData?.equipSlaveArmor(this)
    }
}