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
