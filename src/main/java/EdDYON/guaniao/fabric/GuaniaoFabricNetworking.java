package EdDYON.guaniao.fabric;

import EdDYON.guaniao.network.FeatherFanPiercePacket;
import EdDYON.guaniao.network.BirdRuntimeConfigPacket;
import EdDYON.guaniao.network.SetCameraFilterPacket;
import EdDYON.guaniao.network.SetCameraSettingsPacket;
import EdDYON.guaniao.network.CraftFramePacket;
import EdDYON.guaniao.network.FrameWorkSnapshotPacket;
import EdDYON.guaniao.network.OpenBirdConfigPacket;
import EdDYON.guaniao.network.SaveBirdConfigPacket;
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
 * All original play packet types are registered here; their gameplay and client
 * screens still require full compile and in-world verification.
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
        PayloadTypeRegistry.serverboundPlay().register(
                SetCameraFilterPacket.TYPE, SetCameraFilterPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(
                SetCameraFilterPacket.TYPE, (packet, context) -> packet.handle(context.player()));
        PayloadTypeRegistry.serverboundPlay().register(
                SetCameraSettingsPacket.TYPE, SetCameraSettingsPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(
                SetCameraSettingsPacket.TYPE, (packet, context) -> packet.handle(context.player()));
        PayloadTypeRegistry.serverboundPlay().register(CraftFramePacket.TYPE, CraftFramePacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(
                CraftFramePacket.TYPE, (packet, context) -> packet.handle(context.player()));
        PayloadTypeRegistry.clientboundPlay().register(
                FrameWorkSnapshotPacket.TYPE, FrameWorkSnapshotPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(OpenBirdConfigPacket.TYPE, OpenBirdConfigPacket.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SaveBirdConfigPacket.TYPE, SaveBirdConfigPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(
                SaveBirdConfigPacket.TYPE, (packet, context) -> packet.handle(context.player()));
    }
}
