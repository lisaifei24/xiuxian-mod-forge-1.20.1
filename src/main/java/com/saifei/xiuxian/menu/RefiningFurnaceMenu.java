package com.saifei.xiuxian.menu;

import com.saifei.xiuxian.block.entity.RefiningFurnaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public class RefiningFurnaceMenu extends AbstractContainerMenu {
    private final RefiningFurnaceBlockEntity blockEntity;
    private final ContainerData dataAccess;
    private final BlockPos pos;
    private final Container inputContainer;
    private final Container outputContainer;

    // 服务器构造
    public RefiningFurnaceMenu(int id, Inventory inv, RefiningFurnaceBlockEntity be, ContainerData data) {
        super(ModMenuTypes.REFINING_FURNACE_MENU.get(), id);
        this.blockEntity = be;
        this.dataAccess = data;
        this.pos = be.getBlockPos();
        this.inputContainer = be.getInputContainer();   // 直接使用方块实体
        this.outputContainer = be.getOutputContainer();
        initSlots(inv);
        addDataSlots(dataAccess);
    }

    // 客户端构造（仅显示用，使用虚拟容器）
    public RefiningFurnaceMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        super(ModMenuTypes.REFINING_FURNACE_MENU.get(), id);
        this.blockEntity = null;
        this.pos = extraData.readBlockPos();
        this.dataAccess = new SimpleContainerData(2);
        this.inputContainer = new SimpleContainer(9);
        this.outputContainer = new SimpleContainer(9);
        initSlots(inv);
        addDataSlots(dataAccess);
    }

    private void initSlots(Inventory inv) {
        // ===== 左侧 3x3 输入槽：对齐贴图 X=17, Y=17 =====
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                int index = x + y * 3;
                this.addSlot(new Slot(inputContainer, index, 18 + x * 18, 18 + y * 18));
            }
        }
        // ===== 右侧 3x3 输出槽：对齐贴图 X=107, Y=17 =====
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                int index = x + y * 3;
                this.addSlot(new Slot(outputContainer, index, 108 + x * 18, 18 + y * 18) {
                    @Override public boolean mayPlace(ItemStack stack) { return false; }
                });
            }
        }
        // 玩家主背包
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                this.addSlot(new Slot(inv, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
            }
        }
        // 玩家快捷栏
        for (int x = 0; x < 9; x++) {
            this.addSlot(new Slot(inv, x, 8 + x * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (slotIndex >= 9 && slotIndex < 18) {
            if (!this.moveItemStackTo(stack, 18, 54, true)) return ItemStack.EMPTY;
        } else if (slotIndex >= 18) {
            if (!this.moveItemStackTo(stack, 0, 9, false)) return ItemStack.EMPTY;
        } else {
            if (!this.moveItemStackTo(stack, 18, 54, false)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        if (blockEntity != null) {
            return blockEntity.stillValid(player);
        } else {
            return player.level().getBlockEntity(pos) instanceof RefiningFurnaceBlockEntity
                    && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
        }
    }

    public ContainerData getDataAccess() { return dataAccess; }
}