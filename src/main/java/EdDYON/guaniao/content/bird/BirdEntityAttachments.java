package EdDYON.guaniao.content.bird;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;

/**
 * Persistent, per-entity bird state formerly stored in Forge's getPersistentData() tag.
 * Keep each field optional: a missing attachment distinguishes a newly spawned bird
 * from one whose countdown or last-observed time is actually zero.
 */
public final class BirdEntityAttachments {
    public static final AttachmentType<Integer> MOLT_COOLDOWN = AttachmentRegistry.createPersistent(
            Identifier.fromNamespaceAndPath("guaniao", "molt_cooldown"), Codec.INT);
    public static final AttachmentType<Integer> DROPPING_COOLDOWN = AttachmentRegistry.createPersistent(
            Identifier.fromNamespaceAndPath("guaniao", "dropping_cooldown"), Codec.INT);
    public static final AttachmentType<Long> LAST_NEARBY_PLAYER_TIME = AttachmentRegistry.createPersistent(
            Identifier.fromNamespaceAndPath("guaniao", "last_nearby_player_time"), Codec.LONG);
    public static final AttachmentType<Long> TRANSIENT_FLYBY_SPAWN_TIME = AttachmentRegistry.createPersistent(
            Identifier.fromNamespaceAndPath("guaniao", "transient_flyby_spawn_time"), Codec.LONG);
    public static final AttachmentType<Float> INDIVIDUAL_WALK_FACTOR = AttachmentRegistry.createPersistent(
            Identifier.fromNamespaceAndPath("guaniao", "individual_walk_factor"), Codec.FLOAT);

    private BirdEntityAttachments() {
    }

    /** Eagerly register attachment types before loading entities from disk. */
    public static void registerAttachments() {
        // Class initialization registers all five persistent attachment types.
    }
}
