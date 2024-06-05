package me.aroxu.chasingtails.plugin.objects

import me.aroxu.chasingtails.plugin.ChasingtailsPlugin
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Difficulty
import org.bukkit.GameRule
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Scoreboard

object ChasingTailsUtils {
    val plugin = ChasingtailsPlugin.instance
    val server = plugin.server

    val scoreboard = server.scoreboardManager.newScoreboard.apply {
        reinitializeScoreboard()
    }

    fun Scoreboard.reinitializeScoreboard() {
        colorList.forEachIndexed { index, color ->
            registerNewTeam("team$index").apply {
                color(color)
            }
        }
    }

    val colorList = listOf(
        NamedTextColor.LIGHT_PURPLE,
        NamedTextColor.RED,
        NamedTextColor.GOLD,
        NamedTextColor.YELLOW,
        NamedTextColor.GREEN,
        NamedTextColor.BLUE,
        NamedTextColor.DARK_BLUE,
        NamedTextColor.DARK_PURPLE,

        // OTHER FOUR COLORS UNDECIDED
    )

    fun manageWorld(world: World) = world.apply {
        worldBorder.setCenter(0.5, 0.5)

        worldBorder.size = when (environment) {
            World.Environment.NORMAL, World.Environment.NETHER -> 3200.0
            World.Environment.THE_END -> 500.0
            World.Environment.CUSTOM -> 59999968.0
        }

        setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
        setGameRule(GameRule.KEEP_INVENTORY, true)
        difficulty = Difficulty.EASY
    }

    val Player.gamePlayerData
        get() = ChasingTailsGame.gamePlayers.find { it.uuid == uniqueId }

    var OfflinePlayer.lastLocation: Location?
        get() = plugin.config.getLocation("last_location.${uniqueId}")
        set(value) {
            plugin.config.set("last_location.${uniqueId}", value)
        }

    var initEndSpawn: Location? = null
}
