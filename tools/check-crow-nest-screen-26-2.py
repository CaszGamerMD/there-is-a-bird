#!/usr/bin/env python3
"""Source parity for the custom 26.2 Crow Nest masked-loot GUI."""
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
FILE = "src/main/java/EdDYON/guaniao/client/nest/CrowNestScreen.java"
original = subprocess.check_output(["git", "show", "origin/main:" + FILE],
                                   cwd=ROOT, text=True).replace("\r\n", "\n")
current = (ROOT / FILE).read_text("utf-8").replace("\r\n", "\n")

for key in ("SCREEN_WIDTH = 176", "SCREEN_HEIGHT = 222",
            "LOOT_GRID_X = 8", "LOOT_GRID_Y = 18", "LOOT_CELL = 18",
            "this.inventoryLabelY = 128",
            "this.menu.isSlotRummaged(placement.storageSlot())",
            "this.menu.getCurrentSearchSlot()",
            "this.menu.getSearchTotalTicks()",
            "this.menu.getSearchTicks()",
            "this.menu.getTreasureStack(placement.storageSlot())",
            "CrowNestMenu.TAKE_TREASURE_BUTTON_BASE + placement.storageSlot()",
            "this.getLootAt(", "this.drawMergedSlot(", "this.renderCoveredLoot(",
            "0xFF5D5D5D", "0xFF2B2B2B", "0xFFBFAF6D",
            "0xFF373737", "0xFFFFFFFF", "0xFF8B8B8B", "0xFF555555"):
    assert key in original and key in current, key
for method in ("extractBackground(", "extractContents(", "extractLabels(", "extractTooltip(",
               "graphics.blit(RenderPipelines.GUI_TEXTURED, GENERIC_54,",
               "graphics.nextStratum();", "graphics.item(stack, centerX - 8, centerY - 8)",
               "graphics.setTooltipForNextFrame(this.font, stack, mouseX, mouseY)",
               "mouseClicked(MouseButtonEvent event, boolean doubleClick)",
               "super.mouseClicked(event, doubleClick)",
               "handleInventoryButtonClick(", "super.extractTooltip(graphics, mouseX, mouseY)"):
    assert method in current, method
for old_api in ("GuiGraphics;", "new ResourceLocation(", "renderBg(",
                "renderLabels(", "graphics.renderItem(", "graphics.renderTooltip(",
                "graphics.pose().pushPose()", "mouseClicked(double mouseX",
                "this.imageWidth = ", "this.imageHeight = "):
    assert old_api not in current, old_api
assert 'Identifier.withDefaultNamespace("textures/gui/container/generic_54.png")' in current
assert "super(menu, inventory, title, SCREEN_WIDTH, SCREEN_HEIGHT);" in current
print("CROW NEST 26.2 GUI PARITY OK: 176x222 masked treasure grid,"
      " progress, item tooltip, original server button IDs")
