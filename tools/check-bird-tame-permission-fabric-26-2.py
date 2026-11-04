#!/usr/bin/env python3
"""Guard remaining bird tame attempts using the existing Fabric veto event."""
from pathlib import Path
import subprocess

root = Path(__file__).resolve().parents[1]
parts = ("sparrow/SparrowEntity.java", "longtailedtit/LongTailedTitEntity.java",
         "columbid/AbstractColumbidEntity.java")
old_hook = "!net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, player)"
new_hook = "EdDYON.guaniao.event.BirdTamePermissionEvents.BEFORE_TAME.invoker().canTame(this, player)"
for part in parts:
    rel = "src/main/java/EdDYON/guaniao/content/bird/" + part
    old = subprocess.check_output(["git", "show", "origin/main:" + rel], cwd=root, text=True)
    new = (root / rel).read_text("utf-8")
    assert old.count(old_hook) == 1, f"Original tame cancellation changed in {part}"
    assert new.count(new_hook) == 1 and old_hook not in new, f"Fabric tame veto missing in {part}"
    assert "this.tame(player);" in old and "this.tame(player);" in new
    assert "stack.shrink(1);" in old and "stack.shrink(1);" in new

events = (root / "src/main/java/EdDYON/guaniao/event/BirdTamePermissionEvents.java").read_text("utf-8")
assert "EventFactory.createArrayBacked(Allow.class" in events
assert "if (!listener.canTame(bird, player)) return false;" in events
assert "return true;" in events
print("BIRD TAME VETO OK: sparrow, long-tailed tit and columbid use the existing cancellable Fabric event.")
