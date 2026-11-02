package EdDYON.guaniao.fabric;

import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.Villager;
import org.jetbrains.annotations.Nullable;

/** Real Minecraft 26.2 compile probe, excluded from the distributable source set. */
public final class GuaniaoRelocatedTypesProof {
    private GuaniaoRelocatedTypesProof() { }

    public static Class<?>[] movedTypes() {
        return new Class<?>[] {
                Villager.class, IronGolem.class, AbstractFish.class, EntitySpawnReason.class,
                Cat.class, Fox.class, Wolf.class, AbstractSkeleton.class,
                Spider.class, Zombie.class, EnderMan.class
        };
    }

    @Nullable
    public static AbstractFish nullableFish(@Nullable AbstractFish fish) {
        return fish;
    }
}
