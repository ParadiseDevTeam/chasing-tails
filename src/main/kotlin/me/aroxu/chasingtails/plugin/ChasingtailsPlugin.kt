package me.aroxu.chasingtails.plugin

import cloud.commandframework.execution.CommandExecutionCoordinator
import cloud.commandframework.paper.PaperCommandManager
import me.aroxu.chasingtails.plugin.commands.ChasingtailsCommand
import me.aroxu.chasingtails.plugin.events.HuntingEvent
import me.aroxu.chasingtails.plugin.objects.ChasingTailsGame.isRunning
import me.aroxu.chasingtails.plugin.objects.ChasingTailsGame.startGame
import org.bukkit.plugin.java.JavaPlugin
import java.util.function.Function

class ChasingtailsPlugin : JavaPlugin() {
    companion object {
        lateinit var instance: ChasingtailsPlugin
            private set
    }

    override fun onEnable() {
        instance = this

        isRunning = config.getBoolean("isRunning")
        if (isRunning) {
            startGame()
            logger.warning("이전 서버 종료 때 게임이 진행중이었습니다. 자동으로 게임이 재개되어 시작 명령어를 입력하실 필요가 없습니다.")
        }

        val commandManager = PaperCommandManager(
            this,
            CommandExecutionCoordinator.simpleCoordinator(),
            Function.identity(),
            Function.identity()
        )
        commandManager.command(ChasingtailsCommand.registerCommand(commandManager))
    }

    override fun onDisable() {
        saveConfig()
    }
}
