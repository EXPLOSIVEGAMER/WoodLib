package at.woodexplosive.woodlib.impl.command;

import at.woodexplosive.woodlib.api.uninode.UniNodespace;
import at.woodexplosive.woodlib.impl.interfaces.Internal;
import at.woodexplosive.woodlib.impl.utils.CommandUtils;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * {@code /woodlib netdebug [on|off]}: toggles {@link UniNodespace#setParticleDebug network debug particles}.
 * Without an argument the current state is flipped.
 */
@Internal
public class DebugCommand implements BasicCommand {

    private static final String USAGE = "Usage: /woodlib [options]";

    @Override
    public void execute(@NotNull CommandSourceStack source, @NotNull String @NotNull [] args) {
        if (args.length >= 1) {
            switch (args[0].toLowerCase()) {
                case "netdebug" -> netdebug(args, source);
            }
        }
    }

    private void netdebug(String[] args, CommandSourceStack source) {
        boolean enabled;
        if (args.length == 1) {
            enabled = !UniNodespace.isParticleDebug();
        } else {
            switch (args[1].toLowerCase(Locale.ROOT)) {
                case "on" -> enabled = true;
                case "off" -> enabled = false;
                default -> {
                    source.getSender().sendMessage(USAGE);
                    return;
                }
            }
        }

        UniNodespace.setParticleDebug(enabled);
        source.getSender().sendMessage("Network debug particles: " + (enabled ? "on" : "off"));
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack source, @NotNull String @NotNull [] args) {
        if (args.length <= 1) return CommandUtils.suggest(args.length == 0 ? "" : args[0], "netdebug");
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "netdebug" -> {
                    return CommandUtils.suggest(args[1], "on", "off");
                }
            }
        }
        return List.of();
    }

    @Override
    public @Nullable String permission() {
        return "woodlib.debug";
    }
}
