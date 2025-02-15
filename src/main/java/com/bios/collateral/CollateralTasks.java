package com.bios.collateral;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayDeque;

public class CollateralTasks implements ServerTickEvents.StartTick {
    public static final CollateralTasks TASKS = new CollateralTasks();

    private final ArrayDeque<CollateralTask> tasks;

    private CollateralTasks() {
        this.tasks = new ArrayDeque<>();
    }

    public void add(CollateralTask task) {
        this.tasks.add(task);
    }

    public static void register() {
        ServerTickEvents.START_SERVER_TICK.register(TASKS);
    }

    @Override
    public void onStartTick(MinecraftServer server) {
        int i = 0;
        while (i < this.tasks.size()) {
            if (this.tasks.getFirst().poll()) {
                this.tasks.removeFirst();
            } else {
                i++;
            }
        }
    }
}
