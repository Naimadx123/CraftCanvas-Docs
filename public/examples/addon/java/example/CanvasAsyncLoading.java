package example;

import org.bukkit.plugin.Plugin;
import zone.vao.craftcanvas.CanvasActionResult;
import zone.vao.craftcanvas.CanvasRegistration;
import zone.vao.craftcanvas.CanvasService;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public final class CanvasAsyncLoading {
    private CanvasAsyncLoading() {}

    /** Register globally after whenReady. The loader must start nonblocking work and return promptly. */
    public static CanvasRegistration register(CanvasService canvas, Plugin owner,
            Function<UUID, CompletionStage<String>> loader) {
        return canvas.registerAsyncAction(owner, "example:load", (player, session, data) -> {
            UUID playerId = player.getUniqueId();
            return loader.apply(playerId)
                .thenCompose(label -> canvas.setVariablesAsync(player, session.getId(), Map.of("loaded_label", label)))
                .thenApply(applied -> applied ? CanvasActionResult.CONTINUE : CanvasActionResult.STOP);
        });
    }
}
