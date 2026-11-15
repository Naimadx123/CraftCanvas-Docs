package example

import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import zone.vao.craftcanvas.*
import java.util.UUID
import java.util.concurrent.CompletionStage

/** Register globally. The loader must start nonblocking work and return promptly. */
fun registerDynamicLists(
    canvas: CanvasService,
    owner: Plugin,
    load: (UUID) -> CompletionStage<List<CanvasListItem>>,
    select: (Player, CanvasListSelection) -> Unit
): List<CanvasRegistration> = listOf(
    canvas.registerAsyncAction(owner, "example:load-records", CanvasAsyncAction { player, session, _ ->
        load(player.uniqueId)
            .thenCompose { items -> canvas.setListItemsAsync(player, session.id, "results", items) }
            .thenApply { applied -> if (applied) CanvasActionResult.CONTINUE else CanvasActionResult.STOP }
    }),
    canvas.registerAction(owner, "example:pick-record", CanvasAction { player, session, _ ->
        session.listSelection?.let { select(player, it) }
    })
)
