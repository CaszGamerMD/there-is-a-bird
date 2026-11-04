package EdDYON.guaniao.client.nest;

import EdDYON.guaniao.content.nest.CrowNestLootLayout;
import EdDYON.guaniao.content.nest.CrowNestMenu;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Vanilla chest layout with authored masked multi-cell loot and automatic
 * rummaging. Minecraft 26.2 extracts display state instead of direct rendering.
 */
public final class CrowNestScreen extends AbstractContainerScreen<CrowNestMenu> {
    private static final Identifier GENERIC_54 =
            Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final int SCREEN_WIDTH = 176;
    private static final int SCREEN_HEIGHT = 222;
    private static final int LOOT_GRID_X = 8;
    private static final int LOOT_GRID_Y = 18;
    private static final int LOOT_CELL = 18;

    public CrowNestScreen(CrowNestMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, SCREEN_WIDTH, SCREEN_HEIGHT);
        this.inventoryLabelY = 128;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, GENERIC_54,
                this.leftPos, this.topPos, 0.0F, 0.0F,
                this.imageWidth, this.imageHeight, 256, 256);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractContents(graphics, mouseX, mouseY, partialTick);
        // The custom grid belongs above the vanilla slots and below tooltips.
        graphics.nextStratum();
        this.renderLootGrid(graphics, this.menu.getLootLayout());
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(this.font, this.title, 8, 6, 0xFF404040, false);
        graphics.text(this.font, this.playerInventoryTitle, 8, this.inventoryLabelY, 0xFF404040, false);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        this.renderLootTooltip(graphics, this.menu.getLootLayout(), mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            CrowNestLootLayout.Placement placement =
                    this.getLootAt(event.x(), event.y(), this.menu.getLootLayout());
            if (placement != null && this.menu.isSlotRummaged(placement.storageSlot())) {
                this.minecraft.gameMode.handleInventoryButtonClick(
                        this.menu.containerId,
                        CrowNestMenu.TAKE_TREASURE_BUTTON_BASE + placement.storageSlot());
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private void renderLootGrid(GuiGraphicsExtractor graphics, List<CrowNestLootLayout.Placement> placements) {
        for (CrowNestLootLayout.Placement placement : placements) {
            int x = this.leftPos + LOOT_GRID_X + placement.column() * LOOT_CELL;
            int y = this.topPos + LOOT_GRID_Y + placement.row() * LOOT_CELL;
            int width = placement.width() * LOOT_CELL;
            int height = placement.height() * LOOT_CELL;
            if (this.menu.isSlotRummaged(placement.storageSlot())) {
                this.renderDiscoveredLoot(graphics, placement, x, y, width, height);
            } else {
                this.renderCoveredLoot(graphics, placement, x, y, width, height);
            }
        }
    }

    private void renderDiscoveredLoot(GuiGraphicsExtractor graphics,
                                      CrowNestLootLayout.Placement placement,
                                      int x, int y, int width, int height) {
        if (placement.width() > 1 || placement.height() > 1) {
            this.drawMergedSlot(graphics, x, y, width, height);
        }
        ItemStack stack = this.menu.getTreasureStack(placement.storageSlot());
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        graphics.item(stack, centerX - 8, centerY - 8);
    }

    private void renderCoveredLoot(GuiGraphicsExtractor graphics,
                                   CrowNestLootLayout.Placement placement,
                                   int x, int y, int width, int height) {
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFF5D5D5D);
        graphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, 0xFF2B2B2B);
        if (placement.storageSlot() == this.menu.getCurrentSearchSlot()) {
            int total = Math.max(1, this.menu.getSearchTotalTicks());
            int filled = Mth.floor((width - 4) * Mth.clamp(
                    1.0F - this.menu.getSearchTicks() / (float)total, 0.0F, 1.0F));
            graphics.fill(x + 2, y + height - 4, x + 2 + filled, y + height - 2, 0xFFBFAF6D);
        }
    }

    private void renderLootTooltip(GuiGraphicsExtractor graphics,
                                   List<CrowNestLootLayout.Placement> placements,
                                   int mouseX, int mouseY) {
        CrowNestLootLayout.Placement hovered = this.getLootAt(mouseX, mouseY, placements);
        if (hovered != null && this.menu.isSlotRummaged(hovered.storageSlot())) {
            ItemStack stack = this.menu.getTreasureStack(hovered.storageSlot());
            if (!stack.isEmpty()) {
                graphics.setTooltipForNextFrame(this.font, stack, mouseX, mouseY);
            }
        }
    }

    private CrowNestLootLayout.Placement getLootAt(double mouseX, double mouseY,
                                                  List<CrowNestLootLayout.Placement> placements) {
        for (CrowNestLootLayout.Placement placement : placements) {
            int x = this.leftPos + LOOT_GRID_X + placement.column() * LOOT_CELL;
            int y = this.topPos + LOOT_GRID_Y + placement.row() * LOOT_CELL;
            if (mouseX >= x && mouseX < x + placement.width() * LOOT_CELL
                    && mouseY >= y && mouseY < y + placement.height() * LOOT_CELL) {
                return placement;
            }
        }
        return null;
    }

    private void drawMergedSlot(GuiGraphicsExtractor graphics,
                                int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, 0xFF373737);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFFFFFFFF);
        graphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, 0xFF8B8B8B);
        graphics.fill(x + 1, y + height - 2, x + width - 1, y + height - 1, 0xFF555555);
        graphics.fill(x + width - 2, y + 1, x + width - 1, y + height - 1, 0xFF555555);
    }
}
