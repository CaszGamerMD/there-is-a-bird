#!/usr/bin/env python3
"""Final source-level 26.2 network migration and original protocol parity gate.

Requires a full git checkout containing origin/main (CI uses fetch-depth: 0).
This is a source check only; it does not replace full compilation or client/server tests.
"""
from pathlib import Path
import re
import subprocess

root = Path(__file__).resolve().parents[1]
prefix = "src/main/java/EdDYON/guaniao/"

def old(path):
    return subprocess.check_output(["git", "show", "origin/main:" + prefix + path],
                                   cwd=root, text=True).replace("\r\n", "\n")

def new(path):
    return (root / (prefix + path)).read_text("utf-8").replace("\r\n", "\n")

def body(src, signature):
    start = src.index(signature)
    opened = src.index("{", start)
    depth = 0
    for i in range(opened, len(src)):
        if src[i] == "{":
            depth += 1
        elif src[i] == "}":
            depth -= 1
            if depth == 0:
                return src[opened+1:i]
    raise AssertionError("Unclosed method: " + signature)

source = old("network/GuaniaoNetwork.java")
reg = new("fabric/GuaniaoFabricNetworking.java")
original_classes = re.findall(r"messageBuilder\((\w+)\.class", source)
ported_classes = re.findall(
    r"PayloadTypeRegistry\.(?:serverbound|clientbound)Play\(\)\.register\(\s*(\w+)\.TYPE", reg)
assert len(original_classes) == len(set(original_classes)) == 15
assert len(ported_classes) == len(set(ported_classes)) == 15
assert set(original_classes) == set(ported_classes), (
    sorted(set(original_classes) - set(ported_classes)),
    sorted(set(ported_classes) - set(original_classes)))

server = {"BeginPhotoUploadPacket", "PhotoUploadChunkPacket",
          "FinishPhotoUploadPacket", "PhotoRequestPacket",
          "SaveBirdConfigPacket", "FeatherFanPiercePacket",
          "SetCameraFilterPacket", "SetCameraSettingsPacket",
          "CraftFramePacket"}
clientbound = set(original_classes) - server
for name in server:
    assert re.search(
        r"PayloadTypeRegistry\.serverboundPlay\(\)\.register\(\s*" + name + r"\.TYPE", reg), name
    assert name + ".TYPE, (packet, context) -> packet.handle(context.player())" in reg, name
for name in clientbound:
    assert re.search(
        r"PayloadTypeRegistry\.clientboundPlay\(\)\.register\(\s*" + name + r"\.TYPE", reg), name
    assert "ClientPlayNetworking.registerGlobalReceiver("+name+".TYPE" in new(
        "fabric/GuaniaoFabricClientEntrypoint.java"), name

for name in ("SetCameraFilterPacket", "SetCameraSettingsPacket", "CraftFramePacket",
             "OpenBirdConfigPacket", "SaveBirdConfigPacket"):
    before = old("network/"+name+".java")
    after = new("network/"+name+".java")
    for signature in (
        "public static void encode("+name+" packet, FriendlyByteBuf buffer)",
        "public static "+name+" decode(FriendlyByteBuf buffer)"):
        assert body(before, signature) == body(after, signature), name + " " + signature
    assert "implements CustomPacketPayload" in after
    assert "StreamCodec.of(" in after
    assert "net.minecraftforge" not in after

snapshot = new("network/FrameWorkSnapshotPacket.java")
assert "ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, packet.preview)" in snapshot
assert "ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer)" in snapshot
assert "RegistryFriendlyByteBuf" in snapshot
assert "net.minecraftforge" not in snapshot
save = new("network/SaveBirdConfigPacket.java")
assert "Permissions.COMMANDS_MODERATOR" in save
assert "BirdConfigManager.replaceAndSave(data, player.server)" in save
assert "BirdPopulationTracker.rebuild(player.server)" in save
assert "BirdDroppingEvents.refreshLoadedBirdCooldowns(player.server)" in save

commands = new("command/BirdConfigCommands.java")
assert "CommandRegistrationCallback.EVENT.register" in commands
for literal in ('"guaniaoconfig"', '"birdconfig"', '"seagullRaid"', '"birdNotes"'):
    assert literal in commands
assert "Permissions.COMMANDS_MODERATOR" in commands
assert "ServerPlayNetworking.send(player, new OpenBirdConfigPacket(" in commands
assert "BirdConfigCommands.registerCallbacks();" in new("fabric/GuaniaoFabricEntrypoint.java")
assert "net.minecraftforge" not in commands

photo_commands = new("command/PhotoAdminCommands.java")
assert "CommandRegistrationCallback.EVENT.register" in photo_commands
for literal in ('"photo"', '"stats"', '"list"', '"delete"', '"restore"',
                '"verify"', '"prune"', '"dry_run"', '"confirm"'):
    assert literal in photo_commands, literal
assert "PhotoAdminCommands.registerCallbacks();" in new("fabric/GuaniaoFabricEntrypoint.java")
assert "net.minecraftforge" not in photo_commands

legacy = new("network/GuaniaoNetwork.java")
assert "SimpleChannel" not in legacy and "net.minecraftforge" not in legacy
assert "ServerPlayNetworking.send(player, packet)" in legacy

for path in ("client/camera/CameraClientCapture.java",
             "client/camera/frame/FrameWorkbenchScreen.java",
             "client/config/BirdConfigScreen.java",
             "content/camera/frame/FrameWorkbenchMenu.java"):
    assert "GuaniaoNetwork.send" not in new(path), path

for name in original_classes:
    packet = new("network/"+name+".java")
    assert "implements CustomPacketPayload" in packet, name
    assert "net.minecraftforge" not in packet, name

for path in (root / "src/main/java/EdDYON/guaniao").rglob("*.java"):
    if path.name == "GuaniaoNetwork.java":
        continue
    source = path.read_text("utf-8")
    assert "GuaniaoNetwork.sendToServer(" not in source, str(path)
    assert "GuaniaoNetwork.CHANNEL" not in source, str(path)

print("PASS: all 15 authored play packet types moved to Fabric with identical "
      "camera/config/frame codecs, retained authorization and all command IDs")
