package EdDYON.guaniao.fabric;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Compile-only API probe for Minecraft 26.2 persistent block entity and
 * client sync hooks. This probe must never be registered or packaged.
 */
public final class GuaniaoPersistenceProof extends BlockEntity {
    private int count;
    private boolean prank;
    private java.util.UUID lastVisitor;

    // Compile-only: null block entity type must never be instantiated.
    public GuaniaoPersistenceProof() {
        super(null, BlockPos.ZERO, Blocks.CHEST.defaultBlockState());
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Count", this.count);
        output.putBoolean("Prank", this.prank);
        if (this.lastVisitor != null) {
            long hi = this.lastVisitor.getMostSignificantBits();
            long lo = this.lastVisitor.getLeastSignificantBits();
            output.putIntArray("LastVisitor", new int[]{(int)(hi >> 32), (int)hi, (int)(lo >> 32), (int)lo});
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        if (input.getInt("Count").isPresent()) {
            this.count = input.getIntOr("Count", 0);
        }
        this.prank = input.getBooleanOr("Prank", false);
        this.lastVisitor = input.getIntArray("LastVisitor")
                .filter(parts -> parts.length == 4)
                .map(parts -> new java.util.UUID(
                        ((long)parts[0] << 32) | (parts[1] & 0xffffffffL),
                        ((long)parts[2] << 32) | (parts[3] & 0xffffffffL)))
                .orElse(null);
    }

    /**
     * Compile-only proof of the exact 26.2 APIs used by crow nests and private
     * frame-workbench jobs; 1.20.1 NBT retains nested arrays/lists by key.
     */
    private static void verifyNestedInventoriesAndPrivateJobs(ValueOutput output, ValueInput input) {
        NonNullList<ItemStack> nestItems = NonNullList.withSize(6, ItemStack.EMPTY);
        ContainerHelper.saveAllItems(output.child("Treasures"), nestItems);
        ContainerHelper.loadAllItems(input.childOrEmpty("Treasures"), nestItems);

        ValueOutput.ValueOutputList claims = output.childrenList("CrowClaims");
        ValueOutput crowClaim = claims.addChild();
        crowClaim.putIntArray("Crow", new int[]{1, 2, 3, 4});
        crowClaim.putLong("ClaimTime", 42L);
        for (ValueInput claim : input.childrenListOrEmpty("CrowClaims")) {
            claim.getIntArray("Crow").filter(parts -> parts.length == 4);
            claim.getLongOr("ClaimTime", 0L);
        }

        ValueOutput.ValueOutputList workList = output.childrenList("Works");
        ValueOutput job = workList.addChild();
        job.store("Input", ItemStack.OPTIONAL_CODEC, ItemStack.EMPTY);
        job.putIntArray("Owner", new int[]{1, 2, 3, 4});
        for (ValueInput savedJob : input.childrenListOrEmpty("Works")) {
            savedJob.read("Input", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
            savedJob.getIntArray("Owner");
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
