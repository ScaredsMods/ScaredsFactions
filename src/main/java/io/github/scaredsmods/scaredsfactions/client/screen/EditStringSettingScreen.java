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
import io.github.scaredsmods.scaredsfactions.client.screen.menu.EditStringSettingMenu;
import io.github.scaredsmods.scaredsfactions.common.ScaredsFactionMod;
import io.github.scaredsmods.scaredsfactions.server.network.ModNetworks;
import io.github.scaredsmods.scaredsfactions.server.network.packet.ModScreens;
import io.github.scaredsmods.scaredsfactions.server.network.packet.OpenScreenC2SPacket;
import io.github.scaredsmods.scaredsfactions.server.network.packet.UpdateFactionSettingsPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

public class EditStringSettingScreen extends AbstractContainerScreen<EditStringSettingMenu> {

	private static final ResourceLocation TEXTURE = ScaredsFactionMod.id("textures/gui/container/edit_string_value.png");

	public Screen parent;
	private EditBox newValue;
	private Button confirm;
	private Button back;

	public EditStringSettingScreen(EditStringSettingMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
		super(pMenu, pPlayerInventory, pTitle);
		this.parent = ManageFactionScreen.INSTANCE;
		this.imageHeight = 166;
		this.imageWidth = 176;
		this.inventoryLabelY = this.imageHeight - 94;
	}

	@Override
	public void init() {
		super.init();
		this.newValue = new EditBox(this.font, leftPos + 37, topPos + 39, 106, 12, Component.literal("Rename Faction"));
		this.newValue.setEditable(true);
		this.newValue.setValue("");
		this.newValue.setTextColor(-1);
		this.newValue.setBordered(false);
		this.newValue.setTextColorUneditable(-1);
		this.newValue.setCanLoseFocus(false);
		this.setInitialFocus(this.newValue);
		this.addRenderableWidget(this.newValue);

		this.confirm = Button.builder(Component.literal("Confirm").withStyle(ChatFormatting.GREEN), btn -> {
			String newValue = this.newValue.getValue();
			CompoundTag tag = new CompoundTag();
			tag.putString(this.menu.getNbtId(), newValue);
			ModNetworks.CHANNEL.sendToServer(new UpdateFactionSettingsPacket(this.menu.getNbtId(), tag));
			ModNetworks.CHANNEL.sendToServer(new OpenScreenC2SPacket(ModScreens.FACTION_SETTINGS, Component.literal("Faction Settings")));
		}).bounds(leftPos + 42, topPos + 53, 45, 15).build();

		this.back = Button.builder(Component.literal("Back").withStyle(ChatFormatting.RED), btn -> {
			this.onClose();
		}).bounds(leftPos + 92, topPos + 53, 45,15).build();

		this.addRenderableWidget(this.back);
		this.addRenderableWidget(this.confirm);
	}

	@Override
	public void containerTick() {
		super.containerTick();
		this.newValue.tick();
	}


	@Override
	protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);
		pGuiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
	}

	@Override
	public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
		renderBackground(pGuiGraphics);
		super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
		renderTooltip(pGuiGraphics, pMouseX, pMouseY);
	}

	public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
		String s = this.newValue.getValue();
		this.init(pMinecraft, pWidth, pHeight);
		this.newValue.setValue(s);
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
			return true;
		}
		if (pKeyCode == GLFW.GLFW_KEY_ENTER) {
			this.confirm.onPress();
			return true;
		}

		if (this.minecraft != null && this.minecraft.options.keyInventory.matches(pKeyCode, pScanCode)) {
			return true;
		}
		if (this.newValue.keyPressed(pKeyCode, pScanCode, pModifiers)) {
			return true;
		}
		return super.keyPressed(pKeyCode, pScanCode, pModifiers);
	}

}
