package com.aokiji.partner.utils;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangdonghai on 2018/7/31.
 */

public class ActivityController {

    private static List<Activity> sActivities = new ArrayList<>();


    public static void addActivity(Activity activity) {
        sActivities.add(activity);
    }


    public static void removeActivity(Activity activity) {
        if (sActivities.contains(activity)) {
            sActivities.remove(activity);
        }
    }


    public static void removeAllActivity() {
        for (Activity activity : sActivities) {
            activity.finish();
        }
    }

}
