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

package `is`.prd.chasingtails.plugin.commands

import cloud.commandframework.Command
import cloud.commandframework.CommandManager
import com.github.shynixn.mccoroutine.bukkit.launch
import `is`.prd.chasingtails.plugin.managers.ChasingTailsGameManager.isRunning
import `is`.prd.chasingtails.plugin.managers.ChasingTailsGameManager.startGame
import `is`.prd.chasingtails.plugin.managers.ChasingTailsGameManager.stopGame
import `is`.prd.chasingtails.plugin.objects.ChasingTailsUtils.plugin
import `is`.prd.chasingtails.plugin.objects.ChasingTailsUtils.server
import kotlinx.coroutines.delay
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.GameMode
import org.bukkit.command.CommandSender

/**
 * @author aroxu, DytroC, ContentManager
 */

object ChasingtailsCommand {
    fun registerCommand(manager: CommandManager<CommandSender>): Command.Builder<CommandSender> {
        val builder = manager.commandBuilder("chasingtails", "ct")

        manager.command(builder.literal("start")
            .permission { sender -> sender.isOp }
            .handler { ctx ->
                if (server.onlinePlayers.filter { player -> player.gameMode != GameMode.SPECTATOR }.size !in 7..8) {
                    ctx.sender.sendMessage(text("플레이어의 수가 7 ~ 8명 이어야합니다.", NamedTextColor.RED))
                } else {
                    if (!isRunning) {
                        plugin.launch {
                            server.onlinePlayers.forEach { player ->
                                player.sendMessage(text("게임 플레이 도중 버그는 언제든지 발생 할 수 있음을 미리 고지합니다.", NamedTextColor.RED))
                                player.sendMessage(text("3초 후 게임이 시작됩니다."))
                            }
                            delay(3000)
                            server.onlinePlayers.forEach { player ->
                                repeat(100) {
                                    player.sendMessage(text("\n"))
                                }
                            }

                            startGame()
                            server.onlinePlayers.forEach { player ->
                                player.sendMessage(text("게임을 시작합니다.", NamedTextColor.GREEN))
                            }
                        }
                    } else {
                        ctx.sender.sendMessage(text("이미 게임이 진행중입니다.", NamedTextColor.RED))
                    }
                }
            }
        )

        manager.command(builder.literal("stop")
            .permission { sender -> sender.isOp }
            .handler { ctx ->
                if (isRunning) {
                    stopGame()
                    ctx.sender.sendMessage(text("게임을 종료합니다.", NamedTextColor.GREEN))
                } else {
                    ctx.sender.sendMessage(text("게임이 진행중이지 않습니다.", NamedTextColor.RED))
                }
            }
        )

        return builder
    }
}