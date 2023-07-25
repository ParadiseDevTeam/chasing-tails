package me.aroxu.chasingtails.plugin.objects

import me.aroxu.chasingtails.plugin.ChasingtailsPlugin

@Suppress("UNUSED", "MemberVisibilityCanBePrivate") // REMOVE SUPPRESS WHEN USING!
object ChasingtailObject {
    val plugin = ChasingtailsPlugin.instance
    val server = plugin.server
}
