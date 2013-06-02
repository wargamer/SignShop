
package org.wargamer2010.signshop.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import org.wargamer2010.signshop.configuration.SignShopConfig;

public class googleTranslateUtil {
    private static final String downloadURL = "http://java-google-translate-text-to-speech.googlecode.com/files/";
    private static final Map<String, String> translateCache = new HashMap<String, String>();
    private static Method translateMethod = null;
    private static Object translator = null;
    private static String toLanguage = "";

    private googleTranslateUtil() {

    }

    public static void init() {
        if(!SignShopConfig.getEnableGoogleTranslation())
            return;

        // Download the Jar if needed and load the two needed classes with the current classloader
        Class<?> translatorClass = signshopUtil.loadClass(downloadURL, "gtranslateapi-1.0.jar", "com.gtranslate.Translator");
        Class<?> languageClass = signshopUtil.loadClass(downloadURL, "gtranslateapi-1.0.jar", "com.gtranslate.Language");

        if(translatorClass != null && languageClass != null) {
            try {
                // Get an instance of the Translator
                translator = translatorClass.getMethod("getInstance", new Class<?>[0]).invoke(null);
                // Get an handle to the Translate function
                translateMethod = translatorClass.getMethod("translate", new Class<?>[] { String.class, String.class, String.class });
                String lang = SignShopConfig.getPreferredLanguage().toUpperCase();
                for (Field field : languageClass.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers()) && field.getName().equalsIgnoreCase(lang) && field.getType() == String.class) {
                        // We found the static property we're looking for (i.e. ENGLISH -> 'en')
                        toLanguage = (String)field.get(new String());
                    }
                }
            } catch (NoSuchMethodException ex) {
                translator = null;
            } catch (SecurityException ex) {
                translator = null;
            } catch (IllegalAccessException ex) {
                translator = null;
            } catch (IllegalArgumentException ex) {
                translator = null;
            } catch (InvocationTargetException ex) {
                translator = null;
            }
        }
    }

    /**
     * Translates the given text to the preferred language specified in the SignShop Config
     * @param text
     * @return
     */
    public static synchronized String translateFromEnglish(String text) {
        if(text == null || text.trim().isEmpty() || !SignShopConfig.getEnableGoogleTranslation())
            return text;
        // Translations come out weird when there are capitals in anything other than the first word
        String lower = text.toLowerCase();
        if(translateCache.containsKey(lower))
            return translateCache.get(lower);

        if(translator == null || toLanguage.isEmpty())
            return text;
        try {
            Object result = translateMethod.invoke(translator, lower, "en", toLanguage);
            translateCache.put(lower, (String)result);
            return signshopUtil.capFirstLetter((String)result);
        }
        catch (IllegalAccessException ex) { }
        catch (IllegalArgumentException ex) { }
        catch (InvocationTargetException ex) { }

        return text;
    }



}
