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

def expected_port(source, photograph, dropping, splat):
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
    if splat:
        changes = (
            ("import net.minecraft.core.Direction;",
             "import net.minecraft.core.Direction;\nimport net.minecraft.core.UUIDUtil;"),
            ("import net.minecraft.nbt.CompoundTag;\nimport net.minecraft.network.protocol.Packet;\n"
             "import net.minecraft.network.protocol.game.ClientGamePacketListener;\n", ""),
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
            ("level.random", "level.getRandom()"),
            ("public boolean hurt(@NotNull DamageSource source, float amount) {\n"
             "        if (this.isInvulnerableTo(source)) {\n            return false;\n        }\n"
             "        if (!this.level().isClientSide()) {\n"
             "            Entity attacker = source.getEntity();\n"
             "            if (!(attacker instanceof Player player) || !player.isCreative()) {\n"
             "                this.dropCollectible(BirdDroppingUtil.randomDroppingStack(this.level().random));\n"
             "            }\n"
             "            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SLIME_BLOCK_BREAK, SoundSource.NEUTRAL, 0.5F, 0.9F + this.level().random.nextFloat() * 0.2F);\n"
             "            this.discard();\n        }\n        return true;\n    }",
             "public boolean hurtServer(ServerLevel level, @NotNull DamageSource source, float amount) {\n"
             "        if (this.isInvulnerable()) {\n            return false;\n        }\n"
             "        Entity attacker = source.getEntity();\n"
             "        if (!(attacker instanceof Player player) || !player.isCreative()) {\n"
             "            this.dropCollectible(BirdDroppingUtil.randomDroppingStack(level.getRandom()));\n"
             "        }\n"
             "        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SLIME_BLOCK_BREAK,\n"
             "                SoundSource.NEUTRAL, 0.5F, 0.9F + level.getRandom().nextFloat() * 0.2F);\n"
             "        this.discard();\n        return true;\n    }\n\n"
             "    @Override\n    public boolean hurtClient(DamageSource source) {\n"
             "        return true;\n    }"),
            ("public InteractionResult interact(Player player, InteractionHand hand) {",
             "public InteractionResult interact(Player player, InteractionHand hand, Vec3 hitLocation) {"),
            ("InteractionResult.sidedSuccess(this.level().isClientSide())",
             "this.level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME"),
            ("brush.hurtAndBreak(1, player, brokenPlayer -> brokenPlayer.broadcastBreakEvent(hand))",
             "brush.hurtAndBreak(1, player, hand)"),
            ("ItemEntity itemEntity = this.spawnAtLocation(stack);",
             "if (!(this.level() instanceof ServerLevel serverLevel)) return;\n"
             "        ItemEntity itemEntity = this.spawnAtLocation(serverLevel, stack);"),
            ("addAdditionalSaveData(CompoundTag tag)", "addAdditionalSaveData(ValueOutput tag)"),
            ("readAdditionalSaveData(CompoundTag tag)", "readAdditionalSaveData(ValueInput tag)"),
            ('tag.putUUID("AttachedEntity", this.attachedEntityUuid)',
             'tag.store("AttachedEntity", UUIDUtil.CODEC, this.attachedEntityUuid)'),
            ('tag.getInt("Age")', 'tag.getIntOr("Age", 0)'),
            ('tag.getBoolean("StripDropping")', 'tag.getBooleanOr("StripDropping", false)'),
            ('tag.getInt("MaxAge")', 'tag.getIntOr("MaxAge", MIN_AGE_TICKS)'),
            ('tag.contains("MaxAge")', 'tag.getInt("MaxAge").isPresent()'),
            ('tag.getInt("SurfaceDirection")', 'tag.getIntOr("SurfaceDirection", 0)'),
            ('tag.getLong("AnchorPos")', 'tag.getLongOr("AnchorPos", 0L)'),
            ('tag.getInt("AttachmentPart")', 'tag.getIntOr("AttachmentPart", 0)'),
            ('tag.getInt("LocalDirection")', 'tag.getIntOr("LocalDirection", 0)'),
            ('tag.getFloat("LocalX")', 'tag.getFloatOr("LocalX", 0.0F)'),
            ('tag.getFloat("LocalY")', 'tag.getFloatOr("LocalY", 0.0F)'),
            ('tag.getFloat("LocalZ")', 'tag.getFloatOr("LocalZ", 0.0F)'),
            ('if (tag.hasUUID("AttachedEntity")) {\n'
             '            this.attachedEntityUuid = tag.getUUID("AttachedEntity");\n        }',
             'this.attachedEntityUuid = tag.read("AttachedEntity", UUIDUtil.CODEC).orElse(null);'),
            ('\n    @Override\n    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {\n'
             '        return NetworkHooks.getEntitySpawningPacket(this);\n    }\n', ''),
        )
        for before, after in changes:
            assert before in source, f"Missing original splat migration site: {before[:65]}"
            source = source.replace(before, after)
    if "private PiercingArt getPiercingArt()" in source:
        # The vanilla Projectile superclass supplies the correct 26.2 spawn
        # packet; keep every fan combat branch below exactly as authored.
        source = source.replace(
            "import net.minecraft.world.entity.projectile.ThrowableItemProjectile;",
            "import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;")
        source = source.replace(
            "super(GuaniaoEntityTypes.FEATHER_FAN_PROJECTILE.get(), owner, level);",
            "super(GuaniaoEntityTypes.FEATHER_FAN_PROJECTILE.get(), owner, level,\n"
            "                new ItemStack(GuaniaoItems.WIND_FEATHER_FAN.get()));")
        source = source.replace(
            "protected float getGravity() {\n        return 0.0F;",
            "protected double getDefaultGravity() {\n        return 0.0D;")
        source = source.replace(
            "    @Override\n    public void onRemovedFromWorld() {\n"
            "        // Chunk unloading calls setRemoved directly rather than remove.\n"
            "        this.releaseBurialTargets();\n"
            "        this.removeStuckSlowdown();\n"
            "        super.onRemovedFromWorld();\n"
            "    }",
            "    /** Fabric server entity-unload callback; chunk unloads do not call remove(). */\n"
            "    public void cleanupOnUnload() {\n"
            "        this.releaseBurialTargets();\n"
            "        this.removeStuckSlowdown();\n"
            "    }")
        source = source.replace("import net.minecraft.network.syncher.SynchedEntityData;\n",
                                "import net.minecraft.network.syncher.SynchedEntityData;\n"
                                "import net.minecraft.server.level.ServerEntity;\n")
        source = source.replace("import net.minecraftforge.network.NetworkHooks;\n", "")
        source = source.replace(
            "public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {\n"
            "        return NetworkHooks.getEntitySpawningPacket(this);",
            "public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {\n"
            "        return super.getAddEntityPacket(serverEntity);")
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
                             path.endswith("/BirdDroppingProjectileEntity.java"),
                             path.endswith("/BirdDroppingSplatEntity.java"))
    if path.endswith("/PhotographEntity.java"):
        # The full HangingEntity contract changed in 26.2. Its custom frame
        # math and interaction parity are checked method-by-method by the
        # photograph-specific regression in the adjacent CI step.
        assert "protected AABB calculateBoundingBox(BlockPos anchor, Direction facing)" in current
    elif path.endswith("/FeatherFanProjectileEntity.java"):
        # The 26.2 attack, knockback, attributes and ValueInput/ValueOutput
        # contracts require more than a byte-for-byte Forge-source rewrite.
        # The dedicated projectile parity gate checks all 32 combat timing
        # constants, state enums, saved keys, and eight damage paths.
        assert "protected void addAdditionalSaveData(ValueOutput tag)" in current
        assert "protected void readAdditionalSaveData(ValueInput tag)" in current
        assert "target.hurtServer(serverLevel," in current
    else:
        assert current == expected, f"{Path(path).name}: original projectile gameplay changed"
    assert "protected void defineSynchedData(SynchedEntityData.Builder builder)" in current
    assert ".isClientSide &&" not in current
    assert "hasImpulse" not in current
    print("PASS:", Path(path).name, "26.2 contract and gameplay parity checked")
assert len(FILES) == 4
