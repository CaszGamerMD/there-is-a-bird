#!/usr/bin/env python3
"""Check photograph preview, original framing and secure world placement after 26.2 port."""

from pathlib import Path
import re
import subprocess

path = "src/main/java/EdDYON/guaniao/content/camera/PhotographItem.java"
old = subprocess.check_output(["git", "show", "origin/main:" + path], text=True)
new = Path(path).read_text("utf-8")

def keys(source):
    return re.findall(r'Component\.translatable\("([^"]+)"', source)

assert keys(old) == keys(new), "Photograph frame/tooltips changed authored translation keys"
for fragment in (
    "LegacyPhotoMigration.queue(level, stack);",
    "LegacyPhotoMigration.migrateNow(level, stack);",
    "PhotographData.hasImage(stack)",
    "PhotographData.photographer(stack)",
    "PhotoAuthorText.component(photographer)",
    "FrameDesign.read(stack)",
    "placementDesign.hasArea()",
    "face.getAxis().isHorizontal()",
    "player.mayBuild()",
    "level.mayInteract(player, placePos)",
    "player.mayUseItemAt(placePos, face, stack)",
    "entity.survives()",
    "level.addFreshEntity(entity)",
    "SoundEvents.ITEM_FRAME_PLACE",
    "stack.shrink(1)",
    '"EdDYON.guaniao.client.camera.PhotographClientActions"',
    '"openScreen", ItemStack.class',
    ".invoke(null, copy)",
    "BigDecimal.valueOf(pixels).divide(BigDecimal.valueOf(16)).stripTrailingZeros().toPlainString()",
):
    assert fragment in old and fragment in new, fragment

assert "inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot)" in new
assert "super.inventoryTick(stack, level, entity, slot);" in new
assert "InteractionResult use(Level level, Player player, InteractionHand hand)" in new
assert "InteractionResult useOn(UseOnContext context)" in new
assert new.count("level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME") == 2
assert "if (!level.isClientSide())" in new, "Only server may spawn a photo entity"
assert "Item material = BuiltInRegistries.ITEM.getValue(design.frameMaterial());" in new
assert "material != null ? material.getDescription()" in new
assert "appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display," in new
assert "Consumer<Component> tooltip, TooltipFlag flag)" in new
assert new.count("tooltip.accept(") == old.count("tooltip.add(")
assert "ItemStack copy = stack.copy();" in new
assert "placedPhoto.setCount(1);" in new
assert "LegacyPhotoMigration.queue(level, placedPhoto);" in new
assert "new PhotographEntity(level, placePos, face, placedPhoto)" in new
assert "net.minecraftforge" not in new and "DistExecutor" not in new
assert "BlockEntityWithoutLevelRenderer" not in new and "PhotographItemRenderer" not in new

print("PASS: PhotographItem preserves authored tooltip and photograph preview behavior")
print("PASS: Secure horizontal framing, photo-item consumption and 26.2 API signatures preserved")
