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

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.scaredsmods.scaredsfactions.client.screen.menu.TransferOwnershipMenu;
import io.github.scaredsmods.scaredsfactions.common.network.packet.ModScreens;
import io.github.scaredsmods.scaredsfactions.common.network.packet.OpenScreenC2SPacket;
import io.github.scaredsmods.scaredsfactions.common.network.packet.PendingOwnershipTransferC2SPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class TransferOwnershipScreen extends AbstractContainerScreen<TransferOwnershipMenu> {

	public Screen parent;
	private static final ResourceLocation TEXTURE =
			ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");


	public TransferOwnershipScreen(TransferOwnershipMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
		super(pMenu, pPlayerInventory, pTitle);
		this.parent = ManageFactionScreen.INSTANCE;
		this.imageWidth = 176;
		this.imageHeight = 222;
		this.inventoryLabelY = this.imageHeight - 94;
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
		renderBackground(guiGraphics, mouseX, mouseY, partialTick);
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

	@Override
	public void onClose() {
		if (this.minecraft == null) return;
		this.minecraft.setScreen(this.parent);
	}

	private void onSlotClick(int index, int button) {
		if (index == 49) {
			this.onClose();
			return;
		}

		GameProfile target = getProfileForSlot(index);
		if (target == null) return;
		if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			PacketDistributor.sendToServer(new PendingOwnershipTransferC2SPacket(target.getId()));
			PacketDistributor.sendToServer(new OpenScreenC2SPacket(ModScreens.CONFIRM_TRANSFER, Component.literal("Confirm Ownership Transfer?")));
		}

	}

	public Screen getParent() {
		return parent;
	}

	private @Nullable GameProfile getProfileForSlot(int index) {
		List<GameProfile> slots = this.menu.getSlots();
		if (index < 0 || index >= slots.size()) return null;
		return slots.get(index);
	}

	public void setParent(Screen parent) {
		this.parent = parent;
	}

	public void close() {
		if (this.minecraft == null) return;
		this.minecraft.setScreen(null);
	}
}
