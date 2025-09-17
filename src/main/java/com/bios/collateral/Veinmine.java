package com.bios.collateral;

import com.bios.collateral.config.Config;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.Tag.TagEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.function.Predicate;

public class Veinmine implements CollateralTask {
    private final ServerWorld world;
    private final ServerPlayerEntity player;
    private final int lastSlot;
    private final Predicate<BlockState> blockPredicate;

    private final Set<BlockPos> searched;
    private final ArrayDeque<BlockPos> toSearch;

    private int totalBroken;
    private boolean toolBroken;

    private Veinmine(ServerWorld world, ServerPlayerEntity player, BlockPos pos, int lastSlot, BlockState blockState) {
        this.searched = new HashSet<>();
        this.toSearch = new ArrayDeque<>();
        this.toSearch.add(pos);

        this.world = world;
        this.player = player;
        this.lastSlot = lastSlot;

        List<TagEntry<Block>> tags = blockState
                .streamTags()
                .filter(tag -> Config.getConfig().connectedTags.contains(tag))
                .toList();

        if (tags.isEmpty()) {
            this.blockPredicate = state -> state.isOf(blockState.getBlock());
        } else {
            this.blockPredicate = state -> state.isOf(blockState.getBlock())
                    || tags.stream().anyMatch(state::isIn);
        }

        this.totalBroken = 0;
        this.toolBroken = false;
    }

    public static void veinmine(ServerPlayerEntity player, BlockPos pos) {
        int lastSlot = player.Inventory().selectedSlot;
        BlockState blockState = player.getEntityWorld().getBlockState(pos);

        Veinmine task = new Veinmine(player.getServerWorld(), player, pos, lastSlot, blockState);

        CollateralTasks.TASKS.add(task);
    }

    private boolean breakBlock(BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (!this.blockPredicate.test(state)) return false;
        if (!Config.getConfig().isBlockVeinmineable(state)) return false;

        List<ItemStack> drops = Block.getDroppedStacks(state, world, pos, world.getBlockEntity(pos), player, player.getMainHandStack());
        world.breakBlock(pos, false);
        player.getMainHandStack().damage(1, world, player, (item) -> toolBroken = true);

        for (ItemStack stack : drops) {
            if (stack.isEmpty()) continue;

            ItemEntity itemEntity = new ItemEntity(world, player.getX(), player.getY(), player.getZ(), stack, 0.0, 0.0, 0.0);
            itemEntity.resetPickupDelay();
            world.spawnEntity(itemEntity);
        }

        return true;
    }

    @Override
    public boolean poll() {
        if (this.toSearch.isEmpty()) {
            return true;
        }

        int brokenThisTick = 0;
        while (brokenThisTick < Config.getConfig().blockLimitPerTick) {
            if (this.toSearch.isEmpty()) {
                break;
            }

            if (playerStateChanged()) {
                return true;
            }

            BlockPos pos = this.toSearch.remove();
            boolean broke = this.breakBlock(pos);
            this.searched.add(pos);

            if (broke) {
                for (int x = -1; x < 2; x++) {
                    for (int y = -1; y < 2; y++) {
                        for (int z = -1; z < 2; z++) {
                            if (x == 0 && y == 0 && z == 0) continue;

                            BlockPos add = pos.add(x, y, z);

                            if (!this.searched.contains(add)) {
                                this.toSearch.add(add);
                            }
                        }
                    }
                }

                brokenThisTick += 1;
                totalBroken += 1;

                if (totalBroken >= Config.getConfig().blockLimit) {
                    return true;
                }
            }
        }

        return this.toSearch.isEmpty();
    }

    boolean playerStateChanged() {
        return this.lastSlot != player.getInventory().selectedSlot || this.toolBroken || !Collateral.VEINMINE_PRESSED.getOrDefault(player.getUuid(), false);
    }
}
