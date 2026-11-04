package EdDYON.guaniao.content.bird.cockatiel;

import net.minecraft.resources.Identifier;

public final class CockatielDefinition {
    public static final String ENTITY_ID = "cockatiel";
    public static final String SPAWN_EGG_ID = "cockatiel_spawn_egg";
    public static final int SPAWN_EGG_BASE_COLOR = 0x8C8983;
    public static final int SPAWN_EGG_SPOT_COLOR = 0xF2D45C;
    public static final float WIDTH = 0.24F;
    public static final float HEIGHT = 0.32F;
    public static final double MAX_HEALTH = 8.0D;
    public static final double WALK_SPEED = 0.24D;
    public static final double FLYING_SPEED = 0.34D;
    public static final double FOLLOW_RANGE = 20.0D;
    public static final Identifier MODEL = resource("geo/cockatiel.geo.json");
    public static final Identifier ANIMATION = resource("animations/cockatiel.animation.json");
    public static final Identifier[] TEXTURE_VARIANTS = new Identifier[]{
            resource("textures/entity/cockatiel/gray_yellow_face.png"),
            resource("textures/entity/cockatiel/dark_gray_yellow_face.png"),
            resource("textures/entity/cockatiel/gray_white_face.png"),
            resource("textures/entity/cockatiel/white_yellow_face.png"),
            resource("textures/entity/cockatiel/pale_yellow.png")
    };

    private CockatielDefinition() {
    }

    public static Identifier textureForVariant(int variant) {
        return TEXTURE_VARIANTS[Math.floorMod(variant, TEXTURE_VARIANTS.length)];
    }

    private static Identifier resource(String path) {
        return Identifier.fromNamespaceAndPath("guaniao", path);
    }
}
