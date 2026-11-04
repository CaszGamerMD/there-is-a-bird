package EdDYON.guaniao.content.guide;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class BirdGuideItem extends Item {
    public BirdGuideItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            // Keep the client-only screen class off the dedicated server's classpath.
            try {
                Class.forName("EdDYON.guaniao.client.guide.BirdGuideClient").getMethod("open").invoke(null);
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Failed to open bird guide screen", exception);
            }
        }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }
}
