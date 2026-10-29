package EdDYON.guaniao.client.entity.mutation;

import net.minecraft.resources.Identifier;

/**
 * Turn the original GeckoLib 4 authored paths into GeckoLib 5 resource IDs.
 * The model loader adds geckolib/models and .geo.json; the animation loader
 * adds geckolib/animations and .animation.json. No asset ID is renamed.
 */
public final class GuaniaoGeckoAssets {
    private GuaniaoGeckoAssets() { }

    public static Identifier model(Identifier authored) {
        return normalized(authored, "geo/", ".geo.json");
    }

    public static Identifier animation(Identifier authored) {
        return normalized(authored, "animations/", ".animation.json");
    }

    private static Identifier normalized(Identifier authored, String prefix, String suffix) {
        String path = authored.getPath();
        if (!path.startsWith(prefix) || !path.endsWith(suffix)
                || path.length() <= prefix.length() + suffix.length()) {
            throw new IllegalArgumentException("Unrecognized authored GeckoLib resource: " + authored);
        }
        return Identifier.fromNamespaceAndPath(authored.getNamespace(),
                path.substring(prefix.length(), path.length() - suffix.length()));
    }
}
