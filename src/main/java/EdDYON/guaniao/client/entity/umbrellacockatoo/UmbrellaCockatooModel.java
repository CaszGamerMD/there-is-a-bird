package EdDYON.guaniao.client.entity.umbrellacockatoo;

import EdDYON.guaniao.client.entity.mutation.BirdMutationRenderData;
import EdDYON.guaniao.client.entity.mutation.BirdMutationTextureFactory;
import EdDYON.guaniao.client.entity.mutation.GuaniaoGeckoAssets;
import EdDYON.guaniao.content.bird.umbrellacockatoo.UmbrellaCockatooDefinition;
import EdDYON.guaniao.content.bird.umbrellacockatoo.UmbrellaCockatooEntity;
import com.geckolib.cache.animation.Animation;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * GeckoLib 5 resources and original per-entity expression transition history.
 * Derived clips are rebuilt on resource-reload identity change; no authored media
 * or cached, shared animation is mutated.
 */
public class UmbrellaCockatooModel extends GeoModel<UmbrellaCockatooEntity> {
    public static final DataTicket<CockatooPoseComposer.RenderPose> EXPRESSION_POSE =
            DataTicket.create("guaniao_umbrella_cockatoo_pose", CockatooPoseComposer.RenderPose.class);
    private final Map<UmbrellaCockatooEntity, CockatooVisualState> expressions = new WeakHashMap<>();
    private Animation resourceVersion;
    private Animation displayResidual;
    private Animation ownerPerch;
    private CockatooExpressionSampler curves;

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return GuaniaoGeckoAssets.model(UmbrellaCockatooDefinition.MODEL);
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return BirdMutationRenderData.texture(state, UmbrellaCockatooDefinition.TEXTURE);
    }

    @Override
    public Identifier getAnimationResource(UmbrellaCockatooEntity animatable) {
        return GuaniaoGeckoAssets.animation(UmbrellaCockatooDefinition.ANIMATION);
    }

    @Override
    public void addAdditionalStateData(UmbrellaCockatooEntity bird, Object relatedObject,
                                       GeoRenderState state) {
        state.addGeckolibData(BirdMutationRenderData.MODEL_SCALE, bird.getModelRenderScale());
        BirdMutationRenderData.capture(state,
                BirdMutationTextureFactory.textureFor(bird.getTextureResource(), bird));
        state.addGeckolibData(EXPRESSION_POSE, samplePose(bird));
    }

    @Override
    public Animation getBakedAnimation(UmbrellaCockatooEntity bird, String name) {
        if (!name.equals(CockatooDisplayResidual.NAME)
                && !name.equals(CockatooOwnerPerchAnimation.NAME)) {
            return super.getBakedAnimation(bird, name);
        }
        loadExpressionData(bird);
        return name.equals(CockatooOwnerPerchAnimation.NAME) ? ownerPerch : displayResidual;
    }

    CockatooPoseComposer.RenderPose samplePose(UmbrellaCockatooEntity bird) {
        loadExpressionData(bird);
        // Preserve the original reference on first visible render and after
        // resource reload rather than replaying the opening display curve.
        var state = expressions.computeIfAbsent(bird,
                entity -> new CockatooVisualState(entity.needsDisplayReference() ? 2 : 0));
        var pose = state.sample(curves, bird.getExpressionLevel(), bird.getCockatooAnimationTick());
        float roll = bird.getHeadRoll();
        return new CockatooPoseComposer.RenderPose(pose, Math.abs(roll) < 0.05F ? 0 : roll);
    }

    private void loadExpressionData(UmbrellaCockatooEntity bird) {
        Animation version = super.getBakedAnimation(bird, "animation.emotion2");
        if (curves != null && version == resourceVersion) return;
        // The original authored JSON remains available alongside its unchanged
        // staged GeckoLib 5 copy. Read numeric expression tracks from that data.
        try (Reader reader = Minecraft.getInstance().getResourceManager().openAsReader(
                UmbrellaCockatooDefinition.ANIMATION)) {
            var raw = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("animations");
            var loaded = new CockatooExpressionSampler(raw);
            var residual = CockatooDisplayResidual.create(
                    super.getBakedAnimation(bird, "animation.idle_diff_2"), loaded.hold(2));
            var perched = CockatooOwnerPerchAnimation.create(
                    super.getBakedAnimation(bird, "animation.idle"),
                    getBakedModel(GuaniaoGeckoAssets.model(UmbrellaCockatooDefinition.MODEL)));
            curves = loaded;
            displayResidual = residual;
            ownerPerch = perched;
            resourceVersion = version;
            expressions.clear();
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load umbrella cockatoo expression curves", exception);
        }
    }
}
