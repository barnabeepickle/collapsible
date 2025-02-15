package com.bios.collateral.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ConfigScreen {
    public static Screen getScreen(Screen parent) {
        Config config = Config.getConfig();
        Config def = Config.getDefault();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("title.collateral.config"))
                .setSavingRunnable(config::write);

        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("general"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder.startIntField(Text.translatable("config.collateral.block_limit"), config.blockLimit)
                .setDefaultValue(def.blockLimit)
                .setTooltip(Text.translatable("config.collateral.block_limit.tooltip"))
                .setSaveConsumer(next -> config.blockLimit = next)
                .build());

        general.addEntry(entryBuilder.startIntField(Text.translatable("config.collateral.block_limit_per_tick"), config.blockLimitPerTick)
                .setDefaultValue(def.blockLimitPerTick)
                .setTooltip(Text.translatable("config.collateral.block_limit_per_tick.tooltip"))
                .setSaveConsumer(next -> config.blockLimitPerTick = next)
                .build());

        List<String> defaultTags = def.connectedTags.stream().map(tag -> tag.id().toString()).toList();
        List<String> tags = config.connectedTags.stream().map(tag -> tag.id().toString()).toList();

        general.addEntry(entryBuilder.startStrList(Text.translatable("config.collateral.connected_tags"), tags)
                .setDefaultValue(defaultTags)
                .setTooltip(Text.translatable("config.collateral.connected_tags.tooltip"))
                .setSaveConsumer(next -> config.connectedTags = next
                        .stream()
                        .map(Identifier::tryParse)
                        .filter(Objects::nonNull)
                        .map(i -> TagKey.of(RegistryKeys.BLOCK, i))
                        .collect(Collectors.toSet()))
                .build());

        ListType defaultListType = def.banList ? ListType.BanList : ListType.AllowList;
        ListType listType = config.banList ? ListType.BanList : ListType.AllowList;

        general.addEntry(entryBuilder.startSelector(Text.translatable("config.collateral.list_type"), new ListType[] { ListType.AllowList, ListType.BanList }, listType)
                        .setDefaultValue(defaultListType)
                        .setTooltip(Text.translatable("config.collateral.list_type.tooltip"))
                        .setSaveConsumer(next -> config.banList = next == ListType.BanList)
                .build());

        List<String> defaultBlocks = config.blockList.stream().map(Registries.BLOCK::getId).map(Identifier::toString).toList();
        List<String> blocks = config.blockList.stream().map(Registries.BLOCK::getId).map(Identifier::toString).toList();

        general.addEntry(entryBuilder.startStrList(Text.translatable("config.collateral.block_list"), blocks)
                .setDefaultValue(defaultBlocks)
                .setTooltip(Text.translatable("config.collateral.block_list.tooltip"))
                .setSaveConsumer(next -> config.blockList = next
                        .stream()
                        .map(Identifier::tryParse)
                        .filter(Objects::nonNull)
                        .map(Registries.BLOCK::get)
                        .collect(Collectors.toSet()))
                .build());

        return builder.build();
    }

    private enum ListType {
        AllowList,
        BanList
    }
}
