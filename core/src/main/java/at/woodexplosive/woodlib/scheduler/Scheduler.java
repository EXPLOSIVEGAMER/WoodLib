package at.woodexplosive.woodlib.scheduler;

import at.woodexplosive.woodlib.WoodLib;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static at.woodexplosive.woodlib.WoodLib.plugin;

/**
 * Scheduling helpers for WoodLib.
 *
 * <p>The tick-based methods ({@link #later}, {@link #repeat}, {@link #repeatUntil},
 * {@link #repeatTimes}, {@link #next}) run on the Bukkit main thread; their resolution is one tick
 * (50 ms, 20 ticks = 1 s) and they may freely use the Bukkit API.</p>
 *
 * <p>The {@code *Ms} methods run on a dedicated {@link ScheduledExecutorService} with true
 * millisecond precision. {@link #laterMsAsync}, {@link #repeatMsAsync}, {@link #repeatUntilMsAsync}
 * and {@link #repeatTimesMsAsync} execute on the {@code WoodLib-Scheduler} thread and <b>must not
 * touch the Bukkit API</b>; {@link #laterMsSync} times precisely and then hops back onto the main
 * thread to run the task.</p>
 */
@SuppressWarnings("resource")
public final class Scheduler {

    private static ScheduledExecutorService exec;

    private Scheduler() {}

    // ---- Lifecycle (called by WoodLib.init / WoodLib.disable) ----

    /** Starts the millisecond executor. Idempotent; called by {@link WoodLib#init}. */
    public static void start() {
        if (exec != null && !exec.isShutdown()) return;
        exec = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "WoodLib-Scheduler");
            t.setDaemon(true);
            return t;
        });
    }

    /** Stops the millisecond executor and discards pending tasks. Called by {@link WoodLib#disable}. */
    public static void stop() {
        if (exec == null) return;
        exec.shutdownNow();
        exec = null;
    }

    /**
     * @return the running millisecond executor
     * @throws IllegalStateException if the library was not initialized
     */
    private static @NotNull ScheduledExecutorService exec() {
        if (exec == null) throw new IllegalStateException("WoodLib.init(plugin) was not called");
        return exec;
    }

    // ---- Tick-based (main thread, 50 ms resolution) ----

    /**
     * Runs {@code runnable} once after {@code delayTicks} ticks on the main thread (20 ticks = 1 s).
     * @param runnable the task to run
     * @param delayTicks the delay in ticks
     * @return the scheduled {@link BukkitTask}
     */
    public static BukkitTask later(Runnable runnable, long delayTicks) {
        return Bukkit.getScheduler().runTaskLater(plugin(), runnable, delayTicks);
    }

    /**
     * Repeats {@code runnable} every {@code periodTicks} ticks on the main thread, first after
     * {@code delayTicks}.
     * @param runnable the task to run
     * @param delayTicks the initial delay in ticks
     * @param periodTicks the period between runs, in ticks
     * @return the scheduled {@link BukkitTask}
     */
    public static BukkitTask repeat(Runnable runnable, long delayTicks, long periodTicks) {
        return Bukkit.getScheduler().runTaskTimer(plugin(), runnable, delayTicks, periodTicks);
    }

    /**
     * Schedules an existing {@link BukkitRunnable} as a repeating main-thread task. Use this when the
     * task needs to cancel itself via {@link BukkitRunnable#cancel()}.
     * @param runnable the runnable to schedule
     * @param delayTicks the initial delay in ticks
     * @param periodTicks the period between runs, in ticks
     * @return the scheduled {@link BukkitTask}
     */
    public static BukkitTask repeat(BukkitRunnable runnable, long delayTicks, long periodTicks) {
        return runnable.runTaskTimer(plugin(), delayTicks, periodTicks);
    }

    /**
     * Repeats {@code runnable} on the main thread until {@code stop} returns {@code true}. The
     * condition is checked before each run; once it is {@code true} the task cancels itself without
     * running again.
     * @param stop supplies the stop condition, evaluated before every run
     * @param runnable the task to run
     * @param delayTicks the initial delay in ticks
     * @param periodTicks the period between runs, in ticks
     * @return the scheduled {@link BukkitTask}
     */
    public static BukkitTask repeatUntil(Supplier<Boolean> stop, Runnable runnable, long delayTicks, long periodTicks) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (stop.get()) {
                    this.cancel();
                    return;
                }
                runnable.run();
            }
        }.runTaskTimer(plugin(), delayTicks, periodTicks);
    }

    /**
     * Repeats {@code runnable} on the main thread exactly {@code times} times.
     * @param runnable the task to run
     * @param delayTicks the initial delay in ticks
     * @param periodTicks the period between runs, in ticks
     * @param times how many times to run the task
     * @return the scheduled {@link BukkitTask}
     */
    public static BukkitTask repeatTimes(Runnable runnable, long delayTicks, long periodTicks, int times) {
        AtomicInteger count = new AtomicInteger();
        return repeatUntil(() -> count.getAndIncrement() >= times, runnable, delayTicks, periodTicks);
    }

    /**
     * Runs {@code runnable} on the main thread on the next tick.
     * @param runnable the task to run
     * @return the scheduled {@link BukkitTask}
     */
    public static BukkitTask next(Runnable runnable) {
        return Bukkit.getScheduler().runTask(plugin(), runnable);
    }

    // ---- True millisecond precision (async) ----

    /**
     * Runs {@code runnable} once after {@code delayMs} milliseconds with true ms precision, on the
     * {@code WoodLib-Scheduler} thread. <b>Do not call the Bukkit API.</b>
     * @param runnable the task to run
     * @param delayMs the delay in milliseconds
     * @return a {@link ScheduledFuture} that can be canceled
     */
    public static ScheduledFuture<?> laterMsAsync(Runnable runnable, long delayMs) {
        return exec().schedule(runnable, delayMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Times {@code runnable} with ms precision and then runs it on the main thread (Bukkit API
     * allowed). Note: the hop back onto the main thread can add up to 50 ms of latency.
     * @param runnable the task to run
     * @param delayMs the delay in milliseconds
     * @return a {@link ScheduledFuture} for the timing stage
     */
    public static ScheduledFuture<?> laterMsSync(Runnable runnable, long delayMs) {
        return exec().schedule(() -> next(runnable), delayMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Repeats {@code runnable} with ms precision on the {@code WoodLib-Scheduler} thread, first after
     * {@code delayMs}, then every {@code periodMs}. <b>Do not call the Bukkit API.</b>
     *
     * <p><b>Note:</b> if {@code runnable} throws, {@link ScheduledExecutorService#scheduleAtFixedRate}
     * silently stops the repetition. Guard the body yourself, or use {@link #repeatUntilMsAsync} /
     * {@link #repeatTimesMsAsync}, which already catch and log.</p>
     * @param runnable the task to run
     * @param delayMs the initial delay in milliseconds
     * @param periodMs the period between runs, in milliseconds
     * @return a {@link ScheduledFuture} that can be canceled
     */
    public static ScheduledFuture<?> repeatMsAsync(Runnable runnable, long delayMs, long periodMs) {
        return exec().scheduleAtFixedRate(
                () -> { try { runnable.run(); } catch (Throwable t) { WoodLib.logger().error("repeatUntilMsAsync task failed", t); } }
                , delayMs, periodMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Repeats {@code runnable} with ms precision until {@code stop} returns {@code true}, on the
     * {@code WoodLib-Scheduler} thread. The condition is checked before each run; exceptions thrown by
     * {@code runnable} are caught and logged so the repetition keeps running. <b>Do not call the
     * Bukkit API.</b>
     * @param stop supplies the stop condition, evaluated before every run
     * @param runnable the task to run
     * @param delayMs the initial delay in milliseconds
     * @param periodMs the period between runs, in milliseconds
     * @return a {@link ScheduledFuture} that can be canceled
     */
    public static ScheduledFuture<?> repeatUntilMsAsync(Supplier<Boolean> stop, Runnable runnable, long delayMs, long periodMs) {
        AtomicReference<ScheduledFuture<?>> ref = new AtomicReference<>();

        ref.set(repeatMsAsync(() -> {
            if (stop.get()) {
                ScheduledFuture<?> f = ref.get();
                if (f != null) f.cancel(false);
                return;
            }
            runnable.run();
        }, delayMs, periodMs));

        return ref.get();
    }

    /**
     * Repeats {@code runnable} with ms precision exactly {@code times} times, on the
     * {@code WoodLib-Scheduler} thread. Exceptions are caught and logged (via
     * {@link #repeatUntilMsAsync}). <b>Do not call the Bukkit API.</b>
     * @param runnable the task to run
     * @param delayMs the initial delay in milliseconds
     * @param periodMs the period between runs, in milliseconds
     * @param times how many times to run the task
     * @return a {@link ScheduledFuture} that can be canceled
     */
    public static ScheduledFuture<?> repeatTimesMsAsync(Runnable runnable, long delayMs, long periodMs, int times) {
        AtomicInteger count = new AtomicInteger();
        return repeatUntilMsAsync(() -> count.getAndIncrement() >= times, runnable, delayMs, periodMs);
    }
}
