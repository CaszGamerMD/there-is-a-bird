package EdDYON.guaniao.event;

/**
 * The original Forge mod-event subscriber has been fully migrated to Fabric:
 *
 * - setup / bird dropping compost chances:
 *   EdDYON.guaniao.content.dropping.BirdDroppingUtil
 * - all nineteen bird attributes:
 *   EdDYON.guaniao.fabric.GuaniaoEntityAttributes
 * - all nineteen original spawn placements:
 *   EdDYON.guaniao.registry.GuaniaoEntityTypes
 * - vanilla creative Ingredients and Spawn Eggs entries:
 *   EdDYON.guaniao.fabric.GuaniaoVanillaCreativeTabs
 *
 * The original event declarations remain in the 1.20.1 main branch as the
 * regression baseline. No Forge callback is intentionally invoked on Fabric.
 */
@Deprecated(forRemoval = true)
public final class GuaniaoModEvents {
    private GuaniaoModEvents() { }
}
