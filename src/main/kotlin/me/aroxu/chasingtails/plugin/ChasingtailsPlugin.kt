package me.aroxu.chasingtails.plugin

import cloud.commandframework.execution.CommandExecutionCoordinator
import cloud.commandframework.paper.PaperCommandManager
import me.aroxu.chasingtails.plugin.commands.ChasingtailsCommand
import me.aroxu.chasingtails.plugin.events.SampleEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.function.Function

class ChasingtailsPlugin : JavaPlugin() {
    companion object {
        lateinit var instance: ChasingtailsPlugin
            private set
    }

    override fun onEnable() {
        instance = this

        server.pluginManager.registerEvents(SampleEvent, this)

        val commandManager = PaperCommandManager(
            this,
            CommandExecutionCoordinator.simpleCoordinator(),
            Function.identity(),
            Function.identity()
        )
        commandManager.command(ChasingtailsCommand.registerCommand(commandManager))
    }
}
