package name.blockrooms.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class ItemProjectile extends ArrowLikeProjectile {
    protected boolean updateRenderState;
    private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK_ID = SynchedEntityData.defineId(
            ItemProjectile.class, EntityDataSerializers.ITEM_STACK
    ); private static final EntityDataAccessor<Byte> DATA_ITEM_DISPLAY_ID = SynchedEntityData.defineId(ItemProjectile.class, EntityDataSerializers.BYTE);
    private ItemRenderState itemRenderState;
    private ItemStack getItemStack() {
        return this.entityData.get(DATA_ITEM_STACK_ID);
    }
    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> p_277793_) {
        super.onSyncedDataUpdated(p_277793_);
        if (DATA_ITEM_STACK_ID.equals(p_277793_) || DATA_ITEM_DISPLAY_ID.equals(p_277793_)) {
            this.updateRenderState = true;
        }
    }
    public ItemProjectile(EntityType<? extends Projectile> p_37248_, Level p_37249_) {
        super(p_37248_, p_37249_);

        setItemTransform(ItemDisplayContext.FIXED);
    }
    private ItemProjectile(Level level, Entity owner, ItemStack stack){
        this(ModEntities.ITEM_PROJECTILE.get(), level);
        setOwner(owner);
        updateItemStack(stack);
        setItemTransform(ItemDisplayContext.FIXED);
        setPos(owner.getX(), owner.getY() - 0.1f, owner.getZ());
    }
    public static ItemProjectile of(Level level, Entity owner, ItemStack stack){
        return new ItemProjectile(level, owner, stack);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ITEM_STACK_ID, ItemStack.EMPTY);
        builder.define(DATA_ITEM_DISPLAY_ID, ItemDisplayContext.FIXED.getId());
    }
    public ItemRenderState itemRenderState() {
        return this.itemRenderState;
    }
    private void setItemTransform(ItemDisplayContext itemTransform) {
        this.entityData.set(DATA_ITEM_DISPLAY_ID, itemTransform.getId());
    }

    private ItemDisplayContext getItemTransform() {
        return ItemDisplayContext.BY_ID.apply(this.entityData.get(DATA_ITEM_DISPLAY_ID));
    }
    @Override
    public void tick() {
        boolean flag = !this.noPhysics;
        if(updateRenderState){
            updateRenderSubState();
            updateRenderState = false;
        }
        super.tick();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if(this.level() instanceof ServerLevel sl){
            ItemEntity entity = new ItemEntity(sl, this.getX(), this.getY(), this.getZ(), this.getItemStack());
            sl.addFreshEntity(entity);
        }

        this.discard();

    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if(this.level() instanceof ServerLevel sl){
            ItemEntity entity = new ItemEntity(sl, result.getEntity().getX(), result.getEntity().getY(), result.getEntity().getZ(), this.getItemStack());
            sl.addFreshEntity(entity);
        }
        this.discard();
    }

    public void updateItemStack(ItemStack stack) {
        this.setItemStack(stack);
        this.updateRenderState = true;
    }

    protected void updateRenderSubState() {
        ItemStack itemstack = this.getItemStack();
        itemstack.setEntityRepresentation(this);
        this.itemRenderState = new ItemRenderState(itemstack, getItemTransform());
    }

    public void setItemStack(ItemStack stack){
        this.entityData.set(DATA_ITEM_STACK_ID, stack);
    }
    public record ItemRenderState(ItemStack itemStack, ItemDisplayContext itemTransform) {
    }
    @Override
    protected double getDefaultGravity() {
        return 0.05;
    }
}

