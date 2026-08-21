package name.blockrooms.event.client;

import name.blockrooms.ClientConfig;
import name.blockrooms.client.hud.DifficultyLayer;
import name.blockrooms.client.hud.LevelInfoData;
import name.blockrooms.client.hud.LevelInfoLayer;
import name.blockrooms.client.hud.LevelInfoManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;

import java.util.Optional;

@EventBusSubscriber(value = Dist.CLIENT)
public class LevelInfoHandler {
    private static ResourceKey<Level> lastDimension;
    private static String lastLanguage;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        LevelInfoLayer.instance().tick();

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            lastDimension = null;
            LevelInfoLayer.instance().hide();
            DifficultyLayer.instance().hide();
            return;
        }
        ResourceKey<Level> dimension = mc.player.level().dimension();
        String language = mc.options.languageCode;
        boolean languageChanged = language != null && !language.equals(lastLanguage);
        lastLanguage = language;
        if (!dimension.equals(lastDimension) || languageChanged) {
            lastDimension = dimension;
            refreshPanels(dimension);
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        LevelInfoLayer layer = LevelInfoLayer.instance();
        if (!layer.isActive() || !layer.contains(event.getMouseX(), event.getMouseY())) {
            return;
        }
        double delta = event.getScrollDeltaY();
        if (delta == 0) {
            return;
        }
        layer.scrollBy(delta > 0 ? -1 : 1);
        event.setCanceled(true);
    }
    private static void refreshPanels(ResourceKey<Level> dimension) {
        Optional<LevelInfoData> info = LevelInfoManager.get(dimension);
        if (ClientConfig.LEVEL_INFO_ENABLED.get()) {
            info.ifPresent(LevelInfoLayer.instance()::show);
        }
        DifficultyLayer.instance().show(info.map(LevelInfoData::difficulty).orElse(null));
    }
}
