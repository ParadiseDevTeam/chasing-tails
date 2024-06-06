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
import `is`.prd.chasingtails.plugin.managers.ChasingTailsGameManager
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Scoreboard

/**
 * @author aroxu, DytroC, ContentManager
 */

object ChasingTailsUtils {
    val plugin = ChasingtailsPlugin.instance
    val server = plugin.server

    val scoreboard = server.scoreboardManager.newScoreboard.apply {
        reinitializeScoreboard()
    }

    val mappedColors = hashMapOf<NamedTextColor, String>(
        Pair(NamedTextColor.LIGHT_PURPLE, "핑크색"),
        Pair(NamedTextColor.RED, "빨간색"),
        Pair(NamedTextColor.GOLD, "주황색"),
        Pair(NamedTextColor.YELLOW, "노란색"),
        Pair(NamedTextColor.GREEN, "초록색"),
        Pair(NamedTextColor.DARK_PURPLE, "보라색"),
        Pair(NamedTextColor.DARK_BLUE, "남색"),
        Pair(NamedTextColor.BLUE, "파란색")
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
        worldBorder.setCenter(0.5, 0.5)

        worldBorder.size = when (environment) {
            World.Environment.NORMAL, World.Environment.NETHER -> 3200.0
            World.Environment.THE_END -> 500.0
            World.Environment.CUSTOM -> 59999968.0
        }

        setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
        setGameRule(GameRule.KEEP_INVENTORY, true)
        difficulty = Difficulty.EASY
    }

    val Player.gamePlayerData
        get() = ChasingTailsGameManager.gamePlayers.find { it.player.uniqueId == uniqueId }

    var OfflinePlayer.lastLocation: Location?
        get() = plugin.config.getLocation("last_location.${uniqueId}")
        set(value) {
            plugin.config.set("last_location.${uniqueId}", value)
        }

    var initEndSpawn: Location? = null

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
}
