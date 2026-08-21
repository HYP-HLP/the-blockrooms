package name.blockrooms.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.*;
import name.blockrooms.Blockrooms;
import name.blockrooms.block.ModBlocks;
import name.blockrooms.block.inventory.ErrorCraftingMenu;
import name.blockrooms.block.recipe.ErrorCraftingRecipe;
import name.blockrooms.block.recipe.ErrorCraftingShapelessRecipe;
import name.blockrooms.block.recipe.ModRecipeTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * JEI 集成（可选依赖：游戏里没有装 JEI 时本类不会被加载，模组照常运行）。
 *
 * <ul>
 *   <li>错误合成配方独立注册为专属分类（错误合成 / Error Crafting）——错误配方独有的物品
 *       能在该分类看到配方，与原版配方重复的物品也能看到「错误版本」的配方；</li>
 *   <li>配方通过 {@link IRecipeManagerPlugin} 动态提供：数据源是 NeoForge {@link RecipesReceivedEvent}
 *       同步到客户端的 RecipeMap（单机/联机都走这条路径），每次配方更新都会被查询，进世界即生效；</li>
 *   <li>错误合成台注册为该分类的合成站，支持一键转移（ErrorCraftingMenu 槽位布局与原版一致，
 *       模组已实现 beginPlacingRecipe / finishPlacingRecipe 钩子）。</li>
 * </ul>
 */
@JeiPlugin
public class BlockroomsJeiPlugin implements IModPlugin {

    /** 客户端从服务端同步的完整配方表（RecipesReceivedEvent 提供）。
     *  注意：ClientPacketListener.recipes() 返回 ClientRecipeContainer（不是 RecipeManager），
     *  而且 RecipeAccess 接口没有配方查询 API——客户端配方唯一可靠来源就是这个事件。 */
    private static RecipeMap clientRecipeMap;

    static {
        // 本类只在 JEI 存在时才会被加载（@JeiPlugin 扫描），此时注册客户端配方同步监听是安全的
        NeoForge.EVENT_BUS.register(BlockroomsJeiPlugin.class);
    }

    @SubscribeEvent
    public static void onRecipesReceived(RecipesReceivedEvent event) {
        clientRecipeMap = event.getRecipeMap();
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        clientRecipeMap = null;
    }

    /** 错误合成的 JEI 配方类型（基于模组的 RecipeType 创建，与原版类型解耦）。
     *  createDeferred 返回 Supplier：JEI 可能在注册表绑定前就加载本类，
     *  直接调用 get() 会抛 "Trying to access unbound value"，故延迟到用时再解析。 */
    public static final Supplier<IRecipeHolderType<CraftingRecipe>> ERROR_CRAFTING_TYPE =
            IRecipeHolderType.createDeferred(ModRecipeTypes.ERROR_CRAFTING::get);

    private static IRecipeHolderType<CraftingRecipe> resolvedErrorType;

    public static synchronized IRecipeHolderType<CraftingRecipe> errorType() {
        if (resolvedErrorType == null) {
            resolvedErrorType = ERROR_CRAFTING_TYPE.get();
        }
        return resolvedErrorType;
    }

    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath(Blockrooms.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new ErrorCraftingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addItemStackInfo(
                new ItemStack(ModBlocks.ERROR_CRAFTING_TABLE.get()),
                Component.translatable("blockrooms.jei.info.error_crafting_table"));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(errorType(), ModBlocks.ERROR_CRAFTING_TABLE.get());
        // 错误合成台 + 石制合成台：原版「合成」分类的合成站（错误台也能做原版配方）
//        registration.addCraftingStation(RecipeTypes.CRAFTING,
//                ModBlocks.ERROR_CRAFTING_TABLE.get(), ModBlocks.STONE_CRAFTING_TABLE.get());
        // 错误合成台：「错误合成」分类的合成站

    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        // 转移：错误合成菜单，槽位 1-9 合成格 / 10 结果格 / 11-46 背包（与原版 CraftingMenu 一致）
        registration.addRecipeTransferHandler(new IRecipeTransferInfo<CraftingMenu, RecipeHolder<CraftingRecipe>>() {
            @Override
            public Class<? extends CraftingMenu> getContainerClass() {
                return ErrorCraftingMenu.class;
            }

            @Override
            public Optional<MenuType<CraftingMenu>> getMenuType() {
                return Optional.empty();
            }

            @Override
            public IRecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
                return errorType();
            }

            @Override
            public boolean canHandle(CraftingMenu container, RecipeHolder<CraftingRecipe> recipe) {
                return true;
            }

            @Override
            public List<Slot> getRecipeSlots(CraftingMenu container, RecipeHolder<CraftingRecipe> recipe) {
                return container.slots.subList(1, 10);
            }

            @Override
            public List<Slot> getInventorySlots(CraftingMenu container, RecipeHolder<CraftingRecipe> recipe) {
                return container.slots.subList(10, 46);
            }
        });
    }

    @Override
    public void registerAdvanced(IAdvancedRegistration registration) {
        registration.addRecipeManagerPlugin(new IRecipeManagerPlugin() {
            @Override
            public <V> List<IRecipeType<?>> getRecipeTypes(IFocus<V> focus) {
                return List.of(errorType());
            }

            @Override
            public <T, V> List<T> getRecipes(IRecipeType<T> recipeType, IFocus<V> focus) {
                return errorRecipesFor(recipeType, focus);
            }

            @Override
            public <T> List<T> getRecipes(IRecipeType<T> recipeType) {
                return errorRecipesFor(recipeType);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> errorRecipesFor(IRecipeType<T> recipeType) {
        IRecipeHolderType<CraftingRecipe> type = errorType();
        if (recipeType != type) {
            return List.of();
        }
        RecipeMap map = clientRecipeMap;
        if (map == null || map.values().isEmpty()) {
            return List.of();
        }
        List<RecipeHolder<CraftingRecipe>> recipes = map.values().stream()
                .filter(holder -> holder.value().getType() == ModRecipeTypes.ERROR_CRAFTING.get())
                .map(holder -> (RecipeHolder<CraftingRecipe>) holder)
                .toList();
        return (List<T>) recipes;
    }

    /** 带 focus 的查询：像原版分类那样，点哪个物品就只显示与该物品相关的配方。 */
    @SuppressWarnings("unchecked")
    private static <T, V> List<T> errorRecipesFor(IRecipeType<T> recipeType, IFocus<V> focus) {
        IRecipeHolderType<CraftingRecipe> type = errorType();
        if (recipeType != type) {
            return List.of();
        }
        RecipeMap map = clientRecipeMap;
        if (map == null || map.values().isEmpty()) {
            return List.of();
        }
        List<RecipeHolder<CraftingRecipe>> recipes = map.values().stream()
                .filter(holder -> holder.value().getType() == ModRecipeTypes.ERROR_CRAFTING.get())
                .map(holder -> (RecipeHolder<CraftingRecipe>) holder)
                .toList();
        if (focus != null) {
            recipes = recipes.stream()
                    .filter(holder -> recipeMatchesFocus(holder.value(), focus))
                    .toList();
        }
        return (List<T>) recipes;
    }

    private static boolean recipeMatchesFocus(CraftingRecipe recipe, IFocus<?> focus) {
        Object value = focus.getTypedValue().getIngredient();
        if (!(value instanceof ItemStack stack)) {
            return false;
        }
        if (focus.getRole() == RecipeIngredientRole.OUTPUT) {
            // 点击物品查配方来源：配方输出匹配该物品
            ItemStack result = resultOf(recipe);
            return !result.isEmpty() && ItemStack.isSameItem(result, stack);
        }
        if (focus.getRole() == RecipeIngredientRole.INPUT) {
            // 查看物品用途：配方任一输入 ingredient 匹配该物品
            for (Ingredient input : ingredientsOf(recipe)) {
                if (input.test(stack)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    private static ItemStack resultOf(CraftingRecipe recipe) {
        if (recipe instanceof ErrorCraftingRecipe error) {
            return error.resultItem();
        }
        if (recipe instanceof ErrorCraftingShapelessRecipe shapeless) {
            return shapeless.resultItem();
        }
        return ItemStack.EMPTY;
    }

    private static List<Ingredient> ingredientsOf(CraftingRecipe recipe) {
        if (recipe instanceof ShapedRecipe shaped) {
            return shaped.getIngredients().stream()
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
        }
        if (recipe instanceof ErrorCraftingShapelessRecipe shapeless) {
            return shapeless.ingredients();
        }
        return List.of();
    }
}
