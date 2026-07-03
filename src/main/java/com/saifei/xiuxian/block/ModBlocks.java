package com.saifei.xiuxian.block;

import com.saifei.xiuxian.XiuXianMod;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, XiuXianMod.MOD_ID);

    public static final RegistryObject<Block> LOW_LINGSHI_ORE = BLOCKS.register("low_lingshi_ore",
            () -> new Block(BlockBehaviour.Properties.of().strength(10.0f, 1200.0f).requiresCorrectToolForDrops().sound(Blocks.STONE.defaultBlockState().getSoundType())));
    public static final RegistryObject<Block> MID_LINGSHI_ORE = BLOCKS.register("mid_lingshi_ore",
            () -> new Block(BlockBehaviour.Properties.of().strength(15.0f, 1200.0f).requiresCorrectToolForDrops().sound(Blocks.STONE.defaultBlockState().getSoundType())));
    public static final RegistryObject<Block> HIGH_LINGSHI_ORE = BLOCKS.register("high_lingshi_ore",
            () -> new Block(BlockBehaviour.Properties.of().strength(25.0f, 1200.0f).requiresCorrectToolForDrops().sound(Blocks.STONE.defaultBlockState().getSoundType())));
    public static final RegistryObject<Block> SUPREME_LINGSHI_ORE = BLOCKS.register("supreme_lingshi_ore",
            () -> new Block(BlockBehaviour.Properties.of().strength(40.0f, 1200.0f).requiresCorrectToolForDrops().sound(Blocks.STONE.defaultBlockState().getSoundType())));
}