package EdDYON.guaniao.mixin;

import EdDYON.guaniao.content.enchantment.GuaniaoEnchantments;
import EdDYON.guaniao.event.BurialPlumeAnvilEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds the three original fan + special-book anvil recipes after vanilla processes its inputs. */
@Mixin(AnvilMenu.class)
public abstract class BurialPlumeAnvilMixin extends ItemCombinerMenu {
    @Shadow @Final private DataSlot cost;
    @Shadow private int repairItemCountCost;
    @Shadow private boolean onlyRenaming;

    protected BurialPlumeAnvilMixin(MenuType<?> type, int id, Inventory inventory,
                                    ContainerLevelAccess access, ItemCombinerMenuSlotDefinition slots) {
        super(type, id, inventory, access, slots);
    }

    // Vanilla returns early for our plain custom books, so hook every RETURN, not just TAIL.
    @Inject(method = "createResult", at = @At("RETURN"), require = 1)
    private void guaniao$applyPlumeBook(CallbackInfo ci) {
        ItemStack fan = this.inputSlots.getItem(0);
        ItemStack book = this.inputSlots.getItem(1);
        if (!GuaniaoEnchantments.isWindFeatherFan(fan)) {
            return;
        }
        ResourceKey<Enchantment> plume = GuaniaoEnchantments.forBook(book);
        if (plume == null || GuaniaoEnchantments.hasAnyPlume(fan)) {
            return;
        }

        ItemStack output = fan.copy();
        GuaniaoEnchantments.apply(output, plume, this.player.level().registryAccess());
        this.repairItemCountCost = 1;
        this.onlyRenaming = false;
        this.cost.set(BurialPlumeAnvilEvents.ANVIL_LEVEL_COST);
        this.resultSlots.setItem(0, output);
        this.broadcastChanges();
    }
}
