package EdDYON.guaniao.fabric;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;

/** Standalone real Fabric 26.2 signature proof; not included in the mod JAR. */
public final class GuaniaoFeatherFanCombatProof {
    private GuaniaoFeatherFanCombatProof() { }

    public static void registerContract() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((victim, source, amount) -> true);
        ServerLivingEntityEvents.AFTER_DEATH.register((victim, source) -> {});
    }
}
