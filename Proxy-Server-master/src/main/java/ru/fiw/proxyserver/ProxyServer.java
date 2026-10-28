package ru.fiw.proxyserver;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.apache.commons.lang3.StringUtils;

import static ru.fiw.proxyserver.GuiProxy.isValidIpPort;

public class ProxyServer implements ModInitializer {
    public static boolean proxyEnabled = false;
    public static Proxy proxy = new Proxy();
    public static Proxy lastUsedProxy = new Proxy();
    public static ButtonWidget proxyMenuButton;

    public static String getLastUsedProxyIp() {
        return lastUsedProxy.ipPort.isEmpty() ? "none" : lastUsedProxy.getIp();
    }

    public static boolean setProxy(String ipPort, String username, String password) {
        if (!isValidIpPort(ipPort)) return false;
        Proxy newProxy = new Proxy(false, ipPort, username, password);
        Config.setDefaultProxy(newProxy);
        Config.saveConfig();
        MinecraftClient.getInstance().setScreen(new MultiplayerScreen(new TitleScreen()));
        return true;
    }

    @Override
    public void onInitialize() {
        Config.loadConfig();
    }
}
