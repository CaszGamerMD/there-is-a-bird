package EdDYON.guaniao.registry;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class GuaniaoBlockTags {
    public static final TagKey<Block> BIRD_PERCHES = BlockTags.create(Identifier.fromNamespaceAndPath("guaniao", "bird_perches"));

    private GuaniaoBlockTags() {
    }
}
