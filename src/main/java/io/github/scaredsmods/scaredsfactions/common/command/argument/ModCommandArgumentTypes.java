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
package io.github.scaredsmods.scaredsfactions.common.command.argument;

import io.github.scaredsmods.scaredsfactions.common.ScaredsFactionMod;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCommandArgumentTypes {

	public static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, ScaredsFactionMod.MOD_ID);

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final RegistryObject<ArrayEnumArgument.Info> ARRAY_ENUM_ARGUMENT_TYPE = COMMAND_ARGUMENT_TYPES.register("array_enum", () ->
			ArgumentTypeInfos.registerByClass(ArrayEnumArgument.class, new ArrayEnumArgument.Info()));

	public static void register(IEventBus bus) {
		COMMAND_ARGUMENT_TYPES.register(bus);
	}
}
