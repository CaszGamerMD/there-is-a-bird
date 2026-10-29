package EdDYON.guaniao.fabric;

import EdDYON.guaniao.network.FeatherFanPiercePacket;
import EdDYON.guaniao.network.BirdRuntimeConfigPacket;
import EdDYON.guaniao.network.PhotoDownloadChunkPacket;
import EdDYON.guaniao.network.PhotoDownloadStartPacket;
import EdDYON.guaniao.network.PhotoCaptureResultPacket;
import EdDYON.guaniao.network.PhotoRequestPacket;
import EdDYON.guaniao.network.FinishPhotoUploadPacket;
import EdDYON.guaniao.network.PhotoUploadChunkPacket;
import EdDYON.guaniao.network.BeginPhotoUploadPacket;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Incremental native 26.2 networking registration.
 * The remaining camera settings, config-save and frame-crafting channels are not yet migrated.
 */
public final class GuaniaoFabricNetworking {
    private GuaniaoFabricNetworking() { }

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(
                FeatherFanPiercePacket.TYPE, FeatherFanPiercePacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(
                BirdRuntimeConfigPacket.TYPE, BirdRuntimeConfigPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PhotoCaptureResultPacket.TYPE, PhotoCaptureResultPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PhotoDownloadStartPacket.TYPE, PhotoDownloadStartPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PhotoDownloadChunkPacket.TYPE, PhotoDownloadChunkPacket.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(
                BeginPhotoUploadPacket.TYPE, BeginPhotoUploadPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(
                BeginPhotoUploadPacket.TYPE, (packet, context) -> packet.handle(context.player()));
        PayloadTypeRegistry.serverboundPlay().register(
                PhotoUploadChunkPacket.TYPE, PhotoUploadChunkPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(
                PhotoUploadChunkPacket.TYPE, (packet, context) -> packet.handle(context.player()));
        PayloadTypeRegistry.serverboundPlay().register(
                FinishPhotoUploadPacket.TYPE, FinishPhotoUploadPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(
                FinishPhotoUploadPacket.TYPE, (packet, context) -> packet.handle(context.player()));
        PayloadTypeRegistry.serverboundPlay().register(
                PhotoRequestPacket.TYPE, PhotoRequestPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(
                PhotoRequestPacket.TYPE, (packet, context) -> packet.handle(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(
                FeatherFanPiercePacket.TYPE, (packet, context) -> packet.handle(context.player()));
    }
}
