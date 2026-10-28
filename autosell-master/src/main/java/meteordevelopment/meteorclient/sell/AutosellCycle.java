package meteordevelopment.meteorclient.sell;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.AutoEat;
import net.jasper.mod.automation.PlayerRecorder;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.sell.methods.BasicFunctions;
import meteordevelopment.meteorclient.systems.proxies.Proxies;
import meteordevelopment.meteorclient.systems.proxies.Proxy;
import meteordevelopment.meteorclient.systems.proxies.ProxyType;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.item.BlockItem;
import net.minecraft.block.ShulkerBoxBlock;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class AutosellCycle {

    private final AutosellSetup setup;
    private Thread cycleThread;

    public AutosellCycle(AutosellSetup setup) {
        this.setup = setup;

    }
    private long lastReconnectTime = 0;

    public void cycleSetup(ArrayList<String> args) {

        if (!reconnectSubscribed) {
            MeteorClient.EVENT_BUS.subscribe(this);
            reconnectSubscribed = true;
        }
        MeteorClient.EVENT_BUS.subscribe(this);
        if (MinecraftClient.getInstance() == null || MinecraftClient.getInstance().player == null)
            return;
        MinecraftClient client = MinecraftClient.getInstance();

        if (cycleThread != null && cycleThread.isAlive()) {
            System.out.println("Autosell thread is already running.");
            return;
        }

        List<String> lowerArgs = setup.getArgs().stream()
            .filter(Objects::nonNull)
            .toList();

        if (!lowerArgs.contains(client.player.getName().getString())) {

            if (args.contains("acc")) {
                new NameChange().nameChange(args.get(args.indexOf("acc") + 1), "no name in acc Autosell.");
            }

            int proxyIndex = args.indexOf("proxy");
            if (proxyIndex != -1 && args.size() > proxyIndex + 3) {

                String proxy1 = args.get(proxyIndex + 1);
                String proxy2 = args.get(proxyIndex + 2);
                String proxy3 = args.get(proxyIndex + 3);

                int currentNameIndex = args.indexOf(client.player.getName().getString()) - args.indexOf("acc");
                String currentProxy = args.get(proxyIndex+currentNameIndex);

                if (!currentProxy.equals(proxy1) && !currentProxy.equals(proxy2) && !currentProxy.equals(proxy3)) {

                    Proxies.get().removeAll();
                    System.out.println("proxy 3");
                    sendMessage("proxy 3");

                    Proxy newProxy = createProxy(proxy1);
                    sendMessage(proxy1);
                    Proxies.get().add(newProxy);
                    Proxies.get().setEnabled(newProxy, true);
                }
            }

        }

        String anarchy = getArgValue(args, "an", "анка", "604");
        int loopsIndex = args.contains("loops") ? Integer.parseInt(args.get(args.indexOf("loops") + 1)) : 60;

        this.cycleThread = new Thread(() -> runCycle(args, anarchy, loopsIndex));
        cycleThread.start();
    }

    private void runCycle(ArrayList<String> args, String anarchy, int loopsIndex) {
        if (MinecraftClient.getInstance() == null || MinecraftClient.getInstance().player == null || MinecraftClient.getInstance().getNetworkHandler() == null) {
            System.out.println("Client or player or networkHandler is null. Exiting thread.");
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();

//        AutosellDetect autosellDetect = new AutosellDetect();

        while (!Thread.interrupted()) {
            try {
                if (client.player != null)
                    sendMessage(Text.of("111"));
                boolean skip = false;
//                autosellDetect.checkTelegramCommands();

                if (client.player != null)
                    sendMessage(Text.of("loop: " + setup.getLoops()));
                if (setup.getLoops() >= loopsIndex) {
                    setup.setLoops(0);
                    if (args.contains("hub")) {
                        executeCommand("hub");
                        sleep(350000);

                    } else {
                        disconnect(args);
                        return;
                    }
                }

                if (client.player != null) {
                    executeCommand("an" + anarchy);
                    if (client.player.getHungerManager().getFoodLevel() < 19) {
                        Modules.get().get(AutoEat.class).toggle();
                        sleep(5000);
                        Modules.get().get(AutoEat.class).toggle();
                    }
                    if (args.contains("jump")) {
                        sleep(2000);
                        client.player.jump();

                        sleep(2000);
                        client.player.jump();

                        sleep(2000);
                        client.player.jump();

                        sleep(2000);
                        client.player.jump();

                        sleep(1500);
                        client.player.jump();

                        hotbarSwap();
                        sleep(1500);
                        client.player.jump();

                    } else {
                        if (setup.getLoops() < 2 || setup.getLoops() % 7 == 0f) {

                            executeCommand("spawn");
                            sleep(8000);

                            client.execute(() -> {
                                try {
                                    MinecraftClient mc = MinecraftClient.getInstance();

                                    if (mc.player == null || mc.world == null) {
                                        // Не запускаємо запис, поки клієнт не готовий.
                                        return;
                                    }

                                    PlayerRecorder.startReplay();
                                    String record = "rec/" + ThreadLocalRandom.current().nextInt(1, 6) + ".rec";

                                    System.out.println("Loading: " + record);

                                    sendMessage("Loading: " + record);
                                    PlayerRecorder.loadRecord(record);
                                    PlayerRecorder.startReplay();
                                    PlayerRecorder.togglePause();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        } else {

                            client.execute(() -> {
                                try {
                                    PlayerRecorder.togglePause();

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });

                            sleep(8000);

                            client.execute(() -> {
                                try {
                                    PlayerRecorder.togglePause();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    }
                    if (!(args.contains("items"))) { hotbarSwap(); }
                    sleep(2000);
                }

                if (client.player != null) {
                    PlayerEntity player = client.player;
                    executeCommand("ah " + player.getName().getString());
                    sleep(3000);
                    interactAuction(args, player);
                    shulkerCheck();

                    int syncId = client.player.currentScreenHandler.syncId;

                    client.execute(() -> {
                        client.getNetworkHandler().sendPacket(
                            new CloseHandledScreenC2SPacket(syncId)
                        );
                        client.player.closeHandledScreen();
                    });
                    sleep(500);
                    int items = Integer.parseInt(args.get(args.indexOf("items") + 1));
                    int y = 0;
                    while (true) {
                        executeCommand("an" + anarchy);
                        sendMessage("Цикл перевірки хотбару - "+y);
                        if (y>5) {
                            skip = true;
                            break;
                        }
                        potionSwap();
                        sleep(3000);
                        boolean correct = true;
                        for (int i = 0; i < 8; i++) {
                            player.getInventory().getStack(i).getCount();
                            if (player.getInventory().getStack(i).getCount() != items && !player.getInventory().getStack(i).isEmpty()) {
                                correct = false;
                                sendMessage("break "+i);
                                break;
                            }
                        }
                        if (correct) break;
                        y++;
                    }
                    if (!skip) {
                        sellItems(args, player);
                        shulkerCheck();
                        executeCommand("feed");
                    }
                }

                setup.loopsPlusPlus();
            } catch (Exception e) {
                e.printStackTrace();
                if (client.player != null)
                    sendMessage(Text.of("[Autosell] Помилка в циклі: " + e.getMessage()));
                sleep(5000);
            }
        }
    }

    private void interactAuction(ArrayList<String> args, PlayerEntity player) {
        MinecraftClient client = MinecraftClient.getInstance();

        for (int i = 1; i < 15; ++i) {
            sleep(args.contains("spookytime") ? 1500 : 350);
            if (client.player == null)
                break;

            ScreenHandler handler = player.currentScreenHandler;
            ItemStack item = handler.getSlot(0).getStack();
            if (item.isEmpty() || item.getItem().equals(Items.SHULKER_BOX) )
                break;

            client.interactionManager.clickSlot(handler.syncId, 0, 0,
                SlotActionType.PICKUP, player);
        }
    }


    private void sellItems(ArrayList<String> args, PlayerEntity player) {
        MinecraftClient client = MinecraftClient.getInstance();
        int maxLaps = args.contains("slots")
            ? Integer.parseInt(args.get(args.indexOf("slots") + 1)) : 3;
        int money = args.contains("money")
            ? Integer.parseInt(args.get(args.indexOf("money") + 1)) : 0;
        Random random = new Random();

        for (int i = 0; i < 9; ++i) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty() && !stack.getItem().equals(Items.SHULKER_BOX) ) {
                player.getInventory().setSelectedSlot(i);
                sleep(100);
                sendMessage(player.getInventory().getStack(i).toString());
                executeCommand("ah sell " + money);
                sleep(random.nextInt(300) + 200);
                if (!player.getInventory().getStack(i).isEmpty() && i < 2) {
                    sleep(500);
                    if (!player.getInventory().getSelectedStack().getItem().equals(Items.SHULKER_BOX))
                        executeCommand("ah sell " + money);
                }
            }
        }
    }
    private void shulkerCheck() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        // Захист
        if (mc.player.currentScreenHandler instanceof ShulkerBoxScreenHandler) {
            mc.getNetworkHandler().getConnection()
                .disconnect(Text.of("[Sell] Shulker opened"));
            return;
        }

        int usedSlots = 0;
        ArrayList<Integer> shulkers = new ArrayList<>();

        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);

            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof ShulkerBoxBlock) {
                shulkers.add(i);
            } else {
                usedSlots++;
            }
        }


        if (usedSlots > 9 || shulkers.isEmpty()) return;

        int shulkerSlot = shulkers.get(0);

        MinecraftClient client = MinecraftClient.getInstance();

        client.execute(() -> {
            client.getNetworkHandler().sendPacket(
                new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId)
            );
            client.player.closeHandledScreen();
        });

        sleep(800);
        sendMessage(Text.literal(
            "shulkers=" + shulkers.size() + " used=" + usedSlots
        ));

        // відкрити шалкер
        mc.execute(() -> {
            mc.interactionManager.clickSlot(
                mc.player.currentScreenHandler.syncId,
                shulkerSlot,
                1,
                SlotActionType.PICKUP,
                mc.player
            );
        });

        sleep(300);

        // забрати предмети
        mc.execute(() -> {
            if (!(mc.player.currentScreenHandler instanceof ShulkerBoxScreenHandler handler)) {
                sendMessage(Text.literal("[sell] not shulker"));
                return;
            }

            int moved = 0;
            for (int i = 0; i < 27 && moved < 9; i++) {
                if (!handler.getSlot(i).getStack().isEmpty()) {
                    mc.interactionManager.clickSlot(
                        handler.syncId,
                        i,
                        0,
                        SlotActionType.QUICK_MOVE,
                        mc.player
                    );
                    moved++;
                }
            }

        });
    }


    public Proxy createProxy(String proxyString) {
        String[] parts = proxyString.split(":");

        if (parts.length != 4) {
            MinecraftClient.getInstance().player.sendMessage(Text.of("Invalid proxy: " + proxyString), false);
        }

        return new Proxy.Builder()
            .name(parts[0] + ":" + parts[1])
            .type(ProxyType.Socks5)
            .address(parts[0])
            .port(Integer.parseInt(parts[1]))
            .username(parts[2])
            .password(parts[3])
            .enabled(false)
            .build();
    }
    public void disconnect(ArrayList<String> lowercaseArgs) {

        ArrayList<String> args = setup.getArgs();
        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> {
            if (client.getNetworkHandler() != null) {
                int proxyIndex = args.indexOf("proxy");
                if (proxyIndex != -1 && args.size() > proxyIndex + 3) {
                    sendMessage(Text.of(
                        "proxyIndex=" + proxyIndex + " args.size=" + args.size()
                    ));

                    String proxy1 = args.get(proxyIndex + 1);
                    String proxy2 = args.get(proxyIndex + 2);
                    String proxy3 = args.get(proxyIndex + 3);

                    int currentNameIndex = args.indexOf(client.player.getName().getString()) - args.indexOf("acc");
                    String currentProxy = args.get(proxyIndex+currentNameIndex);

                    if (currentProxy.equals(proxy1)) {
                        System.out.println("proxy 1");
                        sendMessage("proxy 1");
                        Proxies.get().removeAll();

                        Proxy newProxy = createProxy(proxy2);
                        Proxies.get().add(newProxy);
                        Proxies.get().setEnabled(newProxy, true);

                    } else if (currentProxy.equals(proxy2)) {
                        Proxies.get().removeAll();

                        System.out.println("proxy 2");
                        sendMessage("proxy 2");
                        Proxy newProxy = createProxy(proxy3);
                        Proxies.get().add(newProxy);
                        Proxies.get().setEnabled(newProxy, true);

                    } else if (currentProxy.equals(proxy3)) {
                        Proxies.get().removeAll();
                        System.out.println("proxy 3");
                        sendMessage("proxy 3");

                        Proxy newProxy = createProxy(proxy1);
                        Proxies.get().add(newProxy);
                        Proxies.get().setEnabled(newProxy, true);
                    } else {

                        Proxies.get().removeAll();
                        System.out.println("proxy 3");
                        sendMessage("proxy 3");

                        Proxy newProxy = createProxy(proxy1);
                        sendMessage(proxy1);
                        Proxies.get().add(newProxy);
                        Proxies.get().setEnabled(newProxy, true);
                    }


                }

            }

            if (client.getNetworkHandler() != null) {int accIndex = args.indexOf("acc");

                if (accIndex != -1 && args.size() > accIndex + 3) {

                    sendMessage(Text.of("12 f"));
                    sendMessage(Text.of(
                        "accIndex=" + accIndex + " args.size=" + args.size()
                    ));

                    sleep(2000);

                    String name2 = args.get(accIndex + 2);
                    String name3 = args.get(accIndex + 3);
                    String name1 = args.get(accIndex + 1);

                    String currentName = client.player.getName().getString();

                    sendMessage(Text.of("11 f"));

                    if (currentName.equals(name1)) {
                        sendMessage(Text.of("3 f"));
                        new NameChange().nameChange(name2, "Autosell. " + name2);
                    } else if (currentName.equals(name2)) {
                        sendMessage(Text.of("4 f"));
                        new NameChange().nameChange(name3, "Autosell. " + name3);
                    } else if (currentName.equals(name3)) {
                        sendMessage(Text.of("5 f"));
                        new NameChange().nameChange(name1, "Autosell. " + name1);
                    }
                }

            } else {
                client.disconnect(new TitleScreen(), true);
            }
        });
    }

    public void executeCommand(String command) {
        if (MinecraftClient.getInstance() == null || MinecraftClient.getInstance().player == null || MinecraftClient.getInstance().getNetworkHandler() == null || MinecraftClient.getInstance().world == null) {
            stopCycle();
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> {
            if (client.player != null && client.player.networkHandler != null)
                client.player.networkHandler.sendChatCommand(command);
        });
    }

    public void sendMessage(Text message) {
        if (MinecraftClient.getInstance().player == null) return;
        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> {
            MinecraftClient.getInstance().player.sendMessage(message, false);
        });
    }
    public void sendMessage(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (MinecraftClient.getInstance().player == null) return;
        client.execute(() -> {
            MinecraftClient.getInstance().player.sendMessage(Text.of(message), false);
        });
    }

    public void hotbarSwap() {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null || client.interactionManager == null)
            return;

        for (int gui = 8; gui < 36; ++gui) {
            ItemStack item = player.getInventory().getStack(gui);
            if (!item.isEmpty() && !item.getItem().equals(Items.SHULKER_BOX) ) {
                int hotbar = findEmptyHotbarSlot(player);
                if (hotbar < 0)
                    break;
                swapItems(hotbar, gui);
            }
        }
    }
    public void potionSwap() {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;

        if (player == null || client.interactionManager == null) return;

        int syncId = player.currentScreenHandler.syncId;

        for (int hotbar = 0; hotbar < 9; hotbar++) {
            ItemStack stack = player.getInventory().getStack(hotbar);

            if (stack.isEmpty()) continue;
            int hotbarSlot = 36 + hotbar; // офсет для хотбару

            client.interactionManager.clickSlot(
                syncId,
                hotbarSlot,
                0,
                SlotActionType.QUICK_MOVE, // FAST MOVE в інвентар
                player
            );
            sleep(200);
        }
        refillHotbar16();
    }

    boolean stash = false;
    public void refillHotbar16() {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null || client.interactionManager == null) return;

        int syncId = player.currentScreenHandler.syncId;

        for (int hotbar = 0; hotbar < 9; hotbar++) {
            int hotbarSlot = 36 + hotbar;
            ItemStack hotbarStack = player.getInventory().getStack(hotbar);
            List<String> args = this.setup.getArgs();

            if (!hotbarStack.isEmpty()&& hotbarStack.getCount() >= 16)
                continue;

            Item hotbarItem = hotbarStack.isEmpty() ? null : hotbarStack.getItem();

            for (int invSlot = 35; invSlot > 8; invSlot--) {
                ItemStack invStack = player.getInventory().getStack(invSlot);
                if (invStack.isEmpty()) continue;
                if (invStack.getCount() < 16 && args.indexOf("potion")>0) continue;
                if (hotbarItem != null && invStack.getItem() != hotbarItem) continue;
                if (!invStack.getItem().toString().equals(args.get(args.indexOf("item") + 1))) {
//                    if (!stash) {
//                        executeCommand("warp stash"); sleep(7500);;
//                    }
                    client.interactionManager.clickSlot(
                        player.currentScreenHandler.syncId,
                        invSlot,
                        1,
                        SlotActionType.THROW,
                        player
                    );
                    stash = true;
                }

                // взяли стак
                client.interactionManager.clickSlot(syncId, invSlot, 0, SlotActionType.PICKUP, player);
                sleep(500);
                List<String> argsuments = this.setup.getArgs();
                // кладемо ПО 1 — 16 разів
                int items = Integer.parseInt(argsuments.get(args.indexOf("items")+1));
                for (int i = 0; i < items; i++) {
                    client.interactionManager.clickSlot(syncId, hotbarSlot, 1, SlotActionType.PICKUP, player);
                    sleep(100);
                    if (args.contains("delay")) sleep(Integer.parseInt(args.get(args.indexOf("delay")+1)));
                }
                // повертаємо залишок
                sleep(300);
                client.interactionManager.clickSlot(syncId, findEmptyInvSlot(player), 0, SlotActionType.PICKUP, player);
//                if (stash) {
//                    executeCommand("spawn");
//                    sleep(7500);
//
//                    String record = "rec/" + ThreadLocalRandom.current().nextInt(1, 6) + ".rec";
//
//                    System.out.println("Loading: " + record);
//                    sendMessage("Loading: " + record);
//
//                    client.execute(() -> {
//                        try {
//                            if (client.player == null || client.world == null) {
//                                MeteorClient.LOG.warn("[Autosell] Cannot load record: player or world is null.");
//                                return;
//                            }
//
//                            PlayerRecorder.loadRecord(record);
//                            PlayerRecorder.startReplay();
//                            PlayerRecorder.togglePause();
//
//                            stash = false;
//                        } catch (Exception e) {
//                            MeteorClient.LOG.error("[Autosell] Failed to load record: {}", record, e);
//                        }
//                    });
//                }
                break;
            }
        }
    }





    private void swapItems(int hotbar, int gui) {
        MinecraftClient client = MinecraftClient.getInstance();
        client.interactionManager.clickSlot(
            client.player.currentScreenHandler.syncId, gui, hotbar,
            SlotActionType.SWAP, client.player);
    }


    private int findEmptyHotbarSlot(PlayerEntity player) {
        for (int i = 0; i < 9; ++i) {
            if (player.getInventory().getStack(i).isEmpty())
                return i;
        }
        return -1;
    }
    private int findEmptyInvSlot(PlayerEntity player) {
        for (int i = 9; i < 36; i++) {
            if (player.getInventory().getStack(i).isEmpty()) {
                return i; // тут GUI == inventory
            }
        }
        return -1;
    }


    public void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String getArgValue(ArrayList<String> args, String key1, String key2, String defaultVal) {
        int index = Math.max(args.indexOf(key1), args.indexOf(key2));
        return index >= 0 && index + 1 < args.size() ? args.get(index + 1) : defaultVal;
    }

    public boolean isInventoryEmpty() {
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
    public synchronized void stopCycle() {
        if (cycleThread != null) {
            cycleThread.interrupt();
            cycleThread = null;
        }
    }

    public Thread getCycleThread() {
        return cycleThread;
    }


    private boolean reconnectSubscribed = false;
    private boolean wasConnected = false;

    private String reconnectAddress = "funtime.su:25565";
    private ServerInfo reconnectServerInfo = new ServerInfo(
        "funtime.su",
        "funtime.su:25565",
        ServerInfo.ServerType.OTHER
    );

    @EventHandler
    private void onTick(TickEvent.Post event) {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.currentScreen instanceof DisconnectedScreen) {
            System.out.println("[Autosell] DisconnectedScreen detected");
        }

        tryReconnect();
    }
    private void tryReconnect() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return;

        // Запам'ятовуємо сервер, поки гравець підключений.
        if (mc.player != null
            && mc.world != null
            && mc.getNetworkHandler() != null) {

            ServerInfo current = mc.getCurrentServerEntry();

            if (current != null) {
                reconnectServerInfo = current;
                reconnectAddress = current.address;
            }

            wasConnected = true;
            return;
        }

        // Не перепідключаємося, якщо ще не було з'єднання.
        if (!wasConnected) return;

        // Спрацьовуємо тільки на екрані розриву з'єднання.
        if (!(mc.currentScreen instanceof DisconnectedScreen)) return;

        // Не частіше ніж раз на 5 секунд.
        long now = System.currentTimeMillis();
        if (now - lastReconnectTime < 5000) return;

        lastReconnectTime = now;

        mc.execute(() -> {
            try {
                if (mc.player != null
                    || mc.getNetworkHandler() != null
                    || !(mc.currentScreen instanceof DisconnectedScreen)) {
                    return;
                }

                System.out.println(
                    "[Autosell] Reconnecting to " + reconnectAddress
                );

                ConnectScreen.connect(
                    mc.currentScreen,
                    mc,
                    ServerAddress.parse(reconnectAddress),
                    reconnectServerInfo,
                    false,
                    null
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
