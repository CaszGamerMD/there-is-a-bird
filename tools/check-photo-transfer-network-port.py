#!/usr/bin/env python3
"""Compare all seven original photo packet wire codecs and bound/dispatch policy."""
from pathlib import Path
import re
import subprocess

ROOT = Path(__file__).resolve().parents[1]
BASE = "src/main/java/EdDYON/guaniao/"
CLASSES = (
    "BeginPhotoUploadPacket",
    "PhotoUploadChunkPacket",
    "FinishPhotoUploadPacket",
    "PhotoRequestPacket",
    "PhotoCaptureResultPacket",
    "PhotoDownloadStartPacket",
    "PhotoDownloadChunkPacket",
)
SERVERBOUND = CLASSES[:4]
CLIENTBOUND = CLASSES[4:]

def current(path):
    return (ROOT / (BASE + path)).read_text("utf-8").replace("\r\n", "\n")
def original(path):
    return subprocess.check_output(["git", "show", "origin/main:" + BASE + path],
                                   cwd=ROOT, text=True).replace("\r\n", "\n")

def method_body(source, signature):
    start = source.find(signature)
    assert start >= 0, signature
    brace = source.index("{", start)
    depth = 0
    for i in range(brace, len(source)):
        if source[i] == "{": depth += 1
        elif source[i] == "}":
            depth -= 1
            if depth == 0:
                return source[brace + 1:i]
    raise AssertionError("Unclosed Java method "+signature)

for name in CLASSES:
    path = "network/" + name + ".java"
    old, new = original(path), current(path)
    for action, signature in (
        ("encode", "public static void encode("+name+" packet, FriendlyByteBuf buffer)"),
        ("decode", "public static "+name+" decode(FriendlyByteBuf buffer)"),
    ):
        assert method_body(old, signature) == method_body(new, signature), name + "." + action
    assert "implements CustomPacketPayload" in new
    assert "StreamCodec.of(" in new
    assert "RegistryFriendlyByteBuf" in new
    assert "Supplier<NetworkEvent.Context>" not in new
    assert "net.minecraftforge" not in new
    if name in SERVERBOUND:
        assert "public void handle(ServerPlayer player)" in new
        for token in ("PhotoUploadManager.begin(", "PhotoUploadManager.acceptChunk(",
                      "PhotoUploadManager.finish(", "PhotoUploadManager.requestDownload("):
            if token in old: assert token in new, token
    else:
        assert "PhotoClientRepository" not in new, name + " must be server-classloader safe"

upload = current("client/camera/PhotoClientRepository.java")
for name in SERVERBOUND:
    assert "ClientPlayNetworking.send(new " + name + "(" in upload, name
assert "GuaniaoNetwork.sendToServer(" not in upload

manager = current("network/PhotoUploadManager.java")
assert manager.count("ServerPlayNetworking.send(") == 6
assert "GuaniaoNetwork.sendToPlayer(" not in manager

registrar = current("fabric/GuaniaoFabricNetworking.java")
client = current("fabric/GuaniaoFabricClientEntrypoint.java")
for name in SERVERBOUND:
    assert "PayloadTypeRegistry.serverboundPlay().register(" in registrar
    assert name + ".TYPE" in registrar and name + ".CODEC" in registrar
    assert name + ".TYPE, (packet, context) -> packet.handle(context.player())" in registrar
for name in CLIENTBOUND:
    assert "PayloadTypeRegistry.clientboundPlay().register(" in registrar
    assert name + ".TYPE, " + name + ".CODEC" in registrar
    assert "ClientPlayNetworking.registerGlobalReceiver(" + name + ".TYPE" in client

print("PHOTO TRANSFER PARITY OK: seven exact wire codecs, bounded chunks, server handlers and client receivers")
