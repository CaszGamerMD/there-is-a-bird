package EdDYON.guaniao.client.camera.frame;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;

/**
 * Minecraft 26.2 extracted rendering for the original little meadow beneath
 * the second, independent workshop-progress bird.
 *
 * The animated GeckoLib bird is 3D and is deliberately NOT replaced here.
 * Its render state must be queued separately through FrameWorkbenchPipBridge.
 * This helper ports only the original backdrop, preserving all sprite names,
 * exact plant sizes/positions and grass tint (including resource packs).
 */
final class FrameCraftingMeadow {
    private static final float FEET_Y = 264;
    private static final Identifier GRASS_SIDE = Identifier.withDefaultNamespace("block/grass_block_side");
    private static final Identifier GRASS_OVERLAY = Identifier.withDefaultNamespace("block/grass_block_side_overlay");
    private static final Identifier CORNFLOWER = Identifier.withDefaultNamespace("block/cornflower");
    private static final Identifier DANDELION = Identifier.withDefaultNamespace("block/dandelion");
    private static final Identifier GRASS = Identifier.withDefaultNamespace("block/grass");

    private FrameCraftingMeadow() { }

    static void render(GuiGraphicsExtractor graphics) {
        Minecraft client = Minecraft.getInstance();
        TextureAtlas atlas = client.getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
        var side = atlas.getSprite(GRASS_SIDE);
        var overlay = atlas.getSprite(GRASS_OVERLAY);

        // 26.2 replaced BlockColors.getColor with per-layer BlockTintSource.
        // Preserve the inventory grass shade, not biome/world color.
        var state = Blocks.GRASS_BLOCK.defaultBlockState();
        BlockTintSource tintSource = client.getBlockColors().getTintSource(state, 0);
        int tint = tintSource == null ? 0xFFFFFFFF : 0xFF000000 | (tintSource.color(state) & 0xFFFFFF);

        for (int x = 247; x < 324; x += 7) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, side, x, 264, 7, 7);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, overlay, x, 264, 7, 7, tint);
        }

        // Do not place a flower under the food at the right edge.
        plant(graphics, atlas, CORNFLOWER, 251, 7, 0xFFFFFFFF);
        plant(graphics, atlas, GRASS, 270, 5, tint);
        plant(graphics, atlas, DANDELION, 284, 6, 0xFFFFFFFF);
        plant(graphics, atlas, GRASS, 299, 5, tint);
    }

    private static void plant(GuiGraphicsExtractor graphics, TextureAtlas atlas, Identifier spriteId,
                              int centerX, int size, int tint) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, atlas.getSprite(spriteId),
                centerX - size / 2, (int) FEET_Y - size, size, size, tint);
    }
}
