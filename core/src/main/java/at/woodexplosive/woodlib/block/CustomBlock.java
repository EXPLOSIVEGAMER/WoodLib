package at.woodexplosive.woodlib.block;

import at.woodexplosive.woodlib.api.block.CustomBlockPart;
import at.woodexplosive.woodlib.api.block.ICustomBlock;
import at.woodexplosive.woodlib.api.block.event.CustomBlockBreakEvent;
import at.woodexplosive.woodlib.api.block.event.CustomBlockInteractEvent;
import at.woodexplosive.woodlib.api.block.event.CustomBlockPlaceEvent;
import at.woodexplosive.woodlib.block.builder.CustomBlockBuilder;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

/**
 * Default {@link ICustomBlock} implementation. Built via {@link CustomBlockBuilder}.
 */
public class CustomBlock implements ICustomBlock {

    protected NamespacedKey id;
    protected List<CustomBlockPart> parts;
    protected boolean rotatable;

    protected @NotNull Consumer<CustomBlockInteractEvent> onInteract = event -> {};
    protected @NotNull Consumer<CustomBlockPlaceEvent> onPlace = event -> {};
    protected @NotNull Consumer<CustomBlockBreakEvent> onBreak = event -> {};

    public CustomBlock(@NotNull NamespacedKey id, @NotNull List<CustomBlockPart> parts, boolean rotatable,
                       @NotNull Consumer<CustomBlockInteractEvent> onInteract,
                       @NotNull Consumer<CustomBlockPlaceEvent> onPlace,
                       @NotNull Consumer<CustomBlockBreakEvent> onBreak) {
        this.id = id;
        this.parts = List.copyOf(parts);
        this.rotatable = rotatable;
        this.onInteract = onInteract;
        this.onPlace = onPlace;
        this.onBreak = onBreak;
    }

    protected CustomBlock() {}

    @Override
    public @NotNull NamespacedKey id() {
        return id;
    }

    @Override
    public @NotNull List<CustomBlockPart> parts() {
        return parts;
    }

    @Override
    public void onInteract(@NotNull CustomBlockInteractEvent event) {
        onInteract.accept(event);
    }

    @Override
    public void onBreak(@NotNull CustomBlockBreakEvent event) {
        onBreak.accept(event);
    }

    @Override
    public void onPlace(@NotNull CustomBlockPlaceEvent event) {
        onPlace.accept(event);
    }
}
