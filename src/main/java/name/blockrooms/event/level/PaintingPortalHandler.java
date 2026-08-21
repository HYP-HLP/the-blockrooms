package name.blockrooms.event.level;

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
            painting.discard();
            int dx = level.random.nextInt(RANDOM_TELEPORT_RANGE * 2) - RANDOM_TELEPORT_RANGE;
            // 落点固定为走廊中线 z（走廊内部只有 4 格宽），随机 z 几乎总会掉进墙外的虚空
            teleportPlayer(player, ModLevels.GALLERY, new Vec3(player.getX() + dx, 1, TheGalleryGenerator.SPAWN_Z));
            return;
        }

        if (level.random.nextDouble() < GALLERY_CHANCE) {
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
