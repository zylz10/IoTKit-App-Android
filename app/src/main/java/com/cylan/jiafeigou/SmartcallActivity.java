package com.cylan.jiafeigou;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.contract.splash.SplashContract;
import com.cylan.jiafeigou.n.mvp.impl.splash.SplashPresenterImpl;
import com.cylan.jiafeigou.n.view.activity.NeedLoginActivity;
import com.cylan.jiafeigou.n.view.splash.BeforeLoginFragment;
import com.cylan.jiafeigou.n.view.splash.GuideFragment;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.IMEUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.UiHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

/**
 * Created by chen on 5/24/16.
 */
@RuntimePermissions
public class SmartcallActivity extends NeedLoginActivity
        implements SplashContract.View {

    @BindView(R.id.fLayout_splash)
    FrameLayout fLayoutSplash;
    @Nullable
    private SplashContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IMEUtils.fixFocusedViewLeak(getApplication());
        setContentView(R.layout.activity_welcome_page);
        ButterKnife.bind(this);
        initPresenter();
        if (presenter != null) presenter.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        SmartcallActivityPermissionsDispatcher.showWriteSdCardWithCheck(this);
        SmartcallActivityPermissionsDispatcher.showReadPhoneStateWithCheck(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (presenter != null) presenter.stop();
    }

    protected int[] getOverridePendingTransition() {
        return new int[]{R.anim.alpha_in, R.anim.alpha_out};
    }

    private void initPresenter() {
        presenter = new SplashPresenterImpl(this);
    }

    /**
     * 引导页
     */
    private void initGuidePage() {
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, GuideFragment.newInstance())
                .commit();
    }

    /**
     * pre-登陆
     */
    private void initLoginPage() {
        if (isLoginIn()) {
            //进去主页 home page
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                startActivity(new Intent(this, NewHomeActivity.class),
                        ActivityOptionsCompat.makeCustomAnimation(this, R.anim.alpha_in, R.anim.alpha_out).toBundle());
            } else {
                startActivity(new Intent(this, NewHomeActivity.class));
            }
            finish();
        } else {
            //进入登陆页 login page
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, BeforeLoginFragment.newInstance(null))
                    .commitAllowingStateLoss();
        }
    }

    @Override
    public void splashOver() {
        //do your business;
        if (isFirstUseApp()) {
            //第一次打开app
            setFirstUseApp();
            initGuidePage();
        } else {
            initLoginPage();
        }
    }

    @Override
    public void finishDelayed() {
//        StateMaintainer.getAppManager().finishAllActivity();
    }


    private boolean isLoginIn() {
        return PreferencesUtils.getBoolean(this, UiHelper.TAG_LOGING_STATUS, false);
    }

    /**
     * check is the app is fresh
     *
     * @return
     */
    private boolean isFirstUseApp() {
        return PreferencesUtils.getBoolean(this, JConstant.KEY_FRESH, true);
        // return true;
    }

    private void setFirstUseApp() {
        PreferencesUtils.putBoolean(this, JConstant.KEY_FRESH, false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        SmartcallActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public void onWriteSdCardDenied() {
        // NOTE: Perform action that requires the permission.
        // If this is run by PermissionsDispatcher, the permission will have been granted
        Toast.makeText(this, "请你开启SD卡读写权限,应用才能正常工作", Toast.LENGTH_SHORT).show();
        if (presenter != null) presenter.finishAppDelay();
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public void showWriteSdCard() {
        AppLogger.d(JConstant.LOG_TAG.PERMISSION + "show");
    }


    @OnPermissionDenied(Manifest.permission.CAMERA)
    public void onCameraDenied() {
        // NOTE: Deal with a denied permission, e.g. by showing specific UI
        // or disabling certain functionality
        Toast.makeText(this, R.string.permission_camera_denied, Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    public void onCameraNeverAskAgain() {
        Toast.makeText(this, R.string.permission_camera_never_askagain, Toast.LENGTH_SHORT).show();
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    public void showCamera() {
        // NOTE: Perform action that requires the permission. If this is run by PermissionCheckerUitls, the permission will have been granted
        //do you business
    }

    @OnShowRationale(Manifest.permission.CAMERA)
    public void showRationaleForCamera(PermissionRequest request) {
        // NOTE: Show a rationale to explain why the permission is needed, e.g. with a dialog.
        // Call proceed() or cancel() on the provided PermissionRequest to continue or abort
        showRationaleDialog(R.string.permission_camera_rationale, request);
    }

    @NeedsPermission(Manifest.permission.READ_PHONE_STATE)
    public void showReadPhoneState() {

    }

    private void showRationaleDialog(@StringRes int messageResId, final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setPositiveButton(R.string.button_allow, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton(R.string.button_deny, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .setCancelable(false)
                .setMessage(messageResId)
                .show();
    }

    @Override
    public void setPresenter(SplashContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

}
