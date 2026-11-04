package EdDYON.guaniao.config;

import EdDYON.guaniao.GuaniaoMod;
import EdDYON.guaniao.content.camera.frame.FrameDesign;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Server-side crafting limits. Existing frames keep their saved dimensions. */
public final class FrameWorkbenchConfig {
    public static final int DEFAULT_MIN_PIXELS = 0;
    public static final int DEFAULT_MAX_PIXELS = 160;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir()
            .resolve(GuaniaoMod.MOD_ID).resolve("frame-workbench.json");
    private static volatile Settings settings = new Settings();

    private FrameWorkbenchConfig() { }

    /** Load once during Fabric initialization; corrupted files are not overwritten. */
    public static synchronized void loadOrCreateDefaults() {
        try {
            Files.createDirectories(PATH.getParent());
            if (Files.exists(PATH)) {
                try (Reader reader = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
                    JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                    if (!root.has("frameWorkbench") || !root.get("frameWorkbench").isJsonObject()) {
                        throw new IllegalArgumentException("Missing frameWorkbench settings object");
                    }
                    Settings loaded = GSON.fromJson(root.getAsJsonObject("frameWorkbench"), Settings.class);
                    if (loaded == null) throw new IllegalArgumentException("Empty frameWorkbench settings");
                    settings = normalized(loaded);
                }
            } else {
                JsonObject root = new JsonObject();
                root.add("frameWorkbench", GSON.toJsonTree(settings));
                Files.writeString(PATH, GSON.toJson(root), StandardCharsets.UTF_8);
            }
        } catch (Exception exception) {
            GuaniaoMod.LOGGER.warn("Failed to load frame-workbench settings; using defaults (file left unchanged)", exception);
            settings = new Settings();
        }
    }

    private static Settings normalized(Settings input) {
        Settings copy = new Settings();
        copy.minSizeBlocks = bounded(input.minSizeBlocks, 0.0D);
        copy.maxSizeBlocks = bounded(input.maxSizeBlocks, 10.0D);
        return copy;
    }

    private static double bounded(double value, double fallback) {
        return Double.isFinite(value) ? Math.max(0.0D, Math.min(32.0D, value)) : fallback;
    }

    private static final class Settings {
        double minSizeBlocks = 0.0D;
        double maxSizeBlocks = 10.0D;
    }

    public static int minSizePixels() {
        double lower = Math.min(minBlocks(), maxBlocks());
        return Math.min(FrameDesign.MAX_SIZE, (int)Math.ceil(lower * 16.0D));
    }

    public static int maxSizePixels() {
        double upper = Math.max(minBlocks(), maxBlocks());
        // A sub-pixel interval has no exact grid point. Use its nearest upper grid
        // point consistently rather than exposing an inverted range to sliders.
        return Math.max(minSizePixels(), Math.min(FrameDesign.MAX_SIZE, (int)Math.floor(upper * 16.0D)));
    }

    private static double minBlocks() {
        double value = settings.minSizeBlocks;
        return Double.isFinite(value) ? Math.max(0.0D, Math.min(32.0D, value)) : DEFAULT_MIN_PIXELS / 16.0D;
    }

    private static double maxBlocks() {
        double value = settings.maxSizeBlocks;
        return Double.isFinite(value) ? Math.max(0.0D, Math.min(32.0D, value)) : DEFAULT_MAX_PIXELS / 16.0D;
    }

    public static boolean allows(FrameDesign design) {
        return design.hasArea() && design.widthPixels() >= minSizePixels() && design.heightPixels() >= minSizePixels()
                && design.widthPixels() <= maxSizePixels() && design.heightPixels() <= maxSizePixels();
    }
}
