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
