package org.wargamer2010.signshop.player;

import net.md_5.bungee.api.chat.BaseComponent;
import org.jetbrains.annotations.NotNull;
import org.wargamer2010.signshop.SignShop;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class MessageWorker implements Runnable {
    private static boolean bWorking = false;
    private static MessageWorker instance = null;
    private static final DelayQueue<Message> messageQueue = new DelayQueue<>();
    private static final Map<String, HashMap<String, Message>> mPlayerMessageMap = new HashMap<>();

    private MessageWorker() {

    }

    public static void init() {
        synchronized (MessageWorker.class) {
            if (bWorking)
                return;
            if (instance == null)
                instance = new MessageWorker();
            SignShop.getScheduler().runAsync(instance);
            bWorking = true;
        }
    }

    public static boolean isWorking() {
        return bWorking;
    }

    public static void OfferMessage(String message, SignShopPlayer player) {
        OfferMessage(message, null, player);
    }

    /**
     * Offers a message with an optional rich component (for hover events).
     *
     * @param messageKey The plain text message used as deduplication key
     * @param component The rich component to display (can be null for plain text)
     * @param player The player to send the message to
     */
    public static void OfferMessage(String messageKey, BaseComponent component, SignShopPlayer player) {
        String playername = player.getName();
        long timenow = System.currentTimeMillis();
        HashMap<String, Message> mMessageMap = mPlayerMessageMap.get(playername);
        int cooldown = (SignShop.getInstance().getSignShopConfig().getMessageCooldown() * 1000); // Convert to millis

        if (mMessageMap == null) {
            mMessageMap = new HashMap<>();
            mPlayerMessageMap.put(playername, mMessageMap);
        }
        else if (mMessageMap.containsKey(messageKey) && (timenow - mMessageMap.get(messageKey).getLastSeen()) <= cooldown) {
            // Check if message is the same and if less time than the cooldown has passed
            // If so, increment count and push the message to the queue
            mMessageMap.get(messageKey).incCount();
            synchronized (MessageWorker.class) {
                if (!messageQueue.contains(mMessageMap.get(messageKey)))
                    messageQueue.offer(mMessageMap.get(messageKey));
            }
            return;
        }

        if (!mMessageMap.containsKey(messageKey)) {
            mMessageMap.put(messageKey, new Message(messageKey, component, player.GetIdentifier(), timenow));
        }
        else if (mMessageMap.get(messageKey).getCount() > 0) {
            // We've been repeating the message and this is the last in the row, so we should count it
            // That way it's easier to read the message and just multiply the repeated_x_times with the numbers in the message
            mMessageMap.get(messageKey).incCount();
        }

        SendRepeatedMessage(mMessageMap.get(messageKey));
    }

    private static void SendRepeatedMessage(Message message) {
        Map<String, Object> pars = new LinkedHashMap<>();
        pars.put("!times", Integer.toString(message.getCount()));
        String appender = (message.getCount() > 0 ? (" " + SignShop.getInstance().getSignShopConfig().getError("repeated_x_times", pars)) : "");

        // Send component if available, otherwise send string
        if (message.getComponent() != null) {
            if (message.getCount() > 0) {
                // Append repeat count to component
                net.md_5.bungee.api.chat.TextComponent fullMessage = new net.md_5.bungee.api.chat.TextComponent(message.getComponent());
                fullMessage.addExtra(appender);
                message.getPlayer().sendNonDelayedMessage(fullMessage);
            } else {
                message.getPlayer().sendNonDelayedMessage(message.getComponent());
            }
        } else {
            message.getPlayer().sendNonDelayedMessage(message.getMessage() + appender);
        }

        message.clrCount();
        message.setLastSeen(System.currentTimeMillis());
    }

    @Override
    public void run() {
        try {
            Message taken;
            while ((taken = messageQueue.take()) != null) {
                if (taken.getCount() > 0)
                    SendRepeatedMessage(taken);
                synchronized (MessageWorker.class) {
                    messageQueue.remove(taken);
                }
            }
        } catch (InterruptedException ex) {
            SignShop.log("MessageWorker was interrupted because: " + ex.getMessage(), Level.SEVERE);
        }

        bWorking = false;
        init();
    }

    private static class Message implements Delayed {
        private final String sMessage;
        private final BaseComponent component; // Rich component with hover events (can be null)
        private final PlayerIdentifier playerIdentifier;
        private int iCount = 0;
        private long lLastSeen;
        private final int delay = (SignShop.getInstance().getSignShopConfig().getMessageCooldown() * 1000 + 1000); // Convert to millis and give it a second

        private Message(String pMessage, PlayerIdentifier playerIdentifier, long pTime) {
            this(pMessage, null, playerIdentifier, pTime);
        }

        private Message(String pMessage, BaseComponent pComponent, PlayerIdentifier playerIdentifier, long pTime) {
            sMessage = pMessage;
            component = pComponent;
            this.playerIdentifier = playerIdentifier;
            lLastSeen = pTime;
        }

        @Override
        public long getDelay(@NotNull TimeUnit unit) {
            return unit.convert(delay - (System.currentTimeMillis() - lLastSeen),
                    TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(@NotNull Delayed delayed) {
            if (delayed == this) {
                return 0;
            }

            long d = (getDelay(TimeUnit.MILLISECONDS) - delayed.getDelay(TimeUnit.MILLISECONDS));
            return ((d == 0) ? 0 : ((d < 0) ? -1 : 1));
        }

        public String getMessage() {
            return sMessage;
        }

        public BaseComponent getComponent() {
            return component;
        }

        public SignShopPlayer getPlayer() {
           return PlayerCache.getPlayer(playerIdentifier);
        }

        public synchronized int getCount() {
            return iCount;
        }

        public synchronized void incCount() {
            this.iCount++;
        }

        public synchronized void clrCount() {
            this.iCount = 0;
        }

        public synchronized long getLastSeen() {
            return lLastSeen;
        }

        public synchronized void setLastSeen(long lLastSeen) {
            this.lLastSeen = lLastSeen;
        }
    }
}
