package me.aroxu.chasingtails.plugin.objects

import me.aroxu.chasingtails.plugin.objects.ChasingTailsUtils.plugin

object ChasingTailsSettings {
    var targeterAttackable: Boolean
        get() = plugin.config.getBoolean("targeterAttackable")
        set(value) = plugin.config.set("targeterAttackable", value)
}