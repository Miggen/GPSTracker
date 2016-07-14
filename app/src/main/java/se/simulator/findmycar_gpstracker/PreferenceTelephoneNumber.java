
package se.simulator.findmycar_gpstracker;

        import android.content.Context;
        import android.content.SharedPreferences;
        import android.os.Build;
        import android.preference.EditTextPreference;
        import android.telephony.PhoneNumberUtils;
        import android.telephony.TelephonyManager;
        import android.text.TextUtils;
        import android.util.AttributeSet;
        import android.util.Log;


public class PreferenceTelephoneNumber extends EditTextPreference {

    public PreferenceTelephoneNumber(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public PreferenceTelephoneNumber(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PreferenceTelephoneNumber(Context context) {
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

    @Override
    protected void onDialogClosed(boolean positiveResult){
        super.onDialogClosed(positiveResult);

        if (positiveResult){
            String number = getEditText().getText().toString();
            if (callChangeListener(number)){
                SharedPreferences sharedPref = getContext()
                        .getSharedPreferences(getContext()
                                .getString(R.string.pref_file_key),getContext().MODE_PRIVATE);
                PhoneNumberUtils utils = new PhoneNumberUtils();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    number = utils.formatNumber(number,sharedPref
                            .getString(getContext().getString(R.string.saved_country_iso),
                                    sharedPref.getString(getContext().getString(R.string.default_country_iso),"SE")));
                }
                else
                {
                    number = utils.formatNumber(number);
                }
                if (utils.isWellFormedSmsAddress(number)) {
                    setText(number);
                }
            }
        }
    }
}
