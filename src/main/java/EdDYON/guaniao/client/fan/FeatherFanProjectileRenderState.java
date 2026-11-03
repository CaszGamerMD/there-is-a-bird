package EdDYON.guaniao.client.fan;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

/** Only extracted, render-thread-safe data: no live entity references. */
public final class FeatherFanProjectileRenderState extends EntityRenderState {
    public final ItemStackRenderState mainFan = new ItemStackRenderState();
    public final ItemStackRenderState[] rivenFans = new ItemStackRenderState[8];
    public int ticks;
    public float partialTick;
    public float yaw;
    public float pitch;
    public float charge;
    public int rivenTicks;
    public boolean piercing;
    public boolean stuck;
    public boolean hunting;
    public boolean returning;
    public boolean rivenActive;

    public FeatherFanProjectileRenderState() {
        for (int i = 0; i < rivenFans.length; i++) {
            rivenFans[i] = new ItemStackRenderState();
        }
    }
}
