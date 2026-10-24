# Changelog

## 0.1.3-beta.2 — NeoForge 1.21.1 (test build)

- Keep the controller's panel and labels crisp by replacing the deferred vanilla background blur with a translucent dim overlay.

- Prevent Streams Reflowing from emitting new surface bubbles, leaves, sticks and adjacent-current spray from water cells controlled by Still Water or a Stillwater Anchor.
- Add a shared controller settings GUI: clearly labeled radius and diameter, vertical reach, Current Generator speed and facing, and an optional **Connected water only** toggle.
- Connected-only controller regions now use bounded water connectivity, not a blanket circular override. The masks persist and sync to clients so physics, textures and particles agree; water changes are refreshed periodically.
- Keep existing worlds compatible: connected-only is off by default and old saved controller settings still load.

- Apply final stillwater/current overrides at `FluidState.getFlow`, including client render lookups, so a later fluid implementation cannot reintroduce directional movement in the rendered flow vector.
- Use a direction-neutral angle for stillwater in both Streams' client grid and vessel grid, rather than preserving the Flow Wand or Stillwater Anchor facing while speed is zero.
- Allow client flow-grid lookups on chunk rendering threads without a thread-group logical-side gate.
- Add regression assertions for stillwater and directed current angle behavior.
- **Testing:** server-side integration check and in-game client visual verification are required before publishing a JAR.

## 0.1.1-beta

- Replace the Flow Wand, generator top, anchor top, shared sides and shared bottoms with the supplied textures, unchanged.
- Align the generator model with the supplied arrow orientation.


## 0.1.0-beta

- Flow Wand with directed, still and restore modes; configurable circular radius and current strength.
- Waterloggable Current Generator with directional top arrow and rotation controls.
- Waterloggable Stillwater Anchor for docks and other calm-water areas.
- Redstone disables controllers; breaking one immediately removes its override.
- Dedicated creative tab, original pixel textures and survival recipes.
- Per-dimension persistence, chunk-indexed physics queries and server-to-client synchronization.
- Override ordinary water, baked Streams fluids and the extra Streams vessel drift path.
- Height-bounded regions, deterministic overlap priority, owner checks and configurable edit limits.
- Opt-in dedicated-server integration checks.

Targets Minecraft 1.21.1, NeoForge 21.1.219 and Streams Reflowing 2.15.2.
