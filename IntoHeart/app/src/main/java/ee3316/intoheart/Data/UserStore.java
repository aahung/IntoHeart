package ee3316.intoheart.Data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by aahung on 3/31/15.
 */
public class UserStore {
    private final String PREFERENCE = "into_heart";
    private final String PREFS_NAME_NAME = "user_name";
    private final String PREFS_NAME_AGE = "user_age";
    private final String PREFS_NAME_HEIGHT = "user_height";
    private final String PREFS_NAME_WEIGHT = "user_weight";
    private final String PREFS_NAME_EMERGENCY_TEL = "user_emergency_tel";
    private final String PREFS_NAME_EMAIL = "user_email";
    private final String PREFS_NAME_PASSWORD = "user_password";
    private final String PREFS_NAME_RATE_SMOKE = "user_smoke_rate";
    private final String PREFS_NAME_RATE_DRINK = "user_drink_rate";
    private final String PREFS_NAME_RATE_OVERWORK = "user_ow_rate";
    private final String PREFS_NAME_RATE_DISORDER = "user_dis_rate";
    private final String PREFS_NAME_RATE_STAY_UP = "user_stay_rate";

    SharedPreferences settings;

    private Context context;
    public String name, email, password;
    public int age, height, weight;
    public String emergencyTel;
    public float[] lifestyles = new float[5];

    public UserStore(Context context) {
        this.context = context;
        settings = context.getSharedPreferences(PREFERENCE, 0);
        fetch();
    }

    private void fetch() {
        name = settings.getString(PREFS_NAME_NAME, null);
        email = settings.getString(PREFS_NAME_EMAIL, null);
        password = settings.getString(PREFS_NAME_PASSWORD, null);
        emergencyTel = settings.getString(PREFS_NAME_EMERGENCY_TEL, null);
        age = settings.getInt(PREFS_NAME_AGE, -1);
        height = settings.getInt(PREFS_NAME_HEIGHT, -1);
        weight = settings.getInt(PREFS_NAME_WEIGHT, -1);
        lifestyles[0] = settings.getFloat(PREFS_NAME_RATE_SMOKE, 0);
        lifestyles[1] = settings.getFloat(PREFS_NAME_RATE_DRINK, 0);
        lifestyles[2] = settings.getFloat(PREFS_NAME_RATE_OVERWORK, 0);
        lifestyles[3] = settings.getFloat(PREFS_NAME_RATE_DISORDER, 0);
        lifestyles[4] = settings.getFloat(PREFS_NAME_RATE_STAY_UP, 0);
    }

    public void save() {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREFS_NAME_NAME, name);
        editor.putInt(PREFS_NAME_AGE, age);
        editor.putInt(PREFS_NAME_HEIGHT, height);
        editor.putInt(PREFS_NAME_WEIGHT, weight);
        editor.putString(PREFS_NAME_EMERGENCY_TEL, emergencyTel);
        editor.putString(PREFS_NAME_EMAIL, email);
        editor.putString(PREFS_NAME_PASSWORD, password);
        editor.putFloat(PREFS_NAME_RATE_SMOKE, lifestyles[0]);
        editor.putFloat(PREFS_NAME_RATE_DRINK, lifestyles[1]);
        editor.putFloat(PREFS_NAME_RATE_OVERWORK, lifestyles[2]);
        editor.putFloat(PREFS_NAME_RATE_DISORDER, lifestyles[3]);
        editor.putFloat(PREFS_NAME_RATE_STAY_UP, lifestyles[4]);
        editor.commit();
    }

    public Integer getAge() { return (age == -1)? null : Integer.valueOf(age); }

    public Integer getHeight() {
        return (height == -1)? null : Integer.valueOf(height);
    }

    public Integer getWeight() {
        return (weight == -1)? null : Integer.valueOf(weight);
    }

    public boolean getLogin() {
        if (email == null) return false;
        if (email.isEmpty()) return false;
        if (password == null) return false;
        if (!email.isEmpty() && !password.isEmpty()) return true;
        return false;
    }
}
