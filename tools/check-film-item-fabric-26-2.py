#!/usr/bin/env python3
"""Guard FilmItem's original photo behaviors while checking the Fabric item hooks."""

from pathlib import Path
import re
import subprocess

path = "src/main/java/EdDYON/guaniao/content/camera/FilmItem.java"
old = subprocess.check_output(["git", "show", "origin/main:" + path], text=True)
new = Path(path).read_text("utf-8")

def keys(source):
    return re.findall(r'Component\.translatable\("([^"]+)"', source)

assert keys(old) == keys(new), "Original film tooltip translation keys changed"
for fragment in (
    "LegacyPhotoMigration.queue(level, stack);",
    "PhotographData.hasImage(stack)",
    "PhotographData.photographer(stack)",
    "PhotoAuthorText.component(photographer)",
    '"EdDYON.guaniao.client.camera.PhotographClientActions"',
    '"openScreen", ItemStack.class',
    ".invoke(null, copy)",
    "ChatFormatting.GRAY",
    "ChatFormatting.DARK_GRAY",
):
    assert fragment in old and fragment in new, fragment
assert "inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot)" in new
assert "super.inventoryTick(stack, level, entity, slot);" in new
assert "InteractionResult use(Level level, Player player, InteractionHand hand)" in new
assert "level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME" in new
assert "appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display," in new
assert "Consumer<Component> tooltip, TooltipFlag flag)" in new
assert "tooltip.accept(" in new
assert "net.minecraftforge" not in new and "DistExecutor" not in new
assert "BlockEntityWithoutLevelRenderer" not in new
assert "FilmItemRenderer" not in new, "Client item renderer must not load on dedicated server"
assert "ItemStack copy = stack.copy();" in new, "Viewer must receive its own item snapshot"

print("PASS: FilmItem preserves original photo migration, preview, tooltip and color rules")
print("PASS: FilmItem uses native Minecraft 26.2 server tick, use and tooltip contracts")
