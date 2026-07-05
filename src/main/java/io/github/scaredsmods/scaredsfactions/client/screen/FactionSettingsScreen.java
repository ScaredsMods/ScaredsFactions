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
import io.github.scaredsmods.scaredsfactions.client.screen.menu.FactionSettingsMenu;
import io.github.scaredsmods.scaredsfactions.api.common.faction.setting.AbstractFactionSetting;
import io.github.scaredsmods.scaredsfactions.api.common.faction.setting.StringFactionSetting;
import io.github.scaredsmods.scaredsfactions.server.network.ModNetworks;
import io.github.scaredsmods.scaredsfactions.server.network.packet.ModScreens;
import io.github.scaredsmods.scaredsfactions.server.network.packet.OpenEditStringSettingC2SPacket;
import io.github.scaredsmods.scaredsfactions.server.network.packet.OpenScreenC2SPacket;
import io.github.scaredsmods.scaredsfactions.server.network.packet.UpdateFactionSettingsPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class FactionSettingsScreen extends AbstractContainerScreen<FactionSettingsMenu> {

	public Screen parent;
	private static final ResourceLocation TEXTURE =
			ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");

	public FactionSettingsScreen(FactionSettingsMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
		super(pMenu, pPlayerInventory, pTitle);
		this.parent = ManageFactionScreen.INSTANCE;
		this.imageHeight = 222;
		this.imageWidth = 176;
		this.inventoryLabelY = this.imageHeight - 94;
	}

	@Override
	public void init() {
		super.init();
	}

	@Override
	public void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);
		pGuiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		renderBackground(guiGraphics);
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
		Slot slot = this.getSlotUnderMouse();
		if (slot != null){
			onSlotClick(slot.index, pButton);
			return true;
		}
		return super.mouseClicked(pMouseX, pMouseY, pButton);
	}

	private void onSlotClick(int index, int buttonId) {
		if (index == 49) {
			if (this.minecraft == null) return;
			this.minecraft.setScreen(this.parent);
			return;
		}

		var settings = this.menu.getSettings();
		if (index >= settings.size()) return;

		AbstractFactionSetting<?, ?> setting = settings.get(index);

		if (setting instanceof StringFactionSetting) {
			ModNetworks.CHANNEL.sendToServer(new OpenEditStringSettingC2SPacket(setting.getNbtId()));
			return;
		}

		setting.onClick(buttonId, () -> {
			CompoundTag tag = new CompoundTag();
			setting.save(tag);

			ModNetworks.CHANNEL.sendToServer(new UpdateFactionSettingsPacket(setting.getNbtId(), tag));
			ModNetworks.CHANNEL.sendToServer(new OpenScreenC2SPacket(ModScreens.FACTION_SETTINGS, Component.literal("Settings")));
		});
	}

	@Override
	public void onClose() {
		if (this.minecraft == null) return;
		this.minecraft.setScreen(parent);
		super.onClose();
	}

	public Screen getParent() {
		return parent;
	}

	public void setParent(Screen parent) {
		this.parent = parent;
	}

}
