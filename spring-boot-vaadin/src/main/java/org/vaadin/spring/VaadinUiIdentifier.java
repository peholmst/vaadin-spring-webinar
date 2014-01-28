package org.vaadin.spring;

import com.vaadin.ui.UI;

/**
 * Uniquely identifies a UI instance for a given window/tab.
 *
 * @author petter@vaadin.com
 * @author Josh Long (josh@joshlong.com)
 */
public class VaadinUiIdentifier {
    private final int uiId;

    public VaadinUiIdentifier(int uiId) {
        this.uiId = uiId;
    }

    public VaadinUiIdentifier(UI ui) {
        this.uiId = ui.getUIId();
    }

    public int getUiId() {
        return uiId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VaadinUiIdentifier that = (VaadinUiIdentifier) o;

        if (uiId != that.uiId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return uiId;
    }

    @Override
    public String toString() {
        return String.format("%s(%d)", VaadinUiIdentifier.class.getSimpleName(), uiId);
    }
}
