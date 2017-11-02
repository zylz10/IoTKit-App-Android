package com.cylan.jiafeigou.base.wrapper;

import android.graphics.Bitmap;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cylan.jiafeigou.base.view.CallablePresenter;
import com.cylan.jiafeigou.base.view.CallableView;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.push.BellPuller;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;


/**
 * Created by yzd on 16-12-30.
 */

public abstract class BaseCallablePresenter<V extends CallableView> extends BaseViewablePresenter<V> implements CallablePresenter {
    protected Caller mCaller;
    protected Caller mHolderCaller;
    protected boolean mIsInViewerMode = false;

    public BaseCallablePresenter(V view) {
        super(view);
    }


    @Override
    protected String getViewHandler() {
        return mCaller == null ? null : mCaller.caller;
    }

    @Override
    public void pickup() {
        AppLogger.d("正在接听");
        if (mHolderCaller != null) {
            mCaller = mHolderCaller;
            mHolderCaller = null;
            startViewer();
        }
    }

    @Override
    protected void setViewHandler(String handler) {
        if (handler == null && mCaller != null) {
            mCaller = null;
        }
    }


    @Override
    public void newCall(Caller caller) {
        //直播中的门铃呼叫
//                                                mView.onNewCallWhenInLive(mHolderCaller.caller);
//说明不是自己接听的
        Subscription subscribe = RxBus.getCacheInstance().toObservable(RxEvent.CallResponse.class)
                        .mergeWith(
                                Observable.just(mHolderCaller = caller)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .filter(who -> !mIsInViewerMode)
                                        .flatMap(who -> {
//                                    switch (mView.onResolveViewLaunchType()) {
//                                        case JConstant.VIEW_CALL_WAY_LISTEN:
                                            if (mCaller != null && mHolderCaller != null) {//直播中的门铃呼叫
//                                                mView.onNewCallWhenInLive(mHolderCaller.caller);
                                            } else if (mHolderCaller != null) {
                                                mView.onListen();
                                                AppLogger.d("收到门铃呼叫");
                                            }
//                                            break;
//                                        case JConstant.VIEW_CALL_WAY_VIEWER:
//                                            mCaller = mHolderCaller;
//                                            mHolderCaller = null;
//                                            startViewer();
//                                            break;
//                                    }
                                            return RxBus.getCacheInstance().toObservable(RxEvent.CallResponse.class);
                                        })
                        )
                .first()
                .timeout(JFGRules.getCallTimeOut(sourceManager.getDevice(uuid)), TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(answer -> {
                    if (!answer.self) {//说明不是自己接听的
                        AppLogger.d("门铃在其他端接听了");
                        if (mCaller == null) {
                            mView.onCallAnswerInOther();
                            mView.onDismiss();
                        } else {
                            mHolderCaller = null;
                            mView.onCallAnswerInOther();
                        }
                    }
                }, e -> {
                    if (e instanceof TimeoutException) {
                        mHolderCaller = null;
                        AppLogger.w("门铃呼叫超时了!!!");
                        mView.onNewCallTimeOut();
                    }
                    AppLogger.e(e.getMessage());
                });
        addDestroySubscription(subscribe);
    }

    @Override
    public void stop() {
        super.stop();
        mIsInViewerMode = false;
    }

    @Override
    public void loadPreview(String url) {
        Subscription subscribe =  load(BellPuller.getInstance().getUrl(uuid))
                .subscribe(ret -> {
                }, AppLogger::e);
        addDestroySubscription(subscribe);
    }

    protected Observable<Long> load(String url) {
        return Observable.interval(2, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map(s -> {
                    preload(url);
                    return s;
                })
                .takeUntil(RxBus.getCacheInstance().toObservable(Notify.class).first()
                        .observeOn(AndroidSchedulers.mainThread())
                        .filter(ret -> ret != null)
                        .map(notify -> {
                            if (notify.success) {
                                AppLogger.d("正在显示门铃截图");
                                if (mView != null) {
                                    mView.onShowVideoPreviewPicture(url);
                                }
                            }
                            return notify;
                        })

                );
    }

    private void preload(String url) {
        if (mView != null && url != null) {
            Glide.with(mView.activity()).load(url)
                    .asBitmap()
                    .listener(new RequestListener<String, Bitmap>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            RxBus.getCacheInstance().post(new Notify(true));
                            // TODO: 2017/8/29 门铃截图也需要保存起来
                            saveBitmap(resource);
                            return false;
                        }
                    })
                    .preload();
        }
    }

    public static class Notify {
        public boolean success;

        public Notify(boolean success) {
            this.success = success;
        }
    }
}
