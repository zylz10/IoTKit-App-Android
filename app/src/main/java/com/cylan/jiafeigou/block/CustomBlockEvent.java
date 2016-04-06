package com.cylan.jiafeigou.block;

import android.content.Context;
import android.os.Handler;

import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.utils.ToastUtil;

import java.text.SimpleDateFormat;
import java.util.Locale;

import cylan.blockcanary.OnBlockEventInterceptor;

/**
 * Created by hunt on 16-4-6.
 */
public class CustomBlockEvent implements OnBlockEventInterceptor {
    private static final String TAG = "[DEBUG]: cost much time ";
    //需要对应的
    /**
     * {@link cylan.blockcanary.log.Block#timeStart}的格式
     */
    private static final SimpleDateFormat TIME_FORMATTER
            = new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.getDefault());

    @Override
    public void onBlockEvent(Context context, final String timeStart) {
        if (BuildConfig.DEBUG)
            new Handler().post(new Runnable() {
                @Override
                public void run() {
//                    long tmpTime = 0L;
//                    try {
//                        Date date = TIME_FORMATTER.parse(timeStart);
//                        tmpTime = date.getTime();
//                    } catch (ParseException e) {
//                        Log.w("CustomBlockEvent", "ParseException");
//                    }
//                    final String content =
//                            String.format(TAG, (System.currentTimeMillis() - tmpTime));
                    ToastUtil.showFailToast(BlockCanaryContext.get().getContext()
                            , TAG);
                }
            });
    }
}