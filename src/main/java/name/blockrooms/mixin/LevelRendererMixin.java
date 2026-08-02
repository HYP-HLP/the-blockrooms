package name.blockrooms.mixin;

import name.blockrooms.event.DynamicLightingHandler;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Brightness;
import net.minecraft.world.level.BlockAndTintGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer$BrightnessGetter;packedBrightness(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)I"),
        method = "getLightColor(Lnet/minecraft/client/renderer/LevelRenderer$BrightnessGetter;Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I")
    private static int redirectBrightness(LevelRenderer.BrightnessGetter instance, BlockAndTintGetter blockAndTintGetter, BlockPos blockPos) {
        int sky = Brightness.sky(instance.packedBrightness(blockAndTintGetter, blockPos));
        int original = Brightness.block(instance.packedBrightness(blockAndTintGetter, blockPos));
        int modified = DynamicLightingHandler.getLightWithoutOcclusion(blockAndTintGetter, blockPos);
        return Brightness.pack(Math.max(original, modified), sky);
    }
}
