package name.blockrooms.world.generator;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.PaintingVariantTags;
import net.minecraft.util.Util;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.StreamSupport;

public class BlockLevel0Generator extends BaseBlockLevelGenerator {
    public static final MapCodec<BlockLevel0Generator> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(BlockLevel0Generator::getBiomeSource)
            ).apply(instance, BlockLevel0Generator::new)
    );

    /** 据点生成范围：世界中心 128 个区块（=2048 格） */
    private static final int STRUCTURE_RANGE = 128 * 16;

    private static final double LAMP_OFF_CHANCE = 0.10;
    private static final double CARPET_GAP_CHANCE = 0.10;
    private static final double CEILING_HOLE_CHANCE = 0.05;
    private static final double PAINTING_CHANCE = 0.02;

    /** 据点位置的种子偏移常量 */
    private static final long OUTPOST_SEED_XOR = 0xB3E62F0A5D1C4E39L;

    public BlockLevel0Generator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunk.getPos().getMinBlockX() + x;
                int worldZ = chunk.getPos().getMinBlockZ() + z;

                for (int y = this.getMinY(); y <= this.getGenDepth(); y++) {
                    if (y >= 0 && y <= 4) chunk.setBlockState(new BlockPos(x, y, z), Blocks.CAVE_AIR.defaultBlockState(), Block.UPDATE_NONE);
                    else chunk.setBlockState(new BlockPos(x, y, z), Blocks.BEDROCK.defaultBlockState(), Block.UPDATE_NONE);
                }
                chunk.setBlockState(new BlockPos(x, 0, z), Blocks.OAK_PLANKS.defaultBlockState(), Block.UPDATE_NONE);
                chunk.setBlockState(new BlockPos(x, 1, z), Blocks.BROWN_CARPET.defaultBlockState(), Block.UPDATE_NONE);

                if ((worldX % 5 + 5) % 5 < 2 && (worldZ % 2 + 2) % 2 == 0) {
                    chunk.setBlockState(new BlockPos(x, 5, z), Blocks.REDSTONE_LAMP.defaultBlockState().setValue(RedstoneLampBlock.LIT, true), Block.UPDATE_ALL);
                    chunk.setBlockState(new BlockPos(x, 6, z), Blocks.REDSTONE_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
                }
                else {
                    chunk.setBlockState(new BlockPos(x, 5, z), Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public void applyCarvers(WorldGenRegion worldGenRegion, long seed, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk) {
    }

    @Override
    public void buildSurface(WorldGenRegion worldGenRegion, StructureManager structureManager, RandomState randomState, ChunkAccess chunk) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunk.getPos().getMinBlockX() + x;
                int worldZ = chunk.getPos().getMinBlockZ() + z;

                if(worldX <= 1024 && worldX >= -1024 && worldZ <= 1024 && worldZ >= -1024){
                    for (int y = 1; y <= 4; y++) {
                        chunk.setBlockState(new BlockPos(1, y, 1), Blocks.CHISELED_SANDSTONE.defaultBlockState(), Block.UPDATE_SUPPRESS_DROPS);
                        chunk.setBlockState(new BlockPos(2, y, 1), Blocks.CHISELED_SANDSTONE.defaultBlockState(), Block.UPDATE_SUPPRESS_DROPS);
                        chunk.setBlockState(new BlockPos(1, y, 2), Blocks.CHISELED_SANDSTONE.defaultBlockState(), Block.UPDATE_SUPPRESS_DROPS);
                        chunk.setBlockState(new BlockPos(2, y, 2), Blocks.CHISELED_SANDSTONE.defaultBlockState(), Block.UPDATE_SUPPRESS_DROPS);
                        chunk.setBlockState(new BlockPos(14, y, 14), Blocks.CHISELED_SANDSTONE.defaultBlockState(), Block.UPDATE_SUPPRESS_DROPS);
                        chunk.setBlockState(new BlockPos(14, y, 15), Blocks.CHISELED_SANDSTONE.defaultBlockState(), Block.UPDATE_SUPPRESS_DROPS);
                        chunk.setBlockState(new BlockPos(15, y, 14), Blocks.CHISELED_SANDSTONE.defaultBlockState(), Block.UPDATE_SUPPRESS_DROPS);
                        chunk.setBlockState(new BlockPos(15, y, 15), Blocks.CHISELED_SANDSTONE.defaultBlockState(), Block.UPDATE_SUPPRESS_DROPS);
                        chunk.setBlockState(new BlockPos(1, y, 14), Blocks.CHISELED_SANDSTONE.defaultBlockState(), Block.UPDATE_SUPPRESS_DROPS);
                        chunk.setBlockState(new BlockPos(1, y, 15), Blocks.CHISELED_SANDSTONE.defaultBlockState(), Block.UPDATE_SUPPRESS_DROPS);
                        chunk.setBlockState(new BlockPos(2, y, 14), Blocks.CHISELED_SANDSTONE.defaultBlockState(), Block.UPDATE_SUPPRESS_DROPS);
                        chunk.setBlockState(new BlockPos(2, y, 15), Blocks.CHISELED_SANDSTONE.defaultBlockState(), Block.UPDATE_SUPPRESS_DROPS);
                        chunk.setBlockState(new BlockPos(14, y, 1), Blocks.CHISELED_SANDSTONE.defaultBlockState(), Block.UPDATE_SUPPRESS_DROPS);
                        chunk.setBlockState(new BlockPos(14, y, 2), Blocks.CHISELED_SANDSTONE.defaultBlockState(), Block.UPDATE_SUPPRESS_DROPS);
                        chunk.setBlockState(new BlockPos(15, y, 1), Blocks.CHISELED_SANDSTONE.defaultBlockState(), Block.UPDATE_SUPPRESS_DROPS);
                        chunk.setBlockState(new BlockPos(15, y, 2), Blocks.CHISELED_SANDSTONE.defaultBlockState(), Block.UPDATE_SUPPRESS_DROPS);
                    }
                }
//                if (new Random(worldGenRegion.getSeed() ^ (worldX * 0x9e3779b97f4a7c15L) ^ (worldZ * 0xdefacedddeedbeefL)).nextDouble() <= 0.2) {
//                    for (int y = 1; y <= 4; y++) {
//                        chunk.setBlockState(new BlockPos(x, y, z), Blocks.CHISELED_SANDSTONE.defaultBlockState(), Block.UPDATE_SUPPRESS_DROPS);
//                    }
//                }




            }
        }

        long seed = worldGenRegion.getSeed();
        applyVariantRegions(seed, chunk);
        // B.M.E.G. 据点已注册为结构（blockrooms:bmeg_outpost），在 FEATURES 阶段生成，可用 /locate 寻找
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {
        placePaintings(worldGenRegion);
    }

    @Override
    public int getBaseHeight(int i, int i1, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        return 0;
    }

    public static BlockPos outpostCenter(long seed) {
        Random random = new Random(seed ^ OUTPOST_SEED_XOR);
        int x = random.nextInt(STRUCTURE_RANGE * 2) - STRUCTURE_RANGE;
        int z = random.nextInt(STRUCTURE_RANGE * 2) - STRUCTURE_RANGE;
        return new BlockPos(x, 2, z);
    }

    private static void applyVariantRegions(long seed, ChunkAccess chunk) {
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;
        Random random = new Random(seed ^ (chunkX * 0x9e3779b97f4a7c15L) ^ (chunkZ * 0xdefacedddeedbeefL));

        boolean lampsOff = random.nextDouble() < LAMP_OFF_CHANCE;
        boolean carpetGaps = random.nextDouble() < CARPET_GAP_CHANCE;
        boolean ceilingHole = random.nextDouble() < CEILING_HOLE_CHANCE;
        if (!lampsOff && !carpetGaps && !ceilingHole) return;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (lampsOff) {
                    BlockPos lampPos = new BlockPos(x, 5, z);
                    BlockState lamp = chunk.getBlockState(lampPos);
                    if (lamp.is(Blocks.REDSTONE_LAMP)) {
                        chunk.setBlockState(lampPos, lamp.setValue(RedstoneLampBlock.LIT, false), Block.UPDATE_CLIENTS);
                        chunk.setBlockState(new BlockPos(x, 6, z), Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                }
                if (carpetGaps && random.nextDouble() < 0.25) {
                    BlockPos carpetPos = new BlockPos(x, 1, z);
                    if (chunk.getBlockState(carpetPos).is(Blocks.BROWN_CARPET)) {
                        chunk.setBlockState(carpetPos, Blocks.CAVE_AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                }
            }
        }

        if (ceilingHole) {
            int holeX = random.nextInt(14);
            int holeZ = random.nextInt(14);
            for (int dx = 0; dx < 2; dx++) {
                for (int dz = 0; dz < 2; dz++) {
                    int bx = holeX + dx;
                    int bz = holeZ + dz;
                    if (chunk.getBlockState(new BlockPos(bx, 5, bz)).is(Blocks.REDSTONE_LAMP)) {
                        chunk.setBlockState(new BlockPos(bx, 6, bz), Blocks.CAVE_AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                    chunk.setBlockState(new BlockPos(bx, 5, bz), Blocks.CAVE_AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
        }
    }
    private static void placePaintings(WorldGenRegion region) {
        ChunkAccess chunk = region.getChunk(region.getCenter().x, region.getCenter().z);
        List<Holder<PaintingVariant>> smallVariants = StreamSupport.stream(region.registryAccess()
                .lookupOrThrow(Registries.PAINTING_VARIANT)
                .getTagOrEmpty(PaintingVariantTags.PLACEABLE)
                                .spliterator()
                ,false)
                .filter(variant -> variant.value().width() == 1 && variant.value().height() == 1)
                .toList();
        if (smallVariants.isEmpty()) return;

        int minBlockX = chunk.getPos().getMinBlockX();
        int minBlockZ = chunk.getPos().getMinBlockZ();

        for (int y = 2; y <= 3; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!chunk.getBlockState(pos).isAir()) continue;

                    for (Direction dir : Direction.Plane.HORIZONTAL) {
                        // 墙必须在当前区块内：本地坐标越界会被 chunk.getBlockState 按 &15 环绕成对面的方块，
                        // 且世界生成阶段不能对区块外的碰撞查询（会抛 IllegalStateException）
                        int wx = x + dir.getStepX();
                        int wz = z + dir.getStepZ();
                        if (wx < 0 || wx > 15 || wz < 0 || wz > 15) continue;
                        BlockPos wallPos = new BlockPos(wx, y, wz);
                        if (!chunk.getBlockState(wallPos).is(Blocks.CHISELED_SANDSTONE)) continue;
                        // 墙后方也必须是空气，避免画挂在墙的夹缝里
                        int bx = wx + dir.getStepX();
                        int bz = wz + dir.getStepZ();
                        if (bx < 0 || bx > 15 || bz < 0 || bz > 15) continue;
                        if (!chunk.getBlockState(new BlockPos(bx, y, bz)).isAir()) continue;

                        if (region.getRandom().nextDouble() > PAINTING_CHANCE) continue;

                        Optional<Holder<PaintingVariant>> variant = Util.getRandomSafe(smallVariants, region.getRandom());
                        if (variant.isEmpty()) continue;
                        Painting painting = new Painting(region.getLevel(),
                                new BlockPos(minBlockX + x, y, minBlockZ + z), dir.getOpposite(), variant.get());
                        region.addFreshEntity(painting);
                        break;
                    }
                }
            }
        }
    }

}
