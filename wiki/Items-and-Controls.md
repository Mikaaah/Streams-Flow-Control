# Items and controls

All three items are in the **Streams Flow Control** creative tab and have survival crafting recipes.

| Item | Action |
| --- | --- |
| **Flow Wand** | Use on water to apply the selected mode within a circular area. Directed water follows your horizontal view. |
| Wand modes | Aim at the sky and **sneak-use** to cycle **Direct flow → Still water → Restore natural flow**. |
| Wand radius | Aim at the sky and **use** to cycle the radius, or run `/streamcontrol radius <blocks>` while holding the wand. |
| Wand strength | `/streamcontrol strength <0.05–2>` while holding the wand. Server limits apply. |
| **Current Generator** | Place under or beside water, then right-click for its settings screen. Choose radius, flow speed, compass direction and connected-water mode. |
| **Stillwater Anchor** | Place under or beside your dock, then right-click to configure its radius and connected-water mode. Flow speed stays at zero. |
| Controller radius | The screen shows the radius in blocks, full diameter and vertical reach. Right-clicking with a Flow Wand also opens the screen. |
| Connected water only | When enabled, the controller affects just the six-direction-connected water body touching it, not a separate pool within range. Default is off to preserve old behavior. |
| Redstone | A powered controller is **disabled**. Remove power to enable it again. |
| Remove block control | Break the block; its region is removed immediately and underlying flow returns. |

A radius of four means a horizontal circular area four blocks from the selected water or controller.
The default vertical reach is four blocks above and below it. Only water in that volume is affected.
Connected-water mode begins at water in the controller itself or directly adjacent to it (front first). It refreshes periodically to catch changes to nearby canals, without loading distant chunks.
Flow strength changes current velocity; it does not pump water into empty blocks or move the water surface.
Stillwater regions also suppress Streams Reflowing's newly emitted surface bubbles, floating leaves and sticks in the affected water. Existing particles can finish their normal lifetime.

For a dock, place a Stillwater Anchor in the riverbed within four blocks of the moored boats. Adjust its radius if needed.
Stillness stops water-induced drift. It does not lock a boat against steering, collisions, wind from other mods or existing momentum.

[← Wiki home](https://github.com/Mikaaah/Streams-Flow-Control/wiki/Home)
