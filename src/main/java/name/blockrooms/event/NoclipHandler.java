package name.blockrooms.event;

import name.blockrooms.Blockrooms;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class NoclipHandler {
    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;
        if (!(event.getSource().is(DamageTypes.IN_WALL))) return;

        if (player.getRandom().nextDouble() <= 0.3) return;

        Blockrooms.LOGGER.info("Noclip Failed");
        // player.teleportTo(ServerLevel.END, 0, 1, 0);
    }
}
