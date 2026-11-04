package EdDYON.guaniao.fabric;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;

/**
 * Compile-only probe for the native 26.2 jukebox's non-playing restoration path.
 * The obsolete Forge chunk NBT patch must not restart or reset any saved song.
 */
public final class GuaniaoJukeboxProof {
    private GuaniaoJukeboxProof() { }

    public static void ensureQuietRestorationAvailable(JukeboxBlockEntity jukebox, ItemStack disc) {
        jukebox.setSongItemWithoutPlaying(disc);
    }
}
