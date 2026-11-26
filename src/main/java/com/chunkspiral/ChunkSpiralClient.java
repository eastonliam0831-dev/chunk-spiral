package com.chunkspiral;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class ChunkSpiralClient implements ClientModInitializer {
    public static KeyMapping openMenuKey;
    public static KeyMapping toggleKey;

    @Override
    public void onInitializeClient() {
        FlyerConfig.load();

        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("chunkspiral", "main"));

        openMenuKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.chunkspiral.menu", InputConstants.KEY_J, category));
        toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.chunkspiral.toggle", InputConstants.KEY_K, category));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (openMenuKey.consumeClick()) {
                if (mc.player != null && mc.gui.screen() == null) mc.gui.setScreen(new FlyerScreen(null));
            }
            while (toggleKey.consumeClick()) {
                if (mc.player != null && mc.gui.screen() == null) SpiralFlyer.toggle(mc);
            }
            SpiralFlyer.tick(mc);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, mc) -> SpiralFlyer.reset());
    }
}
