package com.saifei.xiuxian;

import com.mojang.logging.LogUtils;
import com.saifei.xiuxian.block.ModBlocks;
import com.saifei.xiuxian.capability.CapabilityRegistration;
import com.saifei.xiuxian.item.ModItems;
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
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(XiuXianMod.MOD_ID)
public class XiuXianMod {
    public static final String MOD_ID = "xiuxian";
    public static final Logger LOGGER = LogUtils.getLogger();

    // ✅ 网络通道（协议版本硬编码）
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    // ✅ 创造模式标签页注册
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final RegistryObject<CreativeModeTab> XIUXIAN_TAB = CREATIVE_TABS.register("xiuxian_tab", () ->
            CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.LOW_LINGSHI.get()))
                    .title(Component.translatable("creativetab.xiuxian_tab"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.LOW_LINGSHI.get());
                        output.accept(ModItems.MID_LINGSHI.get());
                        output.accept(ModItems.HIGH_LINGSHI.get());
                        output.accept(ModItems.SUPREME_LINGSHI.get());
                        output.accept(ModBlocks.LOW_LINGSHI_ORE.get());
                        output.accept(ModBlocks.MID_LINGSHI_ORE.get());
                        output.accept(ModBlocks.HIGH_LINGSHI_ORE.get());
                        output.accept(ModBlocks.SUPREME_LINGSHI_ORE.get());
                    })
                    .build()
    );

    public XiuXianMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 1. 注册 Capability
        CapabilityRegistration.register(modEventBus);

        // 2. 注册物品、方块、创造标签页
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);

        // 3. ✅ 仅注册 BiomeModifier（ConfiguredFeature 和 PlacedFeature 已改为纯 JSON）
        OreGeneration.BIOME_MODIFIERS.register(modEventBus);

        // 4. ✅ 网络包注册移至 FMLCommonSetupEvent，避免单人存档"连接已丢失"
        modEventBus.addListener(this::commonSetup);
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