package com.mathgeniusguide.tapemeasure;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * Created by grenade on 11/26/2017.
 */

public class ChecklistActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        int checkBoxCount = 27;
        int i;
        boolean checked;
        CheckBox box;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checklist);
        SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
        for (i = 0; i < checkBoxCount; i++) {
            checked = (pref.getInt("checkbox" + i, 0) == 1);
            box = (CheckBox) findViewByIdString("checkbox" + i);
            box.setChecked(checked);
        }
        for (i = 100; i < 100 + checkBoxCount; i++) {
            checked = (pref.getInt("checkbox" + i, 0) == 1);
            box = (CheckBox) findViewByIdString("checkbox" + i);
            box.setChecked(checked);
        }
        Button close = (Button) findViewById(R.id.backChecklist);
        close.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                int checkBoxCount = 27;
                int i;
                boolean checked;
                CheckBox box;
                for (i = 0; i < checkBoxCount; i++) {
                    box = (CheckBox) findViewByIdString("checkbox" + i);
                    checked = box.isChecked();
                    saveInt("checkbox" + i, checked ? 1 : 0);
                }
                for (i = 100; i < 100 + checkBoxCount; i++) {
                    box = (CheckBox) findViewByIdString("checkbox" + i);
                    checked = box.isChecked();
                    saveInt("checkbox" + i, checked ? 1 : 0);
                }
                finish();
            }
        });
    }

    public int viewId(String string) {
        return this.getResources().getIdentifier(string, "id", this.getPackageName());
    }

    public View findViewByIdString(String string) {
        return findViewById(viewId(string));
    }

    public void saveInt(String key, int value) {
        Context context = ChecklistActivity.this;
        SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(key, value);
        editor.commit();
    }
}
