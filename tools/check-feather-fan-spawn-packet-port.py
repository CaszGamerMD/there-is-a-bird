#!/usr/bin/env python3
"""Guard the authored feather-fan states while moving projectile spawning off Forge."""

from pathlib import Path
import re
import subprocess


FILE = "src/main/java/EdDYON/guaniao/content/fan/FeatherFanProjectileEntity.java"
original = subprocess.check_output(["git", "show", f"origin/main:{FILE}"], text=True)
current = Path(FILE).read_text(encoding="utf-8")

old_packet = (
    "public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {\n"
    "        return NetworkHooks.getEntitySpawningPacket(this);\n"
    "    }"
)
new_packet = (
    "public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {\n"
    "        return super.getAddEntityPacket(serverEntity);\n"
    "    }"
)
assert old_packet in original, "Original Forge spawn packet changed"
assert current.count(new_packet) == 1, "Use the inherited vanilla 26.2 projectile spawn packet"
assert "net.minecraftforge.network.NetworkHooks" not in current
assert "import net.minecraft.server.level.ServerEntity;" in current

# A spawn packet must still carry the same synchronized fan state; none of the
# charged, returning, stuck or enchanted attack state-machine definitions change.
for regex in (
    r"public enum FanState \{.*?\n    \}",
    r"private enum PiercingArt \{.*?\n    \}",
    r"private static final EntityDataAccessor<[^;]+;",
):
    old = re.findall(regex, original, flags=re.S)
    new = re.findall(regex, current, flags=re.S)
    assert old and old == new, f"Fan state definition changed: {regex}"

print("PASS: feather-fan projectile uses the vanilla Minecraft 26.2 spawn packet")
print("PASS: original synchronized states and enchanted attack modes preserved")
