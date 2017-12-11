package com.cylan.jiafeigou.n.view.mine;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableInt;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.entity.jniCall.JFGShareListInfo;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.databinding.FragmentMineShareToFriendBinding;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineShareToFriendContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineShareToFriendPresenterImp;
import com.cylan.jiafeigou.n.view.adapter.item.ShareFriendItem;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import java.util.ArrayList;

/**
 * 作者：zsl
 * 创建时间：2016/9/13
 * 描述：
 */
public class MineShareToFriendFragment extends IBaseFragment implements MineShareToFriendContract.View {
    private static final int MAX_SHARE_NUMBER = 5;
    private MineShareToFriendContract.Presenter presenter;
    private FastItemAdapter<ShareFriendItem> shareToFriendsAdapter;
    private FragmentMineShareToFriendBinding shareToFriendBinding;
    private ObservableBoolean empty = new ObservableBoolean(true);
    private ObservableInt sharedNumber = new ObservableInt();
    private Runnable callback;
    private JFGShareListInfo shareListInfo;

    public void setCallBack(Runnable runnable) {
        this.callback = runnable;
    }

    public static MineShareToFriendFragment newInstance(Bundle bundle) {
        MineShareToFriendFragment fragment = new MineShareToFriendFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        this.uuid = arguments.getString(JConstant.KEY_DEVICE_ITEM_UUID, "");
        shareToFriendBinding = FragmentMineShareToFriendBinding.inflate(inflater, container, false);
        shareToFriendBinding.customToolbar.setToolbarRightColor(R.color.color_d8d8d8_fffffff);
        shareToFriendBinding.customToolbar.setBackAction(this::onClick);
        shareToFriendBinding.customToolbar.setRightAction(this::onClick);
        shareToFriendBinding.btnToAdd.setOnClickListener(this::onClick);
        shareToFriendBinding.setEmpty(empty);
        shareToFriendBinding.setSharedNumber(sharedNumber);
        shareToFriendsAdapter = new FastItemAdapter<>();
        shareToFriendsAdapter.withSelectable(true);
        shareToFriendsAdapter.withMultiSelect(true);
        shareToFriendsAdapter.withAllowDeselection(true);
        shareToFriendsAdapter.withSelectWithItemUpdate(true);
        shareToFriendsAdapter.withSelectionListener((item, selected) -> addShareWithNumberCheck(item));
        shareListInfo = DataSourceManager.getInstance().getShareListByCid(uuid);
        shareToFriendBinding.setHasSharedNumber(shareListInfo == null ? 0 : shareListInfo.friends.size());
        shareToFriendBinding.rcyMineShareToRelativeAndFriendList.setLayoutManager(new LinearLayoutManager(getContext()));
        shareToFriendBinding.rcyMineShareToRelativeAndFriendList.setAdapter(shareToFriendsAdapter);
        initPresenter();
        return shareToFriendBinding.getRoot();
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    private void initPresenter() {
        presenter = new MineShareToFriendPresenterImp(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null) {
            presenter.getCanShareFriendsList(uuid);
        }
    }
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_toolbar_icon: {
                getActivity().getSupportFragmentManager().popBackStack();
                break;
            }
            case R.id.tv_toolbar_right: {
                if (NetUtils.getNetType(getContext()) == -1) {
                    ToastUtil.showNegativeToast(getString(R.string.Item_ConnectionFail));
                } else {
                    presenter.shareDeviceToFriend(uuid, new ArrayList<>(shareToFriendsAdapter.getSelectedItems()));
                }
                break;
            }
            case R.id.btn_to_add: {
                ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                        MineFriendAddFriendsFragment.newInstance(), android.R.id.content);
                break;
            }
        }
    }

    @Override
    public void onInitCanShareFriendList(ArrayList<ShareFriendItem> list) {
        shareToFriendsAdapter.clear();
        shareToFriendsAdapter.add(list);
        empty.set(shareToFriendsAdapter.getItemCount() == 0);
    }

    private void addShareWithNumberCheck(ShareFriendItem item) {
        int hasSharedNumber = shareListInfo == null ? 0 : shareListInfo.friends.size();
        int selectedNumber = shareToFriendsAdapter.getSelectedItems().size();
        if (hasSharedNumber + selectedNumber > MAX_SHARE_NUMBER) {
            int position = shareToFriendsAdapter.getPosition(item);
            shareToFriendsAdapter.deselect(position);
            ToastUtil.showToast(getString(R.string.Tap3_ShareDevice_Tips));
        } else {
            sharedNumber.set(selectedNumber);
        }

    }

    @Override
    public void showShareToFriendsResult(RxEvent.MultiShareDeviceEvent result) {
        if (result == null) {//操作超时了
            // TODO: 2017/6/28 未注明怎么处理
        } else if (result.ret == 0) {//分享成功了
            ToastUtil.showPositiveToast(getString(R.string.Tap3_ShareDevice_SuccessTips));
            if (callback != null) {
                callback.run();
            }
            getActivity().getSupportFragmentManager().popBackStack();
        } else {//分享失败了
            showShareResultDialog(getString(R.string.Tap3_ShareDevice_FailTips));
        }
    }

    @Override
    public void showLoading(int resId, Object... args) {
        LoadingDialog.showLoading(getActivity(), getString(resId, (Object[]) args), true);
    }

    @Override
    public void hideLoading() {
        LoadingDialog.dismissLoading();
    }

    /**
     * 弹出分享
     *
     * @param title
     */
    private void showShareResultDialog(String title) {
        AlertDialog.Builder builder = AlertDialogManager.getInstance().getCustomDialog(getActivity());
        builder.setTitle(title);
        builder.setPositiveButton(getString(R.string.TRY_AGAIN), (dialog, which) -> {
            dialog.dismiss();
            presenter.shareDeviceToFriend(uuid, new ArrayList<>(shareToFriendsAdapter.getSelectedItems()));
        });
        builder.setNegativeButton(getString(R.string.MAGNETISM_OFF), null);
        AlertDialogManager.getInstance().showDialog("showShareResultDialog", getActivity(), builder);
    }
}
