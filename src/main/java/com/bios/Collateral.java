package com.bios;

import net.fabricmc.api.ModInitializer;

import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.UUID;

public class Collateral implements ModInitializer {
	public static final String MOD_ID = "collateral";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final HashMap<UUID, Boolean> VEINMINE_PRESSED = new HashMap<>();

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		BreakBlockEvent.register();
		CollateralTasks.register();
		VeinminePayload.register();
	}
}