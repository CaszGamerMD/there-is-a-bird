package EdDYON.guaniao.client.camera;

import EdDYON.guaniao.content.camera.CameraFilter;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;

/**
 * Native Minecraft 26.2 post effect lookup for all author camera filters.
 *
 * ShaderManager owns post-chain compilation, caching and reload invalidation.
 * Do not construct the removed Forge-era PostChain or retain a chain across
 * resource reloads. The caller must supply a PRIVATE framebuffer when it
 * later executes the chain; minecraft:main is only the shader's graph-local
 * input/output alias and must never refer to the live world target.
 */
public final class CameraPostChain26 {
    private static final String MOD_ID = "guaniao";
    private static final Set<Identifier> PRIVATE_MAIN_ALIAS = Set.of(PostChain.MAIN_TARGET_ID);
    private static final Identifier OPTICS =
            Identifier.fromNamespaceAndPath(MOD_ID, "camera_optics");

    private CameraPostChain26() { }

    public static Identifier filterId(CameraFilter filter) {
        if (filter == null || filter == CameraFilter.NONE) {
            return null;
        }
        return Identifier.fromNamespaceAndPath(MOD_ID,
                String.format(Locale.ROOT, "camera_filter_%02d", filter.id()));
    }

    /**
     * @return compiled original GPU filter, or null for NONE/missing resources.
     *         Does not process a frame or modify the main world target.
     */
    public static PostChain filter(Minecraft minecraft, CameraFilter filter) {
        Objects.requireNonNull(minecraft, "minecraft");
        Identifier id = filterId(filter);
        return id == null ? null : minecraft.getShaderManager().getPostChain(id, PRIVATE_MAIN_ALIAS);
    }

    /**
     * Resolves only the shader. The native render-graph owner must still feed
     * dynamic aperture/focus/distortion and the 26.2 reversed-depth attachment.
     */
    public static PostChain optics(Minecraft minecraft) {
        Objects.requireNonNull(minecraft, "minecraft");
        return minecraft.getShaderManager().getPostChain(OPTICS, PRIVATE_MAIN_ALIAS);
    }
}
