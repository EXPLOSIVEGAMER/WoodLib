package at.woodexplosive.woodlib.gui.element;

import at.woodexplosive.woodlib.api.gui.element.IGuiElement;
import org.jetbrains.annotations.Contract;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * A {@link Tab} that carries its own ordered list of page elements, for use in
 * {@link at.woodexplosive.woodlib.gui.gui.SimpleTabbedPagedGui}. That GUI pages this list across the
 * content slots; the slot-keyed content map inherited from {@link Tab} is unused there.
 */
public class PagedTab extends Tab {

    private final LinkedList<IGuiElement> pageElements = new LinkedList<>();

    /**
     * @param tabElement the button element used to select this tab
     */
    public PagedTab(IGuiElement tabElement) {
        super(tabElement);
    }

    /**
     * Creates a paged tab with the given button element.
     * @param tabElement the button element used to select this tab
     * @return a new {@link PagedTab}
     */
    @Contract(value = "_ -> new", pure = true)
    public static PagedTab of(IGuiElement tabElement) {
        return new PagedTab(tabElement);
    }

    /**
     * Returns this tab's ordered page elements (the backing list).
     * @return the page elements
     */
    @Contract(pure = true)
    public LinkedList<IGuiElement> getPageElements() {
        return this.pageElements;
    }

    /**
     * Appends a page element.
     * @param element the element to add
     * @return this tab for chaining
     */
    public PagedTab addPageElement(IGuiElement element) {
        this.pageElements.add(element);
        return this;
    }

    /**
     * Appends multiple page elements.
     * @param elements the elements to add
     * @return this tab for chaining
     */
    public PagedTab addPageElements(Collection<? extends IGuiElement> elements) {
        this.pageElements.addAll(elements);
        return this;
    }

    /**
     * Varargs variant of {@link #addPageElements(Collection)}.
     * @param elements the elements to add
     * @return this tab for chaining
     */
    public PagedTab addPageElements(IGuiElement... elements) {
        return this.addPageElements(List.of(elements));
    }

    /**
     * Removes a page element.
     * @param element the element to remove
     * @return this tab for chaining
     */
    public PagedTab removePageElement(IGuiElement element) {
        this.pageElements.remove(element);
        return this;
    }

    /**
     * Removes multiple page elements.
     * @param elements the elements to remove
     * @return this tab for chaining
     */
    public PagedTab removePageElements(Collection<? extends IGuiElement> elements) {
        this.pageElements.removeAll(elements);
        return this;
    }

    /**
     * Replaces all page elements with the given ones.
     * @param elements the new page elements
     * @return this tab for chaining
     */
    public PagedTab setPageElements(Collection<? extends IGuiElement> elements) {
        this.pageElements.clear();
        this.pageElements.addAll(elements);
        return this;
    }
}
