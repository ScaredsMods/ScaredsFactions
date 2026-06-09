package io.github.scaredsmods.scaredsfactions.compat.luckperms;
import net.luckperms.api.LuckPermsProvider;
import net.minecraft.commands.CommandSourceStack;

import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;

/**
 * Class hooking into LuckPerms mod for permission nodes instead
 * of only using the integrated OP level.
 * This file comes from <a href="https://github.com/NEZNAMY/TAB/blob/b23912b52f77b6230e0e1200e4ce7ff33e40ff2e/forge/src/main/java/me/neznamy/tab/platforms/forge/hook/LuckPermsAPIHook.java">...</a>. Thanks to NEZNAMY for making this
 */
public class LuckPermsAPICompat {

    /** Flag tracking presence of LuckPerms API */
    private static final boolean luckPerms = ModList.get().isLoaded("luckperms");

    /**
     * Checks for permission and returns the result.
     *
     * @param   source
     *          Source to check permission of
     * @param   permission
     *          Permission node to check
     * @return  {@code true} if has permission, {@code false} if not
     */
    public static boolean hasPermission(@NotNull CommandSourceStack source, @NotNull String permission) {
        if (source.hasPermission(4)) return true;
        return luckPerms && LuckPermsProvider.get().getUserManager().getUser(source.getPlayer().getUUID()).getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }
}