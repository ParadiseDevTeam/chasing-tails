package me.aroxu.chasingtails.plugin.events

import com.destroystokyo.paper.event.player.PlayerTeleportEndGatewayEvent
import me.aroxu.chasingtails.plugin.objects.ChasingTailsUtils.gamePlayerData
import me.aroxu.chasingtails.plugin.objects.ChasingTailsUtils.initEndSpawn
import me.aroxu.chasingtails.plugin.objects.ChasingTailsUtils.lastLocation
import me.aroxu.chasingtails.plugin.objects.ChasingTailsUtils.scoreboard
import me.aroxu.chasingtails.plugin.objects.ChasingTailsUtils.server
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityTargetLivingEntityEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerPortalEvent
import org.bukkit.event.player.PlayerQuitEvent

object PlayerEvent : Listener {

    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        player.noDamageTicks = 0
    }

    @EventHandler
    fun PlayerQuitEvent.onQuit() {
        player.lastLocation = player.location
    }

    @EventHandler
    fun PlayerMoveEvent.onMove() {
        val gamePlayer = player.gamePlayerData ?: return
        if (gamePlayer.isDeadTemporarily && hasChangedBlock()) {
            isCancelled = true
        }
    }

    @EventHandler
    fun PlayerTeleportEndGatewayEvent.onPlayerTeleportEndGateway() {
        isCancelled = true
    }

    @EventHandler
    fun PlayerChangedWorldEvent.onChangedWorld() {
        server.onlinePlayers.filter { scoreboard.getTeam(player.name)?.entries?.contains(it.name) == true }
            .forEach {
                it.teleportAsync(player.location)
            }
    }

    @EventHandler
    fun PlayerPortalEvent.onPlayerPortal() {
        if (from.world.environment == World.Environment.NORMAL && to.world.environment == World.Environment.THE_END && initEndSpawn == null) {
            initEndSpawn = to

            return
        }

        if (from.world.environment == World.Environment.NORMAL && to.world.environment == World.Environment.NETHER) {
            to = Location(to.world, from.blockX.toDouble(), to.blockY.toDouble(), from.blockZ.toDouble())
        } else if (from.world.environment == World.Environment.NETHER && to.world.environment == World.Environment.NORMAL) {
            to = Location(to.world, from.blockX.toDouble(), to.blockY.toDouble(), from.blockZ.toDouble())
        }
    }

    @EventHandler
    fun PlayerKickEvent.onKick() {
        if (cause == PlayerKickEvent.Cause.FLYING_PLAYER) isCancelled = true
    }

    @EventHandler
    fun EntityTargetLivingEntityEvent.onTarget() {
        if (target is Player) {
            val gamePlayer = (target as Player).gamePlayerData ?: return
            if (gamePlayer.isDeadTemporarily) {
                isCancelled = true
            }
        }
    }

    @EventHandler
    fun BlockPlaceEvent.onPlace() {
        val gamePlayer = player.gamePlayerData ?: return
        if (gamePlayer.isDeadTemporarily) isCancelled = true
    }

    @EventHandler
    fun BlockBreakEvent.onBreak() {
        val gamePlayer = player.gamePlayerData ?: return
        if (gamePlayer.isDeadTemporarily) isCancelled = true
    }
}