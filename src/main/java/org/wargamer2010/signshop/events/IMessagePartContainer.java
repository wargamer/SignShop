
package org.wargamer2010.signshop.events;

import java.util.Map;

public interface IMessagePartContainer {
    /**
     * Sets a message part value. Value can be a String for simple placeholders
     * or an ItemMessagePart for rich content with hover tooltips.
     *
     * @param name The placeholder name (e.g., "!items", "!price")
     * @param value The value (String or ItemMessagePart)
     */
    void setMessagePart(String name, Object value);

    /**
     * Gets all message parts for placeholder replacement.
     *
     * @return Unmodifiable map of message parts
     */
    Map<String, Object> getMessageParts();
}
