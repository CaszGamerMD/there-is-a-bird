package EdDYON.guaniao.fabric;

import EdDYON.guaniao.client.camera.CameraTooltipEvents;
import EdDYON.guaniao.client.dropping.PrankFoodClientEvents;
import EdDYON.guaniao.client.fan.FeatherFanClientEvents;
import EdDYON.guaniao.network.BirdRuntimeConfigPacket;
import EdDYON.guaniao.client.camera.PhotoClientRepository;
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

/** Client bootstrap; feature registrations are restored as their 26.2 ports compile. */
public final class GuaniaoFabricClientEntrypoint implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CameraTooltipEvents.registerCallbacks();
        PrankFoodClientEvents.registerCallbacks();
        FeatherFanClientEvents.registerCallbacks();
        GuaniaoFabricEntityRenderers.register();
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
        // Camera screens, all GeckoLib bird renderers, layers and sound loops
        // must also be registered before the private build can run.
    }
}
