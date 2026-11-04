package EdDYON.guaniao.client.entity.hummingbird;

import EdDYON.guaniao.content.bird.hummingbird.HummingbirdEntity;
import com.geckolib.loading.math.MolangQueries;
import net.minecraft.util.Mth;

/**
 * GeckoLib 5 per-actor replacements for the nine original hummingbird Molang
 * variables. No shared "last bird" parser values or global animation clock.
 */
public final class HummingbirdMolangQueries {
    private static boolean registered;

    private HummingbirdMolangQueries() { }

    public static void register() {
        if (registered) return;
        registered = true;

        MolangQueries.<HummingbirdEntity>setActorVariable("variable.flight_time", actor -> {
            HummingbirdEntity bird = actor.animatable();
            return bird.getFlightAnimationTime() + (bird.isAirborne() ? actor.partialTick() / 20.0 : 0);
        });
        MolangQueries.<HummingbirdEntity>setActorVariable("variable.wing_stroke_time", actor ->
                actor.animatable().getWingStrokeTime(actor.partialTick()));
        MolangQueries.<HummingbirdEntity>setActorVariable("variable.forward_blend", actor ->
                actor.animatable().getForwardFlightBlend(actor.partialTick()));
        MolangQueries.<HummingbirdEntity>setActorVariable("variable.backward_blend", actor ->
                actor.animatable().getBackwardFlightBlend(actor.partialTick()));
        MolangQueries.<HummingbirdEntity>setActorVariable("variable.lateral_blend", actor ->
                actor.animatable().getLateralFlightBlend(actor.partialTick()));
        MolangQueries.<HummingbirdEntity>setActorVariable("variable.wing_pitch", actor -> {
            HummingbirdEntity bird = actor.animatable();
            double nectar = Mth.clamp(bird.getNectarProgress(), 0, 1);
            double forwardBlend = bird.getForwardFlightBlend(actor.partialTick());
            double backwardBlend = bird.getBackwardFlightBlend(actor.partialTick());
            return Mth.lerp(nectar, 40 - forwardBlend * 7.5 + backwardBlend * 7.5, 40);
        });
        MolangQueries.<HummingbirdEntity>setActorVariable("variable.wing_yaw_bias", actor -> {
            HummingbirdEntity bird = actor.animatable();
            float bank = bird.isAirborne() && !bird.isSleeping()
                    ? Mth.clamp(bird.getVisualBankAngle(actor.partialTick()), -15, 15) : 0;
            return Mth.clamp(bank * .3, -4, 4);
        });
        MolangQueries.<HummingbirdEntity>setActorVariable("variable.bank_angle", actor -> {
            HummingbirdEntity bird = actor.animatable();
            return bird.isAirborne() && !bird.isSleeping()
                    ? Mth.clamp(bird.getVisualBankAngle(actor.partialTick()), -15, 15) : 0;
        });
    }
}
