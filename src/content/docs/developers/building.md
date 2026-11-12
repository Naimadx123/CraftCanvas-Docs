---
title: "Building from source"
---

Run `gradlew.bat build` on Windows or `./gradlew build` elsewhere. The normal build packages the minimized JAR and runs fast unit/configuration tests and compiles the Java/Kotlin addon examples; it does not start MockBukkit, repeatedly generate resource packs, or load the four compatibility API families. JDK 21 is sufficient for this path. Add `examplePack` to generate the standalone example resource pack. The final plugin is `build/libs/CraftCanvas-1.0.0-SNAPSHOT.jar`. The Gradle build uses Shadow 9.6.1 and pinned R8 9.5.26 to remove unused dependency classes/methods. R8 is downloaded from Google's Maven repository for building only; it is not included in the plugin or required on the server. Class/method names, annotations and stack-trace source positions are preserved. CraftCanvas classes, the public API, optional integration adapters and the private YAML implementation are retained, with all shipped YAML/PNG/font resources.

Use `gradlew.bat fullCheck` before publishing or distributing a release. It runs all unit tests, tagged integration tests, API-module checks and `paperCompatibility`. Integration tests run in at most two isolated JVM workers; MockBukkit tests remain sequential within each worker. Install JDK 25 as well as 21 for the compatibility matrix. `paperCompatibility` checks the finished JAR's Paper, Adventure and Netty method/field descriptors against these pinned Paper APIs: 1.21.11 build 132, 26.1.2 build 74, 26.2 build 134 and 26.3 build 202. These checks detect removed APIs and incompatible return types; they do not simulate a running vanilla client.

| Command | Work performed |
| --- | --- |
| `gradlew.bat build` | Minimized plugin/API artifacts and fast tests |
| `gradlew.bat assemble` | Artifacts only, with no tests |
| `gradlew.bat test` | Fast unit/configuration tests |
| `gradlew.bat check` | Fast tests and compiled addon examples |
| `gradlew.bat checkApiExamples` | Compile the documented Java/Kotlin addon sources |
| `gradlew.bat integrationTest` | MockBukkit, pack generation and isolated packaged-JAR tests |
| `gradlew.bat paperCompatibility` | Four-version Paper/Adventure/Netty linkage checks |
| `gradlew.bat fullCheck` | All verification; required for release preparation |
| `gradlew.bat build fullCheck examplePack` | Complete release build, verification and example pack |

Heavy tests use JUnit's `integration` tag and stay under `src/test/`. New tests that start MockBukkit, generate full packs or load the finished JAR should carry this tag. Filter a heavy test with `gradlew.bat integrationTest --tests '*RuntimeTest'`; `test --tests '*RuntimeTest'` will not select tagged tests. Keep Gradle's build/configuration caches enabled and avoid `clean` for routine builds; unchanged work is skipped. A normal build does not certify cross-version compatibility or run integration tests.

The JAR bundles the required Kotlin runtime subset and relocated SnakeYAML 2.7. Paper/Adventure/Netty and optional plugin APIs remain server-provided. Paper loads the HikariCP and JDBC libraries declared in `plugin.yml` for SQL player storage. Build tests use both the regular test classpath and the finished JAR in an isolated loader that cannot fall back to the test environment's Kotlin or CraftCanvas classes. The packed-JAR check installs/updates default files, compiles configuration, generates a resource pack and composes book pages. It also verifies that every plugin/API class and default resource remains present. Live server integrations and client graphics still require their normal installation tests.

Shadow's project-class inputs are limited to the production source set. Unit/integration/compatibility test classes do not participate in minimization, and test-only edits do not trigger another shrink. The explicit keep rules retain all CraftCanvas/API classes and optional adapters. JDK 25 and newer Paper API downloads are only needed when running `paperCompatibility` or `fullCheck`.

## Optional Nexo contract tests

To exercise the real public post-generation event and serializer, pass `-PnexoTestJar=<path>` for Nexo 1.29.1 and `-PnexoTestLibraries=<paths>` for Creative API and Minecraft serializer 1.15.3. Library paths are separated by the operating system's path separator (`;` on Windows). These are test-only inputs and are not bundled into CraftCanvas. The targeted artifacts are available from [Nexo's stable Maven repository](https://repo.nexomc.com/releases/).

For PowerShell, quote complete property arguments:

```powershell
.\gradlew.bat --% integrationTest "-PnexoTestJar=C:/test-libs/nexo-1.29.1.jar" "-PnexoTestLibraries=C:/test-libs/creative-api-1.15.3.jar;C:/test-libs/creative-serializer-minecraft-1.15.3.jar"
```

These tests cover typed model/texture import, automatic glyph resources, replacement of previously owned resources and conflict rejection. They do not launch a Minecraft client. `-PnexoTestPack=<path>` additionally enables the captured-pack composition test; supply a ZIP containing the supported Nexo shader pair under `nexo_1_21_11/`. The test writes through a temporary file after closing the input. Keep the capture at a separate path if you want to retain its original bytes. An unsupported or unrelated ZIP is rejected rather than treated as a valid Nexo fixture. Tests needing Nexo are skipped without the matching test properties; some book serialization/item-hover tests are limited by unimplemented MockBukkit methods and still need a real Paper client/server check.

## Animation shader checks

Run `gradlew.bat integrationTest --tests '*ShaderAnimationIntegrationTest'` to check atlas generation, page budgets, two-player session isolation and completion/cancellation. These tests write reusable packs under `build/shader-animation-example/`. Supplying the Nexo test properties above also generates `nexo-composed.zip` with its verified GIF shader template.

For pixel comparisons in an OpenGL context, install Python packages `moderngl` and `glcontext`, obtain an official supported vanilla client JAR, then run:

```text
python tools/check_animation_shader.py --client /path/to/client-1.21.11.jar --pack build/shader-animation-example/craftcanvas.zip
python tools/check_animation_shader.py --client /path/to/client-1.21.11.jar --pack build/shader-animation-example/reward-wheel.zip --max-tiles 3
python tools/check_animation_shader.py --client /path/to/client-1.21.11.jar --pack build/shader-animation-example/nexo-composed.zip
```

The tool checks atlas pixels against source frames through cycles, slowdown, clock wrap and final selection. It does not launch Minecraft or verify book interaction, GUI Scale, network delay or synchronization with the client's world clock. Test those with an actual vanilla client and the installed pack.

Cinematic checks use `integrationTest --tests "*CinematicIntegrationTest"` and write `build/cinematic-example/craftcanvas.zip`. With the Nexo test properties they also produce `nexo-composed.zip`. Run `python tools/check_cinematic_shader.py --client /path/to/client-1.21.11.jar --pack build/cinematic-example/craftcanvas.zip` to compare basic procedural masks and all three movie fit modes across GUI scales and aspect ratios. Decorative effects are checked for full cover/clear endpoints, visible intermediate states, distinct output and changes from direction, intensity and accent-color. Add `--previews build/cinematic-example/previews` to export early/middle/late GPU-rendered images over a checker background. Repeat with the composed pack. These checks do not launch Minecraft.

`NexoShaderApiTest` also imports the generated cinematic ZIP through the real Creative reader, composes the active Nexo overlay and exports `build/cinematic-example/nexo-imported.zip` through its writer. Run the cinematic GPU check against this artifact to verify typed-font detection and the final serialized shader pair, rather than only a manually assembled fixture.

## Shader version matrix

The GPU tools accept official clients 1.21.11, 26.1, 26.1.1, 26.1.2, 26.2 and 26.3, verify their pinned SHA-1 hashes, and select overlays from each client's resource format. Repeat the hover, animation and cinematic commands above for each client JAR. GUI uniform offsets and text pipeline names follow the selected version. These checks render in a standalone OpenGL context and do not launch Minecraft.

`python tools/check_shader_variants.py --client /path/to/client-26.3.jar --pack build/cinematic-example/craftcanvas.zip` links all GUI, grayscale and see-through pipeline permutations. With `--shaderc-library /path/to/shaderc.dll` (or a matching shared library), it also compiles the 26.3 sources as Vulkan SPIR-V with explicit stage locations, plus alpha-only fragment permutations. Use the ShaderC native library from the official client's LWJGL dependency. This checks compilation, not actual Vulkan rendering or the game's OIT blend targets.

`UiIntegrationTest` writes `build/ui-motion-example/craftcanvas.zip`. Run `python tools/check_motion_shader.py --client /path/to/client-26.3.jar` after that test completes to compare component motion GPU pixels against a CPU reference at fractional ticks, across clock wrap, including regular/bold text, exact palette colors and scaled image tiles. Repeat with the supported client JARs to exercise each overlay. The check needs ModernGL; it does not launch the Minecraft client.

With the optional Nexo libraries enabled, `NexoShaderApiTest` reads the generated overlays, composes each family's actual GIF shader pair, verifies repeatability, and serializes `build/cinematic-example/nexo-imported.zip`. Repeat the GPU tools against that ZIP to check the effective merged shaders.

## Publishing the API

Run `./gradlew :api:publish` or `gradlew.bat :api:publish` on Windows. This publishes only `craftcanvas-api`, its sources JAR and Maven/Gradle metadata. Versions ending in `SNAPSHOT` go to `https://repo.vao.zone/snapshots`; other versions go to `https://repo.vao.zone/releases`. Ordinary builds do not upload artifacts.

Provide the Reposilite username and token through `VAO_REPO_USERNAME` and `VAO_REPO_PASSWORD`, or set `vaoUsername` and `vaoPassword` in your user-level `~/.gradle/gradle.properties`. Gradle properties take precedence over the environment. Use credentials with permission to publish `zone/vao/craftcanvas-api`; never commit credentials to the project. 
