package com.saifei.xiuxian.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Set;

/**
 * 灵草/仙草方块：使用 BushBlock 系（半格植物形状 + XZ 随机偏移），
 * 支持自定义可扎根土壤（mayPlaceOn），用于配合 SimpleBlockFeature 在世界中生成。
 */
public class LingCaoBlock extends BushBlock {

    private static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 14.0D, 12.0D);

    private final Set<Block> soils;

    public LingCaoBlock(Properties properties, Block... soils) {
        super(properties);
        this.soils = Set.of(soils);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return soils.contains(state.getBlock());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        return mayPlaceOn(level.getBlockState(belowPos), level, belowPos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Vec3 vec3 = state.getOffset(level, pos);
        return SHAPE.move(vec3.x, vec3.y, vec3.z);
    }
}
