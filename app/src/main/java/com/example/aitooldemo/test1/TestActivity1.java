package com.example.aitooldemo.test1;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.aitooldemo.R;

public class TestActivity1 extends AppCompatActivity {

    private BadgeProgressView badgeProgressLtr;
    private BadgeProgressView badgeProgressRtl;

    private String mUrl = "https://s3.bmp.ovh/2026/04/27/9htkEM0k.png";

    private int mProgress1 = 0;
    private int mProgress2 = 0;

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

        badgeProgressLtr.setProgressDirection(BadgeProgressView.DIRECTION_LTR);
        badgeProgressRtl.setProgressDirection(BadgeProgressView.DIRECTION_RTL);

        if (!TextUtils.isEmpty(mUrl)) {
            badgeProgressLtr.setBadgeImageUrl(mUrl);
            badgeProgressRtl.setBadgeImageUrl(mUrl);
        } else {
            badgeProgressLtr.setBadgeImageResource(R.drawable.test_ic_badge_select);
            badgeProgressRtl.setBadgeImageResource(R.drawable.test_ic_badge_select);
        }

        // 测试进度 50%
        badgeProgressLtr.setProgress(80);
        badgeProgressRtl.setProgress(30);
    }

    public void onTest1(View v) {
        if (mProgress1 > 100) {
            mProgress1 = 0;
        }
        badgeProgressLtr.setProgress(mProgress1);
        mProgress1 += 10;
    }

    public void onTest2(View v) {
        if (mProgress2 > 100) {
            mProgress2 = 0;
        }
        badgeProgressRtl.setProgress(mProgress2);
        mProgress2 += 10;
    }

}
