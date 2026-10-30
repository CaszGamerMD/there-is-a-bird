package EdDYON.guaniao.client.entity.kingfisher;

import EdDYON.guaniao.client.entity.mutation.BirdMutationRenderData;
import EdDYON.guaniao.client.entity.mutation.BirdMutationTextureFactory;
import EdDYON.guaniao.client.entity.mutation.GuaniaoGeckoAssets;
import EdDYON.guaniao.content.bird.kingfisher.KingfisherDefinition;
import EdDYON.guaniao.content.bird.kingfisher.KingfisherEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/** Preserve the authored handling dips and swallow lift after GeckoLib 5 animation. */
public final class KingfisherModel extends GeoModel<KingfisherEntity> {
    public static final DataTicket<Float> HEAD_DIP =
            DataTicket.create("guaniao_kingfisher_head_eating_pose", Float.class);
    public static final DataTicket<Float> BODY_DIP =
            DataTicket.create("guaniao_kingfisher_body_eating_pose", Float.class);

    @Override public Identifier getModelResource(GeoRenderState renderState) {
        return GuaniaoGeckoAssets.model(KingfisherDefinition.MODEL);
    }
    @Override public Identifier getTextureResource(GeoRenderState renderState) {
        return BirdMutationRenderData.texture(renderState, KingfisherDefinition.TEXTURE);
    }
    @Override public Identifier getAnimationResource(KingfisherEntity animatable) {
        return GuaniaoGeckoAssets.animation(KingfisherDefinition.ANIMATION);
    }

    @Override
    public void addAdditionalStateData(KingfisherEntity bird, Object relatedObject, GeoRenderState renderState) {
        renderState.addGeckolibData(BirdMutationRenderData.MODEL_SCALE, bird.getModelRenderScale());
        BirdMutationRenderData.capture(renderState,
                BirdMutationTextureFactory.textureFor(KingfisherDefinition.TEXTURE, bird));
        if (!bird.isEating()) return;
        float tick = bird.getEatingTicks(renderState.getPartialTick());
        // Keep the author's perched pose. Two brief bill dips handle the catch;
        // the final lift accompanies swallowing, never the spray or shake clips.
        float handling = pulse(tick, 11, 5) + pulse(tick, 22, 5);
        float swallow = pulse(tick, 38, 11);
        renderState.addGeckolibData(HEAD_DIP, (-26 * handling + 14 * swallow) * Mth.DEG_TO_RAD);
        renderState.addGeckolibData(BODY_DIP, -5 * handling * Mth.DEG_TO_RAD);
    }

    private static float pulse(float tick, float center, float radius) {
        float value = Mth.clamp(1 - Math.abs(tick - center) / radius, 0, 1);
        return value * value * (3 - 2 * value);
    }
}
