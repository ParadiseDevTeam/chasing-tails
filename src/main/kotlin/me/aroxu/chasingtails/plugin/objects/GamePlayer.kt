package me.aroxu.chasingtails.plugin.objects

import me.aroxu.chasingtails.plugin.objects.ChasingTailsGame.gamePlayers
import me.aroxu.chasingtails.plugin.objects.ChasingTailsGame.mainMasters
import me.aroxu.chasingtails.plugin.objects.ChasingTailsUtils.scoreboard
import me.aroxu.chasingtails.plugin.objects.ChasingTailsUtils.server
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Color
import org.bukkit.EntityEffect
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.attribute.Attribute
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Display
import org.bukkit.entity.Mob
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.NumberConversions
import java.util.UUID

class GamePlayer(val uuid: UUID) {
    val player: Player?
        get() = server.getPlayer(uuid)

    val offlinePlayer: OfflinePlayer
        get() = server.getOfflinePlayer(uuid)

    val color get() = scoreboard.getPlayerTeam(offlinePlayer)?.color() ?: NamedTextColor.WHITE

    val name get() = offlinePlayer.name ?: ""

    val coloredName get() = text(name, color)

    val target
        get() = mainMasters[(mainMasters.indexOf((master ?: this)) + 1) % mainMasters.size]

    var master: GamePlayer? = null

    var tooFar: Boolean = false

    val isDeadTemporarily
        get() = temporaryDeathDuration > 0

    var lastlyReceivedDamage: Pair<GamePlayer, Int>? = null
    var temporaryDeathDuration = 0
        set(value) {
            val current = player
            if (current != null) {
                if (value <= 0) {
                    deathTimer?.remove()
                    deathTimer = null

                    current.playEffect(EntityEffect.TOTEM_RESURRECT)
                    current.addPotionEffects(
                        listOf(
                            PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 5, 1, false, false, false),
                            PotionEffect(PotionEffectType.REGENERATION, 20 * 45, 1, false, false, false),
                            PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 40, 0, false, false, false)
                        )
                    )

                    sendMessage(text("부활하셨습니다!"))

                    val masterLocation = master?.player?.location
                    if (masterLocation != null) {
                        initializeAsSlave()
                        current.teleport(masterLocation)
                    }
                } else {
                    (deathTimer ?: current.world.spawn(
                        current.location.add(0.0, 2.3, 0.0),
                        TextDisplay::class.java,
                    ) {
                        it.isPersistent = true
                        it.alignment = TextDisplay.TextAlignment.CENTER
                        it.billboard = Display.Billboard.CENTER
                    }.also {
                        deathTimer = it
                    }).text(
                        text("부활까지 ${NumberConversions.ceil(value / 20.0)}초", NamedTextColor.YELLOW)
                    )
                }

                field = value
            }
        }

    private var deathTimer: TextDisplay? = null

    fun enslave(slave: GamePlayer, inherited: Boolean = false) {
        slave.master = this
        mainMasters.remove(slave)

        equipSlaveArmor(slave.player ?: return)

        if (!inherited) {
            gamePlayers.forEach {
                if (it.master == slave) enslave(it, true)
            }

            slave.initializeAsSlave()
            slave.player?.let {
                scoreboard.getPlayerTeam(it)?.unregister()
                it.playEffect(EntityEffect.TOTEM_RESURRECT)
                it.sendMessage(
                    text("처치당하셨습니다! 앞으로 ").append(coloredName)
                        .append(text("의 꼬리가 됩니다.", NamedTextColor.WHITE))
                )

                it.inventory.addItem(ItemStack(Material.COMPASS))

                sendMessage(text("목표를 처치하는데 성공하셨습니다! ${it.name}님이 꼬리가 됩니다."))
            }
        }

        scoreboard.getPlayerTeam(offlinePlayer)?.addPlayer(slave.offlinePlayer)

        player?.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = (20 - (
            (gamePlayers.count { it.master == this }) * 2
        )).toDouble()
    }

    fun sendMessage(message: ComponentLike) = player?.sendMessage(message)
    fun alert(message: String) = sendMessage(text(message, NamedTextColor.RED))

    override fun equals(other: Any?) = other is GamePlayer && other.uuid == uuid
    override fun hashCode() = uuid.hashCode()

    fun initializeAsSlave() = player?.apply {
        health = 10.0
        getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 10.0

        foodLevel = 20
        saturation = 3F
        exhaustion = 0F
    }

    fun equipSlaveArmor(slave: Player) = slave.inventory.apply {
        helmet = ItemStack(Material.LEATHER_HELMET).applyColorToArmor()
        chestplate = ItemStack(Material.LEATHER_CHESTPLATE).applyColorToArmor()
        leggings = ItemStack(Material.LEATHER_LEGGINGS).applyColorToArmor()
        boots = ItemStack(Material.LEATHER_BOOTS).applyColorToArmor()
    }

    private fun ItemStack.applyColorToArmor() = apply {
        editMeta {
            if (it is LeatherArmorMeta) it.setColor(Color.fromRGB(color.value()))
            it.isUnbreakable = true
            it.addItemFlags(*ItemFlag.values())
            it.addEnchant(Enchantment.BINDING_CURSE, 1, true)
            it.displayName(text("꼬리 복장", NamedTextColor.YELLOW))
        }
    }

    fun temporarilyKillPlayer(duration: Int) {
        player?.apply {
            health = getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0
            foodLevel = 20
            saturation = 3F
            exhaustion = 0F
            world.getNearbyLivingEntities(location, 30.0).forEach {
                if (it is Mob) {
                    if (it.target?.uniqueId == uuid) it.target = null
                }
            }
        }

        temporaryDeathDuration = duration
        tooFar = false
    }
}