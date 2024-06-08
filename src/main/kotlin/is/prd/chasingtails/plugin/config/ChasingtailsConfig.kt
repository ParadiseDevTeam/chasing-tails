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

package `is`.prd.chasingtails.plugin.config

import `is`.prd.chasingtails.plugin.managers.ChasingTailsGameManager.gamePlayers
import `is`.prd.chasingtails.plugin.managers.ChasingTailsGameManager.mainMasters
import `is`.prd.chasingtails.plugin.objects.ChasingTailsUtils.gamePlayerData
import `is`.prd.chasingtails.plugin.objects.ChasingTailsUtils.plugin
import `is`.prd.chasingtails.plugin.objects.ChasingTailsUtils.scoreboard
import `is`.prd.chasingtails.plugin.objects.ChasingTailsUtils.server

/**
 * @author aroxu, DytroC, ContentManager
 */

object ChasingtailsConfig {
    fun saveConfigGameProgress() {
        server.onlinePlayers.forEach { player ->
            player.gamePlayerData?.let { gamePlayer ->
                val team = scoreboard.getPlayerTeam(player)

                if (team != null) {
                    plugin.config.set(
                        "chasingtails.${player.uniqueId}",
                        GamePlayerData(
                            team.name,
                            team.color().value(),
                            gamePlayer.master?.uuid.toString(),
                            gamePlayer.deathTimer?.uniqueId.toString(),
                            gamePlayer.temporaryDeathDuration
                        )
                    )
                }
            }
        }

        plugin.config.set("gamePlayers", gamePlayers.map { it.player.uniqueId.toString() })
        plugin.config.set("mainMasters", mainMasters.map { it.player.uniqueId.toString() })

        plugin.saveConfig()
    }

    fun resetConfigGameProgress() {
        server.offlinePlayers.forEach { player ->
            plugin.config.set("chasingtails.${player.uniqueId}", null)
        }

        plugin.config.set("chasingtails", null)
        plugin.config.set("gamePlayers", null)
        plugin.config.set("mainMasters", null)

        plugin.saveConfig()
    }
}