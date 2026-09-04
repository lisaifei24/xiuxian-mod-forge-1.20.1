package com.saifei.xiuxian.item;

import com.saifei.xiuxian.XiuXianMod;
import com.saifei.xiuxian.block.ModBlocks;
import com.saifei.xiuxian.skill.SkillType;
import net.minecraft.world.food.FoodProperties;
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
            () -> new TooltipBlockItem(ModBlocks.LOW_LINGSHI_ORE.get(), new Item.Properties(), "挖掘掉落：下品灵石"));
    public static final RegistryObject<Item> MID_LINGSHI_ORE_ITEM = ITEMS.register("mid_lingshi_ore",
            () -> new TooltipBlockItem(ModBlocks.MID_LINGSHI_ORE.get(), new Item.Properties(), "挖掘掉落：中品灵石"));
    public static final RegistryObject<Item> HIGH_LINGSHI_ORE_ITEM = ITEMS.register("high_lingshi_ore",
            () -> new TooltipBlockItem(ModBlocks.HIGH_LINGSHI_ORE.get(), new Item.Properties(), "挖掘掉落：上品灵石"));
    public static final RegistryObject<Item> SUPREME_LINGSHI_ORE_ITEM = ITEMS.register("supreme_lingshi_ore",
            () -> new TooltipBlockItem(ModBlocks.SUPREME_LINGSHI_ORE.get(), new Item.Properties(), "挖掘掉落：极品灵石"));

    // --- 蒲团的 ---
    public static final RegistryObject<Item> MEDITATION_MAT_ITEM = ITEMS.register("meditation_mat",
            () -> new TooltipBlockItem(ModBlocks.MEDITATION_MAT.get(), new Item.Properties(),
                    "坐在上面可冥想修炼，缓慢恢复灵力"));

    // --- 炼化炉的 ---
    public static final RegistryObject<Item> REFINING_FURNACE_ITEM = ITEMS.register("refining_furnace",
            () -> new TooltipBlockItem(ModBlocks.REFINING_FURNACE.get(), new Item.Properties(),
                    "用于炼化合成：100:1 灵石升阶，灵石+灵石矿可炼制突破丹药"));

    // ==================== 第二阶段：突破丹药 ====================
    // 聚气丹：凡人 → 炼气，食用恢复 20~40 灵力，突破成功率 +30%
    public static final RegistryObject<Item> JUNQI_PILL = ITEMS.register("junqi_pill",
            () -> new BreakthroughPillItem(new Item.Properties(), "凡人→炼气", 20, 40, 0.30));
    // 筑基丹：炼气 → 筑基，食用恢复 60~100 灵力，突破成功率 +40%
    public static final RegistryObject<Item> ZHUJI_PILL = ITEMS.register("zhuji_pill",
            () -> new BreakthroughPillItem(new Item.Properties(), "炼气→筑基", 60, 100, 0.40));
    // 结丹丹：筑基 → 金丹，食用恢复 150~250 灵力，突破成功率 +50%
    public static final RegistryObject<Item> JIEDAN_PILL = ITEMS.register("jiedan_pill",
            () -> new BreakthroughPillItem(new Item.Properties(), "筑基→金丹", 150, 250, 0.50));

    // ==================== 第二阶段：功法卷轴 ====================
    public static final RegistryObject<Item> YUJIAN_SCROLL = ITEMS.register("yujian_scroll",
            () -> new SkillScrollItem(new Item.Properties(), SkillType.YUJIAN_SHU));
    public static final RegistryObject<Item> HUOQIU_SCROLL = ITEMS.register("huoqiu_scroll",
            () -> new SkillScrollItem(new Item.Properties(), SkillType.HUOQIU_SHU));
    public static final RegistryObject<Item> HUJI_SCROLL = ITEMS.register("huji_scroll",
            () -> new SkillScrollItem(new Item.Properties(), SkillType.HUTI_JINGUANG));
}