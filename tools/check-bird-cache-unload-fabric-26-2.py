#!/usr/bin/env python3
"""Check the four Fabric world-unload cache hooks preserve all authored lookup logic."""
from pathlib import Path
import subprocess

root = Path(__file__).resolve().parents[1]
prefix = "src/main/java/EdDYON/guaniao/content/bird/"
files = {
    "flock/BirdFlockManager.java": "    private record CacheKey(",
    "brain/BirdHabitatCache.java": "    private static long nextRefreshAt(",
    "nightheron/NightHeronLandingSelector.java": "    private static long nextRefreshAt(",
    "nightheron/NightHeronShorelineCache.java": "    private static List<BlockPos> scan(",
}

def clean_imports(src):
    src = src.replace("import javax.annotation.Nullable;", "import org.jetbrains.annotations.Nullable;")
    for old in (
        "import EdDYON.guaniao.GuaniaoMod;\n",
        "import net.minecraftforge.event.level.LevelEvent;\n",
        "import net.minecraftforge.eventbus.api.SubscribeEvent;\n",
        "import net.minecraftforge.fml.common.Mod;\n",
        "import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;\n",
        "@Mod.EventBusSubscriber(modid = GuaniaoMod.MOD_ID)\n",
    ):
        src = src.replace(old, "")
    return src

for rel, suffix in files.items():
    old = subprocess.check_output(["git", "show", "origin/main:" + prefix + rel], cwd=root, text=True)
    new = (root / prefix / rel).read_text("utf-8")
    old = clean_imports(old)
    new = clean_imports(new)
    old_head = old[:old.index("    @SubscribeEvent\n    public static void onLevelUnload(")]
    new_head = new[:new.index("    public static void onLevelUnload(ServerLevel level)")]
    assert old_head == new_head, f"Lookup, scoring or cache bounds changed in {rel}"
    assert old[old.index(suffix):] == new[new.index(suffix):], f"Cache contents or expiry changed in {rel}"
    assert "LEVEL_CACHES.remove(level)" in new, f"World cache no longer released by {rel}"
    assert "ServerLevelEvents.UNLOAD.register((server, level) -> onLevelUnload(level))" in new
    assert "net.minecraftforge" not in new

entry = (root / "src/main/java/EdDYON/guaniao/fabric/GuaniaoFabricEntrypoint.java").read_text("utf-8")
for name in ("BirdFlockManager", "BirdHabitatCache", "NightHeronLandingSelector", "NightHeronShorelineCache"):
    assert name + ".registerCallbacks();" in entry, f"{name} isn't wired during Fabric initialization"

print("BIRD CACHE UNLOAD OK: flock, habitat, landing and shoreline caches keep source bounds with Fabric cleanup.")
