<p align="center"><img src="docs/assets/banner.png" alt="Flow Control" width="100%"></p>

<h1 align="center">Flow Control</h1>

<p align="center"><a href="https://www.curseforge.com/minecraft/mc-mods/flow-control-streams-reflowing-addon">CurseForge</a> · <a href="https://github.com/Mikaaah/Streams-Flow-Control/wiki">Wiki</a> · <a href="https://github.com/Mikaaah/Streams-Flow-Control/issues">Report an issue</a> · <a href="https://github.com/Mikaaah/Streams-Flow-Control/blob/main/CHANGELOG.md">Changelog</a></p>

<p align="center"><img alt="Minecraft 1.21.1" src="https://img.shields.io/badge/Minecraft-1.21.1-568736?style=flat-square"> <img alt="NeoForge" src="https://img.shields.io/badge/Loader-NeoForge-d77828?style=flat-square"></p>

Shape currents and create calm water around docks, canals and builds. An addon for **Streams Reflowing**, available for NeoForge, Forge and Fabric.

[Download the original NeoForge 1.21.1 release](downloads/streams-flow-control-0.1.1-beta-neoforge-1.21.1.jar?raw=true)

## Versions and downloads

Use the branch and JAR matching your Minecraft version and loader. The seven ports below are version **0.1.2**.

| Minecraft | Loader | Streams Reflowing | Java | Source and download |
| --- | --- | --- | --- | --- |
| 1.21.11 | NeoForge | 2.15.2 | 21 | [neoforge/1.21.11](https://github.com/Mikaaah/Streams-Flow-Control/tree/neoforge/1.21.11) · [JAR](https://github.com/Mikaaah/Streams-Flow-Control/raw/refs/heads/neoforge/1.21.11/downloads/streams-flow-control-neoforge-1.21.11-0.1.2.jar) |
| 26.1.2 | NeoForge | 2.15.2 | 25 | [neoforge/26.1.2](https://github.com/Mikaaah/Streams-Flow-Control/tree/neoforge/26.1.2) · [JAR](https://github.com/Mikaaah/Streams-Flow-Control/raw/refs/heads/neoforge/26.1.2/downloads/streams-flow-control-neoforge-26.1.2-0.1.2.jar) |
| 26.3 | NeoForge | 2.15.2 | 25 | [neoforge/26.3](https://github.com/Mikaaah/Streams-Flow-Control/tree/neoforge/26.3) · [JAR](https://github.com/Mikaaah/Streams-Flow-Control/raw/refs/heads/neoforge/26.3/downloads/streams-flow-control-neoforge-26.3-0.1.2.jar) |
| 1.20.1 | Forge | 2.13.7 | 17 | [forge/1.20.1](https://github.com/Mikaaah/Streams-Flow-Control/tree/forge/1.20.1) · [JAR](https://github.com/Mikaaah/Streams-Flow-Control/raw/refs/heads/forge/1.20.1/downloads/streams-flow-control-forge-1.20.1-0.1.2.jar) |
| 1.20.1 | Fabric | 2.15.2 | 17 | [fabric/1.20.1](https://github.com/Mikaaah/Streams-Flow-Control/tree/fabric/1.20.1) · [JAR](https://github.com/Mikaaah/Streams-Flow-Control/raw/refs/heads/fabric/1.20.1/downloads/streams-flow-control-fabric-1.20.1-0.1.2.jar) |
| 1.21.1 | Fabric | 2.15.0 | 21 | [fabric/1.21.1](https://github.com/Mikaaah/Streams-Flow-Control/tree/fabric/1.21.1) · [JAR](https://github.com/Mikaaah/Streams-Flow-Control/raw/refs/heads/fabric/1.21.1/downloads/streams-flow-control-fabric-1.21.1-0.1.2.jar) |
| 26.1.2 | Fabric | 2.15.2 | 25 | [fabric/26.1.2](https://github.com/Mikaaah/Streams-Flow-Control/tree/fabric/26.1.2) · [JAR](https://github.com/Mikaaah/Streams-Flow-Control/raw/refs/heads/fabric/26.1.2/downloads/streams-flow-control-fabric-26.1.2-0.1.2.jar) |

Install on both client and server. Fabric also requires Fabric API. Each branch documents its exact loader requirements. NeoForge and Forge use TOML server configuration; Fabric uses `<world>/serverconfig/streamcontrol-server.json` and accepts defaults from `defaultconfigs/streamcontrol-server.json`.

Dedicated-server checks passed for all seven ports: ordinary and baked water, boat stillness, save/load, packet encoding, dimension isolation, redstone and controller removal. Client rendering, shaders and full modpack behavior require in-game playtesting.

## Make water work for your build

| Item | What it does |
| --- | --- |
| **Flow Wand** | Direct flow, still water or restore natural flow in a chosen area. |
| **Current Generator** | Maintain a directional current; rotate its top arrow to steer the flow. |
| **Stillwater Anchor** | Calm currents around a dock or mooring. |

All three items have survival recipes. Right-click either controller to open its settings screen: adjust radius (with diameter and vertical reach), choose connected-water-only behavior, and set flow speed and direction on the Current Generator. Redstone power disables a controller. Break it to restore the underlying current.

## Install and start

Install this addon and **Streams Reflowing 2.15.2** on client and server, using **Minecraft 1.21.1, NeoForge and Java 21**. **UltraTerraForged is not required.**

For a dock, place a Stillwater Anchor under or beside the water within four blocks of the boats. Right-click to adjust its radius; if there are multiple nearby ponds, enable *Connected water only*. Use the Flow Wand for temporary or shaped areas.

Flow changes velocity rather than moving water blocks or changing river terrain. Calm water removes current-induced drift; it does not prevent steering, collisions or momentum.

## Guides

[Items and controls](https://github.com/Mikaaah/Streams-Flow-Control/wiki/Items-and-Controls) · [Overlapping areas](https://github.com/Mikaaah/Streams-Flow-Control/wiki/Overlapping-Areas) · [Configuration](https://github.com/Mikaaah/Streams-Flow-Control/wiki/Configuration) · [Troubleshooting](https://github.com/Mikaaah/Streams-Flow-Control/wiki/Troubleshooting)

## Credits and license

By **Towns & Kingdoms**. [GPL-3.0-only](LICENSE). Requires Streams Reflowing; no dependency code or textures are redistributed.
