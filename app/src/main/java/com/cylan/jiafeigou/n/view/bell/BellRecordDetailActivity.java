package com.cylan.jiafeigou.n.view.bell;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.cylan.entity.JfgEnum;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGMsgHttpResult;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.BaseFullScreenActivity;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.module.JFGDPDevice;
import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.model.BellCallRecordBean;
import com.cylan.jiafeigou.n.view.home.ShareDialogFragment;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.Security;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.photoview.PhotoView;
import com.cylan.jiafeigou.support.photoview.PhotoViewAttacher;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.FileUtils;
import com.cylan.jiafeigou.utils.JFGGlideURL;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.misc.JfgCmdInsurance.getCmd;

public class BellRecordDetailActivity extends BaseFullScreenActivity {

    private static final int REQ_DOWNLOAD = 20000;
    @BindView(R.id.act_bell_header_back)
    ImageView mBack;
    @BindView(R.id.act_bell_header_title)
    TextView mTitle;
    @BindView(R.id.act_bell_detail_picture)
    PhotoView mPictureDetail;
    @BindView(R.id.act_bell_picture_opt_download)
    ImageView mDownload;
    @BindView(R.id.act_bell_picture_opt_share)
    ImageView mShare;
    @BindView(R.id.act_bell_picture_opt_collection)
    ImageView mCollect;
    @BindView(R.id.act_bell_pic_option)
    FrameLayout mBellContainer;
    @BindView(R.id.activity_bell_record_detail)
    FrameLayout mBellDetail;
    @BindView(R.id.act_bell_header_container)
    RelativeLayout mHeadContainer;
    private BellCallRecordBean mCallRecord;
    private ShareDialogFragment mShareDialog;
    private File mDownloadFile;

    @Override
    protected JFGPresenter onCreatePresenter() {
        return new BasePresenter() {
        };
    }

    @Override
    protected int getContentViewID() {
        return R.layout.activity_bell_record_detail;
    }

    @Override
    protected void initViewAndListener() {
        super.initViewAndListener();
        ViewUtils.setViewPaddingStatusBar(mHeadContainer);
        mBellDetail.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        mCallRecord = getIntent().getParcelableExtra(JConstant.KEY_DEVICE_ITEM_BUNDLE);
        mTitle.setText(TimeUtils.getMediaVideoTimeInString(mCallRecord.timeInLong));
        mPictureDetail.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                slide();
            }

            @Override
            public void onOutsidePhotoTap() {
                slide();
            }
        });

        Glide.with(this)
                .load(new JFGGlideURL(JfgEnum.JFG_URL.WARNING, mCallRecord.type, mCallRecord.timeInLong / 1000 + ".jpg", mUUID))
                .listener(new RequestListener<JFGGlideURL, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, JFGGlideURL model, Target<GlideDrawable> target, boolean isFirstResource) {
                        ToastUtil.showNegativeToast("图片加载失败");
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, JFGGlideURL model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(mPictureDetail);
    }


    private void slide() {
        if (mHeadContainer.isShown()) {
            AnimatorUtils.slideOut(mHeadContainer, true);
            AnimatorUtils.slideOut(mBellContainer, false);
        } else {
            AnimatorUtils.slideIn(mHeadContainer, true);
            AnimatorUtils.slideIn(mBellContainer, false);
        }
    }


    @OnClick(R.id.act_bell_picture_opt_download)
    public void download() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            downloadFile();//已经获得了授权
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //需要重新提示用户授权
            ToastUtil.showNegativeToast(getString(R.string.DOWNLOAD_NEED_PERMISSION));
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_DOWNLOAD);
        }
    }

    @OnClick(R.id.act_bell_picture_opt_share)
    public void share() {
        if (mShareDialog == null) {
            mShareDialog = ShareDialogFragment.newInstance();
        }
        mShareDialog.setPictureURL(new JFGGlideURL(JfgEnum.JFG_URL.WARNING, mCallRecord.type, mCallRecord.timeInLong / 1000 + ".jpg", mUUID));
        mShareDialog.show(getSupportFragmentManager(), ShareDialogFragment.class.getName());
    }

    @OnClick(R.id.act_bell_picture_opt_collection)
    public void collection() {
        Observable.create((Observable.OnSubscribe<Integer>) subscriber -> {
            //先设置 robotData
            long result = -1;
            try {
                DpMsgDefine.DPWonderItem item = new DpMsgDefine.DPWonderItem();
                item.msgType = DpMsgDefine.DPWonderItem.TYPE_PIC;
                item.cid = mUUID;
                JFGDPDevice device = DataSourceManager.getInstance().getJFGDevice(mUUID);
                item.place = TextUtils.isEmpty(device.alias) ? device.uuid : device.alias;
                item.fileName = mCallRecord.timeInLong / 1000 + "_1.jpg";
                item.time = (int) (mCallRecord.timeInLong / 1000);
                ArrayList<JFGDPMsg> req = new ArrayList<>(1);
                JFGDPMsg msg = new JFGDPMsg(DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG, mCallRecord.timeInLong);
                msg.packValue = item.toBytes();
                req.add(msg);
                result = JfgCmdInsurance.getCmd().robotSetData(mUUID, req);
                AppLogger.e(result + "");
                subscriber.onNext((int) result);
                subscriber.onCompleted();
                AppLogger.d("正在设置 robotData:");
            } catch (Exception e) {
                e.printStackTrace();
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io())
                .zipWith(RxBus.getCacheInstance().toObservable(RxEvent.SetDataRsp.class), (aLong, setDataRsp) -> {
                    AppLogger.d("正在解析 robotSetData的返回结果");
                    if (aLong == (int) setDataRsp.seq) {
                        int code = setDataRsp.rets.get(0).ret;
                        AppLogger.d("setRobotDataResponse" + code);
                        if (code == 0) {//判断返回结果,不为零说明出现异常
                            return true;
                        } else {
                            throw new RxEvent.ErrorRsp(code);
                        }
                    }
                    return false;
                })
                .filter(s -> s)
                .map(code -> {
                    long result = -1;
                    try {
                        String remotePath = "/long/" +
                                Security.getVId(JFGRules.getTrimPackageName()) +
                                "/" +
                                mUUID +
                                "/wonder/" +
                                mCallRecord.timeInLong / 1000 +
                                "_1.jpg";

                        FutureTarget<File> future = Glide.with(ContextUtils.getContext())
                                .load(new JFGGlideURL(JfgEnum.JFG_URL.WARNING, mCallRecord.type, mCallRecord.timeInLong / 1000 + ".jpg", mUUID))
                                .downloadOnly(100, 100);
                        result = getCmd().putFileToCloud(remotePath, future.get().getAbsolutePath());
                        AppLogger.d("正在设置 CloudFile");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return result;
                })
                .zipWith(RxBus.getCacheInstance().toObservable(JFGMsgHttpResult.class), (aLong, jfgMsgHttpResult) -> {
                    AppLogger.e(" 正在解析 http请求返回的结果:" + aLong + new Gson().toJson(jfgMsgHttpResult));
                    return aLong == jfgMsgHttpResult.requestId && jfgMsgHttpResult.ret == 200;
                })
                .filter(s -> s)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> {
                    ToastUtil.showPositiveToast("收藏成功!");
                    AppLogger.d("收藏成功!");
                }, e -> {
                    if (e instanceof RxEvent.ErrorRsp) {
                        RxEvent.ErrorRsp rsp = (RxEvent.ErrorRsp) e;
                        switch (rsp.code) {
                            case 1050://收藏达到上限
                                ToastUtil.showNegativeToast("已达到收藏上限!");
                                break;
                        }
                    }

                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_DOWNLOAD) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadFile();
            } else {
                Toast.makeText(this, getString(R.string.permission_download), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void downloadFile() {
        mDownloadFile = new File(JConstant.MEDIA_DETAIL_PICTURE_DOWNLOAD_DIR, mCallRecord.timeInLong / 1000 + ".jpg");

        if (mDownloadFile.exists()) {
            ToastUtil.showPositiveToast(getString(R.string.FILE_DOWNLOADED));
            return;
        }
        Glide.with(this).load(new JFGGlideURL(JfgEnum.JFG_URL.WARNING, mCallRecord.type, mCallRecord.timeInLong / 1000 + ".jpg", mUUID)).
                downloadOnly(new SimpleTarget<File>() {
                    @Override
                    public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
                        ToastUtil.showPositiveToast(getString(R.string.DOWNLOAD_COMPLETED));
                        FileUtils.copyFile(resource, mDownloadFile);
                        mDownloadFile = null;
                    }

                    @Override
                    public void onLoadStarted(Drawable placeholder) {
                        ToastUtil.showPositiveToast(getString(R.string.DOWNLOAD_START));
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        ToastUtil.showNegativeToast(getString(R.string.DOWNLOAD_FAILD));
                        mDownloadFile = null;
                    }
                });
    }

    @Override
    @OnClick(R.id.act_bell_header_back)
    public void onBackPressed() {
        super.onBackPressed();
    }
}
