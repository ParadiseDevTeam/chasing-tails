package me.aroxu.chasingtails.plugin.commands

import cloud.commandframework.Command
import cloud.commandframework.CommandManager
import cloud.commandframework.arguments.standard.DoubleArgument
import cloud.commandframework.bukkit.parsers.WorldArgument
import cloud.commandframework.bukkit.parsers.location.Location2DArgument
import me.aroxu.chasingtails.plugin.objects.ChasingtailObject.plugin
import net.kyori.adventure.text.Component.text
import org.bukkit.World
import org.bukkit.command.CommandSender
import java.math.BigDecimal
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt


object ChasingtailsCommand {
    fun registerCommand(manager: CommandManager<CommandSender>): Command.Builder<CommandSender> {
        val root = manager.commandBuilder("chasingtail", "ct")

        manager.command(root.literal("worldborder", "wb")
            .handler { ctx ->
                plugin.server.worlds.forEach {
                    ctx.sender.sendMessage(
                        text(
                            "세계 ${it.name}의 크기: ${BigDecimal(it.worldBorder.size)} (${
                                BigDecimal(sqrt(it.worldBorder.size).roundToInt())
                            } x ${BigDecimal(sqrt(it.worldBorder.size).roundToInt())})"
                        )
                    )
                }
            }
        )
        manager.command(root.literal("worldborder", "wb")
            .literal("setBySize")
            .argument(WorldArgument.of("world"))
            .argument(DoubleArgument.of("size"))
            .argument(Location2DArgument.optional("location"))
            .handler { ctx ->
                val world = ctx.get<World>("world")
                val size = ctx.get<Double>("size")
                val location = ctx.getOrDefault("location", world.spawnLocation)
                val worldBorder = world.worldBorder
                worldBorder.center = location
                worldBorder.size = sqrt(size)
                ctx.sender.sendMessage(text("세계 ${world.name}의 월드 보더 크기를 ${sqrt(size)} x ${sqrt(size)} ($size)으로 설정하였습니다."))
            }
        )

        manager.command(root.literal("worldborder", "wb")
            .literal("setBySide")
            .argument(WorldArgument.of("world"))
            .argument(DoubleArgument.of("size"))
            .argument(Location2DArgument.optional("location"))
            .handler { ctx ->
                val world = ctx.get<World>("world")
                val size = ctx.get<Double>("size")
                val location = ctx.getOrDefault("location", world.spawnLocation)
                val worldBorder = world.worldBorder
                worldBorder.center = location
                worldBorder.size = size.pow(2)
                ctx.sender.sendMessage(text("세계 ${world.name}의 월드 보더 크기를 $size x $size (${size.pow(2)})으로 설정하였습니다."))
            }
        )
        return root
    }
}