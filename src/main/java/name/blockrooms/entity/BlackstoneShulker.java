package name.blockrooms.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.level.Level;

public class BlackstoneShulker extends Shulker {

    public BlackstoneShulker(EntityType<? extends BlackstoneShulker> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean hurtServer(ServerLevel p_376092_, DamageSource p_376565_, float p_376357_) {
        Entity e = p_376565_.getDirectEntity();
        if(e instanceof LivingEntity le){
            if(!le.getMainHandItem().is(ItemTags.PICKAXES))return false;
        }
        return super.hurtServer(p_376092_, p_376565_, p_376357_);
    }


}
