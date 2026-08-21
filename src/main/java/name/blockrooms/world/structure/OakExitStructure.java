package name.blockrooms.world.structure;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;


public class OakExitStructure extends Structure {
    public static final MapCodec<OakExitStructure> CODEC = simpleCodec(OakExitStructure::new);

    /** 每个区块被选中生成一个出口的概率为 1 / OAK_EXIT_SPACING。 */
    private static final long OAK_EXIT_SPACING = 16;

    private static final int MIN_OFFSET = 2;
    private static final int MAX_OFFSET = 13;

    public OakExitStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        long hash = scatterHash(context.seed(), chunkPos.x, chunkPos.z);
        if (hash % OAK_EXIT_SPACING != 0) {
            return Optional.empty();
        }
        int dx = MIN_OFFSET + (int) ((hash >>> 8) % (MAX_OFFSET - MIN_OFFSET + 1));
        int dz = MIN_OFFSET + (int) ((hash >>> 16) % (MAX_OFFSET - MIN_OFFSET + 1));
        // 随机旋转：与放置同源的稳定哈希推导，保证跨区块/重载后朝向一致
        Rotation rotation = Rotation.values()[(int) ((hash >>> 24) % 4)];
        BlockPos anchor = new BlockPos(chunkPos.getMinBlockX() + dx, 0, chunkPos.getMinBlockZ() + dz);
        return Optional.of(new GenerationStub(anchor, builder -> builder.addPiece(new OakExitPiece(anchor, rotation))));
    }

    private static long scatterHash(long seed, int chunkX, int chunkZ) {
        long h = seed ^ (chunkX * 341873128712L) ^ (chunkZ * 132897987541L);
        h = (h ^ (h >>> 33)) * 0x9E3779B97F4A7C15L;
        h = (h ^ (h >>> 29)) * 0xBF58476D1CE4E5B9L;
        return (h ^ (h >>> 32)) & Long.MAX_VALUE;
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.OAK_EXIT_TYPE.get();
    }
}
