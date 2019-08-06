package me.vandalko.dynamic_theming;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.google.android.play.core.splitcompat.SplitCompat;

public class MainActivity extends Activity {

    private static final String TAG = "dynamic_theme";

    private static final String BASE_ID = "me.vandalko.dynamic_theming";
    private static final String SPLIT_ID = "me.vandalko.dynamic_theme";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        SplitCompat.install(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SharedPreferences prefs = getSharedPreferences("theme", MODE_PRIVATE);

        switch (prefs.getInt("theme", R.id.reset_theme)) {
            case R.id.builtin_theme:
                getTheme().applyStyle(R.style.BuiltInOverlay, true);
                break;

            case R.id.dynamic_theme:
                // Perform first lookup through existing context.
                int style = findStyleInResources(getResources());
                if (style == 0) {
                    Log.e(TAG, "Style is not found in base context");

                    // It's time to create split APK context.
                    try {
                        // Follow official sample:
                        // https://github.com/googlesamples/android-dynamic-features/blob/2334c45c988ba99581cdecc8150fdf6dc9f79bb1/app/src/main/java/com/google/android/samples/dynamicfeatures/MainActivity.kt#L223-L227
                        Context splitContext = createPackageContext(BASE_ID, 0);
                        SplitCompat.install(splitContext);
                        style = findStyleInResources(splitContext.getResources());
                    } catch (PackageManager.NameNotFoundException e) {
                        Log.e(TAG, "error", e);
                        style = 0;
                    }

                    if (style == 0) {
                        try {
                            Log.e(TAG, "Style is not found in refreshed base context");

                            Context splitContext = createPackageContext(SPLIT_ID, 0);
                            SplitCompat.install(splitContext);
                            style = findStyleInResources(splitContext.getResources());

                            if (style == 0) {
                                Log.e(TAG, "Style is not found in refreshed split context");
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            Log.e(TAG, "error", e);
                            style = 0;
                        }
                    }
                }

                Log.e(TAG, "Style id=" + style);

                if (style != 0) {
                    getTheme().applyStyle(style, true);
                }

                break;

            default:
                // Do nothing.
        }

        setContentView(R.layout.activity_main);

        findViewById(R.id.reset_theme).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefs.edit().remove("theme").apply();
                recreate();
            }
        });

        findViewById(R.id.builtin_theme).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefs.edit().putInt("theme", R.id.builtin_theme).apply();
                recreate();
            }
        });

        findViewById(R.id.dynamic_theme).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefs.edit().putInt("theme", R.id.dynamic_theme).apply();
                recreate();
            }
        });
    }

    private static int findStyleInResources(final Resources resources) {
        final String name = "DynamicOverlay";
        final String defType = "style";

        int style = resources.getIdentifier(name, defType, BASE_ID);
        if (style == 0) {
            Log.e(TAG, "Style is not found in base package");

            style = resources.getIdentifier(name, defType, SPLIT_ID);
            if (style == 0) {
                Log.e(TAG, "Style is not found in split package");
            }
        }

        return style;
    }
}
