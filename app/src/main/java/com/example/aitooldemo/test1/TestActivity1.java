package com.example.aitooldemo.test1;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.aitooldemo.R;

public class TestActivity1 extends AppCompatActivity {

    private BadgeProgressView badgeProgressLtr;
    private BadgeProgressView badgeProgressRtl;

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

        initBadgeProgressDemo();
    }

    private void initBadgeProgressDemo() {
        badgeProgressLtr = findViewById(R.id.badgeProgressLtr);
        badgeProgressRtl = findViewById(R.id.badgeProgressRtl);

        ViewCompat.setLayoutDirection(badgeProgressLtr, ViewCompat.LAYOUT_DIRECTION_LTR);
        ViewCompat.setLayoutDirection(badgeProgressRtl, ViewCompat.LAYOUT_DIRECTION_RTL);
        badgeProgressLtr.setProgressDirection(BadgeProgressView.DIRECTION_LTR);
        badgeProgressRtl.setProgressDirection(BadgeProgressView.DIRECTION_RTL);

        badgeProgressLtr.setBadgeImageResource(R.drawable.test_ic_badge_select);
        badgeProgressRtl.setBadgeImageResource(R.drawable.test_ic_badge_select);

        // 测试进度 50%
        badgeProgressLtr.setProgress(0.5f);
        badgeProgressRtl.setProgress(0.5f);
    }
}
