package name.blockrooms.event;

import name.blockrooms.Blockrooms;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class NoclipHandler {
    public record levelWithChance(ResourceKey<Level> level, double chance) {}
    private static final Map<ResourceKey<Level>, levelWithChance> noclipMap = new HashMap<>();
    private static ResourceKey<Level> level(String key) {
        return ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath(Blockrooms.MODID, key));
    }
    private static boolean teleportPlayer(ServerPlayer player, ResourceKey<Level> level, double x, double y, double z) {
        return player.teleportTo(player.level().getServer().getLevel(level), x, y, z, Set.of(), player.getYRot(), player.getXRot(), true);
    }

    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;
        if (!(event.getSource().is(DamageTypes.IN_WALL))) return;
        if (!noclipMap.containsKey(player.level().dimension())) return;

        levelWithChance destination = noclipMap.get(player.level().dimension());
        if (!(player.getRandom().nextDouble() <= destination.chance)) return;

        teleportPlayer(player, destination.level, player.getX(), 1, player.getZ());
    }

    static {
        noclipMap.put(Level.OVERWORLD,
                new levelWithChance(level("blocklevel0"), 0.2));
        noclipMap.put(level("blocklevel0"),
                new levelWithChance(Level.END, 0.05));
    }
}
