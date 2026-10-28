package meteordevelopment.meteorclient.commands.sellcommands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.sell.AutosellSetup;
import meteordevelopment.meteorclient.sell.NameChange;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AltCommand extends Command {
    AutosellSetup setup;
    public AltCommand() {
        super("alt", "Args Autosell Command");
        this.setup = MeteorClient.INSTANCE.getSetup();
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {

        RequiredArgumentBuilder<CommandSource, String> argsArgument =
            RequiredArgumentBuilder.<CommandSource, String>argument("args", StringArgumentType.greedyString())
                .executes(context -> {
                    String args = StringArgumentType.getString(context, "args");
                    ArrayList<String> argsList = new ArrayList<>(Arrays.asList(args.split(" ")));
                    new NameChange().nameChange(
                        argsList.get(0),
                        "alt " + argsList.get(0)
                    );                    return 1;
                });

        builder.then(argsArgument);
    }
}
