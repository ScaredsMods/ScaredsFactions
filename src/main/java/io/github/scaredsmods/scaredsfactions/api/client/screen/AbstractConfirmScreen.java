package io.github.scaredsmods.scaredsfactions.api.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.scaredsmods.scaredsfactions.api.client.menu.AbstractConfirmMenu;
import io.github.scaredsmods.scaredsfactions.client.screen.ManageFactionScreen;
import io.github.scaredsmods.scaredsfactions.common.ScaredsFactionMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public abstract class AbstractConfirmScreen<T extends AbstractConfirmMenu> extends AbstractContainerScreen<T> {

    private static final ResourceLocation TEXTURE = ScaredsFactionMod.id("textures/gui/container/confirm_transfer.png");
    public Screen parent;
    private Button confirm;
    private Button back;

    public AbstractConfirmScreen(T pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.parent = ManageFactionScreen.INSTANCE;
        this.imageHeight = 166;
        this.imageWidth = 176;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        pGuiGraphics.blit(TEXTURE, leftPos ,topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    @Override
    public void onClose() {
        if (this.minecraft == null) return;
        this.minecraft.setScreen(this.parent);
    }

    public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
        this.init(pMinecraft, pWidth, pHeight);
    }

}
