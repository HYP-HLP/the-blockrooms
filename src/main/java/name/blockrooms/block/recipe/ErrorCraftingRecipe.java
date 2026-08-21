package name.blockrooms.block.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

public class ErrorCraftingRecipe extends ShapedRecipe {
    final ItemStack result;

    public ErrorCraftingRecipe(ShapedRecipePattern pattern, ItemStack result) {
        super("error", CraftingBookCategory.MISC, pattern, result, false);
        this.result = result;
    }

    /** 合成产物（1.21.11 移除了 Recipe.getResultItem，这里显式暴露）。 */
    public ItemStack resultItem() {
        return result;
    }

    @Override
    public RecipeSerializer<? extends ShapedRecipe> getSerializer() {
        // 必须返回 mod 自己的 serializer：Recipe.STREAM_CODEC 按 serializer id 编解码，
        // 若返回原版 SHAPED_RECIPE，网络同步时客户端会用原版 ShapedRecipe.Serializer 解码，
        // 得到原版 ShapedRecipe（getType()=minecraft:crafting），错误配方会被塞进原版「合成」分类
        return ModRecipeTypes.ERROR_CRAFTING_SERIALIZER.get();
    }

    @Override
    public RecipeType<CraftingRecipe> getType() {
        return ModRecipeTypes.ERROR_CRAFTING.get();
    }

    public static class Serializer implements RecipeSerializer<ErrorCraftingRecipe> {
        public static final MapCodec<ErrorCraftingRecipe> CODEC = RecordCodecBuilder.mapCodec(
                i -> i.group(
                        ShapedRecipePattern.MAP_CODEC.forGetter(o -> o.pattern),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(o -> o.result)
                ).apply(i, ErrorCraftingRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, ErrorCraftingRecipe> STREAM_CODEC =
                StreamCodec.of(ErrorCraftingRecipe.Serializer::toNetwork, ErrorCraftingRecipe.Serializer::fromNetwork);

        public Serializer() {
        }

        public MapCodec<ErrorCraftingRecipe> codec() {
            return CODEC;
        }

        public StreamCodec<RegistryFriendlyByteBuf, ErrorCraftingRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static ErrorCraftingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            ShapedRecipePattern pattern = ShapedRecipePattern.STREAM_CODEC.decode(buffer);
            ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
            return new ErrorCraftingRecipe(pattern, result);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, ErrorCraftingRecipe recipe) {
            ShapedRecipePattern.STREAM_CODEC.encode(buffer, recipe.pattern);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
        }
    }
}
