package example;

import org.bukkit.entity.Player;
import zone.vao.craftcanvas.*;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class CanvasScreenDiscovery {
    private CanvasScreenDiscovery() {}

    /** Metadata reads need no player/global scheduler, including from a repository worker. */
    public static boolean supportsNotifications(CanvasService canvas, String screenId) {
        CanvasScreenInfo screen = canvas.screenCatalog().screen(screenId);
        if (screen == null) return false;
        CanvasComponentInfo notice = screen.component("notice");
        return notice != null && notice.getType().equals("notification");
    }

    public static CompletableFuture<CanvasOpenResult> openHome(CanvasService canvas, Player player, String greeting) {
        CanvasScreenCatalog catalog = canvas.screenCatalog();
        CanvasScreenInfo home = catalog.screen("home");
        if (!catalog.getAvailable() || home == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("The home screen is unavailable"));
        }
        CanvasVariableInfo variable = home.variable("session.greeting");
        if (variable == null || variable.getType() != CanvasVariableType.STRING || !variable.accepts(greeting)) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("home needs a compatible session.greeting declaration"));
        }
        // This still checks the current screen, player permission and resource-pack readiness.
        return canvas.openAsync(player, home.getId(), Map.of(variable.getQualifiedName(), greeting));
    }
}
