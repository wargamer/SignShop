
package org.wargamer2010.signshop.timing;

import org.wargamer2010.signshop.util.ItemMessagePart;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Expirable command that executes after a configured delay.
 * Stores command type and message placeholders for delayed execution.
 */
public class TimedCommand implements IExpirable {
    private String ShopType;
    private String CommandType;
    private Map<String, String> MessageParts;

    public TimedCommand() {

    }

    public TimedCommand(String shopType, String commandType, Map<String, Object> messageParts) {
        ShopType = shopType;
        CommandType = commandType;
        MessageParts = convertToStringMap(messageParts);
    }

    /**
     * Converts a Map<String, Object> to Map<String, String> by calling getString()
     * on ItemMessagePart values and toString() on other objects.
     */
    private static Map<String, String> convertToStringMap(Map<String, Object> objectMap) {
        Map<String, String> stringMap = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
            Object value = entry.getValue();
            String stringValue;
            if (value instanceof ItemMessagePart) {
                stringValue = ((ItemMessagePart) value).getString();
            } else if (value != null) {
                stringValue = value.toString();
            } else {
                stringValue = "";
            }
            stringMap.put(entry.getKey(), stringValue);
        }
        return stringMap;
    }

    @Override
    public String getName() {
        return TimedCommand.class.getCanonicalName();
    }

    @Override
    public boolean parseEntry(Map<String, String> entry) {
        if(entry.containsKey("shoptype") && entry.containsKey("commandtype") && entry.containsKey("messageparts")) {
            ShopType = entry.get("shoptype");
            CommandType = entry.get("commandtype");
            MessageParts = new LinkedHashMap<>();
            if(!entry.get("messageparts").isEmpty()) {
                for(String ent : entry.get("messageparts").split("~`~")) {
                    if(ent.contains("~")) {
                        String[] arr= ent.split("~");
                        if(arr.length >= 2)
                            MessageParts.put(arr[0], arr[1]);
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public Map<String, String> getEntry() {
        Map<String, String> entry = new LinkedHashMap<>();
        entry.put("shoptype", ShopType);
        entry.put("commandtype", CommandType);
        StringBuilder builder = new StringBuilder(200);
        for(Map.Entry<String, String> bit : MessageParts.entrySet()) {
            builder.append(bit.getKey());
            builder.append("~");
            builder.append(bit.getValue());
            builder.append("~`~");
        }
        entry.put("messageparts", builder.toString());
        return entry;
    }

    public String getShopType() {
        return ShopType;
    }

    public String getCommandType() {
        return CommandType;
    }

    public Map<String, String> getMessageParts() {
        return Collections.unmodifiableMap(MessageParts);
    }

    /**
     * Returns message parts as Map<String, Object> for compatibility with
     * fillInBlanks() method. Since TimedCommand already converted all values
     * to strings, this is a safe upcast.
     */
    public Map<String, Object> getMessagePartsAsObject() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(MessageParts));
    }
}
