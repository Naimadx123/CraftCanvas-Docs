package example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import zone.vao.craftcanvas.CanvasApi;
import zone.vao.craftcanvas.CanvasActions;
import zone.vao.craftcanvas.CanvasAssets;
import zone.vao.craftcanvas.CanvasComponentDefinition;
import zone.vao.craftcanvas.CanvasRegistration;
import zone.vao.craftcanvas.CanvasScreenDefinition;
import zone.vao.craftcanvas.CanvasService;
import zone.vao.craftcanvas.CanvasVariableType;

public final class CanvasGreeting extends JavaPlugin {
    private final List<CanvasRegistration> registrations = new ArrayList<>();
    private volatile CanvasService canvas;

    @Override
    public void onEnable() {
        CanvasApi.whenReady(this).thenAccept(api -> {
            registrations.add(api.registerAction(this, "greeting:hello", (player, session, data) ->
                player.sendMessage(Component.text("Hello from your addon!"))
            ));
            var screen = CanvasScreenDefinition.builder("greeting:menu")
                .extendsScreen("frame")
                .title("Greeting")
                .variable("greeting", CanvasVariableType.STRING, "Welcome")
                .component("heading", Map.of("text", "<aqua><bold>GREETING"))
                .component(CanvasComponentDefinition.text("welcome")
                    .position(112, 96).size(280, 24).layer(20)
                    .text("<white>{greeting}, {player.name}!"))
                .component(CanvasComponentDefinition.button("hello")
                    .position(112, 140).size(280, 28).layer(20).centered()
                    .asset(CanvasAssets.nativeImage("craftcanvas:button"))
                    .text("Say hello").tooltip("Click to receive a greeting")
                    .onClick(CanvasActions.callback("greeting:hello")))
                .build();
            registrations.add(api.registerScreen(this, screen));
            api.reload().thenAccept(result -> {
                if (isEnabled() && result.getSuccessful()) canvas = api;
                if (!result.getSuccessful()) getLogger().warning("Menu registration failed: " + result);
            }).exceptionally(error -> {
                getLogger().warning("Menu preparation failed: " + error.getMessage());
                return null;
            });
        }).exceptionally(error -> {
            getLogger().warning("CraftCanvas unavailable: " + error.getMessage());
            return null;
        });
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        CanvasService api = canvas;
        if (api == null) {
            sender.sendMessage(Component.text("The menu is still loading."));
            return true;
        }
        api.openAsync(player, "greeting:menu", Map.of("greeting", "Welcome"))
            .thenAccept(result -> {
                if (!result.getOpened() && !result.getQueued()) {
                    getLogger().warning("Cannot open menu: " + result.getStatus());
                }
            }).exceptionally(error -> {
                getLogger().warning("Menu request failed: " + error.getMessage());
                return null;
            });
        return true;
    }

    @Override
    public void onDisable() {
        canvas = null;
        for (int i = registrations.size() - 1; i >= 0; i--) registrations.get(i).unregister();
        registrations.clear();
    }
}
