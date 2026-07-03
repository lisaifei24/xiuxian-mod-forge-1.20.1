package com.saifei.xiuxian.block;

import com.saifei.xiuxian.block.entity.ModBlockEntities;
import com.saifei.xiuxian.block.entity.RefiningFurnaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class RefiningFurnaceBlock extends BaseEntityBlock {

    public RefiningFurnaceBlock() {
        // ✅ 核心修改：设置硬度为 5.0，并开启“必须使用正确工具挖掘才能掉落”的属性
        super(Block.Properties.of()
                .strength(5.0f) // 硬度设为 5（和原版工作台、熔炉接近）
                .requiresCorrectToolForDrops() // 必须使用正确的工具才掉落物品
        );
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RefiningFurnaceBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == ModBlockEntities.REFINING_FURNACE_BE.get()) {
            return (BlockEntityTicker<T>) (lvl, pos, st, be) ->
                    RefiningFurnaceBlockEntity.tick(lvl, pos, st, (RefiningFurnaceBlockEntity) be);
        }
        return null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof RefiningFurnaceBlockEntity furnaceBE) {
                NetworkHooks.openScreen(serverPlayer, furnaceBE, buf -> buf.writeBlockPos(pos));
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}