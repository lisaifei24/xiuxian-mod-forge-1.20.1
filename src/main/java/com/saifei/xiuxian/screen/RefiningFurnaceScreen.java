package com.saifei.xiuxian.screen;

import com.saifei.xiuxian.XiuXianMod;
import com.saifei.xiuxian.menu.RefiningFurnaceMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class RefiningFurnaceScreen extends AbstractContainerScreen<RefiningFurnaceMenu> {
    // 指向自己画的炼化炉 GUI 贴图路径
    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(XiuXianMod.MOD_ID, "textures/gui/refining_furnace_gui.png");

    public RefiningFurnaceScreen(RefiningFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // 1. 绘制整个 GUI 底图
        guiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        // 获取进度数据
        int progress = this.menu.getDataAccess().get(0);
        int maxProgress = this.menu.getDataAccess().get(1);

        if (progress > 0) {
            // ===== 2. 绘制火焰动画 =====
            // 贴图层“火焰动画”坐标：X=82, Y=54，尺寸 W=14, H=14。
            int fireWidth = (int) ((float) progress / maxProgress * 14);
            guiGraphics.blit(GUI_TEXTURE, x + 82, y + 54, 176, 0, fireWidth, 14);

            // ===== 3. 绘制箭头进度条 =====
            // 贴图层“箭头动画”坐标：X=77, Y=35，尺寸 W=24, H=17。
            // (进度越满，箭头填充越宽)
            int arrowWidth = (int) ((float) progress / maxProgress * 24);
            guiGraphics.blit(GUI_TEXTURE, x + 77, y + 35, 176, 14, arrowWidth, 17);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}