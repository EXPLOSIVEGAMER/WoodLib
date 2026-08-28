package at.woodexplosive.woodlib.api.gui.gui;

import java.util.EnumSet;

/**
 * Bitflags describing how a GUI closed, passed to {@link IGui#close(GuiExitFlag...)} /
 * {@link IGui#setExitFlags(GuiExitFlag...)} and read back via {@link IGui#getExitFlag()} (e.g. in
 * {@link at.woodexplosive.woodlib.api.gui.event.GuiCloseEvent#getExitFlag()}). The raw {@code byte} form
 * is a bitwise OR of the individual flags' {@link #getValue()}; use {@link #getFlags(byte)} to decode it
 * back into the atomic flags it was built from.
 */
public enum GuiExitFlag {
    /** No flags set. */
    DEFAULT((byte) 0, false),
    /** Also closes every parent GUI in the navigation stack, instead of just this one. */
    CLOSE_PARENTS((byte) 1, false),
    /** Suppresses the GUI's normal on-close handling for this close. */
    SKIP_ONCLOSE((byte) 2, false),

    /**
     * Convenience alias combining {@link #CLOSE_PARENTS} and {@link #SKIP_ONCLOSE} - the flags
     * {@link IGui#redraw()} closes with before immediately reopening. A "combined" flag: it is never
     * itself returned by {@link #getFlags(byte)}, which decodes its value back into the two atomic
     * flags it's made of.
     */
    REDRAW((byte) (1 | 2), true);

    private final byte value;
    private final boolean combined;

    GuiExitFlag(byte value, boolean combined) {
        this.value = value;
        this.combined = combined;
    }

    /**
     * @return this flag's bit value, for OR-ing into a raw exit flag byte
     */
    public byte getValue() {
        return value;
    }

    /**
     * Decodes a raw exit flag byte back into the atomic (non-{@link #combined}) flags it was built
     * from.
     * @param flag a raw exit flag byte, e.g. from {@link IGui#getExitFlag()}
     * @return the set of atomic flags present in {@code flag}, or {@code {DEFAULT}} if none are set
     */
    public static EnumSet<GuiExitFlag> getFlags(byte flag) {
        EnumSet<GuiExitFlag> flags = EnumSet.noneOf(GuiExitFlag.class);
        for (GuiExitFlag f : GuiExitFlag.values()) {
            if (f.combined || f.value == 0) continue;

            if ((flag & f.value) != 0) {
                flags.add(f);
            }
        }

        if (flags.isEmpty()) flags.add(DEFAULT);
        return flags;
    }

    /**
     * Combines multiple flags into a single raw exit flag byte via bitwise OR.
     * @param flags the flags to combine
     * @return the combined raw exit flag byte
     */
    public static byte combineFlags(GuiExitFlag... flags) {
        byte flag = 0;
        for (GuiExitFlag f : flags) flag = (byte) (flag | f.getValue());
        return flag;
    }

    /**
     * @param flags a raw exit flag byte
     * @return {@code true} if this flag's bit is <b>not</b> set in {@code flags}
     */
    public boolean isPresentIn(byte flags) {
        return (flags & this.value) == 0;
    }
}
