#!/usr/bin/env python3
"""Verify the charged fan's exact zero-argument C2S behavior survives Forge removal."""
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
BASE = "src/main/java/EdDYON/guaniao/"
def current(path):
    return (ROOT / (BASE + path)).read_text("utf-8")
def baseline(path):
    return subprocess.check_output(["git", "show", "origin/main:" + BASE + path],
                                   cwd=ROOT, text=True)

old_packet = baseline("network/FeatherFanPiercePacket.java")
packet = current("network/FeatherFanPiercePacket.java")
assert "fan.tryLaunchPiercing(player)" in old_packet and "fan.tryLaunchPiercing(player)" in packet
assert "player.getUseItem().getItem() instanceof FeatherFanItem fan" in old_packet
assert "player.getUseItem().getItem() instanceof FeatherFanItem fan" in packet
assert "StreamCodec.unit(new FeatherFanPiercePacket())" in packet
assert "implements CustomPacketPayload" in packet and "NetworkEvent" not in packet
network = current("fabric/GuaniaoFabricNetworking.java")
assert "PayloadTypeRegistry.serverboundPlay().register(" in network
assert "ServerPlayNetworking.registerGlobalReceiver(" in network
assert "packet.handle(context.player())" in network
assert "GuaniaoFabricNetworking.register();" in current("fabric/GuaniaoFabricEntrypoint.java")

old_input = baseline("client/fan/FeatherFanClientEvents.java")
inp = current("client/fan/FeatherFanClientEvents.java")
assert "FeatherFanItem.isFullyCharged(player)" in old_input and "FeatherFanItem.isFullyCharged(player)" in inp
assert "ClientPreAttackCallback.EVENT.register(" in inp
assert "clickCount == 0" in inp
assert "client.screen != null" in inp
assert "ClientPlayNetworking.canSend(FeatherFanPiercePacket.TYPE)" in inp
assert "ClientPlayNetworking.send(new FeatherFanPiercePacket())" in inp
assert "FeatherFanClientEvents.registerCallbacks();" in current("fabric/GuaniaoFabricClientEntrypoint.java")
for value in (packet, inp, network):
    assert "net.minecraftforge" not in value
print("PASS: charged left-click -> empty C2S packet -> server-validated piercing action")
