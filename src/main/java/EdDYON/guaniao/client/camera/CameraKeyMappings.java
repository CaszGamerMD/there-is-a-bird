package EdDYON.guaniao.client.camera;

import EdDYON.guaniao.GuaniaoMod;
import com.mojang.blaze3d.platform.InputConstants;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

/** Original camera shortcuts, registered once on Fabric's client initializer. */
public final class CameraKeyMappings {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(GuaniaoMod.MOD_ID, "camera"));
    public static final KeyMapping OPEN_FILTER_LIBRARY = new KeyMapping(
            "key.guaniao.camera.cycle_filter",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            CATEGORY
    );
    public static final KeyMapping OPEN_CREATIVE_CONTROLS = new KeyMapping(
            "key.guaniao.camera.open_creative_controls",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            CATEGORY
    );
    public static final KeyMapping FOCUS = new KeyMapping(
            "key.guaniao.camera.focus",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_TAB,
            CATEGORY
    );
    private static boolean focusMigrationChecked;

    private CameraKeyMappings() { }

    public static void register() {
        KeyMappingHelper.registerKeyMapping(OPEN_FILTER_LIBRARY);
        KeyMappingHelper.registerKeyMapping(OPEN_CREATIVE_CONTROLS);
        KeyMappingHelper.registerKeyMapping(FOCUS);
    }

    /** Only runs on Fabric's end-of-client-tick, after the saved keys load. */
    public static void tick() {
        migrateLegacyFocusKey();
        while (OPEN_FILTER_LIBRARY.consumeClick()) {
            if (CameraClientCapture.canUseViewfinderControls()) CameraFilterPickerScreen.open();
        }
        while (OPEN_CREATIVE_CONTROLS.consumeClick()) {
            if (CameraClientCapture.canUseViewfinderControls()) CameraCreativeControlsScreen.open();
        }
        while (FOCUS.consumeClick()) {
            if (CameraClientCapture.canUseViewfinderControls()) CameraClientCapture.focusAtCrosshair();
        }
    }

    /**
     * A previous release used unmodified R for focus; the newer default is Tab.
     * Preserve the old one-time preference marker, user overrides and the
     * options.txt verification instead of resetting every existing binding.
     *
     * Forge-specific key modifiers are not part of Fabric's vanilla keymap.
     * The legacy R binding is changed only when the saved key is exactly R.
     */
    static void migrateLegacyFocusKey() {
        Minecraft minecraft = Minecraft.getInstance();
        if (focusMigrationChecked || minecraft.options == null || minecraft.gui.overlay() != null
                || Arrays.stream(minecraft.options.keyMappings).noneMatch(binding -> binding == FOCUS)) return;
        focusMigrationChecked = true;
        if (!CameraClientPreferences.isFocusKeyMigrationPending()) return;

        if (KeyMappingHelper.getBoundKeyOf(FOCUS).equals(
                InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_R))) {
            FOCUS.setKey(FOCUS.getDefaultKey());
            KeyMapping.resetMapping();
            minecraft.options.save();
            // Vanilla Options.save logs failures instead of throwing.
            Path options = minecraft.gameDirectory.toPath().resolve("options.txt");
            String savedBinding = "key_" + FOCUS.getName() + ":" + FOCUS.saveString();
            try (Stream<String> lines = Files.lines(options, StandardCharsets.UTF_8)) {
                if (lines.noneMatch(savedBinding::equals)) {
                    GuaniaoMod.LOGGER.warn("Camera focus key migration was not saved; retrying next launch");
                    return;
                }
            } catch (IOException | UncheckedIOException exception) {
                GuaniaoMod.LOGGER.warn("Could not verify camera focus key migration; retrying next launch", exception);
                return;
            }
        }
        CameraClientPreferences.markFocusKeyMigrated();
    }
}
