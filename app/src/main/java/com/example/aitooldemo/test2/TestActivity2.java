package com.example.aitooldemo.test2;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.aitooldemo.R;

public class TestActivity2 extends AppCompatActivity {

    // 测试参数：24小时内最多弹3次，每次间隔至少1分钟
    private static final int DAILY_LIMIT = 3;
    private static final int MIN_INTERVAL_MINUTES = 1;

    private HomePopupManager popupManager;
    private TextView tvPopupResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_test2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        popupManager = new HomePopupManager(this, DAILY_LIMIT, MIN_INTERVAL_MINUTES);
        tvPopupResult = findViewById(R.id.tvPopupResult);
        updateResultText("点击尝试显示弹窗开始测试");
    }

    public void onTest1(View v) {
    }

    public void onTest2(View v) {
    }

    public void onTest3(View v) {
    }

    public void onTest4(View v) {
    }

    public void onTryShowPopup(View v) {
        boolean canShow = popupManager.canShowPopup();
        int count = popupManager.getPopupCountInLast24h();
        if (canShow) {
            new AlertDialog.Builder(this)
                    .setTitle("活动弹窗")
                    .setMessage("这是首页活动弹窗！")
                    .setPositiveButton("知道了", null)
                    .show();
            updateResultText("已显示弹窗，24小时内已弹出：" + count + "/" + DAILY_LIMIT + " 次");
        } else {
            updateResultText("不显示弹窗，24小时内已弹出：" + count + "/" + DAILY_LIMIT + " 次\n（达到上限或未到最小间隔）");
        }
    }

    public void onResetPopup(View v) {
        popupManager.reset();
        updateResultText("已重置，24小时内弹出次数：0/" + DAILY_LIMIT);
    }

    private void updateResultText(String text) {
        tvPopupResult.setText(text);
    }
}
