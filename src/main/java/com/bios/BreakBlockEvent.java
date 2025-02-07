package com.bios;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BreakBlockEvent implements PlayerBlockBreakEvents.Before {
    @Override
    public boolean beforeBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {
        if (world.isClient) return true;

        if (Collateral.VEINMINE_PRESSED.getOrDefault(player.getUuid(), false)) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

            Veinmine.veinmine(serverPlayer, pos);

            return false;
        }

        return true;
    }

    public static void register() {
        PlayerBlockBreakEvents.BEFORE.register(new BreakBlockEvent());
    }
}
