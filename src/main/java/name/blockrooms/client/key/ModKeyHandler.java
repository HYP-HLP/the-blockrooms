package name.blockrooms.client.key;

import name.blockrooms.network.NoclipPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@EventBusSubscriber(value = Dist.CLIENT)
public class ModKeyHandler {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        if (ModKeyBindings.noclippingKey.consumeClick()) {
            // Blockrooms.LOGGER.info("Pressed N");
            ClientPacketDistributor.sendToServer(new NoclipPayload());
            // NetworkHandler.sendToServer()
        }
    }
}
