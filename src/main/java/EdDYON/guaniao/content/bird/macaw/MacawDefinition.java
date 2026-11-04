package EdDYON.guaniao.content.bird.macaw;

import net.minecraft.resources.Identifier;

public final class MacawDefinition {
    public static final String ENTITY_ID = "macaw";
    public static final String SPAWN_EGG_ID = "macaw_spawn_egg";
    public static final int SPAWN_EGG_BASE_COLOR = 0xC82E32;
    public static final int SPAWN_EGG_SPOT_COLOR = 0xF2C94C;
    public static final float WIDTH = 0.36F;
    public static final float HEIGHT = 0.64F;
    public static final double MAX_HEALTH = 14.0D;
    public static final double WALK_SPEED = 0.22D;
    public static final double FLYING_SPEED = 0.38D;
    public static final double FOLLOW_RANGE = 28.0D;
    public static final Identifier MODEL = resource("geo/macaw.geo.json");
    public static final Identifier ANIMATION = resource("animations/macaw.animation.json");
    public static final Identifier[] TEXTURE_VARIANTS = new Identifier[]{
            resource("textures/entity/macaw/variant_1.png"),
            resource("textures/entity/macaw/variant_2.png"),
            resource("textures/entity/macaw/variant_3.png"),
            resource("textures/entity/macaw/variant_4.png"),
            resource("textures/entity/macaw/variant_5.png")
    };

    private MacawDefinition() {
    }

    public static Identifier textureForVariant(int variant) {
        return TEXTURE_VARIANTS[Math.floorMod(variant, TEXTURE_VARIANTS.length)];
    }

    private static Identifier resource(String path) {
        return Identifier.fromNamespaceAndPath("guaniao", path);
    }
}
