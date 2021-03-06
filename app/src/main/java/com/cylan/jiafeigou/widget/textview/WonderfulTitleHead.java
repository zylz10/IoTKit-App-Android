package com.cylan.jiafeigou.widget.textview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

import com.cylan.jiafeigou.R;

/**
 * Created by chen on 6/12/16.
 */
public class WonderfulTitleHead extends TextView {
    private boolean titleHeadIsTop = false;
    private boolean timeLineShow = false;

    public WonderfulTitleHead(Context context) {
        super(context);
    }

    public WonderfulTitleHead(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WonderfulTitleHead(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WonderfulTitleHead(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    public void setBackgroundToRight() {
        if (titleHeadIsTop) {
            setCompoundDrawablesWithIntrinsicBounds(0, 0, timeLineShow ? R.drawable.wonderful_icon_up_white : R.drawable.wonderful_icon_down_white, 0);
        } else {
            setCompoundDrawablesWithIntrinsicBounds(0, 0, timeLineShow ? R.drawable.wonderful_arrow_up : R.drawable.wonderful_arrow_down, 0);
        }
    }

    public void setTitleHeadIsTop(boolean titleHeadIsTop) {
        this.titleHeadIsTop = titleHeadIsTop;
    }

    public void setTimeLineShow(boolean timeLineShow) {
        this.timeLineShow = timeLineShow;
    }
}
