package name.blockrooms.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockAndTintGetter;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.jspecify.annotations.Nullable;

import java.util.Set;

@EventBusSubscriber(value = Dist.CLIENT)
public class DynamicLightingHandler {
    @Nullable private static BlockPos sourcePos;
    private static Set<Entity> lightSources;

    private static boolean canOcclude(BlockAndTintGetter level, BlockPos pos) {
        return level.getBlockState(pos).canOcclude();
    }

    public static int getLightWithoutOcclusion(BlockAndTintGetter level, BlockPos pos) {
        if (sourcePos != null) {
            int distance = Math.abs(sourcePos.getX() - pos.getX())
                    + Math.abs(sourcePos.getY() - pos.getY())
                    + Math.abs(sourcePos.getZ() - pos.getZ());
            return Math.max(0, 15 - distance);
        }
        return 0;
    }

    /* private static void calculateLightPos(Level level, BlockPos pos) {

    } */

    /* at SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (player.getInventory().contains(input -> input.is(Items.GLOWSTONE))) {
                lightSources.add(player);
            }
        }
    } */

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        LocalPlayer player = Minecraft.getInstance().player;
        ClientLevel level = Minecraft.getInstance().level;
        if (player != null && level != null) {
            sourcePos = player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.GLOWSTONE_DUST) ? BlockPos.containing(player.position()) : null;

            /* level.getEntitiesOfClass(Entity.class,
                    AABB.unitCubeFromLowerCorner(player.position()).inflate(12.0F)); */
        }
    }
}
