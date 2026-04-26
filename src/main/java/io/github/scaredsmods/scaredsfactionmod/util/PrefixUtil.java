/*
*  Copyright (C) 2025 ScaredRabbitNL
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
package io.github.scaredsmods.scaredsfactionmod.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class PrefixUtil {

	public static final Component PREFIX = Component.literal("[")
			.withStyle(ChatFormatting.GRAY)
			.append(Component.literal("ScaredsFactions").withStyle(ChatFormatting.LIGHT_PURPLE))
			.append(Component.literal("]").withStyle(ChatFormatting.GRAY))
			.append(Component.literal(" » ").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));

	public static final Component PREFIX_PLAIN = Component.literal("[")
			.withStyle(ChatFormatting.GRAY)
			.append(Component.literal("ScaredsFactions").withStyle(ChatFormatting.LIGHT_PURPLE))
			.append(Component.literal("]").withStyle(ChatFormatting.GRAY));

	public static Component info(String message) {
		return PREFIX.copy().append(Component.literal(message).withStyle(ChatFormatting.WHITE));
	}

	public static Component error(String error) {
		return PREFIX.copy().append(Component.literal(error).withStyle(ChatFormatting.RED));
	}

	public static Component success(String success) {
		return PREFIX.copy().append(Component.literal(success).withStyle(ChatFormatting.GREEN));
	}

	public static Component formattedMessage(String message, ChatFormatting... extraStyles) {
		return PREFIX.copy().append(Component.literal(message).withStyle(extraStyles));
	}
}
