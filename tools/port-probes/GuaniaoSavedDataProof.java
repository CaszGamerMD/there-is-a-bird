package EdDYON.guaniao.fabric;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import java.util.stream.LongStream;

/** Compile-only API proof; does not replace the original persisted cake state. */
public final class GuaniaoSavedDataProof extends SavedData {
    private static final String DATA_NAME = "guaniao_polluted_cakes";
    private static final Codec<GuaniaoSavedDataProof> CODEC = Codec.LONG_STREAM
            .fieldOf("Positions")
            .xmap(GuaniaoSavedDataProof::fromPositions, GuaniaoSavedDataProof::positionsStream)
            .codec();
    public static final SavedDataType<GuaniaoSavedDataProof> TYPE = new SavedDataType<>(
            Identifier.withDefaultNamespace(DATA_NAME), GuaniaoSavedDataProof::new, CODEC, null);

    private long[] positions = new long[0];

    private static GuaniaoSavedDataProof fromPositions(LongStream data) {
        GuaniaoSavedDataProof decoded = new GuaniaoSavedDataProof();
        decoded.positions = data.toArray();
        return decoded;
    }

    private LongStream positionsStream() {
        return java.util.Arrays.stream(this.positions);
    }
}
