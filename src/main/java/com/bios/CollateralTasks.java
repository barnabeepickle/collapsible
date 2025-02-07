package com.bios;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayDeque;
import java.util.Queue;

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
        while (!this.tasks.isEmpty()) {
            CollateralTask task = this.tasks.removeFirst();
            if (!task.poll()) {
                this.tasks.addFirst(task);
            }
        }
    }
}
