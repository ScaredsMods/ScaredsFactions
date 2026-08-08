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

import io.github.scaredsmods.scaredsfactions.api.client.screen.AbstractConfirmScreen;
import io.github.scaredsmods.scaredsfactions.common.ScaredsFactionMod;
import io.github.scaredsmods.scaredsfactions.client.screen.menu.ConfirmTransferOwnershipMenu;
import io.github.scaredsmods.scaredsfactions.common.network.packet.ModScreens;
import io.github.scaredsmods.scaredsfactions.common.network.packet.OpenScreenC2SPacket;
import io.github.scaredsmods.scaredsfactions.common.network.packet.PendingOwnershipTransferC2SPacket;
import io.github.scaredsmods.scaredsfactions.common.network.packet.TransferOwnershipC2SPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class ConfirmTransferOwnershipScreen extends AbstractConfirmScreen<ConfirmTransferOwnershipMenu> {

	private static final ResourceLocation TEXTURE = ScaredsFactionMod.id("textures/gui/container/confirm_transfer.png");
	public Screen parent;
	private Button confirm;
	private Button back;

	public ConfirmTransferOwnershipScreen(ConfirmTransferOwnershipMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
		super(pMenu, pPlayerInventory, pTitle);
		this.parent = ManageFactionScreen.INSTANCE;
		this.imageHeight = 166;
		this.imageWidth = 176;
		this.inventoryLabelY = this.imageHeight - 94;
	}

	@Override
	public void init() {
		super.init();

		this.confirm = Button.builder(Component.literal("Confirm").withStyle(ChatFormatting.GREEN), btn -> {
			PacketDistributor.sendToServer(new TransferOwnershipC2SPacket(this.menu.getTarget()));
			PacketDistributor.sendToServer(new OpenScreenC2SPacket(ModScreens.CLOSE, Component.literal("")));
		}).bounds(leftPos + 40, topPos + 38, 45, 15).build();

		this.back = Button.builder(Component.literal("Back").withStyle(ChatFormatting.RED), btn -> {
			PacketDistributor.sendToServer(new PendingOwnershipTransferC2SPacket(null));
			this.onClose();
		}
		).bounds(leftPos + 90, topPos + 38, 45,15).build();

		this.addRenderableWidget(this.back);
		this.addRenderableWidget(this.confirm);
	}

}
