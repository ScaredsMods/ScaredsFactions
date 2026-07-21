package io.github.scaredsmods.scaredsfactions.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.scaredsmods.scaredsfactions.api.client.screen.AbstractConfirmScreen;
import io.github.scaredsmods.scaredsfactions.client.screen.menu.ConfirmResetBeaconPosMenu;
import io.github.scaredsmods.scaredsfactions.common.ScaredsFactionMod;
import io.github.scaredsmods.scaredsfactions.server.network.ModNetworks;
import io.github.scaredsmods.scaredsfactions.server.network.packet.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ConfirmResetBeaconPosScreen extends AbstractConfirmScreen<ConfirmResetBeaconPosMenu> {

    private static final ResourceLocation TEXTURE = ScaredsFactionMod.id("textures/gui/container/confirm_transfer.png");
    public Screen parent;
    private Button confirm;
    private Button back;


    public ConfirmResetBeaconPosScreen(ConfirmResetBeaconPosMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
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
            ModNetworks.CHANNEL.sendToServer(new ResetBeaconPosPacket());
            ModNetworks.CHANNEL.sendToServer(new OpenScreenC2SPacket(ModScreens.MANAGE_FACTION, Component.literal("Manage Faction")));
        })
                .bounds(leftPos + 40, topPos + 38, 45, 15).build();

        this.back = Button.builder(Component.literal("Back").withStyle(ChatFormatting.RED), btn -> this.onClose())
                .bounds(leftPos + 90, topPos + 38, 45,15).build();

        this.addRenderableWidget(this.back);
        this.addRenderableWidget(this.confirm);
    }

}
