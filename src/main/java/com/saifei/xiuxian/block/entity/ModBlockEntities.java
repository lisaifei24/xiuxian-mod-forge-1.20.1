package com.saifei.xiuxian.block.entity;

import com.saifei.xiuxian.XiuXianMod;
import com.saifei.xiuxian.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, XiuXianMod.MOD_ID);
    public static final RegistryObject<BlockEntityType<RefiningFurnaceBlockEntity>> REFINING_FURNACE_BE =
            BLOCK_ENTITIES.register("refining_furnace", () ->
                    BlockEntityType.Builder.of(RefiningFurnaceBlockEntity::new, ModBlocks.REFINING_FURNACE.get()).build(null));
}
