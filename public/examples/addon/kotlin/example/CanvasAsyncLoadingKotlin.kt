package example

import org.bukkit.plugin.Plugin
import zone.vao.craftcanvas.CanvasActionResult
import zone.vao.craftcanvas.CanvasAsyncAction
import zone.vao.craftcanvas.CanvasService
import java.util.UUID
import java.util.concurrent.CompletionStage

/** Register globally after whenReady. The loader must start nonblocking work and return promptly. */
fun registerLoading(canvas: CanvasService, owner: Plugin, load: (UUID) -> CompletionStage<String>) =
    canvas.registerAsyncAction(owner, "example:load", CanvasAsyncAction { player, session, _ ->
        val playerId = player.uniqueId
        load(playerId)
            .thenCompose { label -> canvas.setVariablesAsync(player, session.id, mapOf("loaded_label" to label)) }
            .thenApply { applied -> if (applied) CanvasActionResult.CONTINUE else CanvasActionResult.STOP }
    })
