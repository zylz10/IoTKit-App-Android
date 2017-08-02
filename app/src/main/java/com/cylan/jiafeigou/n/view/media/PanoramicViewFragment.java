package com.cylan.jiafeigou.n.view.media;


import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.StringSignature;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.model.CamMessageBean;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.DensityUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.widget.video.PanoramicView360_Ext;
import com.cylan.jiafeigou.widget.video.VideoViewFactory;
import com.cylan.panorama.CameraParam;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.misc.JConstant.KEY_SHARED_ELEMENT_LIST;
import static com.cylan.jiafeigou.n.view.media.CamMediaActivity.KEY_INDEX;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PanoramicViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PanoramicViewFragment extends IBaseFragment {


    @BindView(R.id.fLayout_panoramic_container)
    FrameLayout mPanoramicContainer;
    private String uuid;
    private PanoramicView360_Ext panoramicView;
    private CamMessageBean camMessageBean;
    private Subscription subscription;

    public PanoramicViewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create activity_cloud_live_mesg_call_out_item new instance of
     * this fragment using the provided parameters.
     *
     * @param bundle Parameter 1.
     * @return A new instance of fragment NormalMediaFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PanoramicViewFragment newInstance(Bundle bundle) {
        PanoramicViewFragment fragment = new PanoramicViewFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup lLayoutPreview,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_panoramic_view, lLayoutPreview, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        final int screenWidth = DensityUtils.getScreenWidth();
        ViewGroup.LayoutParams lp = mPanoramicContainer.getLayoutParams();
        lp.height = screenWidth;
        mPanoramicContainer.setLayoutParams(lp);
        this.uuid = getArguments().getString(JConstant.KEY_DEVICE_ITEM_UUID);
        camMessageBean = getArguments().getParcelable(KEY_SHARED_ELEMENT_LIST);
        if (getUserVisibleHint()) {//当前页面才显示
            loadBitmap(getArguments().getInt(KEY_INDEX, 0));
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (subscription != null) subscription.unsubscribe();
        try {
            if (panoramicView != null) {
                panoramicView.onDestroy();
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        showLoading(false);
    }

    private void showLoading(boolean show) {
//        if (getView() != null) {
//            getView().post(() -> {
//                if (show)
//                    LoadingDialog.showLoading(getFragmentManager(), "", true);
//                else LoadingDialog.dismissLoading(getFragmentManager());
//            });
//        }
    }

    private Target target;
    private int lastLoadIndex;

    public void loadBitmap(int index, String mode) {
        lastLoadIndex = index;
        Log.d("panoramicView", "null? " + (panoramicView == null) + " " + (getContext() == null));
        if (panoramicView == null) {
            panoramicView = new PanoramicView360_Ext(getContext());
            panoramicView.setInterActListener(new VideoViewFactory.InterActListener() {
                @Override
                public boolean onSingleTap(float x, float y) {
                    if (callBack != null) {
                        callBack.callBack(null);
//                        callBack = null;
                    }
                    return true;
                }

                @Override
                public void onSnapshot(Bitmap bitmap, boolean tag) {

                }
            });
            panoramicView.setMode(TextUtils.equals(mode, "0") ? 0 : 1);
            panoramicView.config360(TextUtils.equals(mode, "0") ? CameraParam.getTopPreset() : CameraParam.getWallPreset());
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mPanoramicContainer.addView(panoramicView, layoutParams);
        }
        try {
            if (target != null)
                Glide.clear(target);
        } catch (Exception e) {

        }
        //填满
        Glide.with(ContextUtils.getContext())
                .load(MiscUtils.getCamWarnUrl(uuid, camMessageBean, index + 1))
                .asBitmap()
                //解决黑屏问题
                .signature(new StringSignature(System.currentTimeMillis() + ""))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onLoadStarted(Drawable placeholder) {
                        showLoading(true);
                    }

                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                        try {
                            if (resource != null && !resource.isRecycled())
                                panoramicView.loadImage(resource);
                            else {
                                AppLogger.e("bitmap is recycled");
                            }
                        } catch (Exception e) {
                            AppLogger.e("pan view is out date,");
                        }
                        showLoading(false);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        AppLogger.e(e);
                        loadBitmap(lastLoadIndex);
                        showLoading(false);
                    }
                });
    }

    public void loadBitmap(int index) {
        String mode = camMessageBean.alarmMsg == null ? "0" : camMessageBean.alarmMsg.tly;
        Log.d("loadBitmap", "loadBitmap: " + mode);
        if (subscription != null) subscription.unsubscribe();
        subscription = Observable.just(mode)
                .subscribeOn(Schedulers.io())
                .delay(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    loadBitmap(index, mode);
                }, AppLogger::e);
    }
}
