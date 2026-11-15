package example;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import zone.vao.craftcanvas.*;
import java.util.UUID;

public final class CanvasCodeScreens {
    private CanvasCodeScreens() {}

    /** Call globally after CanvasApi.whenReady; then await canvas.reload() asynchronously. */
    public static CanvasRegistration register(Plugin owner, CanvasService canvas) {
        var screen = CanvasScreenDefinition.builder("example:code-menu")
            .extendsScreen("frame")
            .title("Addon menu")
            .variable("claimed", CanvasVariableType.BOOLEAN, false)
            .component(CanvasComponentDefinition.button("claim")
                .position(112, 128).size(160, 28).layer(20).centered()
                .text("Claim").tooltip("Claim your reward")
                .asset(CanvasAssets.nativeImage("craftcanvas:button"))
                .enabledWhen(CanvasConditions.variableEquals("claimed", false))
                .onClick(CanvasActions.setVariable("claimed", true), CanvasActions.message("Claimed!")))
            .build();
        return canvas.registerScreen(owner, screen);
    }

    /** Call on the player's owner; the ID rejects updates to replaced sessions. */
    public static boolean markClaimed(CanvasService canvas, Player player, UUID sessionId) {
        var state = canvas.uiState(player, sessionId);
        if (state == null || state.component("claim") == null) return false;
        return canvas.patchComponent(player, sessionId, "claim", CanvasComponentPatch.builder()
            .appearance(CanvasComponentAppearance.DISABLED).text(Component.text("Claimed")).build());
    }

    public static boolean restoreText(CanvasService canvas, Player player, UUID sessionId) {
        return canvas.patchComponent(player, sessionId, "claim", CanvasComponentPatch.builder()
            .reset(CanvasComponentProperty.TEXT).build());
    }
}
