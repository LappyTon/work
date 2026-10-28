package net.wurstclient.sell;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.network.chat.Component;
import net.wurstclient.mixin.MinecraftClientMixin;

import java.util.*;

public class AutosellCycle {

    private final AutosellSetup setup;
    private Thread cycleThread;

    public AutosellCycle(AutosellSetup setup) {
        this.setup = setup;
    }

    public void cycleSetup(ArrayList<String> args) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null)
            return;

        if (cycleThread != null && cycleThread.isAlive()) {
            System.out.println("Autosell thread is already running.");
            return;
        }

        List<String> lowerArgs = setup.getArgs().stream()
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .toList();

        if (!lowerArgs.contains(client.player.getName().getString().toLowerCase()) && args.contains("acc")) {
//            new NameChange().nameChange(args.get(args.indexOf("acc") + 1), "no name in acc Autosell.");
        }

        String anarchy = getArgValue(args, "an", "анка", "604");
        int loopsIndex = args.contains("loops") ? Integer.parseInt(args.get(args.indexOf("loops") + 1)) : 60;

        this.cycleThread = new Thread(() -> runCycle(args, anarchy, loopsIndex));
        cycleThread.start();
    }

    private void runCycle(ArrayList<String> args, String anarchy, int loopsIndex) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null || client.getConnection() == null) {
            System.out.println("Client or player or connection is null. Exiting thread.");
            return;
        }

//        AutosellDetect autosellDetect = new AutosellDetect();

        while (!Thread.interrupted()) {
            try {
                if (client.player != null)
                    MinecraftClient.getInstance.message(Component.literal("111"));

//                autosellDetect.checkTelegramCommands();

                if (client.player != null)
                    client.player.sendMessage(Component.literal("loop: " + setup.getLoops()));

                if (setup.getLoops() >= loopsIndex) {
                    setup.setLoops(0);
                    if (args.contains("hub")) {
                        executeCommand("hub");
                        sleep(350_000);
                    } else {
                        disconnect(args);
                        return;
                    }
                }

                if (client.player != null) {
                    executeCommand("an" + anarchy);
                    sleep(2000);
                    client.player.jump();
                    sleep(2000);
                    client.player.jump();
                    sleep(2000);
                    client.player.jump();
                    sleep(2000);
                    client.player.jumpFromGround();
                    sleep(1500);
                    client.player.jump();

                    hotbarSwap();
                    sleep(1500);
                    client.player.jump();
                }

                if (client.player != null) {
                    Player player = client.player;
                    executeCommand("ah " + player.getName().getString());
                    interactAuction(args, player);
                    shulkerCheck();
                    sellItems(args, player);
                    client.player.jump();
                    shulkerCheck();
                    executeCommand("feed");
                }

                setup.loopsPlusPlus();
            } catch (Exception e) {
                e.printStackTrace();
                if (client.player != null)
                    client.player.sendMessage(Component.literal("[Autosell] Помилка в циклі: " + e.getMessage()));
                sleep(5000);
            }
        }
    }

    private void interactAuction(ArrayList<String> args, Player player) {
        Minecraft client = Minecraft.getInstance();

        for (int i = 1; i < 15; ++i) {
            sleep(args.contains("spookytime") ? 700 : 350);
            if (client.player == null) break;

            AbstractContainerMenu handler = player.containerMenu;
            ItemStack item = handler.getSlot(0).getItem();
            if (item.isEmpty() || item.getItem() == Items.SHULKER_BOX || item.getItem() == Items.COOKED_PORKCHOP)
                break;

            client.interactionManager.clickSlot(handler.containerId, 0, 0,
                    ClickAction.PICKUP, player);
        }
    }

    private void sellItems(ArrayList<String> args, Player player) {
        Minecraft client = Minecraft.getInstance();
        int money = args.contains("money")
                ? Integer.parseInt(args.get(args.indexOf("money") + 1)) : 0;
        Random random = new Random();

        for (int i = 0; i < 9; ++i) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() != Items.SHULKER_BOX && stack.getItem() != Items.COOKED_PORKCHOP) {
                player.getInventory().selected = i;
                sleep(100);
                executeCommand("ah sell " + money);
                sleep(random.nextInt(300) + 200);
            }
        }
    }

    private void shulkerCheck() {
        Minecraft client = Minecraft.getInstance();
        if (client.player.containerMenu instanceof ShulkerBoxMenu) {
            client.getConnection().disconnect(Component.literal("[Sell] Opened Shulker Box"));
            return;
        }

        int slots = 0;
        ArrayList<Integer> shulkers = new ArrayList<>();

        for (int i = 0; i < 36; i++) {
            ItemStack itemStack = client.player.getInventory().getItem(i);
            if (itemStack.getItem() == Items.SHULKER_BOX) {
                CompoundTag nbt = itemStack.getTag();
                if (nbt != null && nbt.contains("BlockEntityTag")) {
                    CompoundTag blockEntityTag = nbt.getCompound("BlockEntityTag");
                    if (blockEntityTag.contains("Items") && !blockEntityTag.getList("Items", 10).isEmpty()) {
                        shulkers.add(i);
                    }
                }
            } else if (!itemStack.isEmpty()) {
                slots++;
            }
        }

        Player player = client.player;
        player.sendMessage(Component.literal("shulkers: " + shulkers.size() + " full slots: " + slots));

        if (slots <= 9 && !shulkers.isEmpty()) {
            executeCommand("ah sell");
            executeCommand("msg IIoner qq");
            sleep(1000);

            int shulkerSlot = shulkers.get(0);
            client.execute(() -> {
                client.interactionManager.clickSlot(player.containerMenu.containerId, shulkerSlot, 1,
                        ClickAction.PICKUP, player);
            });

            sleep(200);

            client.execute(() -> {
                if (player.containerMenu instanceof ShulkerBoxMenu menu) {
                    int full = 0;
                    for (int i = 0; i < 27 && full < 9; ++i) {
                        Slot slot = menu.getSlot(i);
                        ItemStack item = slot.getItem();
                        if (!item.isEmpty()) {
                            client.interactionManager.clickSlot(menu.containerId, i, 0, ClickAction.QUICK_MOVE, player);
                            full++;
                        }
                    }
                } else {
                    player.sendMessage(Component.literal("[sell] no handler"));
                }
            });
        }
    }

    private void disconnect(ArrayList<String> args) {
        Minecraft client = Minecraft.getInstance();
        client.execute(() -> {
            if (client.getConnection() != null) {
                int accIndex = args.indexOf("acc");
                if (accIndex != -1 && args.size() > accIndex + 4) {
                    List<String> lowerArgs = setup.getArgs().stream()
                            .filter(Objects::nonNull)
                            .map(String::toLowerCase)
                            .toList();

                    String name1 = lowerArgs.get(accIndex + 1);
                    String name2 = lowerArgs.get(accIndex + 2);
                    String name3 = lowerArgs.get(accIndex + 3);
                    String name4 = lowerArgs.get(accIndex + 4);

                    String currentName = client.player.getName().getString().toLowerCase();

                    if (currentName.equals(name1)) {
                        new NameChange().nameChange(name2, "Autosell. " + name2);
                    } else if (currentName.equals(name2)) {
                        new NameChange().nameChange(name3, "Autosell. " + name3);
                    } else if (currentName.equals(name3)) {
                        new NameChange().nameChange(name1, "Autosell. " + name1);
                    }
                }
            } else {
                client.setScreen(new TitleScreen());
            }
        });
    }

    public void executeCommand(String command) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null || client.getConnection() == null || client.level == null) {
            stopCycle();
            return;
        }
        client.execute(() -> {
            if (client.player != null && client.player.networkHandler != null)
                client.player.networkHandler.sendCommand(command);
        });
    }

    public static void hotbarSwap() {
        // Можеш додати сучасний код swapItems тут, якщо потрібно
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted", e);
        }
    }

    private String getArgValue(ArrayList<String> args, String key1, String key2, String defaultVal) {
        int index = Math.max(args.indexOf(key1), args.indexOf(key2));
        return index >= 0 && index + 1 < args.size() ? args.get(index + 1) : defaultVal;
    }

    public synchronized void stopCycle() {
        if (cycleThread != null) {
            cycleThread.interrupt();
            cycleThread = null;
        }
        System.out.println("Autosell thread stopped.");
    }

    public Thread getCycleThread() {
        return cycleThread;
    }
}
