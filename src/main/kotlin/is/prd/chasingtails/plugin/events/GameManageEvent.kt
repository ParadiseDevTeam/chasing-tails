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

package `is`.prd.chasingtails.plugin.events

import com.destroystokyo.paper.event.player.PlayerTeleportEndGatewayEvent
import `is`.prd.chasingtails.plugin.managers.ChasingTailsGameManager.gamePlayers
import `is`.prd.chasingtails.plugin.managers.ChasingTailsGameManager.haltGame
import `is`.prd.chasingtails.plugin.managers.ChasingTailsGameManager.mainMasters
import `is`.prd.chasingtails.plugin.objects.ChasingTailsUtils.admins
import `is`.prd.chasingtails.plugin.objects.ChasingTailsUtils.checkPlayers
import `is`.prd.chasingtails.plugin.objects.ChasingTailsUtils.gamePlayerData
import `is`.prd.chasingtails.plugin.objects.ChasingTailsUtils.initEndSpawn
import `is`.prd.chasingtails.plugin.objects.ChasingTailsUtils.lastLocation
import `is`.prd.chasingtails.plugin.objects.ChasingTailsUtils.notifyTeam
import `is`.prd.chasingtails.plugin.objects.ChasingTailsUtils.plugin
import `is`.prd.chasingtails.plugin.objects.ChasingTailsUtils.restoreGamePlayer
import `is`.prd.chasingtails.plugin.objects.ChasingTailsUtils.scoreboard
import `is`.prd.chasingtails.plugin.objects.ChasingTailsUtils.server
import `is`.prd.chasingtails.plugin.objects.GamePlayer
import `is`.prd.chasingtails.plugin.tasks.ChasingTailsTasks.startTasks
import `is`.prd.chasingtails.plugin.tasks.ChasingTailsTasks.stopTasks
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.Component.translatable
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityTargetLivingEntityEvent
import org.bukkit.event.player.*

/**
 * @author aroxu, DytroC, ContentManager
 */

object GameManageEvent : Listener {

    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        val gamePlayer = GamePlayer(player)

        val configGamePlayers = plugin.config.getStringList("gamePlayers")
        val configMainMasters = plugin.config.getStringList("mainMasters")

        player.noDamageTicks = 0

        if (player.uniqueId.toString() !in admins) {
            joinMessage(
                translatable("multiplayer.player.joined", player.displayName().color(null)).color(NamedTextColor.YELLOW)
            )
        }
        else joinMessage(null)

        if (player.uniqueId.toString() in configGamePlayers && gamePlayers.firstOrNull { it.player.uniqueId == player.uniqueId } == null) gamePlayers.add(
            gamePlayer
        )
        if (player.uniqueId.toString() in configMainMasters && mainMasters.firstOrNull { it.player.uniqueId == player.uniqueId } == null) mainMasters.add(
            gamePlayer
        )

        if (haltGame) {
            val check = checkPlayers()

            if (check) {
                player.sendMessage(text("기존 게임 플레이 인원이 전부 접속되지 않아 현재 게임이 정지되어있습니다.", NamedTextColor.GRAY))
            } else {
                haltGame = false

                server.onlinePlayers.forEach {
                    it.restoreGamePlayer()
                    it.sendMessage(text("게임을 재개합니다.", NamedTextColor.GREEN))

                }

                server.onlinePlayers.forEach {
                    it.gamePlayerData?.notifyTeam()
                }

                startTasks()
            }
        }

        if (player.gamePlayerData == null) {
            player.gameMode = GameMode.SPECTATOR
            player.sendMessage(text("현재 게임이 진행중이어 관전자로 변경되었습니다!", NamedTextColor.GRAY))
        }
    }

    @EventHandler
    fun PlayerQuitEvent.onQuit() {
        player.lastLocation = player.location

        if (player.uniqueId.toString() !in admins) {
            quitMessage(
                translatable("multiplayer.player.left", player.displayName().color(null)).color(NamedTextColor.YELLOW)
            )
        }
        else quitMessage(null)

        haltGame = true

        server.onlinePlayers.forEach {
            it.sendMessage(
                text("! ", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD)
                    .append(
                        text("플레이를 진행하는 유저가 접속을 종료하여 재접속 이전까지 일시적으로 게임을 중단합니다.", NamedTextColor.YELLOW).decoration(
                            TextDecoration.BOLD,
                            false
                        )
                    )
            )
        }

        stopTasks()
    }

    @EventHandler
    fun PlayerMoveEvent.onMove() {
        val gamePlayer = player.gamePlayerData ?: return
        if (gamePlayer.isDeadTemporarily && hasChangedBlock()) {
            isCancelled = true
        }

        if (haltGame) isCancelled = true
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

            if (haltGame) isCancelled = true
        }
    }

    @EventHandler
    fun BlockPlaceEvent.onPlace() {
        val gamePlayer = player.gamePlayerData ?: return
        if (gamePlayer.isDeadTemporarily) isCancelled = true
        if (haltGame) isCancelled = true
    }

    @EventHandler
    fun BlockBreakEvent.onBreak() {
        val gamePlayer = player.gamePlayerData ?: return
        if (gamePlayer.isDeadTemporarily) isCancelled = true
        if (haltGame) isCancelled = true
    }
}