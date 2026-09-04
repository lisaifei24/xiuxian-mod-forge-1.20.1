package com.saifei.xiuxian.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.saifei.xiuxian.entity.FireballProjectile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * 火球实体渲染器：不渲染任何模型（火球视觉由服务器广播的粒子特效呈现），
 * 仅按要求实现必需方法，避免客户端报错。
 */
public class FireballProjectileRenderer extends EntityRenderer<FireballProjectile> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("xiuxian:textures/entity/fireball_projectile.png");

    public FireballProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(FireballProjectile entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // 空实现：粒子特效已经足够表现火球
    }

    @Override
    public ResourceLocation getTextureLocation(FireballProjectile entity) {
        return TEXTURE;
    }
}
