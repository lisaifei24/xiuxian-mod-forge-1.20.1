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

    // --- 四品灵石：功能分工（下品=灵力补剂/中品=低阶突破媒介/上品=炼丹炼化材料/极品=渡劫护体+顶级突破保底） ---
    // 下品：基础灵力补剂（5%），量大易得，蕴含灵力少
    public static final RegistryObject<Item> LOW_LINGSHI = ITEMS.register("low_lingshi",
            () -> new LingShiItem(new Item.Properties(), 5, "基础灵力补剂，量大易得，蕴含灵力少"));
    // 中品：低阶突破媒介（10%）
    public static final RegistryObject<Item> MID_LINGSHI = ITEMS.register("mid_lingshi",
            () -> new LingShiItem(new Item.Properties(), 10, "低阶突破媒介：凡人→炼气、炼气→筑基 突破保底"));
    // 上品：炼丹炼化核心材料（20%）
    public static final RegistryObject<Item> HIGH_LINGSHI = ITEMS.register("high_lingshi",
            () -> new LingShiItem(new Item.Properties(), 20, "炼丹与炼化核心材料"));
    // 极品：渡劫护体（40%）
    public static final RegistryObject<Item> SUPREME_LINGSHI = ITEMS.register("supreme_lingshi",
            () -> new LingShiItem(new Item.Properties(), 40, "渡劫护体：可抵消一道天雷；元婴及以上突破保底"));

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
                    "用于炼化合成：5:1 灵石升阶（下→中→上→极）；灵石+灵石矿可炼制突破丹药"));

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

    // ============ 第三阶段：进阶丹药链 ============
    // 元婴丹：金丹 → 元婴，食用恢复 300~500 灵力，突破成功率 +55%
    public static final RegistryObject<Item> YUANYING_PILL = ITEMS.register("yuanying_pill",
            () -> new BreakthroughPillItem(new Item.Properties(), "金丹→元婴", 300, 500, 0.55));
    // 化神丹：元婴 → 化神，食用恢复 600~1000 灵力，突破成功率 +60%
    public static final RegistryObject<Item> HUASHEN_PILL = ITEMS.register("huashen_pill",
            () -> new BreakthroughPillItem(new Item.Properties(), "元婴→化神", 600, 1000, 0.60));
    // 合体丹：化神 → 合体，食用恢复 1000~1600 灵力，突破成功率 +65%
    public static final RegistryObject<Item> HETI_PILL = ITEMS.register("heti_pill",
            () -> new BreakthroughPillItem(new Item.Properties(), "化神→合体", 1000, 1600, 0.65));
    // 大乘丹：合体 → 大乘，食用恢复 1600~2500 灵力，突破成功率 +70%
    public static final RegistryObject<Item> DACHENG_PILL = ITEMS.register("dacheng_pill",
            () -> new BreakthroughPillItem(new Item.Properties(), "合体→大乘", 1600, 2500, 0.70));
    // 渡劫丹：大乘 → 渡劫，食用恢复 2500~4000 灵力，突破成功率 +75%
    public static final RegistryObject<Item> DUJIE_PILL = ITEMS.register("dujie_pill",
            () -> new BreakthroughPillItem(new Item.Properties(), "大乘→渡劫", 2500, 4000, 0.75));
    // 真仙丹：渡劫 → 真仙，食用恢复 4000~6000 灵力，突破成功率 +80%
    public static final RegistryObject<Item> ZHENXIAN_PILL = ITEMS.register("zhenxian_pill",
            () -> new BreakthroughPillItem(new Item.Properties(), "渡劫→真仙", 4000, 6000, 0.80));

    // ==================== 第二阶段：功法卷轴 ====================
    public static final RegistryObject<Item> YUJIAN_SCROLL = ITEMS.register("yujian_scroll",
            () -> new SkillScrollItem(new Item.Properties(), SkillType.YUJIAN_SHU));
    public static final RegistryObject<Item> HUOQIU_SCROLL = ITEMS.register("huoqiu_scroll",
            () -> new SkillScrollItem(new Item.Properties(), SkillType.HUOQIU_SHU));
    public static final RegistryObject<Item> HUJI_SCROLL = ITEMS.register("huji_scroll",
            () -> new SkillScrollItem(new Item.Properties(), SkillType.HUTI_JINGUANG));
}