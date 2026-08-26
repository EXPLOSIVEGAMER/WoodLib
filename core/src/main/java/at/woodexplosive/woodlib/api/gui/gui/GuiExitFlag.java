package at.woodexplosive.woodlib.api.gui.gui;

import java.util.EnumSet;

public enum GuiExitFlag {
    DEFAULT((byte) 0, false),
    CLOSE_PARENTS((byte) 1, false),
    SKIP_ONCLOSE((byte) 2, false),

    REDRAW((byte) (1 | 2), true);

    private final byte value;
    private final boolean combined;

    GuiExitFlag(byte value, boolean combined) {
        this.value = value;
        this.combined = combined;
    }

    public byte getValue() {
        return value;
    }

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

    public static byte combineFlags(GuiExitFlag... flags) {
        byte flag = 0;
        for (GuiExitFlag f : flags) flag = (byte) (flag | f.getValue());
        return flag;
    }

    public boolean isPresentIn(byte flags) {
        return (flags & this.value) == 0;
    }
}
