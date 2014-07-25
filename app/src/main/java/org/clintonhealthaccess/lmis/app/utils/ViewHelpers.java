package org.clintonhealthaccess.lmis.app.utils;

public class ViewHelpers {
    public static int getIntFromString(String number) {
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }


}
