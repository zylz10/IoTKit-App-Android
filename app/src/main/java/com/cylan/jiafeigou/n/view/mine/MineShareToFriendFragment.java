package com.cylan.jiafeigou.n.view.mine;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineShareToFriendContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineShareToFriendPresenterImp;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.n.view.adapter.ShareToFriendsAdapter;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.superadapter.internal.SuperViewHolder;
import com.sina.weibo.sdk.utils.LogUtil;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/13
 * 描述：
 */
public class MineShareToFriendFragment extends Fragment implements MineShareToFriendContract.View {


    @BindView(R.id.iv_mine_share_to_relative_friend_back)
    ImageView ivMineShareToRelativeFriendBack;
    @BindView(R.id.tv_mine_share_to_relative_friend_true)
    TextView tvMineShareToRelativeFriendTrue;
    @BindView(R.id.rcy_mine_share_to_relative_and_friend_list)
    RecyclerView rcyMineShareToRelativeAndFriendList;
    @BindView(R.id.ll_no_friend)
    LinearLayout llNoFriend;
    @BindView(R.id.rl_send_pro_hint)
    RelativeLayout rlSendProHint;

    private MineShareToFriendContract.Presenter presenter;

    private ShareToFriendsAdapter shareToFriendsAdapter;
    private int hasShareNum;

    private ArrayList<RelAndFriendBean> isChooseToShareList = new ArrayList<>();

    public static MineShareToFriendFragment newInstance() {
        return new MineShareToFriendFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_share_to_relative_and_friend, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        return view;
    }

    private void initPresenter() {
        presenter = new MineShareToFriendPresenterImp(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null) {
            presenter.start();
        }
    }

    @Override
    public void setPresenter(MineShareToFriendContract.Presenter presenter) {

    }

    @OnClick({R.id.iv_mine_share_to_relative_friend_back, R.id.tv_mine_share_to_relative_friend_true})
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.iv_mine_share_to_relative_friend_back:
                getFragmentManager().popBackStack();
                break;

            case R.id.tv_mine_share_to_relative_friend_true:
                if (presenter.checkNetConnetion()){
                    presenter.sendShareToFriendReq(isChooseToShareList);
                }else {
                    ToastUtil.showToast(getContext(),"网络不可用");
                }
                break;
        }
    }

    @Override
    public void initRecycleView(ArrayList<RelAndFriendBean> list) {
        rcyMineShareToRelativeAndFriendList.setLayoutManager(new LinearLayoutManager(getContext()));
        shareToFriendsAdapter = new ShareToFriendsAdapter(getContext(), list, null);
        rcyMineShareToRelativeAndFriendList.setAdapter(shareToFriendsAdapter);
        initAdaListener();
    }

    @Override
    public void showNoFriendNullView() {
        llNoFriend.setVisibility(View.VISIBLE);
    }

    @Override
    public void setHasShareFriendNum(boolean isChange, int number) {
        if (number == 0) {
            tvMineShareToRelativeFriendTrue.setText("确定（0/5）");
            tvMineShareToRelativeFriendTrue.setTextColor(Color.GRAY);
        } else if (isChange) {
            tvMineShareToRelativeFriendTrue.setTextColor(Color.WHITE);
            tvMineShareToRelativeFriendTrue.setText("确定（" + number + "/5）");
        } else {
            tvMineShareToRelativeFriendTrue.setTextColor(Color.GRAY);
            tvMineShareToRelativeFriendTrue.setText("确定（" + number + "/5）");
        }
    }

    @Override
    public void showShareAllSuccess() {
        //TODO 完善
        ToastUtil.showToast(getContext(), "分享成功");
    }

    @Override
    public void showShareSomeFail(int some) {
        //TODO 完善
        showShareResultDialog(some + "位用户分享失败");
    }

    @Override
    public void showShareAllFail() {
        //TODO 完善
        showShareResultDialog("分享失败");
    }

    @Override
    public void showSendProgress() {
        rlSendProHint.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideSendProgress() {
        rlSendProHint.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showNumIsOverDialog(SuperViewHolder holder) {
        //当人数超过5人时选中 松开手之后弹起
        holder.setChecked(R.id.checkbox_is_share_check,false);
        ToastUtil.showToast(getContext(),"该设备已达到最大分享数");
    }

    /**
     * 弹出分享
     *
     * @param title
     */
    private void showShareResultDialog(String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title);
        builder.setPositiveButton("重试", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //TODO　部分分享失败 等待SDK返回的数据
                presenter.sendShareToFriendReq(isChooseToShareList);
            }
        });
        builder.setNegativeButton("关闭", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    /**
     * 列表的监听器
     */
    private void initAdaListener() {
        shareToFriendsAdapter.setOnShareCheckListener(new ShareToFriendsAdapter.OnShareCheckListener() {
            @Override
            public void onCheck(boolean isCheckFlag, SuperViewHolder holder, RelAndFriendBean item) {
                hasShareNum = presenter.getHasShareFriendNumber();
                boolean numIsChange = false;
                isChooseToShareList.clear();
                for (RelAndFriendBean bean : shareToFriendsAdapter.getList()) {
                    if (bean.isCheckFlag == 1) {
                        hasShareNum++;
                        numIsChange = true;
                        isChooseToShareList.add(bean);
                    }
                }
                presenter.checkShareNumIsOver(holder,numIsChange,hasShareNum);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null){
            presenter.stop();
        }
    }
}
