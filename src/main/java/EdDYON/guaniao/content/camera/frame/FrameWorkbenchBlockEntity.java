package EdDYON.guaniao.content.camera.frame;

import EdDYON.guaniao.registry.GuaniaoBlockEntityTypes;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Each visitor owns a private, persistent job; no inventory is exposed to automation. */
public final class FrameWorkbenchBlockEntity extends BlockEntity {
    private final Map<UUID, Work> works = new HashMap<>();
    private boolean dismantled;
    private long lastTick = Long.MIN_VALUE;

    public FrameWorkbenchBlockEntity(BlockPos pos, BlockState state) {
        super(GuaniaoBlockEntityTypes.FRAME_WORKBENCH.get(), pos, state);
    }

    Work open(UUID owner, FrameWorkbenchMenu menu) {
        if (dismantled || isRemoved()) return null;
        Work work = works.computeIfAbsent(owner, ignored -> new Work(this));
        work.menu = menu;
        return work;
    }

    boolean owns(UUID owner, Work work, FrameWorkbenchMenu menu) {
        return !dismantled && !isRemoved() && work != null
                && works.get(owner) == work && work.menu == menu;
    }

    void release(UUID owner, Work work, FrameWorkbenchMenu menu) {
        if (!owns(owner, work, menu)) return;
        work.menu = null;
        if (!work.busy() && work.input.isEmpty() && work.result.isEmpty()) works.remove(owner);
        setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FrameWorkbenchBlockEntity bench) {
        if (level.isClientSide || bench.dismantled || bench.isRemoved()
                || bench.lastTick == level.getGameTime()) return;
        bench.lastTick = level.getGameTime();
        boolean changed = false;
        for (Work work : bench.works.values()) {
            if (!work.busy() || work.startedTick == level.getGameTime()) continue;
            // Count actual loaded ticks only. Closing, time commands, chunk loading
            // and a server restart must not convert wall time into completed work.
            work.remaining--;
            changed = true;
            if (work.remaining == 0) {
                work.result.setItem(0, work.pending);
                work.pending = ItemStack.EMPTY;
                work.reservedSource = ItemStack.EMPTY;
                work.snapshotVersion++;
                level.playSound(null, pos, SoundEvents.UI_STONECUTTER_TAKE_RESULT,
                        SoundSource.BLOCKS, 0.7F, 1.1F);
            }
        }
        if (changed) bench.setChanged();
    }

    /** Break cancels unfinished work: the reserved original is the only refund. */
    public void dismantle() {
        if (dismantled || level == null || level.isClientSide) return;
        dismantled = true;
        for (Work work : works.values()) {
            ItemStack refund = work.busy() ? work.reservedSource : ItemStack.EMPTY;
            work.reservedSource = ItemStack.EMPTY;
            work.pending = ItemStack.EMPTY;
            work.remaining = 0;
            work.snapshotVersion++;
            drop(refund);
            drop(work.input.removeItemNoUpdate(0));
            drop(work.result.removeItemNoUpdate(0));
        }
        works.clear();
        setChanged();
    }

    private void drop(ItemStack stack) {
        if (!stack.isEmpty()) Containers.dropItemStack(level,
                worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, stack);
    }

    // Chunk updates go to every observer. Private photographs are sent only by
    // the owner's menu packet, never in a block-entity update or chunk tag.
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return new CompoundTag();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ValueOutput.ValueOutputList list = output.childrenList("Works");
        works.forEach((owner, work) -> {
            if (!work.busy() && work.input.isEmpty() && work.result.isEmpty()) return;
            ValueOutput entry = list.addChild();
            // CompoundTag.putUUID used the same four-integer representation.
            entry.putIntArray("Owner", uuidToIntArray(owner));
            entry.store("Input", ItemStack.OPTIONAL_CODEC, work.input.getItem(0));
            entry.store("Result", ItemStack.OPTIONAL_CODEC, work.result.getItem(0));
            entry.store("Pending", ItemStack.OPTIONAL_CODEC, work.pending);
            entry.store("ReservedSource", ItemStack.OPTIONAL_CODEC, work.reservedSource);
            entry.putInt("Remaining", work.remaining);
            entry.putInt("Revision", work.revision);
            entry.putLong("Seed", work.seed);
        });
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        works.clear();
        dismantled = false;
        lastTick = Long.MIN_VALUE;
        for (ValueInput entry : input.childrenListOrEmpty("Works")) {
            UUID owner = entry.getIntArray("Owner")
                    .filter(parts -> parts.length == 4)
                    .map(FrameWorkbenchBlockEntity::uuidFromIntArray)
                    .orElse(null);
            if (owner == null || works.containsKey(owner)) continue;
            Work work = new Work(this);
            work.input.setItem(0, readStack(entry, "Input"));
            work.result.setItem(0, readStack(entry, "Result"));
            work.pending = readStack(entry, "Pending");
            work.reservedSource = readStack(entry, "ReservedSource");
            work.remaining = Math.max(0, Math.min(FrameWorkbenchMenu.CRAFT_DURATION_TICKS,
                    entry.getIntOr("Remaining", 0)));
            work.revision = entry.getIntOr("Revision", 0) & 0x7fff;
            work.seed = entry.getLongOr("Seed", 0L);
            // Incomplete or damaged work cannot become a finished photograph.
            // Keep the original recoverable rather than synthesizing an output.
            if (work.pending.isEmpty() || work.remaining == 0 || !work.result.isEmpty()) {
                work.pending = ItemStack.EMPTY;
                work.remaining = 0;
                if (work.result.isEmpty() && work.input.isEmpty() && !work.reservedSource.isEmpty()) {
                    work.input.setItem(0, work.reservedSource);
                }
                work.reservedSource = ItemStack.EMPTY;
            }
            if (work.busy() || !work.input.isEmpty() || !work.result.isEmpty()) works.put(owner, work);
        }
    }

    private static int[] uuidToIntArray(UUID uuid) {
        long most = uuid.getMostSignificantBits();
        long least = uuid.getLeastSignificantBits();
        return new int[]{(int)(most >> 32), (int)most, (int)(least >> 32), (int)least};
    }

    private static UUID uuidFromIntArray(int[] parts) {
        return new UUID(((long)parts[0] << 32) | (parts[1] & 0xffffffffL),
                ((long)parts[2] << 32) | (parts[3] & 0xffffffffL));
    }

    private static ItemStack readStack(ValueInput entry, String name) {
        ItemStack stack = entry.read(name, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        if (!stack.isEmpty()) stack.setCount(1);
        return stack;
    }

    static final class Work {
        final SimpleContainer input = new SimpleContainer(1);
        final SimpleContainer result = new SimpleContainer(1);
        ItemStack reservedSource = ItemStack.EMPTY;
        ItemStack pending = ItemStack.EMPTY;
        int remaining;
        int revision;
        long seed;
        long startedTick = Long.MIN_VALUE;
        long snapshotVersion;
        FrameWorkbenchMenu menu;

        Work(FrameWorkbenchBlockEntity bench) {
            input.addListener(ignored -> { snapshotVersion++; bench.setChanged(); });
            result.addListener(ignored -> { snapshotVersion++; bench.setChanged(); });
        }

        boolean busy() { return remaining > 0 && !pending.isEmpty(); }
        ItemStack preview() { return busy() ? pending : result.getItem(0); }
    }
}
