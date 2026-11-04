#!/usr/bin/env python3
"""Keep authored camera key IDs, defaults, help/control behavior and 26.2 API."""
import json
import re
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CAM = "src/main/java/EdDYON/guaniao/client/camera/CameraKeyMappings.java"
OLD = subprocess.check_output(["git", "show", "origin/main:" + CAM],
                              cwd=ROOT, text=True).replace("\r\n", "\n")
NEW = (ROOT / CAM).read_text("utf-8")
CLIENT = (ROOT / "src/main/java/EdDYON/guaniao/fabric/GuaniaoFabricClientEntrypoint.java").read_text("utf-8")
WORKFLOW = (ROOT / ".github/workflows/fabric-26.2-port.yml").read_text("utf-8")
GRADLE = (ROOT / "build.gradle").read_text("utf-8")

for item, identifier, key in [
    ("OPEN_FILTER_LIBRARY", "key.guaniao.camera.cycle_filter", "GLFW.GLFW_KEY_V"),
    ("OPEN_CREATIVE_CONTROLS", "key.guaniao.camera.open_creative_controls", "GLFW.GLFW_KEY_C"),
    ("FOCUS", "key.guaniao.camera.focus", "GLFW.GLFW_KEY_TAB")
]:
    for text in (OLD, NEW):
        regex = rf"\b{item}\s*=\s*new KeyMapping\(\s*\"{identifier}\".*?{key}"
        assert re.search(regex, text, re.S), f"Authored default changed: {item}"
    assert f"KeyMappingHelper.registerKeyMapping({item});" in NEW

for phrase in ("KeyMapping.Category.register(", 'Identifier.fromNamespaceAndPath(GuaniaoMod.MOD_ID, "camera")',
               "InputConstants.Type.KEYSYM", "KeyMappingHelper.getBoundKeyOf(FOCUS)",
               "FOCUS.setKey(FOCUS.getDefaultKey())", "KeyMapping.resetMapping()",
               "minecraft.options.save()", "Stream<String> lines = Files.lines(",
               "CameraClientPreferences.isFocusKeyMigrationPending()",
               "CameraClientPreferences.markFocusKeyMigrated()",
               "CameraClientCapture.canUseViewfinderControls()",
               "CameraFilterPickerScreen.open()", "CameraCreativeControlsScreen.open()",
               "CameraClientCapture.focusAtCrosshair()"):
    assert phrase in NEW, phrase
assert "net.minecraftforge." not in NEW and "@SubscribeEvent" not in NEW
assert "CameraKeyMappings.register();" in CLIENT
assert "CameraKeyMappings.tick();" in CLIENT
assert "ClientTickEvents.END_CLIENT_TICK.register(" in CLIENT
for language, text in [("en_us", "Birdwatching"), ("zh_cn", "观鸟")]:
    data = json.loads((ROOT / f"src/main/resources/assets/guaniao/lang/{language}.json").read_text("utf-8"))
    assert data["key.category.guaniao.camera"] == data["key.categories.guaniao"] == text
    assert all("key.guaniao.camera." + k in data for k in ("cycle_filter", "open_creative_controls", "focus"))
assert "GuaniaoCameraKeyMappingProof.java" in GRADLE
assert "check-camera-key-mappings-26-2.py" in WORKFLOW
print("CAMERA KEYMAPPING 26.2 SOURCE PARITY OK: V/C/Tab, registered category, saved R migration")
