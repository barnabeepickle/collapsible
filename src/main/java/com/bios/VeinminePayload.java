package com.bios;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public record VeinminePayload(boolean pressed) implements CustomPayload {
    public static final Identifier PACKET_ID = Collateral.id("veinmine");
    public static final Id<VeinminePayload> ID = new Id<>(PACKET_ID);
    public static final PacketCodec<RegistryByteBuf, VeinminePayload> CODEC = PacketCodec.tuple(PacketCodecs.BOOLEAN, VeinminePayload::pressed, VeinminePayload::new);

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        registerServer();
    }

    private static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
            UUID playerId = context.player().getUuid();
            Collateral.VEINMINE_PRESSED.put(playerId, payload.pressed);
        });
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
