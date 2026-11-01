package EdDYON.guaniao.config;

import EdDYON.guaniao.GuaniaoMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Persisted Fabric settings; the original Forge limits and defaults are unchanged. */
public final class HummingbirdConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir()
            .resolve(GuaniaoMod.MOD_ID).resolve("hummingbird.json");
    private static volatile Settings settings = new Settings();

    private HummingbirdConfig() { }

    public static synchronized void loadOrCreateDefaults() {
        try {
            Files.createDirectories(PATH.getParent());
            if (Files.exists(PATH)) {
                try (Reader reader = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
                    JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                    if (!root.has("hummingbird") || !root.get("hummingbird").isJsonObject()) {
                        throw new IllegalArgumentException("Missing hummingbird settings object");
                    }
                    Settings loaded = GSON.fromJson(root.getAsJsonObject("hummingbird"), Settings.class);
                    if (loaded == null) throw new IllegalArgumentException("Empty hummingbird settings");
                    settings = normalized(loaded);
                }
            } else {
                JsonObject root = new JsonObject();
                root.add("hummingbird", GSON.toJsonTree(settings));
                Files.writeString(PATH, GSON.toJson(root), StandardCharsets.UTF_8);
            }
        } catch (Exception exception) {
            GuaniaoMod.LOGGER.warn("Failed to load hummingbird settings; using defaults (file left unchanged)", exception);
            settings = new Settings();
        }
    }

    private static int bounded(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
    private static double bounded(double value, double min, double max) {
        return Double.isFinite(value) ? Math.max(min, Math.min(max, value)) : min;
    }

    private static Settings normalized(Settings input) {
        Settings copy = new Settings();
        copy.nectarTicks = bounded(input.nectarTicks, 20, 200);
        copy.tamingFeeds = bounded(input.tamingFeeds, 1, 20);
        copy.pollinationEnabled = input.pollinationEnabled;
        copy.pollinationPeriodTicks = bounded(input.pollinationPeriodTicks, 100, 2400);
        copy.pollinationRadius = bounded(input.pollinationRadius, 1, 16);
        copy.pollinationGraceTicks = bounded(input.pollinationGraceTicks, 0, 400);
        copy.flowerCooldownMinTicks = bounded(input.flowerCooldownMinTicks, 200, 12000);
        copy.flowerCooldownMaxTicks = bounded(input.flowerCooldownMaxTicks, 200, 12000);
        copy.flowerScanBudget = bounded(input.flowerScanBudget, 256, 16384);
        copy.cropBudget = bounded(input.cropBudget, 16, 4096);
        copy.seedMinTicks = bounded(input.seedMinTicks, 1200, 72000);
        copy.seedMaxTicks = bounded(input.seedMaxTicks, 1200, 72000);
        copy.seedChance = bounded(input.seedChance, 0, 1);
        copy.territoryMaleChance = bounded(input.territoryMaleChance, 0, 1);
        copy.territoryFemaleChance = bounded(input.territoryFemaleChance, 0, 1);
        copy.territoryCooldownTicks = bounded(input.territoryCooldownTicks, 200, 12000);
        copy.gardenBonus = bounded(input.gardenBonus, 0, 0.15);
        return copy;
    }

    private static final class Settings {
        int nectarTicks = 60;
        int tamingFeeds = 5;
        boolean pollinationEnabled = true;
        int pollinationPeriodTicks = 400;
        double pollinationRadius = 8.0;
        int pollinationGraceTicks = 120;
        int flowerCooldownMinTicks = 1200;
        int flowerCooldownMaxTicks = 2400;
        int flowerScanBudget = 4096;
        int cropBudget = 512;
        int seedMinTicks = 12000;
        int seedMaxTicks = 18000;
        double seedChance = 0.35;
        double territoryMaleChance = 0.20;
        double territoryFemaleChance = 0.02;
        int territoryCooldownTicks = 2400;
        double gardenBonus = 0.15;
    }

    public static int nectarTicks() { return settings.nectarTicks; }
    public static int tamingFeeds() { return settings.tamingFeeds; }
    public static boolean pollinationEnabled() { return settings.pollinationEnabled; }
    public static int pollinationPeriodTicks() { return settings.pollinationPeriodTicks; }
    public static double pollinationRadius() { return settings.pollinationRadius; }
    public static int pollinationGraceTicks() { return settings.pollinationGraceTicks; }
    public static int flowerCooldownMinTicks() { return settings.flowerCooldownMinTicks; }
    public static int flowerCooldownMaxTicks() { return Math.max(settings.flowerCooldownMinTicks, settings.flowerCooldownMaxTicks); }
    public static int flowerScanBudget() { return settings.flowerScanBudget; }
    public static int cropBudget() { return settings.cropBudget; }
    public static int seedMinTicks() { return settings.seedMinTicks; }
    public static int seedMaxTicks() { return Math.max(settings.seedMinTicks, settings.seedMaxTicks); }
    public static double seedChance() { return settings.seedChance; }
    public static double territoryMaleChance() { return settings.territoryMaleChance; }
    public static double territoryFemaleChance() { return settings.territoryFemaleChance; }
    public static int territoryCooldownTicks() { return settings.territoryCooldownTicks; }
    public static double gardenBonus() { return settings.gardenBonus; }
    public static int gardenRadius() { return 16; }
}
