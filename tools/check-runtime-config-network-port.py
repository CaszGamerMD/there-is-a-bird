#!/usr/bin/env python3
"""Ensure three runtime booleans retain their exact order and join/change delivery."""
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
BASE = "src/main/java/EdDYON/guaniao/"
def read(name): return (ROOT / (BASE+name)).read_text("utf-8")
def old(name): return subprocess.check_output(["git", "show", "origin/main:"+BASE+name],
                                                cwd=ROOT, text=True)
previous = old("network/BirdRuntimeConfigPacket.java")
packet = read("network/BirdRuntimeConfigPacket.java")
names = ["birdsPassThroughLeaves", "aprilFoolsMode", "skyBirdEcologyEnabled"]
assert [previous.index("buffer.writeBoolean(packet."+n+")") for n in names] == sorted(
    previous.index("buffer.writeBoolean(packet."+n+")") for n in names)
assert [packet.index("ByteBufCodecs.BOOL, BirdRuntimeConfigPacket::"+n) for n in names] == sorted(
    packet.index("ByteBufCodecs.BOOL, BirdRuntimeConfigPacket::"+n) for n in names)
assert "BirdConfigManager.applyRemoteRuntime(" in packet
assert "implements CustomPacketPayload" in packet
assert "net.minecraftforge" not in packet
net = read("fabric/GuaniaoFabricNetworking.java")
assert "PayloadTypeRegistry.clientboundPlay().register(" in net
assert "BirdRuntimeConfigPacket.TYPE, BirdRuntimeConfigPacket.CODEC" in net
client = read("fabric/GuaniaoFabricClientEntrypoint.java")
assert "ClientPlayNetworking.registerGlobalReceiver(BirdRuntimeConfigPacket.TYPE" in client
assert "(packet, context) -> packet.applyOnClient()" in client
join = read("event/BirdRuntimeConfigEvents.java")
save = read("network/SaveBirdConfigPacket.java")
assert "ServerPlayConnectionEvents.JOIN.register" in join
assert "ServerPlayNetworking.send(player, new BirdRuntimeConfigPacket(" in join
assert "ServerPlayNetworking.canSend(player, BirdRuntimeConfigPacket.TYPE)" in join
assert "ServerPlayNetworking.send(online, new BirdRuntimeConfigPacket(" in save
assert "ServerPlayNetworking.canSend(online, BirdRuntimeConfigPacket.TYPE)" in save
for name in names:
    assert "BirdConfigManager."+name+"()" in join
    assert "BirdConfigManager."+name+"()" in save
print("PASS: native runtime-config packet stays three booleans, joins and broadcasts wired")
