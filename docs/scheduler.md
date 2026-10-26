# Scheduler & Utils (`Scheduler` / `BlockPos` / `MathUtil`)

Small helpers around WoodLib: tick scheduling on the main thread, real millisecond timing on a dedicated thread, position types and the `/woodlib` debug command.

**Package:** `at.woodexplosive.woodlib.impl.Scheduler`, `at.woodexplosive.woodlib.api.utils.*`, `at.woodexplosive.woodlib.impl.utils.*`

> **Not Folia-safe:** The tick methods run via `Bukkit.getScheduler()`. For code that should run on Folia, use Paper's region/global/entity schedulers instead.

---

## 1. Tick-based (main thread, 1 tick = 50 ms)

```java
Scheduler.later(() -> player.sendMessage(Component.text("5 seconds later")), 100);
BukkitTask task = Scheduler.repeat(this::updateBoard, 0, 20);                 // every second
Scheduler.repeatUntil(() -> !player.isOnline(), this::tickEffect, 0, 5);      // until condition is true
Scheduler.repeatTimes(() -> countdown(player), 0, 20, 10);                    // exactly 10×
Scheduler.next(() -> gui.open(player));                                      // next tick
```

| Method | Description |
|---|---|
| `later(runnable, delayTicks)` | Once after a delay |
| `repeat(runnable, delayTicks, periodTicks)` | Repeating |
| `repeat(BukkitRunnable, delay, period)` | For tasks that end themselves via `cancel()` |
| `repeatUntil(stop, runnable, delay, period)` | `stop` is checked **before** each run; on `true` the task ends without running again |
| `repeatTimes(runnable, delay, period, times)` | Exactly `times` runs |
| `next(runnable)` | On the next tick |

All return a `BukkitTask` (`cancel()`). Bukkit API usage is allowed.

## 2. Millisecond-precise (dedicated thread)

These run on the daemon thread `WoodLib-Scheduler` (`ScheduledExecutorService`). **No Bukkit API** in `*Async` tasks.

| Method | Description |
|---|---|
| `laterMsAsync(runnable, delayMs)` | Once, async |
| `laterMsSync(runnable, delayMs)` | Precise timing, then handed to the main thread via `next` (Bukkit API allowed, + up to 50 ms latency) |
| `repeatMsAsync(runnable, delayMs, periodMs)` | Repeating; exceptions are logged, repetition continues |
| `repeatUntilMsAsync(stop, runnable, delayMs, periodMs)` | Until `stop` returns `true` |
| `repeatTimesMsAsync(runnable, delayMs, periodMs, times)` | Exactly `times` runs |

Returns: `ScheduledFuture<?>` (`cancel(false)`). `WoodLib.disable()` shuts down the executor and discards pending tasks.

---

## 3. Positions (`BlockPos` / `DirPos`)

| Type | Description |
|---|---|
| `BlockPos(x, y, z)` / `BlockPos.of(block)` | Immutable block position without a world; `offset(BlockFace)`, `toBlock(world)`, `equals`/`hashCode` → suitable as a map key |
| `DirPos(x, y, z, BlockFace)` / `DirPos.of(self, face)` | Position **plus** direction; `of` returns the neighbor in direction `face` – the format for `GenNode` connections ([uninode.md](./uninode.md)) |
| `DirPos.CARDINAL_6` | `UP, DOWN, NORTH, SOUTH, WEST, EAST` |

## 4. Odds and ends (`impl.utils`)

| Class | Method | Description |
|---|---|---|
| `MathUtil` | `intRange(first, last)` / `intRangeClosed(first, last)` | `List<Integer>` (exclusive/inclusive) – handy for slot lists: `MathUtil.intRange(0, 45)` |
| `CommandUtils` | `suggest(typed, suggestions...)` | Filter tab completion by prefix (case-insensitive) |
| `Tuple.Pair<X, Y>` | `key()`, `value()` | Simple record pair |

## 5. `/woodlib` command

Registered by `WoodLib.init` via the Paper lifecycle (no `paper-plugin.yml` entry needed), permission **`woodlib.debug`**.

| Command | Effect |
|---|---|
| `/woodlib netdebug` | Toggle network debug particles |
| `/woodlib netdebug on\|off` | Explicitly on/off |
