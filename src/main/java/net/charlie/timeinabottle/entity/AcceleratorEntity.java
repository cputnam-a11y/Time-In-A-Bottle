package net.charlie.timeinabottle.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.charlie.timeinabottle.TimeInABottle;

import java.util.logging.Logger;

@SuppressWarnings("unchecked")
public class AcceleratorEntity extends Entity {
    public static final TagKey<Block> ACCELERATION_BLACKLIST = TagKey.of(
            RegistryKeys.BLOCK,
            new Identifier(TimeInABottle.MOD_ID, "acceleration_blacklist")
    );
    public static final TrackedData<Integer> TIME_RATE = DataTracker.registerData(
            AcceleratorEntity.class,
            TrackedDataHandlerRegistry.INTEGER
    );
    private int remainingTime;
    public AcceleratorEntity(EntityType<? extends Entity> type, World world ) {
        super(type, world);
    }
    public AcceleratorEntity(World world, BlockPos target) {
        super(TimeInABottle.ACCELERATOR, world);
        this.noClip = true;
        this.setPos(target.getX(), target.getY(), target.getZ());
        this.updateTrackedPosition(target.getX(), target.getY(), target.getZ());
    }
    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient()) {
            return;
        }
        BlockPos pos = this.getBlockPos();
        BlockEntityTicker<BlockEntity> ticker = null;
        BlockState state = this.getWorld().getBlockState(pos);
        BlockEntity be = this.getWorld().getBlockEntity(pos);
        if (!state.isIn(ACCELERATION_BLACKLIST)) {
            Block var6 = state.getBlock();
            if (var6 instanceof BlockEntityProvider provider) {
                if (be != null) {
                    ticker = (BlockEntityTicker<BlockEntity>) provider.getTicker(this.getWorld(), state, be.getType());
                }
            }
            for(int i = 0; i < this.getTimeRate(); ++i) {
                if (ticker != null) {
                    ticker.tick(this.getWorld(), pos, state, be);
                }
                if (this.getWorld().random.nextInt(1365) == 0) {
                    BlockState targetBlock = this.getWorld().getBlockState(pos);
                    if (targetBlock.getBlock().hasRandomTicks(targetBlock)) {
                        targetBlock.randomTick((ServerWorld) this.getWorld(), pos, this.getWorld().random);
                    }
                }
            }
        }
        --this.remainingTime;
        if (this.remainingTime <= 0) {
            this.discard();
        }
    }
    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(TIME_RATE, 1);
    }
    @Override
    protected void readCustomDataFromNbt(NbtCompound tag) {
        this.remainingTime = tag.getInt("remainingTime");
        this.setTimeRate(tag.getInt("timeRate"));
    }
    @Override
    protected void writeCustomDataToNbt(NbtCompound tag) {
        tag.putInt("remainingTime", this.remainingTime);
        tag.putInt("timeRate", this.getTimeRate());
    }
    @SuppressWarnings("RedundantMethodOverride")
    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }
    public void setTimeRate(int timeRate) {
        this.dataTracker.set(TIME_RATE, timeRate);
    }
    public int getTimeRate() {
        return this.dataTracker.get(TIME_RATE);
    }
    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }
    public int getRemainingTime() {
        return this.remainingTime;
    }
}
