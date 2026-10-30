#!/usr/bin/env python3
"""Port contract for the original camera END tick and logout lifecycle."""
from pathlib import Path
import subprocess
ROOT=Path(__file__).resolve().parent.parent
SRC="src/main/java/EdDYON/guaniao/client/camera/"
old=subprocess.check_output(["git","show","origin/main:"+SRC+"CameraClientEvents.java"],
                            cwd=ROOT,text=True).replace("\r\n","\n")
new=(ROOT/"src/main/java/EdDYON/guaniao/fabric/GuaniaoFabricClientEntrypoint.java").read_text("utf-8")
capture=(ROOT/(SRC+"CameraClientCapture.java")).read_text("utf-8")
keys=(ROOT/(SRC+"CameraKeyMappings.java")).read_text("utf-8")
workflow=(ROOT/".github/workflows/fabric-26.2-port.yml").read_text("utf-8")
for name in ("CameraClientCapture.tickViewfinder();",
             "PhotographTextureCache.tickHeldPhotographs();",
             "CameraClientCapture.disconnect();",
             "PhotographTextureCache.clear();",
             "BirdConfigManager.loadOrCreateDefaults();"):
    assert name in old and name in new, "Lost original event: "+name
assert "ClientTickEvents.END_CLIENT_TICK.register(" in new
assert "ClientPlayConnectionEvents.DISCONNECT.register(" in new
assert new.index("CameraClientCapture.tickViewfinder();") < new.index("CameraKeyMappings.tick();")
assert new.index("CameraClientCapture.disconnect();") < new.index("PhotographTextureCache.clear();")
assert "CameraKeyMappings.tick();" in new
assert "migrateLegacyFocusKey();" in keys
for token in ("if (!viewfinderOpen)", "closeViewfinder();",
              "if (currentState.focusMode() == CameraFocusMode.AF_C)",
              "tickContinuousFocus(minecraft);",
              "restoreAfterCleanCapture();"):
    assert token in capture, token
assert "check-camera-fabric-lifecycle-26-2.py" in workflow
print("PASS: Fabric camera tick and disconnect restore original autofocus, state and cleanup")
