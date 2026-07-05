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

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArrayEnumArgument<T extends Enum<T>> implements ArgumentType<T> {
	private static final Dynamic2CommandExceptionType INVALID_ENUM = new Dynamic2CommandExceptionType(
			(found, constants) -> Component.translatable("commands.forge.arguments.enum.invalid", constants, found));
	private final Class<T> enumClass;
	private final T[] allowedValues;

	public static <R extends Enum<R>> ArrayEnumArgument<R> enumArgument(Class<R> enumClass) {
		return new ArrayEnumArgument<>(enumClass, enumClass.getEnumConstants());
	}

	@SafeVarargs
	public static <R extends Enum<R>> ArrayEnumArgument<R> enumArgument(Class<R> enumClass, R... allowedValues) {
		return new ArrayEnumArgument<>(enumClass, allowedValues);
	}

	private ArrayEnumArgument(final Class<T> enumClass, final T[] allowedValues) {
		this.enumClass = enumClass;
		this.allowedValues = allowedValues;
	}

	@Override
	public T parse(final StringReader reader) throws CommandSyntaxException {
		String name = reader.readUnquotedString();
		for (T value : allowedValues) {
			if (value.name().equals(name)) return value;
		}
		throw INVALID_ENUM.createWithContext(reader, name, Arrays.toString(Arrays.stream(allowedValues).map(Enum::name).toArray()));
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
		return SharedSuggestionProvider.suggest(Stream.of(allowedValues).map(Enum::name), builder);
	}

	@Override
	public Collection<String> getExamples() {
		return Stream.of(allowedValues).map(Enum::name).collect(Collectors.toList());
	}

	public static class Info<T extends Enum<T>> implements ArgumentTypeInfo<ArrayEnumArgument<T>, ArrayEnumArgument.Info<T>.Template> {
		@Override
		public void serializeToNetwork(ArrayEnumArgument.Info.Template template, FriendlyByteBuf buffer) {
			buffer.writeUtf(template.enumClass.getName());
			buffer.writeInt(template.allowedValues.length);
			for (Enum<?> value : template.allowedValues) {
				buffer.writeUtf(value.name());
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public ArrayEnumArgument.Info.Template deserializeFromNetwork(FriendlyByteBuf buffer) {
			try {
				String name = buffer.readUtf();
				Class<T> clazz = (Class<T>) Class.forName(name);
				int size = buffer.readInt();
				T[] values = (T[]) new Enum[size];
				for (int i = 0; i < size; i++) {
					values[i] = Enum.valueOf(clazz, buffer.readUtf());
				}
				return new ArrayEnumArgument.Info.Template(clazz, values);
			} catch (ClassNotFoundException e) {
				return null;
			}
		}

		@Override
		public void serializeToJson(ArrayEnumArgument.Info.Template template, JsonObject json) {
			json.addProperty("enum", template.enumClass.getName());
		}

		@Override
		public ArrayEnumArgument.Info.Template unpack(ArrayEnumArgument<T> argument) {
			return new ArrayEnumArgument.Info.Template(argument.enumClass, argument.allowedValues);
		}

		public class Template implements ArgumentTypeInfo.Template<ArrayEnumArgument<T>> {
			final Class<T> enumClass;
			final T[] allowedValues;

			Template(Class<T> enumClass, T[] allowedValues) {
				this.enumClass = enumClass;
				this.allowedValues = allowedValues;
			}

			@Override
			public ArrayEnumArgument<T> instantiate(CommandBuildContext pStructure) {
				return new ArrayEnumArgument<>(this.enumClass, this.allowedValues);
			}

			@Override
			public ArgumentTypeInfo<ArrayEnumArgument<T>, ?> type() {
				return ArrayEnumArgument.Info.this;
			}
		}
	}
}
