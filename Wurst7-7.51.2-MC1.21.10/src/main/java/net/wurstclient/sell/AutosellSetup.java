package net.wurstclient.sell;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
//import net.wurstclient.ocr.OcrCheck;
import net.wurstclient.


import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AutosellSetup {
    private AutosellCycle cycle = new AutosellCycle(this);;
    private AutosellRename rename = new AutosellRename();
    private int switches = 0;
    private ArrayList<String> args = new ArrayList<>();
    private List<String> logins;
    private List<String> passwords;

    private int loops = 0;
    private int tryes = 0;

    private long lastRestartTime = 0;
    private static final long RESTART_INTERVAL = 2 * 60 * 60 * 1000; // 2 години

    private boolean keyComboPreviouslyPressed = false;
    boolean renamerEnabled = false;

    public void setup() {
//		// Автоматичний рестарт циклу
//		ClientTickEvents.END_CLIENT_TICK.register(client -> {
//			if (cycle != null && client.player != null && client.world != null) {
//				long currentTime = System.currentTimeMillis();
//				if (currentTime - lastRestartTime >= RESTART_INTERVAL) {
//					restartCycle();
//					client.player.sendMessage(Text.of("§6[Autosell] §fCycle auto-restarted."), false);
//				}
//			}
//		});

        ClientSendMessageEvents.ALLOW_COMMAND.register((ClientSendMessageEvents.AllowCommand) (command) -> {
            String trimmedCommand = command.trim();
            String[] allCommands = trimmedCommand.split(" ");
            MinecraftClient.getInstance().player.sendMessage(Text.of(command));

            if (command.contains("read")){
                loadNicknames(Integer.parseInt(allCommands[1]));
                return false;
            }

            if (command.contains("swap")){
                cycle.hotbarSwap();
                MinecraftClient.getInstance().player.sendMessage(Text.of("---"));
                return false;
            }

            if (command.equals("a")){
                cycle.executeCommand("anvil");
                return false;
            }
            if (command.equals("get")){
                Look look = new Look();
                look.getCoords();
                return false;
            }
            if (command.equals("i")){
                Look look = new Look();
                look.lookOnCoords(90, -30);
                return false;
            }
            if (command.equals("o")){
                Look look = new Look();
                look.lookOnCoords(90, 0);
                return false;
            }
            if (command.equals("p")){
                Look look = new Look();
                look.lookOnCoords(90, 30);
                return false;
            }
            if (command.equals("j")){
                Look look = new Look();
                look.lookOnCoords(-90, -30);
                return false;
            }
            if (command.equals("k")){
                Look look = new Look();
                look.lookOnCoords(-90, 0);
                return false;
            }
            if (command.equals("l")){
                Look look = new Look();
                look.lookOnCoords(-90, 30);
                return false;
            }
            if (command.contains("stopautosell")) {
                Minecraft.getInstance().player.sendSystemMessage(Component.literal("hi"));
                stopCycle();
                return false;
            }
//			if (command.contains("ocr")) {
//				new OcrCheck().runScreenshotOcr();
//				return false;
//			}

            if (command.contains("autosell") || command.contains("фгещыулл") || command.contains("фгещіулл")) {
                MinecraftClient.getInstance().player.sendMessage(Text.of("§6[Autosell] §fEnabled."), false);
                startCycle();
                return false;
            }

            if (command.contains("capcha") && !command.contains("args")) {
                new NameChange().nameChange(logins.get(tryes));
                return false;
            }

            if (command.contains("rename")) {
                rename.rename(this, args);
                if (!renamerEnabled) {
                    renamerEnabled = true;
                    ClientTickEvents.END_CLIENT_TICK.register(client -> {
                        if (client.player != null && client.world != null) {
                            long windowHandle = client.getWindow().getHandle();

                            boolean isLeftAltDown = GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS;
                            boolean isShiftDown = GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS;
                            boolean isEnterDown = GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_ENTER) == GLFW.GLFW_PRESS;

                            boolean isComboPressedNow = isLeftAltDown && isShiftDown && isEnterDown;

                            if (isComboPressedNow && !keyComboPreviouslyPressed) {
                                rename.rename(this, args);
                                client.player.sendMessage(Text.of("Rename command executed!"), false);
                            }

                            keyComboPreviouslyPressed = isComboPressedNow;
                        }
                    });
                }
                return false;
            }

            if (command.contains("alt") && !command.contains("args")) {
                new NameChange().nameChange(Arrays.stream(command.split(" ")).toList().get(1));
                return false;
            }

            if (command.contains("1f23")) {
                MinecraftClient.getInstance().player.sendMessage(Text.literal(logins + " ||| " + passwords));
            }

            if (command.contains("args")) {
                tryes = 0;
                args = new ArrayList<>(Arrays.asList(command.split("\\s+")));
                MinecraftClient.getInstance().player.sendMessage(Text.literal(args.toString()));

                if (args.contains("capcha")) {
                    List<String> list = args.stream().map(o -> String.valueOf(o).toLowerCase()).collect(Collectors.toList());

                    int capcha = list.indexOf("capcha");
                    int pass = list.indexOf("pass");

                    logins = (capcha >= 0 && pass > capcha) ? list.subList(capcha + 1, pass) : Collections.emptyList();
                    passwords = (pass >= 0) ? list.subList(pass + 1, list.size()) : Collections.emptyList();

                    MinecraftClient.getInstance().player.sendMessage(Text.literal("Logins: " + logins + " | Passwords: " + passwords));
                }

                return false;
            }

            return true;
        });
    }

    public void startCycle() {
        if (cycle.getCycleThread() != null && cycle.getCycleThread().isAlive()) {
            cycle.stopCycle();
            cycle = new AutosellCycle(this);
        }

        cycle.cycleSetup(args);
        lastRestartTime = System.currentTimeMillis();
    }

    private void stopCycle() {
        if (cycle != null) {
            cycle.stopCycle();
            cycle = null;
        }
    }

    private void restartCycle() {
        stopCycle();
        startCycle();
    }

    public AutosellCycle getCycle() {
        return cycle;
    }

    public void setSwitches(int newswitches) {
        switches = newswitches;
    }

    public int getSwitches() {
        return switches;
    }

    public ArrayList<String> getArgs() {
        return args;
    }

    public List<String> getLogins() {
        return logins;
    }

    public List<String> getPasswords() {
        return passwords;
    }

    public int getTryes() {
        return tryes;
    }

    public void setTryes(int tryes) {
        this.tryes = tryes;
    }

    public int getLoops() {
        return loops;
    }

    public void setLoops(int loops) {
        this.loops = loops;
    }

    public void loopsPlusPlus() {
        loops++;
    }

    public static File getNicknamesFile(int num) {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path myDir = configDir.resolve("proxyserver");
        Path file = myDir.resolve("nicknames"+num+".txt");

        try {
            if (!Files.exists(myDir)) {
                Files.createDirectories(myDir);
            }
            if (!Files.exists(file)) {
                Files.createFile(file);
                Files.write(file, List.of("Player1", "Player2")); // стартові значення
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file.toFile();
    }

    public static List<String> loadNicknames(int number) {
        File file = getNicknamesFile(number);

        try {
            return Files.readAllLines(file.toPath())
                    .stream()
                    .filter(s -> !s.isBlank())
                    .toList();
        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }
}
