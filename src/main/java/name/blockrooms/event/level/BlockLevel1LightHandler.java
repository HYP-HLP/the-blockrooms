package name.blockrooms.event.level;

import name.blockrooms.Blockrooms;
import name.blockrooms.block.ModBlocks;
import name.blockrooms.util.ModLevels;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.*;

@EventBusSubscriber
public class BlockLevel1LightHandler {
    private static final BooleanProperty LIT = BlockStateProperties.LIT;
    private static final Set<ChunkPos> loadedChunks = new HashSet<>();
    private static boolean wasDay = false;

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        Level level = event.getChunk().getLevel();
        if (!level.dimension().equals(ModLevels.BLOCKLEVEL_1)) return;
        loadedChunks.add(event.getChunk().getPos());

        boolean isDay = level.getDayTime() % 24000 < 13000;
        handleLit(event.getChunk(), isDay);
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (!event.getChunk().getLevel().dimension().equals(ModLevels.BLOCKLEVEL_1)) return;
        loadedChunks.remove(event.getChunk().getPos());
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        Level level = event.getServer().getLevel(ModLevels.BLOCKLEVEL_1);
        if (level != null) wasDay = level.getDayTime() % 24000 < 13000;
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        loadedChunks.clear();
    }

    @SubscribeEvent
    public static void onDayNightChanged(LevelTickEvent.Pre event) {
        Level level = event.getLevel();
        if (!level.dimension().equals(ModLevels.BLOCKLEVEL_1)) return;
        boolean isDay = level.getDayTime() % 24000 < 13000;
        if (wasDay == isDay) return;
        wasDay = isDay;

        long startTime = System.currentTimeMillis();
        handleLit(level, isDay);
        long time = System.currentTimeMillis() - startTime;
        Blockrooms.LOGGER.info("Took {}ms for BlockLevel 1 to handle one cycle", time);
    }

    private static void handleLit(Level level, boolean isDay) {
        for (ChunkPos chunkPos : new HashSet<>(loadedChunks)) {
            LevelChunk chunk = level.getChunk(chunkPos.x, chunkPos.z);
            handleLit(chunk, isDay);
        }
    }

    private static void handleLit(LevelChunk chunk, boolean isDay) {
        chunk.findBlocks(state -> isValidDetectorBlock(state) && (state.getValue(LIT) ^ isDay),
                (pos, output) -> {
                    chunk.setBlockState(pos, output.setValue(LIT, isDay));
                    chunk.getLevel().sendBlockUpdated(pos, output, output.setValue(LIT, isDay), 2);
                });
    }

    private static boolean isValidDetectorBlock(BlockState state) {
        return state.is(ModBlocks.DETECTOR_TORCH) || state.is(ModBlocks.DETECTOR_WALL_TORCH) || state.is(ModBlocks.DETECTOR_REDSTONE_LAMP_BLOCK);
    }
}
