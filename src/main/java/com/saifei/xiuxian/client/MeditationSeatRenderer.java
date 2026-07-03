package com.saifei.xiuxian.client;

import com.saifei.xiuxian.entity.MeditationSeatEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

// 一个不渲染任何东西的空渲染器，专门给不可见实体使用
public class MeditationSeatRenderer extends EntityRenderer<MeditationSeatEntity> {

    public MeditationSeatRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(MeditationSeatEntity entity) {
        // 返回 null 有可能在某些渲染循环里引发空指针，
        // 这里返回一个 Minecraft 里绝对存在的贴图（比如空气的贴图）来彻底防止崩溃
        return ResourceLocation.parse("minecraft:textures/block/air.png");
    }

    @Override
    public void render(MeditationSeatEntity entity, float entityYaw, float partialTick, com.mojang.blaze3d.vertex.PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource buffer, int packedLight) {
        // 什么也不做，让这个实体彻底透明、不渲染。
    }
}