package se.simulator.findmycar_gpstracker;

import android.content.Context;
import android.preference.EditTextPreference;
import android.text.TextUtils;
import android.util.AttributeSet;

/**
 * Created by mikael on 2016-06-24.
 */
public class AutoSummaryEditTextPreference extends EditTextPreference {

    public AutoSummaryEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public AutoSummaryEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoSummaryEditTextPreference(Context context) {
        super(context);
    }

    @Override
    public CharSequence getSummary() {
        String text = getText();
        if (TextUtils.isEmpty(text)) {
            return getEditText().getHint();
        } else {
            CharSequence summary = super.getSummary();
            if (summary != null) {
                return String.format(summary.toString(), text);
            } else {
                return null;
            }
        }
    }
}
