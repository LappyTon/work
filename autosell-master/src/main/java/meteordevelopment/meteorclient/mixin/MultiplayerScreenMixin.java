/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.NameProtect;
import meteordevelopment.meteorclient.systems.proxies.Proxies;
import meteordevelopment.meteorclient.systems.proxies.Proxy;
import meteordevelopment.meteorclient.utils.render.color.Color;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Arrays;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(MultiplayerScreen.class)
public abstract class MultiplayerScreenMixin extends Screen {
    @Unique
    private int textColor1;

    @Unique
    private int textColor2;

    @Unique
    private String loggedInAs;

    @Unique
    private int loggedInAsLength;

    @Unique
    private ButtonWidget accounts;

    @Unique
    private ButtonWidget proxies;

    @Unique
    private TextFieldWidget autosellArgs;

    @Unique
    private ButtonWidget saveAutosellArgs;

    public MultiplayerScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "refreshWidgetPositions", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        textColor1 = Color.fromRGBA(255, 255, 255, 255);
        textColor2 = Color.fromRGBA(175, 175, 175, 255);

        loggedInAs = "Logged in as ";
        loggedInAsLength = textRenderer.getWidth(loggedInAs);

        // Accounts
        if (accounts == null) {
            accounts = addDrawableChild(
                new ButtonWidget.Builder(
                    Text.literal("Accounts"),
                    button -> client.setScreen(GuiThemes.get().accountsScreen())
                )
                    .size(75, 20)
                    .build()
            );
        }

        accounts.setPosition(this.width - 75 - 3, 3);

        // Proxies
        if (proxies == null) {
            proxies = addDrawableChild(
                new ButtonWidget.Builder(
                    Text.literal("Proxies"),
                    button -> client.setScreen(GuiThemes.get().proxiesScreen())
                )
                    .size(75, 20)
                    .build()
            );
        }

        proxies.setPosition(this.width - 75 - 3 - 75 - 2, 3);

        // Autosell arguments text field
        if (autosellArgs == null) {
            autosellArgs = new TextFieldWidget(
                textRenderer,
                3,
                3,
                250,
                20,
                Text.literal("Autosell arguments")
            );

            autosellArgs.setMaxLength(1000);

            // Load current arguments from AutosellSetup
            if (!MeteorClient.INSTANCE.getSetup().getArgs().isEmpty()) {
                autosellArgs.setText(
                    String.join(
                        " ",
                        MeteorClient.INSTANCE.getSetup().getArgs()
                    )
                );
            }

            addDrawableChild(autosellArgs);
        }

        // Position text field
        autosellArgs.setPosition(
            3,
            this.height - 45
        );

        // Save button
        if (saveAutosellArgs == null) {
            saveAutosellArgs = addDrawableChild(
                new ButtonWidget.Builder(
                    Text.literal("Save Autosell Args"),
                    button -> saveAutosellArguments()
                )
                    .size(150, 20)
                    .build()
            );
        }

        // Position save button
        saveAutosellArgs.setPosition(
            3,
            this.height - 22
        );
    }

    @Unique
    private void saveAutosellArguments() {
        String text = autosellArgs.getText().trim();

        ArrayList<String> argsList = new ArrayList<>();

        if (!text.isEmpty()) {
            argsList.addAll(
                Arrays.asList(
                    text.split("\\s+")
                )
            );
        }

        MeteorClient.INSTANCE.getSetup().setArgs(argsList);
    }

    @Override
    public void render(
        DrawContext context,
        int mouseX,
        int mouseY,
        float deltaTicks
    ) {
        super.render(
            context,
            mouseX,
            mouseY,
            deltaTicks
        );

        int x = 3;
        int y = 3;

        // Logged in as
        context.drawTextWithShadow(
            mc.textRenderer,
            loggedInAs,
            x,
            y,
            textColor1
        );

        context.drawTextWithShadow(
            mc.textRenderer,
            Modules.get()
                .get(NameProtect.class)
                .getName(client.getSession().getUsername()),
            x + loggedInAsLength,
            y,
            textColor2
        );

        y += textRenderer.fontHeight + 2;

        // Proxy
        Proxy proxy = Proxies.get().getEnabled();

        String left = proxy != null
            ? "Using proxy "
            : "Not using a proxy";

        String right = proxy != null
            ? (
            proxy.name.get() != null
                && !proxy.name.get().isEmpty()
                ? "(" + proxy.name.get() + ") "
                : ""
        )
            + proxy.address.get()
            + ":"
            + proxy.port.get()
            : null;

        context.drawTextWithShadow(
            mc.textRenderer,
            left,
            x,
            y,
            textColor1
        );

        if (right != null) {
            context.drawTextWithShadow(
                mc.textRenderer,
                right,
                x + textRenderer.getWidth(left),
                y,
                textColor2
            );
        }

        // Autosell label
        context.drawTextWithShadow(
            mc.textRenderer,
            "Autosell Args:",
            3,
            this.height - 58,
            textColor1
        );
    }
}
