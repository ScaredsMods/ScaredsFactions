package io.github.scaredsmods.scaredsfactions.client.screen.menu;

import io.github.scaredsmods.scaredsfactions.api.client.menu.AbstractConfirmMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class ConfirmResetBeaconPosMenu extends AbstractConfirmMenu {

    public ConfirmResetBeaconPosMenu(int containerId, Inventory inventory) {
        super(ModMenuTypes.RESET_BEACON_POS.get(), containerId);
    }

    public ConfirmResetBeaconPosMenu(int pContainerId, Inventory pPlayerInventory, FriendlyByteBuf buf) {
        this(pContainerId, pPlayerInventory);
    }

}
