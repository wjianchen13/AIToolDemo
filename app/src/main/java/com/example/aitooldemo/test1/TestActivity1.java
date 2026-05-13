package com.example.aitooldemo.test1;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.aitooldemo.R;

public class TestActivity1 extends AppCompatActivity {

    private HomePopupManager mPopupManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_test1);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize popup manager: dailyLimit=3, minInterval=2 minutes
        mPopupManager = new HomePopupManager(3, 2);
    }

    public void onTest1(View v) {
        Toast.makeText(this, "Test1 clicked", Toast.LENGTH_SHORT).show();
    }

    public void onTest2(View v) {
        Toast.makeText(this, "Test2 clicked", Toast.LENGTH_SHORT).show();
    }

    public void onCheckPopup(View v) {
        boolean shouldShow = mPopupManager.shouldShowPopup();
        String msg = shouldShow ? "POPUP SHOWN (returned true)" : "POPUP HIDDEN (returned false)";
        Toast.makeText(this, msg + "\nCount: " + mPopupManager.getTimestampCount(), Toast.LENGTH_LONG).show();
        android.util.Log.d("HomePopupTest", msg);
    }

    public void onCheckPopupBurst(View v) {
        StringBuilder sb = new StringBuilder("Burst test results:\n");
        int showCount = 0;
        for (int i = 0; i < 5; i++) {
            boolean shouldShow = mPopupManager.shouldShowPopup();
            sb.append("  #").append(i + 1).append(": ").append(shouldShow ? "SHOW" : "HIDE").append("\n");
            if (shouldShow) showCount++;
        }
        sb.append("Total shown: ").append(showCount);
        Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show();
        android.util.Log.d("HomePopupTest", sb.toString());
    }

    public void onResetPopup(View v) {
        mPopupManager.clear();
        Toast.makeText(this, "Popup Manager reset (list cleared)", Toast.LENGTH_SHORT).show();
    }
}
