#!/usr/bin/env python3
"""Guard the feather fan's original combat and save contract during Fabric 26.2 migration."""
from pathlib import Path
import re
import subprocess

PATH = "src/main/java/EdDYON/guaniao/content/fan/FeatherFanProjectileEntity.java"
old = subprocess.check_output(["git", "show", f"origin/main:{PATH}"], text=True)
new = Path(PATH).read_text(encoding="utf-8")

for pattern in (
    r"private static final (?:int|float|double) \\w+ = [^;]+;",
    r"public static final (?:int|float|double) \\w+ = [^;]+;",
    r"public enum FanState \\{[\\s\\S]*?    \\}",
    r"private enum PiercingArt \\{[\\s\\S]*?    \\}",
):
    before = re.findall(pattern, old)
    after = re.findall(pattern, new)
    assert before and before == after, f"Original projectile constants/states changed: {pattern}"

def saved_keys(src):
    start = src.index("addAdditionalSaveData(")
    stop = src.index("readAdditionalSaveData(", start)
    return set(re.findall(r'tag\\.(?:put\\w*|store)\\("([^"]+)"', src[start:stop]))

assert saved_keys(old) == saved_keys(new), "A saved field name was added or lost"
assert "ValueOutput tag" in new and "ValueInput tag" in new
assert 'Codec.STRING.listOf()' in new and 'UUIDUtil.CODEC' in new
assert "tag.getInt(\"FanState\").isPresent()" in new
assert "tag.getInt(\"StuckBlockX\").isPresent()" in new
assert "new IllegalStateException" not in new
assert new.count("this.damageTarget(") == 8, "Original eight damage calls changed"
assert "target.hurtServer(serverLevel," in new
assert new.count(".knockback(") == old.count(".knockback(") == 3
assert 'AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL' in new
assert "movementSpeed.getModifier(this.slowdownId())" in new
assert "movementSpeed.removeModifier(this.slowdownId())" in new
assert 'owner.getCooldowns().addCooldown(fan, RETURN_COOLDOWN_TICKS)' in new
assert 'this.spawnAtLocation(serverLevel, fan)' in new
assert "loadHitSet(List<String> saved, Set<UUID> hits)" in new
assert 'Skipped {} invalid saved feather fan target UUIDs' in new
assert 'protected void addAdditionalSaveData(CompoundTag tag)' not in new
print("PASS: all original fan states, combat constants, damage sites and saved keys survive 26.2")
