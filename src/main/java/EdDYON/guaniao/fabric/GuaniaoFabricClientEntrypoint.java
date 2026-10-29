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
import EdDYON.guaniao.client.config.BirdConfigClient;
import EdDYON.guaniao.network.OpenBirdConfigPacket;
import net.fabricmc.api.ClientModInitializer;

/** Client bootstrap; feature registrations are restored as their 26.2 ports compile. */
public final class GuaniaoFabricClientEntrypoint implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CameraTooltipEvents.registerCallbacks();
        PrankFoodClientEvents.registerCallbacks();
        FeatherFanClientEvents.registerCallbacks();
        GuaniaoFabricEntityRenderers.register();
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
