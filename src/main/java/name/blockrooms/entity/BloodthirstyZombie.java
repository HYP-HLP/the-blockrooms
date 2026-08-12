package name.blockrooms.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;

/**
 * 嗜血僵尸：画廊维度的怪物。
 * 模型与渲染沿用原版僵尸（渲染器注册见 BlockroomsClient）；
 * 生命值 / 攻击 / 移速从 Config 读取，可直接在配置文件里调整
 * （run/config/blockrooms-common.toml）。
 */
public class BloodthirstyZombie extends Zombie {
    public BloodthirstyZombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    /** 属性构建：在原版僵尸基础上覆盖 Config 中的数值 */
    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 60)
                .add(Attributes.ATTACK_DAMAGE, 10)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }
}
