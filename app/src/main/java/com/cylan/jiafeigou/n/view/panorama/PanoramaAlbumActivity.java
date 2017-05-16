package com.cylan.jiafeigou.n.view.panorama;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.injector.component.ActivityComponent;
import com.cylan.jiafeigou.base.wrapper.BaseActivity;
import com.cylan.jiafeigou.n.view.adapter.PanoramaAdapter;
import com.cylan.jiafeigou.support.superadapter.OnItemClickListener;
import com.cylan.jiafeigou.support.superadapter.OnItemLongClickListener;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.pop.RelativePopupWindow;
import com.cylan.jiafeigou.widget.pop.RoundRectPopup;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

import static com.cylan.jiafeigou.dp.DpMsgMap.ID_202_MAC;

public class PanoramaAlbumActivity extends BaseActivity<PanoramaAlbumContact.Presenter>
        implements PanoramaAlbumContact.View,
        RadioGroup.OnCheckedChangeListener,
        OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener,
        OnItemLongClickListener {
    @BindView(R.id.act_panorama_album_toolbar_container)
    RelativeLayout toolbarContainer;
    @BindView(R.id.act_panorama_album_toolbar_header_title)
    TextView toolbarAlbumViewMode;

    @BindView(R.id.tv_album_delete)
    TextView tvAlbumDelete;

    @BindView(R.id.act_panorama_album_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.pan_empty_list)
    View emptyView;

    @BindView(R.id.act_panorama_album_lists)
    RecyclerView recyclerView;
    private RoundRectPopup albumModeSelectPop;
    @ALBUM_VIEW_MODE
    private int albumViewMode = ALBUM_VIEW_MODE.MODE_BOTH;
    //    private RadioGroup menuContainer;
    private String[] titles = {"相机+手机相册", "全景相册", "手机相册"};
    private PanoramaAdapter panoramaAdapter;
    private LinearLayoutManager layoutManager;
    public boolean loading;

    @Override
    protected void initViewAndListener() {
        super.initViewAndListener();

        panoramaAdapter = new PanoramaAdapter(uuid, this, null);
        panoramaAdapter.setOnItemClickListener(this);
        panoramaAdapter.setOnItemLongClickListener(this);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            public int currentPage;
            public int previousTotal;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = recyclerView.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                if (loading) {

                    if (totalItemCount > previousTotal) {
                        //说明数据已经加载结束
                        previousTotal = totalItemCount;
                    }
                }
                //这里需要好好理解
                if (!loading && totalItemCount - visibleItemCount <= firstVisibleItem) {
                    currentPage++;
                    onLoadMore();
                    loading = true;
                }
            }
        });
        recyclerView.setAdapter(panoramaAdapter);
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    private void onLoadMore() {
        loading = true;
        PanoramaAlbumContact.PanoramaItem item = panoramaAdapter.getItem(panoramaAdapter.getCount() - 1);
        presenter.fetch(item.time, albumViewMode);
    }

    @Override
    protected int getContentViewID() {
        return R.layout.activity_panorama_album;
    }

    @Override
    protected void onStart() {
        super.onStart();
        ViewUtils.setViewPaddingStatusBar(toolbarContainer);
        String mac = sourceManager.getDevice(uuid).$(ID_202_MAC, "");
        if (mac != null) {
            String routerMac = NetUtils.getRouterMacAddress(getApplication());
            if (TextUtils.equals(mac, routerMac)) {
                albumViewMode = 2;
                toolbarAlbumViewMode.setEnabled(true);
            } else {
                albumViewMode = 0;//非 AP 模式,但此时还不知道是否在同一个局域网内
                toolbarAlbumViewMode.setEnabled(false);
            }
        }
        toolbarAlbumViewMode.setText(titles[modeToResId(albumViewMode, false)]);
        presenter.fetch(0, albumViewMode);
    }

    @Override
    protected void onStop() {
        super.onStop();
        ViewUtils.clearViewPaddingStatusBar(toolbarContainer);
    }

    @Override
    protected void setActivityComponent(ActivityComponent activityComponent) {
        activityComponent.inject(this);
    }


    @OnClick(R.id.act_panorama_album_toolbar_header_title)
    public void showAlbumViewModePop() {
        if (albumModeSelectPop == null) {
            albumModeSelectPop = new RoundRectPopup(this, this);
            albumModeSelectPop.setTouchable(true);
            albumModeSelectPop.setOutsideTouchable(false);
            albumModeSelectPop.setFocusable(false);
            albumModeSelectPop.setCheckIndex(modeToResId(albumViewMode, false));
        }
        albumModeSelectPop.showOnAnchor(toolbarAlbumViewMode, RelativePopupWindow.VerticalPosition.ALIGN_TOP, RelativePopupWindow.HorizontalPosition.ALIGN_LEFT);
    }

    public boolean hideAlbumViewModePop() {
        if (albumModeSelectPop != null && albumModeSelectPop.isShowing()) {
            albumModeSelectPop.dismiss();
            return true;
        }
        return false;
    }

    private int modeToResId(@ALBUM_VIEW_MODE int mode, boolean isPop) {
        switch (mode) {
            case ALBUM_VIEW_MODE.MODE_BOTH:
                return isPop ? R.id.menu_item_album_pop_both : 0;
            case ALBUM_VIEW_MODE.MODE_PANORAMA:
                return isPop ? R.id.menu_item_album_pop_panorama : 1;
            case ALBUM_VIEW_MODE.MODE_PHOTO:
                return isPop ? R.id.menu_item_album_pop_photo : 2;
            default:
                return isPop ? R.id.menu_item_album_pop_both : 0;
        }
    }

    private int resIdToMode(@ALBUM_VIEW_MODE int resId) {
        switch (resId) {
            case R.id.menu_item_album_pop_both:
                return ALBUM_VIEW_MODE.MODE_BOTH;
            case R.id.menu_item_album_pop_panorama:
                return ALBUM_VIEW_MODE.MODE_PANORAMA;
            case R.id.menu_item_album_pop_photo:
                return ALBUM_VIEW_MODE.MODE_PHOTO;
            default:
                return ALBUM_VIEW_MODE.MODE_BOTH;
        }
    }

    @Override
    @OnClick(R.id.act_panorama_album_back)
    public void onBackPressed() {
        if (!hideAlbumViewModePop()) {
            super.onBackPressed();
        }
    }

    @OnClick(R.id.tv_album_delete)
    public void albumDelete() {
        String content = tvAlbumDelete.getText().toString();
        if (TextUtils.isEmpty(content) && tvAlbumDelete.isEnabled()) {
            //active state
            tvAlbumDelete.setText(getString(R.string.CANCEL));
            ViewUtils.setDrawablePadding(tvAlbumDelete, -1, 0);
            toggleEditMode(true);
        } else {
            //cancel
            tvAlbumDelete.setText("");
            ViewUtils.setDrawablePadding(tvAlbumDelete, R.drawable.album_delete_selector, 0);
            toggleEditMode(false);
        }
    }

    /**
     * @param toggle
     */
    private void toggleEditMode(boolean toggle) {
        panoramaAdapter.setInEditMode(toggle);
        final int lPos = ((LinearLayoutManager) recyclerView.getLayoutManager())
                .findLastVisibleItemPosition();
        panoramaAdapter.reverseEdition(toggle, lPos);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        albumViewMode = resIdToMode(checkedId);
        toolbarAlbumViewMode.setText(titles[modeToResId(albumViewMode, false)]);
        hideAlbumViewModePop();
        presenter.fetch(0, albumViewMode);
        Log.d("onCheckedChanged", "onCheckedChanged");
    }

    @Override
    public void onItemClick(View itemView, int viewType, int position) {
        PanoramaAlbumContact.PanoramaItem item = panoramaAdapter.getItem(position);
        if (panoramaAdapter.isInEditMode()) {
            panoramaAdapter.reverseItemSelectedState(position);
        } else {
            Intent intent = PanoramaDetailActivity.getIntent(this, uuid, item);
            startActivity(intent);
        }
    }

    @Override
    public void onItemLongClick(View itemView, int viewType, int position) {
        new AlertDialog.Builder(this)
                .setMessage("delete item?")
                .setPositiveButton(getString(R.string.OK), (DialogInterface dialog, int which) -> {
                    presenter.deletePanoramaItem(Collections.singletonList(panoramaAdapter.getList().get(position)));
                })
                .setNegativeButton(getString(R.string.CANCEL), null)
                .show();
    }

    @Override
    public void onRefresh() {
        presenter.fetch(0, albumViewMode);
    }

    @Override
    public void onAppend(List<PanoramaAlbumContact.PanoramaItem> resultList, boolean isRefresh, boolean loadFinish) {
        loading = !loadFinish;
        if (isRefresh) {
            panoramaAdapter.clear();
        }
        if (resultList != null && resultList.size() > 0)
            panoramaAdapter.addAll(resultList);
        //setEmptyView
        emptyView.setVisibility(panoramaAdapter.getCount() > 0 ? View.GONE : View.VISIBLE);
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.post(() -> {
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.setEnabled(true);
        });
    }

    @Override
    public void onDelete(List<PanoramaAlbumContact.PanoramaItem> positionList) {
        swipeRefreshLayout.setRefreshing(false);
        if (positionList.size() > 0) {
            panoramaAdapter.removeAll(positionList);
        }
        //setEmptyView
        emptyView.setVisibility(panoramaAdapter.getCount() > 0 ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onUpdate(PanoramaAlbumContact.PanoramaItem needUpdate, int position) {
        swipeRefreshLayout.setRefreshing(false);
        panoramaAdapter.notifyItemChanged(position);
    }

    @Override
    public List<PanoramaAlbumContact.PanoramaItem> getList() {
        return panoramaAdapter.getList();
    }

    @Override
    public void onViewModeChanged(int apiType) {
        if (apiType == 1) {//forward
            albumViewMode = 0;
            presenter.fetch(0, albumViewMode);
            toolbarAlbumViewMode.setText(titles[modeToResId(albumViewMode, false)]);
            toolbarAlbumViewMode.setEnabled(false);
        } else if (apiType == 0) {
            toolbarAlbumViewMode.setEnabled(true);
        }
    }
}
