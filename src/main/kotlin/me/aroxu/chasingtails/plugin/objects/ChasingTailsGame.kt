package me.aroxu.chasingtails.plugin.objects

import me.aroxu.chasingtails.plugin.events.HuntingEvent
import me.aroxu.chasingtails.plugin.events.PlayerEvent
import me.aroxu.chasingtails.plugin.objects.ChasingTailsUtils.plugin
import me.aroxu.chasingtails.plugin.objects.ChasingTailsUtils.reinitializeScoreboard
import me.aroxu.chasingtails.plugin.objects.ChasingTailsUtils.scoreboard
import me.aroxu.chasingtails.plugin.objects.ChasingTailsUtils.server
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.event.HandlerList
import java.util.LinkedList
import kotlin.random.Random

object ChasingTailsGame {
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
                .map { GamePlayer(it.uniqueId) }.shuffled())
            mainMasters.addAll(gamePlayers)

            server.worlds.forEach(ChasingTailsUtils::manageWorld)
        }

        gamePlayers.forEachIndexed { index, gamePlayer ->
            val player = gamePlayer.player ?: return@forEachIndexed
            player.scoreboard = scoreboard.also {
                it.getTeam("team$index")?.addPlayer(player)
            }

            val x = Random.nextInt(-750, 750)
            val z = Random.nextInt(-750, 750)

            player.teleport(
                Location(
                    player.world,
                    x + 0.5,
                    player.world.getHighestBlockYAt(x, z) + 1.0,
                    z + 0.5,
                )
            )

            player.sendMessage(
                text("당신의 팀: ", NamedTextColor.YELLOW)
                    .append(gamePlayer.coloredName)
            )

            player.sendMessage(
                text("당신의 타겟은 ", NamedTextColor.YELLOW).append(
                    gamePlayer.target
                        .coloredName
                ).append(text("입니다.", NamedTextColor.YELLOW))
            )


            player.restoreDefaults()
        }

        server.pluginManager.registerEvents(HuntingEvent, plugin)
        server.pluginManager.registerEvents(PlayerEvent, plugin)

        ChasingTailsTasks.startTasks()
    }

    fun stopGame() {
        gamePlayers.clear()
        mainMasters.clear()

        currentTick = 0

        HandlerList.unregisterAll(HuntingEvent)
        HandlerList.unregisterAll(PlayerEvent)

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
