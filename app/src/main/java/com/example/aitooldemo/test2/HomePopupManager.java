package com.example.aitooldemo.test2;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.LinkedList;

/**
 * 管理首页弹窗的显示策略。
 * 规则：
 *   - homePopupDailyLimit：24小时内最多弹出次数
 *   - homePopupMinIntervalMinutes：两次弹出之间的最小间隔（分钟）
 *
 * 时间戳通过 SharedPreferences 持久化，防止 App 重启后丢失计数。
 */
public class HomePopupManager {

    private static final String PREF_NAME = "home_popup_prefs";
    private static final String KEY_TIMESTAMPS = "popup_timestamps";
    private static final String SEPARATOR = ",";
    private static final long MILLIS_24H = 24 * 60 * 60 * 1000L;

    private final Context context;
    private final int homePopupDailyLimit;
    private final long minIntervalMillis;
    private final LinkedList<Long> timestamps;

    public HomePopupManager(Context context, int homePopupDailyLimit, int homePopupMinIntervalMinutes) {
        this.context = context.getApplicationContext();
        this.homePopupDailyLimit = homePopupDailyLimit;
        this.minIntervalMillis = (long) homePopupMinIntervalMinutes * 60 * 1000L;
        this.timestamps = loadTimestamps();
    }

    /**
     * 判断是否应该显示首页弹窗。
     * 如果可以显示，会自动记录本次弹出时间戳。
     *
     * @return true 表示显示弹窗，false 表示不显示
     */
    public boolean canShowPopup() {
        // 0=不弹窗
        if (homePopupDailyLimit == 0) {
            return false;
        }

        long now = System.currentTimeMillis();

        // 1. 若超出 dailyLimit，删除最旧的多余记录（-1=无限制时跳过）
        if (homePopupDailyLimit > 0) {
            boolean removed = false;
            while (timestamps.size() > homePopupDailyLimit) {
                timestamps.removeFirst();
                removed = true;
            }
            if (removed) saveTimestamps(timestamps);
        }

        // 2. 删除超过 24 小时的记录
        if (timestamps.removeIf(ts -> now - ts >= MILLIS_24H)) {
            saveTimestamps(timestamps);
        }

        // 3. 检查距上次弹出是否满足最小间隔
        if (!timestamps.isEmpty()) {
            long lastTimestamp = timestamps.getLast();
            if (now - lastTimestamp < minIntervalMillis) {
                return false;
            }
        }

        // 4. 检查 24 小时内是否已达上限（-1=无限制时跳过）
        if (homePopupDailyLimit > 0 && timestamps.size() >= homePopupDailyLimit) {
            return false;
        }

        // 可以显示：记录本次时间戳并持久化
        timestamps.addLast(now);
        saveTimestamps(timestamps);
        return true;
    }

    /** 清除所有历史记录（用于测试或重置） */
    public void reset() {
        timestamps.clear();
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().remove(KEY_TIMESTAMPS).apply();
    }

    /** 返回当前 24 小时内已记录的弹出次数（用于调试） */
    public int getPopupCountInLast24h() {
        long now = System.currentTimeMillis();
        int count = 0;
        for (Long ts : timestamps) {
            if (now - ts < MILLIS_24H) count++;
        }
        return count;
    }

    private LinkedList<Long> loadTimestamps() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String raw = prefs.getString(KEY_TIMESTAMPS, "");
        LinkedList<Long> list = new LinkedList<>();
        if (raw.isEmpty()) return list;
        for (String part : raw.split(SEPARATOR)) {
            try {
                list.add(Long.parseLong(part));
            } catch (NumberFormatException ignored) {
            }
        }
        return list;
    }

    private void saveTimestamps(LinkedList<Long> timestamps) {
        StringBuilder sb = new StringBuilder();
        for (Long ts : timestamps) {
            if (sb.length() > 0) sb.append(SEPARATOR);
            sb.append(ts);
        }
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().putString(KEY_TIMESTAMPS, sb.toString()).apply();
    }
}
