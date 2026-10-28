package meteordevelopment.meteorclient.utils;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.MinecraftClient;

import java.io.*;

public class ChatPipe {
    private static final String INSTANCE =
        System.getProperty("mc.instance", "default");

    private static final String PIPE = "/tmp/mc-chat-" + INSTANCE;

    public static void start() {
        File pipe = new File(PIPE);

        if (!pipe.exists()) {
            try {
                Process process = new ProcessBuilder("mkfifo", PIPE)
                    .start();

                if (process.waitFor() != 0) {
                    MeteorClient.LOG.error("Failed to create FIFO: {}", PIPE);
                    return;
                }
            } catch (Exception e) {
                MeteorClient.LOG.error("Failed to create chat pipe", e);
                return;
            }
        }

        Thread thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try (BufferedReader reader =
                         new BufferedReader(new FileReader(PIPE))) {

                    String message;

                    while ((message = reader.readLine()) != null) {
                        message = message.trim();

                        if (message.isEmpty()) continue;

                        String command = message;

                        MinecraftClient.getInstance().execute(() -> {
                            var client = MinecraftClient.getInstance();

                            if (client.player == null) return;

                            if (command.startsWith("/")) {
                                client.player.networkHandler.sendChatCommand(
                                    command.substring(1)
                                );
                            } else {
                                client.player.networkHandler.sendChatMessage(
                                    command
                                );
                            }
                        });
                    }
                } catch (Exception e) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }, "Meteor-ChatPipe-" + INSTANCE);

        thread.setDaemon(true);
        thread.start();

        MeteorClient.LOG.info("ChatPipe started: {}", PIPE);
    }
}
