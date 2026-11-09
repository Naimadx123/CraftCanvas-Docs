---
title: "Build your first addon"
---

This addon opens the bundled `home` screen and registers a `greeting:hello` callback. It waits for CraftCanvas startup and uses the player scheduling helpers for command execution.

## Add the dependency

Gradle Kotlin DSL:

```kotlin
repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.vao.zone/snapshots")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("zone.vao:craftcanvas-api:1.0.0-SNAPSHOT")
}
```

Maven:

```xml
<repositories>
    <repository>
        <id>paper</id>
        <url>https://repo.papermc.io/repository/maven-public/</url>
    </repository>
    <repository>
        <id>vao-snapshots</id>
        <url>https://repo.vao.zone/snapshots</url>
    </repository>
</repositories>
<dependencies>
    <dependency>
        <groupId>io.papermc.paper</groupId>
        <artifactId>paper-api</artifactId>
        <version>1.21.11-R0.1-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>zone.vao</groupId>
        <artifactId>craftcanvas-api</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

Use `https://repo.vao.zone/releases` for release versions. Set Java release 21 to support the complete server range. Keep CraftCanvas API classes out of your addon JAR. Kotlin addons also need their own Kotlin build setup and runtime strategy.

## Declare the plugin

Create `src/main/resources/plugin.yml`:

```yaml
name: CanvasGreeting
version: 1.0.0
main: example.CanvasGreeting
api-version: '1.21.11'
folia-supported: true
depend: [CraftCanvas]
commands:
  greeting:
    description: Open the greeting menu
```

For the Kotlin example, set `main: example.CanvasGreetingKotlin`. Only declare Folia support if the rest of your addon also follows its threading rules.

## Java

```java
package example;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import zone.vao.craftcanvas.CanvasApi;
import zone.vao.craftcanvas.CanvasService;

import java.util.Map;

public final class CanvasGreeting extends JavaPlugin {
    private volatile CanvasService canvas;

    @Override
    public void onEnable() {
        CanvasApi.whenReady(this).thenAccept(api -> {
            api.registerAction(this, "greeting:hello", (player, session, data) ->
                player.sendMessage(Component.text("Hello from " + session.getScreen()))
            );
            canvas = api;
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
            sender.sendMessage(Component.text("The menu is loading."));
            return true;
        }
        api.openAsync(player, "home", Map.of("greeting", "Welcome!"))
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
    }
}
```

## Kotlin

```kotlin
package example

import net.kyori.adventure.text.Component
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import zone.vao.craftcanvas.CanvasAction
import zone.vao.craftcanvas.CanvasApi
import zone.vao.craftcanvas.CanvasService

class CanvasGreetingKotlin : JavaPlugin() {
    @Volatile
    private var canvas: CanvasService? = null

    override fun onEnable() {
        CanvasApi.whenReady(this).thenAccept { api ->
            api.registerAction(this, "greeting:hello", CanvasAction { player, session, _ ->
                player.sendMessage(Component.text("Hello from ${session.screen}"))
            })
            canvas = api
        }.exceptionally { error ->
            logger.warning("CraftCanvas unavailable: ${error.message}")
            null
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return true
        val api = canvas
        if (api == null) {
            sender.sendMessage(Component.text("The menu is loading."))
            return true
        }
        api.openAsync(sender, "home", mapOf("greeting" to "Welcome!"))
            .thenAccept { result ->
                if (!result.opened && !result.queued) logger.warning("Cannot open menu: ${result.status}")
            }.exceptionally { error ->
                logger.warning("Menu request failed: ${error.message}")
                null
            }
        return true
    }

    override fun onDisable() {
        canvas = null
    }
}
```

The source files are in [examples/addon](https://github.com/Naimadx123/CraftCanvas/tree/master/examples/addon). `gradlew.bat checkApiExamples` compiles both against the public API. Use one implementation in your addon.

## Connect the callback to YAML

Add this action to a button in a screen under `plugins/CraftCanvas/screens/`, then run `/cc reload` and load the rebuilt pack:

```yaml
actions:
  click:
    - {type: callback, id: 'greeting:hello'}
```

`/greeting` opens `home`, which declares the `greeting` variable. On a server where the administrator removed that example, install your own screen through an [addon bundle](/developers/bundles/) and change the screen ID and data to match it.

## Choose the right thread

`whenReady` is intended for `onEnable` or the global scheduler; its success callback runs in that context. Register callbacks there. The helper also works when the service is already available and completes immediately.

`openAsync`, `sessionAsync`, `setVariablesAsync` and `closeAsync` can be called from any thread. `withPlayer` schedules a short compound operation on the player's owner. Synchronous `openDetailed`, `session` and mutations belong on the player owner; callback actions already run there. Bundle requests and registration handles belong on the global scheduler. Reload accepts any thread.

A future continuation may execute on the caller, a worker or a player region. Use the scheduling helpers before accessing Bukkit/player state from an arbitrary continuation, and never block a tick thread on a future. CraftCanvas cannot schedule another plugin's internal work for it.

Next: [session updates](/developers/sessions/), [callbacks and conditions](/developers/callbacks/), [packaged addon resources](/developers/bundles/).
