package name.blockrooms.event;

import name.blockrooms.util.FlexibleMap;
import name.blockrooms.util.ModLevels;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import static name.blockrooms.util.TeleportUtils.teleportPlayer;

public class NoclipHandler {
    public record levelWithChance(ResourceKey<Level> level, double chance) {}
    private static final FlexibleMap<ResourceKey<Level>, BlockState, levelWithChance> noclipMap = new FlexibleMap<>();


    public static void noclipByCondition(ServerPlayer player, BlockState state) {
        levelWithChance destination = noclipMap.get(player.level().dimension(), state);
        if (destination == null) return;
        if (!(player.getRandom().nextDouble() <= destination.chance)) return;

        teleportPlayer(player, destination.level, player.getX(), 1, player.getZ());
    }

    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;
        if (!(event.getSource().is(DamageTypes.IN_WALL))) return;

        noclipByCondition(player, player.getInBlockState());
    }

    static {
        noclipMap.put(Level.OVERWORLD,
                new levelWithChance(ModLevels.BLOCKLEVEL_0, 0.2));
        noclipMap.put(Level.OVERWORLD, Blocks.AMETHYST_BLOCK.defaultBlockState(),
                new levelWithChance(Level.NETHER, 0.9));
        noclipMap.put(ModLevels.BLOCKLEVEL_0, Blocks.AMETHYST_BLOCK.defaultBlockState(),
                new levelWithChance(ModLevels.BLOCKLEVEL_4, 0.9));
    }
}