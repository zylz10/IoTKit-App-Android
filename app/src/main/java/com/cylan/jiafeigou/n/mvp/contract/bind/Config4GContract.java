package com.cylan.jiafeigou.n.mvp.contract.bind;

import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.utils.APObserver;

/**
 * Created by yanzhendong on 2018/3/25.
 */

public interface Config4GContract {


    interface View extends BaseView {
        void onSIMCheckerFailed(APObserver.ScanResult scanResult);

        void onSIMCheckerSuccess(APObserver.ScanResult scanResult);

        void onApnUpdateRequired(APObserver.ScanResult scanResult);

        void onApnUpdateError();

        void onApnUpdateTimeout();

        void onApnUpdateSuccess();
    }

    interface Presenter extends JFGPresenter {
        void performSIMCheckerAndGoNext();

        void performSendApnTableAndGoNext();
    }

}