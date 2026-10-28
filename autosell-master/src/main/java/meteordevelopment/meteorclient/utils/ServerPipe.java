    package meteordevelopment.meteorclient.utils;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class ServerPipe {
    private static final String INSTANCE =
        System.getProperty("mc.instance", "default");

    private static final String PIPE = "/tmp/mc-server-" + INSTANCE;

    public static void start() {
        File pipe = new File(PIPE);

        if (!pipe.exists()) {
            try {
                Process process = new ProcessBuilder("mkfifo", PIPE)
                    .start();

                if (process.waitFor() != 0) {
                    MeteorClient.LOG.error(
                        "Failed to create server pipe: {}", PIPE
                    );
                    return;
                }
            } catch (Exception e) {
                MeteorClient.LOG.error(
                    "Failed to create server pipe: {}", PIPE, e
                );
                return;
            }
        }

        Thread thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try (BufferedReader reader =
                         new BufferedReader(new FileReader(PIPE))) {

                    String address;

                    while ((address = reader.readLine()) != null) {
                        address = address.trim();

                        if (address.isEmpty()) continue;

                        String serverAddress = address;

                        MinecraftClient client =
                            MinecraftClient.getInstance();

                        client.execute(() -> {
                            try {
                                ServerInfo info = new ServerInfo(
                                    serverAddress,
                                    serverAddress,
                                    ServerInfo.ServerType.OTHER
                                );

                                ConnectScreen.connect(
                                    client.currentScreen,
                                    client,
                                    ServerAddress.parse(serverAddress),
                                    info,
                                    false,
                                    null
                                );
                            } catch (Exception e) {
                                MeteorClient.LOG.error(
                                    "Failed to connect to {}",
                                    serverAddress,
                                    e
                                );
                            }
                        });
                    }
                } catch (Exception e) {
                    MeteorClient.LOG.error(
                        "ServerPipe error: {}", PIPE, e
                    );

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }, "Meteor-ServerPipe-" + INSTANCE);

        thread.setDaemon(true);
        thread.start();

        MeteorClient.LOG.info(
            "ServerPipe started: {}", PIPE
        );
    }
}
