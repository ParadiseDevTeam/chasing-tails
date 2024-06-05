package me.aroxu.chasingtails.plugin.events

import com.github.shynixn.mccoroutine.bukkit.launch
import kotlinx.coroutines.delay
import me.aroxu.chasingtails.plugin.objects.ChasingTailsGame
import me.aroxu.chasingtails.plugin.objects.ChasingTailsSettings.targeterAttackable
import me.aroxu.chasingtails.plugin.objects.ChasingTailsUtils.gamePlayerData
import me.aroxu.chasingtails.plugin.objects.ChasingTailsUtils.initEndSpawn
import me.aroxu.chasingtails.plugin.objects.ChasingTailsUtils.lastLocation
import me.aroxu.chasingtails.plugin.objects.ChasingTailsUtils.plugin
import me.aroxu.chasingtails.plugin.objects.ChasingTailsUtils.server
import me.aroxu.chasingtails.plugin.objects.DamageResult
import me.aroxu.chasingtails.plugin.objects.GamePlayer
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.entity.TNTPrimed
import org.bukkit.entity.Tameable
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.time.Duration

object HuntingEvent : Listener {
    @EventHandler
    fun PlayerInteractEvent.onInteract() {
        val gamePlayer = player.gamePlayerData ?: return
        if (action.isRightClick && item?.type == Material.DIAMOND && gamePlayer.master == null) {
            val target = gamePlayer.target.offlinePlayer

            val location = target.location ?: target.lastLocation

            if (location != null) {
                if (player.location.world == location.world) {
                    val eye = player.eyeLocation
                    val offset = location.add(0.0, 1.5, 0.0).toVector().subtract(eye.toVector()).normalize()

                    plugin.launch {
                        repeat(10) {
                            repeat(15) { distance ->
                                player.world.spawnParticle(
                                    Particle.SOUL_FIRE_FLAME,
                                    eye.clone().add(offset.clone().multiply(distance * 0.5)),
                                    1,
                                    0.0,
                                    0.0,
                                    0.0,
                                    0.0
                                )
                            }

                            delay(200)
                        }
                    }

                    player.playSound(Sound.sound(Key.key("block.note_block.bit"), Sound.Source.MASTER, 1000F, 1F))

                    item?.let { it.amount-- }
                } else {
                    player.sendActionBar(text(" -- 추적 오류: 플레이어와 같은 월드에 있지 않습니다. -- ", NamedTextColor.RED))
                }
            } else {
                player.sendActionBar(text(" -- 추적 오류: 플레이어의 위치를 찾을 수 없습니다. -- ", NamedTextColor.RED))
            }
        } else if (item?.type == Material.END_CRYSTAL && player.world.environment == World.Environment.THE_END) {
            isCancelled = true
        } else if (item?.type == Material.COMPASS && gamePlayer.master != null) {
            isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun EntityDamageByEntityEvent.onDamageByEntity() {
        val gamePlayer = (entity as? Player)?.gamePlayerData ?: return
        val attacker = when (val source = damager) {
            is Player -> source
            is Projectile -> source.shooter as? Player
            is Tameable -> source.owner as? Player
            is TNTPrimed -> source.source as? Player
            else -> null
        }?.gamePlayerData ?: return

        if (attacker == gamePlayer) return
        if (attacker.isDeadTemporarily) {
            isCancelled = true
            return
        }

        val result = processDamage(gamePlayer, attacker, true)

        isCancelled = result == DamageResult.DISALLOW
        if (result == DamageResult.ALLOW_ONLY_KNOCKBACK) damage = 0.0
    }

    @EventHandler
    fun EntityDamageEvent.onDamage() {
        val gamePlayer = (entity as? Player)?.gamePlayerData ?: return

        if (gamePlayer.isDeadTemporarily && cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun PlayerDeathEvent.onDeath() {
        val gamePlayer = entity.gamePlayerData ?: return

        isCancelled = true

        val master = gamePlayer.master

        val killer = (player.killer?.gamePlayerData ?: gamePlayer.lastlyReceivedDamage?.takeIf { (_, time) ->
            ChasingTailsGame.currentTick - time < (20 * 60)
        }?.first)

        val killerMaster = (killer?.master ?: killer).takeIf { gamePlayer != it }

        if (gamePlayer == killerMaster?.target && master == null) {
            if (gamePlayer.isDeadTemporarily) gamePlayer.temporaryDeathDuration = -99

            if (ChasingTailsGame.mainMasters.size == 2) {
                server.showTitle(
                    Title.title(
                        text(""),
                        killerMaster.coloredName.append(text("님이 우승하셨습니다!", NamedTextColor.WHITE)),
                        Title.Times.times(Duration.ofSeconds(0), Duration.ofSeconds(5), Duration.ofSeconds(0))
                    )
                )

                server.broadcast(text("게임을 종료합니다.", NamedTextColor.GREEN))
                ChasingTailsGame.stopGame()

                return
            }

            killerMaster.enslave(gamePlayer)
        } else {
            if (master != null) {
                gamePlayer.temporarilyKillPlayer(20 * 30)

                ChasingTailsGame.gamePlayers.forEach {
                    if (it.master == gamePlayer.master || it == gamePlayer.master) {
                        it.sendMessage(text("${gamePlayer.name}이(가) 사망했습니다. (30초 후 리스폰)"))
                    }
                }
            } else {
                if (player.world.environment == World.Environment.THE_END) {
                    initEndSpawn?.let { spawn -> player.teleportAsync(spawn) }
                }

                gamePlayer.temporarilyKillPlayer(120 * 20)
                gamePlayer.sendMessage(
                    text("사망하셨습니다! ", NamedTextColor.RED).append(
                        text(
                            "현 시점으로부터 2분 뒤에 부활합니다.",
                            NamedTextColor.WHITE
                        )
                    )
                )
            }
        }

        player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 40, 0, false, false, false))
    }

    private val damageableAlert
        get() = if (targeterAttackable) "타겟 플레이어와 그 플레이어의 꼬리, 자신의 헌터와 그 헌터의 꼬리, 자신의 꼬리 이외의 플레이어는 공격할 수 없습니다!"
        else "타겟 플레이어와 그 플레이어의 꼬리, 자신의 꼬리 이외의 플레이어는 공격할 수 없습니다!"

    private fun processDamage(damaged: GamePlayer, damager: GamePlayer, sendAlerts: Boolean = false) = if (damaged == damager.target) {
        damaged.lastlyReceivedDamage = damager to ChasingTailsGame.currentTick
        

        DamageResult.ALLOW
    } else {
        if (damaged.master == damager.target) (if (damaged.isDeadTemporarily) DamageResult.ALLOW_ONLY_KNOCKBACK else DamageResult.ALLOW)
        else if (damaged.target == damager) {
            if (damaged.isDeadTemporarily && sendAlerts) damager.alert("해당 플레이어는 임시 사망 상태입니다!")

            if (targeterAttackable && !damaged.isDeadTemporarily) DamageResult.ALLOW else DamageResult.ALLOW_ONLY_KNOCKBACK
        } else if (damaged.master?.target == damager) {
            if (damaged.isDeadTemporarily && sendAlerts) damager.alert("해당 플레이어는 임시 사망 상태입니다!")

            if (damaged.isDeadTemporarily) DamageResult.ALLOW_ONLY_KNOCKBACK else DamageResult.ALLOW
        } else if (damager.master != null) when {
            damaged.target == damager.master -> {
                if (damaged.isDeadTemporarily && sendAlerts) damager.alert("해당 플레이어는 임시 사망 상태입니다!")

                if (targeterAttackable && !damaged.isDeadTemporarily) DamageResult.ALLOW else DamageResult.ALLOW_ONLY_KNOCKBACK
            }

            damaged.master != null && damaged.master?.target == damager.master -> {
                if (damaged.isDeadTemporarily && sendAlerts) damager.alert("해당 플레이어는 임시 사망 상태입니다!")

                if (damaged.isDeadTemporarily) DamageResult.ALLOW_ONLY_KNOCKBACK else DamageResult.ALLOW
            }

            damaged == damager.master -> {
                if (sendAlerts) damager.alert("당신의 주인은 공격할 수 없습니다!")
                DamageResult.DISALLOW
            }

            else -> {
                if (sendAlerts) damager.alert(damageableAlert)
                DamageResult.DISALLOW
            }
        } else if (damaged.master == damager) {
            DamageResult.ALLOW
        } else {
            if (sendAlerts) damager.alert(damageableAlert)
            DamageResult.DISALLOW
        }
    }
}
