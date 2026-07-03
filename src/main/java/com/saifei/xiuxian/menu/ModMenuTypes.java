package com.saifei.xiuxian.menu;

import com.saifei.xiuxian.XiuXianMod;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, XiuXianMod.MOD_ID);

    public static final RegistryObject<MenuType<RefiningFurnaceMenu>> REFINING_FURNACE_MENU =
            MENUS.register("refining_furnace",
                    () -> IForgeMenuType.create(RefiningFurnaceMenu::new));
}