package example;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import zone.vao.craftcanvas.CanvasService;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** Targets declared by examples/interactive_ui.yml; call with its current session ID. */
public final class CanvasGuiControls {
    private CanvasGuiControls() {}

    public static CompletableFuture<Boolean> saved(CanvasService canvas, Player player, UUID sessionId) {
        return canvas.showNotificationAsync(player, sessionId, "notice",
            Component.text("Saved!", NamedTextColor.GREEN));
    }

    /** Call on the player's owning thread, or through canvas.withPlayer. */
    public static void resetPresentation(CanvasService canvas, Player player, UUID sessionId) {
        canvas.dismissNotification(player, sessionId, "notice");
        canvas.setScrollOffset(player, sessionId, "results", 0);
        canvas.playComponentAnimation(player, sessionId, "status");
    }
}
