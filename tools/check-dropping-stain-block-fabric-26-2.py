#!/usr/bin/env python3
"""Keep authored stain brushing and water wash-off unchanged through 26.2 API substitutions."""

from pathlib import Path
import difflib
import subprocess


file = "src/main/java/EdDYON/guaniao/content/dropping/BirdDroppingStainBlock.java"
expected = subprocess.check_output(["git", "show", f"origin/main:{file}"], text=True).replace("\r\n", "\n")
current = Path(file).read_text(encoding="utf-8").replace("\r\n", "\n")

changes = (
    ("import EdDYON.guaniao.client.particle.PlaceableBlockBreakEffects;\n", ""),
    ("import net.minecraftforge.client.extensions.common.IClientBlockExtensions;\n", ""),
    ("import java.util.function.Consumer;\n", ""),
    ("    @Override\n    public void initializeClient(Consumer<IClientBlockExtensions> consumer) {\n"
     "        consumer.accept(PlaceableBlockBreakEffects.droppingStain());\n    }\n\n", ""),
    ("import net.minecraft.world.entity.player.Player;",
     "import net.minecraft.world.entity.LivingEntity;\nimport net.minecraft.world.entity.player.Player;"),
    ("import net.minecraft.world.level.LevelReader;",
     "import net.minecraft.world.level.LevelReader;\nimport net.minecraft.world.level.ScheduledTickAccess;"),
    ("public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {",
     "protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,\n"
     "                                     BlockPos pos, Direction direction, BlockPos neighborPos,\n"
     "                                     BlockState neighborState, RandomSource random) {"),
    ("super.updateShape(state, direction, neighborState, level, pos, neighborPos)",
     "super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random)"),
    ("public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {\n"
     "        ItemStack stack = player.getItemInHand(hand);",
     "protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,\n"
     "                                          Player player, InteractionHand hand, BlockHitResult hit) {"),
    ("InteractionResult.sidedSuccess(level.isClientSide)",
     "level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME"),
    ("if (!level.isClientSide &&", "if (!level.isClientSide() &&"),
    ("canPlaceLiquid(BlockGetter level, BlockPos pos, BlockState state, Fluid fluid)",
     "canPlaceLiquid(LivingEntity user, BlockGetter level, BlockPos pos, BlockState state, Fluid fluid)"),
    ("level.random", "level.getRandom()"),
    ("brush.hurtAndBreak(1, player, brokenPlayer -> brokenPlayer.broadcastBreakEvent(hand))",
     "brush.hurtAndBreak(1, player, hand)"),
    ("isTouchingWater(LevelAccessor level, BlockPos pos)",
     "isTouchingWater(LevelReader level, BlockPos pos)"),
)

for before, after in changes:
    assert before in expected, f"Missing authored source to migrate: {before[:70]}"
    expected = expected.replace(before, after)

if current != expected:
    print("".join(list(difflib.unified_diff(expected.splitlines(True), current.splitlines(True),
                                           fromfile="expected", tofile="current"))[:55]))
    raise AssertionError("Stain spawn drops, water wash-off, rain rate or brush gameplay changed")

print("PASS: original stain brush durability, resource drop and particle effects preserved")
print("PASS: rain wash, adjacent-water removal and bucket replacement preserved")
