/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.commands.sellcommands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.sell.AutosellSetup;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

public class AutosellCommand extends Command {
    AutosellSetup setup;
    public AutosellCommand() {
        super("autosell", "Args Autosell Command");
        this.setup = MeteorClient.INSTANCE.getSetup();
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(args -> {
            sendMessage("[sell] starting autosell...");
            setup.getCycle().cycleSetup(setup.getArgs());
            return 1;
        });
    }

    private void sendMessage(String msg) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.player != null) {
            client.inGameHud.getChatHud().addMessage(Text.of(msg));
        }
    }
}
