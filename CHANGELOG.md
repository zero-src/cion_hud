# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.0] - 2026-05-26

### Added

- **Overflow health bar** — extra hearts stack on top of the normal row instead of cluttering the screen. A small number on the left shows which row you're on.
- **Overflow armor bar** — extra armor points stack the same way. A small number on the left shows your row.
- **Absorption support** — golden hearts from totems, golden apples and similar effects also stack and follow the same overflow logic.
- **Harvest crosshair** — a small dot appears in the middle of your crosshair when you aim at a block. It tells you whether your current tool will work:
  - **Green** — you can mine the block and it will drop as an item.
  - **Blue** — you can mine it, but it won't drop without a special tool (Silk Touch, Shears for leaves, etc.).
  - **Red** — you can't mine the block at all with what you're holding (wrong tool tier or bedrock-like).
- **Config file** `cion_hud.json` — turn features on or off:
  - Overflow hearts on/off
  - Overflow armor on/off
  - Harvest crosshair on/off
  - Hide the row number next to hearts/armor
  - Pick a text color for the row number
  - Inverse heart coloring (overflow hearts get the base color, base hearts get the overflow tint)
- **In-game reload** — type `/cion_hud reload` in chat to apply config changes without restarting Minecraft.

### Dependencies

- Requires [cion_core](https://github.com/zero-src/cion_core) `>= 0.1.0`.
- Requires Fabric API on Minecraft 26.2.
