package name.blockrooms.client.renderer;

import name.blockrooms.Blockrooms;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ShulkerRenderer;
import net.minecraft.client.renderer.entity.state.ShulkerRenderState;
import net.minecraft.resources.Identifier;

/**
 * 黑石潜影贝渲染器：外壳为黑石纹理（见 assets/blockrooms/textures/entity/blackstone_shulker.png），
 * 其余渲染逻辑（模型、开合动画、附着面朝向）与原版潜影贝完全一致。
 */
public class BlackstoneShulkerRenderer extends ShulkerRenderer {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Blockrooms.MODID, "textures/entity/blackstone_shulker.png");

    public BlackstoneShulkerRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public Identifier getTextureLocation(ShulkerRenderState state) {
        return TEXTURE;
    }
}
