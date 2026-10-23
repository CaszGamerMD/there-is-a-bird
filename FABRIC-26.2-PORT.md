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
 
## Current implementation checkpoint (2026-10-23)

These pieces have been **converted at source level** on `port/fabric-26.2`:
- Original sound and eight custom particle IDs use native Fabric registration and have passed isolated compilation.
- Block, block-entity, entity, feature and creative-tab declarations now use `FabricRegistryQueue`. All registry identifiers are compared with `main` by CI.
- All original block constructors now provide 26.2 block keys. All entity builders provide entity keys. Block entity registration uses Fabric's factory.
- `GuaniaoBlockTags` was ported and compiled against the new 26.2 registry API.
- `BirdServerTickManager` and `PhotoTransferEvents` now have Fabric event callback registrations in source. These **are not wired to the game initializer yet**: their dependencies need porting first.
- CI separately compiles Fabric infrastructure and probes for updated 26.2 construction and lifecycle APIs. These probe files are never included in the mod JAR.

**Important:** The inherited source still has many Forge and GeckoLib 4 usages; the full
`./gradlew build` failing is expected. Do not claim the mod is loadable, functional,
or safe to distribute based on individual compiling files or passing parity tests.

### Remaining implementation dependencies
- Port `GuaniaoItems` without replacing custom spawn eggs or changing item behavior
- Port all bird classes, 26.2 attributes, biome spawns, nesting AI and world state
- Migrate GeckoLib 4 to GeckoLib 5 and the modern 26.2 rendering architecture
- Convert photo/camera UI and networking; **wire all lifecycle callback registrations**
- Port recipe/loot/enchantment/menu/mixin APIs; update resource formats
- Full client and dedicated-server tests and behavioral comparison against the original mod


## Item, spawn-egg and resource compatibility checkpoint

The 1.20.1 `GuaniaoItems` declarations have been converted from
`RegistryObject` to native `FabricRegistryQueue.Ref` and supplied with
26.2 item resource keys. The original field names, literal IDs,
species-to-feather mappings and fries nutrition/saturation were retained.

The original painted eggs now extend 26.2 `SpawnEggItem` and specify their
spawned species using `Item.Properties.spawnEgg`. Cockatoo and kingfisher
were vanilla-style tinted spawn eggs in the original mod; their 26.2
`assets/guaniao/items/*.json` definitions explicitly retain the authored
primary and secondary colors. The other painted eggs receive no second tint.

All 67 original item models now have client item JSON references targeting
the **existing** model IDs without duplicating or editing textures.
This is a compatibility bridge, not final proof of correct rendering:
the custom Forge `separate_transforms` camera/guide models, dynamic photograph
renderers, and enchanted feather-fan predicates must still be ported and tested
against 26.2 rendering/model APIs.

The original music disc declared `lengthInTicks = 2411` and comparator 7 in
Minecraft 1.20.1; **the equivalent modern song duration is 120.55 seconds**
(2411 / 20), not 2411 seconds. The newer 1.21.1 NeoForge source contained
a 2411-second value, which is not behaviorally identical to the 1.20.1
baseline. The Fabric data-driven song corrects this and retains the same
sound event and translated description. CI derives the expected duration from
the original Forge `RecordItem` declaration to catch regressions.

Combined pack metadata now declares the 26.2 resource format 88.0 and data
format 107.1 range. The mod is still **not runnable**: source registries and
the updated item assets do not substitute for fully ported bird entities,
items, renderer hooks, menus, gameplay/network systems and client/server tests.


## Fabric bootstrap, server lifecycle and GeckoLib 5 checkpoint

The original Forge `GuaniaoMod` class now retains only its stable mod ID and
logger; the Fabric initializer performs startup. Ported native registration
queues run in dependency order: sounds/particles, blocks, block entities,
bird entity types, items, features and creative tab. This is the initial
bootstrap skeleton only; the original menus, network packets, enchantments,
biome spawning, all gameplay events and GeckoLib client renderers are still
required. **Do not interpret bootstrap wiring as a runnable mod.**

The original global `config/guaniao/bird_settings.json` path now uses Fabric
Loader's config directory. Its existing world scope and default-reload logic
remain intact. The corresponding pre-server-start and stopping lifecycle
events are now Fabric hooks, with the original per-server load and global
reset behavior. Migrated events also cover active bird ticking, rain-triggered
earthworm spawning at the original 200-tick cadence and 800-tick cooldown,
nest cleanup, photo transfers, join-time runtime bird-config sync and
legacy-photo scanning on login and newly opened server containers.

Unlike Forge's `PlayerContainerEvent.Open`, newly opened container menus
are observed by identity at the start of each server tick. Every newly opened
container is queued once, without repeatedly scanning an unchanged menu; menu
tracking is discarded on disconnect and server shutdown. Actual photo
conversion still uses the original `LegacyPhotoMigration` queue. In-game
verification of this replacement event timing is required.

The head-perched hummingbird, kestrel and umbrella cockatoo cleanup now uses
Fabric logout/stop callbacks; owner-only top-face placement is registered with
Fabric's block-use callback. The transition from Forge event priorities to
Fabric callback ordering needs real interaction testing for mod compatibility.

Four GeckoLib 4 block-entity interfaces have been mapped to their GeckoLib 5
equivalents without changing any other implementation line:
bird cages, bird baths, bagged fries and crow nests.
`tools/check-geckolib-blockentities.py` compares each migrated file against
the Forge baseline to enforce this invariant. The independent real 26.2
compile gate now includes GeckoLib 5 block/entity API proof sources and
modern server lifecycle/menu access probes. **Renderers, GeckoLib animation
controllers and client-side rendering are not ported by these changes.**

Regression scripts used by CI:
`tools/check-registry-parity.py`,
`tools/check-port-resources.py`,
`tools/check-lifecycle-port.py`,
`tools/check-geckolib-blockentities.py`.
The full Java compile and real-world functional tests remain release blockers.
