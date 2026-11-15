package example;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import zone.vao.craftcanvas.*;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class CanvasDynamicLists {
    private CanvasDynamicLists() {}

    /** Register on the global/Paper main thread. load must start nonblocking work and return promptly. */
    public static List<CanvasRegistration> register(
        CanvasService canvas, Plugin owner,
        Function<UUID, CompletionStage<List<CanvasListItem>>> load,
        BiConsumer<Player, CanvasListSelection> select
    ) {
        CanvasRegistration loading = canvas.registerAsyncAction(owner, "example:load-records", (player, session, data) ->
            load.apply(player.getUniqueId())
                .thenCompose(items -> canvas.setListItemsAsync(player, session.getId(), "results", items))
                .thenApply(applied -> applied ? CanvasActionResult.CONTINUE : CanvasActionResult.STOP)
        );
        CanvasRegistration picking = canvas.registerAction(owner, "example:pick-record", (player, session, data) -> {
            CanvasListSelection selection = session.getListSelection();
            if (selection != null) select.accept(player, selection);
        });
        return List.of(loading, picking);
    }
}
