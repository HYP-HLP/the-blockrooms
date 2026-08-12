package name.blockrooms.event;

import name.blockrooms.util.ModLevels;
import name.blockrooms.util.TeleportUtils;
import name.blockrooms.world.generator.TheGalleryGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.List;

import static name.blockrooms.util.TeleportUtils.teleportPlayer;

/**
 * 穿画传送：玩家走进画时会被传送到"画后的空间"。
 * <ul>
 *   <li>BlockLevel 0：大部分情况下是本域层中的另一部分（非欧几里得随机偏移）；
 *       罕见地（10%）到达画廊维度，落点固定为走廊中线（随机 z 几乎总会掉进走廊外的虚空）</li>
 *   <li>画廊（The Gallery）：穿过画会来到一个和普通画廊无异的地方，同时原本的画也会突然消失
 *       （复刻 wiki 设定：传送后原画消失）</li>
 * </ul>
 */
@EventBusSubscriber
public class PaintingPortalHandler {
    private static final double GALLERY_CHANCE = 0.05;
    private static final int RANDOM_TELEPORT_RANGE = 512;

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof Painting painting)) return;
        Level level = painting.level();
        boolean inBlockLevel0 = level.dimension().equals(ModLevels.BLOCKLEVEL_0);
        boolean inGallery = level.dimension().equals(ModLevels.GALLERY);
        if (!inBlockLevel0 && !inGallery) return;
        if (level.isClientSide()) return;

        List<ServerPlayer> players = level.getEntitiesOfClass(ServerPlayer.class, painting.getBoundingBox().inflate(0.25));
        if (players.isEmpty()) return;
        ServerPlayer player = players.getFirst();

        if (inGallery) {
            // 画廊内穿画：来到另一个"和普通画廊无异"的地方，原本的画突然消失
            painting.discard();
            int dx = level.random.nextInt(RANDOM_TELEPORT_RANGE * 2) - RANDOM_TELEPORT_RANGE;
            // 落点固定为走廊中线 z（走廊内部只有 4 格宽），随机 z 几乎总会掉进墙外的虚空
            teleportPlayer(player, ModLevels.GALLERY, new Vec3(player.getX() + dx, 1, TheGalleryGenerator.SPAWN_Z));
            return;
        }

        if (level.random.nextDouble() < GALLERY_CHANCE) {
            // 罕见：穿过画到达画廊维度
            ServerLevel gallery = ((ServerLevel) level).getServer().getLevel(ModLevels.GALLERY);
            if (gallery == null) return;
            int dx = level.random.nextInt(RANDOM_TELEPORT_RANGE * 2) - RANDOM_TELEPORT_RANGE;
            BlockPos target = new BlockPos(dx, 1, TheGalleryGenerator.SPAWN_Z);
            BlockPos safe = TeleportUtils.findSafeSpot(gallery, target);
            if (safe == null) return;
            teleportPlayer(player, gallery.dimension(), safe.getBottomCenter());
        } else {
            int dx = level.random.nextInt(RANDOM_TELEPORT_RANGE * 2) - RANDOM_TELEPORT_RANGE;
            int dz = level.random.nextInt(RANDOM_TELEPORT_RANGE * 2) - RANDOM_TELEPORT_RANGE;
            BlockPos target = player.blockPosition().offset(dx, 0, dz);
            BlockPos safe = TeleportUtils.findSafeSpot(level, target);
            if (safe == null) return;
            player.teleportTo(safe.getX() + 0.5, safe.getY(), safe.getZ() + 0.5);
        }
    }
}
