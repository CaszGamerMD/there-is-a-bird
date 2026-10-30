package EdDYON.guaniao.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

/**
 * Compile-only proof of the real Minecraft 26.2 key mapping constructor,
 * Fabric registration, category and previously saved R -> Tab migration.
 * Not an entrypoint; it must never register duplicate keys at runtime.
 */
public final class GuaniaoCameraKeyMappingProof {
    private GuaniaoCameraKeyMappingProof() { }

    public static KeyMapping register() {
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("guaniao", "camera_probe"));
        return KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.guaniao.camera.probe", InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_TAB, category));
    }

    public static void migrateLegacyR(KeyMapping focus) {
        if (KeyMappingHelper.getBoundKeyOf(focus).equals(
                InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_R))) {
            focus.setKey(focus.getDefaultKey());
            KeyMapping.resetMapping();
        }
    }
}
