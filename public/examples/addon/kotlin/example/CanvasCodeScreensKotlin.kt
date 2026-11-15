package example

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import zone.vao.craftcanvas.*
import java.util.UUID

/** Register globally, then await canvas.reload() without blocking the tick thread. */
fun registerCodeMenu(owner: Plugin, canvas: CanvasService): CanvasRegistration = canvas.registerScreen(owner,
    CanvasScreenDefinition.builder("example:code-menu")
        .extendsScreen("frame")
        .title("Addon menu")
        .variable("claimed", CanvasVariableType.BOOLEAN, false)
        .component(CanvasComponentDefinition.button("claim")
            .position(112, 128).size(160, 28).layer(20).centered()
            .text("Claim").tooltip("Claim your reward")
            .asset(CanvasAssets.nativeImage("craftcanvas:button"))
            .enabledWhen(CanvasConditions.variableEquals("claimed", false))
            .onClick(CanvasActions.setVariable("claimed", true), CanvasActions.message("Claimed!")))
        .build())

fun markCodeMenuClaimed(canvas: CanvasService, player: Player, sessionId: UUID) =
    canvas.patchComponentAsync(player, sessionId, "claim", CanvasComponentPatch(
        appearance = CanvasComponentAppearance.DISABLED, text = Component.text("Claimed")))
