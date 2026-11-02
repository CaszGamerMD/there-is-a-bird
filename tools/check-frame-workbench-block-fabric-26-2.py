#!/usr/bin/env python3
"""Guard the original frame-workbench body, screen, server ticker and removal flow."""
from pathlib import Path
import subprocess

root = Path(__file__).resolve().parents[1]
path = "src/main/java/EdDYON/guaniao/content/camera/frame/FrameWorkbenchBlock.java"
before = subprocess.check_output(["git", "show", "origin/main:" + path], cwd=root,
                                 text=True).replace("\r\n", "\n")
after = (root / path).read_text("utf-8").replace("\r\n", "\n")
registry = (root / "src/main/java/EdDYON/guaniao/registry/GuaniaoBlocks.java").read_text("utf-8")
gradle = (root / "build.gradle").read_text("utf-8")
proof = (root / "tools/port-probes/GuaniaoFrameWorkbenchBlockProof.java").read_text("utf-8")

start = "    private static final VoxelShape BODY = Shapes.or("
end = "    public FrameWorkbenchBlock(Properties properties) {"
assert before[before.index(start):before.index(end)] == after[after.index(start):after.index(end)], \
    "authored workbench silhouette, legs or collision changed"

for token in (
    "BlockStateProperties.HORIZONTAL_FACING", "registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH))",
    "context.getHorizontalDirection().getOpposite()", "EAST_WEST_SHAPE : NORTH_SOUTH_SHAPE",
    "protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos,",
    "protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,",
    "return openWorkbench(level, pos, player);",
    "player instanceof ServerPlayer serverPlayer", "serverPlayer.openMenu(",
    "new net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider<BlockPos>()",
    "return new FrameWorkbenchMenu(containerId, inventory, bench);",
    'Component.translatable("gui.guaniao.frame_workbench.title")',
    "level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME",
    "FrameWorkbenchBlockEntity::serverTick", "bench.dismantle();",
    "protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston)",
    "super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston)",
    "simpleCodec(FrameWorkbenchBlock::new)",
):
    assert token in after, "missing authored workbench behavior or modern 26.2 hook: " + token

for old_api in ("InteractionResult.sidedSuccess", "getPistonPushReaction(BlockState",
                "public InteractionResult use(BlockState", "public void onRemove(", "level.isClientSide &&"):
    assert old_api not in after, "old workbench block API remains: " + old_api

assert 'BLOCKS.register("frame_workbench"' in registry
assert ".pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK)" in registry
assert "GuaniaoFrameWorkbenchBlockProof.java" in gradle
for expected in ("extends BaseEntityBlock", "useItemOn(", "useWithoutItem(",
                 "affectNeighborsAfterRemoval(", "simpleCodec(", "pushReaction(PushReaction.BLOCK)"):
    assert expected in proof, "missing Java 25 block API proof: " + expected

print("PASS: original workbench geometry, horizontal facing and private menu opening preserved")
print("PASS: original non-pushable block and dismantling registered with modern lifecycle")
print("PASS: exact 26.2 block hooks and codec included in Java 25 focused compilation")
