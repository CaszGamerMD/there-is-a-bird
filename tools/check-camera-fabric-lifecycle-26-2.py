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
assert (new.index("CameraClientCapture.disconnect();")
        < new.index("PhotoClientRepository.clear();")
        < new.index("PhotographTextureCache.clear();"))
assert "CameraKeyMappings.tick();" in new
assert "migrateLegacyFocusKey();" in keys
for token in ("if (!viewfinderOpen)", "closeViewfinder();",
              "if (currentState.focusMode() == CameraFocusMode.AF_C)",
              "tickContinuousFocus(minecraft);",
              "restoreAfterCleanCapture();"):
    assert token in capture, token
repository = (ROOT / (SRC + "PhotoClientRepository.java")).read_text("utf-8")
assert "public static void clear()" in repository
clear = repository.split("public static void clear()", 1)[1].split("private static void reject", 1)[0]
# No old JPEG, in-flight requests, image hashes or per-server capture IDs
# may remain when the player joins another world.
for token in ("IMAGES.clear();", "DOWNLOADS.clear();", "EXPECTED_HASHES.clear();",
              "RETRY_AFTER.clear();", "PENDING_UPLOADS.clear();", "DISPATCHED_UPLOADS.clear();",
              "CAPTURE_IMAGES.clear();", "RECENT_CAPTURES.clear();", "REQUEST_QUEUE.clear();",
              "FAILURE_COUNTS.clear();", "VALIDATING.clear();", "generation++;",
              "activeRequest = null;", "recentCaptureBytes = 0L;"):
    assert token in clear, "Missing session reset: " + token
assert "check-camera-fabric-lifecycle-26-2.py" in workflow
print("PASS: Fabric camera tick and disconnect restore original autofocus, state and cleanup")
