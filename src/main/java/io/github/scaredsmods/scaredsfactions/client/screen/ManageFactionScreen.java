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
import io.github.scaredsmods.scaredsfactions.client.screen.menu.ManageFactionMenu;
import io.github.scaredsmods.scaredsfactions.server.network.ModNetworks;
import io.github.scaredsmods.scaredsfactions.server.network.packet.ModScreens;
import io.github.scaredsmods.scaredsfactions.server.network.packet.OpenScreenC2SPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;


public class ManageFactionScreen extends AbstractContainerScreen<ManageFactionMenu> {

	public Screen parent;
	private static final ResourceLocation TEXTURE =
			ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
	public static ManageFactionScreen INSTANCE;
	public ManageFactionScreen(ManageFactionMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.imageHeight = 132;
		this.inventoryLabelY = this.imageHeight - 94;
		INSTANCE = this;
	}
	@Override
	public void init() {
		super.init();
	}


	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		renderBackground(guiGraphics);
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		renderTooltip(guiGraphics, mouseX, mouseY);

	}

	@Override
	protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);
		pGuiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, 35);
		pGuiGraphics.blit(TEXTURE, leftPos, topPos + 35, 0, 126, imageWidth, 96);
	}

	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
		Slot slot = this.getSlotUnderMouse();
		if (slot != null){
			onSlotClick(slot.index);
			return true;
		}
		return super.mouseClicked(pMouseX, pMouseY, pButton);
	}

	private void onSlotClick(int index) {
		switch (index) {
			case 0 -> ModNetworks.CHANNEL.sendToServer(new OpenScreenC2SPacket(ModScreens.RENAME_FACTION, Component.literal("Rename Faction")));
			case 1 -> ModNetworks.CHANNEL.sendToServer(new OpenScreenC2SPacket(ModScreens.TRANSFER_OWNERSHIP, Component.literal("Transfer Ownership")));
			case 2 -> ModNetworks.CHANNEL.sendToServer(new OpenScreenC2SPacket(ModScreens.VIEW_MEMBERS, Component.literal("View Members")));
			case 3 -> ModNetworks.CHANNEL.sendToServer(new OpenScreenC2SPacket(ModScreens.FACTION_SETTINGS, Component.literal("Settings")));
			case 4 -> ModNetworks.CHANNEL.sendToServer(new OpenScreenC2SPacket(ModScreens.CONFIRM_RESET_BEACON, Component.literal("Confirm Reset Beacon Pos")));
			case 8 -> this.onClose();
		}
	}

	@Override
	public void onClose() {
		if (this.minecraft == null) return;
		this.minecraft.setScreen(parent);
		super.onClose();
	}

}
