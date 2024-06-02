package net.charlie.timeinabottle.item;

import net.charlie.timeinabottle.TimeInABottle;
import net.charlie.timeinabottle.entity.AcceleratorEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;
import java.util.Objects;

public class TimeInABottleItem extends Item {
    private static final String DATA_KEY = "timeData";
    private static final String STORED_KEY = "storedTime";
    private static final SoundEvent SOUND = SoundEvents.BLOCK_NOTE_BLOCK_HARP.value();
    public TimeInABottleItem() {
        super((new FabricItemSettings()).maxCount(1).rarity(Rarity.UNCOMMON));
    }
    public ActionResult useOnBlock(ItemUsageContext ctx) {
        World world = ctx.getWorld();
        if (world.isClient()) {
            return ActionResult.PASS;
        }
        PlayerEntity player = ctx.getPlayer();
        BlockPos pos = ctx.getBlockPos();
        NbtCompound nbt = ctx.getStack().getSubNbt(DATA_KEY);
        Box box = new Box(
                new Vec3d(
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5
                ),
                new Vec3d(
                        pos.getX() + 1.5,
                        pos.getY() + 1.5,
                        pos.getZ() + 1.5
                )).shrink(0.2, 0.2, 0.2);
        List<AcceleratorEntity> accelerators =
                world.getEntitiesByClass(
                        AcceleratorEntity.class,
                        box,
                        e -> true
                );
        if (accelerators.isEmpty()) {
            assert nbt != null;
            int timeAvailable = nbt.getInt(STORED_KEY);
            if (timeAvailable >= 20 * TimeInABottle.config.getDuration()) {
                assert player != null;
                if (!player.getAbilities().creativeMode)
                    nbt.putInt(
                            STORED_KEY,
                            timeAvailable - 20 * TimeInABottle.config.getDuration()
                    );
                AcceleratorEntity accelerator = new AcceleratorEntity(world, pos);
                accelerator.setTimeRate(1);
                accelerator.setRemainingTime(20 * TimeInABottle.config.getDuration());
                accelerator.setBoundingBox(box);
                world.spawnEntity(accelerator);
                playSound(world, pos, accelerator.getTimeRate());
                return ActionResult.SUCCESS;
            }
        } else {
            AcceleratorEntity accelerator = accelerators.get(0);
            int curRate = accelerator.getTimeRate();
            int usedUp = 20 * TimeInABottle.config.getDuration() - accelerator.getRemainingTime();
            int[] speedLevels = TimeInABottle.config.getSpeedLevels();
            if (curRate < speedLevels[speedLevels.length - 1]) {
                int curIndex = ArrayUtils.indexOf(speedLevels, curRate);
                int next = speedLevels[curIndex + 1];
                // 320 * 30
                int timeRequired = (next / 2 * 20 * TimeInABottle.config.getDuration()) + 1400;
                assert nbt != null;
                int timeAvailable = nbt.getInt(STORED_KEY);
                if (timeAvailable >= timeRequired || player.getAbilities().creativeMode) {
                    int added = (next * usedUp - curRate * usedUp) / next;
                    assert player != null;
                    if (!player.getAbilities().creativeMode) {
                        nbt.putInt(STORED_KEY, timeAvailable - timeRequired);
                        player.setStackInHand(ctx.getHand(), ctx.getStack());
                    }
                    accelerator.setTimeRate(next);
                    accelerator.setRemainingTime(accelerator.getRemainingTime() + added);
                    playSound(world, pos, next);
                    return ActionResult.SUCCESS;
                }
            }
        }
        return super.useOnBlock(ctx);
    }
    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        if (world.isClient()) {
            return;
        }
        int time = TimeInABottle.config.getTimeSecond();
        if (world.getTime() % 20L == 0L) {
            NbtCompound timeData = stack.getSubNbt(DATA_KEY);
            if (timeData == null) {
                timeData = new NbtCompound();
                stack.setSubNbt(DATA_KEY, timeData);
                timeData.putInt(STORED_KEY, 0);
            }
            assert timeData != null;
            if ((long) timeData.getInt(STORED_KEY) < TimeInABottle.config.getMaxTime()) {
                timeData.putInt(STORED_KEY, timeData.getInt(STORED_KEY) + time);
            }
        }
        if (world.getTime() % 60L == 0L && entity instanceof PlayerEntity player) {
            for (int i = 0; i < player.getInventory().size(); ++i) {
                ItemStack other = player.getInventory().getStack(i);
                if (other.isOf(stack.getItem()) && other != stack) {
                    NbtCompound duplicate = other.getSubNbt(DATA_KEY);
                    NbtCompound original = stack.getSubNbt(DATA_KEY);
                    assert duplicate != null;
                    assert original != null;
                    int duplicateTime = duplicate.getInt(STORED_KEY);
                    int originalTime = original.getInt(STORED_KEY);
                    if (originalTime < duplicateTime) {
                        original.putInt(STORED_KEY, 0);
                    }
                }
            }
        }
    }
    @Environment(EnvType.CLIENT)
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
        NbtCompound timeData = stack.getSubNbt("timeData");
        int storedTime = timeData == null ? 0 : timeData.getInt("storedTime");
        int storedSeconds = storedTime / 20;
        int hours = storedSeconds / 3600;
        int minutes = storedSeconds % 3600 / 60;
        int seconds = storedSeconds % 60;
        //noinspection SpellCheckingInspection
        tooltip.add(Text.translatable( "item.timeinabottle.time_in_a_bottle.tooltip", new Object[]{hours, minutes, seconds}).formatted(Formatting.AQUA));
    }
    public boolean allowNbtUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
        return false;
    }
    protected static void playSound(World world, BlockPos pos, int nextRate) {
        switch (nextRate) {
            case 1:
                world.playSound(null, pos, SOUND, SoundCategory.BLOCKS, 0.5F, 0.749154F);
                break;
            case 2:
                world.playSound(null, pos, SOUND, SoundCategory.BLOCKS, 0.5F, 0.793701F);
                break;
            case 4:
            case 32:
                world.playSound(null, pos, SOUND, SoundCategory.BLOCKS, 0.5F, 0.890899F);
                break;
            case 8:
                world.playSound(null, pos, SOUND, SoundCategory.BLOCKS, 0.5F, 1.059463F);
                break;
            case 16:
                world.playSound(null, pos, SOUND, SoundCategory.BLOCKS, 0.5F, 0.943874F);
                break;
            default:
                world.playSound(null, pos, SOUND, SoundCategory.BLOCKS, 0.5F, 1.0F);
        }
    }
}
