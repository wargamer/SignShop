
package org.wargamer2010.signshop.util;

import org.wargamer2010.signshop.events.IMessagePartContainer;

public class CooldownUtil {
    private CooldownUtil() {

    }

    /**
     * Sets the !cooldownleft in seconds on the given container including other units
     * @param container Container to set the message on
     * @param left Seconds left on cooldown
     */
    public static void setCooldownMessage(IMessagePartContainer container, long left) {
        formatCooldownFromSeconds(container, left, 1, "");
        formatCooldownFromSeconds(container, left, 60, "minutes");
        formatCooldownFromSeconds(container, left, 3600, "hours");
        formatCooldownFromSeconds(container, left, 86400, "days");
        formatCooldownFromSeconds(container, left, 31536000, "years");
    }

    private static void formatCooldownFromSeconds(IMessagePartContainer container, long left, int amountOfSecondsInUnit, String unitName) {
        long amountInDisplayUnit = (long)Math.floor(((double)left / (double)amountOfSecondsInUnit));
        String name = "!cooldown" + unitName + "left";
        if(amountInDisplayUnit >= 1)
            container.setMessagePart(name, Long.toString(amountInDisplayUnit));
        else
            container.setMessagePart(name, "< 1");
    }
}
