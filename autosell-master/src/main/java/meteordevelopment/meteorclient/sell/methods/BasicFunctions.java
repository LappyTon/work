/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.sell.methods;

import net.minecraft.client.MinecraftClient;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.ArrayList;

public class BasicFunctions {

    public static void sendMessage(Text message) {
        if (MinecraftClient.getInstance().player == null) return;
        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> {
            MinecraftClient.getInstance().player.sendMessage(message, false);
        });
    }
    public static void sendMessage(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (MinecraftClient.getInstance().player == null) return;
        client.execute(() -> {
            MinecraftClient.getInstance().player.sendMessage(Text.of(message), false);
        });
    }


    public static String getArgValue(ArrayList<String> args, String key1, String key2, String defaultVal) {
        int index = Math.max(args.indexOf(key1), args.indexOf(key2));
        return index >= 0 && index + 1 < args.size() ? args.get(index + 1) : defaultVal;
    }

    public static boolean isInventoryEmpty() {
        MinecraftClient client = MinecraftClient.getInstance();
        Inventory inventory = client.player.getInventory();
        for (int i = 0; i < inventory.size(); ++i) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty() && !stack.getItem().equals(Items.SHULKER_BOX)) {
                return false;
            }
        }
        return true;
    }
}
