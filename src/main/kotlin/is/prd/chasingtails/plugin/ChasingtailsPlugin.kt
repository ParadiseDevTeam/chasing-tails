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

package `is`.prd.chasingtails.plugin

import `is`.prd.chasingtails.plugin.commands.ChasingtailsCommand.registerCommand
import `is`.prd.chasingtails.plugin.managers.ChasingTailsGameManager.isRunning
import `is`.prd.chasingtails.plugin.managers.ChasingTailsGameManager.mainMasters
import `is`.prd.chasingtails.plugin.managers.ChasingTailsGameManager.startGame
import `is`.prd.chasingtails.plugin.objects.ChasingTailsUtils.gamePlayerData
import `is`.prd.chasingtails.plugin.objects.GamePlayer
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author aroxu, DytroC, ContentManager as Vector3
 */

class ChasingtailsPlugin : JavaPlugin() {
    companion object {
        lateinit var instance: ChasingtailsPlugin
            private set
    }

    override fun onEnable() {
        instance = this

        ConfigurationSerialization.registerClass(GamePlayer::class.java)

        isRunning = config.getBoolean("isRunning")
        if (isRunning) {
            startGame()
            logger.warning("이전 서버 종료 때 게임이 진행중이었습니다. 자동으로 게임이 재개되어 시작 명령어를 입력하실 필요가 없습니다.")
        }

        registerCommand()
    }

    override fun onDisable() {
        config.set("masterData", mainMasters)
        config.set("gamePlayerData", server.onlinePlayers.map { it.gamePlayerData })
        saveConfig()
    }
}
