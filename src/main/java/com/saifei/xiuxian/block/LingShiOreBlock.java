package com.saifei.xiuxian.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class LingShiOreBlock extends Block {
    public LingShiOreBlock() {
        // 1.20.1 不再使用 Material，直接用 Properties.of()
        // getSoundType() 需要传入 State，官方写法是 Blocks.STONE.defaultBlockState().getSoundType()
        super(BlockBehaviour.Properties.of()
                .strength(10.0f, 1200.0f) // 硬度
                .requiresCorrectToolForDrops() // 必须用正确工具才能掉落
                .sound(Blocks.STONE.defaultBlockState().getSoundType()) // 声音
        );
    }
}