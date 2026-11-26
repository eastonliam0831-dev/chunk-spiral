package com.chunkspiral;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class ChunkSpiralClient implements ClientModInitializer {
    public static KeyMapping openMenuKey;
    public static KeyMapping toggleKey;

    @Override
    public void onInitializeClient() {
        FlyerConfig.load();

        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("chunkspiral", "main"));

        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.chunkspiral.menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, category));
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.chunkspiral.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, category));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (openMenuKey.consumeClick()) {
                if (mc.player != null && mc.screen == null) mc.setScreen(new FlyerScreen(null));
            }
            while (toggleKey.consumeClick()) {
                if (mc.player != null && mc.screen == null) SpiralFlyer.toggle(mc);
            }
            SpiralFlyer.tick(mc);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, mc) -> SpiralFlyer.reset());
    }
}
