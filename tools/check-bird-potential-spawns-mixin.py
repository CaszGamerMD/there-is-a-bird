#!/usr/bin/env python3
"""Require vanilla 26.2 NaturalSpawner to receive the original bird weight policy."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
source = (ROOT / "src/main/java/EdDYON/guaniao/mixin/BirdPotentialSpawnsMixin.java").read_text("utf-8")
config = (ROOT / "src/main/resources/guaniao.mixins.json").read_text("utf-8")
legacy = (ROOT / "src/main/java/EdDYON/guaniao/event/BirdSpawnConfigEvents.java").read_text("utf-8")
repop = (ROOT / "src/main/java/EdDYON/guaniao/event/BirdRepopulationEvents.java").read_text("utf-8")
assert "BirdPotentialSpawnsMixin" in json.loads(config)["mixins"]
for token in ("@Mixin(NaturalSpawner.class)", "@Inject(method = \"mobsAt\", at = @At(\"RETURN\"), cancellable = true, require = 1)",
              "MobCategory.CREATURE", "ServerLevel level, StructureManager structures, ChunkGenerator generator",
              "BlockPos pos, Holder<Biome> biome",
              "CallbackInfoReturnable<WeightedList<MobSpawnSettings.SpawnerData>>",
              "cir.setReturnValue(BirdSpawnConfigEvents.adjustedPotentialSpawns(level, pos, original))"):
    assert token in source, token
assert "original == null || original.isEmpty()" in source
assert "entries.add(original);" in legacy  # preserve all non-bird biome/structure spawn entries
assert "new Weighted<>(new MobSpawnSettings.SpawnerData(" in legacy
assert repop.count("BirdSpawnConfigEvents.adjustedPotentialSpawns(") == 2
assert "NaturalSpawner" not in repop  # custom repopulation calls helper itself
print("PASS: ordinary natural-spawn pool and custom repopulator both route to native bird modifiers")
