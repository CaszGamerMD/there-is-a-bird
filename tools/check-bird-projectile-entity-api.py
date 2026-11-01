#!/usr/bin/env python3
"""Compare Minecraft 26.2 entity-data/side-sync moves with byte-for-byte authored gameplay."""
from pathlib import Path
import re
import subprocess

ROOT = Path(__file__).resolve().parents[1]
FILES = (
    "src/main/java/EdDYON/guaniao/content/dropping/BirdDroppingProjectileEntity.java",
    "src/main/java/EdDYON/guaniao/content/dropping/BirdDroppingSplatEntity.java",
    "src/main/java/EdDYON/guaniao/content/fan/FeatherFanProjectileEntity.java",
    "src/main/java/EdDYON/guaniao/content/camera/PhotographEntity.java",
)

def expected_port(source, photograph, dropping):
    source = source.replace(
        "protected void defineSynchedData()",
        "protected void defineSynchedData(SynchedEntityData.Builder builder)")
    source = source.replace("super.defineSynchedData();", "super.defineSynchedData(builder);")
    source = source.replace("this.entityData.define(", "builder.define(")
    source = source.replace("this.getEntityData().define(", "builder.define(")
    source = re.sub(r"\.isClientSide\b(?!\s*\()", ".isClientSide()", source)
    source = re.sub(r"\bhasImpulse\b", "needsSync", source)
    # Minecraft 26.2 moved both vanilla mob classes; this is an import-only
    # namespace migration and does not alter bird-dropping hit behavior.
    source = source.replace(
        "import net.minecraft.world.entity.animal.IronGolem;",
        "import net.minecraft.world.entity.animal.golem.IronGolem;")
    source = source.replace(
        "import net.minecraft.world.entity.npc.Villager;",
        "import net.minecraft.world.entity.npc.villager.Villager;")
    if photograph:
        source = source.replace("import net.minecraft.resources.ResourceLocation;",
                                "import net.minecraft.resources.Identifier;")
        source = source.replace("import net.minecraftforge.registries.ForgeRegistries;\n", "")
        source = source.replace("ResourceLocation", "Identifier")
        source = source.replace("ForgeRegistries.ITEMS.getKey(",
                                "BuiltInRegistries.ITEM.getKey(")
    if dropping:
        # Preserve the collision/visual algorithm while adapting the concrete
        # 26.2 ThrownItemEntity constructor, gravity and typed NBT contract.
        changes = (
            ("import net.minecraft.world.entity.projectile.ThrowableItemProjectile;",
             "import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;"),
            ("import net.minecraft.nbt.CompoundTag;\nimport net.minecraft.network.protocol.Packet;\n"
             "import net.minecraft.network.protocol.game.ClientGamePacketListener;",
             "import net.minecraft.core.UUIDUtil;"),
            ("import net.minecraft.world.item.Item;",
             "import net.minecraft.world.item.Item;\nimport net.minecraft.world.item.ItemStack;"),
            ("import net.minecraft.world.level.Level;",
             "import net.minecraft.world.level.Level;\nimport net.minecraft.world.level.storage.ValueInput;\n"
             "import net.minecraft.world.level.storage.ValueOutput;"),
            ("import net.minecraftforge.network.NetworkHooks;\n", ""),
            ("import software.bernie.geckolib.animatable.GeoEntity;\n"
             "import software.bernie.geckolib.core.animatable.GeoAnimatable;\n"
             "import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;\n"
             "import software.bernie.geckolib.core.animation.AnimatableManager;\n"
             "import software.bernie.geckolib.util.GeckoLibUtil;",
             "import com.geckolib.animatable.GeoEntity;\n"
             "import com.geckolib.animatable.instance.AnimatableInstanceCache;\n"
             "import com.geckolib.animatable.manager.AnimatableManager;\n"
             "import com.geckolib.util.GeckoLibUtil;"),
            ("GeckoLibUtil.createInstanceCache((GeoAnimatable)this)",
             "GeckoLibUtil.createInstanceCache(this)"),
            ("super(GuaniaoEntityTypes.BIRD_DROPPING_PROJECTILE.get(), owner, level);",
             "super(GuaniaoEntityTypes.BIRD_DROPPING_PROJECTILE.get(), owner, level,\n"
             "                new ItemStack(variant == null ? BirdDroppingVariant.ONE.item() : variant.item()));"),
            ("protected float getGravity() {\n        return this.isStripDropping() ? 0.025F : 0.06F;",
             "protected double getDefaultGravity() {\n        return this.isStripDropping() ? 0.025D : 0.06D;"),
            ("MobEffects.MOVEMENT_SLOWDOWN", "MobEffects.SLOWNESS"),
            ("level.random.nextFloat()", "level.getRandom().nextFloat()"),
            ("public void addAdditionalSaveData(CompoundTag tag)",
             "protected void addAdditionalSaveData(ValueOutput tag)"),
            ("public void readAdditionalSaveData(CompoundTag tag)",
             "protected void readAdditionalSaveData(ValueInput tag)"),
            ('tag.putUUID("SourceBirdUuid", this.sourceBirdUuid)',
             'tag.store("SourceBirdUuid", UUIDUtil.CODEC, this.sourceBirdUuid)'),
            ('tag.getInt("Variant")', 'tag.getIntOr("Variant", 0)'),
            ('tag.getBoolean("NaturalDropping")', 'tag.getBooleanOr("NaturalDropping", false)'),
            ('tag.getBoolean("StripDropping")', 'tag.getBooleanOr("StripDropping", false)'),
            ('tag.getBoolean("LaxativeDropping")', 'tag.getBooleanOr("LaxativeDropping", false)'),
            ('tag.getInt("LifeTicks")', 'tag.getIntOr("LifeTicks", 0)'),
            ('tag.contains("StripStartedAt")\n                ? tag.getLong("StripStartedAt") : this.level().getGameTime() - this.lifeTicks',
             'tag.getLongOr("StripStartedAt",\n                this.level().getGameTime() - this.lifeTicks)'),
            ('if (tag.hasUUID("SourceBirdUuid")) {\n            this.sourceBirdUuid = tag.getUUID("SourceBirdUuid");\n        }',
             'this.sourceBirdUuid = tag.read("SourceBirdUuid", UUIDUtil.CODEC).orElse(null);'),
            ('\n    @Override\n    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {\n'
             '        return NetworkHooks.getEntitySpawningPacket(this);\n    }\n', ''),
        )
        for before, after in changes:
            assert before in source, f"Missing original projectile migration site: {before[:60]}"
            source = source.replace(before, after)
    # Datapack enchantments are ResourceKeys in 26.2, not Forge-registered
    # Enchantment objects. Keep both original piercing-art decisions unchanged.
    if "private PiercingArt getPiercingArt()" in source:
        source = source.replace("import net.minecraft.world.item.enchantment.EnchantmentHelper;\n", "")
        source = source.replace(
            "if (EnchantmentHelper.getItemEnchantmentLevel(\n"
            "                GuaniaoEnchantments.RIVEN_PLUME.get(), fan) > 0) {",
            "if (GuaniaoEnchantments.has(fan, GuaniaoEnchantments.RIVEN_PLUME)) {")
        source = source.replace(
            "if (EnchantmentHelper.getItemEnchantmentLevel(\n"
            "                GuaniaoEnchantments.BURIAL_PLUME.get(), fan) > 0) {",
            "if (GuaniaoEnchantments.has(fan, GuaniaoEnchantments.BURIAL_PLUME)) {")
    return source

for path in FILES:
    authored = subprocess.check_output(
        ["git", "show", "origin/main:" + path], cwd=ROOT,
        text=True).replace("\r\n", "\n")
    current = (ROOT / path).read_text("utf-8").replace("\r\n", "\n")
    expected = expected_port(authored, path.endswith("/PhotographEntity.java"),
                             path.endswith("/BirdDroppingProjectileEntity.java"))
    assert current == expected, f"{Path(path).name}: original projectile/photo gameplay changed"
    assert "protected void defineSynchedData(SynchedEntityData.Builder builder)" in current
    assert ".isClientSide &&" not in current
    assert "hasImpulse" not in current
    print("PASS:", Path(path).name, "original behavior retained")
assert len(FILES) == 4
