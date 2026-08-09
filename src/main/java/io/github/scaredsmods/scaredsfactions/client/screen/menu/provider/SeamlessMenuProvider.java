package io.github.scaredsmods.scaredsfactions.client.screen.menu.provider;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

public class SeamlessMenuProvider {


    public static MenuProvider wrap(MenuProvider delegate) {
        return new MenuProvider() {
            @Override
            public @NotNull Component getDisplayName() {
                return delegate.getDisplayName();
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player player) {
                return delegate.createMenu(containerId, inventory, player);
            }

            @Override
            public boolean shouldTriggerClientSideContainerClosingOnOpen() {
                return false;
            }
        };
    }

}
