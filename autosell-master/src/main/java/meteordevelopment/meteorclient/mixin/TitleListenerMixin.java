package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.MeteorClient;
import net.jasper.mod.automation.PlayerRecorder;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.ThreadLocalRandom;

@Mixin(ClientPlayNetworkHandler.class)
public class TitleListenerMixin {
    @Inject(method = "onTitle", at = @At("HEAD"))
    private void onTitle(TitleS2CPacket packet, CallbackInfo ci) {
        Text text = packet.text();

        if (!text.getString().contains("Успешно!")) return;

        MeteorClient.mc.execute(() -> {
            if (MeteorClient.mc.player == null) return;

            MeteorClient.mc.player.networkHandler.sendChatCommand("spawn");

            String record = "rec/" +
                ThreadLocalRandom.current().nextInt(1, 6) +
                ".rec";

            System.out.println("Loading: " + record);

            MeteorClient.mc.player.sendMessage(
                Text.of("Loading: " + record),
                false
            );

            PlayerRecorder.loadRecord(record);
            PlayerRecorder.startReplay();
            PlayerRecorder.togglePause();
        });
    }

    @Inject(method = "onSubtitle", at = @At("HEAD"))
    private void onSubtitle(SubtitleS2CPacket packet, CallbackInfo ci) {
        Text text = packet.text();

        System.out.println("[SUBTITLE] " + text.getString());
    }
}
