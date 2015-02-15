
package org.wargamer2010.signshop.util;

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
        String timeString = "";
        timeUnit[] timeUnits = { (new timeUnit(60, "Second", time)), (new timeUnit(60, "Minute")), (new timeUnit(24, "Hour")), (new timeUnit(365, "Day")) };
        for(int i = 0; (i+1) < timeUnits.length; i++)
            while(timeUnits[i].decrement())
                timeUnits[i+1].increment();
        int temp;
        Boolean first = true;
        for(int i = (timeUnits.length-1); i >= 0; i--) {
            temp = timeUnits[i].getAmount();
            if(temp > 0) {
                if(!first && i >= 0)
                    timeString += ", ";
                else
                    first = false;
                timeString += (temp + " " + timeUnits[i].getName());
                if(temp > 1)
                    timeString += "s";
            }
        }
        int pos = timeString.lastIndexOf(',');
        if (pos >= 0)
            timeString = timeString.substring(0,pos) + " and" + timeString.substring(pos+1);
        return timeString;
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
            if(currentAmount >= maxAmount) {
                currentAmount -= maxAmount;
                return true;
            }

            return false;
        }

        Boolean singleDecrement() {
            if(currentAmount > 0) {
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
