package com.luminabox.mixin;

import com.luminabox.audio.CustomMusicManager;
import com.luminabox.config.ModConfig;
import net.minecraft.client.sounds.MusicManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MusicManager.class)
public abstract class MusicManagerMixin {

    @Shadow public abstract void stopPlaying();

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(CallbackInfo ci) {
        if (ModConfig.getInstance().isReplaceVanillaMusic() &&
            CustomMusicManager.getInstance().getState() == CustomMusicManager.PlaybackState.PLAYING) {
            this.stopPlaying();
            ci.cancel();
        }
    }
}
