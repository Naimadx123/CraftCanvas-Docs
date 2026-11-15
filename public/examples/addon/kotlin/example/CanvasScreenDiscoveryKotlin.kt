package example

import org.bukkit.entity.Player
import zone.vao.craftcanvas.*
import java.util.concurrent.CompletableFuture

/** Capture once when reading several declarations; safe on any thread. */
fun declaredScreens(canvas: CanvasService): Set<String> = canvas.screenCatalog().screens.keys

fun openDiscoveredHome(canvas: CanvasService, player: Player, greeting: String): CompletableFuture<CanvasOpenResult> {
    val catalog = canvas.screenCatalog()
    val home = catalog.screen("home")
        ?: return CompletableFuture.failedFuture(IllegalStateException("The home screen is unavailable"))
    val variable = home.variable("session.greeting")
    if (variable == null || variable.type != CanvasVariableType.STRING || !variable.accepts(greeting)) {
        return CompletableFuture.failedFuture(IllegalArgumentException("home needs a compatible session.greeting declaration"))
    }
    return canvas.openAsync(player, home.id, mapOf(variable.qualifiedName to greeting))
}
