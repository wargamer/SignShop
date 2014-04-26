
package org.wargamer2010.signshop.player;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.SignShopConfig;

public class MessageWorker implements Runnable {
    private static boolean bWorking = false;
    private static MessageWorker instance = null;
    private static DelayQueue<Message> messageQueue = new DelayQueue<Message>();
    private static Map<String, HashMap<String, Message>> mPlayerMessageMap = new HashMap<String, HashMap<String, Message>>();

    private MessageWorker() {

    }

    public static void init() {
        synchronized(MessageWorker.class) {
            if(bWorking)
                return;
            if(instance == null)
                instance = new MessageWorker();
            Bukkit.getScheduler().runTaskAsynchronously(SignShop.getInstance(), instance);
            bWorking = true;
        }
    }

    public static boolean isWorking() {
        return bWorking;
    }

    public static void OfferMessage(String message, SignShopPlayer player) {
        String playername = player.getName();
        long timenow = System.currentTimeMillis();
        HashMap<String, Message> mMessageMap = mPlayerMessageMap.get(playername);
        int cooldown = (SignShopConfig.getMessageCooldown() * 1000); // Convert to millis

        if(mMessageMap == null) {
            mMessageMap = new HashMap<String, Message>();
            mPlayerMessageMap.put(playername, mMessageMap);
        } else if(mMessageMap.containsKey(message) && (timenow - mMessageMap.get(message).getLastSeen()) <= cooldown) {
            // Check if message is the same and if less time than the cooldown has passed
            // If so, increment count and push the message to the queue
            mMessageMap.get(message).incCount();
            synchronized(MessageWorker.class) {
                if(!messageQueue.contains(mMessageMap.get(message)))
                    messageQueue.offer(mMessageMap.get(message));
            }
            return;
        }

        if(!mMessageMap.containsKey(message)) {
            mMessageMap.put(message, new Message(message, player.GetIdentifier(), timenow));
        } else if(mMessageMap.get(message).getCount() > 0) {
            // We've been repeating the message and this is the last in the row so we should count it
            // That way it's easier to read the message and just multiply the repeated_x_times with the numbers in the message
            mMessageMap.get(message).incCount();
        }

        SendRepeatedMessage(mMessageMap.get(message));
    }

    private static void SendRepeatedMessage(Message message) {
        Map<String, String> pars = new LinkedHashMap<String, String>();
        pars.put("!times", Integer.toString(message.getCount()));
        String appender = (message.getCount() > 0 ? (" " + SignShopConfig.getError("repeated_x_times", pars)) : "");
        message.getPlayer().sendNonDelayedMessage(message.getMessage() + appender);
        message.clrCount();
        message.setLastSeen(System.currentTimeMillis());
    }

    @Override
    public void run() {
        try {
            Message taken;
            while((taken = messageQueue.take()) != null) {
                if(taken.getCount() > 0)
                    SendRepeatedMessage(taken);
                synchronized(MessageWorker.class) {
                    messageQueue.remove(taken);
                }
            }
        } catch(InterruptedException ex) {
            SignShop.log("MessageWorker was interrupted because: " + ex.getMessage(), Level.SEVERE);
        }

        bWorking = false;
        init();
    }

    private static class Message implements Delayed {
        private String sMessage;
        private PlayerIdentifier Player;
        private int iCount = 0;
        private long lLastSeen;
        private int delay = (SignShopConfig.getMessageCooldown() * 1000 + 1000); // Convert to millis and give it a second

        private Message(String pMessage, PlayerIdentifier player, long pTime) {
            sMessage = pMessage;
            Player = player;
            lLastSeen = pTime;
        }

        @Override
        public long getDelay( TimeUnit unit ) {
            return unit.convert( delay - ( System.currentTimeMillis() - lLastSeen ),
                    TimeUnit.MILLISECONDS );
        }

        @Override
        public int compareTo( Delayed delayed ) {
            if (delayed == this) {
                return 0;
            }

            long d = (getDelay(TimeUnit.MILLISECONDS) - delayed.getDelay(TimeUnit.MILLISECONDS));
            return ( ( d == 0 ) ? 0 : ( ( d < 0 ) ? -1 : 1 ) );
        }

        public String getMessage() {
            return sMessage;
        }

        public SignShopPlayer getPlayer() {
            return new SignShopPlayer(Player);
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
