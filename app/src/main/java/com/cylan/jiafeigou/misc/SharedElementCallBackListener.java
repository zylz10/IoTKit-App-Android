package com.cylan.jiafeigou.misc;

import android.view.View;

import java.util.List;
import java.util.Map;

/**
 * Created by cylan-hunt on 16-9-7.
 */
public interface SharedElementCallBackListener {
    void onSharedElementCallBack(List<String> names, Map<String, View> sharedElements);
}