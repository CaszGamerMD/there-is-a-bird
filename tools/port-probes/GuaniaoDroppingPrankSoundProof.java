package EdDYON.guaniao.proof;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.wolf.Wolf;

/** Compile the 26.2 sound contract for three legacy data-driven animal sounds. */
public final class GuaniaoDroppingPrankSoundProof {
    private static final SoundEvent WOLF_WHINE = SoundEvent.createVariableRangeEvent(Identifier.withDefaultNamespace("entity.wolf.whine"));
    private static final SoundEvent WOLF_AMBIENT = SoundEvent.createVariableRangeEvent(Identifier.withDefaultNamespace("entity.wolf.ambient"));
    private static final SoundEvent CAT_HISS = SoundEvent.createVariableRangeEvent(Identifier.withDefaultNamespace("entity.cat.hiss"));

    private GuaniaoDroppingPrankSoundProof() {
    }

    private static void playOriginalPrankSounds(ServerLevel level, Wolf wolf, Cat cat) {
        wolf.playSound(WOLF_WHINE, 0.8F, 0.85F + level.getRandom().nextFloat() * 0.25F);
        cat.playSound(CAT_HISS, 0.9F, 0.9F + level.getRandom().nextFloat() * 0.25F);
        level.playSound(null, wolf.getX(), wolf.getY(), wolf.getZ(), WOLF_AMBIENT,
                SoundSource.AMBIENT, 0.75F, 1.25F + level.getRandom().nextFloat() * 0.3F);
    }
}
