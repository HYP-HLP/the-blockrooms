package name.blockrooms.block.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class ErrorCraftingShapelessRecipe extends ShapelessRecipe {

    final String group;
    final CraftingBookCategory category;
    final ItemStack result;
    final List<Ingredient> ingredients;
    private @Nullable PlacementInfo placementInfo;
    private final boolean isSimple;

    public ErrorCraftingShapelessRecipe(String group, CraftingBookCategory category, ItemStack result, List<Ingredient> ingredients) {
        super(group, category, result, ingredients);
        this.group = group;
        this.category = category;
        this.result = result;
        this.ingredients = ingredients;
        this.isSimple = ingredients.stream().allMatch(Ingredient::isSimple);
    }

    @Override
    public RecipeSerializer<ShapelessRecipe> getSerializer() {
        return RecipeSerializer.SHAPELESS_RECIPE;
    }

    @Override
    public String group() {
        return this.group;
    }

    @Override
    public CraftingBookCategory category() {
        return this.category;
    }

    @Override
    public PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = PlacementInfo.create(this.ingredients);
        }

        return this.placementInfo;
    }

    public boolean matches(CraftingInput p_346123_, Level p_44263_) {
        if (p_346123_.ingredientCount() != this.ingredients.size()) {
            return false;
        } else if (!isSimple) {
            var nonEmptyItems = new java.util.ArrayList<ItemStack>(p_346123_.ingredientCount());
            for (var item : p_346123_.items())
                if (!item.isEmpty())
                    nonEmptyItems.add(item);
            return net.neoforged.neoforge.common.util.RecipeMatcher.findMatches(nonEmptyItems, this.ingredients) != null;
        } else {
            return p_346123_.size() == 1 && this.ingredients.size() == 1
                    ? this.ingredients.getFirst().test(p_346123_.getItem(0))
                    : p_346123_.stackedContents().canCraft(this, null);
        }
    }

    public ItemStack assemble(CraftingInput p_345555_, HolderLookup.Provider p_335725_) {
        return this.result.copy();
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(
                new ShapelessCraftingRecipeDisplay(
                        this.ingredients.stream().map(Ingredient::display).toList(),
                        new SlotDisplay.ItemStackSlotDisplay(this.result),
                        new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)
                )
        );
    }

    public static class Serializer implements RecipeSerializer<ErrorCraftingShapelessRecipe> {
        private static final MapCodec<ErrorCraftingShapelessRecipe> CODEC = RecordCodecBuilder.mapCodec(
                p_360072_ -> p_360072_.group(
                                Codec.STRING.optionalFieldOf("group", "").forGetter(p_301127_ -> p_301127_.group),
                                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(p_301133_ -> p_301133_.category),
                                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(p_301142_ -> p_301142_.result),
                                Codec.lazyInitialized(() -> Ingredient.CODEC.listOf(1, ShapedRecipePattern.getMaxHeight() * ShapedRecipePattern.getMaxWidth())).fieldOf("ingredients").forGetter(p_360071_ -> p_360071_.ingredients)
                        )
                        .apply(p_360072_, ErrorCraftingShapelessRecipe::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, ErrorCraftingShapelessRecipe> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8,
                p_360074_ -> p_360074_.group,
                CraftingBookCategory.STREAM_CODEC,
                p_360073_ -> p_360073_.category,
                ItemStack.STREAM_CODEC,
                p_360070_ -> p_360070_.result,
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()),
                p_360069_ -> p_360069_.ingredients,
                ErrorCraftingShapelessRecipe::new
        );

        @Override
        public MapCodec<ErrorCraftingShapelessRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ErrorCraftingShapelessRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
    @Override
    public RecipeType<CraftingRecipe> getType() {
        return ModRecipeTypes.ERROR_CRAFTING.get();
    }
}
