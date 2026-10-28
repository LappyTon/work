package me.seyit.mixin;

import me.seyit.NoMissClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void onAttack(CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (!NoMissClient.isEnabled()) {
            return;
        }

        // No Miss: не бити в повітря
        if (mc.crosshairTarget == null
                || mc.crosshairTarget.getType() != HitResult.Type.ENTITY) {
            cir.setReturnValue(false);
            return;
        }

        // No Weak Attack: тільки для алмазного та незеритового мечів
        if (mc.player != null) {
            var item = mc.player.getMainHandStack().getItem();

            if ((item == Items.DIAMOND_SWORD || item == Items.NETHERITE_SWORD)
                    && mc.player.getAttackCooldownProgress(0.0f) < 0.848f) {
                cir.setReturnValue(false);
            }
        }
    }
}