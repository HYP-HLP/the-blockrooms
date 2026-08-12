package name.blockrooms.event;

import name.blockrooms.util.FlexibleMap;
import name.blockrooms.util.ModLevels;
import name.blockrooms.util.TeleportUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber
public class NoclipHandler {
    public record levelWithChance(ResourceKey<Level> level, double chance) {}
    private static final FlexibleMap<ResourceKey<Level>, BlockState, levelWithChance> noclipMap = new FlexibleMap<>();

    public static void noclipByCondition(ServerPlayer player, BlockState state) {
        levelWithChance destination = noclipMap.get(player.level().dimension(), state);
        if (destination == null) return;
        if (!(player.getRandom().nextDouble() <= destination.chance)) return;

        TeleportUtils.teleportPlayer(player, destination.level);
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;
        if (!(event.getSource().is(DamageTypes.IN_WALL))) return;

        noclipByCondition(player, player.getInBlockState());
    }

    static {
        noclipMap.put(Level.OVERWORLD,
                new levelWithChance(ModLevels.BLOCKLEVEL_0, 0.2));
        // DEBUG
        noclipMap.put(ModLevels.BLOCKLEVEL_0, Blocks.AMETHYST_BLOCK.defaultBlockState(),
                new levelWithChance(ModLevels.BLOCKLEVEL_4, 0.9));
        noclipMap.put(ModLevels.BLOCKLEVEL_4, Blocks.AMETHYST_BLOCK.defaultBlockState(),
                new levelWithChance(Level.NETHER, 0.9));
    }
}