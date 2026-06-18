package com.luminabox.mixin;

import com.luminabox.client.gui.MusicPlayerScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {
    protected PauseScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        // Add "Music Player" button in top-left of the Pause screen
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.luminabox.pause_menu_btn"),
            button -> this.minecraft.gui.setScreen(new MusicPlayerScreen())
        ).bounds(10, 10, 90, 20).build());
    }
}
