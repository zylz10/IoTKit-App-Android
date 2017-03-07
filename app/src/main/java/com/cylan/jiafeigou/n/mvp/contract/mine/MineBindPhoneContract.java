package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.rx.RxEvent;

import rx.Subscription;

/**
 * 作者：zsl personal
 * 创建时间：2016/9/1
 * 描述：
 */
public interface MineBindPhoneContract {

    interface View extends BaseView<Presenter> {
        /**
         * 修改标题
         */
        void initToolbarTitle(String title);

        /**
         * 获取到输入号码
         *
         * @return
         */
        String getInputPhone();

        /**
         * 获取到输入的验证码
         *
         * @return
         */
        String getInputCheckCode();

        /**
         * 检测账号的是否已经注册的结果
         *
         * @param checkAccountCallback
         */
        void handlerCheckPhoneResult(RxEvent.CheckAccountCallback checkAccountCallback);

        /**
         * 校验短信验证码的结果
         */
        void handlerCheckCodeResult(RxEvent.ResultVerifyCode resultVerifyCode);

        /**
         * 处理修改结果
         */
        void handlerResetPhoneResult(RxEvent.GetUserInfo getUserInfo);

        /**
         * 显示loading
         */
        void showLoadingDialog();

        /**
         * 隐藏loading
         */
        void hideLoadingDialog();

        /**
         * 网络状态变化
         */
        void onNetStateChanged(int state);

        /**
         * 获取验证码的结果
         */
        void getSmsCodeResult(int code);
    }

    interface Presenter extends BasePresenter {
        /**
         * 判断是绑定还是修改
         *
         * @param userinfo
         */
        void isBindOrChange(JFGAccount userinfo);

        /**
         * 获取到验证码
         */
        void getCheckCode(String phone);

        /**
         * 检测账号是否已经注册
         */
        void checkPhoneIsBind(String phone);

        /**
         * 获取到检测账号的回调
         *
         * @return
         */
        Subscription getCheckPhoneCallback();

        /**
         * 发送修改手机号请求
         */
        void sendChangePhoneReq(String newPhone,String token);

        /**
         * 获取验证码的回调
         *
         * @return
         */
        Subscription getCheckCodeCallback();

        /**
         * 获取到用户的信息
         *
         * @return
         */
        Subscription getAccountCallBack();

        /**
         * 校验短信验证码
         *
         * @param code
         */
        void CheckVerifyCode(String phone,String inputcode, String code);

        /**
         * 校验短信验证码的回调
         *
         * @return
         */
        Subscription checkVerifyCodeCallBack();

        /**
         * 注册网络监听
         */
        void registerNetworkMonitor();

        /**
         * 移除网络监听
         */
        void unregisterNetworkMonitor();

        /**
         * 三方登录的回调
         *
         * @return
         */
        Subscription openLoginBack();

        /**
         * 是否三方登录
         *
         * @return
         */
        boolean isOpenLogin();

    }

}
