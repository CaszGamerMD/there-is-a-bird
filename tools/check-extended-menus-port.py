#!/usr/bin/env python3
"""Guard original Crow Nest / Frame Workbench extended-menu wire and gameplay wiring."""
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
BASE = "src/main/java/EdDYON/guaniao/"

def original(path):
    return subprocess.check_output(["git", "show", "origin/main:" + BASE + path],
                                   cwd=ROOT, text=True).replace("\r\n", "\n")

def current(path):
    return (ROOT / (BASE + path)).read_text(encoding="utf-8").replace("\r\n", "\n")

registry = current("registry/GuaniaoMenuTypes.java")
legacy_registry = original("registry/GuaniaoMenuTypes.java")
for name in ("crow_nest", "frame_workbench"):
    assert f'MENU_TYPES.register("{name}"' in registry
    assert f'MENU_TYPES.register("{name}"' in legacy_registry
for marker in ("FabricRegistryQueue<MenuType<?>> MENU_TYPES",
               "new ExtendedMenuType<>(", "(RegistryFriendlyByteBuf buffer) -> buffer.readBlockPos()",
               "buffer.writeBlockPos(pos)", "BLOCK_POSITION"):
    assert marker in registry, marker
assert registry.count("new ExtendedMenuType<>(") == 2
assert "IForgeMenuType" not in registry and "net.minecraftforge" not in registry

initialization = current("fabric/GuaniaoFabricEntrypoint.java")
client = current("fabric/GuaniaoFabricClientEntrypoint.java")
assert "GuaniaoMenuTypes.MENU_TYPES.registerAll();" in initialization
for marker in ("MenuScreens.register(GuaniaoMenuTypes.CROW_NEST.get(), CrowNestScreen::new);",
               "MenuScreens.register(GuaniaoMenuTypes.FRAME_WORKBENCH.get(), FrameWorkbenchScreen::new);"):
    assert marker in client, marker

for block, menu, title in (
    ("content/nest/CrowNestBlock.java", "content/nest/CrowNestMenu.java", "gui.guaniao.crow_nest.title"),
    ("content/camera/frame/FrameWorkbenchBlock.java",
     "content/camera/frame/FrameWorkbenchMenu.java", "gui.guaniao.frame_workbench.title"),
):
    before_block = original(block)
    now_block = current(block)
    before_menu = original(menu)
    now_menu = current(menu)
    assert "NetworkHooks.openScreen" in before_block
    assert "NetworkHooks.openScreen" not in now_block
    assert "net.minecraftforge.network.NetworkHooks" not in now_block
    assert "serverPlayer.openMenu(" in now_block
    assert "ExtendedMenuProvider<BlockPos>()" in now_block
    assert "getScreenOpeningData(ServerPlayer player)" in now_block
    assert "return pos.immutable();" in now_block
    assert title in before_block and title in now_block
    assert "public " + ("CrowNestMenu" if "CrowNest" in menu else "FrameWorkbenchMenu") +            "(int containerId, Inventory inventory, BlockPos pos)" in now_menu
    assert "buffer.readBlockPos()" in before_menu and "buffer.readBlockPos()" in now_menu
    assert "FriendlyByteBuf" in before_menu and "FriendlyByteBuf" in now_menu

# The original server-side constructors and their authoritative work state stay intact.
crow = current("content/nest/CrowNestMenu.java")
assert "this.nest.beginAutomaticSearch();" in original("content/nest/CrowNestMenu.java")
assert "this.nest.beginAutomaticSearch();" in crow
assert "new CrowNestMenu(containerId, inventory, nest)" in current("content/nest/CrowNestBlock.java")
frame = current("content/camera/frame/FrameWorkbenchMenu.java")
old_frame = original("content/camera/frame/FrameWorkbenchMenu.java")
for marker in ("this.work = this.bench == null ? null : this.bench.open(owner.getUUID(), this)",
               "this.addDataSlot(craftResponse);", "sendWorkSnapshot();",
               "work.pending = output;", "work.remaining = CRAFT_DURATION_TICKS;"):
    assert marker in old_frame and marker in frame, marker
assert "new FrameWorkbenchMenu(containerId, inventory, bench)" in current(
    "content/camera/frame/FrameWorkbenchBlock.java")

print("FABRIC EXTENDED MENUS SOURCE PARITY OK: two original menu IDs,"
      " server-authored block positions, nest search and photo job continuity")
