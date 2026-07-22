package at.woodexplosive.woodlib.gui.element;

import at.woodexplosive.woodlib.api.gui.element.IGuiElement;
import at.woodexplosive.woodlib.api.gui.element.ITab;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default {@link ITab} implementation: a tab button element plus a {@code slot → element} content
 * map. The owning {@link at.woodexplosive.woodlib.gui.gui.SimpleTabbedGui} assigns its content slots
 * when the tab is added.
 */
public class Tab implements ITab {

    private IGuiElement tabElement;
    private List<Integer> contentSlots = new ArrayList<>();
    private final Map<Integer, IGuiElement> contentElements = new HashMap<>();

    /**
     * @param tabElement the button element used to select this tab
     */
    public Tab(IGuiElement tabElement) {
        this.tabElement = tabElement;
    }

    /**
     * @param tabElement the button element used to select this tab
     * @param contentSlots the slots this tab may fill with content
     */
    public Tab(IGuiElement tabElement, List<Integer> contentSlots) {
        this.tabElement = tabElement;
        this.contentSlots = new ArrayList<>(contentSlots);
    }

    /**
     * Creates a tab with the given button element.
     * @param tabElement the button element used to select this tab
     * @return a new {@link Tab}
     */
    @Contract(value = "_ -> new", pure = true)
    public static Tab of(IGuiElement tabElement) {
        return new Tab(tabElement);
    }

    @Override
    public IGuiElement getTabElement() {
        return this.tabElement;
    }

    @Override
    public Tab setTabElement(@NonNull IGuiElement element) {
        this.tabElement = element;
        return this;
    }

    @Override
    public List<Integer> getContentSlots() {
        return this.contentSlots;
    }

    @Override
    public Tab setContentSlots(@NonNull List<Integer> slots) {
        this.contentSlots = slots;
        return this;
    }

    @Override
    public Tab addContentSlot(int slot) {
        this.contentSlots.add(slot);
        return this;
    }

    @Override
    public Tab addContentSlots(@NonNull Collection<Integer> slots) {
        this.contentSlots.addAll(slots);
        return this;
    }

    @Override
    public Map<Integer, IGuiElement> getContentElements() {
        return this.contentElements;
    }

    @Override
    public Tab setContentElement(int slot, @NonNull IGuiElement element) {
        this.contentElements.put(slot, element);
        return this;
    }

    @Override
    public Tab setContentElements(@NonNull Map<Integer, IGuiElement> elements) {
        this.contentElements.clear();
        this.contentElements.putAll(elements);
        return this;
    }

    @Override
    public Tab removeContentElement(@NonNull IGuiElement element) {
        this.contentElements.values().removeIf(e -> e == element);
        return this;
    }

    @Override
    public Tab removeContentElements(@NonNull Collection<? extends IGuiElement> elements) {
        for (IGuiElement element : elements) this.removeContentElement(element);
        return this;
    }
}
