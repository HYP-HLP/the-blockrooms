package name.blockrooms.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import name.blockrooms.entity.projectiles.BlockProjectile;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class BlockProjectileRenderer extends EntityRenderer<BlockProjectile, BlockProjectileRenderState> {
    public BlockProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void submit(BlockProjectileRenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        if (renderState != null && renderState.hasSubState()) {
            super.submit(renderState, poseStack, nodeCollector, cameraRenderState);
            this.submitInner(renderState, poseStack, nodeCollector, renderState.lightCoords);
        }
    }

    public void submitInner(BlockProjectileRenderState p_432901_, PoseStack p_434089_, SubmitNodeCollector p_433174_, int p_435266_) {
        p_433174_.submitBlock(p_434089_, p_432901_.blockRenderState.blockState(), p_435266_, OverlayTexture.NO_OVERLAY, p_432901_.outlineColor);
    }
    @Override
    public BlockProjectileRenderState createRenderState() {
        return new BlockProjectileRenderState();
    }
    @Override
    public void extractRenderState(BlockProjectile p_362697_, BlockProjectileRenderState p_363759_, float p_360854_) {
        super.extractRenderState(p_362697_, p_363759_, p_360854_);
        p_363759_.blockRenderState = p_362697_.blockRenderState();
    }
}
