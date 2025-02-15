package com.bios.collateral;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class CollateralClient implements ClientModInitializer {
    public static KeyBinding VEINMINE;

    @Override
    public void onInitializeClient() {
        VEINMINE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.collateral.veinmine",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_GRAVE_ACCENT,
                "category.collateral"
        ));

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.world != null) {
                ClientPlayNetworking.send(new VeinminePayload(VEINMINE.isPressed()));
            }
        });
    }
}
