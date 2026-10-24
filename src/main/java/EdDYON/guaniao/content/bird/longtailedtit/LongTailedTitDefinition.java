package EdDYON.guaniao.content.bird.longtailedtit;

import net.minecraft.resources.Identifier;

public final class LongTailedTitDefinition {
    public static final String ENTITY_ID = "long_tailed_tit";
    public static final String SPAWN_EGG_ID = "long_tailed_tit_spawn_egg";
    public static final int SPAWN_EGG_BASE_COLOR = 0xE9E6E0;
    public static final int SPAWN_EGG_SPOT_COLOR = 0x9D6F75;
    public static final float WIDTH = 0.18F;
    public static final float HEIGHT = 0.20F;
    public static final Identifier MODEL = resource("geo/long_tailed_tit.geo.json");
    public static final Identifier TEXTURE = resource("textures/entity/long_tailed_tit.png");
    public static final Identifier ANIMATION = resource("animations/long_tailed_tit.animation.json");

    private LongTailedTitDefinition() {
    }

    private static Identifier resource(String path) {
        return Identifier.fromNamespaceAndPath("guaniao", path);
    }
}
