package name.blockrooms;

import com.mojang.logging.LogUtils;
import name.blockrooms.block.ModBlocks;
import name.blockrooms.block.recipe.ModRecipeTypes;
import name.blockrooms.effect.ModMobEffects;
import name.blockrooms.entity.BloodZombie;
import name.blockrooms.entity.ModEntities;
import name.blockrooms.event.*;
import name.blockrooms.item.ModCreativeModeTabs;
import name.blockrooms.item.ModItems;
import name.blockrooms.item.components.ModDataComponents;
import name.blockrooms.sounds.ModSounds;
import name.blockrooms.world.generator.ModGenerators;
import name.blockrooms.world.structure.ModStructures;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(Blockrooms.MODID)
public class Blockrooms {
    public static final String MODID = "blockrooms";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Blockrooms(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerPlacements);
        modEventBus.addListener(this::registerEntityAttributes);

        ModMobEffects.register(modEventBus);
        ModRecipeTypes.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
        ModSounds.register(modEventBus);
        ModGenerators.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModEntities.register(modEventBus);
        ModStructures.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        NeoForge.EVENT_BUS.register(this);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // 嗜血僵尸的生成放置规则：地面 + 任意光照（画廊靠灯笼照明，
        // 常规的"黑暗"判定会拒绝生成）
    }

    private void registerPlacements(RegisterSpawnPlacementsEvent event){
        event.register(ModEntities.BLOOD_ZOMBIE.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkAnyLightMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
    /** 注册实体属性（生命/攻击/移速来自 Config，见 Config.java） */
    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.BLOOD_ZOMBIE.get(), BloodZombie.createAttributes().build());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        // LOGGER.info("HELLO from server starting");
    }
}
