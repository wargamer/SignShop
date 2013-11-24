
package org.wargamer2010.signshop.timing;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class TimedCommand implements IExpirable {
    private String ShopType;
    private String CommandType;
    private Map<String, String> MessageParts;

    public TimedCommand() {

    }

    public TimedCommand(String shopType, String commandType, Map<String, String> messageParts) {
        ShopType = shopType;
        CommandType = commandType;
        MessageParts = messageParts;
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
            MessageParts = new LinkedHashMap<String, String>();
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
        Map<String, String> entry = new LinkedHashMap<String, String>();
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
}
