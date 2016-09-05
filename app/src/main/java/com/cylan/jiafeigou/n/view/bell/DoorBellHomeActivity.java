package com.cylan.jiafeigou.n.view.bell;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.misc.SpacesItemDecoration;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.engine.SimpleHelperIntentService;
import com.cylan.jiafeigou.n.mvp.contract.ActivityResultContract;
import com.cylan.jiafeigou.n.mvp.contract.bell.DoorBellHomeContract;
import com.cylan.jiafeigou.n.mvp.impl.ActivityResultPresenterImpl;
import com.cylan.jiafeigou.n.mvp.impl.bell.BellSettingPresenterImpl;
import com.cylan.jiafeigou.n.mvp.impl.bell.DBellHomePresenterImpl;
import com.cylan.jiafeigou.n.mvp.model.BellCallRecordBean;
import com.cylan.jiafeigou.n.view.adapter.BellCallRecordListAdapter;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.jiafeigou.utils.AppLogger;
import com.cylan.jiafeigou.utils.ViewUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DoorBellHomeActivity extends BaseFullScreenFragmentActivity
        implements DoorBellHomeContract.View,
        BellCallRecordListAdapter.SimpleLongClickListener,
        BellCallRecordListAdapter.SimpleClickListener,
        ActivityResultContract.View {

    private static final String tag = "DoorBellHomeActivity";
    @BindView(R.id.tv_top_bar_left)
    TextView imgVTopBarCenter;
    @BindView(R.id.fLayout_top_bar_container)
    FrameLayout fLayoutTopBarContainer;
    @BindView(R.id.rv_bell_list)
    RecyclerView rvBellList;
    @BindView(R.id.fLayout_bell_list_container)
    FrameLayout fLayoutBellListContainer;
    @BindView(R.id.tv_bell_home_list_cancel)
    TextView tvBellHomeListCancel;
    @BindView(R.id.tv_bell_home_list_select_all)
    TextView tvBellHomeListSelectAll;
    @BindView(R.id.tv_bell_home_list_delete)
    TextView tvBellHomeListDelete;
    @BindView(R.id.fLayout_bell_home_list_edition)
    FrameLayout fLayoutBellHomeListEdition;
    private DoorBellHomeContract.Presenter presenter;
    private ActivityResultContract.Presenter activityResultPresenter;
    private WeakReference<BellSettingFragment> fragmentWeakReference;
    private WeakReference<LBatteryWarnDialog> lBatteryWarnDialog;
    private BellCallRecordListAdapter bellCallRecordListAdapter;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_door_bell);
//        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        ButterKnife.bind(this);
        initAdapter();
        initToolbar();
        initSomething();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (presenter == null)
            presenter = new DBellHomePresenterImpl(this);
        presenter.start();
        startService(new Intent(getApplicationContext(), SimpleHelperIntentService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (presenter != null)
            presenter.stop();
        if (lBatteryWarnDialog != null
                && lBatteryWarnDialog.get() != null
                && lBatteryWarnDialog.get().isResumed())
            lBatteryWarnDialog.get().dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (activityResultPresenter != null)
            activityResultPresenter.stop();
    }

    private void initAdapter() {
        bellCallRecordListAdapter = new BellCallRecordListAdapter(getApplicationContext(),
                null, R.layout.layout_bell_call_list_item);
        bellCallRecordListAdapter.setSimpleClickListener(this);
        bellCallRecordListAdapter.setSimpleLongClickListener(this);
        rvBellList.setAdapter(bellCallRecordListAdapter);
        rvBellList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvBellList.addItemDecoration(new SpacesItemDecoration(new Rect(ViewUtils.dp2px(10), ViewUtils.dp2px(15), 0, 0)));
    }

    private void initSomething() {
        if (activityResultPresenter == null)
            activityResultPresenter = new ActivityResultPresenterImpl(this);
        activityResultPresenter.start();
    }

    private void initToolbar() {
        imgVTopBarCenter.setText("");
        ViewUtils.setViewMarginStatusBar(fLayoutTopBarContainer);
    }

    @OnClick({R.id.tv_top_bar_left, R.id.imgv_toolbar_right,
            R.id.btn_start_calling})
    public void onElementClick(View v) {
        switch (v.getId()) {
            case R.id.imgv_toolbar_right:
                ViewUtils.deBounceClick(v);
                initSettingFragment();
                BellSettingFragment fragment = fragmentWeakReference.get();
                new BellSettingPresenterImpl(fragment);
                getSupportFragmentManager().beginTransaction()
                        //如果需要动画，可以把动画添加进来
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                                , R.anim.slide_in_left, R.anim.slide_out_right)
                        .add(android.R.id.content, fragment, fragment.getClass().getSimpleName())
                        .addToBackStack(fragment.getClass().getSimpleName())
                        .commit();
                break;
            case R.id.tv_top_bar_left:
                onBackPressed();
                break;
            case R.id.btn_start_calling:
                Intent intent = new Intent(this, BellLiveActivity.class);
                intent.putExtra("text", "nihao");
                startActivity(intent);
                break;
        }
    }

    private void initSettingFragment() {
        if (fragmentWeakReference == null || fragmentWeakReference.get() == null) {
            fragmentWeakReference = new WeakReference<>(BellSettingFragment.newInstance(new Bundle()));
        }
        Bundle bundle = getIntent().getBundleExtra(JConstant.KEY_DEVICE_ITEM_BUNDLE);
        if (bundle != null) {
            fragmentWeakReference.get().setArguments(bundle);
        } else {
            AppLogger.d("item bundle not found");
        }
    }

    @Override
    public void onBackPressed() {
        if (checkExtraChildFragment()) {
            return;
        } else if (checkExtraFragment())
            return;
        if (reverseEditionMode())
            return;
        finishExt();
    }

    /**
     * 反转编辑模式
     *
     * @return
     */
    private boolean reverseEditionMode() {
        if (bellCallRecordListAdapter.getMode() == 1) {
            bellCallRecordListAdapter.setMode(0);
            final int lPos = ((LinearLayoutManager) rvBellList.getLayoutManager())
                    .findLastVisibleItemPosition();
            bellCallRecordListAdapter.reverseEdition(true, lPos);
            showEditBar(false);
            return true;
        }
        return false;
    }

    @Override
    public void onLoginState(int state) {
    }


    @Override
    public void onBellBatteryDrainOut() {
        initBatteryDialog();
        LBatteryWarnDialog dialog = lBatteryWarnDialog.get();
        if (dialog.isResumed())
            return;
        dialog.show(getSupportFragmentManager(), "lBattery");
    }

    private void initBatteryDialog() {
        if (lBatteryWarnDialog == null || lBatteryWarnDialog.get() == null)
            lBatteryWarnDialog = new WeakReference<>(LBatteryWarnDialog.newInstance(null));
    }

    @Override
    public void onRecordsListRsp(ArrayList<BellCallRecordBean> beanArrayList) {
        bellCallRecordListAdapter.addAll(beanArrayList);
    }

    @Override
    public void onActivityResult(RxEvent.ActivityResult result) {
        if (result == null || result.bundle == null)
            return;
        if (result.bundle.containsKey(JConstant.KEY_ACTIVITY_RESULT_CODE)) {
            final int resultCode = result.bundle.getInt(JConstant.KEY_ACTIVITY_RESULT_CODE);
            switch (resultCode) {
                case JConstant.RESULT_CODE_REMOVE_ITEM:
//                    activityResultPresenter.setActivityResult(result);
//                    popAllFragmentStack();
                    finishExt();
                    break;
            }
        }
    }

    @Override
    public void setPresenter(DoorBellHomeContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    public boolean onLongClick(View v) {
        Object o = v.getTag();
        if (o == null || !(o instanceof Integer)) {
            AppLogger.d("v tag is null");
            return false;
        }
        final int position = (int) v.getTag();
        if (position < 0 || position >= bellCallRecordListAdapter.getCount()) {
            AppLogger.d("position is invalid");
            return false;
        }
        //toggle edit mode
        if (bellCallRecordListAdapter.getMode() == 0) {
            AppLogger.d("enter edition mode");
            bellCallRecordListAdapter.setMode(1);
            reverseItemSelectedState(position);
            showEditBar(true);
        }
        return true;
    }

    private void showEditBar(boolean show) {
        AnimatorUtils.slide(fLayoutBellHomeListEdition);
    }

    @Override
    public void onClick(View v) {
        Object o = v.getTag();
        if (o == null || !(o instanceof Integer)) {
            AppLogger.d("v tag is null");
            return;
        }
        final int position = (int) v.getTag();
        if (position < 0 || position >= bellCallRecordListAdapter.getCount()) {
            AppLogger.d("position is invalid");
            return;
        }
        //
        if (bellCallRecordListAdapter.getMode() == 0) {
            AppLogger.d("normal mode");
            return;
        }
        reverseItemSelectedState(position);
    }

    private void reverseItemSelectedState(int position) {
        BellCallRecordBean bean = bellCallRecordListAdapter.getItem(position);
        if (bean == null) {
            AppLogger.d("bean is null");
            return;
        }
        bean.selected = !bean.selected;
        bellCallRecordListAdapter.notifyItemChanged(position);
    }

    @OnClick({R.id.tv_bell_home_list_cancel, R.id.tv_bell_home_list_select_all, R.id.tv_bell_home_list_delete})
    public void onEditBarClick(View view) {
        final int lPos = ((LinearLayoutManager) rvBellList.getLayoutManager())
                .findLastVisibleItemPosition();
        switch (view.getId()) {
            case R.id.tv_bell_home_list_cancel:
                bellCallRecordListAdapter.reverseEdition(true, lPos);
                bellCallRecordListAdapter.setMode(0);
                showEditBar(false);
                break;
            case R.id.tv_bell_home_list_select_all:
                bellCallRecordListAdapter.selectAll(lPos);
                break;
            case R.id.tv_bell_home_list_delete:
                break;
        }
    }
}