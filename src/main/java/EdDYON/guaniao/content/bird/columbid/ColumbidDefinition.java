package EdDYON.guaniao.content.bird.columbid;

import EdDYON.guaniao.GuaniaoMod;
import net.minecraft.resources.Identifier;

public final class ColumbidDefinition {
    public static final Identifier MODEL = resource("geo/columbid.geo.json");
    public static final Identifier ANIMATION = resource("animations/columbid.animation.json");

    private ColumbidDefinition() {
    }

    static Identifier resource(String path) {
        return Identifier.fromNamespaceAndPath(GuaniaoMod.MOD_ID, path);
    }
}
