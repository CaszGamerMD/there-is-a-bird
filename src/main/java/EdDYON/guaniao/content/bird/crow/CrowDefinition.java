package EdDYON.guaniao.content.bird.crow;

import net.minecraft.resources.Identifier;

public final class CrowDefinition {
    public static final String ENTITY_ID = "crow";
    public static final String SPAWN_EGG_ID = "crow_spawn_egg";
    public static final int SPAWN_EGG_BASE_COLOR = 0x141820;
    public static final int SPAWN_EGG_SPOT_COLOR = 0x6D7582;
    public static final float WIDTH = 0.34F;
    public static final float HEIGHT = 0.50F;
    public static final double MAX_HEALTH = 8.0D;
    public static final double WALK_SPEED = 0.25D;
    public static final double FLYING_SPEED = 0.42D;
    public static final double FOLLOW_RANGE = 18.0D;
    public static final Identifier MODEL = Identifier.fromNamespaceAndPath("guaniao", "geo/crow.geo.json");
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("guaniao", "textures/entity/crow.png");
    public static final Identifier ANIMATION = Identifier.fromNamespaceAndPath("guaniao", "animations/crow.animation.json");

    private CrowDefinition() {
    }
}
