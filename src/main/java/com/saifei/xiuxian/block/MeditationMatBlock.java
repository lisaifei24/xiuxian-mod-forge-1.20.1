package com.saifei.xiuxian.block;

import com.saifei.xiuxian.entity.MeditationSeatEntity;
import com.saifei.xiuxian.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MeditationMatBlock extends Block {

    // ✅ 修正：碰撞箱应为完整方块高度（Y=0 到 Y=1），确保玩家站在上面时可交互
    public static final VoxelShape FULL_BLOCK_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public MeditationMatBlock() {
        super(Properties.of()
                .mapColor(MapColor.COLOR_BROWN)
                .strength(0.5f)
                .sound(SoundType.WOOL)
                .noOcclusion()
        );
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return FULL_BLOCK_SHAPE; // ✅ 使用完整方块碰撞箱
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return FULL_BLOCK_SHAPE; // ✅ 视觉形状也应为完整方块
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isSolid(); // 保持原逻辑
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.isPassenger()) {
            if (!level.isClientSide) {
                MeditationSeatEntity seat = new MeditationSeatEntity(ModEntities.MEDITATION_SEAT.get(), level);
                // ✅ 对齐蒲团顶部（Y=0.5），确保玩家坐下后位置正确
                seat.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                level.addFreshEntity(seat);
                player.startRiding(seat);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }
}