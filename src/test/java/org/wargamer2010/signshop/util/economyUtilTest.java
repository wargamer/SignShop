package org.wargamer2010.signshop.util;

import junit.framework.TestCase;
import org.wargamer2010.signshop.configuration.SignShopConfig;

public class economyUtilTest extends TestCase {
    /**
     * Test the international price parser.
     */
    public void testparsePrice() {
        SignShopConfig.CommaDecimalSeparatorState prev = SignShopConfig.allowCommaDecimalSeparator();
        SignShopConfig.setAllowCommaDecimalSeparator(SignShopConfig.CommaDecimalSeparatorState.TRUE, false);
        assertEquals(0.0D, economyUtil.parsePrice(null));
        assertEquals(0.0D, economyUtil.parsePrice("null"));
        assertEquals(0.0D, economyUtil.parsePrice("NaN"));
        assertEquals(5.0D, economyUtil.parsePrice("-5"));
        assertEquals(1234.0D, economyUtil.parsePrice("1234"));
        assertEquals(1234.0D, economyUtil.parsePrice("1234.00"));
        assertEquals(1234.0D, economyUtil.parsePrice("1234,00"));
        assertEquals(1234.0D, economyUtil.parsePrice("1,234.00"));
        assertEquals(1234.0D, economyUtil.parsePrice("1.234,00"));
        assertEquals(1234.0D, economyUtil.parsePrice("1 234.00"));
        assertEquals(1234.0D, economyUtil.parsePrice("1 234,00"));
        assertEquals(1234.0D, economyUtil.parsePrice("1, 234.00"));
        assertEquals(1234.0D, economyUtil.parsePrice("1. 234,00"));
        assertEquals(123400.0D, economyUtil.parsePrice("1, 234,00"));
        assertEquals(123400.0D, economyUtil.parsePrice("1. 234.00"));
        assertEquals(123400.0D, economyUtil.parsePrice("1. 234. 00"));
        assertEquals(123400.0D, economyUtil.parsePrice("1, 234, 00"));
        assertEquals(1234.0D, economyUtil.parsePrice("wa 1234"));
        assertEquals(1234.0D, economyUtil.parsePrice("wa 1234 wa"));
        assertEquals(1234.0D, economyUtil.parsePrice("wa 1234.00"));
        assertEquals(1234.0D, economyUtil.parsePrice("wa 1234,00"));
        assertEquals(1234.0D, economyUtil.parsePrice("wa 1234.00 wa"));
        assertEquals(1234.0D, economyUtil.parsePrice("wa 1234,00 wa"));
        assertEquals(1234.0D, economyUtil.parsePrice("wa 1,234 wa"));
        assertEquals(1234.0D, economyUtil.parsePrice("wa 1.234 wa"));
        assertEquals(1234.0D, economyUtil.parsePrice("1,,,,,234"));
        assertEquals(1234.0D, economyUtil.parsePrice("1.....234"));
        assertEquals(1234.0D, economyUtil.parsePrice("1,2,3,4,,,"));
        assertEquals(1234.0D, economyUtil.parsePrice("1.2.3.4..."));
        assertEquals(1234.0D, economyUtil.parsePrice(",,,1,2,3,4"));
        assertEquals(1234.0D, economyUtil.parsePrice("...1.2.3.4"));
        assertEquals(0.0D, economyUtil.parsePrice("..,.,.,.1234,.,.,.,"));
        assertEquals(0.0D, economyUtil.parsePrice("1,.2,.3,.4,."));
        assertEquals(0.0D, economyUtil.parsePrice("1.,2.,3.,4.,"));
        assertEquals(12341234.0D, economyUtil.parsePrice("wa 1234 wa 1234"));
        assertEquals(12341234.0D, economyUtil.parsePrice("1234 wa 1234 wa"));
        assertEquals(121212.0D, economyUtil.parsePrice("12 wa 12 wa 12 wa"));
        assertEquals(121212.0D, economyUtil.parsePrice("wa 12 wa 12 wa 12"));
        assertEquals(12341234.0D, economyUtil.parsePrice("wa, 12,3,4 w,a 1,2,34,"));
        assertEquals(12341234.0D, economyUtil.parsePrice("1234. wa. 1.2.3.4 .wa."));
        assertEquals(1212.12D, economyUtil.parsePrice("12. wa 12. wa, 12 wa"));
        assertEquals(1212.12D, economyUtil.parsePrice("wa, 12, wa 1,2 wa. 12"));
        assertEquals(0.0D, economyUtil.parsePrice("12. wa 1,2 wa 12 wa"));
        assertEquals(0.0D, economyUtil.parsePrice("wa, 12, wa 1.2 wa 12"));
        assertEquals(1234.0D, economyUtil.parsePrice("!@#$%^&*()1234!@#$%^&*()"));
        assertEquals(0.0D, economyUtil.parsePrice(""));
        assertEquals(0.0D, economyUtil.parsePrice("i am nothing"));
        assertEquals(40711031.0D, economyUtil.parsePrice("i 4m n07h1ng w1th s0m3th1ng"));
        assertEquals(43.0D, economyUtil.parsePrice("giggity goo ga 43"));
        assertEquals(12341234.0D, economyUtil.parsePrice("1234.1234"));
        assertEquals(12341234.0D, economyUtil.parsePrice("1234,1234"));
        assertEquals(12341234.0D, economyUtil.parsePrice("1234+1234"));
        SignShopConfig.setAllowCommaDecimalSeparator(prev, false);
    }
}