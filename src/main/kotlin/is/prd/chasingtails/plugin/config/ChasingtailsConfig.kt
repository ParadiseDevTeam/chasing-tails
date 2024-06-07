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

import `is`.prd.chasingtails.plugin.objects.ChasingTailsUtils.gamePlayerData
import `is`.prd.chasingtails.plugin.objects.ChasingTailsUtils.plugin
import `is`.prd.chasingtails.plugin.objects.ChasingTailsUtils.scoreboard
import `is`.prd.chasingtails.plugin.objects.ChasingTailsUtils.server

/**
 * @author aroxu, DytroC, ContentManager
 */

object ChasingtailsConfig {


    fun saveGameProgress() {
        server.onlinePlayers.forEach { player ->
            player.gamePlayerData?.let { gamePlayer ->
                val team = scoreboard.getPlayerTeam(gamePlayer.offlinePlayer)

                if (team != null) {
                    plugin.config.set("chasingtails.${player.uniqueId}.teamname", team.name)
                    plugin.config.set("chasingtails.${player.uniqueId}.teamcolor", team.color().value())
                    plugin.config.set(
                        "chasingtails.${player.uniqueId}.master",
                        gamePlayer.master?.player?.uniqueId?.toString()
                    )
                    plugin.config.set("chasingtails.${player.uniqueId}.")
                }
            }
        }

        plugin.saveConfig()
    }
}