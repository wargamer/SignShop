package org.wargamer2010.signshop.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility methods for time formatting, duration display, and timestamp generation.
 *
 * <p>Converts seconds into human-readable duration strings (e.g., "2 Hours, 30 Minutes")
 * and provides date/time formatting for transaction logs and shop creation timestamps.</p>
 *
 * <h2>Key Methods:</h2>
 * <ul>
 *   <li>{@link #parseTime(int)} - Converts seconds to "X Days, Y Hours, Z Minutes" format</li>
 *   <li>{@link #getDateTimeStamp()} - Compact timestamp for file naming (MMddyyHHmm)</li>
 *   <li>{@link #getDateTimeFromLong(Long)} - Formatted date for display (MM/dd/yy-HH:mm)</li>
 * </ul>
 *
 * @see org.wargamer2010.signshop.timing.TimeManager
 */
public class SSTimeUtil {
    private SSTimeUtil() {

    }

    /**
     * Returns a string representation of the given amount of seconds
     *
     * @param time Time in seconds
     * @return String representation of the time param
     */
    public static String parseTime(int time) {
        String timeString;
        timeUnit[] timeUnits = {(new timeUnit(60, "Second", time)), (new timeUnit(60, "Minute")), (new timeUnit(24, "Hour")), (new timeUnit(365, "Day"))};
        for (int i = 0; (i + 1) < timeUnits.length; i++)
            while (timeUnits[i].decrement())
                timeUnits[i + 1].increment();
        int temp;
        boolean first = true;
        StringBuilder timeStringBuilder = new StringBuilder();
        for (int i = (timeUnits.length - 1); i >= 0; i--) {
            temp = timeUnits[i].getAmount();
            if (temp > 0) {
                if (!first && i >= 0)
                    timeStringBuilder.append(", ");
                else
                    first = false;
                timeStringBuilder.append(temp).append(" ").append(timeUnits[i].getName());
                if (temp > 1)
                    timeStringBuilder.append("s");
            }
        }
        timeString = timeStringBuilder.toString();
        int pos = timeString.lastIndexOf(',');
        if (pos >= 0)
            timeString = timeString.substring(0, pos) + " and" + timeString.substring(pos + 1);
        return timeString;
    }

    public static String getDateTimeStamp() {
        return new SimpleDateFormat("MMddyyHHmm").format(new Date(System.currentTimeMillis()));
    }

    public static String getDateTimeFromLong(Long timeSinceEpoch){
        if (timeSinceEpoch <= 0) return "Never";
        return new SimpleDateFormat("MM'/'dd'/'yy'-'HH':'mm").format(new Date(timeSinceEpoch));

    }

    private static class timeUnit {
        int maxAmount;
        int currentAmount = 0;
        String name;

        timeUnit(int pMaxAmount, String pName) {
            maxAmount = pMaxAmount;
            name = pName;
        }

        timeUnit(int pMaxAmount, String pName, int pCurrentAmount) {
            maxAmount = pMaxAmount;
            name = pName;
            currentAmount = pCurrentAmount;
        }

        Boolean decrement() {
            if (currentAmount >= maxAmount) {
                currentAmount -= maxAmount;
                return true;
            }

            return false;
        }

        Boolean singleDecrement() {
            if (currentAmount > 0) {
                currentAmount--;
                return true;
            }

            return false;
        }

        void increment() {
            currentAmount++;
        }

        void fullIncrement() {
            currentAmount += maxAmount;
        }

        String getName() {
            return name;
        }

        int getAmount() {
            return currentAmount;
        }

        void setAmount(int amount) {
            currentAmount = amount;
        }
    }
}
