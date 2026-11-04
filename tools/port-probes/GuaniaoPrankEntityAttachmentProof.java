package EdDYON.guaniao.fabric;

import EdDYON.guaniao.content.dropping.PrankEntityAttachments;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.npc.villager.Villager;

/** Compile-only proof: immutable per-player maps and optional trade penalty attachments. */
public final class GuaniaoPrankEntityAttachmentProof {
    private GuaniaoPrankEntityAttachmentProof() {
    }

    public static void verify(Villager villager, IronGolem golem, String playerId, long now) {
        Map<String, Integer> villagerHits = new HashMap<>(villager.getAttachedOrElse(
                PrankEntityAttachments.VILLAGER_PLAYER_HITS, Map.of()));
        villagerHits.merge(playerId, 1, Integer::sum);
        villager.setAttached(PrankEntityAttachments.VILLAGER_PLAYER_HITS, Map.copyOf(villagerHits));

        Map<String, Integer> golemHits = new HashMap<>(golem.getAttachedOrElse(
                PrankEntityAttachments.GOLEM_PLAYER_HITS, Map.of()));
        golemHits.merge(playerId, 1, Integer::sum);
        golem.setAttached(PrankEntityAttachments.GOLEM_PLAYER_HITS, Map.copyOf(golemHits));

        Map<String, Long> times = new HashMap<>(golem.getAttachedOrElse(
                PrankEntityAttachments.GOLEM_PLAYER_HIT_TIMES, Map.of()));
        times.put(playerId, now);
        golem.setAttached(PrankEntityAttachments.GOLEM_PLAYER_HIT_TIMES, Map.copyOf(times));

        Integer penalty = villager.getAttached(PrankEntityAttachments.VILLAGER_TRADE_PENALTY);
        Integer applied = villager.getAttached(PrankEntityAttachments.VILLAGER_APPLIED_PENALTY);
        Long until = villager.getAttached(PrankEntityAttachments.VILLAGER_TRADE_PENALTY_UNTIL);
        if (penalty == null || applied == null || until == null) {
            villager.setAttached(PrankEntityAttachments.VILLAGER_TRADE_PENALTY, 2);
            villager.setAttached(PrankEntityAttachments.VILLAGER_APPLIED_PENALTY, 2);
            villager.setAttached(PrankEntityAttachments.VILLAGER_TRADE_PENALTY_UNTIL, now + 3600);
        }
        villager.removeAttached(PrankEntityAttachments.VILLAGER_TRADE_PENALTY);
    }
}
