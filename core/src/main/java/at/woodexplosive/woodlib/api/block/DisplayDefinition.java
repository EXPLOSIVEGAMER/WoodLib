package at.woodexplosive.woodlib.api.block;

import org.bukkit.Color;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * The visual configuration for a single display spawned for a {@link CustomBlockPart}: either a
 * vanilla {@link BlockData} rendered via a {@link org.bukkit.entity.BlockDisplay} ({@link OfBlock}),
 * or a (typically custom-modeled) {@link ItemStack} rendered via an {@link ItemDisplay}
 * ({@link OfItem}).
 */
public sealed interface DisplayDefinition permits DisplayDefinition.OfBlock, DisplayDefinition.OfItem {

    /** @return the transform's translation */
    @NotNull Vector3f translation();

    /** @return the transform's left rotation */
    @NotNull Quaternionf leftRotation();

    /** @return the transform's scale */
    @NotNull Vector3f scale();

    /** @return the transform's right rotation */
    @NotNull Quaternionf rightRotation();

    /** @return how the display faces the viewer */
    @NotNull Display.Billboard billboard();

    /** @return a fixed brightness override, or {@code null} to use the world's light level */
    @Nullable Display.Brightness brightness();

    /** @return whether the display has a glowing outline */
    boolean glowing();

    /** @return the glow outline color, or {@code null} for the entity's team color */
    @Nullable Color glowColorOverride();

    /**
     * @return a new {@link Transformation} built from this definition's translation/rotation/scale
     */
    @Contract(value = "-> new", pure = true)
    default @NotNull Transformation transformation() {
        return new Transformation(translation(), leftRotation(), scale(), rightRotation());
    }

    /**
     * Starts a builder for a display rendering the given block via a {@link org.bukkit.entity.BlockDisplay},
     * with an identity transform, {@link Display.Billboard#FIXED} billboard, no brightness override and
     * no glow.
     * @param blockData the block to render
     * @return a new {@link Builder}
     */
    @Contract(value = "_ -> new", pure = true)
    static @NotNull Builder builder(@NotNull BlockData blockData) {
        return new Builder(blockData);
    }

    /**
     * Starts a builder for a display rendering the given item (e.g. one carrying a custom item model
     * via {@link at.woodexplosive.woodlib.api.item.AbstractItemBuilder#setItemModel}) via an
     * {@link ItemDisplay}, with an identity transform, {@link Display.Billboard#FIXED} billboard,
     * {@link ItemDisplay.ItemDisplayTransform#FIXED} item transform, no brightness override and no glow.
     * @param itemStack the item to render
     * @return a new {@link ItemBuilder}
     */
    @Contract(value = "_ -> new", pure = true)
    static @NotNull ItemBuilder builder(@NotNull ItemStack itemStack) {
        return new ItemBuilder(itemStack);
    }

    /**
     * A {@link DisplayDefinition} rendering a vanilla {@link BlockData} via a
     * {@link org.bukkit.entity.BlockDisplay}.
     * @param blockData the block rendered by the display
     */
    record OfBlock(@NotNull BlockData blockData, @NotNull Vector3f translation,
                    @NotNull Quaternionf leftRotation, @NotNull Vector3f scale,
                    @NotNull Quaternionf rightRotation, @NotNull Display.Billboard billboard,
                    @Nullable Display.Brightness brightness, boolean glowing,
                    @Nullable Color glowColorOverride) implements DisplayDefinition {
    }

    /**
     * A {@link DisplayDefinition} rendering an {@link ItemStack} via an {@link ItemDisplay} - the way
     * to show a custom (resource-pack-modeled) 3D model instead of a vanilla block model.
     * @param itemStack the item rendered by the display
     * @param itemTransform the {@link ItemDisplay.ItemDisplayTransform} display context
     */
    record OfItem(@NotNull ItemStack itemStack, @NotNull ItemDisplay.ItemDisplayTransform itemTransform,
                   @NotNull Vector3f translation, @NotNull Quaternionf leftRotation, @NotNull Vector3f scale,
                   @NotNull Quaternionf rightRotation, @NotNull Display.Billboard billboard,
                   @Nullable Display.Brightness brightness, boolean glowing,
                   @Nullable Color glowColorOverride) implements DisplayDefinition {
    }

    /** Fluent builder for a {@link OfBlock} {@link DisplayDefinition}. */
    final class Builder {
        private final BlockData blockData;
        private Vector3f translation = new Vector3f(0, 0, 0);
        private Quaternionf leftRotation = new Quaternionf();
        private Vector3f scale = new Vector3f(1, 1, 1);
        private Quaternionf rightRotation = new Quaternionf();
        private Display.Billboard billboard = Display.Billboard.FIXED;
        private Display.Brightness brightness;
        private boolean glowing;
        private Color glowColorOverride;

        private Builder(@NotNull BlockData blockData) {
            this.blockData = blockData;
        }

        /**
         * Sets the transform's translation.
         * @param x x offset
         * @param y y offset
         * @param z z offset
         * @return this builder for chaining
         */
        @Contract(value = "_, _, _ -> this")
        public Builder translation(float x, float y, float z) {
            this.translation = new Vector3f(x, y, z);
            return this;
        }

        /**
         * Sets the transform's left rotation.
         * @param leftRotation the left rotation
         * @return this builder for chaining
         */
        @Contract(value = "_ -> this")
        public Builder leftRotation(@NotNull Quaternionf leftRotation) {
            this.leftRotation = leftRotation;
            return this;
        }

        /**
         * Sets the transform's scale.
         * @param x x scale
         * @param y y scale
         * @param z z scale
         * @return this builder for chaining
         */
        @Contract(value = "_, _, _ -> this")
        public Builder scale(float x, float y, float z) {
            this.scale = new Vector3f(x, y, z);
            return this;
        }

        /**
         * Sets the transform's scale.
         * @param scale the scale
         * @return this builder for chaining
         */
        @Contract(value = "_ -> this")
        public Builder scale(@NotNull Vector3f scale) {
            this.scale = scale;
            return this;
        }

        /**
         * Sets the transform's right rotation.
         * @param rightRotation the right rotation
         * @return this builder for chaining
         */
        @Contract(value = "_ -> this")
        public Builder rightRotation(@NotNull Quaternionf rightRotation) {
            this.rightRotation = rightRotation;
            return this;
        }

        /**
         * Sets the billboard mode.
         * @param billboard the {@link Display.Billboard}
         * @return this builder for chaining
         */
        @Contract(value = "_ -> this")
        public Builder billboard(@NotNull Display.Billboard billboard) {
            this.billboard = billboard;
            return this;
        }

        /**
         * Sets a fixed brightness override.
         * @param brightness the {@link Display.Brightness}, or {@code null} to use world lighting
         * @return this builder for chaining
         */
        @Contract(value = "_ -> this")
        public Builder brightness(@Nullable Display.Brightness brightness) {
            this.brightness = brightness;
            return this;
        }

        /**
         * Enables a glowing outline, optionally with an override color.
         * @param glowColorOverride the outline color, or {@code null} for the entity's team color
         * @return this builder for chaining
         */
        @Contract(value = "_ -> this")
        public Builder glowing(@Nullable Color glowColorOverride) {
            this.glowing = true;
            this.glowColorOverride = glowColorOverride;
            return this;
        }

        /**
         * Builds the configured {@link OfBlock} display.
         * @return the new {@link DisplayDefinition}
         */
        @Contract(value = "-> new", pure = true)
        public @NotNull DisplayDefinition build() {
            return new OfBlock(blockData, translation, leftRotation, scale, rightRotation,
                    billboard, brightness, glowing, glowColorOverride);
        }
    }

    /** Fluent builder for a {@link OfItem} {@link DisplayDefinition}. */
    final class ItemBuilder {
        private final ItemStack itemStack;
        private ItemDisplay.ItemDisplayTransform itemTransform = ItemDisplay.ItemDisplayTransform.FIXED;
        private Vector3f translation = new Vector3f(0, 0, 0);
        private Quaternionf leftRotation = new Quaternionf();
        private Vector3f scale = new Vector3f(1, 1, 1);
        private Quaternionf rightRotation = new Quaternionf();
        private Display.Billboard billboard = Display.Billboard.FIXED;
        private Display.Brightness brightness;
        private boolean glowing;
        private Color glowColorOverride;

        private ItemBuilder(@NotNull ItemStack itemStack) {
            this.itemStack = itemStack;
        }

        /**
         * Sets the {@link ItemDisplay.ItemDisplayTransform} display context (e.g. {@code FIXED} for a
         * static world object, {@code GROUND} to render like a dropped item).
         * @param itemTransform the item display transform
         * @return this builder for chaining
         */
        @Contract(value = "_ -> this")
        public ItemBuilder itemTransform(@NotNull ItemDisplay.ItemDisplayTransform itemTransform) {
            this.itemTransform = itemTransform;
            return this;
        }

        /**
         * Sets the transform's translation.
         * @param x x offset
         * @param y y offset
         * @param z z offset
         * @return this builder for chaining
         */
        @Contract(value = "_, _, _ -> this")
        public ItemBuilder translation(float x, float y, float z) {
            this.translation = new Vector3f(x, y, z);
            return this;
        }

        /**
         * Sets the transform's left rotation.
         * @param leftRotation the left rotation
         * @return this builder for chaining
         */
        @Contract(value = "_ -> this")
        public ItemBuilder leftRotation(@NotNull Quaternionf leftRotation) {
            this.leftRotation = leftRotation;
            return this;
        }

        /**
         * Sets the transform's scale.
         * @param x x scale
         * @param y y scale
         * @param z z scale
         * @return this builder for chaining
         */
        @Contract(value = "_, _, _ -> this")
        public ItemBuilder scale(float x, float y, float z) {
            this.scale = new Vector3f(x, y, z);
            return this;
        }

        /**
         * Sets the transform's scale.
         * @param scale the scale
         * @return this builder for chaining
         */
        @Contract(value = "_ -> this")
        public ItemBuilder scale(@NotNull Vector3f scale) {
            this.scale = scale;
            return this;
        }

        /**
         * Sets the transform's right rotation.
         * @param rightRotation the right rotation
         * @return this builder for chaining
         */
        @Contract(value = "_ -> this")
        public ItemBuilder rightRotation(@NotNull Quaternionf rightRotation) {
            this.rightRotation = rightRotation;
            return this;
        }

        /**
         * Sets the billboard mode.
         * @param billboard the {@link Display.Billboard}
         * @return this builder for chaining
         */
        @Contract(value = "_ -> this")
        public ItemBuilder billboard(@NotNull Display.Billboard billboard) {
            this.billboard = billboard;
            return this;
        }

        /**
         * Sets a fixed brightness override.
         * @param brightness the {@link Display.Brightness}, or {@code null} to use world lighting
         * @return this builder for chaining
         */
        @Contract(value = "_ -> this")
        public ItemBuilder brightness(@Nullable Display.Brightness brightness) {
            this.brightness = brightness;
            return this;
        }

        /**
         * Enables a glowing outline, optionally with an override color.
         * @param glowColorOverride the outline color, or {@code null} for the entity's team color
         * @return this builder for chaining
         */
        @Contract(value = "_ -> this")
        public ItemBuilder glowing(@Nullable Color glowColorOverride) {
            this.glowing = true;
            this.glowColorOverride = glowColorOverride;
            return this;
        }

        /**
         * Builds the configured {@link OfItem} display.
         * @return the new {@link DisplayDefinition}
         */
        @Contract(value = "-> new", pure = true)
        public @NotNull DisplayDefinition build() {
            return new OfItem(itemStack, itemTransform, translation, leftRotation, scale, rightRotation,
                    billboard, brightness, glowing, glowColorOverride);
        }
    }
}
