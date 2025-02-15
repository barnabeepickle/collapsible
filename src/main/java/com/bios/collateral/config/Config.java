package com.bios.collateral.config;

import com.bios.collateral.Collateral;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Config {
    private static Config CONFIG = null;

    public static Config getConfig() {
        if (CONFIG == null) {
            CONFIG = read();
        }
        return CONFIG;
    }

    public int blockLimit = 512;
    public int blockLimitPerTick = 27;
    public Set<TagKey<Block>> connectedTags = Stream.of(
            BlockTags.COAL_ORES,
            BlockTags.COPPER_ORES,
            BlockTags.IRON_ORES,
            BlockTags.GOLD_ORES,
            BlockTags.REDSTONE_ORES,
            BlockTags.LAPIS_ORES,
            BlockTags.EMERALD_ORES,
            BlockTags.DIAMOND_ORES
    ).collect(Collectors.toSet());

    public static Config read() {
        Gson gson = new Gson();
        String json;
        try {
            json = Files.readString(configPath());
        } catch (NoSuchFileException e) {
            Collateral.LOGGER.info("config file not found, creating a new one");
            Config config = new Config();
            config.write();
            return config;
        } catch (IOException e) {
            Collateral.LOGGER.warn("error reading config file: ", e);
            return new Config();
        }

        JsonConfig config = gson.fromJson(json, JsonConfig.class);
        return config.toConfig();
    }

    public static Config getDefault() {
        return new Config();
    }

    private Config() {}

    public void write() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        String json = gson.toJson(new JsonConfig(this));
        try {
            Files.writeString(configPath(), json, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            Collateral.LOGGER.warn("error writing config file: ", e);
        }
    }

    static Path configPath() {
        return FabricLoader
                .getInstance()
                .getConfigDir()
                .resolve(Collateral.MOD_ID + ".json");
    }

    static class JsonConfig {
        int blockLimit;
        int blockLimitPerTick;
        Set<String> connectedTags;

        JsonConfig(Config config) {
            this.blockLimit = config.blockLimit;
            this.blockLimitPerTick = config.blockLimitPerTick;
            this.connectedTags = config
                    .connectedTags
                    .stream()
                    .map(TagKey::id)
                    .map(Identifier::toString)
                    .collect(Collectors.toSet());
        }

        Config toConfig() {
            Config config = new Config();
            config.blockLimit = this.blockLimit;
            config.blockLimitPerTick = this.blockLimitPerTick;
            config.connectedTags = this
                    .connectedTags
                    .stream()
                    .map(s -> TagKey.of(RegistryKeys.BLOCK, Identifier.of(s)))
                    .collect(Collectors.toSet());
            return config;
        }
    }
}
