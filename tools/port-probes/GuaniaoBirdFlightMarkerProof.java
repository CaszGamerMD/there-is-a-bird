package EdDYON.guaniao.fabric;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;

/** 26.2 API proof: removed FlyingAnimal -> protected Entity air-mover override.
 * Bird classes retain their original isFlying methods for animation decisions.
 */
public abstract class GuaniaoBirdFlightMarkerProof extends Animal {
    protected GuaniaoBirdFlightMarkerProof(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    @Override
    protected boolean omnidirectionalAirMover() {
        return true;
    }

    public boolean isFlying() {
        return !this.onGround();
    }
}
