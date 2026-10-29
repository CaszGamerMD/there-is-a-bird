#!/usr/bin/env python3
"""Check feather-fan damage immunity and death visuals against the Forge baseline."""
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
rel = "src/main/java/EdDYON/guaniao/event/FeatherFanCombatEvents.java"
old = subprocess.check_output(["git", "show", "origin/main:" + rel], cwd=ROOT, text=True)
new = (ROOT / rel).read_text("utf-8")
boot = (ROOT / "src/main/java/EdDYON/guaniao/fabric/GuaniaoFabricEntrypoint.java").read_text("utf-8")
assert "net.minecraftforge" not in new
assert "ServerLivingEntityEvents.ALLOW_DAMAGE.register(FeatherFanCombatEvents::allowDamage)" in new
assert "ServerLivingEntityEvents.AFTER_DEATH.register(FeatherFanCombatEvents::afterDeath)" in new
assert "FeatherFanCombatEvents.registerCallbacks();" in boot
assert "return false;" in new and "return true;" in new
assert "if (!victim.getType().is(BirdTags.BIRDS))" in new
for start, finish in (
    ("boolean projectileAttack =", "if (projectileAttack || meleeAttack)"),
    ("boolean projectileKill =", "if (!projectileKill && !meleeKill)"),
):
    assert old.split(start, 1)[1].split(finish, 1)[0] == new.split(start, 1)[1].split(finish, 1)[0], start
original_particles = old.split("        double x = victim.getX();", 1)[1].rsplit("    }", 1)[0]
expected_particles = original_particles.replace("level.random.nextInt(5)", "level.getRandom().nextInt(5)")
actual_particles = new.split("        double x = victim.getX();", 1)[1].rsplit("    }", 1)[0]
assert expected_particles == actual_particles, "authored particle positions, counts or velocities changed"
assert "victim instanceof Player" in old and "victim instanceof Player" in new
assert "return false;" in new.split("if (projectileAttack || meleeAttack)", 1)[1].split("}", 1)[0]
assert "return true;" in new.split("if (projectileAttack || meleeAttack)", 1)[1]
print("PASS: feather-fan bird immunity, hit attribution and original death particles preserved")
