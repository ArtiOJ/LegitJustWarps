package dev.artifabrian.legitjustwarps

import io.papermc.paper.command.brigadier.BasicCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class LegitJustWarps : JavaPlugin() {

    private lateinit var file: File
    private lateinit var warps: YamlConfiguration

    override fun onEnable() {
        file = File(dataFolder, "warps.yml")
        warps = YamlConfiguration.loadConfiguration(file)

        registerCommand("setwarp", object : BasicCommand {
            override fun permission() = "ljw.setwarp"

            override fun execute(source: CommandSourceStack, args: Array<String>) {
                val player = source.executor as? Player ?: return
                val name = args.singleOrNull()?.lowercase()?.takeIf { "." !in it } ?: return usage(source, "/setwarp <warp>")
                val loc = player.location
                warps.set("$name.world", loc.world.name)
                warps.set("$name.x", loc.x)
                warps.set("$name.y", loc.y)
                warps.set("$name.z", loc.z)
                warps.save(file)
                player.sendMessage(Component.text("Warp $name set.", NamedTextColor.GREEN))
            }
        })

        registerCommand("delwarp", object : BasicCommand {
            override fun permission() = "ljw.delwarp"

            override fun execute(source: CommandSourceStack, args: Array<String>) {
                val name = args.singleOrNull()?.lowercase() ?: return usage(source, "/delwarp <warp>")
                if (!warps.contains(name)) return notFound(source, name)
                warps.set(name, null)
                warps.save(file)
                source.sender.sendMessage(Component.text("Warp $name deleted.", NamedTextColor.GREEN))
            }

            override fun suggest(source: CommandSourceStack, args: Array<String>) = complete(args)
        })

        registerCommand("warp", object : BasicCommand {
            override fun permission() = "ljw.warp"

            override fun execute(source: CommandSourceStack, args: Array<String>) {
                val player = source.executor as? Player ?: return
                val name = args.singleOrNull()?.lowercase() ?: return usage(source, "/warp <warp>")
                val section = warps.getConfigurationSection(name) ?: return notFound(source, name)
                val world = server.getWorld(section.getString("world") ?: "")
                    ?: return source.sender.sendMessage(Component.text("World for warp $name is not loaded.", NamedTextColor.RED))
                val current = player.location
                player.teleportAsync(Location(world, section.getDouble("x"), section.getDouble("y"), section.getDouble("z"), current.yaw, current.pitch))
            }

            override fun suggest(source: CommandSourceStack, args: Array<String>) = complete(args)
        })
    }

    private fun complete(args: Array<String>): Collection<String> {
        if (args.size > 1) return emptyList()
        val prefix = args.firstOrNull()?.lowercase() ?: ""
        return warps.getKeys(false).filter { it.startsWith(prefix) }
    }

    private fun usage(source: CommandSourceStack, text: String) =
        source.sender.sendMessage(Component.text("Usage: $text", NamedTextColor.RED))

    private fun notFound(source: CommandSourceStack, name: String) =
        source.sender.sendMessage(Component.text("Warp $name does not exist.", NamedTextColor.RED))
}
