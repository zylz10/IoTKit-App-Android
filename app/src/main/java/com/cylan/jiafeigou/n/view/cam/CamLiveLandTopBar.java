package com.cylan.jiafeigou.n.view.cam;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by cylan-hunt on 16-7-27.
 */
public class CamLiveLandTopBar extends FrameLayout {

    @BindView(R.id.imgV_cam_live_land_nav_back)
    TextView imgVCamLiveLandNavBack;
    @BindView(R.id.imgV_cam_switch_speaker)
    ImageView imgVCamSwitchSpeaker;
    @BindView(R.id.imgV_cam_trigger_recorder)
    ImageView imgVCamTriggerRecorder;
    @BindView(R.id.imgV_cam_trigger_capture)
    ImageView imgVCamTriggerCapture;

    public CamLiveLandTopBar(Context context) {
        this(context, null);
    }

    public CamLiveLandTopBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CamLiveLandTopBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_cam_land_live_top_bar, this, true);
        ButterKnife.bind(view);
    }


    public void setAllElementsState(CameraLiveFragment.CamLandLiveLayerViewBundle beanTopBar) {
        imgVCamLiveLandNavBack.setText(beanTopBar.title);
        imgVCamSwitchSpeaker.setEnabled(beanTopBar.speakerState != -1);
        imgVCamTriggerRecorder.setEnabled(beanTopBar.recorderState != -1);
        imgVCamTriggerCapture.setEnabled(beanTopBar.captureState != -1);
    }

    @OnClick({R.id.imgV_cam_live_land_nav_back, R.id.imgV_cam_switch_speaker, R.id.imgV_cam_trigger_recorder, R.id.imgV_cam_trigger_capture})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgV_cam_live_land_nav_back:
                if (topBarAction != null)
                    topBarAction.onBack();
                break;
            case R.id.imgV_cam_switch_speaker:
                if (topBarAction != null)
                    topBarAction.onSwitchSpeaker();
                break;
            case R.id.imgV_cam_trigger_recorder:
                if (topBarAction != null)
                    topBarAction.onTriggerRecorder();
                break;
            case R.id.imgV_cam_trigger_capture:
                if (topBarAction != null)
                    topBarAction.onTriggerCapture();
                break;
        }
    }

    private TopBarAction topBarAction;

    public void setTopBarAction(TopBarAction topBarAction) {
        this.topBarAction = topBarAction;
    }

    public interface TopBarAction {
        void onBack();

        void onSwitchSpeaker();

        void onTriggerRecorder();

        void onTriggerCapture();
    }
}