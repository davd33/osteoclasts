package org.osteo.main;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Created with IntelliJ IDEA.
 * User: davidr
 * Date: 7/3/13
 * Time: 7:42 PM
 * To change this template use File | Settings | File Templates.
 */
public enum Bundle {

    UI("ui");
    private String name;
    private ResourceBundle bundle;
    private static Locale locale = Locale.getDefault();

    public static Locale getLocale() {
        return locale;
    }

    public String getName() {
        return name;
    }

    Bundle(String name) {
        this.name = name;
        this.bundle = ResourceBundle.getBundle("org.osteo.bundles." + name);
    }

    public String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException ex) {
            System.out.println(ex.getMessage());
        }
        return "###";
    }

    public String getFormatedString(String key, Object... values) {
        return String.format(getLocale(), bundle.getString(key), values).toString();
    }
}
