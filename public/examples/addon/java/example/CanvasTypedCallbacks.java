package example;

import net.kyori.adventure.text.Component;
import org.bukkit.plugin.Plugin;
import zone.vao.craftcanvas.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class CanvasTypedCallbacks {
    public static final CanvasParameter<String> DESTINATION = CanvasParameter.choice("destination", List.of("spawn", "market"));
    public static final CanvasParameter<Long> AMOUNT = CanvasParameter.integer("amount", 1, 64).optional(1L);
    public static final CanvasParameter<Boolean> SILENT = CanvasParameter.bool("silent").optional(false);
    public static final CanvasActionSchema SCHEMA = CanvasActionSchema.of(DESTINATION, AMOUNT, SILENT);
    public static final CanvasParameter<Long> MINIMUM_LEVEL = CanvasParameter.integer("minimum-level", 0, 100).optional(0L);
    private CanvasTypedCallbacks() {}

    /** Register globally after whenReady; no casts or parsing in the callback. */
    public static CanvasRegistration register(CanvasService canvas, Plugin owner) {
        return canvas.registerAction(owner, "example:typed", SCHEMA, (player, session, args) -> {
            String destination = args.get(DESTINATION);
            long amount = args.get(AMOUNT);
            if (!args.get(SILENT)) player.sendMessage(Component.text(destination + ": " + amount));
        });
    }

    public static CanvasRegistration registerAsync(CanvasService canvas, Plugin owner) {
        return canvas.registerAsyncAction(owner, "example:typed-async", SCHEMA, (player, session, args) -> {
            long amount = args.get(AMOUNT);
            // Replace this completed stage with your nonblocking service operation.
            return CompletableFuture.completedFuture(amount > 0 ? CanvasActionResult.CONTINUE : CanvasActionResult.STOP);
        });
    }

    public static CanvasRegistration registerCondition(CanvasService canvas, Plugin owner) {
        return canvas.registerCondition(owner, "example:level", CanvasActionSchema.of(MINIMUM_LEVEL),
            (player, session, args) -> player.getLevel() >= args.get(MINIMUM_LEVEL));
    }
}
