
package org.wargamer2010.signshop.events;

import java.util.Map;

public interface IMessagePartContainer {
    void setMessagePart(String name, String value);

    Map<String, String> getMessageParts();
}
