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


## Server events, persistent block entities and native spawn registration

This checkpoint ports another set of original, behavior-sensitive Forge handlers:

- The existing wild-bird population tracker now clears tracked birds when Fabric
  unloads entities and unloads each level. The original thresholds and scan
  budgets remain unchanged.
- Cassowary avoidance now attaches the same avoidance goal to zombies,
  skeletons and spiders on load, clears targets at the original 20-tick cadence,
  and forgets tracked hostiles on unload. Minecraft 26.2 requires the public
  `getGoalSelector()` accessor.
- Original bird conference, fluffy line and high-eye advancement conditions
  run from Fabric end-server ticks with the same staggered 40/10-tick checks.
- Bird flyby encounters (five authored habitat types), colony habitat scan
  cleanup, and the polluted-cake prank/cleanup callbacks now use Fabric hooks;
  spawn weights, durations and scan budgets are source-parity checked.
- All 19 original bird attribute provider methods have dedicated Fabric
  registrations. The original 19 species spawn-position checks, species
  predicates, ground-vs-leaves placement modes, heightmaps and
  `CHUNK_GENERATION` exclusions have been mapped into Fabric mob builders.
  **The complete bird entity code and real-world spawning remain unverified.**
- Vanilla creative Ingredients retains Earthworm and Spawn Eggs retains the
  original 19 bird egg entries in their original order. The four bird-dropping
  composter values use Fabric's `CompostableRegistry`, leaving the original
  farmland-fertilizing odds alone. All four Forge mod-bus responsibilities are
  now ported and its subscriber retired.

### Persistent 26.2 block entities

Bagged Fries and Bird Baths now use `ValueInput` / `ValueOutput` for their
Minecraft 26.2 save hooks and the correct `HolderLookup.Provider`-aware
`getUpdateTag`. The original NBT field names are unchanged:

- Bagged Fries keeps `FryMask`, `Laxative` and its block-state fallback.
  An explicitly saved zero mask remains empty rather than being restored full.
- Bird Baths keep the type, water/food level, cleanliness, spoil state, frozen
  sugar-water state, environmental tick offset, current occupancy and recently
  occupied timers. `CurrentUser` remains the original four-integer UUID NBT
  array, including prior save compatibility.
- Neither block's other gameplay implementation or animation rendering was
  changed during persistence migration.

The author-origin preservation checks for both sources now permit *only*
those persistence methods and GeckoLib package conversions to differ.
Compile-only tests now exercise Minecraft 26.2 Value I/O and legacy UUID
int-array access. Other old block-entity serializers (especially the
inventories in Crow Nests and Frame Workbenches) still need migration.

### CI and release status

Audits validate original entity/attribute/spawn-table ordering, 19 species
heightmaps, creative tab contents, compost odds, population caps, flyby and
cake behavior, plus stable resources and unchanged audio/images/models.
The `verifyRegistriesPort` task compiles isolated real 26.2/Fabric APIs,
including new block-entity persistence and spawn placement probes. The full
Gradle compile still fails while gameplay, UI, networking, rendering, entity
serialization, loot and world generation remain on pre-26.2/Forge APIs.

**No playable mod build, release or asset-containing distributable exists.**
The draft PR is intentionally unmerged and original author asset licensing
restrictions remain in force.

## Crow nest and private frame workbench persistence checkpoint (2026-10-23)

Two more block entities now use Minecraft 26.2's `ValueInput` / `ValueOutput`
rather than the 1.20.1 `CompoundTag` override and obsolete packet callbacks.

- **Crow nest:** the original nested `Treasures.Items` layout is written/read via
  26.2 `ContainerHelper`; natural nest flag, crow UUID/time claim list, rummaged
  bitmask, active search slot/timers, loot layout seed and all three layout arrays
  keep their original keys and sanitization rules. Crow UUIDs remain four-int
  arrays; the 26.2 client packet uses the original same full update tag.
- **Frame workbench:** private `Works` entries retain original keys for owner,
  input, result, pending, reserved source, remaining ticks, revision and seed.
  Empty stacks are encoded with `ItemStack.OPTIONAL_CODEC`. Owner UUIDs are
  four-int arrays as before. Invalid unfinished jobs still refund the reserved
  original instead of materializing completed photos. The chunk update tag
  remains deliberately **empty** to avoid leaking private photographs to
  non-owners; owner-only menu sync still requires the separate menu/network port.
- CI now checks the migrated frame workbench's gameplay source remains identical
  outside the explicit I/O region, and checks crow nest code against the original
  with only GeckoLib and persistence migrations permitted. A 26.2 Java API proof
  compiles the nested `ValueInput`/`ValueOutput`, `ContainerHelper` and
  `ItemStack.OPTIONAL_CODEC` signatures independent of unported classes.

**Still blocked:** full compilation, runtime migration of old 1.20.1 item-stack
data via vanilla DataFixerUpper, private photograph menu/network sync, and
crow nest/client live update verification all require full client and dedicated
server tests. These commits do not produce a working mod or distributable JAR.

### Follow-up: 26.2 compiler-diagnostic compatibility

- `CrowNestBlockEntity` moves the nest index registration from the removed
  `BlockEntity.onLoad()` hook to `setLevel(Level)`; `setRemoved()` still removes
  the index. All client/server side checks use `Level.isClientSide()`. Stack
  merging uses `ItemStack.isSameItemSameComponents` rather than the old
  NBT-specific matcher. The custom render bounding box remains available, but
  no longer claims a vanilla override.
- `FrameWorkbenchBlockEntity` retains both private-slot dirty notifications
  via `SimpleContainer.setChanged()` overrides, replacing the removed
  `addListener` API. Input/result changes each still increment the snapshot
  and mark the workbench dirty; side checks use the current getter.
- `BaggedFriesBlockEntity` keeps its original brown/green dust RGB components
  and scales while passing packed colors using the native 26.2
  `ARGB.colorFromFloat` format. Original particle cadence and count are
  untouched; `Level.isClientSide()` replaces the old exposed field.
- Migration parity scripts pin these compatibility substitutions exactly and
  continue rejecting unrelated gameplay edits.

These adaptations require in-world validation, especially nest level
assignment/unload and frame-workbench inventory notifications. Isolated
compilation of a separate API probe does not certify full gameplay.

## Continued full-feature Fabric 26.2 migration checkpoint — 2026-10-23

The author-origin behavior contract remains in force. No dummy bird/entity
implementations, disabled features, stripped resources or release JAR were
introduced to bypass remaining compilation failures.

### Authored birds and GeckoLib rendering

- All 17 original bird definition classes that still referenced
  `ResourceLocation` now use 26.2 `Identifier` and
  `Identifier.fromNamespaceAndPath`; constants, dimensions, health, speed,
  model/texture/animation paths and other gameplay details are unchanged.
  `tools/check-bird-definition-identifiers.py` checks all 17 source files
  against `main`.
- Five constant-resource GeckoLib 4 model classes (Bagged Fries, Nikon D750,
  Crow Nest block/item, Bird Dropping Projectile) now use GeckoLib 5's
  `GeoModel` and `GeoRenderState` getter signatures. Their file/resource
  references are unchanged. A separate real 26.2/GeckoLib compilation proof
  checks those signatures and `tools/check-geckolib-static-models.py`
  checks their authored asset/path invariants.
- **Rendering is not yet working.** Dynamic bird mutation textures, bath/cage
  model variants, animation state extraction, custom render layers and all
  renderer registrations still require porting. GeckoLib 5's changed asset
  lookup layout must be resolved without altering/repackaging restricted
  model/animation/texture assets without the author's approval.

### 26.2 crafting and datapack migration

- Migrated `FilmToPhotographRecipe` and `BirdDroppingFoodRecipe` from the
  removed 1.20.1 constructors/`CraftingContainer`/registry parameters to
  the new `CraftingInput` and stateless `RecipeSerializer(MapCodec,
  StreamCodec)` API. Kept their ingredient matching and image-copy or prank
  item output flow.
- Converted the custom `PhotographFrameRecipe` to a `CraftingRecipe`
  delegating to modern `ShapedRecipe`. Its codec retains the original
  `frame_shape` field, shaped inputs, one-photo requirement, photo/image
  component copying, original frame shape check and shape-specific recipe
  display. This is required because 26.2's generic serializer cannot be
  covariantly overridden from `ShapedRecipe`. The codec/display APIs have a
  separate compile-only proof.
- All 21 original recipe JSONs now live in
  `data/guaniao/recipe/*.json`. Ingredients use the 26.2 item/tag holder
  encoding, item results use the new `id` key, and recipe-book categories
  are mapped to the closest current vanilla categories. The exact original
  ingredient lists, quantities, patterns, output IDs and cooking values are
  checked against `main` by `tools/check-26-2-recipes.py`.
- Relocated 98 other old datapack files (advancements, loot tables, mod tags
  and vanilla tag extensions) to 26.2's singular registry folder names.
  Their original JSON bytes and content are unchanged. The
  `tools/check-26-2-datapack-paths.py` gate verifies this one-for-one
  against `main` and rejects lingering legacy paths.
- **Not all datapacks are yet fully verified.** Original Forge biome
  modifiers, global loot modifiers, advancement trigger compatibility,
  vanilla loot codec compatibility and worldgen need explicit Fabric runtime
  migrations/tests.

### Release gate is still closed

Full compilation, all 19 bird AI/entities, GeckoLib 5 renderers, networking,
camera/photos, recipe gameplay, full resource loading and dedicated server
and client tests must pass before constructing a working test JAR. Do not
publish/distribute an asset-containing modified build without the author's
prior written permission as required by `DISTRIBUTION-POLICY.md`.

## Bird-dropping pressure-plate callback checkpoint (2026-10-23)

The original one-shot bird-dropping pressure plate pulse now registers via
Fabric `ServerTickEvents.END_LEVEL_TICK` and `ServerLevelEvents.UNLOAD`,
instead of Forge level events. The same per-level expiry map, configured
duration, no-repeat rule and 200-tick stale-entry cleanup are preserved.
The native 26.2 block-inside call supplies `InsideBlockEffectApplier.NOOP`
and the modern precision flag. A compile-only API probe and main-branch
source comparison protect this compatibility change. The block interaction
must still be observed in a loaded client/server world; these changes alone
do not create a runnable test JAR.

## Bird-dropping effect and particle compatibility checkpoint (2026-10-23)

The original prank-food effect durations, amplifiers and sounds are unchanged;
old `CONFUSION`, `MOVEMENT_SLOWDOWN` and `MOVEMENT_SPEED` identifiers
now use Minecraft 26.2's `NAUSEA`, `SLOWNESS` and `SPEED` effects.
Four authored dust particle colors in the dropping reactions and polluted-cake
saved-data visuals are passed through 26.2's packed ARGB constructor with
full opacity. All original red/green/blue components, scales, particle counts,
random chances, application timings and gameplay methods remain identical.
Original-source parity checks and an isolated Java 26.2 API probe cover these
substitutions. Full-project compilation and actual client/server gameplay are
still required before a private test build can be considered working.

## Polluted cake SavedData codec conversion (2026-10-24)

`PollutedCakeData` now uses the Minecraft 26.2 `SavedDataType` and codec
rather than removed `CompoundTag` override and 1.20.1
`computeIfAbsent(loader, factory, filename)`. The name remains
`guaniao_polluted_cakes`, and `Codec.LONG_STREAM` under `Positions`
preserves the original NBT *long array* rather than changing it into a list.
Loading still deduplicates positions into the same chunk buckets; mutations
still mark the data dirty. A source parity gate checks the entire unchanged
runtime cake/particles/chunk-validation region, plus storage invariants.
The independent 26.2 compile gate includes the matching `SavedDataType` /
long-stream codec API proof. Existing-world and real-server tests are still
required: the source parity/compilation checks do not prove DFU migration or
loaded-game compatibility.

## Feather-fan enchanted book tooltip signature checkpoint (2026-10-24)

All three original glowing custom books — Burial Plume, Riven Plume and
Hunting Return — now use Minecraft 26.2's `Item.TooltipContext`,
`TooltipDisplay` and `Consumer<Component>` item tooltip hook. The
`isFoil` return, existing localization keys, dark-gray italic style and
independent item identities are unchanged. A source-equality parity test
and isolated 26.2 override compilation probe guard against accidental
edits. This does not yet port the enchantment registry/loot-distribution
system itself or prove that the books work during gameplay.

## Bird entity enum migrations, first batch (2026-10-24)

Ported native 26.2 `EntitySpawnReason` and `PathType` classes in the
original budgerigar, cassowary, cockatiel, cockatoo, columbid,
pigeon and spotted-dove entity sources. The field/method bodies and authored
enum constants are unmodified. Extended GeckoLib source-parity rules and
added isolated Java API compile tests; further bird species remain in progress.
This is not yet a playable mod or a complete bird AI conversion.

### Second bird entity enum batch (2026-10-24)

Converted CrowEntity, HummingbirdEntity, KestrelEntity, KingfisherEntity, KiwiEntity, LongTailedTitEntity, MacawEntity source types to
the same 26.2 native spawn/path enums, preserving their authored branches,
enums, constant values and flight/foraging choices. Existing GeckoLib and
whole-file no-Gecko parity tests now cover all 14 migrated classes.

### Bird enum relocation coverage complete (2026-10-24)

The final batch (MynaEntity.java, NightHeronEntity.java, SeagullEntity.java, SparrowEntity.java, UmbrellaCockatooEntity.java, WoodcockEntity.java) brings all 20 original bird entity
classes onto native `EntitySpawnReason` and `PathType` symbols wherever
the old types were used, plus the exact 26.2 fish/chunk package moves.
The original spawn-reason comparisons and path penalties remain untouched.
The 20-source parity gate and GeckoLib parity test remain active.
These class moves do not repair the other unported AI, persistence, Forge
event, flight or rendering APIs and do not yield a playable JAR.

## Crow/Kestrel namespaced identifier types (2026-10-24)

Converted the original crow texture getter and kestrel learned-fetch item
field and save parser from Minecraft `ResourceLocation` to the native 26.2
`Identifier`. Kestrel still saves the **same namespaced item string** under
the exact original save key; `Identifier.tryParse` continues returning
`null` for malformed IDs and the original registry-existence check is kept.
An isolated Java API proof and GeckoLib/AI source parity test guard the
conversion. No crow texture or kestrel fetch targeting behavior was changed.

## Bird dropping event registration cleanup (2026-10-24)

The original `BirdDroppingEvents` has no Forge event-subscriber methods.
Bird dropping updates are explicitly invoked by `BirdServerTickManager` on
the same end-server-tick cadence. Removed its unused Forge bus annotation and
import without changing any other line. The new source parity test verifies
the tick call remains connected and no spawning chance, cooldown, variant,
particles or behavior code changes. Entity persistence and fully migrated
bird behavior are still outstanding.

## Bird repopulation Fabric lifecycle (2026-10-24)

The original loaded-habitat repopulation scheduler is now invoked by
Fabric `END_LEVEL_TICK` and its per-world attempt state is removed by
`ServerLevelEvents.UNLOAD`. Registered both at initialization. The entire
spawn selection and population cap body, rate/interval arithmetic, random
calls, player choice, loaded-chunk limits and regional retries remain
byte-for-byte unchanged, enforced by an upstream parity script. This changes
event delivery only. Repopulation still calls legacy Forge spawn hooks and
old weighted-spawn APIs, which must be replaced with an equivalent Fabric
mechanism before full build/runtime verification. No spawning behavior is
claimed playable yet.

### Native repopulation spawn/gamerule symbols (2026-10-24)

The same repopulation site predicates now use `EntitySpawnReason.NATURAL`
instead of `MobSpawnType.NATURAL` and Minecraft 26.2's
`GameRules.SPAWN_MOBS` access instead of the removed
`RULE_DOMOBSPAWNING`/boolean getter combination. The existing
`GuaniaoSpawnRulesProof` independently compiles that gamerule API.
All original probability, cap, height, group and spawn-reason decisions
remain untouched, as checked against `origin/main`.
The Forge potential-spawn and finalize-spawn integration remains unported.

## 26.2 FlyingAnimal removal and air movement (2026-10-24)

Minecraft 26.2 removes the `FlyingAnimal` interface and uses the
protected `Entity.omnidirectionalAirMover()` hook. All nine original
classes that implemented `FlyingAnimal` now override the native hook
to return `true`, preserving the original flight-movement marker.
Their existing `isFlying()` methods and every flight/animation decision
remain unchanged (the method is no longer an interface override).
The whole-file GeckoLib bird parity gate derives the exact interface-to-hook
conversion from the original Forge source, and a separate real-API
compile probe verifies the 26.2 override signature. This does not
by itself complete entity compilation, navigation, or bird simulation.

## 26.2 synchronized bird state builder migration (first batch, 2026-10-24)

The eight classes BudgerigarEntity.java, CassowaryEntity.java, CockatielEntity.java, AbstractColumbidEntity.java, PigeonEntity.java, SpottedDoveEntity.java, CrowEntity.java, HummingbirdEntity.java now initialize synchronized
entity state through `SynchedEntityData.Builder` instead of the removed
`entityData.define` override. All 40
original field registrations retain their accessor, value, order and
superclass-before-subclass ordering. The checker compares each entire
initialization method against the original Forge source after **only**
the signature and builder call substitutions. An isolated 26.2 Java
compilation probe verifies the method signatures. Other entities remain
unconverted; no playable mod is implied.

### All synchronized bird states migrated (2026-10-24)

Converted the remaining nine class overrides (KestrelEntity.java, KingfisherEntity.java, KiwiEntity.java, MynaEntity.java, NightHeronEntity.java, SeagullEntity.java, SparrowEntity.java, UmbrellaCockatooEntity.java, WoodcockEntity.java) to
`SynchedEntityData.Builder`. Across all **17** bird classes declaring
synchronized fields, the source parity script checks the original
field accessor names, default values, registration order and inherited
registration chaining. The other three subclasses inherit their
parent's converted initialization without adding fields. This is
an API migration, not a behavior change or a playable-jar milestone.

## 26.2 nullable annotation compatibility (2026-10-24)

Replaced the unavailable `javax.annotation.Nullable` import with
`org.jetbrains.annotations.Nullable` in 11 bird entity classes; all
original `@Nullable` sites and their semantics are preserved.
Whole-file source parity and a dedicated count check guard this
mechanical import migration. It removes one retired dependency symbol
without changing entity state or behavior.

## Bird client/server side-check migration (first batch, 2026-10-24)

The first nine bird classes now use `level.isClientSide()` instead
of reading the removed `level.isClientSide` field. 55
original side-checks retain their boolean conditions, negations,
return paths and exact branches. The whole-file parity scripts allow
only this mechanical call change alongside previous GeckoLib/26.2
adaptations. The remaining nine classes with this old API will be
converted in the follow-up batch.

### All bird client/server guard field queries converted (2026-10-24)

Converted the remaining nine entities (KiwiEntity.java, LongTailedTitEntity.java, MacawEntity.java, MynaEntity.java, NightHeronEntity.java, SeagullEntity.java, SparrowEntity.java, UmbrellaCockatooEntity.java, WoodcockEntity.java) to the
native `Level.isClientSide()` method, without changing branching
behavior. All **18** classes with old side-check fields are now ported
and covered by the complete GeckoLib and non-Gecko source-parity gates.
The two other original entity subclasses had no old field reads.

## World height-bound semantics preserved (2026-10-24)

Converted six files (five bird entities and habitat repopulation) to
`LevelHeightAccessor.getMinY()` and `getMaxY()`, which are the 26.2
equivalents of the old minimum/maximum build heights. The same
comparisons and margins for perching, flight and wild habitat checks
remain intact. Added an isolated Java API compile proof and updated
the original-source parity checks. The maximum bound is **not**
replaced with `getHeight()`, since that represents world height
rather than the absolute upper Y coordinate.

## 26.2 bird movement synchronization flag (2026-10-24)

The seven bird classes writing `Entity.hasImpulse` now use the native
`Entity.needsSync` flag. All **35**
references retain their exact original guards and assignments, including
flight movements, wing/stroke transitions and nudges. The whole-file
source parity compares the Forge original with only the symbol rename,
and an isolated Java API proof compiles direct 26.2 field access.
Rendering and network gameplay have not yet been runtime tested.

## Bird droppings, feather fan and photograph entity APIs (2026-10-24)

Migrated the original bird-dropping projectile, splat entity, feather-fan
projectile and photograph entity to the 26.2 `SynchedEntityData.Builder`
initialization signature without changing **any** accessor or initial value.
Converted side checks to `isClientSide()` and fan movement synchronization
to `needsSync` while preserving all original conditions. The photograph
entity now looks up feather item IDs through vanilla
`BuiltInRegistries.ITEM.getKey` and `Identifier` rather than the Forge
registry wrapper; the comparison and ID string are unchanged.

CI compares all four *complete original source files* against exact
mechanical 26.2 substitutions and fails on any other line change.
The action/use, physics, saved-state, spawning and rendering APIs are
**not yet fully ported**; this is not a playable build.


## 2026-10-29 — 26.2 source compatibility and fish-capture hook checkpoint

The private experimental branch has continued without changing game assets or
original bird decision constants:

- Relocated `Villager`, `IronGolem` and `AbstractFish` to their Minecraft
  26.2 packages in the server bird-tick handler, droppings gameplay, villager
  pricing mixin and night-heron fishing helper/event.
- Updated 17 remaining bird navigation, habitat, sound and social-behavior
  helper classes from `javax.annotation.Nullable` to JetBrains `Nullable`.
  Their annotated contracts and gameplay method bodies are unchanged.
- Replaced the night-heron's one Forge cancellable fish-capture event with a
  Fabric array-backed event. Registered listeners can still veto a capture.
  Rechecking capture eligibility after the callback, discarding a captured
  fish only after that recheck, and the existing anti-reentrancy reset remain
  in the original order.
- Added `tools/check-bird-relocated-api.py`,
  `tools/check-night-heron-capture-port.py`, the authored-source parity
  allowance for the heron event substitution, and two standalone Java
  API compilation probes. CI references these checks.

**Verification status:** The commits are in GitHub, but GitHub Actions attempts
on 2026-10-29 failed before a runner started (no job steps/logs). Consequently
the new probes have **not** been validated by a real Gradle compile. The
last executable `compileJava` result still contains legacy Forge/GeckoLib
and Minecraft 26.2 API errors across other source files. Client, server,
spawning, fish capture, existing-save, and visual checks remain outstanding.
This is **not a playable build or release**. Keep the PR draft and repository
private; do not merge to `main` or distribute asset-containing artifacts.


## 2026-10-29 — feather-fan combat events (source migration only)

The original Forge `LivingAttackEvent` / `LivingDeathEvent` feather-fan
listeners have been translated into Fabric 26.2
`ServerLivingEntityEvents.ALLOW_DAMAGE` and `AFTER_DEATH` callbacks
and explicitly registered in `GuaniaoFabricEntrypoint`.

- Birds in the existing `BirdTags.BIRDS` tag still reject damage from
  either a directly fired feather-fan projectile or a player directly
  wielding the wind feather fan. Unrelated damage continues normally.
- Feather-fan projectile or direct-player kills of non-player living entities
  still produce the original three poofs and `3 + random.nextInt(5)`
  kill-feather particles at their authored positions and velocities.
  The only random API substitution is `level.getRandom()` for
  removed field access.
- Added `tools/check-feather-fan-combat-port.py` for baseline-derived
  conditions/particle parity and an independent real-API compile probe
  `tools/port-probes/GuaniaoFeatherFanCombatProof.java`.

**Current verification:** Independent GitHub source inspection confirms
the key predicates, callback registrations and particle formulas. However,
the latest GitHub Actions job did not start a runner (zero executed steps);
the new check scripts and Gradle compilation were not executed. Fabric's
server damage/death callback dispatch may differ in timing from Forge event
priorities, and in-game tests remain mandatory. Neither the complete
port nor a distributable JAR is ready. Keep the draft PR unmerged and
respect the asset distribution restrictions.


## 2026-10-29 — spawn-reason API and flyby initialization checkpoint

The private source branch now uses Minecraft 26.2 `EntitySpawnReason`
in kingfisher habitat checks and all four naturally occurring flyby
initializations (sparrow, budgerigar, night heron and columbid).
The old `SPAWN_EGG` reason maps to `SPAWN_ITEM_USE`, preserving the
kingfisher manual-spawn exemption alongside `COMMAND`.
Flyby initializers now use the current four-argument `finalizeSpawn`
signature, and the vanilla mob-spawning gamerule uses
`GameRules.SPAWN_MOBS` in the native 26.2 gamerule package.

Source comparison against pre-migration commit `dbeee5f` confirms
that only these API substitutions were applied, with no flock size,
weights, timing, destinations, path decisions, habitat scan or authored
biome-rule changes. New `tools/check-bird-spawn-reason-port.py` protects
that mechanical transformation, and a standalone compile probe tests
the 26.2 enum, gamerule and mob initializer method signatures.

These checks are source-level only until the GitHub Actions runner starts.
The latest workflow attempt did not run build steps. Unported Forge event
handling and broader spawning integration remain, so spawning in an actual
world is **not yet verified**. No released or playable build.


## 2026-10-29 — native weighted bird spawning and repopulation (source checkpoint)

Migrated the prior Forge `PotentialSpawns` adjustment logic into an explicit
`BirdSpawnConfigEvents.adjustedPotentialSpawns` function for Minecraft 26.2,
then wired both call sites in the existing bird repopulation scheduler.

Minecraft 26.2 represents biome spawn pools as `WeightedList<SpawnerData>`
and each entry as `Weighted<SpawnerData>`. The `SpawnerData` itself is a
record with `type()`, `minCount()` and `maxCount()` (weight is no longer a
field of `SpawnerData`). The new code keeps per-species spawn multipliers,
original hummingbird threefold garden cap, 1.0–1.15 songbird multiplier,
colony group sizes, regional/nearby population ceilings, entry cache limit
of 512 and unchanged biome entries for other mobs.

The loaded-world repopulator now:
- Adjusts candidate spawn weights through the original positional policy
  before applying its unchanged underrepresented-species adjustment.
- Draws through native `WeightedList.getRandom` using the same server RNG;
  the 4-bird pack limit, retry rate, scan budget and species/biome filtering
  remain in place.
- Rechecks the adjusted spawn list at each actual candidate position.
- Uses native 26.2 `SpawnPlacements.isSpawnPositionOk`, mob spawn and
  obstruction checks, four-argument `Mob.finalizeSpawn`, `EntityType.create`
  with the `NATURAL` reason, `getSpawnAABB` and
  `Mob.getMaxSpawnClusterSize` in place of the removed Forge wrappers.
- Uses `level.getRandom()` rather than old mutable random field access.

Added `tools/check-bird-weighted-spawns-port.py` and
`tools/port-probes/GuaniaoWeightedSpawnProof.java`, both registered in
the existing CI checks; source inspection confirms original numeric cadence
constants, garden multipliers, cap expressions, vanilla/non-bird spawn-entry
retention and both new call sites.

**Natural-spawn follow-up:** a dedicated `BirdPotentialSpawnsMixin`
now injects at the return of Minecraft 26.2 `NaturalSpawner.mobsAt`, only
for `MobCategory.CREATURE`, passing the finished biome/structure weighted
list through the same position-specific helper. Non-bird entries remain
unaltered and chunk-generation placement rules remain distinct. The mixin
is registered in `guaniao.mixins.json`, has `require = 1`, and a source
guard plus isolated Java callback-signature probe were added. This is an
**unverified injection until real client/server startup and spawning tests**.
Forge third-party PotentialSpawns vetoes still require separate compatibility
review rather than being assumed preserved.

GitHub Actions is currently failing before assigning a runner (zero steps
executed), so the new Java proof, checker, full compile, dedicated-server
start, actual world spawning, and client checks are **not verified**.
Do not merge the draft PR or distribute an asset-containing JAR.


### NaturalSpawner hook validation still outstanding

The new injection targets the private `NaturalSpawner.mobsAt` return
value (26.2 mappings) to restore normal natural-spawn weighting as well as
the custom repopulation flow. Before claiming working parity, run the
registered mixin in a **dedicated server** and on the client, verify that
the `mobsAt` target resolves at runtime, then test hummingbird gardens,
non-bird biome spawns, configured caps, low-weight species, colony flocks,
and chunk-generation exclusions. No third-party spawn-list hook parity
has been established. Do not merge until these tests and full compilation
pass.


## 2026-10-29 — private JAR build preparation: prank food and authored notes

The next source cleanup targets build blockers rather than new features:

- Prank-food marker keys and corrupted names now use Minecraft 26.2
  `DataComponents.CUSTOM_DATA` / `CUSTOM_NAME`, and item registration IDs
  come from vanilla `BuiltInRegistries` instead of Forge registries.
- `PrankFoodEvents` uses Fabric `UseItemCallback` to begin eating; a
  guarded, server-side `LivingEntity.completeUsingItem` mixin applies the
  original nausea/hunger/slowness effects when the held item still matches.
  Client-only prank-food and dropping tooltip callbacks are registered
  separately so dedicated-server class loading stays side-safe.
- All authored bird note books now use the 26.2 `WRITTEN_BOOK_CONTENT`
  component (unchanged title, author, translatable page and resolved flag).
  The `GuaniaoNote` marker is stored in `CUSTOM_DATA`; reading awards the
  same bird-note and original five developer-author advancements.
- Bird advancement IDs migrated to `Identifier` and vanilla
  `AdvancementHolder` reward/progress access.
- Added source parity checks `tools/check-prank-food-port.py` and
  `tools/check-bird-note-components-port.py`, and two isolated real-API
  Gradle compile probes. In this environment checks were inspected at
  source level; GitHub Actions has not executed them yet.
- `build-private-fabric-26.2.cmd` offers a local Windows/Java 25 build
  path: first compile isolated 26.2 APIs, then run the FULL `clean build`
  (including strict migration audit), then confirm the resulting JAR
  contains `fabric.mod.json`. No successful JAR will be advertised
  merely because a subset compiles.

**Still unresolved for a complete playable JAR:** the original Forge
global loot-modifier serializers (including chest note generation), other
Forge event classes and custom network packets, GeckoLib 4 renderers,
camera/photo screens and packet paths, and remaining 26.2 source API
incompatibilities. The prank-food mixin and written-note creation need
real server and client tests; their runtime equivalence is not yet proven.

**Build runner status:** GitHub Actions repeatedly fails before a runner
starts (runner ID 0, zero steps), including a manual re-run; this cannot
be used as evidence of Gradle compilation success. Do not release or
share any modified asset-containing JAR without the original author's
written permission. Keep PR #1 draft and main untouched.


## 2026-10-29 — native Fabric networking and runtime loot milestones (source-only)

Three scoped migration checkpoints were added to the private
`port/fabric-26.2` development branch. These are **not** evidence of a
successful mod compilation or playable JAR:

1. **Charged feather fan** — `FeatherFanPiercePacket` now implements
   `CustomPacketPayload` and uses a zero-field `StreamCodec.unit`,
   registered with `PayloadTypeRegistry.serverboundPlay` before a
   `ServerPlayNetworking` receiver. `FeatherFanClientEvents` now uses
   Fabric's `ClientPreAttackCallback`; only new clicks while the fan is fully
   charged send the packet, and the original server-side held-item check and
   `tryLaunchPiercing(player)` are retained. The client hook is now registered
   in the Fabric client initializer.
2. **Three-boolean multiplayer configuration** — `BirdRuntimeConfigPacket`
   is now a clientbound payload with its original wire field order: leaf
   passage, April Fools, sky-bird ecology. Native networking sends it on the
   existing player-join callback and broadcasts after successful config saves.
   The client registration applies the same `BirdConfigManager.applyRemoteRuntime`
   values, without client-only symbols in common packet classes. The remaining
   serverbound save-config packet and its GUI channel still use legacy Forge.
3. **Runtime loot** — all original global loot modifiers now register
   `LootTableEvents.MODIFY_DROPS`: handwritten bird notes use their unchanged
   loot-table tier/rarity rules; feather-fan books still use 4% chest or
   0.25% monster odds; earthworms still use 20% chance only for a
   survival player's shovel on the configured forage ground tag. This
   preserves original drops and is additive. The old Forge loot-modifier
   registry file now acts as the single Fabric callback registrar, preventing
   accidental double registration.

Regression guards added:
- `tools/check-feather-fan-network-port.py`
- `tools/check-runtime-config-network-port.py`
- `tools/check-bird-note-loot-port.py`
- `tools/check-bird-all-loot-modifiers-port.py`

Standalone 26.2 compilation probes added to `verifyRegistriesPort`:
- `GuaniaoFeatherFanPacketProof`
- `GuaniaoRuntimeConfigPacketProof`
- `GuaniaoBirdNoteLootProof`

**Outstanding blockers to a private JAR:** legacy `GuaniaoNetwork`
still refers to Forge SimpleChannel and the remaining original
photo/config/crafting packets have not been moved to Fabric payload codecs.
GeckoLib entity and item renderers, menus, camera-photo client code and many
other Forge APIs still fail full compilation. Source-level checks are useful
but cannot validate cross-loader behavior. A real Java 25 `compileJava`
and `build` followed by dedicated server and client startup are mandatory.

**GitHub Actions status at this checkpoint:** workflow jobs still fail before
a runner is allocated (runner ID 0, zero executed steps). No JAR artifact
was generated or made available. Keep the PR draft, `main` untouched and
do not distribute asset-containing binaries without the author's consent.


## 2026-10-29 — native seven-packet photograph transfer checkpoint

The private 26.2 branch now has a complete **typed transport path** for
the existing photo transfer feature, retaining the original payload fields,
field order, byte-array bounds and server-side session checks:

- Client to server: `BeginPhotoUploadPacket`, `PhotoUploadChunkPacket`,
  `FinishPhotoUploadPacket`, `PhotoRequestPacket`.
- Server to client: `PhotoCaptureResultPacket`,
  `PhotoDownloadStartPacket`, `PhotoDownloadChunkPacket`.
- All seven implement Minecraft's `CustomPacketPayload` with
  `StreamCodec<RegistryFriendlyByteBuf, T>`; codec registration occurs in
  the common Fabric initializer on the correct directional registry before
  receiver installation.
- The original `PhotoClientRepository` sends all four serverbound messages
  through `ClientPlayNetworking`. The server's `PhotoUploadManager`
  sends its six results/chunks through `ServerPlayNetworking`. The Fabric
  client initializer registers all three receiving callbacks and passes the
  decoded values to the original client photo repository.
- The existing `PhotoTransferLimits` protections (including maximum chunk
  and photo ID/hash sizes) and server-side session/authorization checks are
  unchanged. Source comparison of each original vs. migrated Java
  `encode` and `decode` method body confirmed **all 14 methods match
  exactly**.
- The regression gate `tools/check-photo-transfer-network-port.py` also
  asserts directional registration, payload bounds, unchanged codecs and
  complete client/server send sites. A standalone 26.2
  `GuaniaoPhotoTransferPacketProof` compiles the typed-chunk API separately
  once CI runners become available.

**Not yet a functional camera:** Forge-based `SetCameraSettingsPacket`,
`SetCameraFilterPacket`, frame-workbench packets, the general
`GuaniaoNetwork` SimpleChannel and camera GUI/rendering dependencies still
need conversion. Do not describe photo networking or camera screenshots
as tested in-game until a full client/server build loads and transfer
round-trips succeed. The author-required asset distribution restrictions
continue to apply. This checkpoint neither produces nor authorizes a
redistributable JAR.


## 2026-10-29 — full original play-protocol mapping and command port (source checkpoint)

All **15** original Forge `GuaniaoNetwork.CHANNEL.messageBuilder` entries
are now represented exactly once by 26.2 Fabric
`PayloadTypeRegistry.serverboundPlay/clientboundPlay` registrations.
Compared the original class list against the branch: **15 originals, 15
Fabric entries, zero missing, zero duplicates**. The original direction
and server/client receiver for each are retained in the Fabric initializers.

Recent contained migrations:
- `SetCameraFilterPacket`, `SetCameraSettingsPacket` and
  `CameraClientCapture`: native Fabric serverbound settings transport;
  preserve original camera-item check, state codecs and non-finite focal/
  focus-distance rejection.
- `CraftFramePacket`, `FrameWorkSnapshotPacket`, frame screen and menu:
  native serverbound crafting and clientbound owner-only work snapshots;
  item previews now use 26.2 `ItemStack.OPTIONAL_STREAM_CODEC` with
  `RegistryFriendlyByteBuf` instead of retired `writeItem/readItem`.
- `OpenBirdConfigPacket`, `SaveBirdConfigPacket`, `BirdConfigScreen`:
  preserve original `BirdConfigPacketCodec` wire fields and server-only
  config authorization, population-tracker rebuild and droppings reset
  behavior. Operator check uses 26.2 `Permissions.COMMANDS_MODERATOR`.
- `BirdConfigCommands` and `PhotoAdminCommands`: replace Forge's
  command-registration event with Fabric `CommandRegistrationCallback`
  while retaining the authored command and admin operations.
- The old Forge `SimpleChannel` body in `GuaniaoNetwork` is gone;
  this legacy class is a server-only send compatibility adapter.
  All known client senders now use `ClientPlayNetworking` directly.

`tools/check-final-network-port.py` compares original vs migrated
camera/config/frame encoder and decoder bodies, original network IDs,
directions and registrations, command names, permission checks, and
searches the entire Java source tree for obsolete
`GuaniaoNetwork.sendToServer` and `GuaniaoNetwork.CHANNEL` usages.
The isolated `GuaniaoFinalNetworkingProof.java` compiles command
registrations, 26.2 permission capabilities and registry-aware frame
preview codecs once the CI runner is able to run.
Source inspection confirmed all 15 registered payload classes, not a
working in-game client/server protocol.

**Private JAR release gate still not passed.** The full Java sources
retain other Forge and legacy GeckoLib/client-screen APIs. GitHub
Actions jobs have been failing with runner ID 0 and zero steps executed.
Full `./gradlew compileJava`, `build`, server startup, player join,
all command paths, the photo-transfer round-trip and visual renderer
tests are **not verified**. The draft PR is not ready to merge. Respect
the original model/texture rights; do not distribute a modified
asset-containing JAR without the author's permission.


## 2026-10-29 — GeckoLib 5 bird render-state and asset staging checkpoint

This milestone addresses one of the remaining critical blockers for a
private Java 25 / Fabric 26.2 test build: GeckoLib 4 renderer methods
cannot directly access their entity with GeckoLib 5's render-state pipeline.

- **Nine bird models** now snapshot the original per-entity mutation
  texture through `BirdMutationRenderData.MUTATION_TEXTURE`:
  Kiwi, Sparrow, Myna, LongTailedTit, Seagull, NightHeron, Crow,
  Macaw and Budgerigar. Mutation selection still calls the original
  `BirdMutationTextureFactory.textureFor(originalBaseTexture, bird)`.
  It is never replaced with a shared last-rendered-bird variable.
- **Six bird renderers** (Kiwi, Sparrow, Myna, LongTailedTit, Macaw
  and Budgerigar) now use GeckoLib 5's `scaleModelForRender`
  with a per-entity float data ticket captured in the model.
  Their original shadow radii and original `getModelRenderScale()`
  values are preserved. These six renderers are now registered using
  Fabric's client-only entity renderer registration event.
- Macaw and Budgerigar palette lookups still use each bird's original
  variant-specific base texture. Their inherited texture getter
  signatures now consistently return `Identifier`. The budgerigar's
  original tame advancement ID and grant criterion remain unchanged,
  using the 26.2 advancement-holder accessor instead.
- `BirdMutationTextureFactory` uses Minecraft 26.2 `Identifier`
  for cached and registered dynamic textures. Its original pixel
  recoloring formulas, 24-frame rainbow animation and cache keys are
  unchanged; these still require real client graphics compilation
  and texture reload tests.
- **Assets:** original `assets/guaniao/geo/*.geo.json` and
  `assets/guaniao/animations/*.animation.json` remain untouched.
  Gradle `processResources` stages byte-identical copies under
  GeckoLib 5's `geckolib/models` and `geckolib/animations`
  resource directories. `GuaniaoGeckoAssets` converts legacy
  authored IDs to GeckoLib 5 short lookup IDs. The nine migrated
  bird models and five previously migrated static models use it.
- Added `tools/check-geckolib-bird-renderstate.py` and
  `tools/check-geckolib-assets-26.2.py` to CI and updated
  `tools/check-geckolib-static-models.py` to require the short
  GeckoLib 5 resource IDs, while retaining source-model parity.
  Added isolated `GuaniaoBirdRenderStateProof.java` to
  `verifyRegistriesPort`.

**Not yet compiled or playable.** Some bird renderers still use
GeckoLib 4's `preRender` and legacy held-item layers. Custom
animation/Molang handling in the hummingbird, kestrel, cassowary,
woodcock, cockatiel and umbrella cockatoo requires a separate
render-state migration. Only six bird renderers are registered,
and 26.2 dynamic texture upload APIs and remaining client GUI
render paths have not been validated. Real Java 25 Gradle
compilation, dedicated server/client startups, mutation textures,
resource reload and in-game visual tests are required before
providing a private test JAR. Never redistribute the author's
restricted media assets without written permission.


## 2026-10-29 — four more GeckoLib 5 bird renderers (source checkpoint)

Resumed from the existing nine model ports and six client renderer
registrations. This pass added **pigeon, spotted dove, cockatoo and
cockatiel**. The Fabric client now registers **10 of the original 19
bird entity renderers**, backed by 12 converted GeckoLib models
(two columbid species share one model).

- **Columbid model:** pigeon and spotted dove use the shared
  `ColumbidModel`, preserving gray pigeon, white pigeon and spotted
  dove resource selection via the individual entity's
  `getTextureResource()`, mutation-palette snapshot and individual model
  scale. Both original shadows (0.15 and 0.14) remain.
- **Cockatoo:** model and per-bird scale are now GeckoLib 5 render state.
  The original 0.751-pixel toe correction uses
  `adjustRenderPose(RenderPassInfo)`, after the renderer applies the
  per-bird scale, so it remains proportional rather than becoming a
  hard-coded global translation.
- **Cockatiel:** each bird's original five emotional crest rotations
  (relaxed 0, happy -0.12, curious -0.22, alert -0.38, afraid -0.56)
  are snapshotted into a float `DataTicket`, then added to the animated
  crest `BoneSnapshot` by `adjustModelBonesForRender`. These offsets
  augment animation, not replace it. The five authored texture variants,
  mutation colors, individual model scales and 0.12 shadow remain.
- Updated inherited 26.2 `Identifier` texture getter signatures for
  these birds and extended whole-file GeckoLib parity guards to permit
  exactly those API substitutions.
- Expanded `tools/check-geckolib-bird-renderstate.py` for the two
  columbid palettes, cockatoo toe alignment, all five cockatiel crest
  states and 10 unique registry entries; expanded
  `tools/check-geckolib-assets-26.2.py` for 12 models. Added
  `tools/port-probes/GuaniaoCockatielCrestProof.java` to the existing
  Gradle isolated compile task, checking the real GeckoLib 5 bone
  snapshots/data-ticket APIs.

**This is a source migration, not a successful executable test.**
Still to migrate or fully verify: nine other renderers, remaining
held-item layers, GeckoLib custom procedural models/pose renderers,
camera block/item renderers, menus and other old Forge/Minecraft APIs.
The full Java build, Minecraft client/server startup, species variant
render comparisons, crest rotations and toe alignment must pass before
a private test JAR is usable.

GitHub Actions on this branch has repeatedly failed before a runner
was allocated, so source checks and compilation have not run in CI.
Use the existing `build-private-fabric-26.2.cmd` with Java 25 for a
local contributor-only build; if it fails, treat the JAR as not ready.
The original media distribution restrictions still apply; keep the
draft PR private and do not release a modified asset-containing JAR
without the copyright holder's permission.


## 2026-10-29 — cassowary and woodcock procedural GeckoLib 5 ports

Two more bird renderers now participate in Fabric client registration:
**12 of 19 registered bird species**, with **14 GeckoLib bird models**
migrated at source level (the two columbids share one model).

- **Cassowary**: original gaze remains head-led, with separate
  `head`, `neckUpper`, and `neckLower` pitch/yaw driven by
  `getClientGazeWeight()`; the exact 0.001 cutoff and
  `Mth.DEG_TO_RAD` math are preserved. The six weighted rotations are
  snapshotted as GeckoLib 5 `DataTicket<Float>` values per bird and
  added *after* authored bone animation. Eye bones remain untouched.
  The per-bird scale and 0.493 shadow remain.
- **Woodcock**: original fixed `root_shape` 0.45× scale is applied
  after animated bones, retaining the workaround for animation
  transition size flicker. The separate `wing_left`,
  `wing_right` and `fly` rigs use the actual per-bird flight
  flag in a GeckoLib 5 render-state ticket, now implemented through
  bone snapshots' `skipRender` and `skipChildrenRender` methods.
  The 0.16 shadow, individual scale and mutation textures are kept.
- `tools/check-geckolib-bird-renderstate.py` now audits both
  procedural bone conversions against their original animation
  decisions and counts 12 unique Fabric bird registrations.
  `tools/check-geckolib-assets-26.2.py` covers 14 model lookups
  and byte-preserves all authored model/animation JSON.
- Added `GuaniaoProceduralBoneProof.java` to the Gradle isolated
  26.2 API compile checks for `BoneSnapshots`, `DataTicket`,
  post-animation rotation/scale and wing/child visibility.
  Confirmed GeckoLib 5 public Java API exposes those methods;
  **this does not prove the mod compiles**.

**Private JAR remains blocked.** Seven bird renderers have more
complex held-item layers, owner positioning, procedural poses or Molang
animation handling; other Forge/Minecraft 26.2 migration work remains.
No in-game rendering or full Java compilation has been verified due
to GitHub Actions failing before runner allocation. Keep this as a
draft and do not redistribute original copyrighted assets.


## Held-item renderer checkpoint (2026-10-29)

The Fabric client entrypoint now registers **15 of 19 original bird renderers** in
`GuaniaoFabricEntityRenderers`. The newest migrated species are Crow, Seagull
and Night Heron. Unlike earlier, simpler renderer conversions, these three
carry live item stacks on animated GeckoLib bones.

- Crow keeps the original `mouth_anchor`, 0.8x item scale, deterministic
  two-axis pose variations, pose seed and falling-back-to-entity-ID hash.
- Seagull keeps the `foot_right` attachment and authored 0.46x item scale,
  rather than moving its food to the beak.
- Night Heron keeps the `fish_anchor` bill location, only renders edible
  fish, and retains the distinct smaller/surface-forward delivery pose.
- All three use GeckoLib 5 render-state snapshots and deferred per-bone item
  submission, not the retired Forge/GeckoLib 4 immediate buffer hook.
- The original shadow radii, per-bird mutation size snapshots, texture
  selection, authored model files and animation JSON are unchanged.
- `check-geckolib-bird-renderstate.py` now guards all 15 registrations,
  mutation scales and the carry-pose constants against `origin/main`.
  `GuaniaoHeldItemRenderProof` additionally checks the real GeckoLib 5
  item-model snapshot and submission API when Gradle's isolated verification
  task can run.

**Still not release-ready.** Hummingbird, Kestrel, Kingfisher and Umbrella
Cockatoo still need full renderer conversions, including custom Molang, owner
perching, bone tracking and animation effects. Many unrelated Forge source
files still block `compileJava`, and actual client and dedicated-server
behavior remains untested. The GitHub Actions runs observed at this checkpoint
are failing *before any job steps start* (zero step summaries, no usable log),
so a passing source-level check cannot currently be claimed from CI. No
playable JAR has been produced, and private assets must not be published.


### Kestrel in-progress: GeckoLib 5 model and talon layer

The next renderer is underway, but **Kestrel is not yet counted among the 15
registered bird renderers**. Its model now captures the actual mutation
texture, individual render scale and `hasExtendedTalons()` signal in
GeckoLib 5 per-bird render state. Its carried-item layer now snapshots the
carried item through GeckoLib 5 and submits it on the animated `root` bone.
The original 0.018/-0.095 offset, 90/135-degree angles and 0.46 scale are
unchanged, with parity checks committed.

Still required before registering Kestrel: recreate the original post-animation
leg/root extended-talon resets using bone snapshots, preserve owner head-riding
render offsets and pitch, flight pitch and bank, and name visibility when
learning fetch. None of these behaviors should be silently discarded just to
raise the renderer count.

As of the latest observed workflow run, the private Fabric compile job fails
before GitHub allocates a runner (runner_id 0, zero job steps). This is a CI
scheduling/availability failure, **not** a Gradle compiler diagnostic.
Check GitHub Actions billing/limits and runner availability separately;
without a runner, no source-level build or JAR validation has happened.


## Next checkpoint: 17/19 renderer registrations and Kingfisher staged (2026-10-29)

The Kestrel renderer is now registered on Fabric. It captures owner-head
position, body yaw and pitch for owner perching, preserves flight pitch/bank
when away from the owner, restores the original extended-talon leg and root
bone transforms *after* authored animation, and retains the fetch-learning
nameplate and the root-held item.

The Hummingbird renderer is also registered, with the original tiny size,
0.06 shadow radius, owner-head position and pitch, seven head-perch support
bones, and head-local garden item. The eight original Molang expressions
for flight time, wing stroke, forward/backward/lateral blending, wing pitch,
wing yaw bias and visual bank now come from GeckoLib 5 per-actor queries
rather than overwriting process-global values from the last animated bird.

Kingfisher is **partially migrated but not registered**. Its GeckoLib 5 model
now captures the original two bill-handling dips and later swallow lift
without changing the keyframe pulse equations; the held-fish layer keeps
the original size, tilt, head anchor and fully-swallowed visibility cutoff.
The legacy renderer remains until the per-bone defecation anchor can be
translated faithfully into GeckoLib 5's deferred bone-position listener.
Disabling this effect merely to increase the renderer count is not an option.

Source parity checks cover Kestrel pose and food, Hummingbird Molang,
support bones and garden item, Kingfisher eating/fish and all the new asset
lookup bridges. **Compilation and in-game testing are still unverified.**
GitHub Actions jobs on this branch have been failing before a runner starts,
independently of the Java migration. Kingfisher and Umbrella Cockatoo
remain the two bird-renderer blockers; the larger Forge gameplay/client
migration also remains incomplete.


## 2026-10-29 — 19 of 19 Fabric bird renderers registered (source-level)

All nineteen original bird species now have GeckoLib 5 renderer registrations.
The final two source conversions are Kingfisher and Umbrella Cockatoo.

- **Kingfisher**: original 0.10 shadow and -0.006486-pixel sole correction;
  individual mutation texture and scale; whole-body dive flight pitch; original
  two handling dips and swallow lift; progressively shrinking head-held fish;
  and the animated dropping origin measured from the actual `body` bone with
  GeckoLib 5's per-bone render callback. Only a real client visual test can
  verify that the new deferred matrix convention exactly matches the 4.x
  emitter; the entity's fallback source-side dropping offset remains intact.
- **Umbrella Cockatoo**: 0.20 shadow, individual texture/scale, owner head
  offset/yaw/pitch, authored head-roll and per-entity emotion history. The
  original sampled emotion channels are added to `BoneSnapshots` rather than
  changing global cached `GeoBone` state. The two original derived clips
  (`animation.idle_diff_2_residual` and `animation.owner_perch`) are
  rebuilt from GeckoLib 5's immutable baked animation data on reload, with
  explicit neutral channels to clear earlier flight transforms.
- A Kestrel method override visibility mismatch was corrected to match the
  GeckoLib 5 renderer superclass.
- `tools/check-geckolib-bird-renderstate.py` now checks all 19 Fabric
  registrations, their original positional/animation decisions and the final
  derived-clip helper API shapes. `tools/check-geckolib-assets-26.2.py`
  checks all 17 mutation-model paths plus Hummingbird. Authored assets were
  **not** changed.
- `tools/port-probes/GuaniaoFinalBirdRendererProof.java` was added to
  `verifyRegistriesPort` as an isolated Java 25 / GeckoLib 5 compile probe
  for per-bone rendering and immutable synthesized animation objects.

**This is NOT yet a successful build or test JAR.** Many non-renderer Java
files still use Forge, obsolete Minecraft mappings and prior GeckoLib hooks.
Neither full `compileJava`, Fabric client startup nor dedicated-server startup
has been verified. The latest GitHub Actions Fabric run (41000616520) again
failed before executing any job step, so there are no compiler diagnostics.
Restoring a working runner or collecting Java 25 local compile output is the
next actionable requirement, followed by full feature and visual parity tests.
Keep the original copyrighted media and resulting test artifacts private.


## 2026-10-29 — Restore Fabric 26.2 custom-menu wiring

- The two original Forge `IForgeMenuType` registrations (`crow_nest` and
  `frame_workbench`) have been replaced with Fabric 26.2
  `ExtendedMenuType<Menu, BlockPos>`, preserving both registry IDs.
- A `RegistryFriendlyByteBuf` stream codec writes/reads the exact block
  position that both original Forge opening packets sent.
- Both block interactions now open an `ExtendedMenuProvider<BlockPos>`
  directly on the server. The original menu display names, server-side
  `CrowNestBlockEntity` automatic rummaging, and per-player persistent
  `FrameWorkbenchBlockEntity` job sessions remain in place.
- Client factories now use an explicit `BlockPos` overload, while their
  existing buffered constructors remain available; the original server-side
  block-entity constructors remain unchanged.
- The Fabric initializer flushes the new menu registry. The client initializer
  registers `CrowNestScreen` and `FrameWorkbenchScreen` with vanilla
  `MenuScreens`.
- `tools/check-extended-menus-port.py` verifies source identity and exact
  original menu-opening, title and gameplay invariants; the CI workflow
  includes the check. `GuaniaoExtendedMenuProof.java` is an independent
  Java 25/Fabric API compile probe for the new menu type and opening codec.

The new registrations are still **uncompiled** and the two screen classes
require the remainder of their Minecraft 26.2 client GUI migrations. Crow Nest
still has the original Forge-specific custom block break-particle extension,
and other placeable features share that extension. Those effects must be
migrated without removing the authored colored debris effects. This remains
a private work-in-progress; no usable test JAR has been produced.


## 2026-10-29 — Client block particles, all eight particle providers and Crow Nest 26.2 GUI

**Restored the original custom debris rather than removing it.** All six
block families—Bagged Fries, Bird Baths, Bird Cages, Crow Nests, Breadcrumb
Piles and Dropping Stains—now intercept `ClientLevel.addDestroyBlockEffect`
and `ClientLevel.addBreakingBlockEffect` via a *client-only* mixin. In
Minecraft 26.2 these are the actual terrain-particle generation methods;
old `ParticleEngine.destroy/crack` methods no longer exist. The client mixin
delegates unknown block types to the vanilla implementation. The original
color palettes, particle counts, velocities and per-variant effects remain
identical; an actual `BlockHitResult` is used to retain the original hit
location when available. Removed all six Forge `initializeClient` hooks.

**Converted all eight authored particles.** The seven feather/combat effect
classes (Kill Feather, Burial Wind, Burial Cyclone, Riven Split, Riven Streak,
Hunting Mark and Hunting Streak) retain *exact* original physics, delay,
sway, spin, fade and size formulas. Only the removed
`TextureSheetParticle`/provider signatures were changed to
`SingleQuadParticle`, `SingleQuadParticle.Layer.TRANSLUCENT` and the
26.2 random-aware `ParticleProvider` factory. Placeable Fleck also retains
its original gravity/rolling/deceleration/lifetime and now sets tint on the
correct single-quad subclass. `GuaniaoFabricClientEntrypoint` registers
all eight sprite-aware `ParticleProviderRegistry` factories, restoring the
client registration formerly performed by Forge's
`RegisterParticleProvidersEvent`.

**Converted Crow Nest's actual inventory screen.**
`CrowNestScreen` now uses 26.2 `GuiGraphicsExtractor`, a 176×222
vanilla chest layout, the same masked 18-pixel loot grid and merging, the
original rummage progress bar, tooltip and exact server-side
`TAKE_TREASURE_BUTTON_BASE` ID; input uses the new
`MouseButtonEvent`. Its previous direct `GuiGraphics` methods no longer
exist in 26.2, and we preserved visual stacking between inventory slots
and loot tooltips.

Source-level regression checks added to CI:
`check-block-break-particle-port.py`,
`check-particle-providers-26-2.py` and
`check-crow-nest-screen-26-2.py`. These verify *author-preserved*
effects and UI behavior; they do **not** replace compilation or an
in-game visual test. The much larger `FrameWorkbenchScreen` still needs
the 26.2 `GuiGraphicsExtractor` migration, as do other Forge-only
client classes. The private Fabric runner still fails before beginning
steps, and no complete mod JAR has been produced.


## 2026-10-30 — Frame Workbench GUI groundwork on Minecraft 26.2

The complete frame construction workflow and 3D rendering features are still
required. The 1.20.1 `GuiGraphics`/immediate `PoseStack` pipeline cannot be
migrated by simple method renames because 26.2 now uses extracted 2D graphics
and deferred 3D geometry; **the overall Frame Workbench screen is not yet
ready to compile or use**.

Completed and committed in this checkpoint:

- `FrameWorkbenchBackdrop`: 26.2 `GuiGraphicsExtractor` drawing and tinted
  vanilla birch wood quad with the original 0.48 shade; original gradients,
  trim, continuous texture and 38-pixel design retained.
- `FrameOutlineEditor`: same authored 32×32 mask, brush/erase, line,
  rectangle, ellipse, flood fill, grid, zoom and panning; switched only the
  drawing entry points to 26.2 extractor and native outline.
- `FrameNumberControl`: numeric input, exact BigDecimal formatting, range
  clamps, step buttons, precision slider and typed-edit focus preserved;
  converted widget extraction and `MouseButtonEvent`, `KeyEvent` and
  `CharacterEvent`. Uses the new native slider sprites rather than the
  removed nine-slice code.
- `FrameWorkbenchScreen`: migrated scaled mouse button, drag, release,
  scroll and keyboard event routing, including modal input routing and
  protection from clicking inventory slots through open palettes. Its
  custom truncated button labels now use extracted button sprites/text
  rather than the removed `Button.renderString`.
- `tools/check-frame-workbench-gui-26-2.py` in private CI asserts the
  unchanged outline algorithm and vital author-created GUI behavior;
  the original 3D/animated workshop render calls must still exist.
- The three independent GUI helpers are now included in Gradle's
  `verifyRegistriesPort` real API-compile source set so they can be tested
  without first compiling the entire unported rendering system.

**Unfinished:** full `FrameWorkbenchScreen` extraction/render methods,
`FrameCraftingProgress`, `FrameCraftingAnimation`, `FrameWorkshopBird`,
`FrameDesignRenderer` and item/texture submission need a coherent port to
the new 26.2 3D render pipeline. Keep 3D build preview, the assembly
animation, visitor bird, frame-to-slot delivery, palette modal, inventory
slots and interactions; do not remove or replace them with placeholders.
The ongoing absence of a GitHub Actions runner still prevents Java 25
compilation and private test JAR validation.


## 2026-10-30 — Workbench material picker and the 26.2 deferred-3D bridge

**Author-preserving material picker port:** `FrameMaterialPalette` now uses
Minecraft's 26.2 `Identifier`, per-item food components and vanilla
`BlockTags`/`ItemTags` rather than Forge's `Tags` and removed item APIs.
Its original live item-registry search, modded materials, color families,
category ordering, ten category labels and saved-design handling remain.
Missing optional-mod items become a safe empty item instead of crashing.
The migrated picker is included in `verifyRegistriesPort`, and
`check-frame-material-palette-26-2.py` compares original selection,
sorting and category invariants. **The full editor still has dependent 1.20.1
classes (`FrameDesign`, `FrameDesignRenderer`) to migrate.**

**Verified 26.2 3D architecture before altering the frame meshes:**
Minecraft's `GuiGraphicsExtractor` records GUI elements, and Fabric
provides `PictureInPictureRendererRegistry` and
`PictureInPictureRenderer<PictureInPictureRenderState>` for custom
offscreen 3D GUI rendering. The new
`GuaniaoGuiRenderStateAccess` client-only accessor and
`FrameWorkbenchPipBridge` submit custom render states into the actual
`GuiRenderState.addPicturesInPictureState` queue. No old immediate
`GuiGraphics` drawing is used by this bridge.
`GuaniaoFramePictureInPictureProof.java` exercises the official
Fabric renderer-registration, bounds, offscreen rendering and
`SubmitNodeCollector.submitCustomGeometry` APIs in an isolated
compile-only test. The probe is deliberately **not** registered at runtime:
there is no fake or simplified frame preview. The bridge and probe are
included in `verifyRegistriesPort` and source-parity checks in CI.

**Crafting-progress meadow:** `FrameCraftingMeadow` is a separate
26.2-ready UI-extraction helper. It preserves the original eleven
grass-side/grass-overlay tiles, four plants, plant sizes and positions,
and the original grass shade using 26.2's `BlockTintSource`.
Sprites come from the live block atlas, so resource packs are respected.
It is included in the isolated compiler gate, with
`check-frame-meadow-26-2.py` verifying the original geometry and art.
The existing `FrameCraftingProgress` caller still uses 1.20.1
rendering until its animated GeckoLib workshop bird is migrated;
the meadow helper has **not yet** been wired to replace that caller.

**Remaining end-to-end 3D implementation work:** migrate
`FrameDesignRenderer`'s quad/texture submissions to
`SubmitNodeCollector.submitCustomGeometry` and the new
`RenderTypes`; implement and register a real
`FrameWorkbench` `PictureInPictureRenderer` for the custom
3D frame, photo texture, depth and user rotation; migrate
`FrameCraftingAnimation` and `FrameWorkshopBird` GeckoLib
entity rendering to the same deferred pipeline (including the independent
observer and progress birds), then migrate the last
`FrameWorkbenchScreen` render methods and coordinate/scissor
handling. Only then is a visual end-to-end test meaningful.
The earlier Forge-only source outside the Workbench also still needs
migration. **No complete Fabric 26.2 test JAR is available yet.**


## 2026-10-30 — Exact authored 3D frame surface pipeline and NBT migration

**Extracted the ACTUAL original frame geometry algorithms.**
`FrameWorkbenchMeshGeometry` contains the exact `FrameDesign.rectangles`,
`FrameDesignRenderer.addBoundaryRuns`, `boundaryBounds`,
`boundaryRim`, `outline`, `occupied`, `bounds`,
`face`, `liningFace`, and `tile` algorithms (with only the
`FrameDesign.CellRect` type name relocated). The new isolated builder
takes the original 32 bitmap rows, window rows, frame dimensions,
depth, trim and glass mode. It retains front+rear backing, all normal
vectors, merged inner/exterior edges, trim, glass UVs, and texture tiling.
Source-equivalence was checked against the original 1.20.1 methods.
This is **not** a bounding-box or generic-frame substitute.

**Deferred 3D rendering implemented for original frame surfaces.**
`FrameWorkbenchMeshSubmitter` calls the Minecraft 26.2
`SubmitNodeCollector.submitCustomGeometry` API, preserving texture
coordinates, normals, lighting, color and alpha. IMPORTANT:
26.2 `TextureAtlasSprite.getU/getV` accepts normalized 0–1 UV
coordinates; multiplying by 16 here would corrupt frame textures.
`FrameWorkbenchPreview3DRenderer` is a real registered
`PictureInPictureRenderer` with its own depth target, centered pose,
user yaw/pitch, rear/front lining, actual wood frame, shaded inner
rim and optional trim. `FrameWorkbenchPreview3DState` carries the
deferred snapshot, and `FrameWorkbenchMeshFromDesign` maps an
original `FrameDesign` into this preserved mesh.

**Original block materials restored through 26.2 APIs.**
`FrameWorkbenchMaterialResolver` chooses the real block from the
current item registry (including other installed mods), resolves its
baked block-particle sprite through
`Minecraft.getModelManager().getBlockStateModelSet()`, applies
the vanilla block tint and the original paper shade, and checks
translucency/glass (including the vanilla-glass sprite fallback).
Missing optional-mod blocks use oak planks.
`FrameWorkbenchPreview3DFactory` combines this live material
data and mesh/angles into the new render state.

**FrameDesign data API port.** The immutable 39-kB
`FrameDesign` class now uses `Identifier`, Optional-aware 26.2
NBT field access, and `CustomData.update(DataComponents.CUSTOM_DATA)`
instead of removed raw ItemStack NBT getters/setters. The saved
`FrameDesign` NBT compound, version-3 field names, value limits,
mask decomposition, and binary packet field order stay unchanged;
`FrameWorkbenchScreen` now copies existing photograph item components
via `applyComponents(source.getComponentsPatch())` instead of discarding
photo ownership and modded data. A real 26.2 data-component API
compile-only probe and source parity test have been added to CI.

**Verification planned in private CI:** `check-frame-mesh-26-2.py`
compares geometry algorithms with the original sources and checks
live materials, registered PiP and state creation.
`GuaniaoFrameMeshGeometryRegression.java` is a standalone Java 25
test that actually constructs solid, windowed and irregular 3D masks
and checks UVs/normals, trim and invalid input; it is compiled/run
before Gradle in the workflow. These tests are **committed but have
NOT executed** while the GitHub Actions job has zero runner/steps.

**Still blocked:** connect `FrameWorkbenchPreview3DFactory.capture()`
and `FrameWorkbenchPipBridge.submit()` from the fully migrated
`FrameWorkbenchScreen.extractContents`. The screen's old
`GuiGraphics`/3D pose calls remain unported. The original photo
texture/crop, printable sticker decorations, staged assembly, observer
bird and independent progress bird still require deferred render-state
migration. These are **not** silently dropped: their original
source remains in the branch. No private playable test JAR yet.


## 2026-10-30 — Frame Workbench native 26.2 screen extraction and photograph cropping

The current port has progressed beyond the bare 3D-rendering API proof.
`FrameWorkbenchScreen.extractRenderState` now invokes the real
`FrameWorkbenchPreview3DFactory` + `FrameWorkbenchPipBridge` for
**photo-free** frames. It places the PiP at the original viewport
(virtual canvas X=79..231, Y=41..191) in screen coordinates, using
the existing 390x300 centered responsive scale, zoom, user yaw/pitch
and original orientation restrictions on the decoration page.
It deliberately does not draw a blank frame as a counterfeit preview
when a real photograph exists; the photo-texture cache is still pending.

The screen now has a 26.2 `extractContents` stage that translates
and scales the exact original 390x300 virtual coordinates for **real
vanilla inventory slots and child widgets**. Full-screen modal
palettes render their 140 material cells and tooltips in the new GUI
extractor and suppress inventory slot hover/drawing behind them;
the outline editor retains its zoom, authored mask and invalid
selection state. The main Workbench 2D extraction reproduces the
original wood panels, inventory slot backdrops, editable 32x32 mask,
grid and labels. This includes the legacy input workaround for scaled
sliders and pointer events. The previously missed old-style
`mouseDragged` forwarding was fixed.

**The real photograph layer is now represented by geometry and a
deferred submission path**, not a generated substitute.
`FrameWorkbenchPhotoGeometry` preserves the original author code's
per-window rectangle clipping, contain/cover, rotations, zoom,
offsets, UVs and relative depth behind the frame's front edge;
`FrameWorkbenchPhotoSubmitter` emits the photo's real source
texture through `SubmitNodeCollector.submitCustomGeometry`.
`FrameWorkbenchPreview3DState.Photo` now carries its image
identifier/UV snapshots, and
`FrameWorkbenchPreview3DFactory.captureWithPhoto` prepares it when
passed an actual photograph texture and dimensions.
`GuaniaoFramePhotoGeometryRegression` tests solid/no-photo frames,
letterboxing, cover, rotation, max zoom, offsets, and separated
windows (which must not blend through their wooden divider).
`check-frame-photo-26-2.py` guards source parity.

**Crafting-area 2D extraction:** original meadow, native food item,
scaled food fade, two bite fragments, work-stage labels, countdown
and ready flash are preserved by `FrameCraftingProgress2D` and
the screen's `extractCraftingArea2D`; these track the original
server-authoritative work/progress clocks. The second (independent)
progress bird is still real 3D GeckoLib content requiring a
separate deferred renderer: a 2D substitute was not introduced.
`check-frame-extraction-26-2.py` checks stage/modal/slot
behavior and original 3D methods remaining in source.

**Remaining before a playable JAR:** screen still contains legacy
`GuiGraphics` `render`/`renderBg` methods during this staged
migration; they must be replaced rather than assumed compile-ready.
The actual `PhotographTextureCache`/photo buffer API needs to
supply `captureWithPhoto` with the downloaded real image and
pixel size, after which photo-bearing previews can be queued.
The full staged construction, bird carrying the frame, observer
and progress birds, plus photograph stickers require 26.2 GeckoLib
and deferred texture submission. Many legacy Forge classes remain.
The private GitHub Actions job still has not reached a runner,
so the new Java API proofs and regression tests are committed
but have not executed; compilation and a JAR cannot be claimed.


## 2026-10-30 — Original photograph textures wired into the 26.2 3D Workbench

The Fabric Workbench preview now requests **real decoded JPEG pixel dimensions and the registered photo texture** from `PhotographTextureCache.readyImageFor`, then submits them via `FrameWorkbenchPreview3DFactory.captureWithPhoto` and the existing original window-clipping geometry. If an image has not downloaded yet, the preview does not counterfeit a blank frame. The authored photo ID/hash keys, transfer queue, fit, rotation, zoom, offsets and window mask are unchanged.

The cache's retired OpenGL `GL11`, `ResourceLocation`, `TextureUtil.prepareImage` and immediate texture upload were replaced with Minecraft 26.2 `Identifier` and backend-independent `DynamicTexture`. It retains the original 256-slot maximum, 60-second idle eviction, two-upload pacing and generation cancellation. As 26.2 `DynamicTexture` retains decoded `NativeImage` pixels as well as GPU storage, a 512-MiB resident native-pixel budget now evicts least-recently-used entries rather than retaining more than the original intended memory budget. The Fabric client entrypoint now pumps two uploads and preloads only held photos each client tick, clearing the texture cache on disconnect. Two uploads per **client tick** replaces the original two per **render frame** pending performance verification.

A new `tools/check-photo-texture-cache-26-2.py` source regression gate is included in private CI. **Not yet compiled or gameplay-verified:** Actions jobs still fail before any runner step. The other original Forge camera and photograph renderers, remaining workshop 3D GeckoLib pieces, and broader build integration remain pending. Validate photo download, texture reload, cropping, logout invalidation and Vulkan/OpenGL upload when a genuine client test is possible. No test JAR is available. `main` is unchanged.


## 2026-10-30 — Camera shortcut registration migrated from Forge to Fabric

Removed the Forge event-bus keybinding registration and `KeyModifier` coupling from `CameraKeyMappings`. Minecraft 26.2's real `KeyMapping.Category`, `InputConstants.Type.KEYSYM` and Fabric `KeyMappingHelper.registerKeyMapping` register the original three keys: V opens the filter library, C opens creative camera settings, and Tab focuses the camera. The existing guarded `consumeClick()` handlers now run from the Fabric end-of-client-tick callback instead of the unused Forge client event. A category translation matching the original Birdwatching/观鸟 label has been added to both authored languages, without removing the old translation keys.

The original saved binding migration still runs once after keymaps load, changes **only** a legacy R focus binding to the newer Tab default, rebuilds the key lookup and verifies the persisted options.txt line before setting the existing preference marker. User-remapped keys and the original first-use help/control behavior are preserved. Forge's modifier-state API does not exist in vanilla Fabric; this change cannot reinterpret historical Forge-only chord modifiers, and needs a game test with customized controls.

Added `GuaniaoCameraKeyMappingProof.java` to the isolated 26.2 compilation gate and `check-camera-key-mappings-26-2.py` to private CI. **Not yet tested in a full game/client build** because CI still fails before runner steps execute. Other camera event/rendering paths remain on legacy Forge APIs; camera operation is not yet parity certified. The private PR stays draft.
