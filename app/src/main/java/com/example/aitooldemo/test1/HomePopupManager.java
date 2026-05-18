package com.example.aitooldemo.test1;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.LinkedList;

/**
 * 首页弹窗管理类。
 * 使用单调时钟（SystemClock.elapsedRealtime）防止用户修改系统时间绕过限制。
 *
 * <p>配置说明：
 * <ul>
 *   <li>dailyLimit = -1：24小时内无限制，仅受最小间隔约束</li>
 *   <li>dailyLimit =  0：永不弹窗</li>
 *   <li>dailyLimit >  0：24小时内最多弹出 N 次</li>
 * </ul>
 */
public class HomePopupManager {
    private static final long HOURS_24_MS = 24L * 60 * 60 * 1000;
    private static final String PREFS_NAME = "home_popup_prefs";
    private static final String KEY_BOOT_TIME = "boot_time";
    private static final String KEY_TIMESTAMPS = "timestamps";

    /** 按时间顺序存储每次弹窗的时间戳，最早在头部，最新在尾部 */
    private final LinkedList<Long> mTimestampList = new LinkedList<>();

    /** 24小时内最大弹窗次数，-1=无限制，0=不弹窗 */
    private int mHomePopupDailyLimit;

    /** 两次弹窗之间的最小间隔（分钟） */
    private int mHomePopupMinIntervalMinutes;

    public HomePopupManager(int dailyLimit, int minIntervalMinutes) {
        mHomePopupDailyLimit = dailyLimit;
        mHomePopupMinIntervalMinutes = minIntervalMinutes;
    }

    /** 更新服务器下发的配置 */
    public void updateConfig(int dailyLimit, int minIntervalMinutes) {
        mHomePopupDailyLimit = dailyLimit;
        mHomePopupMinIntervalMinutes = minIntervalMinutes;
    }

    /**
     * 判断当前是否应该弹出对话框。
     *
     * @return true 显示弹窗，false 不显示
     */
    public boolean shouldShowPopup() {
        // 步骤0：dailyLimit == 0，永不弹窗
        if (mHomePopupDailyLimit == 0) {
            return false;
        }

        long now = SystemClock.elapsedRealtime();
        long minIntervalMs = (long) mHomePopupMinIntervalMinutes * 60 * 1000;

        // 步骤2：删除超过24小时的过期记录
        while (!mTimestampList.isEmpty()
                && (now - mTimestampList.getFirst() > HOURS_24_MS)) {
            mTimestampList.removeFirst();
        }

        // 步骤1：超出每日上限时，从最早的记录开始删除多余数据（仅 dailyLimit > 0 时生效）
        if (mHomePopupDailyLimit > 0) {
            while (mTimestampList.size() > mHomePopupDailyLimit) {
                mTimestampList.removeFirst();
            }
        }

        // 步骤3：列表为空（首次调用或所有记录已过期）
        if (mTimestampList.isEmpty()) {
            mTimestampList.addLast(now);
            return true;
        }

        // 步骤3.3：检查与最近一次弹窗的间隔是否满足最小间隔
        long elapsedSinceLast = now - mTimestampList.getLast();
        if (elapsedSinceLast < minIntervalMs) {
            return false;
        }

        // 步骤3.2：dailyLimit == -1，无限制模式，仅受最小间隔约束
        if (mHomePopupDailyLimit == -1) {
            mTimestampList.addLast(now);
            return true;
        }

        // 步骤3.1：最小间隔已满足，检查24小时内是否已达日上限
        long elapsedSinceEarliest = now - mTimestampList.getFirst();

        if (elapsedSinceEarliest < HOURS_24_MS) {
            // 步骤3.1.1：最早记录在24小时内且已达每日上限，不弹窗
            if (mTimestampList.size() >= mHomePopupDailyLimit) {
                return false;
            } else {
                // 步骤3.1.2：未达上限，记录当前时间戳，允许弹窗
                mTimestampList.addLast(now);
                return true;
            }
        } else {
            // 步骤3.1.3：最早记录超过24小时，添加当前时间戳并裁减多余数据
            mTimestampList.addLast(now);
            while (mTimestampList.size() > mHomePopupDailyLimit) {
                mTimestampList.removeFirst();
            }
            return true;
        }
    }

    /** 直接记录一次弹窗事件，不经过完整判断流程 */
    public void recordPopupShown() {
        long now = SystemClock.elapsedRealtime();
        mTimestampList.addLast(now);

        // 清理超过24小时的过期记录
        while (!mTimestampList.isEmpty()
                && (now - mTimestampList.getFirst() > HOURS_24_MS)) {
            mTimestampList.removeFirst();
        }
        // 裁剪超出每日上限的多余数据
        if (mHomePopupDailyLimit > 0) {
            while (mTimestampList.size() > mHomePopupDailyLimit) {
                mTimestampList.removeFirst();
            }
        }
    }

    /**
     * 将 mTimestampList 持久化到本地 SharedPreferences。
     * 同时存储开机时间用于检测设备是否重启过。
     */
    public void saveToFile(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long bootTime = System.currentTimeMillis() - SystemClock.elapsedRealtime();

        JSONArray jsonArray = new JSONArray();
        for (Long ts : mTimestampList) {
            jsonArray.put(ts);
        }

        prefs.edit()
                .putLong(KEY_BOOT_TIME, bootTime)
                .putString(KEY_TIMESTAMPS, jsonArray.toString())
                .apply();
    }

    /**
     * 从本地 SharedPreferences 加载 mTimestampList。
     * 如果设备已重启（开机时间不匹配），则清除旧数据。
     * 加载时会自动过滤超过24小时的过期记录。
     */
    public void loadFromFile(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long savedBootTime = prefs.getLong(KEY_BOOT_TIME, -1);

        long currentBootTime = System.currentTimeMillis() - SystemClock.elapsedRealtime();
        // 首次使用或设备已重启（开机时间不匹配），清除旧数据
        if (savedBootTime == -1 || Math.abs(currentBootTime - savedBootTime) > 5000) {
            prefs.edit().clear().apply();
            return;
        }

        String timestampsJson = prefs.getString(KEY_TIMESTAMPS, null);
        if (timestampsJson == null) {
            return;
        }

        try {
            JSONArray jsonArray = new JSONArray(timestampsJson);
            long now = SystemClock.elapsedRealtime();
            mTimestampList.clear();

            for (int i = 0; i < jsonArray.length(); i++) {
                long ts = jsonArray.getLong(i);
                // 仅加载24小时内的记录，过期数据直接丢弃
                if (now - ts <= HOURS_24_MS) {
                    mTimestampList.addLast(ts);
                }
            }

            // 裁剪超出每日上限的多余数据
            if (mHomePopupDailyLimit > 0) {
                while (mTimestampList.size() > mHomePopupDailyLimit) {
                    mTimestampList.removeFirst();
                }
            }
        } catch (JSONException e) {
            mTimestampList.clear();
        }
    }

    /** 获取当前记录的时间戳数量 */
    public int getTimestampCount() {
        return mTimestampList.size();
    }

    /** 清空所有记录 */
    public void clear() {
        mTimestampList.clear();
    }
}
