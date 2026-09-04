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
import net.minecraft.world.level.material.MapColor;
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

    // ============ v1.4.1：灵草 / 仙草（8 种） ============
    // 回灵草：下品·恢复灵力，平原/森林草地
    public static final RegistryObject<Block> HUI_LING_CAO = BLOCKS.register("hui_ling_cao",
            () -> new LingCaoBlock(plantProperties(), Blocks.GRASS_BLOCK, Blocks.DIRT));
    // 凝神花：下品·清心凝神，森林/繁花森林/桦木森林
    public static final RegistryObject<Block> NING_SHEN_HUA = BLOCKS.register("ning_shen_hua",
            () -> new LingCaoBlock(plantProperties(), Blocks.GRASS_BLOCK, Blocks.DIRT));
    // 赤焰果：中品·火系，沙漠/恶地/savanna 及熔岩湖畔
    public static final RegistryObject<Block> CHI_YAN_GUO = BLOCKS.register("chi_yan_guo",
            () -> new LingCaoBlock(plantProperties(), Blocks.SAND, Blocks.RED_SAND));
    // 天山雪莲：中品·冰系，雪林/雪原/冰刺之地/裸岩山峰
    public static final RegistryObject<Block> TIAN_SHAN_XUE_LIAN = BLOCKS.register("tian_shan_xue_lian",
            () -> new LingCaoBlock(plantProperties(), Blocks.SNOW_BLOCK, Blocks.PACKED_ICE, Blocks.STONE));
    // 解毒藤：中品·解毒，沼泽
    public static final RegistryObject<Block> JIE_DU_TENG = BLOCKS.register("jie_du_teng",
            () -> new LingCaoBlock(plantProperties(), Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.MUD));
    // 玄铁灵芝：上品·疗伤，y<0 深层洞穴
    public static final RegistryObject<Block> XUAN_TIE_LING_ZHI = BLOCKS.register("xuan_tie_ling_zhi",
            () -> new LingCaoBlock(plantProperties(), Blocks.STONE, Blocks.DEEPSLATE, Blocks.TUFF, Blocks.DRIPSTONE_BLOCK, Blocks.MOSS_BLOCK, Blocks.SCULK));
    // 龙血草：上品·续命，海洋/深海洋/温水海洋海底
    public static final RegistryObject<Block> LONG_XUE_CAO = BLOCKS.register("long_xue_cao",
            () -> new LingCaoBlock(plantProperties(), Blocks.GRAVEL, Blocks.SAND, Blocks.CLAY));
    // 九转还魂草：极品·复活/突破，高山之巅稀有
    public static final RegistryObject<Block> JIU_ZHUAN_HUAN_HUN_CAO = BLOCKS.register("jiu_zhuan_huan_hun_cao",
            () -> new LingCaoBlock(plantProperties(), Blocks.STONE, Blocks.SNOW_BLOCK));

    private static BlockBehaviour.Properties plantProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .instabreak()
                .noCollission()
                .sound(SoundType.GRASS)
                .offsetType(BlockBehaviour.OffsetType.XZ);
    }
}