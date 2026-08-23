package at.woodexplosive.woodlib.block.builder;

import at.woodexplosive.woodlib.api.block.CustomBlockPart;
import at.woodexplosive.woodlib.api.block.DisplayDefinition;
import at.woodexplosive.woodlib.api.block.ICustomBlock;
import at.woodexplosive.woodlib.api.block.builder.ICustomBlockBuilder;
import at.woodexplosive.woodlib.api.block.event.CustomBlockBreakEvent;
import at.woodexplosive.woodlib.api.block.event.CustomBlockInteractEvent;
import at.woodexplosive.woodlib.api.block.event.CustomBlockPlaceEvent;
import at.woodexplosive.woodlib.block.CustomBlock;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Fluent builder for {@link ICustomBlock} definitions.
 *
 * <p>A CustomBlock has one or more {@link CustomBlockPart}s, each with a position relative to the
 * structure's placement origin. Use {@link #single(List)} for the common single-block case, or
 * {@link #part(int, int, int, Material, List)} repeatedly for multi-block structures.</p>
 */
public final class CustomBlockBuilder implements ICustomBlockBuilder<CustomBlockBuilder> {

    private final NamespacedKey id;
    private final List<CustomBlockPart> parts = new ArrayList<>();
    private boolean rotatable = false;

    private Consumer<CustomBlockInteractEvent> onInteract;
    private Consumer<CustomBlockPlaceEvent> onPlace;
    private Consumer<CustomBlockBreakEvent> onBreak;

    private CustomBlockBuilder(@NotNull NamespacedKey id) {
        this.id = id;
    }

    /**
     * Starts a new builder for a CustomBlock with the given unique registry id.
     * @param id the CustomBlock id (e.g. {@code "myplugin:custom_furnace"})
     * @return a new {@link CustomBlockBuilder}
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull CustomBlockBuilder of(@NotNull NamespacedKey id) {
        return new CustomBlockBuilder(id);
    }

    /**
     * Adds a part to this structure.
     * @param part the {@link CustomBlockPart} to add
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    @Override
    public @NotNull CustomBlockBuilder part(@NotNull CustomBlockPart part) {
        this.parts.add(part);
        return this;
    }

    /**
     * Adds a part at the given offset from the structure's origin, with an explicit collision material.
     * @param x offset x
     * @param y offset y
     * @param z offset z
     * @param barrierMaterial the collision material for this part
     * @param displays the {@link DisplayDefinition}s rendered at this part
     * @return this builder for chaining
     */
    @Contract(value = "_, _, _, _, _ -> this")
    public @NotNull CustomBlockBuilder part(int x, int y, int z, @NotNull Material barrierMaterial, @NotNull List<DisplayDefinition> displays) {
        return part(new CustomBlockPart(new BlockVector(x, y, z), barrierMaterial, displays));
    }

    /**
     * Adds a part at the given offset from the structure's origin, using {@link Material#BARRIER} for
     * collision.
     * @param x offset x
     * @param y offset y
     * @param z offset z
     * @param displays the {@link DisplayDefinition}s rendered at this part
     * @return this builder for chaining
     */
    @Contract(value = "_, _, _, _ -> this")
    public @NotNull CustomBlockBuilder part(int x, int y, int z, @NotNull List<DisplayDefinition> displays) {
        return part(x, y, z, Material.BARRIER, displays);
    }

    /**
     * Convenience for the common single-block case: adds one part at offset {@code (0,0,0)} with
     * {@link Material#BARRIER} for collision.
     * @param displays the {@link DisplayDefinition}s rendered at the structure's origin
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    public @NotNull CustomBlockBuilder single(@NotNull List<DisplayDefinition> displays) {
        return part(0, 0, 0, displays);
    }

    /**
     * Sets whether this CustomBlock is placed with a 4-way cardinal rotation snapped from the placing
     * player's yaw. Defaults to {@code false}.
     * @param rotatable {@code true} to enable placement rotation
     * @return this builder for chaining
     */
    @Contract(value = "_ -> this")
    @Override
    public @NotNull CustomBlockBuilder rotatable(boolean rotatable) {
        this.rotatable = rotatable;
        return this;
    }

    @Override
    public @NonNull CustomBlockBuilder setOnBlockInteract(@NotNull Consumer<CustomBlockInteractEvent> event) {
        this.onInteract = event;
        return this;
    }

    @Override
    public @NonNull CustomBlockBuilder setOnBlockBreakEvent(@NotNull Consumer<CustomBlockBreakEvent> event) {
        this.onBreak = event;
        return this;
    }

    @Override
    public @NonNull CustomBlockBuilder setOnBlockPlaceEvent(@NotNull Consumer<CustomBlockPlaceEvent> event) {
        this.onPlace = event;
        return this;
    }

    /**
     * Builds the configured {@link ICustomBlock}. Does not register it - use
     * {@link at.woodexplosive.woodlib.api.block.CustomBlockRegistry#register(ICustomBlock, org.bukkit.inventory.ItemStack)}.
     * @return the new {@link ICustomBlock}
     * @throws IllegalStateException if no parts were added
     */
    @Contract(value = "-> new")
    @Override
    public @NotNull ICustomBlock build() {
        if (parts.isEmpty()) throw new IllegalStateException("CustomBlock '" + id + "' has no parts");
        return new CustomBlock(id, parts, rotatable, onInteract, onPlace, onBreak);
    }
}
