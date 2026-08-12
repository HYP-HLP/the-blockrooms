package name.blockrooms.event;

import name.blockrooms.util.ModLevels;
import name.blockrooms.util.TeleportUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
public class GalleryExitHandler {
    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Entity target = event.getTarget();
        if (!(target instanceof ItemFrame frame)) return;
        Level level = frame.level();
        if (level.isClientSide()) return;
        if (!level.dimension().equals(ModLevels.GALLERY)) return;
        if (!frame.getItem().is(Items.PAINTING)) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        event.setCanceled(true);

        ServerLevel blockLevel0 = level.getServer().getLevel(ModLevels.BLOCKLEVEL_0);
        if (blockLevel0 == null) return;
        BlockPos targetPos = new BlockPos(player.blockPosition().getX(), 1, player.blockPosition().getZ());
        BlockPos safe = TeleportUtils.findSafeSpot(blockLevel0, targetPos);
        if (safe == null) return;
        TeleportUtils.teleportPlayer(player, ModLevels.BLOCKLEVEL_0, safe.getX() + 0.5, safe.getY(), safe.getZ() + 0.5);
    }
}
