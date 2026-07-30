package at.woodexplosive.woodlib.api.block;

import org.bukkit.Color;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Display;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * The visual configuration for a single {@link org.bukkit.entity.BlockDisplay} spawned for a
 * {@link CustomBlockPart}.
 *
 * @param blockData the block rendered by the display
 * @param translation the transform's translation
 * @param leftRotation the transform's left rotation
 * @param scale the transform's scale
 * @param rightRotation the transform's right rotation
 * @param billboard how the display faces the viewer
 * @param brightness a fixed brightness override, or {@code null} to use the world's light level
 * @param glowing whether the display has a glowing outline
 * @param glowColorOverride the glow outline color, or {@code null} for the entity's team color
 */
public record DisplayDefinition(@NotNull BlockData blockData, @NotNull Vector3f translation,
                                 @NotNull Quaternionf leftRotation, @NotNull Vector3f scale,
                                 @NotNull Quaternionf rightRotation, @NotNull Display.Billboard billboard,
                                 @Nullable Display.Brightness brightness, boolean glowing,
                                 @Nullable Color glowColorOverride) {

    /**
     * @return a new {@link Transformation} built from this definition's translation/rotation/scale
     */
    @Contract(value = "-> new", pure = true)
    public @NotNull Transformation transformation() {
        return new Transformation(translation, leftRotation, scale, rightRotation);
    }

    /**
     * Starts a builder for a display rendering the given block, with an identity transform,
     * {@link Display.Billboard#FIXED} billboard, no brightness override and no glow.
     * @param blockData the block to render
     * @return a new {@link Builder}
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Builder builder(@NotNull BlockData blockData) {
        return new Builder(blockData);
    }

    /** Fluent builder for {@link DisplayDefinition}. */
    public static final class Builder {
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
         * Builds the configured {@link DisplayDefinition}.
         * @return the new {@link DisplayDefinition}
         */
        @Contract(value = "-> new", pure = true)
        public @NotNull DisplayDefinition build() {
            return new DisplayDefinition(blockData, translation, leftRotation, scale, rightRotation,
                    billboard, brightness, glowing, glowColorOverride);
        }
    }
}
