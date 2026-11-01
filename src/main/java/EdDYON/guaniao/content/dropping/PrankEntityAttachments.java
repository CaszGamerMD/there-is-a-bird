package EdDYON.guaniao.content.dropping;

import com.mojang.serialization.Codec;
import java.util.Map;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;

/** Villager trade reactions and golem warnings, saved per entity across restarts. */
public final class PrankEntityAttachments {
    // Dynamic keys from the original Forge NBT are now immutable maps indexed by player UUID.
    public static final AttachmentType<Map<String, Integer>> VILLAGER_PLAYER_HITS = AttachmentRegistry.createPersistent(
            Identifier.fromNamespaceAndPath("guaniao", "villager_dropping_player_hits"),
            Codec.unboundedMap(Codec.STRING, Codec.INT));
    public static final AttachmentType<Map<String, Integer>> GOLEM_PLAYER_HITS = AttachmentRegistry.createPersistent(
            Identifier.fromNamespaceAndPath("guaniao", "golem_dropping_player_hits"),
            Codec.unboundedMap(Codec.STRING, Codec.INT));
    public static final AttachmentType<Map<String, Long>> GOLEM_PLAYER_HIT_TIMES = AttachmentRegistry.createPersistent(
            Identifier.fromNamespaceAndPath("guaniao", "golem_dropping_player_hit_times"),
            Codec.unboundedMap(Codec.STRING, Codec.LONG));
    public static final AttachmentType<Integer> VILLAGER_TRADE_PENALTY = AttachmentRegistry.createPersistent(
            Identifier.fromNamespaceAndPath("guaniao", "villager_dropping_trade_penalty"), Codec.INT);
    public static final AttachmentType<Integer> VILLAGER_APPLIED_PENALTY = AttachmentRegistry.createPersistent(
            Identifier.fromNamespaceAndPath("guaniao", "villager_dropping_applied_penalty"), Codec.INT);
    public static final AttachmentType<Long> VILLAGER_TRADE_PENALTY_UNTIL = AttachmentRegistry.createPersistent(
            Identifier.fromNamespaceAndPath("guaniao", "villager_dropping_trade_penalty_until"), Codec.LONG);

    private PrankEntityAttachments() {
    }

    public static void registerAttachments() {
        // Class initialization registers all six persistent attachment types.
    }
}
