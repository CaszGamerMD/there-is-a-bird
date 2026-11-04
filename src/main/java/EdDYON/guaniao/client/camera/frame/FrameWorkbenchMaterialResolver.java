package EdDYON.guaniao.client.camera.frame;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Minecraft 26.2 material resolver for the authored Frame Workbench.
 *
 * The original Forge model particle-icon API was replaced by baked
 * BlockStateModelSet.getParticleMaterial(state).sprite(). The selected
 * material remains the real user-chosen block texture (including modded
 * blocks and resource packs), with vanilla inventory tint and transparency.
 * No unsafe offscreen GL capture or Forge APIs are needed for this stage.
 */
final class FrameWorkbenchMaterialResolver {
    private static final Identifier PAPER=Identifier.withDefaultNamespace("paper");
    private static final Identifier AIR=Identifier.withDefaultNamespace("air");

    private FrameWorkbenchMaterialResolver() { }

    static FrameWorkbenchPreview3DState.Surface surface(Identifier id) {
        Minecraft client=Minecraft.getInstance();
        boolean paper=id.equals(PAPER) || id.equals(AIR);
        Item item=BuiltInRegistries.ITEM.getValue(id);
        BlockState state=paper ? BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace("white_wool")).defaultBlockState()
                : item instanceof BlockItem block ? block.getBlock().defaultBlockState()
                : Blocks.OAK_PLANKS.defaultBlockState();
        TextureAtlasSprite sprite=client.getModelManager().getBlockStateModelSet()
                .getParticleMaterial(state).sprite();
        // If an optional mod disappeared, use the original oak fallback.
        if(sprite==null) {
            sprite=client.getModelManager().getBlockStateModelSet()
                    .getParticleMaterial(Blocks.OAK_PLANKS.defaultBlockState()).sprite();
        }
        int tint=0xFFFFFF;
        if(!paper) {
            BlockTintSource source=client.getBlockColors().getTintSource(state,0);
            if(source!=null) {
                int color=source.color(state);
                if(color!=-1) tint=color & 0xFFFFFF;
            }
        }
        float r=((tint>>16)&255)/255.0F;
        float g=((tint>>8)&255)/255.0F*(paper ? 0.96F : 1.0F);
        float blue=(tint&255)/255.0F*(paper ? 0.86F : 1.0F);
        return new FrameWorkbenchPreview3DState.Surface(
                sprite,r,g,blue,sprite.transparency().hasTranslucent());
    }

    static boolean isGlass(Identifier id) {
        Item item=BuiltInRegistries.ITEM.getValue(id);
        if(!(item instanceof BlockItem block))return false;
        if(block.getBlock() instanceof StainedGlassPaneBlock
                || id.getPath().contains("glass") || id.getPath().endsWith("_pane")
                || block.getBlock()==Blocks.GLASS_PANE)return true;
        // Preserve the original special case for beacons and third-party
        // blocks whose actual particle material is a vanilla glass surface.
        Identifier texture=surface(id).sprite().contents().name();
        if(!texture.getNamespace().equals("minecraft"))return false;
        String path=texture.getPath();
        if(path.equals("block/glass") || path.equals("block/tinted_glass"))return true;
        for(DyeColor color:DyeColor.values()) {
            if(path.equals("block/"+color.getName()+"_stained_glass"))return true;
        }
        return false;
    }
}
