# cion_hud — project notes

Fabric client mod, MC 26.2-pre-2, Fabric Loader 0.19.2, Fabric API 0.150.1+26.2, Java 25, Loom 1.16. Tested with Vulkan (Experimental) renderer.

## MC 26.2 vs older versions — API renames (CRITICAL)

26.2 split the HUD class and renamed graphics extractor. **Reference mods written for 26.1 will not compile as-is.**

| 26.1 / older                | 26.2 (current)                            |
| --------------------------- | ----------------------------------------- |
| `net.minecraft.client.gui.Gui` (HUD methods) | `net.minecraft.client.gui.Hud` |
| `Gui.HeartType`             | `Hud.HeartType` (FQN: `net/minecraft/client/gui/Hud$HeartType`) |
| `GuiGraphics`               | `GuiGraphicsExtractor` (`net.minecraft.client.gui.GuiGraphicsExtractor`) |
| `GuiGraphics.drawString(Font, String, x, y, color)` | `GuiGraphicsExtractor.text(Font, Component, x, y, color, boolean dropShadow)` — name is `text`, not `addText` (addText takes pre-built `GuiTextRenderState`). Wrap String via `Component.literal(s)`. Also `textWithBackdrop(Font, Component, ...)`, `centeredText(Font, FormattedText, x, y, w, color)` |
| `GuiGraphics` extends Gui-side blit helpers | `GuiGraphicsExtractor` has `pose()` → `Matrix3x2fStack`; `blitSprite(RenderPipeline, Identifier, x, y, w, h[, colorInteger])`, `blit(RenderPipeline, Identifier, x, y, u, v, w, h, texW, texH[, color])` |

`Gui` class still exists separately in 26.2 (top-level UI controller — owns Hud, chat, debug overlay). `Minecraft.gui` is type `Gui`, NOT `Hud`. To reach the Hud instance from outside, either inject a mixin accessor on `Gui.hud` field, or avoid the dependency (e.g., use `Minecraft.getInstance().player` instead of `Hud.getCameraPlayer()`).

`Options.hideGui` does NOT exist. There's `Options.toggleGui` / `Options.keyToggleGui` (a `KeyMapping`) only — the hide-GUI state lives elsewhere now. For HUD elements registered via `HudElementRegistry`, vanilla already skips invoking them when the HUD is hidden, so don't gate on hideGui yourself.

`Hud` no longer has `leftHeight` / `rightHeight` / `tickCount` int fields (they were in 26.1). Stacking is computed centrally via `HudStatusBarHeightRegistry`; position params are passed to each `extractXxx(...)` method by Hud's main render flow. Don't try to access non-existent fields via accesswidener — it will compile (accesswidener doesn't validate field presence at AW-parse time, only at runtime) but fail at runtime.

`Hud.HeartType` enum constants: `CONTAINER, NORMAL, POISIONED, WITHERED, ABSORBING, FROZEN`. Method `forPlayer(Player)` is package-private — needs accesswidener `accessible method`. Method `getSprite(boolean hardcore, boolean halfHeart, boolean blinking)` is public.

`Hud` private fields seen via constant-pool scrape: `displayHealth`, `lastHealth`, `healthRowHeight`, `rowHearts`, `rowOffset`, `currentAirSupplyTicks`, `maxAirSupplyTicks`, etc. There is NO `leftHeight`/`rightHeight`/`tickCount` int field anymore. `Hud.getGuiTicks()` did not appear in the constant pool either — use your own tick counter via `ClientTickEvents.START_CLIENT_TICK` instead.

Vanilla render flow uses an extract→render two-stage pattern. Key Hud methods (private/package, target via mixin):
- `extractPlayerHealth(GuiGraphicsExtractor, Player, int, int, int, int)`
- `extractHearts(GuiGraphicsExtractor, Player, int lines, int regenIdx, int maxHealth, int lastHealth, int currentHealth, float, int absorption, int, int, boolean blinking)` — 10 params after player
- `extractArmor(GuiGraphicsExtractor, Player, int, int, int, int)`
- `extractFood(GuiGraphicsExtractor, Player, int, int)`
- `extractAirBubbles(GuiGraphicsExtractor, Player, int, int, int)`
- `extractVehicleHealth(GuiGraphicsExtractor)`
- private heart blit helper: `(GuiGraphicsExtractor, Hud$HeartType, int x, int y, boolean, boolean, boolean)`

## Fabric API 0.150.1+26.2 — new HUD API

Located in `net.fabricmc.fabric.api.client.rendering.v1.hud`:

- `HudElement` — functional interface with `void extractRenderState(GuiGraphicsExtractor, DeltaTracker)`
- `HudElementRegistry` — static methods:
  - `addFirst(Identifier, HudElement)`
  - `addLast(Identifier, HudElement)`
  - `attachElementBefore(Identifier anchor, Identifier id, HudElement)`
  - `attachElementAfter(Identifier anchor, Identifier id, HudElement)`
  - `removeElement(Identifier)`
  - `replaceElement(Identifier, Function<HudElement, HudElement>)` — wrap original
- `VanillaHudElements` constants (use as `Identifier`): `HOTBAR, ARMOR_BAR, HEALTH_BAR, FOOD_BAR, AIR_BAR, MOUNT_HEALTH, INFO_BAR, EXPERIENCE_LEVEL, CROSSHAIR, MISC_OVERLAYS, HELD_ITEM_TOOLTIP, SPECTATOR_MENU, SPECTATOR_TOOLTIP, MOB_EFFECTS, BOSS_BAR, SLEEP, DEMO_TIMER, SCOREBOARD, OVERLAY_MESSAGE, TITLE_AND_SUBTITLE, SUBTITLES, CHAT, PLAYER_LIST`
- `HudStatusBarHeightRegistry` — `addLeft(Identifier, StatusBarHeightProvider)`, `addRight(...)`, `getHeight(Identifier)`. Resolved height is the **maximum** of registered providers per id, not the sum.
- `StatusBarHeightProvider` extends `ToIntFunction<Player>` — `int getStatusBarHeight(Player)`

When fabric-api wraps a vanilla element (via height provider impl), it does `pose().pushMatrix(); translate(0, -delta); element.extractRenderState(...); popMatrix()`. So replacement elements run in a possibly translated coordinate space.

## Inspecting compiled jars (no internet, no decompiler)

Useful loom cache locations:
- MC client jar: `C:\Users\<user>\.gradle\caches\fabric-loom\minecraftMaven\net\minecraft\minecraft-clientonly-deobf\<version>\minecraft-clientonly-deobf-<version>.jar`
- fabric-api: `C:\Users\<user>\.gradle\caches\modules-2\files-2.1\net.fabricmc.fabric-api\fabric-api\<version>\<hash>\fabric-api-<version>.jar`

Fabric API fat jar nests subjars under `META-INF/jars/` — open the nested jar to find specific API modules (e.g. `fabric-rendering-v1-*.jar` for HUD APIs).

To extract a specific class file from a jar via PowerShell:
```powershell
$jar = "<path>.jar"
Add-Type -AssemblyName System.IO.Compression.FileSystem
$z = [System.IO.Compression.ZipFile]::OpenRead($jar)
$e = $z.GetEntry("net/minecraft/client/gui/Hud.class")
$fs = [System.IO.File]::OpenWrite("out.class")
$e.Open().CopyTo($fs); $fs.Dispose(); $z.Dispose()
```

To read constant-pool strings (method names, signatures) without `javap`:
```powershell
$bytes = [System.IO.File]::ReadAllBytes("out.class")
$str = [System.Text.Encoding]::UTF8.GetString($bytes)
([regex]::Matches($str, '[\x20-\x7e]{4,}')).Value | Sort-Object -Unique
```

Note: `javap` is NOT on PATH (Oracle javapath ships only `java`/`javac`/`javaw`/`jshell`). Don't waste time hunting for it — constant-pool string scraping is enough to recover method names and JVM descriptors.

## Accesswidener namespace gotcha (Loom 1.16 + MC 26.2)

`cion_hud.accesswidener` header MUST be `accessWidener	v2	official`. With `named` Loom fails the build:

```
fabric-loom:access-widener - Expected official namespace for access widener entry, found: named
```

This is opposite to older Loom conventions where `named` was expected. Mojmap is shipped natively as "official" mapping now.

## Identifier API

`Identifier.of(String, String)` does NOT exist in 26.2. Use `Identifier.fromNamespaceAndPath(String namespace, String path)`. Other useful statics: `parse`, `tryParse`, `withDefaultNamespace`, `createUntrusted`, `createWithContext`.

## Project conventions

- `cion.hud.CionHud.id(String)` returns `Identifier.fromNamespaceAndPath("cion_hud", path)`. Use it.
- Mod uses an accesswidener (`src/main/resources/cion_hud.accesswidener`, format `v2 official`) declared in `build.gradle` (`loom { accessWidenerPath = ... }`) and in `fabric.mod.json` (`"accessWidener": "cion_hud.accesswidener"`).
- Client-only mixins live in `src/client/java/cion/hud/client/mixin/`, config in `cion_hud.client.mixins.json` (compatibilityLevel `JAVA_25`).
- Depends on `cion_core` (suite library) — pulled via `modImplementation "cion:cion_core:${cion_core_version}"` from `mavenLocal()`. Config loading via `cion.core.config.ConfigManager<T>` (see `cion.hud.client.config.HudConfig` facade).
- Vulkan compatibility: only use `RenderPipelines.GUI_TEXTURED` + `blitSprite`/`blit`/`addText` on `GuiGraphicsExtractor`. No raw GL.

## Reference (for porting only — different MC version)

`E:\__code\minecraft\26.2\overflowing-bars-26.1.x` — fuzs's Overflowing Bars, MC 26.1.2, uses puzzleslib. Useful for heart-overflow logic and sprite assets (`Common/src/main/resources/assets/overflowingbars/textures/gui/sprites/hud/`). Do **not** depend on puzzleslib — replicate needed helpers inline.
