package com.saifei.xiuxian.item;

import com.saifei.xiuxian.XiuXianMod;
import com.saifei.xiuxian.block.ModBlocks;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, XiuXianMod.MOD_ID);

    // --- 四品灵石 ---
    // 下品：恢复 1 ~ 5 点
    public static final RegistryObject<Item> LOW_LINGSHI = ITEMS.register("low_lingshi",
            () -> new LingShiItem(new Item.Properties(), 1, 5));
    // 中品：恢复 10 ~ 20 点
    public static final RegistryObject<Item> MID_LINGSHI = ITEMS.register("mid_lingshi",
            () -> new LingShiItem(new Item.Properties(), 10, 20));
    // 上品：恢复 30 ~ 50 点
    public static final RegistryObject<Item> HIGH_LINGSHI = ITEMS.register("high_lingshi",
            () -> new LingShiItem(new Item.Properties(), 30, 50));
    // 极品：恢复 80 ~ 120 点
    public static final RegistryObject<Item> SUPREME_LINGSHI = ITEMS.register("supreme_lingshi",
            () -> new LingShiItem(new Item.Properties(), 80, 120));

    // --- 四品灵石矿石的方块物品 ---
    public static final RegistryObject<Item> LOW_LINGSHI_ORE_ITEM = ITEMS.register("low_lingshi_ore",
            () -> new BlockItem(ModBlocks.LOW_LINGSHI_ORE.get(), new Item.Properties()));
    public static final RegistryObject<Item> MID_LINGSHI_ORE_ITEM = ITEMS.register("mid_lingshi_ore",
            () -> new BlockItem(ModBlocks.MID_LINGSHI_ORE.get(), new Item.Properties()));
    public static final RegistryObject<Item> HIGH_LINGSHI_ORE_ITEM = ITEMS.register("high_lingshi_ore",
            () -> new BlockItem(ModBlocks.HIGH_LINGSHI_ORE.get(), new Item.Properties()));
    public static final RegistryObject<Item> SUPREME_LINGSHI_ORE_ITEM = ITEMS.register("supreme_lingshi_ore",
            () -> new BlockItem(ModBlocks.SUPREME_LINGSHI_ORE.get(), new Item.Properties()));

    // --- 蒲团的 ---
    public static final RegistryObject<Item> MEDITATION_MAT_ITEM = ITEMS.register("meditation_mat",
            () -> new BlockItem(ModBlocks.MEDITATION_MAT.get(), new Item.Properties()));

    // --- 炼化炉的 ---
    public static final RegistryObject<Item> REFINING_FURNACE_ITEM = ITEMS.register("refining_furnace",
            () -> new BlockItem(ModBlocks.REFINING_FURNACE.get(), new Item.Properties()));
}