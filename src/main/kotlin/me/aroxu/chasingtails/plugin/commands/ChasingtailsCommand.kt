package me.aroxu.chasingtails.plugin.commands

import cloud.commandframework.Command
import cloud.commandframework.CommandManager
import cloud.commandframework.arguments.standard.BooleanArgument
import me.aroxu.chasingtails.plugin.objects.ChasingTailsGame
import me.aroxu.chasingtails.plugin.objects.ChasingTailsGame.isRunning
import me.aroxu.chasingtails.plugin.objects.ChasingTailsGame.startGame
import me.aroxu.chasingtails.plugin.objects.ChasingTailsGame.stopGame
import me.aroxu.chasingtails.plugin.objects.ChasingTailsSettings
import me.aroxu.chasingtails.plugin.objects.ChasingTailsUtils.plugin
import me.aroxu.chasingtails.plugin.objects.ChasingTailsUtils.server
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.GameMode
import org.bukkit.command.CommandSender


object ChasingtailsCommand {
    fun registerCommand(manager: CommandManager<CommandSender>): Command.Builder<CommandSender> {
        val builder = manager.commandBuilder("chasingtail", "ct")

        manager.command(builder.literal("start")
            .permission { sender -> sender.isOp }
            .handler { ctx ->
                server.scheduler.runTask(plugin, Runnable {
                    if (server.onlinePlayers.filter { it.gameMode != GameMode.SPECTATOR }.size !in 7..8) {
                        ctx.sender.sendMessage(text("플레이어의 수가 7명에서 8명이여야 합니다.", NamedTextColor.RED))
                    } else {
                        if (!isRunning) {
                            startGame()
                            server.onlinePlayers.forEach {
                                it.sendMessage(text("게임을 시작합니다.", NamedTextColor.GREEN))
                            }
                        } else {
                            ctx.sender.sendMessage(text("이미 게임이 진행중입니다.", NamedTextColor.RED))
                        }
                    }
                })
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

        manager.command(
            builder.literal("settings")
                .literal("targeterAttackable")
                .argument(BooleanArgument.of("value")) { "타겟 → 타게터 공격 여부를 설정합니다." }
                .handler { ctx ->
                    val argumentValue = ctx.get<Boolean>("value")

                    ChasingTailsSettings.targeterAttackable = argumentValue
                    ctx.sender.sendMessage(text("타겟 → 타게터 공격 여부: $argumentValue", NamedTextColor.GREEN))
                    ctx.sender.sendMessage(text("성공적으로 설정을 변경하였습니다.", NamedTextColor.WHITE))
                }
        )

        return builder
    }
}