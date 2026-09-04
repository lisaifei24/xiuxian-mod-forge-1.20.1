package com.saifei.xiuxian.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 带中文说明(灰色 tooltip)的方块物品。
 * 用于灵石矿、蒲团、炼化炉等需要向玩家说明用法的方块物品，
 * 与丹药/功法卷轴保持统一的灰色文字风格（对齐 style=tooltip 风格）。
 */
public class TooltipBlockItem extends BlockItem {

    private final String[] tooltips;

    public TooltipBlockItem(Block block, Properties properties, String... tooltips) {
        super(block, properties);
        this.tooltips = tooltips;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        for (String line : tooltips) {
            tooltip.add(Component.literal("§7" + line).withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
