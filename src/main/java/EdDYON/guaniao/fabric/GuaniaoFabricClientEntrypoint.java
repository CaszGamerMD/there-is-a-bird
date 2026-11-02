package EdDYON.guaniao.fabric;

import EdDYON.guaniao.client.camera.CameraTooltipEvents;
import EdDYON.guaniao.client.bath.BirdBathItemRenderer;
import EdDYON.guaniao.client.cage.BirdCageItemRenderer;
import EdDYON.guaniao.client.nest.CrowNestItemRenderer;
import EdDYON.guaniao.content.nest.CrowNestItem;
import EdDYON.guaniao.content.cage.BirdCageItem;
import EdDYON.guaniao.client.camera.NikonD750ItemRenderer;
import EdDYON.guaniao.content.bath.BirdBathItem;
import EdDYON.guaniao.content.camera.NikonD750Item;
import EdDYON.guaniao.registry.GuaniaoItems;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.renderer.GeoItemRenderer;
import java.util.function.Supplier;
import EdDYON.guaniao.client.dropping.PrankFoodClientEvents;
import EdDYON.guaniao.client.fan.FeatherFanClientEvents;
import EdDYON.guaniao.client.hummingbird.HummingbirdSoundEvents;
import EdDYON.guaniao.client.entity.umbrellacockatoo.CockatooSpecialCallSoundEvents;
import EdDYON.guaniao.network.BirdRuntimeConfigPacket;
import EdDYON.guaniao.client.camera.PhotoClientRepository;
import EdDYON.guaniao.client.camera.PhotographTextureCache;
import EdDYON.guaniao.client.camera.CameraKeyMappings;
import EdDYON.guaniao.client.camera.CameraClientCapture;
import EdDYON.guaniao.client.camera.CameraPreviewGuiBridge;
import EdDYON.guaniao.client.camera.CameraWorldFrame26;
import EdDYON.guaniao.GuaniaoMod;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;
import EdDYON.guaniao.config.BirdConfigManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import EdDYON.guaniao.network.PhotoCaptureResultPacket;
import EdDYON.guaniao.network.PhotoDownloadStartPacket;
import EdDYON.guaniao.network.PhotoDownloadChunkPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import EdDYON.guaniao.network.FrameWorkSnapshotPacket;
import EdDYON.guaniao.content.camera.frame.FrameWorkbenchMenu;
import EdDYON.guaniao.registry.GuaniaoMenuTypes;
import EdDYON.guaniao.client.nest.CrowNestScreen;
import EdDYON.guaniao.client.camera.frame.FrameWorkbenchScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import EdDYON.guaniao.client.config.BirdConfigClient;
import EdDYON.guaniao.network.OpenBirdConfigPacket;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.world.item.Item;
import EdDYON.guaniao.client.particle.KillFeatherParticle;
import EdDYON.guaniao.client.particle.BurialWindParticle;
import EdDYON.guaniao.client.particle.BurialCycloneParticle;
import EdDYON.guaniao.client.particle.RivenSplitParticle;
import EdDYON.guaniao.client.particle.RivenStreakParticle;
import EdDYON.guaniao.client.particle.HuntingMarkParticle;
import EdDYON.guaniao.client.particle.HuntingStreakParticle;
import EdDYON.guaniao.client.particle.PlaceableBreakFleckParticle;
import EdDYON.guaniao.registry.GuaniaoParticleTypes;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
import EdDYON.guaniao.client.camera.frame.FrameWorkbenchPreview3DRenderer;

/** Client bootstrap; feature registrations are restored as their 26.2 ports compile. */
public final class GuaniaoFabricClientEntrypoint implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Six GeckoLib bath block-items retain a cached authored model renderer.
        registerBirdBathItemRenderers();
        registerBirdCageItemRenderers();
        registerCrowNestItemRenderer();
        registerNikonCameraRenderer();
        CameraTooltipEvents.registerCallbacks();
        CameraKeyMappings.register();
        // Real world-frame snapshots must happen in the GPU draw phase.
        // The copier remains idle until a genuine filter renderer installs.
        CameraWorldFrame26.register();
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(GuaniaoMod.MOD_ID, "camera_viewfinder"),
                (graphics, deltaTracker) -> CameraClientCapture.renderViewfinder(graphics));
        // The original Forge render-tick callback does not run on Fabric.
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            PhotographTextureCache.pumpUploads();
            PhotographTextureCache.tickHeldPhotographs();
            // Original Forge END tick: held-camera loss and continuous AF-C focus
            // must update before processing viewfinder camera shortcuts.
            CameraClientCapture.tickViewfinder();
            CameraKeyMappings.tick();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            // Restore zoom sensitivity, abort any incomplete shutter upload
            // and drop private texture data before leaving the server.
            CameraClientCapture.disconnect();
            // The next server must not reuse a native GPU lens provider
            // holding on to textures allocated for the prior world.
            CameraPreviewGuiBridge.clear();
            CameraWorldFrame26.reset();
            // Disconnect the JPEG/request/upload repository too: image and
            // in-flight upload IDs must not survive into a different server.
            // The repository generation gate ignores any late async responses.
            PhotoClientRepository.clear();
            PhotographTextureCache.clear();
            BirdConfigManager.loadOrCreateDefaults();
        });
        PrankFoodClientEvents.registerCallbacks();
        FeatherFanClientEvents.registerCallbacks();
        HummingbirdSoundEvents.registerCallbacks();
        CockatooSpecialCallSoundEvents.registerCallbacks();
        GuaniaoFabricEntityRenderers.register();
        PictureInPictureRendererRegistry.register(context -> new FrameWorkbenchPreview3DRenderer());
        ParticleProviderRegistry.getInstance().register(GuaniaoParticleTypes.KILL_FEATHER.get(), KillFeatherParticle.Provider::new);
        ParticleProviderRegistry.getInstance().register(GuaniaoParticleTypes.BURIAL_WIND.get(), BurialWindParticle.Provider::new);
        ParticleProviderRegistry.getInstance().register(GuaniaoParticleTypes.BURIAL_CYCLONE.get(), BurialCycloneParticle.Provider::new);
        ParticleProviderRegistry.getInstance().register(GuaniaoParticleTypes.RIVEN_SPLIT.get(), RivenSplitParticle.Provider::new);
        ParticleProviderRegistry.getInstance().register(GuaniaoParticleTypes.RIVEN_STREAK.get(), RivenStreakParticle.Provider::new);
        ParticleProviderRegistry.getInstance().register(GuaniaoParticleTypes.HUNTING_MARK.get(), HuntingMarkParticle.Provider::new);
        ParticleProviderRegistry.getInstance().register(GuaniaoParticleTypes.HUNTING_STREAK.get(), HuntingStreakParticle.Provider::new);
        ParticleProviderRegistry.getInstance().register(GuaniaoParticleTypes.PLACEABLE_FLECK.get(), PlaceableBreakFleckParticle.Provider::new);
        MenuScreens.register(GuaniaoMenuTypes.CROW_NEST.get(), CrowNestScreen::new);
        MenuScreens.register(GuaniaoMenuTypes.FRAME_WORKBENCH.get(), FrameWorkbenchScreen::new);
        ClientPlayNetworking.registerGlobalReceiver(BirdRuntimeConfigPacket.TYPE,
                (packet, context) -> packet.applyOnClient());
        ClientPlayNetworking.registerGlobalReceiver(PhotoCaptureResultPacket.TYPE,
                (packet, context) -> PhotoClientRepository.captureResult(packet.uploadId(), packet.success()));
        ClientPlayNetworking.registerGlobalReceiver(PhotoDownloadStartPacket.TYPE,
                (packet, context) -> PhotoClientRepository.beginDownload(packet.photoId(), packet.found(), packet.totalBytes(), packet.width(), packet.height(), packet.contentHash()));
        ClientPlayNetworking.registerGlobalReceiver(PhotoDownloadChunkPacket.TYPE,
                (packet, context) -> PhotoClientRepository.acceptDownloadChunk(packet.photoId(), packet.chunkIndex(), packet.data()));
        ClientPlayNetworking.registerGlobalReceiver(FrameWorkSnapshotPacket.TYPE, (packet, context) -> {
            var player = context.client().player;
            if (player != null && player.containerMenu instanceof FrameWorkbenchMenu menu
                    && menu.containerId == packet.containerId()) {
                menu.acceptWorkSnapshot(packet.pos(), packet.preview(), packet.seed(),
                        packet.remaining(), packet.revision());
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(OpenBirdConfigPacket.TYPE,
                (packet, context) -> BirdConfigClient.open(packet.data()));
        // Camera screens, remaining GeckoLib bird renderers and layers
        // still need Fabric client hooks.
    }

    private static void registerBirdBathItemRenderers() {
        registerBirdBathItemRenderer(GuaniaoItems.WOODEN_BIRD_BATH.get());
        registerBirdBathItemRenderer(GuaniaoItems.STONE_BIRD_BATH.get());
        registerBirdBathItemRenderer(GuaniaoItems.BIRD_BATH.get());
        registerBirdBathItemRenderer(GuaniaoItems.WOODEN_BIRD_BATH_2.get());
        registerBirdBathItemRenderer(GuaniaoItems.STONE_BIRD_BATH_2.get());
        registerBirdBathItemRenderer(GuaniaoItems.BIRD_BATH_2.get());
    }

    private static void registerBirdBathItemRenderer(Item item) {
        if (!(item instanceof BirdBathItem bath)) {
            throw new IllegalStateException("Expected the authored bird bath item for " + item);
        }
        bath.geoRenderProvider.setValue(new GeoRenderProvider() {
            private final Supplier<GeoItemRenderer<BirdBathItem>> renderer =
                    com.google.common.base.Suppliers.memoize(() -> new BirdBathItemRenderer(bath));

            @Override
            public GeoItemRenderer<BirdBathItem> getGeoItemRenderer() {
                return this.renderer.get();
            }
        });
    }

    private static void registerBirdCageItemRenderers() {
        registerBirdCageItemRenderer(GuaniaoItems.SMALL_BIRD_CAGE.get());
        registerBirdCageItemRenderer(GuaniaoItems.MEDIUM_BIRD_CAGE.get());
        registerBirdCageItemRenderer(GuaniaoItems.LARGE_BIRD_CAGE.get());
    }

    private static void registerBirdCageItemRenderer(Item item) {
        if (!(item instanceof BirdCageItem cage)) {
            throw new IllegalStateException("Expected the authored bird cage item for " + item);
        }
        cage.geoRenderProvider.setValue(new GeoRenderProvider() {
            private final Supplier<GeoItemRenderer<BirdCageItem>> renderer =
                    com.google.common.base.Suppliers.memoize(() -> new BirdCageItemRenderer(cage));

            @Override
            public GeoItemRenderer<BirdCageItem> getGeoItemRenderer() {
                return this.renderer.get();
            }
        });
    }

    private static void registerCrowNestItemRenderer() {
        Item item = GuaniaoItems.CROW_NEST.get();
        if (!(item instanceof CrowNestItem nest)) {
            throw new IllegalStateException("Expected the authored crow nest item for " + item);
        }
        nest.geoRenderProvider.setValue(new GeoRenderProvider() {
            private final Supplier<GeoItemRenderer<CrowNestItem>> renderer =
                    com.google.common.base.Suppliers.memoize(CrowNestItemRenderer::new);

            @Override
            public GeoItemRenderer<CrowNestItem> getGeoItemRenderer() {
                return this.renderer.get();
            }
        });
    }

    private static void registerNikonCameraRenderer() {
        Item item = GuaniaoItems.NIKON_D750.get();
        if (!(item instanceof NikonD750Item camera)) {
            throw new IllegalStateException("Expected the authored Nikon camera item for " + item);
        }
        camera.geoRenderProvider.setValue(new GeoRenderProvider() {
            private final Supplier<GeoItemRenderer<NikonD750Item>> renderer =
                    com.google.common.base.Suppliers.memoize(NikonD750ItemRenderer::new);

            @Override
            public GeoItemRenderer<NikonD750Item> getGeoItemRenderer() {
                return this.renderer.get();
            }
        });
    }
}
