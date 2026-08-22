package org.wargamer2010.signshop.scheduling;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Scheduler abstraction supporting both standard Bukkit and Folia region-based scheduling.
 * Detects the server type at runtime and dispatches to the appropriate implementation.
 */
public class SchedulerAdapter {
    private final Plugin plugin;
    private final boolean isFolia;
    
    private Method getRegionSchedulerMethod;
    private Method getGlobalRegionSchedulerMethod;
    private Method getAsyncSchedulerMethod;
    private Method runMethod;
    private Method runDelayedMethod;
    private Method runAtFixedRateMethod;
    private Method executeMethod;
    private Method runDelayedAsyncMethod;
    private Method cancelTasksMethod;

    public SchedulerAdapter(Plugin plugin) {
        this.plugin = plugin;
        this.isFolia = FoliaDetector.isFolia();
        
        if (isFolia) {
            initializeFoliaReflection();
        }
    }

    private void initializeFoliaReflection() {
        try {
            plugin.getLogger().info("[Folia] Initializing Folia scheduler reflection...");
            
            Class<?> serverClass = Bukkit.getServer().getClass();
            plugin.getLogger().info("[Folia] Server class: " + serverClass.getName());
            
            plugin.getLogger().info("[Folia] Getting scheduler methods from Server...");
            getRegionSchedulerMethod = serverClass.getMethod("getRegionScheduler");
            plugin.getLogger().info("[Folia]   ✓ getRegionScheduler()");
            
            getGlobalRegionSchedulerMethod = serverClass.getMethod("getGlobalRegionScheduler");
            plugin.getLogger().info("[Folia]   ✓ getGlobalRegionScheduler()");
            
            getAsyncSchedulerMethod = serverClass.getMethod("getAsyncScheduler");
            plugin.getLogger().info("[Folia]   ✓ getAsyncScheduler()");
            
            plugin.getLogger().info("[Folia] Loading RegionScheduler class...");
            Class<?> regionSchedulerClass = Class.forName("io.papermc.paper.threadedregions.scheduler.RegionScheduler");
            plugin.getLogger().info("[Folia]   ✓ RegionScheduler loaded");
            
            plugin.getLogger().info("[Folia] Getting RegionScheduler methods...");
            // Folia uses Consumer<ScheduledTask> instead of Runnable
            runMethod = regionSchedulerClass.getMethod("run", Plugin.class, Location.class, Consumer.class);
            plugin.getLogger().info("[Folia]   ✓ run(Plugin, Location, Consumer)");
            
            runDelayedMethod = regionSchedulerClass.getMethod("runDelayed", Plugin.class, Location.class, Consumer.class, long.class);
            plugin.getLogger().info("[Folia]   ✓ runDelayed(Plugin, Location, Consumer, long)");
            
            plugin.getLogger().info("[Folia] Loading GlobalRegionScheduler class...");
            Class<?> globalRegionSchedulerClass = Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            plugin.getLogger().info("[Folia]   ✓ GlobalRegionScheduler loaded");
            
            plugin.getLogger().info("[Folia] Getting GlobalRegionScheduler methods...");
            // Folia uses Consumer<ScheduledTask> instead of Runnable
            runAtFixedRateMethod = globalRegionSchedulerClass.getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class);
            plugin.getLogger().info("[Folia]   ✓ runAtFixedRate(Plugin, Consumer, long, long)");
            
            plugin.getLogger().info("[Folia] Loading AsyncScheduler class...");
            Class<?> asyncSchedulerClass = Class.forName("io.papermc.paper.threadedregions.scheduler.AsyncScheduler");
            plugin.getLogger().info("[Folia]   ✓ AsyncScheduler loaded");
            
            plugin.getLogger().info("[Folia] Getting AsyncScheduler methods...");
            // Folia uses Consumer<ScheduledTask> instead of Runnable
            runDelayedAsyncMethod = asyncSchedulerClass.getMethod("runDelayed", Plugin.class, Consumer.class, long.class, TimeUnit.class);
            plugin.getLogger().info("[Folia]   ✓ runDelayed(Plugin, Consumer, long, TimeUnit)");
            
            plugin.getLogger().info("[Folia] Getting cancelTasks method...");
            try {
                cancelTasksMethod = serverClass.getMethod("cancelTasks", Plugin.class);
                plugin.getLogger().info("[Folia]   ✓ cancelTasks(Plugin)");
            } catch (NoSuchMethodException e) {
                plugin.getLogger().warning("[Folia]   ⚠ cancelTasks(Plugin) not found - task cancellation will use alternative method");
                cancelTasksMethod = null;
            }
            
            plugin.getLogger().info("[Folia] ✓ Folia reflection initialized successfully!");
        } catch (Exception e) {
            plugin.getLogger().severe("[Folia] ✗ Failed to initialize Folia reflection!");
            plugin.getLogger().severe("[Folia] Error details: " + e.getClass().getName() + ": " + e.getMessage());
            if (e.getCause() != null) {
                plugin.getLogger().severe("[Folia] Caused by: " + e.getCause().getClass().getName() + ": " + e.getCause().getMessage());
            }
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize Folia reflection", e);
        }
    }

    /**
     * Schedules a task on the region that owns the given location.
     * On Bukkit, runs synchronously on the main thread.
     */
    public void runAtLocation(Location location, Runnable task) {
        if (isFolia) {
            try {
                Object regionScheduler = getRegionSchedulerMethod.invoke(Bukkit.getServer());
                // Folia expects Consumer<ScheduledTask>, wrap our Runnable
                Consumer<Object> consumer = (scheduledTask) -> task.run();
                runMethod.invoke(regionScheduler, plugin, location, consumer);
            } catch (Exception e) {
                throw new RuntimeException("Failed to schedule Folia region task", e);
            }
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    /**
     * Schedules a delayed task on the region that owns the given location.
     * On Bukkit, runs synchronously on the main thread after delay.
     */
    public void runAtLocationLater(Location location, Runnable task, long delayTicks) {
        if (isFolia) {
            try {
                Object regionScheduler = getRegionSchedulerMethod.invoke(Bukkit.getServer());
                // Folia expects Consumer<ScheduledTask>, wrap our Runnable
                Consumer<Object> consumer = (scheduledTask) -> task.run();
                runDelayedMethod.invoke(regionScheduler, plugin, location, consumer, delayTicks);
            } catch (Exception e) {
                throw new RuntimeException("Failed to schedule delayed Folia region task", e);
            }
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }

    /**
     * Schedules a task on the region that owns the given entity.
     * On Bukkit, runs synchronously on the main thread.
     */
    public void runAtEntity(Entity entity, Runnable task) {
        runAtLocation(entity.getLocation(), task);
    }

    /**
     * Schedules a delayed task on the region that owns the given entity.
     * On Bukkit, runs synchronously on the main thread after delay.
     */
    public void runAtEntityLater(Entity entity, Runnable task, long delayTicks) {
        runAtLocationLater(entity.getLocation(), task, delayTicks);
    }

    /**
     * Schedules a task on the region that owns the given block.
     * On Bukkit, runs synchronously on the main thread.
     */
    public void runAtBlock(Block block, Runnable task) {
        runAtLocation(block.getLocation(), task);
    }

    /**
     * Schedules a delayed task on the region that owns the given block.
     * On Bukkit, runs synchronously on the main thread after delay.
     */
    public void runAtBlockLater(Block block, Runnable task, long delayTicks) {
        runAtLocationLater(block.getLocation(), task, delayTicks);
    }

    /**
     * Schedules a repeating task globally.
     * On Bukkit, runs synchronously on the main thread.
     */
    public ScheduledTask runTimer(Runnable task, long delayTicks, long periodTicks) {
        if (isFolia) {
            try {
                Object globalRegionScheduler = getGlobalRegionSchedulerMethod.invoke(Bukkit.getServer());
                // Folia expects Consumer<ScheduledTask>, wrap our Runnable
                Consumer<Object> consumer = (scheduledTask) -> task.run();
                // Folia requires delay > 0, but Bukkit allows 0
                long foliaDelay = Math.max(1, delayTicks);
                Object foliaTask = runAtFixedRateMethod.invoke(globalRegionScheduler, plugin, consumer, foliaDelay, periodTicks);
                return new FoliaScheduledTask(foliaTask);
            } catch (Exception e) {
                throw new RuntimeException("Failed to schedule Folia repeating task", e);
            }
        } else {
            int taskId = Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks).getTaskId();
            return new BukkitScheduledTask(taskId);
        }
    }

    /**
     * Schedules an asynchronous task.
     * Safe for non-Bukkit API operations only.
     */
    public void runAsync(Runnable task) {
        if (isFolia) {
            try {
                Object asyncScheduler = getAsyncSchedulerMethod.invoke(Bukkit.getServer());
                // Folia expects Consumer<ScheduledTask>, wrap our Runnable
                Consumer<Object> consumer = (scheduledTask) -> task.run();
                runDelayedAsyncMethod.invoke(asyncScheduler, plugin, consumer, 0L, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                throw new RuntimeException("Failed to schedule Folia async task", e);
            }
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }

    /**
     * Schedules a delayed asynchronous task.
     * Safe for non-Bukkit API operations only.
     */
    public void runAsyncLater(Runnable task, long delayTicks) {
        if (isFolia) {
            try {
                Object asyncScheduler = getAsyncSchedulerMethod.invoke(Bukkit.getServer());
                long delayMillis = delayTicks * 50; // Convert ticks to milliseconds
                // Folia expects Consumer<ScheduledTask>, wrap our Runnable
                Consumer<Object> consumer = (scheduledTask) -> task.run();
                runDelayedAsyncMethod.invoke(asyncScheduler, plugin, consumer, delayMillis, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                throw new RuntimeException("Failed to schedule delayed Folia async task", e);
            }
        } else {
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks);
        }
    }

    /**
     * Schedules a repeating asynchronous task.
     * Returns a wrapper that allows cancellation.
     */
    public ScheduledTask runAsyncTimer(Runnable task, long delayTicks, long periodTicks) {
        if (isFolia) {
            return new FoliaAsyncRepeatingTask(this, task, delayTicks, periodTicks);
        } else {
            int taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks).getTaskId();
            return new BukkitScheduledTask(taskId);
        }
    }

    /**
     * Cancels all tasks scheduled by this plugin.
     */
    public void cancelAllTasks() {
        if (isFolia) {
            if (cancelTasksMethod != null) {
                try {
                    cancelTasksMethod.invoke(Bukkit.getServer(), plugin);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to cancel Folia tasks", e);
                }
            } else {
                // Folia doesn't have cancelTasks method - tasks are managed by schedulers
                // Individual tasks should be cancelled via their ScheduledTask handles
                plugin.getLogger().info("[Folia] cancelAllTasks not available - tasks should be cancelled individually");
            }
        } else {
            Bukkit.getScheduler().cancelTasks(plugin);
        }
    }

    /**
     * Represents a scheduled task that can be cancelled.
     */
    public interface ScheduledTask {
        void cancel();
        int getTaskId();
        boolean isCancelled();
    }

    private static class BukkitScheduledTask implements ScheduledTask {
        private final int taskId;
        private volatile boolean cancelled = false;

        public BukkitScheduledTask(int taskId) {
            this.taskId = taskId;
        }

        @Override
        public void cancel() {
            Bukkit.getScheduler().cancelTask(taskId);
            cancelled = true;
        }

        @Override
        public int getTaskId() {
            return taskId;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }
    }

    private static class FoliaScheduledTask implements ScheduledTask {
        private final Object foliaTask;
        private Method cancelMethod;
        private Method isCancelledMethod;

        public FoliaScheduledTask(Object foliaTask) {
            this.foliaTask = foliaTask;
            try {
                Class<?> taskClass = Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask");
                cancelMethod = taskClass.getMethod("cancel");
                isCancelledMethod = taskClass.getMethod("isCancelled");
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize Folia task reflection", e);
            }
        }

        @Override
        public void cancel() {
            try {
                cancelMethod.invoke(foliaTask);
            } catch (Exception e) {
                throw new RuntimeException("Failed to cancel Folia task", e);
            }
        }

        @Override
        public int getTaskId() {
            return -1; // Folia tasks don't have numeric IDs
        }

        @Override
        public boolean isCancelled() {
            try {
                return (boolean) isCancelledMethod.invoke(foliaTask);
            } catch (Exception e) {
                throw new RuntimeException("Failed to check Folia task cancellation", e);
            }
        }
    }

    /**
     * Manual implementation of repeating async task for Folia.
     * Folia's async scheduler doesn't support repeating tasks directly.
     */
    private static class FoliaAsyncRepeatingTask implements ScheduledTask, Runnable {
        private final SchedulerAdapter adapter;
        private final Runnable task;
        private final long periodTicks;
        private volatile boolean cancelled = false;

        public FoliaAsyncRepeatingTask(SchedulerAdapter adapter, Runnable task, long delayTicks, long periodTicks) {
            this.adapter = adapter;
            this.task = task;
            this.periodTicks = periodTicks;
            adapter.runAsyncLater(this, delayTicks);
        }

        @Override
        public void run() {
            if (cancelled) {
                return;
            }
            
            try {
                task.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            if (!cancelled) {
                adapter.runAsyncLater(this, periodTicks);
            }
        }

        @Override
        public void cancel() {
            cancelled = true;
        }

        @Override
        public int getTaskId() {
            return -1;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }
    }
}
