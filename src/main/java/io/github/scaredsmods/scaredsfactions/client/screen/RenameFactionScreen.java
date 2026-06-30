/*
*  Copyright (C) 2026 ScaredRabbitNL
*
*  This program is free software: you can redistribute it and/or modify
*  it under the terms of the GNU Lesser General Public License as published by
*  the Free Software Foundation, either version 3 of the License, or
*  (at your option) any later version.
*
*  This program is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU Lesser General Public License for more details.
*
*  You should have received a copy of the GNU Lesser General Public License
*  along with this program. If not, see <https://www.gnu.org/licenses/>.
*/
package io.github.scaredsmods.scaredsfactions.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.scaredsmods.scaredsfactions.ScaredsFactionMod;
import io.github.scaredsmods.scaredsfactions.client.screen.menu.RenameFactionMenu;
import io.github.scaredsmods.scaredsfactions.server.network.ModNetworks;
import io.github.scaredsmods.scaredsfactions.server.network.packet.ModScreens;
import io.github.scaredsmods.scaredsfactions.server.network.packet.OpenScreenS2CPacket;
import io.github.scaredsmods.scaredsfactions.server.network.packet.RenameFactionPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

public class RenameFactionScreen extends AbstractContainerScreen<RenameFactionMenu> {
	private static final ResourceLocation TEXTURE = ScaredsFactionMod.id("textures/gui/container/rename_faction.png");

	public Screen parent;
	private EditBox name;
	private Button confirm;
	private Button back;

	public RenameFactionScreen(RenameFactionMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
		super(pMenu, pPlayerInventory, pTitle);
		this.parent = ManageFactionScreen.INSTANCE;
	}

	@Override
	protected void init() {
		super.init();
		this.name = new EditBox(this.font, 162, 76, 106, 12, Component.literal("Rename Faction"));
		this.name.setEditable(true);
		this.name.setValue("");
		this.name.setTextColor(-1);
		this.name.setBordered(false);
		this.name.setTextColorUneditable(-1);
		this.name.setCanLoseFocus(false);
		this.setInitialFocus(this.name);
		this.addRenderableWidget(this.name);

		this.confirm = Button.builder(Component.literal("Confirm").withStyle(ChatFormatting.GREEN), btn -> {
			String newName = this.name.getValue();
			ModNetworks.CHANNEL.sendToServer(new RenameFactionPacket(this.name.getValue()));
			ModNetworks.CHANNEL.sendToServer(new OpenScreenS2CPacket(ModScreens.MANAGE_FACTION, Component.literal(newName.replace("&", "§"))));
		}).bounds(165, 90, 45, 15).build();

		this.back = Button.builder(Component.literal("Back").withStyle(ChatFormatting.RED), btn -> {
			this.onClose();
		}).bounds(215, 90, 45,15).build();

		this.addRenderableWidget(this.back);
		this.addRenderableWidget(this.confirm);
	}


	@Override
	public void containerTick() {
		super.containerTick();
		this.name.tick();
	}


	public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
		String s = this.name.getValue();
		this.init(pMinecraft, pWidth, pHeight);
		this.name.setValue(s);
	}

	@Override
	protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);

		int x = (this.width - imageWidth) / 2;
		int y = (this.height - imageHeight) / 2;

		pGuiGraphics.blit(TEXTURE, x,y, 0, 0, imageWidth, imageHeight);
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

	@Override
	public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {

		if (pKeyCode == GLFW.GLFW_KEY_ESCAPE) {
			this.onClose();
		}
		if (pKeyCode == GLFW.GLFW_KEY_ENTER) {
			this.confirm.onPress();
		}

		return super.keyPressed(pKeyCode, pScanCode, pModifiers);
	}
}
