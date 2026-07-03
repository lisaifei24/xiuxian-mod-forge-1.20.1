package com.saifei.xiuxian;

import com.mojang.logging.LogUtils;
import com.saifei.xiuxian.block.ModBlocks;
import com.saifei.xiuxian.block.entity.ModBlockEntities;
import com.saifei.xiuxian.capability.CapabilityRegistration;
import com.saifei.xiuxian.entity.ModEntities;
import com.saifei.xiuxian.item.ModItems;
import com.saifei.xiuxian.menu.ModMenuTypes;
import com.saifei.xiuxian.network.SyncCultivationPacket;
import com.saifei.xiuxian.world.OreGeneration;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;  // ✅ 使用这个类
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(XiuXianMod.MOD_ID)
public class XiuXianMod {
    public static final String MOD_ID = "xiuxian";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(
            ResourceLocation.parse(MOD_ID + ":main"),   // ✅ 修正：必须包含路径
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final RegistryObject<CreativeModeTab> XIUXIAN_TAB = CREATIVE_TABS.register("xiuxian_tab", () ->
            CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.LOW_LINGSHI.get()))
                    .title(Component.translatable("creativetab.xiuxian_tab"))
                    .displayItems((parameters, output) -> {

                        //灵石
                        output.accept(ModItems.LOW_LINGSHI.get());
                        output.accept(ModItems.MID_LINGSHI.get());
                        output.accept(ModItems.HIGH_LINGSHI.get());
                        output.accept(ModItems.SUPREME_LINGSHI.get());

                        //灵石矿石
                        output.accept(ModBlocks.LOW_LINGSHI_ORE.get());
                        output.accept(ModBlocks.MID_LINGSHI_ORE.get());
                        output.accept(ModBlocks.HIGH_LINGSHI_ORE.get());
                        output.accept(ModBlocks.SUPREME_LINGSHI_ORE.get());

                        //蒲团 & 炼化炉
                        output.accept(ModBlocks.MEDITATION_MAT.get());
                        output.accept(ModBlocks.REFINING_FURNACE.get());
                    })
                    .build()
    );

    public XiuXianMod() {
        // ✅ 恢复使用 FMLJavaModLoadingContext（虽然弃用，但方法存在）
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        CapabilityRegistration.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        OreGeneration.BIOME_MODIFIERS.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        int packetId = 0;
        NETWORK.registerMessage(packetId++, SyncCultivationPacket.class,
                SyncCultivationPacket::encode,
                SyncCultivationPacket::decode,
                SyncCultivationPacket::handle);
        LOGGER.info("✅ 修仙模组网络通道初始化完成");
    }
}