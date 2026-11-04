package EdDYON.guaniao.client.camera.frame;

import EdDYON.guaniao.content.bird.budgerigar.BudgerigarEntity;
import EdDYON.guaniao.content.bird.myna.MynaEntity;
import EdDYON.guaniao.content.bird.sparrow.SparrowEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Deterministic data and animation selection for the two non-world workshop birds.
 *
 * Minecraft 26.2 GUI rendering is deferred: this class deliberately owns no
 * Forge event subscribers, mutable renderer instances, baked GeoBones or
 * immediate GUI draw calls. A PiP renderer consumes this stable actor data.
 */
final class FrameWorkshopBird {
    private static final Species[] SPECIES={
            new Species("sparrow",1.30F,1.65F,.65F),
            new Species("long_tailed_tit",2.30F,2.60F,.85F),
            new Species("cockatiel",.95F,1.10F,.65F),
            new Species("budgerigar",.90F,1.15F,.60F),
            new Species("myna",1.20F,1.55F,.70F)
    };
    private static final long PROGRESS_SEED_SALT=0x6a09e667f3bcc909L;

    public enum Action { IDLE, LOOK_AROUND, FLY, WALK, EAT }

    private FrameWorkshopBird() { }

    /** Stable across reopening the same job, independently of render count and world RNG. */
    static String speciesForSeed(long seed) { return species(seed).id; }

    static String progressSpeciesForSeed(long seed) {
        return species(seed ^ PROGRESS_SEED_SALT).id;
    }

    /** A fresh default stack; each choice belongs to the corresponding bird's existing food tag. */
    static ItemStack progressFood(long seed) {
        return new ItemStack(switch (progressSpeciesForSeed(seed)) {
            case "long_tailed_tit" -> Items.SWEET_BERRIES;
            case "budgerigar" -> Items.MELON_SEEDS;
            case "myna" -> Items.APPLE;
            default -> Items.WHEAT_SEEDS;
        });
    }

    /** Standing height which fits the complete wing cycle and tail in both dimensions. */
    static float fittedStandingHeight(long seed, float size) {
        Species species=species(seed);
        float box=Float.isFinite(size) ? Mth.clamp(size,0,56) : 0;
        return box*.92F/Math.max(species.halfWidth*2,species.aboveOrigin+species.belowOrigin);
    }

    /**
     * Preserve the authored per-species preview controller selections. Long-tailed
     * tit intentionally uses SparrowEntity's adapter so its subclass maps the
     * shared action into its own native animation enum.
     */
    static void applyAction(Mob bird, Action action) {
        if (bird instanceof MynaEntity myna) {
            myna.setGuidePreviewAnimation(switch (action) {
                case IDLE -> MynaEntity.GuidePreviewAnimation.IDLE;
                case LOOK_AROUND -> MynaEntity.GuidePreviewAnimation.IDLE_2;
                case FLY -> MynaEntity.GuidePreviewAnimation.FLY;
                case WALK -> MynaEntity.GuidePreviewAnimation.WALK;
                case EAT -> MynaEntity.GuidePreviewAnimation.IDLE_2;
            });
        } else if (bird instanceof SparrowEntity sparrow) {
            sparrow.setGuidePreviewAnimation(switch (action) {
                case IDLE -> SparrowEntity.GuidePreviewAnimation.IDLE;
                case LOOK_AROUND -> SparrowEntity.GuidePreviewAnimation.LOOK_AROUND;
                case FLY -> SparrowEntity.GuidePreviewAnimation.FLY;
                case WALK -> SparrowEntity.GuidePreviewAnimation.WALK;
                case EAT -> SparrowEntity.GuidePreviewAnimation.PECK;
            });
        } else if (bird instanceof BudgerigarEntity budgerigar) {
            budgerigar.setGuidePreviewAnimation(switch (action) {
                case IDLE -> BudgerigarEntity.GuidePreviewAnimation.IDLE;
                case LOOK_AROUND -> BudgerigarEntity.GuidePreviewAnimation.CURIOUS;
                case FLY -> BudgerigarEntity.GuidePreviewAnimation.FLY;
                case WALK -> BudgerigarEntity.GuidePreviewAnimation.WALK;
                case EAT -> BudgerigarEntity.GuidePreviewAnimation.EAT;
            });
        }
    }

    private static Species species(long seed) {
        long mixed=(seed^(seed>>>33))*0xff51afd7ed558ccdL;
        mixed=(mixed^(mixed>>>33))*0xc4ceb9fe1a85ec53L;
        return SPECIES[Math.floorMod(mixed^(mixed>>>33),SPECIES.length)];
    }

    private record Species(String id,float halfWidth,float aboveOrigin,float belowOrigin) { }
}
