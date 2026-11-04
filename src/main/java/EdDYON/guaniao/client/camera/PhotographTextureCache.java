package EdDYON.guaniao.client.camera;

import EdDYON.guaniao.GuaniaoMod;
import EdDYON.guaniao.content.camera.PhotographData;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/** Asynchronous JPEG decoding with a small per-render-tick GPU upload budget. */
public final class PhotographTextureCache {
    // 26.2 DynamicTexture owns the decoded image while its GPU copy is live.
    // Bound both count and resident pixel memory after long photo sessions.
    private static final int MAX_TEXTURES = 256;
    private static final long MAX_RESIDENT_PIXEL_BYTES = 512L * 1024 * 1024;
    private static long residentPixelBytes;
    private static final int MAX_PENDING_DECODES = 8;
    private static final int MAX_UPLOADS_PER_CLIENT_TICK = 2;
    private static final long UNUSED_TEXTURE_MILLIS = 60_000L;
    private static final Identifier FALLBACK = Identifier.fromNamespaceAndPath(GuaniaoMod.MOD_ID, "textures/item/photograph.png");
    private static final Map<String, CachedTexture> TEXTURES = new LinkedHashMap<>(MAX_TEXTURES + 1, 0.75F, true);
    private static final Set<String> DECODING = new HashSet<>();
    /** Keys whose jpeg could not be decoded; retried only after a logout clears it. */
    private static final Set<String> FAILED = new HashSet<>();
    private static final ArrayDeque<DecodedImage> READY = new ArrayDeque<>();
    private static final ArrayDeque<Identifier> AVAILABLE_SLOTS = new ArrayDeque<>();
    private static final ThreadPoolExecutor DECODER = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(MAX_PENDING_DECODES), runnable -> {
        Thread thread = new Thread(runnable, "Guaniao-Photo-Texture-Decode");
        thread.setDaemon(true);
        return thread;
    });
    private static volatile int generation;

    static {
        resetSlots();
    }

    private PhotographTextureCache() {
    }

    public record ReadyImage(Identifier texture, int width, int height) { }

    public static Identifier textureFor(ItemStack stack) {
        Identifier texture = readyTextureFor(stack);
        return texture == null ? FALLBACK : texture;
    }

    /** Requests the original photograph; never substitutes an unrelated icon. */
    @Nullable
    public static Identifier readyTextureFor(ItemStack stack) {
        ReadyImage ready = readyImageFor(stack);
        return ready == null ? null : ready.texture();
    }

    /** The original decoded JPEG dimensions for exact photo-window cropping. */
    @Nullable
    public static ReadyImage readyImageFor(ItemStack stack) {
        if (!PhotographData.hasImage(stack)) return null;
        String photoId = PhotographData.id(stack);
        String contentHash = PhotographData.contentHash(stack);
        String key = photoId + ":" + contentHash;
        CachedTexture cached = TEXTURES.get(key);
        if (cached != null) {
            cached.lastUsedMillis = System.currentTimeMillis();
            return new ReadyImage(cached.location, cached.width, cached.height);
        }
        byte[] jpeg = PhotoClientRepository.getOrRequest(photoId, contentHash);
        if (jpeg != null) imageAvailable(photoId, contentHash, jpeg);
        return null;
    }

    public static void preload(ItemStack stack) {
        readyTextureFor(stack);
    }

    public static void tickHeldPhotographs() {
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        // Include exposed film, whose held/inventory renderer uses a fixed icon.
        // Never download the entire inventory or world just to open one photo.
        preload(player.getMainHandItem());
        preload(player.getOffhandItem());
    }

    static void imageAvailable(String photoId, String contentHash, byte[] jpeg) {
        String key = photoId + ":" + contentHash;
        if (!TEXTURES.containsKey(key) && DECODING.size() < MAX_PENDING_DECODES
                && !FAILED.contains(key) && DECODING.add(key)) {
            int decodeGeneration = generation;
            try {
                DECODER.execute(() -> decode(key, jpeg, decodeGeneration));
            } catch (java.util.concurrent.RejectedExecutionException exception) {
                // A competing decode can fill the executor queue after the check.
                DECODING.remove(key);
            }
        }
    }

    public static void pumpUploads() {
        Minecraft minecraft = Minecraft.getInstance();
        for (int uploaded = 0; uploaded < MAX_UPLOADS_PER_CLIENT_TICK; uploaded++) {
            DecodedImage decoded;
            synchronized (READY) {
                decoded = READY.pollFirst();
            }
            if (decoded == null) break;
            if (decoded.generation != generation) {
                decoded.image.close();
                continue;
            }
            DECODING.remove(decoded.key);
            if (TEXTURES.containsKey(decoded.key)) {
                decoded.image.close();
                continue;
            }
            Identifier location = reserveSlot();
            DynamicTexture texture = null;
            try {
                int width = decoded.image.getWidth(), height = decoded.image.getHeight();
                // Backend-independent 26.2 texture upload (OpenGL or Vulkan).
                // DynamicTexture takes ownership of these NativeImage pixels.
                texture = new DynamicTexture(() -> "Guaniao photograph " + decoded.key, decoded.image);
                minecraft.getTextureManager().register(location, texture);
                TEXTURES.put(decoded.key, new CachedTexture(location,
                        System.currentTimeMillis(), width, height));
                residentPixelBytes += (long) width * height * 4L;
            } catch (RuntimeException exception) {
                if (texture != null) texture.close();
                else decoded.image.close();
                AVAILABLE_SLOTS.addLast(location);
                FAILED.add(decoded.key);
                GuaniaoMod.LOGGER.warn("Could not upload photograph texture {}", decoded.key, exception);
            }
        }
        evictTextures();
    }

    public static void clear() {
        generation++;
        DECODER.getQueue().clear();
        Minecraft minecraft = Minecraft.getInstance();
        for (CachedTexture texture : TEXTURES.values()) {
            minecraft.getTextureManager().release(texture.location);
        }
        TEXTURES.clear();
        residentPixelBytes = 0L;
        resetSlots();
        DECODING.clear();
        FAILED.clear();
        synchronized (READY) {
            while (!READY.isEmpty()) {
                READY.removeFirst().image.close();
            }
        }
        PhotoClientRepository.clear();
    }

    public static Path export(ItemStack stack) throws IOException {
        if (!PhotographData.hasImage(stack)) {
            throw new IOException("Photograph has no pixel data.");
        }
        String photoId = PhotographData.id(stack);
        String contentHash = PhotographData.contentHash(stack);
        byte[] jpeg = PhotoClientRepository.getOrRequest(photoId, contentHash);
        if (jpeg == null) {
            throw new IOException("Photograph is still downloading.");
        }
        Path directory = Minecraft.getInstance().gameDirectory.toPath().resolve("guaniao_photos");
        Files.createDirectories(directory);
        Path file = directory.resolve(safe(photoId) + ".png");
        try (NativeImage image = NativeImage.read(new ByteArrayInputStream(jpeg))) {
            image.writeToFile(file);
        }
        return file;
    }

    private static void decode(String key, byte[] jpeg, int decodeGeneration) {
        if (decodeGeneration != generation) return;
        try {
            NativeImage image = NativeImage.read(new ByteArrayInputStream(jpeg));
            synchronized (READY) {
                if (decodeGeneration == generation) READY.addLast(new DecodedImage(key, image, decodeGeneration));
                else image.close();
            }
        } catch (IOException | RuntimeException exception) {
            // Mark the key failed so a broken photo cannot resubmit itself to the
            // decoder every frame.
            Minecraft.getInstance().execute(() -> finishDecodeFailure(key, decodeGeneration));
        }
    }

    private static void finishDecodeFailure(String key, int decodeGeneration) {
        if (decodeGeneration != generation) {
            return;
        }
        DECODING.remove(key);
        FAILED.add(key);
    }

    private static void evictTextures() {
        Minecraft minecraft = Minecraft.getInstance();
        long cutoff = System.currentTimeMillis() - UNUSED_TEXTURE_MILLIS;
        Iterator<Map.Entry<String, CachedTexture>> iterator = TEXTURES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, CachedTexture> entry = iterator.next();
            if (entry.getValue().lastUsedMillis >= cutoff
                    && residentPixelBytes <= MAX_RESIDENT_PIXEL_BYTES) {
                continue;
            }
            residentPixelBytes -= entry.getValue().pixelBytes();
            iterator.remove();
            minecraft.getTextureManager().release(entry.getValue().location);
            AVAILABLE_SLOTS.addLast(entry.getValue().location);
        }
    }

    private static Identifier reserveSlot() {
        if (!AVAILABLE_SLOTS.isEmpty()) return AVAILABLE_SLOTS.removeFirst();
        Iterator<CachedTexture> iterator = TEXTURES.values().iterator();
        CachedTexture oldest = iterator.next();
        iterator.remove();
        residentPixelBytes -= oldest.pixelBytes();
        Minecraft.getInstance().getTextureManager().release(oldest.location);
        return oldest.location;
    }

    private static void resetSlots() {
        AVAILABLE_SLOTS.clear();
        for (int slot = 0; slot < MAX_TEXTURES; slot++) {
            AVAILABLE_SLOTS.addLast(Identifier.fromNamespaceAndPath(GuaniaoMod.MOD_ID, "dynamic/guaniao_photo/" + slot));
        }
    }

    private static String safe(String id) {
        String safe = id.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]", "_");
        return safe.isEmpty() ? "photograph" : safe;
    }

    private static final class CachedTexture {
        private final Identifier location;
        private final int width, height;
        private long lastUsedMillis;

        private CachedTexture(Identifier location, long lastUsedMillis, int width, int height) {
            this.location = location;
            this.lastUsedMillis = lastUsedMillis;
            this.width = width;
            this.height = height;
        }

        private long pixelBytes() {
            return (long) width * height * 4L;
        }
    }

    private record DecodedImage(String key, NativeImage image, int generation) {
    }
}
