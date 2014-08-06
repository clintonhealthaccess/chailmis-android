package org.clintonhealthaccess.lmis.app.validators;

import android.widget.AutoCompleteTextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AllocationIdValidator implements AutoCompleteTextView.Validator {
    @Override
    public boolean isValid(CharSequence text) {
        String patternString = "^\\w{2}-\\d+$";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(text);
        return matcher.matches();
    }

    @Override
    public CharSequence fixText(CharSequence invalidText) {
        return invalidText;
    }
}
