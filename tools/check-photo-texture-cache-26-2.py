#!/usr/bin/env python3
"""Source regression gate for real 26.2 photograph textures and frame previews."""
from pathlib import Path
ROOT = Path(__file__).resolve().parent.parent
cache = (ROOT / "src/main/java/EdDYON/guaniao/client/camera/PhotographTextureCache.java").read_text()
screen = (ROOT / "src/main/java/EdDYON/guaniao/client/camera/frame/FrameWorkbenchScreen.java").read_text()
client = (ROOT / "src/main/java/EdDYON/guaniao/fabric/GuaniaoFabricClientEntrypoint.java").read_text()
factory = (ROOT / "src/main/java/EdDYON/guaniao/client/camera/frame/FrameWorkbenchPreview3DFactory.java").read_text()
submitter = (ROOT / "src/main/java/EdDYON/guaniao/client/camera/frame/FrameWorkbenchPhotoSubmitter.java").read_text()

def includes(source, *terms):
    for term in terms:
        assert term in source, f"Photo migration contract missing: {term}"
includes(cache, "record ReadyImage(Identifier texture, int width, int height)",
         "PhotoClientRepository.getOrRequest(photoId, contentHash)",
         'String key = photoId + ":" + contentHash',
         "new DynamicTexture(", "decoded.image.getWidth()", "decoded.image.getHeight()",
         "MAX_UPLOADS_PER_CLIENT_TICK = 2", "MAX_TEXTURES = 256",
         "MAX_RESIDENT_PIXEL_BYTES", "residentPixelBytes -= oldest.pixelBytes()",
         "residentPixelBytes -= entry.getValue().pixelBytes()", "decoded.generation != generation",
         "PhotoClientRepository.clear()")
for removed in ("org.lwjgl.opengl", "ResourceLocation", "TextureUtil.prepareImage", "RenderSystem.bindTexture"):
    assert removed not in cache, f"Retired direct-GL/cache API: {removed}"
includes(screen, "PhotographTextureCache.readyImageFor(this.previewStack)",
         "if(hasPhoto && readyPhoto==null)return",
         "FrameWorkbenchPreview3DFactory.captureWithPhoto(",
         "readyPhoto.texture(),readyPhoto.width(),readyPhoto.height()")
includes(factory, "FrameWorkbenchPhotoGeometry.build(", "imageWidth,imageHeight",
         "design.offsetX(),design.offsetY()")
includes(submitter, "collector.submitCustomGeometry(", "RenderTypes.text(texture)")
includes(client, "ClientTickEvents.END_CLIENT_TICK.register(",
         "PhotographTextureCache.pumpUploads()", "PhotographTextureCache.tickHeldPhotographs()",
         "ClientPlayConnectionEvents.DISCONNECT", "PhotographTextureCache.clear()")
print("PASS: 26.2 original JPEG cache, dimensions, PiP clipping and client lifecycle")
