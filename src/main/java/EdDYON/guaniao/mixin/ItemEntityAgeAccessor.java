package EdDYON.guaniao.mixin;

import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Vanilla 26.2 drops item entities at age 6000 and no longer exposes Forge's
 * per-entity lifespan. A negative initial age preserves the original timer
 * without changing the authored feather/dropping lifetimes or item data.
 */
@Mixin(ItemEntity.class)
public interface ItemEntityAgeAccessor {
    @Accessor("age")
    void guaniao$setAge(int age);
}
