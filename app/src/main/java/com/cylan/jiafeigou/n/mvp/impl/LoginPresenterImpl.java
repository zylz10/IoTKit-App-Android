package com.cylan.jiafeigou.n.mvp.impl;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.cylan.entity.JfgEnum;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.JCache;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.login.LoginContract;
import com.cylan.jiafeigou.n.mvp.model.LoginAccountBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.qqLogIn.Constants;
import com.cylan.jiafeigou.support.qqLogIn.TencentInstance;
import com.cylan.jiafeigou.support.sina.AccessTokenKeeper;
import com.cylan.jiafeigou.support.sina.SinaLogin;
import com.cylan.jiafeigou.support.sina.UsersAPI;
import com.cylan.jiafeigou.utils.AESUtil;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.FileUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.gson.Gson;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.utils.LogUtil;
import com.tencent.connect.UserInfo;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


/**
 * Created by lxh on 16-6-24.
 */
public class LoginPresenterImpl extends AbstractPresenter<LoginContract.View>
        implements LoginContract.Presenter {

    private static final String DEFAULT_REQUEST_VALUE = "default_request_value";
    private Context ctx;
    //    private CompositeSubscription subscription;
    private SinaLogin sinaUtil;
    private TencentInstance tencentInstance;
    private QQAuthrizeListener qqAuthrizeListener;
    private TwitterAuthClient twitterAuthClient;
    private CallbackManager callbackManager;

    private boolean isLoginSucc;
    private boolean isRegSms;
    private boolean isReg;

    public LoginPresenterImpl(LoginContract.View view) {
        super(view);
        view.setPresenter(this);
        ctx = view.getContext();
    }

    @Override
    public void executeLogin(final LoginAccountBean login) {
        Observable.just(login)
                .subscribeOn(Schedulers.newThread())
                .map((LoginAccountBean o) -> {
                    try {
                        JfgCmdInsurance.getCmd().login(o.userName, o.pwd);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                    AppLogger.i("LoginAccountBean: " + new Gson().toJson(login));
                    //非三方登录的标记
                    RxBus.getCacheInstance().postSticky(false);
                    return o;
                })
                .subscribe();

        //账号和密码
        try {
            String hex = AESUtil.encrypt(login.userName+"|"+login.pwd);
            FileUtils.saveDataToFile(getView().getContext(),hex);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 执行第三方登录
     *
     * @param accend_token
     */
    @Override
    public void executeOpenLogin(final String accend_token, int type) {
        rx.Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe((Object o) -> {
                    try {
                        JfgCmdInsurance.getCmd().openLogin(accend_token, "www.cylan.com", type);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                    //第三方登录的标记
                    RxBus.getCacheInstance().postSticky(true);
                }, (Throwable throwable) -> {
                    AppLogger.e("executeOpenLogin" + new Gson().toJson(accend_token));
                });
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{
                resultLoginSub(),
                resultVerifyCodeSub(),
                smsCodeResultSub(),
                switchBoxSub(),
                loginPopBackSub(),
//                resultRegisterSub(),
                checkAccountBack()
        };
    }

    private Subscription resultLoginSub() {
        //sdk中，登陆失败的话，自动一分钟登录一次。
        return RxBus.getCacheInstance().toObservable(RxEvent.ResultLogin.class)
                .delay(500, TimeUnit.MILLISECONDS)//set a delay
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((RxEvent.ResultLogin resultLogin) -> {
                    if (getView().isLoginViewVisible()) {
                        getView().loginResult(resultLogin.code);
                        if (resultLogin.code == 0){
                            isLoginSucc = true;
                        }
                   }
                }, (Throwable throwable) -> {
                    AppLogger.e("" + throwable);
                });
    }

    private Subscription resultRegisterSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.ResultRegister.class)
                .subscribeOn(Schedulers.newThread())
                .delay(1000, TimeUnit.MILLISECONDS)//set a delay
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((RxEvent.ResultRegister register) -> {
                    if (getView().isLoginViewVisible()) {
                        getView().registerResult(register.code);
                    }
                    if (register.code == JError.ErrorOK) {
                        //注册成功
                        PreferencesUtils.putString(JConstant.KEY_REGISTER_SMS_TOKEN, "");
                        getView().registerResult(register.code);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("" + throwable.getLocalizedMessage());
                    }
                });
    }

    private Subscription resultVerifyCodeSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.ResultVerifyCode.class)
                .subscribeOn(Schedulers.newThread())
                .delay(1000, TimeUnit.MILLISECONDS)//set a delay
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.ResultVerifyCode>() {
                    @Override
                    public void call(RxEvent.ResultVerifyCode resultVerifyCode) {
                        if (isRegSms)
                        getView().verifyCodeResult(resultVerifyCode.code);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("" + throwable.getLocalizedMessage());
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        AppLogger.d("complete?");
                    }
                });
    }

    private Subscription smsCodeResultSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.SmsCodeResult.class)
                .delay(1000, TimeUnit.MILLISECONDS)//set a delay
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((RxEvent.SmsCodeResult smsCodeResult) -> {
                    if (getView().isLoginViewVisible() && JCache.isSmsAction) {
//                            getView().registerResult(smsCodeResult.error);
                        if (smsCodeResult.error == 0) {
                            //store the token .
                            PreferencesUtils.putString(JConstant.KEY_REGISTER_SMS_TOKEN, smsCodeResult.token);
                        }
                    }
                }, (Throwable throwable) -> {
                    AppLogger.e("" + throwable.getLocalizedMessage());
                });
    }

    private Subscription switchBoxSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.SwitchBox.class)
                .delay(1000, TimeUnit.MILLISECONDS)//set a delay
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.SwitchBox>() {
                    @Override
                    public void call(RxEvent.SwitchBox switchBox) {
                        getView().switchBox("");
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("" + throwable.getLocalizedMessage());
                    }
                });
    }

    private Subscription loginPopBackSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.LoginPopBack.class)
                .delay(1000, TimeUnit.MILLISECONDS)//set a delay
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.LoginPopBack>() {
                    @Override
                    public void call(RxEvent.LoginPopBack loginPopBack) {
                        getView().updateAccount(loginPopBack.account);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("" + throwable.getLocalizedMessage());
                    }
                });
    }

    @Override
    public void getQQAuthorize(Activity activity) {
        tencentInstance = new TencentInstance();
        if (tencentInstance.mTencent.isSessionValid()) {
            executeOpenLogin(tencentInstance.mTencent.getAccessToken(), 3);
            return;
        }
        qqAuthrizeListener = new QQAuthrizeListener();
        tencentInstance.logIn(activity, Constants.SCOPE, qqAuthrizeListener);
    }

    @Override
    public void startSinaAuthorize(Activity activity) {
        Oauth2AccessToken accessToken = AccessTokenKeeper.readAccessToken(getView().getContext());
        if (accessToken != null && accessToken.isSessionValid()) {
            executeOpenLogin(accessToken.getToken(), 4);
            UsersAPI usersAPI = new UsersAPI(accessToken, getView().getContext());
            Long uid = Long.parseLong(accessToken.getUid());
            usersAPI.show(uid, sinaRequestListener);
            return;
        }

        sinaUtil = new SinaLogin(activity);
        sinaUtil.login(activity, new SinaAuthorizeListener());
    }

    /**
     * 获取Twitter的授权
     * @param activity
     */
    @Override
    public void getTwitterAuthorize(Activity activity) {
        if (twitterAuthClient == null){
            twitterAuthClient = new TwitterAuthClient();
        }
        twitterAuthClient.authorize(activity, new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                String name = result.data.getUserName();
                long userId = result.data.getUserId();
                long id = result.data.getId();
                TwitterAuthToken token = result.data.getAuthToken();
                String secret = token.secret;
                String strToken = token.token;
                executeOpenLogin(strToken,6);

                // 获取用户的的信息
                TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
                Call<User> call =  twitterApiClient.getAccountService().verifyCredentials(false, false);
                call.enqueue(new Callback<User>() {
                    @Override
                    public void success(Result<User> result) {
                        String dataResult = "Name: " + result.data.name +
                                "\nScreenName: " + result.data.screenName +
                                "\nProfileImage: " + result.data.profileImageUrl +
                                "\nBackgroungUrl" + result.data.profileBannerUrl +
                                "\nCreated at" + result.data.createdAt +
                                "\nDescription" + result.data.description +
                                "\nEmail" + result.data.email+
                                "\nFriends Count" + result.data.friendsCount;
                        System.out.println(result.data.profileImageUrl);

                        String twitter_id = String.valueOf(result.data.id);
                        String twitter_name = result.data.name;
                        String[] str  = {twitter_name, result.data.profileImageUrl};

                        PreferencesUtils.putString(JConstant.OPEN_LOGIN_USER_ALIAS, str[0]);
                        PreferencesUtils.putString(JConstant.OPEN_LOGIN_USER_ICON, str[1]);
                    }

                    @Override
                    public void failure(TwitterException exception) {
                        AppLogger.e("twittergetUserInfo"+exception.getMessage());
                    }
                });
            }

            @Override
            public void failure(TwitterException e) {
                ToastUtil.showNegativeToast("授权失败");
                AppLogger.e("twitter授权："+e);
            }
        });
    }

    /**
     * 获取Facebook的授权
     * @param activity
     */
    @Override
    public void getFaceBookAuthorize(Activity activity) {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null && !accessToken.isExpired()){
            //直接登录
            executeOpenLogin(accessToken.getToken(),7);
        }
        if (accessToken == null || accessToken.isExpired()) {
            LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList("public_profile", "user_friends"));
        }
    }

    @Override
    public void registerByPhone(String phone, String verificationCode) {
        AppLogger.d("just send phone ");
    }

    @Override
    public void getCodeByPhone(final String phone) {
        Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        JfgCmdInsurance.getCmd().sendCheckCode(phone,
                                JfgEnum.JFG_SMS_REGISTER);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("" + throwable.getLocalizedMessage());
                    }
                });
    }

    @Override
    public void verifyCode(final String phone, final String code, final String token) {
        Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        try {
                            JfgCmdInsurance.getCmd().verifySMS(phone, code, token);
                            isRegSms = true;
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.i("throw:" + throwable.getLocalizedMessage());
                    }
                });
    }

    @Override
    public SsoHandler getSinaCallBack() {
        if (sinaUtil == null) {
            return null;
        } else {
            return sinaUtil.mSsoHandler;
        }
    }

    /**
     * QQ登录在OnActivity中的回调
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResultData(int requestCode, int resultCode, Intent data) {
        Tencent.onActivityResultData(requestCode, resultCode, data, qqAuthrizeListener);
    }

    @Override
    public TwitterAuthClient getTwitterBack() {
        if (twitterAuthClient == null){
            return null;
        }
        return twitterAuthClient;
    }

    /**
     * 新浪微博的授权
     */
    private class SinaAuthorizeListener implements WeiboAuthListener {
        @Override
        public void onComplete(Bundle values) {
            Oauth2AccessToken accessToken = Oauth2AccessToken.parseAccessToken(values);
            UsersAPI usersAPI = new UsersAPI(accessToken,getView().getContext());
            Long uid = Long.parseLong(accessToken.getUid());
            usersAPI.show(uid, sinaRequestListener);
            if (accessToken != null && accessToken.isSessionValid()) {
                executeOpenLogin(accessToken.getToken(), 4);
                AccessTokenKeeper.writeAccessToken(getView().getContext(), accessToken);
            } else {
                String code = values.getString("code", "");
                AppLogger.d("sina_code" + code);
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            if (getView() != null) {
                getView().onSinaAuthorizeResult(LoginContract.AUTHORIZE_ERROR);
            }
            AppLogger.e(e.toString());
        }

        @Override
        public void onCancel() {
            if (getView() != null) {
                getView().onSinaAuthorizeResult(LoginContract.AUTHORIZE_CANCLE);
            }
        }
    }

    /**
     * 新浪权限检查的监听器
     */
    private RequestListener sinaRequestListener = new RequestListener() {
        @Override
        public void onComplete(String response) {
            try {
                if (!TextUtils.isEmpty(response)) {
                    String strId = new JSONObject(response).getString("idstr");
                    String profile_image_url = new JSONObject(response).getString("profile_image_url");
                    String userAlias = new JSONObject(response).getString("screen_name");
                    PreferencesUtils.putString(JConstant.OPEN_LOGIN_USER_ICON, profile_image_url);
                    PreferencesUtils.putString(JConstant.OPEN_LOGIN_USER_ALIAS, userAlias);
                }
            } catch (JSONException e) {
                AppLogger.e(e.toString());
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            if (getView() != null) {
                getView().onSinaAuthorizeResult(LoginContract.AUTHORIZE_CANCLE);
            }
            AppLogger.e(e.toString());
        }
    };

    /**
     * QQ授权回调
     */
    private class QQAuthrizeListener implements IUiListener {

        @Override
        public void onComplete(Object response) {
            if (null == response) {
                if (getView() != null) {
                    getView().onQQAuthorizeResult(LoginContract.AUTHORIZE_ERROR);
                }
                return;
            }
            JSONObject jsonResponse = (JSONObject) response;
            if (null != jsonResponse && jsonResponse.length() == 0) {
                ToastUtil.showNegativeToast("授权失败");
                return;
            }
            doComplete(jsonResponse);
        }

        @Override
        public void onError(UiError uiError) {
            if (getView() != null) {
                getView().onQQAuthorizeResult(LoginContract.AUTHORIZE_ERROR);
            }
        }

        @Override
        public void onCancel() {
            if (getView() != null) {
                getView().onQQAuthorizeResult(LoginContract.AUTHORIZE_CANCLE);
            }
        }
    }

    /**
     * QQ登录回调解析token
     *
     * @param response
     */
    private void doComplete(JSONObject response) {
        try {
            if (response.getInt("ret") == 0) {
                String openID = response.getString("openid");
                String accessToken = response.getString("access_token");
                String expires = response.getString("expires_in");
                tencentInstance.mTencent.setOpenId(openID);
                tencentInstance.mTencent.setAccessToken(accessToken, expires);
                getuserInfo();
                //执行登录
                executeOpenLogin(accessToken, 3);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getuserInfo() {
        UserInfo qqInfo = new UserInfo(getView().getContext(), tencentInstance.mTencent.getQQToken());
        qqInfo.getUserInfo(getQQinfoListener);
    }

    /**
     * 获取用户信息
     */
    private IUiListener getQQinfoListener = new IUiListener() {
        @Override
        public void onComplete(Object response) {
            try {
                JSONObject jsonObject = (JSONObject) response;
                String nickname = jsonObject.getString("nickname");
                String figureurl = jsonObject.getString("figureurl");
                PreferencesUtils.putString(JConstant.OPEN_LOGIN_USER_ICON, figureurl);
                PreferencesUtils.putString(JConstant.OPEN_LOGIN_USER_ALIAS, nickname);
                //处理自己需要的信息
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(UiError uiError) {

        }

        @Override
        public void onCancel() {

        }
    };


    @Override
    public void checkAccountIsReg(String account) {
        rx.Observable.just(account)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        try {
                            isReg = true;
                            JfgCmdInsurance.getCmd().checkAccountRegState(s);
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                },throwable -> {
                    AppLogger.e("checkAccountIsReg"+throwable.getLocalizedMessage());
                });
    }

    @Override
    public Subscription checkAccountBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.CheckRegsiterBack.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.CheckRegsiterBack>() {
                    @Override
                    public void call(RxEvent.CheckRegsiterBack checkRegsiterBack) {
                        if (isReg)
                        getView().checkAccountResult(checkRegsiterBack);
                    }
                });
    }

    /**
     * 登录计时
     */
    @Override
    public void loginCountTime() {
        rx.Observable.just(null)
                .delay(30000,TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o ->{
                    if (getView() != null && !isLoginSucc)
                        getView().loginResult(JError.ErrorConnect);
                });
    }

    @Override
    public String getTempAccPwd() {
        String decrypt = "";
        String dataFromFile = FileUtils.getDataFromFile(getView().getContext());
        if (TextUtils.isEmpty(dataFromFile)){
            return "";
        }
        try {
            decrypt = AESUtil.decrypt(dataFromFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decrypt;
    }

    /**
     * FaceBook回调结果
     */
    @Override
    public void fackBookCallBack() {
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();
                executeOpenLogin(accessToken.getToken(),7);
                GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        //获取登录成功之后的用户详细信息
                        String facebook_id = object.optString("id");
                        String facebook_name = object.optString("name");
                        String picture = object.optString("picture");
                        String imageUrl = null;
                        try {
                            JSONObject jsonObject = new JSONObject(picture);
                            String data = jsonObject.getString("data");
                            imageUrl = new JSONObject(data).getString("url");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // 保存用户信息
                        PreferencesUtils.putString(JConstant.OPEN_LOGIN_USER_ICON, imageUrl);
                        PreferencesUtils.putString(JConstant.OPEN_LOGIN_USER_ALIAS, facebook_name);
                    }
                });

                Bundle parameters = new Bundle();
                parameters.putString("fields", DEFAULT_REQUEST_VALUE);
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                ToastUtil.showToast("facebook 授权取消");
            }

            @Override
            public void onError(FacebookException error) {
                ToastUtil.showToast(error.toString());
            }
        });
    }

    @Override
    public CallbackManager getFaceBookBackObj() {
        if (callbackManager != null){
            return callbackManager;
        }else {
            return null;
        }
    }

}
