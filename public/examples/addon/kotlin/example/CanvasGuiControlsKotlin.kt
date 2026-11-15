package example

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import zone.vao.craftcanvas.CanvasService
import java.util.UUID

/** Targets declared by examples/interactive_ui.yml. Capture the ID before starting I/O. */
fun showSaved(canvas: CanvasService, player: Player, sessionId: UUID) =
    canvas.showNotificationAsync(player, sessionId, "notice", Component.text("Saved!", NamedTextColor.GREEN))

/** Call on the player's owning thread, or through canvas.withPlayer. */
fun resetPresentation(canvas: CanvasService, player: Player, sessionId: UUID) {
    canvas.dismissNotification(player, sessionId, "notice")
    canvas.setScrollOffset(player, sessionId, "results", 0)
    canvas.playComponentAnimation(player, sessionId, "status")
}
