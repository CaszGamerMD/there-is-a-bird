#!/usr/bin/env python3
"""Check Minecraft 26.2 deferred fan submission preserves original fan visuals."""
from pathlib import Path
import subprocess

PATH = "src/main/java/EdDYON/guaniao/client/fan/FeatherFanProjectileRenderer.java"
STATE = "src/main/java/EdDYON/guaniao/client/fan/FeatherFanProjectileRenderState.java"
original = subprocess.check_output(["git", "show", "origin/main:" + PATH], text=True)
renderer = Path(PATH).read_text(encoding="utf-8")
state = Path(STATE).read_text(encoding="utf-8")

assert 'extends EntityRenderer<FeatherFanProjectileEntity, FeatherFanProjectileRenderState>' in renderer
assert 'extends EntityRenderState' in state
assert 'void extractRenderState(FeatherFanProjectileEntity entity,' in renderer
assert 'void submit(FeatherFanProjectileRenderState state, PoseStack poseStack,' in renderer
assert 'new ItemStackRenderState[8]' in state
assert 'getItemModelResolver()' in renderer
assert 'updateForTopItem(state.mainFan' in renderer
assert 'entity.getId() * 31 + i' in renderer
assert 'state.mainFan.submit(poseStack, collector,' in renderer
assert 'state.rivenFans[i].submit(poseStack, collector,' in renderer
assert 'MultiBufferSource' not in renderer and 'renderStatic' not in renderer
assert 'public void render(' not in renderer
registry = Path('src/main/java/EdDYON/guaniao/fabric/GuaniaoFabricEntityRenderers.java').read_text(encoding='utf-8')
assert 'EntityRendererRegistry.register(GuaniaoEntityTypes.FEATHER_FAN_PROJECTILE.get(), FeatherFanProjectileRenderer::new)' in registry
assert 'public ResourceLocation getTextureLocation(' not in renderer
for fragment in (
    'private static final float RENDER_SCALE = 1.35F;',
    '90.0F - pitch',
    'Axis.ZP.rotationDegrees(43.0F)',
    'poseStack.scale(0.90F, 0.90F, 0.90F);',
    'reformProgress * reformProgress * (3.0F - 2.0F * reformProgress)',
    'Mth.lerp(eased, 0.34F, 1.0F)',
    'Mth.lerp(entity.getCharge(), 30.0F, 55.0F)',
    '? 72.0F',
    'Mth.sin((entity.tickCount + partialTick) * 0.72F) * 7.5F',
    'float pulse = 0.82F + Mth.sin(age * 0.82F) * 0.035F;',
    'Math.sin(baseAngle * 2.0D) * 0.85D * heightScale',
    'poseStack.scale(0.82F, 0.82F, 0.82F);',
    'for (int i = 0; i < 8; i++)',
):
    assert fragment in original, fragment
for fragment in (
    '90.0F - state.pitch',
    'Axis.ZP.rotationDegrees(43.0F)',
    'poseStack.scale(0.90F, 0.90F, 0.90F);',
    'reformProgress * reformProgress * (3.0F - 2.0F * reformProgress)',
    'Mth.lerp(eased, 0.34F, 1.0F)',
    'Mth.lerp(state.charge, 30.0F, 55.0F)',
    '? 72.0F',
    'Mth.sin((state.ticks + state.partialTick) * 0.72F) * 7.5F',
    'float pulse = 0.82F + Mth.sin(age * 0.82F) * 0.035F;',
    'Math.sin(baseAngle * 2.0D) * 0.85D * heightScale',
    'poseStack.scale(0.82F, 0.82F, 0.82F);',
    'for (int i = 0; i < 8; i++)',
):
    assert fragment in renderer, fragment
assert 'state.hunting ? LightCoordsUtil.FULL_BRIGHT : state.lightCoords' in renderer
assert 'LightCoordsUtil.FULL_BRIGHT' in renderer
print('PASS: original fan spin, Riven 8-copy geometry, light and model seeds preserved')
