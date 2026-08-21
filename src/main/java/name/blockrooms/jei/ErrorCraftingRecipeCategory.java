package name.blockrooms.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import name.blockrooms.block.ModBlocks;
import name.blockrooms.block.recipe.ErrorCraftingRecipe;
import name.blockrooms.block.recipe.ErrorCraftingShapelessRecipe;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JEI「错误合成」分类：独立展示错误合成台的专属配方（与原版合成分类分开），
 * 布局与原版 3x3 合成一致（输入 3x3 格 + 输出格），便于一键转移。
 */
public class ErrorCraftingRecipeCategory implements IRecipeCategory<RecipeHolder<CraftingRecipe>> {
    private final IDrawable icon;
    private final IDrawable arrow;

    public ErrorCraftingRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.ERROR_CRAFTING_TABLE.get()));
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public IRecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
        return BlockroomsJeiPlugin.errorType();
    }

    @Override
    public Component getTitle() {
        return Component.translatable("blockrooms.jei.category.error_crafting");
    }

    @Override
    public int getWidth() {
        return 116;
    }

    @Override
    public int getHeight() {
        return 54;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<CraftingRecipe> recipe, IFocusGroup focuses) {
        CraftingRecipe value = recipe.value();

        if (value instanceof ShapedRecipe shaped) {

            // 有序配方：按 3x3 网格逐格摆放；空气格也保留槽位背景（不消失），方便对照原版合成
            int width = shaped.getWidth();
            int height = shaped.getHeight();
            List<Optional<net.minecraft.world.item.crafting.Ingredient>> ingredients = shaped.getIngredients();
            var inputSlots = createInputSlots(builder, width, height);
            int i = 0;

            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    Optional<net.minecraft.world.item.crafting.Ingredient> input = ingredients.get(i);
                    mezz.jei.api.gui.builder.IRecipeSlotBuilder slot = inputSlots.get(getCraftingIndex(i, width, height));
                    if(input.isEmpty()) slot.add(ItemStack.EMPTY);
                    else input.ifPresent(slot::add);
                    i++;
                }
            }
        } else if (value instanceof ErrorCraftingShapelessRecipe shapeless) {
            var inputSlots = createInputSlots(builder, 0, 0);
            // 无序配方：从左到右、从上到下按 3 列流式填充（原版 JEI 无序配方同样式）
            List<net.minecraft.world.item.crafting.Ingredient> ingredients = shapeless.ingredients();
            for (int i = 0; i < ingredients.size(); i++) {
                net.minecraft.world.item.crafting.Ingredient input = ingredients.get(i);
                mezz.jei.api.gui.builder.IRecipeSlotBuilder slot =
                        inputSlots.get(i);
                if (!input.isEmpty()) {
                    slot.add(input);
                }
            }
        }
        // 错误配方自带公开的 result 字段（ErrorCraftingRecipe / ErrorCraftingShapelessRecipe）
        ItemStack result;
        if (value instanceof ErrorCraftingRecipe error) {
            result = error.resultItem();
        } else if (value instanceof ErrorCraftingShapelessRecipe shapeless) {
            result = shapeless.resultItem();
        } else {
            result = ItemStack.EMPTY;
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 19)
                .add(result)
                .setOutputSlotBackground();
    }
    private static List<IRecipeSlotBuilder> createInputSlots(IRecipeLayoutBuilder builder, int width, int height) {
        if (width <= 0 || height <= 0) {
            builder.setShapeless();
        }

        List<IRecipeSlotBuilder> inputSlots = new ArrayList<>();
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 3; ++x) {
                IRecipeSlotBuilder slot = builder.addInputSlot(x * 18 + 1, y * 18 + 1)
                        .setStandardSlotBackground();
                inputSlots.add(slot);
            }
        }
        return inputSlots;
    }
    @Override
    public void draw(RecipeHolder<CraftingRecipe> recipe, IRecipeSlotsView slotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 60, 19);
    }

    private static int getCraftingIndex(int i, int width, int height) {
        int index;
        if (width == 1) {
            if (height == 3) {
                index = (i * 3) + 1;
            } else if (height == 2) {
                index = (i * 3) + 1;
            } else {
                index = 4;
            }
        } else if (height == 1) {
            index = i + 3;
        } else if (width == 2) {
            index = i;
            if (i > 1) {
                index++;
                if (i > 3) {
                    index++;
                }
            }
        } else if (height == 2) {
            index = i + 3;
        } else {
            index = i;
        }
        return index;
    }
}
