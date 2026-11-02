#!/usr/bin/env python3
"""Compare authored breadcrumb ecology exactly, allowing only Minecraft 26.2 lifecycle hooks."""

from pathlib import Path
import difflib
import subprocess


file = "src/main/java/EdDYON/guaniao/content/feed/BreadcrumbPileBlock.java"
original = subprocess.check_output(["git", "show", f"origin/main:{file}"], text=True).replace("\r\n", "\n")
current = Path(file).read_text(encoding="utf-8").replace("\r\n", "\n")

expected = original
changes = (
    # This old Forge-only client extension was removed in an earlier
    # migration; modern break effects are routed through Fabric client hooks.
    ("import EdDYON.guaniao.client.particle.PlaceableBlockBreakEffects;\n", ""),
    ("import net.minecraftforge.client.extensions.common.IClientBlockExtensions;\n", ""),
    ("import java.util.function.Consumer;\n", ""),
    ("    @Override\n    public void initializeClient(Consumer<IClientBlockExtensions> consumer) {\n"
     "        consumer.accept(PlaceableBlockBreakEffects.breadcrumbs());\n"
     "    }\n\n", ""),
    # 26.2 light transparency is declared on the block properties rather than
    # using the removed propagatesSkylightDown override.
    ("    @Override\n    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {\n"
     "        return true;\n    }\n\n", ""),
    ("import net.minecraft.world.level.LevelAccessor;\n", ""),
    ("import net.minecraft.world.level.LevelReader;\n",
     "import net.minecraft.world.level.LevelReader;\nimport net.minecraft.world.level.ScheduledTickAccess;\n"),
    ("public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {\n"
     "        if (level instanceof ServerLevel serverLevel && !newState.is(this)) {\n"
     "            BreadcrumbSearchCache.remove(serverLevel, pos);\n"
     "        }\n"
     "        super.onRemove(state, level, pos, newState, movedByPiston);\n"
     "    }",
     "protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {\n"
     "        if (!level.getBlockState(pos).is(this)) {\n"
     "            BreadcrumbSearchCache.remove(level, pos);\n"
     "        }\n"
     "        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);\n"
     "    }"),
    ("public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {",
     "protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,\n"
     "                                     BlockPos pos, Direction direction, BlockPos neighborPos,\n"
     "                                     BlockState neighborState, RandomSource random) {"),
    ("return super.updateShape(state, direction, neighborState, level, pos, neighborPos);",
     "return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);"),
    ("if (!level.isClientSide) {", "if (!level.isClientSide()) {"),
)

for before, after in changes:
    assert before in expected, f"Missing original source to port: {before[:65]}"
    expected = expected.replace(before, after)
if current != expected:
    print("".join(list(difflib.unified_diff(expected.splitlines(True), current.splitlines(True),
                                           fromfile="expected", tofile="current"))[:55]))
    raise AssertionError("Breadcrumb state, rain decay, serving count, support or sparrow cache behavior changed")
registry = Path("src/main/java/EdDYON/guaniao/registry/GuaniaoBlocks.java").read_text(encoding="utf-8")
breadcrumb_registration = registry.split('BLOCKS.register("breadcrumbs",', 1)[1].split('BLOCKS.register(', 1)[0]
assert ".noCollision()" in breadcrumb_registration and ".noOcclusion()" in breadcrumb_registration
print("PASS: all original breadcrumb placement, servings, rain and bird search logic preserved")
print("PASS: removed skylight override is preserved by 26.2 no-occlusion block property")
print("PASS: removed blocks release the sparrow search cache through 26.2 server removal callback")
