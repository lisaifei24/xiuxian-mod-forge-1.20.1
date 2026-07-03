package com.saifei.xiuxian.block.entity;

import com.saifei.xiuxian.XiuXianMod;
import com.saifei.xiuxian.capability.CapabilityRegistration;
import com.saifei.xiuxian.item.ModItems;
import com.saifei.xiuxian.menu.RefiningFurnaceMenu;
import com.saifei.xiuxian.network.SyncCultivationPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class RefiningFurnaceBlockEntity extends BlockEntity implements MenuProvider {

    private final NonNullList<ItemStack> inputItems = NonNullList.withSize(9, ItemStack.EMPTY);
    private final NonNullList<ItemStack> outputItems = NonNullList.withSize(9, ItemStack.EMPTY);

    private int progress = 0;
    private static final int MAX_PROGRESS = 200; // 10秒 = 200 tick
    private UUID currentPlayerUUID = null;

    // 数据同步（给 GUI 显示进度条用的）
    private final ContainerData dataAccess = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> MAX_PROGRESS;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {
            if (index == 0) progress = value;
        }
        @Override public int getCount() { return 2; }
    };

    public RefiningFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REFINING_FURNACE_BE.get(), pos, state);
    }

    // ==================== 提供容器代理 ====================

    public Container getInputContainer() {
        return new Container() {
            @Override public int getContainerSize() { return inputItems.size(); }
            @Override public boolean isEmpty() { return inputItems.stream().allMatch(ItemStack::isEmpty); }
            @Override public ItemStack getItem(int slot) { return inputItems.get(slot); }
            @Override public ItemStack removeItem(int slot, int amount) { return ContainerHelper.removeItem(inputItems, slot, amount); }
            @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(inputItems, slot); }
            @Override public void setItem(int slot, ItemStack stack) { inputItems.set(slot, stack); setChanged(); }
            @Override public void setChanged() { RefiningFurnaceBlockEntity.this.setChanged(); }
            @Override public boolean stillValid(Player player) { return RefiningFurnaceBlockEntity.this.stillValid(player); }
            @Override public void clearContent() { inputItems.clear(); }
        };
    }

    public Container getOutputContainer() {
        return new Container() {
            @Override public int getContainerSize() { return outputItems.size(); }
            @Override public boolean isEmpty() { return outputItems.stream().allMatch(ItemStack::isEmpty); }
            @Override public ItemStack getItem(int slot) { return outputItems.get(slot); }
            @Override public ItemStack removeItem(int slot, int amount) { return ContainerHelper.removeItem(outputItems, slot, amount); }
            @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(outputItems, slot); }
            @Override public void setItem(int slot, ItemStack stack) { outputItems.set(slot, stack); setChanged(); }
            @Override public void setChanged() { RefiningFurnaceBlockEntity.this.setChanged(); }
            @Override public boolean stillValid(Player player) { return RefiningFurnaceBlockEntity.this.stillValid(player); }
            @Override public void clearContent() { outputItems.clear(); }
        };
    }

    // ==================== 1:100 核心炼化逻辑 ====================

    public static void tick(Level level, BlockPos pos, BlockState state, RefiningFurnaceBlockEntity be) {
        if (level.isClientSide) return;

        if (be.currentPlayerUUID == null) { be.progress = 0; return; }

        ServerPlayer player = level.getServer().getPlayerList().getPlayer(be.currentPlayerUUID);
        if (player == null) { be.currentPlayerUUID = null; be.progress = 0; return; }

        // 检查是否符合 1:100 比例 (100个同阶灵石) 并且有空间产出
        if (!be.hasValidInput() || !be.hasSpaceForOutput()) {
            be.progress = 0;
            return;
        }

        player.getCapability(CapabilityRegistration.CULTIVATION_CAPABILITY).ifPresent(cap -> {
            // 每秒（20 tick）触发一次灵力消耗
            if (be.progress % 20 == 0 && be.progress > 0) {
                int perSecondCost = switch (be.getCurrentRefiningType()) {
                    case 0 -> level.random.nextInt(6);   // 0-5
                    case 1 -> level.random.nextInt(51);  // 0-50
                    case 2 -> level.random.nextInt(101); // 0-100
                    default -> 0;
                };
                // 防止抽到0导致玩家觉得"没扣蓝"，强制最低消耗1点
                if (perSecondCost == 0) perSecondCost = 1;

                if (cap.getSpiritualPower() < perSecondCost) {
                    be.progress = 0;
                    return;
                }
                cap.addSpiritualPower(-perSecondCost);

                // ✅【新增】消耗灵力后立即同步给客户端，确保 HUD 显示正确
                XiuXianMod.NETWORK.send(PacketDistributor.PLAYER.with(() -> player),
                        new SyncCultivationPacket(cap.getSpiritualPower(), cap.getMaxSpiritualPower(), cap.getRealm()));
            }

            be.progress++;
            be.setChanged();

            if (be.progress >= MAX_PROGRESS) {
                be.progress = 0;
                be.refineItems();
            }
        });
    }

    private int getCurrentRefiningType() {
        int lowCount = 0, midCount = 0, highCount = 0;
        for (ItemStack stack : inputItems) {
            if (!stack.isEmpty()) {
                if (stack.getItem() == ModItems.LOW_LINGSHI.get()) {
                    lowCount += stack.getCount();
                } else if (stack.getItem() == ModItems.MID_LINGSHI.get()) {
                    midCount += stack.getCount();
                } else if (stack.getItem() == ModItems.HIGH_LINGSHI.get()) {
                    highCount += stack.getCount();
                }
            }
        }
        if (lowCount >= 100) return 0; // 下品 炼 中品
        if (midCount >= 100) return 1; // 中品 炼 上品
        if (highCount >= 100) return 2;// 上品 炼 极品
        return -1;
    }

    private boolean hasValidInput() {
        return getCurrentRefiningType() != -1;
    }

    private boolean hasSpaceForOutput() {
        for (ItemStack stack : outputItems) {
            if (stack.getCount() < stack.getMaxStackSize()) return true;
        }
        return false;
    }

    private void refineItems() {
        int type = getCurrentRefiningType();
        if (type == -1) return;

        ItemStack result = switch (type) {
            case 0 -> new ItemStack(ModItems.MID_LINGSHI.get());
            case 1 -> new ItemStack(ModItems.HIGH_LINGSHI.get());
            case 2 -> new ItemStack(ModItems.SUPREME_LINGSHI.get());
            default -> ItemStack.EMPTY;
        };
        ItemStack required = switch (type) {
            case 0 -> new ItemStack(ModItems.LOW_LINGSHI.get());
            case 1 -> new ItemStack(ModItems.MID_LINGSHI.get());
            case 2 -> new ItemStack(ModItems.HIGH_LINGSHI.get());
            default -> ItemStack.EMPTY;
        };
        if (result.isEmpty() || required.isEmpty()) return;

        // ✅ 精准跨槽位扣除 100 个灵石
        int remaining = 100;
        for (int i = 0; i < inputItems.size(); i++) {
            if (remaining <= 0) break;
            ItemStack stack = inputItems.get(i);
            if (!stack.isEmpty() && stack.getItem() == required.getItem()) {
                int toRemove = Math.min(stack.getCount(), remaining);
                stack.shrink(toRemove);
                remaining -= toRemove;
            }
        }
        // 如果扣除失败（理论上不存在），安全退出
        if (remaining > 0) return;

        // 产出对应的灵石
        for (int i = 0; i < outputItems.size(); i++) {
            ItemStack stack = outputItems.get(i);
            if (stack.isEmpty()) {
                outputItems.set(i, result.copy());
                break;
            } else if (stack.getItem() == result.getItem() && stack.getCount() < stack.getMaxStackSize()) {
                stack.grow(1);
                break;
            }
        }
        setChanged();
    }

    // ==================== MenuProvider ====================

    @Override
    public Component getDisplayName() {
        return Component.literal("炼化炉");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        this.currentPlayerUUID = player.getUUID();
        return new RefiningFurnaceMenu(containerId, inventory, this, this.dataAccess);
    }

    // ==================== NBT ====================

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, inputItems);
        ContainerHelper.saveAllItems(tag, outputItems);
        tag.putInt("Progress", progress);
        if (currentPlayerUUID != null) tag.putUUID("PlayerUUID", currentPlayerUUID);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, inputItems);
        ContainerHelper.loadAllItems(tag, outputItems);
        progress = tag.getInt("Progress");
        if (tag.contains("PlayerUUID")) currentPlayerUUID = tag.getUUID("PlayerUUID");
    }

    // ==================== 辅助方法 ====================

    public boolean stillValid(Player player) {
        return player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    public ContainerData getDataAccess() {
        return dataAccess;
    }
}