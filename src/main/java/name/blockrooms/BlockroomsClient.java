package name.blockrooms;

import name.blockrooms.client.renderer.BlockProjectileRenderer;
import name.blockrooms.client.renderer.ItemProjectileRenderer;
import name.blockrooms.entity.ModEntities;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@Mod(value = Blockrooms.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Blockrooms.MODID, value = Dist.CLIENT)
public class BlockroomsClient {
    public BlockroomsClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.BLOCK_PROJECTILE.get(), BlockProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.ITEM_PROJECTILE.get(), ItemProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.BLOOD_ZOMBIE.get(), ZombieRenderer::new);
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        // Call event.createDatapackRegistryObjects(...) first if adding datapack objects
    }
}
