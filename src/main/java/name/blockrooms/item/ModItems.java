package name.blockrooms.item;

import name.blockrooms.Blockrooms;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ClearAllStatusEffectsConsumeEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Blockrooms.MODID);
    public static final DeferredItem<Item> ALMOND_MILK_BUCKET =
            ITEMS.registerSimpleItem("almond_milk_bucket",
                    properties -> properties.craftRemainder(Items.BUCKET)
                            .usingConvertsTo(Items.BUCKET).stacksTo(1)
                            .component(DataComponents.CONSUMABLE,
                            Consumables.defaultDrink()
                                    .onConsume(ClearAllStatusEffectsConsumeEffect.INSTANCE)
                                    .onConsume(new ApplyStatusEffectsConsumeEffect(
                                            List.of(
                                                    new MobEffectInstance(MobEffects.REGENERATION, 600, 1),
                                                    new MobEffectInstance(MobEffects.ABSORPTION, 600, 1)
                                            )
                                    ))
                                    .build()));
    public static final DeferredItem<Item> RUBY =
            ITEMS.registerSimpleItem("ruby");
    public static final DeferredItem<Item> RUBY_SWORD =
            ITEMS.registerSimpleItem("ruby_sword", properties -> properties.sword(ModToolMaterials.RUBY, 3.0F, -2.4F));
    public static final DeferredItem<Item> RUBY_SHOVEL =
            ITEMS.registerSimpleItem("ruby_shovel", properties -> properties.shovel(ModToolMaterials.RUBY, 1.5F, -3.0F));
    public static final DeferredItem<Item> RUBY_PICKAXE =
            ITEMS.registerSimpleItem("ruby_pickaxe", properties -> properties.pickaxe(ModToolMaterials.RUBY, 1.0F, -2.8F));
    public static final DeferredItem<Item> RUBY_AXE =
            ITEMS.registerSimpleItem("ruby_axe", properties -> properties.axe(ModToolMaterials.RUBY, 5.0F, -3.0F));
    public static final DeferredItem<Item> RUBY_HOE =
            ITEMS.registerSimpleItem("ruby_hoe", properties -> properties.hoe(ModToolMaterials.RUBY, -3.0F, 0.0F));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
