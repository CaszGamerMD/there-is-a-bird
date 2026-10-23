# Bird Watching: Fabric 26.2 migration (IN PROGRESS)

This branch is a **source-level work branch, NOT a working mod or release**.
The starting source is EdDYON's 3.9.7 / Minecraft 1.20.1 / Forge 47.2 project.
EdDYON also has a newer `port/1.21.1` NeoForge branch for API reference:
https://github.com/EdDYON/there-is-a-bird/tree/port/1.21.1

## Target toolchain

* Minecraft 26.2 (Mojang mappings, un-obfuscated code)
* Fabric Loader 0.19.5; Fabric API 0.161.0+26.2
* Fabric Loom 1.18-SNAPSHOT (currently resolving to 1.18.4); Gradle 9.7.1; Java 25
* GeckoLib Fabric 5.5.5; keep bird geometry, animation and textures unchanged

The build scripts and Fabric metadata are initial wiring only. The inherited
Java sources still call Forge and GeckoLib 4 APIs, and **will not compile** until
those call sites have been ported.

## Behavior preservation contract

* Keep the `guaniao` mod ID and item/entity/block IDs unchanged so existing
  saves and resources have the best possible path to compatibility.
* No intentional change to species, flight/pathfinding, taming, breeding,
  spawning, AI schedules, drops, sounds, camera photos, frame designs, or GUIs.
* Do not remove features as a shortcut to make compilation pass.
* Preserve models, animation data, textures and audio byte-for-byte unless the
  author explicitly authorizes an asset change.
* The public repository lacks the official **online Public Album** code; a
  public-source Fabric port cannot recreate or claim to preserve that feature.
* See `DISTRIBUTION-POLICY.md` and `LICENSE-MODELS.md`: MIT is for code
  only. Local test JARs containing restricted assets must not be distributed
  without the author's explicit permission.

## Required migration gates

1. Replace Forge deferred registries and event bus with Fabric registries and
   callbacks. Preserve IDs, spawn placement/biome rules, attributes, menus,
   serializers, enchantments and worldgen.
2. Port all bird entity classes, movement/AI/flight, interaction, tame/breed,
   nesting, death loot and state saving to Minecraft 26.2 Mojang APIs.
3. Port GeckoLib 4 renderers/models/controllers to GeckoLib 5 render states,
   animation handling and Fabric rendering registration, with no changed visuals.
4. Port camera/photo capture and shader logic to the 26.2 Blaze3D path;
   preserve film, filters, photograph data, frame crafting and network flow.
5. Replace Forge network channels and config lifecycle with Fabric payload
   registration, synchronization and server-side validation.
6. Port all datapacks/recipes/loot/biomes/assets to valid 26.2 resource formats;
   preserve gameplay outputs. Check translated text and model assets.
7. Port and test the five mixins. Validate both the client and dedicated server,
   existing worlds and newly generated worlds, bird eggs, items, and photos.
8. Pass Gradle build **and** comprehensive gameplay parity verification before
   declaring the migration complete or distributing a build.

Run `python3 tools/port-audit.py` for a categorised source inventory and
`python3 tools/port-audit.py --strict` to block release when obvious old APIs
remain. These checks deliberately **do not** claim to prove gameplay parity.
