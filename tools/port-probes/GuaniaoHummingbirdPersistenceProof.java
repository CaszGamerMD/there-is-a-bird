import EdDYON.guaniao.event.BirdTamePermissionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import java.util.UUID;

/** Real 26.2 type check for legacy-compatible hummingbird save data and pre-tame vetoes. */
public final class GuaniaoHummingbirdPersistenceProof {
    private GuaniaoHummingbirdPersistenceProof() { }

    static void save(ValueOutput output, UUID candidate, ItemStack held, BlockPos garden) {
        output.store("TamingCandidate", UUIDUtil.CODEC, candidate);
        output.store("GardenItem", ItemStack.CODEC, held);
        output.putLong("Garden", garden.asLong());
    }

    static void load(ValueInput input) {
        UUID candidate = input.read("TamingCandidate", UUIDUtil.CODEC).orElse(null);
        ItemStack held = input.read("GardenItem", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        BlockPos garden = input.getLong("Garden").map(BlockPos::of).orElse(null);
        boolean male = input.getBooleanOr("Male", false);
    }

    static void veto() {
        BirdTamePermissionEvents.BEFORE_TAME.register((bird, player) -> true);
    }
}
