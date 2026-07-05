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
package io.github.scaredsmods.scaredsfactions.common.compat.luckperms;
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
