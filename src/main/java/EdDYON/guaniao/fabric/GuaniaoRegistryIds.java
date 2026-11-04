package EdDYON.guaniao.fabric;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/** Stable IDs from the original guaniao mod, now expressed as Minecraft 26.2 registry keys. */
public final class GuaniaoRegistryIds {
    private static final String NAMESPACE = "guaniao";

    private GuaniaoRegistryIds() { }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(NAMESPACE, path);
    }

    public static ResourceKey<Block> block(String path) {
        return ResourceKey.create(Registries.BLOCK, id(path));
    }

    public static ResourceKey<Item> item(String path) {
        return ResourceKey.create(Registries.ITEM, id(path));
    }

    public static ResourceKey<EntityType<?>> entity(String path) {
        return ResourceKey.create(Registries.ENTITY_TYPE, id(path));
    }
}
