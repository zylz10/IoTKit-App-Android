package com.cylan.jiafeigou.n.view.panorama;

import android.support.annotation.IntDef;

import com.cylan.jiafeigou.base.view.ViewablePresenter;
import com.cylan.jiafeigou.base.view.ViewableView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.cylan.jiafeigou.n.view.panorama.PanoramaCameraContact.View.CONNECTION_MODE.BAD_NETWORK;
import static com.cylan.jiafeigou.n.view.panorama.PanoramaCameraContact.View.CONNECTION_MODE.DEVICE_OFFLINE;
import static com.cylan.jiafeigou.n.view.panorama.PanoramaCameraContact.View.CONNECTION_MODE.FINE;
import static com.cylan.jiafeigou.n.view.panorama.PanoramaCameraContact.View.PANORAMA_RECORD_MODE.MODE_LONG;
import static com.cylan.jiafeigou.n.view.panorama.PanoramaCameraContact.View.PANORAMA_RECORD_MODE.MODE_NONE;
import static com.cylan.jiafeigou.n.view.panorama.PanoramaCameraContact.View.PANORAMA_RECORD_MODE.MODE_SHORT;
import static com.cylan.jiafeigou.n.view.panorama.PanoramaCameraContact.View.PANORAMA_VIEW_MODE.MODE_HIDE;
import static com.cylan.jiafeigou.n.view.panorama.PanoramaCameraContact.View.PANORAMA_VIEW_MODE.MODE_PICTURE;
import static com.cylan.jiafeigou.n.view.panorama.PanoramaCameraContact.View.PANORAMA_VIEW_MODE.MODE_VIDEO;
import static com.cylan.jiafeigou.n.view.panorama.PanoramaCameraContact.View.SPEED_MODE.AUTO;
import static com.cylan.jiafeigou.n.view.panorama.PanoramaCameraContact.View.SPEED_MODE.FLUENCY;
import static com.cylan.jiafeigou.n.view.panorama.PanoramaCameraContact.View.SPEED_MODE.HD;
import static com.cylan.jiafeigou.n.view.panorama.PanoramaCameraContact.View.SPEED_MODE.NORMAL;

/**
 * Created by yanzhendong on 2017/3/7.
 */

public interface PanoramaCameraContact {

    interface View extends ViewableView {

        void onBellBatteryDrainOut();

        void onDeviceBatteryChanged(Integer battery);

        void onSDFormatResult(int code);

        void onDeviceInitFinish();

        void onShowNewMsgHint();

        void onDeviceOnLine();


        @IntDef({AUTO, FLUENCY, NORMAL, HD})
        @Retention(RetentionPolicy.SOURCE)
        @interface SPEED_MODE {
            int AUTO = -2;    //自动 目前未定义
            int FLUENCY = -1;//流畅 目前未定义
            int NORMAL = 0;//标清
            int HD = 1;//高清
        }

        @IntDef({FINE, DEVICE_OFFLINE, BAD_NETWORK})
        @Retention(RetentionPolicy.SOURCE)
        @interface CONNECTION_MODE {
            int FINE = 0;
            int DEVICE_OFFLINE = 1;
            int BAD_NETWORK = 2;
        }

        @IntDef({MODE_PICTURE, MODE_VIDEO, MODE_HIDE})
        @Retention(RetentionPolicy.SOURCE)
        @interface PANORAMA_VIEW_MODE {
            int MODE_PICTURE = 0;
            int MODE_VIDEO = 1;
            int MODE_HIDE = 2;
            int MODE_LIVE = 3;
        }

        @IntDef({MODE_NONE, MODE_SHORT, MODE_LONG})
        @Retention(RetentionPolicy.SOURCE)
        @interface PANORAMA_RECORD_MODE {
            int MODE_NONE = 0;
            int MODE_SHORT = 1;
            int MODE_LONG = 2;
            int MODE_SHORT_MD = 3;
            int MODE_LIVE = 4;
        }


        void onShowPreviewPicture(String picture);

        void onSwitchSpeedMode(@SPEED_MODE int mode);

        void onRefreshVideoRecordUI(int second, int type);

        void onReportDeviceError(int err, boolean sync);

        void onNewFirmwareRsp();

        void onRefreshViewModeUI(int viewMode, boolean enable, boolean record);

        void onRefreshControllerView(boolean enable, boolean all);

        /**
         * @param connectionType -1:根据设备网络和真实网络情况决定是否显示 banner
         */
        void onRefreshConnectionMode(int connectionType);

        void onRefreshControllerViewVisible(boolean visible);

        class RecordFinishEvent {
            public static final RecordFinishEvent INSTANCE = new RecordFinishEvent();
        }
    }

    interface Presenter extends ViewablePresenter<View> {

        void makePhotograph();//拍照

        void checkAndInitRecord();

        void switchVideoResolution(@View.SPEED_MODE int mode);

        void startVideoRecord(int type);

        void stopVideoRecord(int type);

        void formatSDCard();

        boolean isApiAvailable();

        void startLiveRecord();

        void stopLiveRecord();
    }
}
