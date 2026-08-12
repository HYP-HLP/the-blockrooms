package name.blockrooms.util;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;
import java.util.Set;

public class TeleportUtils {
    public static boolean teleportPlayer(ServerPlayer player, ResourceKey<Level> level, double x, double y, double z) {
        return player.teleportTo(Objects.requireNonNull(player.level().getServer().getLevel(level)), x, y, z, Set.of(), player.getYRot(), player.getXRot(), true);
    }

    /** 在目标位置附近寻找可站立的安全落点（脚下实心、上下都有空间的空气格） */
    public static BlockPos findSafeSpot(Level level, BlockPos target) {
        for (int dy = -2; dy <= 3; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos pos = target.offset(dx, dy, dz);
                    if (isSafe(level, pos)) return pos;
                }
            }
        }
        return null;
    }

    public static boolean isSafe(Level level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return level.getBlockState(pos).isAir()
                && level.getBlockState(pos.above()).isAir()
                && below.isSolid();
    }
}
