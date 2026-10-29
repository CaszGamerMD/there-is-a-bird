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
import net.fabricmc.api.ClientModInitializer;

/** Client bootstrap; feature registrations are restored as their 26.2 ports compile. */
public final class GuaniaoFabricClientEntrypoint implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CameraTooltipEvents.registerCallbacks();
        PrankFoodClientEvents.registerCallbacks();
        FeatherFanClientEvents.registerCallbacks();
        ClientPlayNetworking.registerGlobalReceiver(BirdRuntimeConfigPacket.TYPE,
                (packet, context) -> packet.applyOnClient());
        ClientPlayNetworking.registerGlobalReceiver(PhotoCaptureResultPacket.TYPE,
                (packet, context) -> PhotoClientRepository.captureResult(packet.uploadId(), packet.success()));
        ClientPlayNetworking.registerGlobalReceiver(PhotoDownloadStartPacket.TYPE,
                (packet, context) -> PhotoClientRepository.beginDownload(packet.photoId(), packet.found(), packet.totalBytes(), packet.width(), packet.height(), packet.contentHash()));
        ClientPlayNetworking.registerGlobalReceiver(PhotoDownloadChunkPacket.TYPE,
                (packet, context) -> PhotoClientRepository.acceptDownloadChunk(packet.photoId(), packet.chunkIndex(), packet.data()));
        // Camera screens, all GeckoLib bird renderers, layers and sound loops
        // must also be registered before the private build can run.
    }
}
