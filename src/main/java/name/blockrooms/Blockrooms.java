package name.blockrooms;

import com.mojang.logging.LogUtils;
import name.blockrooms.block.ModBlocks;
import name.blockrooms.block.entity.ModBlockEntities;
import name.blockrooms.block.recipe.ModRecipeTypes;
import name.blockrooms.effect.ModMobEffects;
import name.blockrooms.entity.BloodZombie;
import name.blockrooms.entity.EnhancedSkeleton;
import name.blockrooms.entity.ModEntities;
import name.blockrooms.event.*;
import name.blockrooms.item.ModCreativeModeTabs;
import name.blockrooms.item.ModItems;
import name.blockrooms.item.components.ModDataComponents;
import name.blockrooms.sounds.ModSounds;
import name.blockrooms.world.generator.ModGenerators;
import name.blockrooms.world.structure.ModStructures;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
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
        modEventBus.addListener(this::addPackFinders);

        ModMobEffects.register(modEventBus);
        ModRecipeTypes.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
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
    }

    private void registerPlacements(RegisterSpawnPlacementsEvent event){
        event.register(ModEntities.BLOOD_ZOMBIE.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkAnyLightMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.BLOOD_ZOMBIE.get(), BloodZombie.createAttributes().build());
        event.put(ModEntities.SKELETON.get(), EnhancedSkeleton.createAttributes().build());
        event.put(ModEntities.BLACKSTONE_SHULKER.get(), Shulker.createAttributes().build());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        // LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public void onDatapackSync(OnDatapackSyncEvent event) {
        // 关键：NeoForge 的配方同步（RecipeContentPayload）只同步显式注册过的配方类型。
        // 不注册的话错误配方永远不会到客户端——错误合成台/JEI 都看不到专属配方。
        event.sendRecipes(ModRecipeTypes.ERROR_CRAFTING.get());
    }

    /**
     * 注册一个优先级高于内置数据包的子数据包（jar 根目录 recipe_tweaks/）。
     *
     * <p>为什么需要它：NeoForge 自带的数据包（mod/neoforge）里包含全套
     * {@code data/minecraft/recipe/}，并且它的优先级高于本模组的数据包——
     * 直接把覆盖配方放在 {@code data/minecraft/recipe/} 里会被静默遮蔽（不解析、
     * 不报错、原版配方照常生效）。通过 {@link AddPackFindersEvent} 注册的子数据包
     * 加载顺序在所有模组数据包之后，因此能真正覆盖原版配方。</p>
     *
     * <p>用法：在 {@code recipe_tweaks/data/minecraft/recipe/<id>.json} 里用
     * {@code "neoforge:conditions": [{"type": "neoforge:never"}]} 禁用原版配方。</p>
     */
    private void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.SERVER_DATA) {
            event.addPackFinders(
                    Identifier.fromNamespaceAndPath(Blockrooms.MODID, "recipe_tweaks"),
                    PackType.SERVER_DATA,
                    Component.literal("The Blockrooms Recipe Tweaks"),
                    PackSource.BUILT_IN,
                    true,
                    Pack.Position.TOP);
        }
    }
}
