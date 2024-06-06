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

package `is`.prd.chasingtails.plugin.managers

import `is`.prd.chasingtails.plugin.events.HuntingEvent
import `is`.prd.chasingtails.plugin.events.GameManageEvent
import `is`.prd.chasingtails.plugin.tasks.ChasingTailsTasks
import `is`.prd.chasingtails.plugin.objects.ChasingTailsUtils
import `is`.prd.chasingtails.plugin.objects.ChasingTailsUtils.plugin
import `is`.prd.chasingtails.plugin.objects.ChasingTailsUtils.reinitializeScoreboard
import `is`.prd.chasingtails.plugin.objects.ChasingTailsUtils.scoreboard
import `is`.prd.chasingtails.plugin.objects.ChasingTailsUtils.server
import `is`.prd.chasingtails.plugin.objects.GamePlayer
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.event.HandlerList
import java.util.LinkedList
import kotlin.random.Random

/**
 * @author aroxu, DytroC, ContentManager
 */

object ChasingTailsGameManager {
    var isRunning = false
        set(value) {
            plugin.config.set("isRunning", value)

            field = value
        }

    val gamePlayers = LinkedList<GamePlayer>()
    val mainMasters = LinkedList<GamePlayer>()

    var currentTick = 0

    fun startGame() {
        if (!isRunning) {
            isRunning = true

            scoreboard.reinitializeScoreboard()

            gamePlayers.addAll(server.onlinePlayers.filter { it.gameMode != GameMode.SPECTATOR }
                .map { GamePlayer(it) }.shuffled())
            mainMasters.addAll(gamePlayers)

            server.worlds.forEach(ChasingTailsUtils::manageWorld)

            gamePlayers.forEachIndexed { index, gamePlayer ->
                val player = gamePlayer.player
                player.scoreboard = scoreboard.also {
                    it.getTeam("team$index")?.addPlayer(player)
                }

                val x = Random.nextInt(-750, 750)
                val z = Random.nextInt(-750, 750)

                player.teleportAsync(
                    Location(
                        player.world,
                        x + 0.5,
                        player.world.getHighestBlockYAt(x, z) + 1.0,
                        z + 0.5,
                    )
                )

                player.restoreDefaults()
            }

            gamePlayers.forEach { player ->
                player.sendMessage(
                    text("당신의 팀: ", NamedTextColor.YELLOW)
                        .append(text(player.koreanColor, player.color))
                )

                player.sendMessage(
                    text("당신의 타겟은 ", NamedTextColor.YELLOW).append(
                        text(
                            player.target
                                .koreanColor, player.target.color
                        )
                    ).append(text("입니다.", NamedTextColor.YELLOW))
                )
            }
        } else {
            // TODO: RESTORE PLAYER DATA
        }

        server.pluginManager.registerEvents(HuntingEvent, plugin)
        server.pluginManager.registerEvents(GameManageEvent, plugin)

        ChasingTailsTasks.startTasks()
    }

    fun stopGame() {
        gamePlayers.clear()
        mainMasters.clear()

        currentTick = 0

        HandlerList.unregisterAll(HuntingEvent)
        HandlerList.unregisterAll(GameManageEvent)

        ChasingTailsTasks.stopTasks()

        scoreboard.teams.forEach { it.unregister() }

        server.onlinePlayers.forEach { it.restoreDefaults() }

        server.worlds.forEach { world ->
            world.entities.filterIsInstance<TextDisplay>().forEach {
                it.remove()
            }
        }

        isRunning = false
    }

    private fun Player.restoreDefaults() {
        inventory.clear()
        getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 20.0

        health = 20.0
        foodLevel = 20
        saturation = 3F
        exhaustion = 0F
    }
}
