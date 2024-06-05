package me.aroxu.chasingtails.plugin.objects

import com.github.shynixn.mccoroutine.bukkit.launch
import kotlinx.coroutines.delay
import me.aroxu.chasingtails.plugin.objects.ChasingTailsGame.gamePlayers
import me.aroxu.chasingtails.plugin.objects.ChasingTailsGame.mainMasters
import me.aroxu.chasingtails.plugin.objects.ChasingTailsUtils.lastLocation
import me.aroxu.chasingtails.plugin.objects.ChasingTailsUtils.plugin
import me.aroxu.chasingtails.plugin.objects.ChasingTailsUtils.server
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.NumberConversions
import kotlin.math.roundToInt

object ChasingTailsTasks {
    private val masterComponent = text("주인(")

    fun startTasks() {
        distancingTask()
        gameTick()
        heartbeatTask()
    }

    fun stopTasks() {
        server.scheduler.cancelTasks(plugin)
    }

    private fun gameTick() {
        server.scheduler.runTaskTimer(plugin, Runnable {
            ++ChasingTailsGame.currentTick

            gamePlayers.forEach {
                val master = it.master

                if (it.isDeadTemporarily) {
                    val value = --it.temporaryDeathDuration

                    if (master == null) {
                        it.player?.apply {
                            sendActionBar(
                                text("부활까지 ", NamedTextColor.GOLD)
                                    .append(text(NumberConversions.ceil(value / 20.0), NamedTextColor.YELLOW))
                                    .append(text("초 남았습니다!", NamedTextColor.GOLD))
                            )
                        }
                    }
                }

                it.player?.playerListName(text(it.name, NamedTextColor.WHITE))

                if (master != null) {
                    it.player?.apply {
                        val masterLocation = master.player?.location ?: master.offlinePlayer.lastLocation ?: return@forEach
                        compassTarget = masterLocation

                        if (location.world != master.player?.world) {
                            sendActionBar(
                                masterComponent.color(NamedTextColor.RED)
                                    .append(master.coloredName)
                                    .append(text(")과 같은 차원에 존재하지 않습니다!", NamedTextColor.RED))
                            )
                        } else if (it.tooFar) {
                            sendActionBar(
                                masterComponent.color(NamedTextColor.RED)
                                    .append(master.coloredName)
                                    .append(text(")과의 거리가 너무 멉니다!", NamedTextColor.RED))
                            )
                        } else {
                            sendActionBar(
                                masterComponent.color(NamedTextColor.GOLD)
                                    .append(master.coloredName)
                                    .append(
                                        text(
                                            ")과의 거리: ",
                                            NamedTextColor.GOLD
                                        )
                                    )
                                    .append(
                                        text(
                                            "${
                                                location.distance(masterLocation).roundToInt()
                                            }m",
                                            NamedTextColor.WHITE
                                        )
                                    )
                            )
                        }
                    }
                }
            }
        }, 0, 1)
    }

    private fun heartbeatTask() {
        server.scheduler.runTaskTimer(plugin, Runnable {
            mainMasters.forEach {
                val target = it.target.player
                val player = it.player ?: return@forEach

                if (target != null) {
                    if (player.location.world == target.location.world) {
                        val dist = player.location.distance(target.location)
                        val volume = when (dist) {
                            in 0.0..10.0 -> 1.0F
                            in 10.0..20.0 -> 0.8F
                            in 20.0..30.0 -> 0.6F
                            in 30.0..40.0 -> 0.4F
                            in 40.0..50.0 -> 0.2F
                            else -> 0.0F
                        }

                        if (dist <= 50.0) {
                            plugin.launch {
                                target.sendActionBar(
                                    text(
                                        "[ ♥ ]",
                                        NamedTextColor.RED
                                    ).decorate(TextDecoration.BOLD)
                                )
                                delay(200)
                                target.sendActionBar(text(""))
                            }

                            target.playSound(
                                Sound.sound(
                                    Key.key("entity.warden.heartbeat"),
                                    Sound.Source.MASTER,
                                    volume,
                                    1F
                                )
                            )
                        }
                    }
                }

            }
        }, 0, 20)
    }

    private fun distancingTask() {
        server.scheduler.runTaskTimer(plugin, Runnable {
            gamePlayers.forEach {
                val master = it.master?.player ?: return@forEach
                if (!it.isDeadTemporarily) {
                    val player = it.player ?: return@forEach
                    if (player.world != master.world || player.location.distance(master.location) > 50) {
                        player.damage(1.0)
                        it.tooFar = true
                    } else {
                        it.tooFar = false
                    }
                }
            }
        }, 0, 10)
    }
}