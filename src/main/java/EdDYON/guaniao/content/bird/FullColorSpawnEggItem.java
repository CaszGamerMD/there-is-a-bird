package EdDYON.guaniao.content.bird;

import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

/**
 * Spawn egg with a fully painted icon, as in the original Forge release.
 *
 * Minecraft 26.2 determines the spawned type through Item.Properties.spawnEgg()
 * instead of a ForgeSpawnEggItem constructor. The legacy color values remain
 * constructor inputs for source compatibility; the packaged model is already
 * colored and must not receive a second runtime tint.
 */
public final class FullColorSpawnEggItem extends SpawnEggItem {
    public FullColorSpawnEggItem(
            Supplier<? extends EntityType<? extends Mob>> entityType,
            int backgroundColor,
            int highlightColor,
            Item.Properties properties
    ) {
        super(Objects.requireNonNull(properties, "properties")
                .spawnEgg(Objects.requireNonNull(entityType.get(), "entityType")));
    }
}
