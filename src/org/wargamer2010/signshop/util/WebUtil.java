
package org.wargamer2010.signshop.util;

import com.gtranslate.Language;
import com.gtranslate.Translator;
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
    private static final Map<String, String> translateCache = new HashMap<String, String>();
    private static final Map<String, String> namesFromTheWeb = new HashMap<String, String>();
    private static Translator translator = null;
    private static String toLanguage = "";

    private WebUtil() {

    }

    public static void init() {
        if(SignShopConfig.getEnableGoogleTranslation()) {
            // Download the Jar if needed and load the two needed classes with the current classloader
            if(!JarUtil.loadClass("gtranslateapi-1.0.jar", "com.gtranslate.Translator"))
                return;
            if(!JarUtil.loadClass("gtranslateapi-1.0.jar", "com.gtranslate.Language"))
                return;

            try {
                // Get an instance of the Translator
                translator = Translator.getInstance();
                // Get an handle to the Translate function
                String lang = SignShopConfig.getPreferredLanguage().toUpperCase();
                for (Field field : Language.class.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers()) && field.getName().equalsIgnoreCase(lang) && field.getType() == String.class) {
                        // We found the static property we're looking for (i.e. ENGLISH -> 'en')
                        toLanguage = (String)field.get(new String());
                    }
                }
            } catch (SecurityException ex) {
                translator = null;
            } catch (IllegalAccessException ex) {
                translator = null;
            } catch (IllegalArgumentException ex) {
                translator = null;
            }
        }

        loadNamesFromWeb();
    }

    /**
     * Translates the given text to the preferred language specified in the SignShop Config
     * @param text
     * @return
     */
    public static synchronized String translateFromEnglish(String text) {
        if(text == null || text.trim().isEmpty() || !SignShopConfig.getEnableGoogleTranslation() || toLanguage.equals(baseLanguage))
            return text;
        // Translations come out weird when there are capitals in anything other than the first word
        String lower = text.toLowerCase();
        if(translateCache.containsKey(lower))
            return translateCache.get(lower);

        if(translator == null || toLanguage.isEmpty())
            return text;

        String result = translator.translate(lower, baseLanguage, toLanguage);
        String capped = signshopUtil.capFirstLetter(result);
        translateCache.put(lower, capped);
        return capped;
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
