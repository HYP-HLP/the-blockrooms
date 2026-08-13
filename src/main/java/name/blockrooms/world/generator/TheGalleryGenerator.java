package name.blockrooms.world.generator;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import name.blockrooms.event.level.GalleryExitHandler;
import name.blockrooms.event.level.PaintingPortalHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.PaintingVariantTags;
import net.minecraft.util.Util;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.StreamSupport;

/**
 * 画廊维度生成器（复刻 blockrooms wiki 归档版"The Gallery"设定）。
 * <p>
 * 设定要点（已归档页面/The_Gallery）：
 * <ul>
 *   <li>一条由橡木木板构成的<b>无限长走廊</b>，走廊两边挂满了画</li>
 *   <li>有些画可以穿过（像 BlockLevel 0），穿过后会来到一个和普通画廊无异的地方，
 *       同时原本的画也会突然消失（见 {@link PaintingPortalHandler}）</li>
 *   <li>走廊中不断响起活塞推拉的声音（由 biome 的环境音循环提供）</li>
 *   <li>随机出现空的物品展示框；极稀有的带画展示框是出口——
 *       转动上面的画可回到 BlockLevel 0（见 {@link GalleryExitHandler}），
 *       出口旁有"像火把一样的光源"（探员日志线索）</li>
 *   <li>没有怪物</li>
 * </ul>
 * 走廊沿 X 轴无限延伸，横截面参数为公开常量，供穿画传送落点使用
 * （传送必须落在走廊内部，避免掉进墙外的虚空）。
 */
public class TheGalleryGenerator extends BaseBlockLevelGenerator {
    /** 北墙 z（世界坐标） */
    public static final int WALL_Z_NEG = -1;
    /** 南墙 z（世界坐标） */
    public static final int WALL_Z_POS = 4;
    /** 地板 y（橡木木板） */
    public static final int FLOOR_Y = 0;
    /** 天花板 y（橡木木板，上方为基岩封顶） */
    public static final int CEILING_Y = 5;
    /** 穿画传送落点用的走廊中线 z（走廊内部 z 为 0..3） */
    public static final int SPAWN_Z = 2;

    /** 大型画锚点 y：固定为 2（4x4 画覆盖 y=1..4 的墙面，顶部恰好贴到天花板 y=5 下缘） */
    private static final int PAINT_ANCHOR_Y = 2;
    /** 锚点 x 限定在本 chunk 内 [2, 12]：北墙画覆盖 [x-1, x+3]、南墙画覆盖 [x-2, x+2]，
     *  都不跨出本 chunk，相邻 chunk 的画也不会伸进来重叠 */
    private static final int PAINT_X_MIN = 2;
    private static final int PAINT_X_MAX = 12;
    /** 同 chunk 两幅画的锚点最小间距（画宽 4 + 缓冲 2） */
    private static final int PAINT_GAP = 6;
    /** 第二幅画的出现概率（每面墙 1~2 幅） */
    private static final double SECOND_PAINTING_CHANCE = 0.5;
    /** 挂画位变成带画展示框（出口）的概率：每 chunk 有 2~4 个挂画位，0.04 时
     *  平均每 8 个 chunk ≈ 130 格一个出口，符合"极稀有"；探员日志：展示框出现很多
     *  但大多没有画，出口旁有"像火把一样的光源" */
    private static final double EXIT_FRAME_CHANCE = 0.04;
    /** 走廊天花板挂灯笼的间隔（格）：每 8 格一盏，灯笼半径 14 格可照亮整个走廊 */
    private static final int LAMP_INTERVAL = 8;
    /** 灯笼所在 z（走廊中线，铁链 + 灯笼，不挡玩家行走） */
    private static final int LAMP_Z = SPAWN_Z;

    public static final MapCodec<TheGalleryGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(TheGalleryGenerator::getBiomeSource)
            ).apply(instance, TheGalleryGenerator::new)
    );

    public TheGalleryGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {
        int minBlockZ = chunk.getPos().getMinBlockZ();
        for (int x = 0; x < 16; x++) {
            for (int lz = 0; lz < 16; lz++) {
                int wz = minBlockZ + lz;
                if (wz < WALL_Z_NEG || wz > WALL_Z_POS) continue;

                // 只写走廊本体 + 一层基岩封顶（y=0..6），上方不写方块：
                // 基岩不可破坏，玩家够不到上方的虚空。否则每 chunk 要写约 2.5 万个方块
                // （基岩堆到世界顶），进维度时上百区块同时生成、数千万次方块写入会卡到生成超时
                for (int y = this.getMinY(); y <= CEILING_Y + 1; y++) {
                    BlockState state = getBlockState(y, wz);
                    // 每隔几格在走廊中线挂一盏灯笼（y=4 铁链 + y=3 灯笼）：
                    // 走廊被基岩封顶、没有自然光，不放光源会一片漆黑
                    if (x % LAMP_INTERVAL == 0 && wz == LAMP_Z) {
                        if (y == CEILING_Y - 1) {
                            state = Blocks.IRON_CHAIN.defaultBlockState();
                        } else if (y == CEILING_Y - 2) {
                            state = Blocks.LANTERN.defaultBlockState();
                        }
                    }
                    chunk.setBlockState(new BlockPos(x, y, lz), state, Block.UPDATE_NONE);
                }
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    private static @NonNull BlockState getBlockState(int y, int wz) {
        BlockState state;
        if (y >= CEILING_Y + 1) {
            // 走廊上方基岩封顶
            state = Blocks.BEDROCK.defaultBlockState();
        } else if (y == FLOOR_Y || y == CEILING_Y) {
            state = Blocks.OAK_PLANKS.defaultBlockState();
        } else if (wz == WALL_Z_NEG || wz == WALL_Z_POS) {
            state = Blocks.OAK_PLANKS.defaultBlockState();
        } else {
            state = Blocks.CAVE_AIR.defaultBlockState();
        }
        return state;
    }

    @Override
    public void applyCarvers(WorldGenRegion worldGenRegion, long seed, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk) {
    }

    @Override
    public void buildSurface(WorldGenRegion worldGenRegion, StructureManager structureManager, RandomState randomState, ChunkAccess chunk) {
    }


    @Override
    public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {
        ChunkPos chunkPos = worldGenRegion.getCenter();
        // 画锚点格 z=0/3 只存在于 chunk z=0（走廊内部 z=0..3）；其余 chunk（走廊外或 z=-1）没有墙内格
        if (chunkPos.getMinBlockZ() != 0) return;

        long seed = worldGenRegion.getSeed();
        Random random = new Random(seed ^ (chunkPos.x * 0x9e3779b97f4a7c15L) ^ (chunkPos.z * 0xdefacedddeedbeefL));

        List<Holder<PaintingVariant>> largeVariants = StreamSupport.stream(worldGenRegion.registryAccess()
                .lookupOrThrow(Registries.PAINTING_VARIANT)
                .getTagOrEmpty(PaintingVariantTags.PLACEABLE)
                                .spliterator()
                ,false)
                .filter(variant -> variant.value().width() == 4 && variant.value().height() == 4)
                .toList();
        if (largeVariants.isEmpty()) return;

        int minBlockX = chunkPos.getMinBlockX();

        // 每面墙 1~2 幅（两面墙独立随机），每 chunk 共 2~4 幅：
        // setItem 死锁已修复（改为主线程挂画），实体数量不再是超时瓶颈，恢复"挂满了画"的密度
        for (boolean northWall : new boolean[]{true, false}) {
            // 第一幅必挂（锚点 x 随机）
            int x1 = PAINT_X_MIN + random.nextInt(PAINT_X_MAX - PAINT_X_MIN + 1);
            placePainting(worldGenRegion, random, largeVariants, minBlockX, x1, northWall);
            // 约一半概率在同墙挂第二幅：锚点 x 错开固定间距（环形偏移），两幅画不重叠
            if (random.nextDouble() < SECOND_PAINTING_CHANCE) {
                int x2 = x1 + PAINT_GAP;
                if (x2 > PAINT_X_MAX) x2 -= (PAINT_X_MAX - PAINT_X_MIN + 1);
                placePainting(worldGenRegion, random, largeVariants, minBlockX, x2, northWall);
            }
        }
    }

    /**
     * 在走廊一面墙上、指定锚点 x 处挂一幅 4x4 大型画。
     *
     * @param x         锚点 x（chunk 本地坐标，限定 [1, 12]）
     * @param northWall true = 北墙（锚点 z=0，面朝 +Z）；false = 南墙（锚点 z=3，面朝 -Z）
     */
    private static void placePainting(WorldGenRegion region, Random random,
                                      List<Holder<PaintingVariant>> largeVariants,
                                      int minBlockX, int x, boolean northWall) {
        int az = northWall ? 0 : 3;
        Direction facing = northWall ? Direction.SOUTH : Direction.NORTH;
        BlockPos anchor = new BlockPos(minBlockX + x, PAINT_ANCHOR_Y, az);

        if (random.nextDouble() < EXIT_FRAME_CHANCE) {
            ItemFrame frame = new ItemFrame(region.getLevel(), anchor, facing);
            region.addFreshEntity(frame);
            // 画不能在这里直接挂：ItemFrame.setItem() 会触发 updateNeighbourForOutputSignal
            // → getBlockState 查询方块，而生成阶段中心 chunk 尚未 FULL，
            // ServerChunkCache 会 join() 永久等待（FULL 依赖本线程的 SPAWN 步骤完成）
            // → worldgen 线程自死锁，生成流水线整体卡死（崩溃报告已证实）。
            // 改为交给服务器主线程挂画：那时区块已 FULL，getBlockState 立即返回；
            // 若展示框已被移除（区块卸载）则跳过。
            region.getLevel().getServer().execute(() -> {
                if (frame.isAlive()) {
                    frame.setItem(new ItemStack(Items.PAINTING));
                }
            });
            region.setBlock(anchor.below(),
                    Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, facing),
                    Block.UPDATE_NONE);
            return;
        }

        Optional<Holder<PaintingVariant>> variant = Util.getRandomSafe(largeVariants, region.getRandom());
        if (variant.isEmpty()) return;
        // 画实体：位置 = 贴墙空气格，面朝走廊（支撑墙在反方向），不调用 survives（区域外查询会崩）
        Painting painting = new Painting(region.getLevel(), anchor, facing, variant.get());
        region.addFreshEntity(painting);
    }

    @Override
    public int getBaseHeight(int i, int i1, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        return 0;
    }
}
