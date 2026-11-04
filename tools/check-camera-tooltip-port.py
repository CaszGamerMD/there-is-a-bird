#!/usr/bin/env python3
"""Camera help tooltip source parity and concrete Fabric client registration checks."""
import json
import re
import subprocess
from pathlib import Path

root = Path(__file__).resolve().parents[1]
tooltip_path = "src/main/java/EdDYON/guaniao/client/camera/CameraTooltipEvents.java"
legacy = subprocess.check_output(["git", "show", "origin/main:" + tooltip_path], cwd=root, text=True)
ported = (root / tooltip_path).read_text("utf-8")
def keys(text):
    return re.findall(r'Component\.translatable\("([^"]+)"', text)
if keys(legacy) != keys(ported):
    raise SystemExit("Camera tooltip text or localization keys were changed")
if "CameraHelpScreen.controlLines()" not in ported:
    raise SystemExit("Camera controls are missing from the expanded Shift tooltip")
if 'stack.is(GuaniaoItems.NIKON_D750.get())' not in ported:
    raise SystemExit("Camera tooltip must not appear on other items")
if "if (hasShiftDown)" not in ported:
    raise SystemExit("Expanded camera tooltip must remain a hold-Shift feature")
if "ItemTooltipCallback.EVENT.register" not in ported:
    raise SystemExit("Fabric ItemTooltipCallback was not registered")
if "Minecraft.getInstance().hasShiftDown()" not in ported:
    raise SystemExit("Tooltip must use the current Minecraft 26.2 Shift query")
if "net.minecraftforge" in ported:
    raise SystemExit("Legacy Forge imports still present in camera tooltip")
manifest = json.loads((root / "src/main/resources/fabric.mod.json").read_text("utf-8"))
client = "EdDYON.guaniao.fabric.GuaniaoFabricClientEntrypoint"
if client not in manifest.get("entrypoints", {}).get("client", []):
    raise SystemExit("Missing Fabric client initializer in fabric.mod.json")
client_source = (root / "src/main/java/EdDYON/guaniao/fabric/GuaniaoFabricClientEntrypoint.java").read_text("utf-8")
if "CameraTooltipEvents.registerCallbacks();" not in client_source:
    raise SystemExit("Client initializer does not invoke camera tooltip registration")
print("FABRIC CAMERA TOOLTIP PARITY OK: original localized control hints, Shift behavior, and active client callback")
