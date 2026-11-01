package EdDYON.guaniao.mixin;

import EdDYON.guaniao.content.bird.hummingbird.FlowerHabitatIndex;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Invoked after an actual state change, including non-player edits and crop growth. */
@Mixin(Level.class)
public abstract class HummingbirdHabitatBlockUpdateMixin {
    @Inject(method = "setBlocksDirty", at = @At("HEAD"))
    private void guaniao$invalidateHabitat(BlockPos pos, BlockState before, BlockState after, CallbackInfo ci) {
        if ((Object) this instanceof ServerLevel level) FlowerHabitatIndex.invalidate(level, pos);
    }
}
