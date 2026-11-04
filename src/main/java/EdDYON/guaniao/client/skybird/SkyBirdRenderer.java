package EdDYON.guaniao.client.skybird;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Client sky ecology renderer for Minecraft 26.2.
 *
 * All mutable flock data and interpolated positions are captured during world
 * extraction. The later render callback consumes immutable camera-relative
 * vertices and submits deferred geometry; it never reads the live ClientLevel
 * or advances the flock simulation while drawing.
 */
public final class SkyBirdRenderer {
    private static final Vec3 WORLD_UP = new Vec3(0.0D, 1.0D, 0.0D);
    private static volatile Snapshot pending = Snapshot.EMPTY;

    private SkyBirdRenderer() {
    }

    static void extract(LevelExtractionContext context, SkyBirdManager manager) {
        if (!manager.textureReady() || manager.flocks().isEmpty()) {
            pending = Snapshot.EMPTY;
            return;
        }

        Vec3 cameraPosition = context.camera().position();
        float partialTick = context.deltaTracker().getGameTimeDeltaPartialTick(false);
        Minecraft minecraft = Minecraft.getInstance();
        double farStart = manager.farFadeStart(minecraft);
        double farEnd = manager.farFadeEnd(minecraft);
        List<SpeciesBatch> batches = new ArrayList<>();

        for (SkyBirdSpecies species : SkyBirdSpecies.values()) {
            if (!manager.textureReady(species)) {
                continue;
            }

            List<Vertex> vertices = new ArrayList<>();
            int renderedForSpecies = 0;
            for (SkyFlock flock : manager.flocks()) {
                if (flock.species() != species) {
                    continue;
                }

                for (SkyBird bird : flock.birds()) {
                    Vec3 forward = flock.birdRenderVelocity(bird, partialTick).normalize();
                    if (forward.lengthSqr() < 1.0E-6D) {
                        continue;
                    }
                    Vec3 right = WORLD_UP.cross(forward).normalize();
                    if (right.lengthSqr() < 1.0E-6D) {
                        continue;
                    }
                    Vec3 up = forward.cross(right).normalize();
                    Vec3 birdPosition = flock.birdPosition(bird, partialTick);
                    double distance = cameraPosition.distanceTo(birdPosition);
                    float alpha = distanceAlpha(distance, farStart, farEnd)
                            * viewAlpha(cameraPosition, birdPosition)
                            * flock.lifecycleAlpha(partialTick);
                    if (alpha <= 0.0F) {
                        continue;
                    }

                    appendBird(vertices, cameraPosition, birdPosition, forward, right, up,
                            animationFrame(flock, bird, partialTick),
                            flock.renderRoll(bird, partialTick), alpha, bird.size(), species);
                    renderedForSpecies++;
                }
            }
            if (!vertices.isEmpty()) {
                batches.add(new SpeciesBatch(species.texture(), List.copyOf(vertices)));
                manager.recordRenderedSpecies(species, renderedForSpecies);
            }
        }
        pending = batches.isEmpty() ? Snapshot.EMPTY : new Snapshot(List.copyOf(batches));
    }

    static void submit(LevelRenderContext context) {
        Snapshot snapshot = pending;
        if (snapshot.batches().isEmpty()) {
            return;
        }
        SubmitNodeCollector collector = context.submitNodeCollector();
        for (SpeciesBatch batch : snapshot.batches()) {
            RenderType renderType = SkyBirdRenderTypes.translucent(batch.texture());
            collector.submitCustomGeometry(context.poseStack(), renderType, (pose, output) -> {
                for (Vertex vertex : batch.vertices()) {
                    output.addVertex(pose, vertex.x(), vertex.y(), vertex.z())
                            .setColor(255, 255, 255, vertex.alpha())
                            .setUv(vertex.u(), vertex.v())
                            .setOverlay(0)
                            .setLight(LightCoordsUtil.FULL_BRIGHT)
                            .setNormal(pose, vertex.nx(), vertex.ny(), vertex.nz());
                }
            });
        }
    }

    static void clear() {
        pending = Snapshot.EMPTY;
    }

    private static float distanceAlpha(double distance, double farStart, double farEnd) {
        float nearAlpha;
        if (distance <= SkyBirdManager.NEAR_FADE_START) {
            nearAlpha = 0.0F;
        } else if (distance < SkyBirdManager.NEAR_FADE_END) {
            nearAlpha = (float) ((distance - SkyBirdManager.NEAR_FADE_START)
                    / (SkyBirdManager.NEAR_FADE_END - SkyBirdManager.NEAR_FADE_START));
        } else {
            nearAlpha = 1.0F;
        }

        float farAlpha;
        if (distance <= farStart) {
            farAlpha = 1.0F;
        } else if (distance >= farEnd) {
            farAlpha = 0.0F;
        } else {
            farAlpha = (float) ((farEnd - distance) / (farEnd - farStart));
        }
        return nearAlpha * farAlpha;
    }

    /**
     * These flat atmosphere sprites are meant to be observed from below. They
     * fade only at render time when the camera reaches their altitude or sees
     * the flight plane at a very shallow angle; natural ecology keeps ticking.
     */
    private static float viewAlpha(Vec3 cameraPosition, Vec3 birdPosition) {
        double heightAboveCamera = birdPosition.y - cameraPosition.y;
        float altitudeAlpha;
        if (heightAboveCamera <= 4.0D) {
            altitudeAlpha = 0.0F;
        } else if (heightAboveCamera < 18.0D) {
            altitudeAlpha = (float) ((heightAboveCamera - 4.0D) / 14.0D);
        } else {
            altitudeAlpha = 1.0F;
        }

        Vec3 toCamera = cameraPosition.subtract(birdPosition);
        if (toCamera.lengthSqr() < 1.0E-6D) {
            return 0.0F;
        }
        double underside = -toCamera.normalize().y;
        float angleAlpha;
        if (underside <= 0.15D) {
            angleAlpha = 0.0F;
        } else if (underside < 0.30D) {
            angleAlpha = (float) ((underside - 0.15D) / 0.15D);
        } else {
            angleAlpha = 1.0F;
        }
        return altitudeAlpha * angleAlpha;
    }

    private static void appendBird(List<Vertex> out, Vec3 cameraPosition, Vec3 birdPosition,
                                   Vec3 forward, Vec3 right, Vec3 up, int frame,
                                   float rollDegrees, float alpha, float individualSize,
                                   SkyBirdSpecies species) {
        double roll = Math.toRadians(rollDegrees);
        Vec3 bankedRight = right.scale(Math.cos(roll)).add(up.scale(Math.sin(roll))).normalize();
        // Keep both windings so the atmosphere sprite remains visible from above and below.
        Vec3 surfaceNormal = forward.cross(bankedRight).scale(-1.0D).normalize();
        float halfSize = species.renderSize() * individualSize * 0.5F;

        Vec3 topLeft = birdPosition.add(forward.scale(halfSize)).subtract(bankedRight.scale(halfSize));
        Vec3 topRight = birdPosition.add(forward.scale(halfSize)).add(bankedRight.scale(halfSize));
        Vec3 bottomRight = birdPosition.subtract(forward.scale(halfSize)).add(bankedRight.scale(halfSize));
        Vec3 bottomLeft = birdPosition.subtract(forward.scale(halfSize)).subtract(bankedRight.scale(halfSize));

        // Sample texel centres instead of atlas borders to avoid neighbouring frames bleeding in.
        float atlasWidth = species.textureWidthPixels();
        float u0 = (frame * SkyBirdSpecies.FRAME_PIXELS + 0.5F) / atlasWidth;
        float u1 = ((frame + 1) * SkyBirdSpecies.FRAME_PIXELS - 0.5F) / atlasWidth;
        float v0 = 0.5F / SkyBirdSpecies.FRAME_PIXELS;
        float v1 = (SkyBirdSpecies.FRAME_PIXELS - 0.5F) / SkyBirdSpecies.FRAME_PIXELS;
        int alphaByte = Mth.clamp((int) (alpha * 255.0F), 0, 255);

        add(out, topLeft, cameraPosition, u0, v0, alphaByte, surfaceNormal);
        add(out, bottomLeft, cameraPosition, u0, v1, alphaByte, surfaceNormal);
        add(out, bottomRight, cameraPosition, u1, v1, alphaByte, surfaceNormal);
        add(out, topRight, cameraPosition, u1, v0, alphaByte, surfaceNormal);

        Vec3 reverseNormal = surfaceNormal.scale(-1.0D);
        add(out, topRight, cameraPosition, u1, v0, alphaByte, reverseNormal);
        add(out, bottomRight, cameraPosition, u1, v1, alphaByte, reverseNormal);
        add(out, bottomLeft, cameraPosition, u0, v1, alphaByte, reverseNormal);
        add(out, topLeft, cameraPosition, u0, v0, alphaByte, reverseNormal);
    }

    private static void add(List<Vertex> out, Vec3 position, Vec3 cameraPosition,
                            float u, float v, int alpha, Vec3 normal) {
        Vec3 relative = position.subtract(cameraPosition);
        out.add(new Vertex((float) relative.x, (float) relative.y, (float) relative.z,
                u, v, alpha, (float) normal.x, (float) normal.y, (float) normal.z));
    }

    private static int animationFrame(SkyFlock flock, SkyBird bird, float partialTick) {
        SkyBirdSpecies species = flock.species();
        if (species.animationStyle() == SkyBirdSpecies.AnimationStyle.COORDINATED_SLOW_FLAP) {
            float phaseTicks = bird.wingPhase()
                    * species.frameCount() * species.frameInterval() * 0.35F;
            float animationTick = flock.age() + partialTick + phaseTicks;
            return Mth.floor(animationTick / species.frameInterval()) % species.frameCount();
        }
        if (species.animationStyle() == SkyBirdSpecies.AnimationStyle.BURST_FLAP_WITH_SHORT_GLIDE) {
            int cycle = species.flapCycle(flock.seed(), bird.index());
            if (cycle <= 0) {
                return 0;
            }
            int phaseOffset = Mth.floor(bird.wingPhase() * cycle);
            int cycleTick = Math.floorMod(flock.age() + phaseOffset, cycle);
            int flapDuration = species.frameCount() * species.frameInterval() * 3;
            if (cycleTick >= flapDuration) {
                return 0;
            }
            return (cycleTick / species.frameInterval()) % species.frameCount();
        }
        if (species.animationStyle() == SkyBirdSpecies.AnimationStyle.GLIDE_WITH_OCCASIONAL_FLAP) {
            int cycle = species.flapCycle(flock.seed(), bird.index());
            if (cycle <= 0) {
                return 0;
            }
            int phaseOffset = Mth.floor(bird.wingPhase() * cycle);
            int cycleTick = Math.floorMod(flock.age() + phaseOffset, cycle);
            int flapDuration = species.frameCount() * species.frameInterval();
            if (cycleTick >= flapDuration) {
                return 0;
            }
            return Math.min(cycleTick / species.frameInterval(), species.frameCount() - 1);
        }

        float phaseTicks = bird.wingPhase() * species.frameCount() * species.frameInterval();
        float animationTick = flock.age() + partialTick + phaseTicks;
        return Mth.floor(animationTick / species.frameInterval()) % species.frameCount();
    }

    private record Vertex(float x, float y, float z, float u, float v, int alpha,
                          float nx, float ny, float nz) {
    }

    private record SpeciesBatch(net.minecraft.resources.Identifier texture, List<Vertex> vertices) {
        private SpeciesBatch {
            vertices = List.copyOf(vertices);
        }
    }

    private record Snapshot(List<SpeciesBatch> batches) {
        private static final Snapshot EMPTY = new Snapshot(List.of());
    }
}
