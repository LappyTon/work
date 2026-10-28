///*
// * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
// * Copyright (c) Meteor Development.
// */
//
//package meteordevelopment.meteorclient.sell.methods;
//
//import net.minecraft.client.MinecraftClient;
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.item.Item;
//import net.minecraft.item.ItemStack;
//import net.minecraft.item.Items;
//import net.minecraft.screen.slot.SlotActionType;
//
//import java.util.List;
//
//public class SwapFunctions {
//
//    public void hotbarSwap() {
//        MinecraftClient client = MinecraftClient.getInstance();
//        PlayerEntity player = client.player;
//        if (player == null || client.interactionManager == null)
//            return;
//
//        for (int gui = 8; gui < 36; ++gui) {
//            ItemStack item = player.getInventory().getStack(gui);
//            if (!item.isEmpty() && !item.getItem().equals(Items.SHULKER_BOX) && !item.getItem().equals(Items.COOKED_PORKCHOP)) {
//                int hotbar = findEmptyHotbarSlot(player);
//                if (hotbar < 0)
//                    break;
//                swapItems(hotbar, gui);
//            }
//        }
//    }
//    public void potionSwap() {
//        MinecraftClient client = MinecraftClient.getInstance();
//        PlayerEntity player = client.player;
//
//        if (player == null || client.interactionManager == null) return;
//
//        int syncId = player.currentScreenHandler.syncId;
//
//        for (int hotbar = 0; hotbar < 9; hotbar++) {
//            ItemStack stack = player.getInventory().getStack(hotbar);
//
//            if (stack.isEmpty()) continue;
//            if (stack.getItem().equals(Items.COOKED_PORKCHOP) ||
//                stack.getItem().equals(Items.GOLDEN_CARROT)) continue;
//
//            int hotbarSlot = 36 + hotbar; // офсет для хотбару
//
//            client.interactionManager.clickSlot(
//                syncId,
//                hotbarSlot,
//                0,
//                SlotActionType.QUICK_MOVE, // FAST MOVE в інвентар
//                player
//            );
//            sleep(200);
//        }
//        refillHotbar16();
//    }
//
//
//    public void refillHotbar16() {
//        MinecraftClient client = MinecraftClient.getInstance();
//        PlayerEntity player = client.player;
//        if (player == null || client.interactionManager == null) return;
//
//        int syncId = player.currentScreenHandler.syncId;
//
//        for (int hotbar = 0; hotbar < 9; hotbar++) {
//            int hotbarSlot = 36 + hotbar;
//            ItemStack hotbarStack = player.getInventory().getStack(hotbar);
//            List<String> args = this.setup.getArgs();
//
//            if (!hotbarStack.isEmpty()&& hotbarStack.getCount() >= 16)
//                continue;
//
//            Item hotbarItem = hotbarStack.isEmpty() ? null : hotbarStack.getItem();
//
//            for (int invSlot = 9; invSlot < 36; invSlot++) {
//                ItemStack invStack = player.getInventory().getStack(invSlot);
//
//                if (invStack.isEmpty()) continue;
//                if (invStack.getCount() < 16 && args.indexOf("potion")>0) continue;
//                if (hotbarItem != null && invStack.getItem() != hotbarItem) continue;
//
//                // взяли стак
//                client.interactionManager.clickSlot(syncId, invSlot, 0, SlotActionType.PICKUP, player);
//                sleep(500);
//                List<String> argsuments = this.setup.getArgs();
//                // кладемо ПО 1 — 16 разів
//                int items = Integer.parseInt(argsuments.get(args.indexOf("items")+1));
//                for (int i = 0; i < items; i++) {
//                    client.interactionManager.clickSlot(syncId, hotbarSlot, 1, SlotActionType.PICKUP, player);
//                    sleep(100);
//                    if (args.contains("delay")) sleep(Integer.parseInt(args.get(args.indexOf("delay")+1)));
//                }
//                // повертаємо залишок
//                sleep(300);
//                client.interactionManager.clickSlot(syncId, findEmptyInvSlot(player), 0, SlotActionType.PICKUP, player);
//
//                break;
//            }
//        }
//
//}
