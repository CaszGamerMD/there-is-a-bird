package EdDYON.guaniao.registry;

import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class GuaniaoBlockTags {
    public static final TagKey<Block> BIRD_PERCHES = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("guaniao", "bird_perches"));

    private GuaniaoBlockTags() {
    }
}
