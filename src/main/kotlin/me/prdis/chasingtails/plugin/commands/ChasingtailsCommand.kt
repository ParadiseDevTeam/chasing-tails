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

package me.prdis.chasingtails.plugin.commands

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mojang.brigadier.Command
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import me.prdis.chasingtails.plugin.managers.ChasingTailsGameManager.isRunning
import me.prdis.chasingtails.plugin.managers.ChasingTailsGameManager.startGame
import me.prdis.chasingtails.plugin.managers.ChasingTailsGameManager.stopGame
import me.prdis.chasingtails.plugin.objects.ChasingTailsUtils.plugin
import me.prdis.chasingtails.plugin.objects.ChasingTailsUtils.server
import kotlinx.coroutines.delay
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.GameMode

/**
 * @author aroxu, DytroC, ContentManager
 */

@Suppress("UnstableApiUsage")
object ChasingtailsCommand {
    fun registerCommand() {
        plugin.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            val registrar = event.registrar()
            val ct = Commands.literal("chasingtails").requires { it.sender.isOp }

            ct.executes {
                val ctx = it.source.sender
                ctx.sendMessage(
                    text(
                        """
                    꼬리잡기 (Chasing-tails) 플러그인
                    by Paradise Dev Team
                    -----------------------------
                    도움말
                    - 게임 시작: /chasingtails start
                    - 게임 종료: /chasingtails stop
                    
                    ※ /ct 로 축약 사용 가능합니다.
                """.trimIndent()
                    )
                )

                Command.SINGLE_SUCCESS
            }

            ct.then(Commands.literal("start").executes {
                val ctx = it.source.sender

                if (server.onlinePlayers.filter { player -> player.gameMode != GameMode.SPECTATOR }.size !in 7..8) {
                    ctx.sendMessage(text("플레이어의 수가 7 ~ 8명 이어야합니다.", NamedTextColor.RED))
                } else {
                    if (!isRunning) {
                        plugin.launch {
                            server.onlinePlayers.forEach { player ->
                                player.sendMessage(text("게임 플레이 도중 버그는 언제든지 발생 할 수 있음을 미리 고지합니다. 버그를 신고하는 경우, 양식에 맞춰 신고해주세요.", NamedTextColor.RED))
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
                        ctx.sendMessage(text("이미 게임이 진행중입니다.", NamedTextColor.RED))
                    }
                }

                Command.SINGLE_SUCCESS
            })

            ct.then(Commands.literal("stop").executes {
                val ctx = it.source.sender

                if (isRunning) {
                    stopGame()
                    ctx.sendMessage(text("게임을 종료합니다.", NamedTextColor.GREEN))
                } else {
                    ctx.sendMessage(text("게임이 진행중이지 않습니다.", NamedTextColor.RED))
                }

                Command.SINGLE_SUCCESS
            })

            registrar.register(ct.build(), "A chasing tails command.", listOf("ct"))
        }
    }
}