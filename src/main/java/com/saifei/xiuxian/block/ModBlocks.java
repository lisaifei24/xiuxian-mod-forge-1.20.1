package com.saifei.xiuxian.block;

import com.saifei.xiuxian.XiuXianMod;
import com.saifei.xiuxian.block.entity.ModBlockEntities;
import com.saifei.xiuxian.block.entity.RefiningFurnaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import com.saifei.xiuxian.block.MeditationMatBlock;

import javax.annotation.Nullable;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, XiuXianMod.MOD_ID);

    // 灵石矿石
    public static final RegistryObject<Block> LOW_LINGSHI_ORE = BLOCKS.register("low_lingshi_ore",
            () -> new Block(BlockBehaviour.Properties.of().strength(10.0f, 1200.0f).requiresCorrectToolForDrops().sound(Blocks.STONE.defaultBlockState().getSoundType())));
    public static final RegistryObject<Block> MID_LINGSHI_ORE = BLOCKS.register("mid_lingshi_ore",
            () -> new Block(BlockBehaviour.Properties.of().strength(15.0f, 1200.0f).requiresCorrectToolForDrops().sound(Blocks.STONE.defaultBlockState().getSoundType())));
    public static final RegistryObject<Block> HIGH_LINGSHI_ORE = BLOCKS.register("high_lingshi_ore",
            () -> new Block(BlockBehaviour.Properties.of().strength(25.0f, 1200.0f).requiresCorrectToolForDrops().sound(Blocks.STONE.defaultBlockState().getSoundType())));
    public static final RegistryObject<Block> SUPREME_LINGSHI_ORE = BLOCKS.register("supreme_lingshi_ore",
            () -> new Block(BlockBehaviour.Properties.of().strength(40.0f, 1200.0f).requiresCorrectToolForDrops().sound(Blocks.STONE.defaultBlockState().getSoundType())));

    // 蒲团
    public static final RegistryObject<Block> MEDITATION_MAT = BLOCKS.register("meditation_mat", MeditationMatBlock::new);

    // 炼化炉
    public static final RegistryObject<Block> REFINING_FURNACE = BLOCKS.register("refining_furnace", RefiningFurnaceBlock::new);
}