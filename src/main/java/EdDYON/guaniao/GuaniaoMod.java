package EdDYON.guaniao;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

/**
 * Shared mod identity, kept for source compatibility with the original mod.
 * Fabric loads EdDYON.guaniao.fabric.GuaniaoFabricEntrypoint, not this class.
 *
 * Do not put loader-specific initialization here: older features still refer
 * to GuaniaoMod.MOD_ID and LOGGER throughout the codebase.
 */
public final class GuaniaoMod {
    public static final String MOD_ID = "guaniao";
    public static final Logger LOGGER = LogUtils.getLogger();

    private GuaniaoMod() { }
}
