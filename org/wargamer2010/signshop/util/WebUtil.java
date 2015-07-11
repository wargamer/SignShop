
package org.wargamer2010.signshop.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.inventory.ItemStack;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.SignShopConfig;

public class WebUtil {
    private static final String namesURL = "http://minecraft-ids.grahamedgecombe.com/";
    private static final String baseLanguage = "en";
    private static final Map<String, String> namesFromTheWeb = new HashMap<String, String>();

    private WebUtil() {

    }

    public static void init() {
        loadNamesFromWeb();
    }

    public static String getNameFromWeb(ItemStack stack) {
        if(!SignShopConfig.getEnableNamesFromTheWeb())
            return "";
        String data = Byte.toString(stack.getData().getData());
        String idToLookup = Integer.toString(stack.getTypeId());
        String combo = idToLookup;
        if(!data.equals("0"))
            combo += (":" + data);
        if(namesFromTheWeb.containsKey(combo))
            return namesFromTheWeb.get(combo);
        else if(namesFromTheWeb.containsKey(idToLookup))
            return namesFromTheWeb.get(idToLookup);
        return "";
    }

    private static void loadNamesFromWeb() {
        if(!SignShopConfig.getEnableNamesFromTheWeb())
            return;
        try {
            if(!JarUtil.loadClass("jsoup-1.7.2.jar", "Jsoup")) {
                SignShop.log("JSoup could not be loaded (needed for the NamesFromTheWeb feature),"
                        + " please make sure you have an active internet connection and remove any jsoup JAR's from the SignShop/lib folder to try again.", Level.WARNING);
                return;
            }

            Document doc = Jsoup.connect(namesURL).get();
            Element rows = doc.getElementById("rows");
            if(rows == null)
                return;

            for(Element tablerow : rows.getElementsByTag("tr")) {
                String id = tablerow.getElementsByClass("id").first().text();
                String name = tablerow.getElementsByClass("name").first().text();
                namesFromTheWeb.put(id, name);
            }
        } catch (IOException ex) {

        }
    }

}
