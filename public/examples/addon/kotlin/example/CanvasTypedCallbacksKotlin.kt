package example

import net.kyori.adventure.text.Component
import org.bukkit.plugin.Plugin
import zone.vao.craftcanvas.*
import java.util.concurrent.CompletableFuture

private val DESTINATION = CanvasParameter.choice("destination", listOf("spawn", "market"))
private val AMOUNT = CanvasParameter.integer("amount", 1, 64).optional(1L)
private val SILENT = CanvasParameter.bool("silent").optional(false)
private val SCHEMA = CanvasActionSchema.of(DESTINATION, AMOUNT, SILENT)
private val MINIMUM_LEVEL = CanvasParameter.integer("minimum-level", 0, 100).optional(0L)

fun registerTyped(canvas: CanvasService, owner: Plugin) =
    canvas.registerAction(owner, "example:typed", SCHEMA, CanvasTypedAction { player, _, args ->
        val destination: String = args[DESTINATION]
        val amount: Long = args[AMOUNT]
        if (!args[SILENT]) player.sendMessage(Component.text("$destination: $amount"))
    })

fun registerTypedAsync(canvas: CanvasService, owner: Plugin) =
    canvas.registerAsyncAction(owner, "example:typed-async", SCHEMA, CanvasTypedAsyncAction { _, _, args ->
        // Replace this completed stage with your nonblocking service operation.
        CompletableFuture.completedFuture(if (args[AMOUNT] > 0) CanvasActionResult.CONTINUE else CanvasActionResult.STOP)
    })

fun registerTypedCondition(canvas: CanvasService, owner: Plugin) =
    canvas.registerCondition(owner, "example:level", CanvasActionSchema.of(MINIMUM_LEVEL), CanvasTypedCondition { player, _, args ->
        player.level.toLong() >= args[MINIMUM_LEVEL]
    })
