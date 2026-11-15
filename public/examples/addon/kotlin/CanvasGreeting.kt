package example

import net.kyori.adventure.text.Component
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import zone.vao.craftcanvas.*

class CanvasGreeting : JavaPlugin() {
    private val registrations = mutableListOf<CanvasRegistration>()
    @Volatile private var canvas: CanvasService? = null

    override fun onEnable() {
        CanvasApi.whenReady(this).thenAccept { api ->
            registrations += api.registerAction(this, "greeting:hello", CanvasAction { player, _, _ ->
                player.sendMessage(Component.text("Hello from your addon!"))
            })
            val screen = CanvasScreenDefinition.builder("greeting:menu")
                .extendsScreen("frame")
                .title("Greeting")
                .variable("greeting", CanvasVariableType.STRING, "Welcome")
                .component("heading", mapOf("text" to "<aqua><bold>GREETING"))
                .component(CanvasComponentDefinition.text("welcome")
                    .position(112, 96).size(280, 24).layer(20)
                    .text("<white>{greeting}, {player.name}!"))
                .component(CanvasComponentDefinition.button("hello")
                    .position(112, 140).size(280, 28).layer(20).centered()
                    .asset(CanvasAssets.nativeImage("craftcanvas:button"))
                    .text("Say hello").tooltip("Click to receive a greeting")
                    .onClick(CanvasActions.callback("greeting:hello")))
                .build()
            registrations += api.registerScreen(this, screen)
            api.reload().thenAccept { result ->
                if (isEnabled && result.successful) canvas = api
                if (!result.successful) logger.warning("Menu registration failed: $result")
            }.exceptionally { error ->
                logger.warning("Menu preparation failed: ${error.message}")
                null
            }
        }.exceptionally { error ->
            logger.warning("CraftCanvas unavailable: ${error.message}")
            null
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return true
        val api = canvas
        if (api == null) {
            sender.sendMessage(Component.text("The menu is still loading."))
            return true
        }
        api.openAsync(sender, "greeting:menu", mapOf("greeting" to "Welcome"))
            .thenAccept { result ->
                if (!result.opened && !result.queued) logger.warning("Cannot open menu: ${result.status}")
            }.exceptionally { error ->
                logger.warning("Menu request failed: ${error.message}")
                null
            }
        return true
    }

    override fun onDisable() {
        canvas = null
        registrations.asReversed().forEach(CanvasRegistration::unregister)
        registrations.clear()
    }
}
