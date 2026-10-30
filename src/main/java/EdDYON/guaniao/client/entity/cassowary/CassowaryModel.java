package EdDYON.guaniao.client.entity.cassowary;

import EdDYON.guaniao.client.entity.mutation.BirdMutationRenderData;
import EdDYON.guaniao.client.entity.mutation.BirdMutationTextureFactory;
import EdDYON.guaniao.client.entity.mutation.GuaniaoGeckoAssets;
import EdDYON.guaniao.content.bird.cassowary.CassowaryDefinition;
import EdDYON.guaniao.content.bird.cassowary.CassowaryEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * GeckoLib 5 renders each cassowary from immutable per-bird state.
 * Keep the original head-led gaze and upper/lower-neck distribution.
 */
public final class CassowaryModel extends GeoModel<CassowaryEntity> {
    public static final DataTicket<Float> HEAD_PITCH =
            DataTicket.create("guaniao_cassowary_head_pitch", Float.class);
    public static final DataTicket<Float> HEAD_YAW =
            DataTicket.create("guaniao_cassowary_head_yaw", Float.class);
    public static final DataTicket<Float> UPPER_NECK_PITCH =
            DataTicket.create("guaniao_cassowary_upper_neck_pitch", Float.class);
    public static final DataTicket<Float> UPPER_NECK_YAW =
            DataTicket.create("guaniao_cassowary_upper_neck_yaw", Float.class);
    public static final DataTicket<Float> LOWER_NECK_PITCH =
            DataTicket.create("guaniao_cassowary_lower_neck_pitch", Float.class);
    public static final DataTicket<Float> LOWER_NECK_YAW =
            DataTicket.create("guaniao_cassowary_lower_neck_yaw", Float.class);

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return GuaniaoGeckoAssets.model(CassowaryDefinition.MODEL);
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return BirdMutationRenderData.texture(renderState, CassowaryDefinition.TEXTURE);
    }

    @Override
    public Identifier getAnimationResource(CassowaryEntity animatable) {
        return GuaniaoGeckoAssets.animation(CassowaryDefinition.ANIMATION);
    }

    @Override
    public void addAdditionalStateData(CassowaryEntity animatable, Object relatedObject,
                                       GeoRenderState renderState) {
        renderState.addGeckolibData(BirdMutationRenderData.MODEL_SCALE,
                animatable.getModelRenderScale());
        BirdMutationRenderData.capture(renderState,
                BirdMutationTextureFactory.textureFor(CassowaryDefinition.TEXTURE, animatable));

        // Original gaze weight cutoff and degree-to-radian math, with no shared
        // mutable model bones between birds. The authored eye bones remain untouched.
        float weight = animatable.getClientGazeWeight();
        if (weight <= 0.001F) return;
        float weightedRadians = weight * Mth.DEG_TO_RAD;
        renderState.addGeckolibData(HEAD_PITCH, animatable.getClientHeadPitch() * weightedRadians);
        renderState.addGeckolibData(HEAD_YAW, animatable.getClientHeadYaw() * weightedRadians);
        renderState.addGeckolibData(UPPER_NECK_PITCH, animatable.getClientUpperNeckPitch() * weightedRadians);
        renderState.addGeckolibData(UPPER_NECK_YAW, animatable.getClientUpperNeckYaw() * weightedRadians);
        renderState.addGeckolibData(LOWER_NECK_PITCH, animatable.getClientLowerNeckPitch() * weightedRadians);
        renderState.addGeckolibData(LOWER_NECK_YAW, animatable.getClientLowerNeckYaw() * weightedRadians);
    }
}
