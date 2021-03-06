package com.cylan.jiafeigou.n.view.mine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.Account;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineAddFromContactContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineAddFromContactPresenterImp;
import com.cylan.jiafeigou.n.view.adapter.item.FriendContextItem;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.LoadingDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * 作者：zsl
 * 创建时间：2016/9/7
 * 描述：
 */
public class MineAddFromContactFragment extends Fragment implements MineAddFromContactContract.View {

    @BindView(R.id.et_mine_add_contact_mesg)
    EditText etMineAddContactMesg;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    @BindView(R.id.iv_mine_add_contact_clear_text)
    ImageView ivMineAddContactClearText;

    private MineAddFromContactContract.Presenter presenter;
    private FriendContextItem friendContextItem;

    public static MineAddFromContactFragment newInstance(Bundle bundle) {
        MineAddFromContactFragment fragment = new MineAddFromContactFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_add_from_contact, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        getIntentData();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @OnTextChanged(R.id.et_mine_add_contact_mesg)
    public void onEditChange(CharSequence s, int start, int before, int count) {
        if (TextUtils.isEmpty(s)) {
            ivMineAddContactClearText.setVisibility(View.GONE);
        } else {
            ivMineAddContactClearText.setVisibility(View.VISIBLE);
        }
    }

    /**
     * desc:获取传递过来的数据
     */
    private void getIntentData() {
        Bundle bundle = getArguments();
        friendContextItem = bundle.getParcelable("friendItem");
    }

    private void initPresenter() {
        presenter = new MineAddFromContactPresenterImp(this);
    }

    @Override
    public void setPresenter(MineAddFromContactContract.Presenter presenter) {

    }

    @Override
    public String getUuid() {
        return null;
    }

    @Override
    public void initEditText(String alids) {
        if (!TextUtils.isEmpty(alids)) {
            ivMineAddContactClearText.setVisibility(View.VISIBLE);
        }
        etMineAddContactMesg.setText(String.format(getString(R.string.Tap3_FriendsAdd_StuffContents), alids));
    }

    @Override
    public String getSendMesg() {
        String mesg = etMineAddContactMesg.getText().toString();
        if (TextUtils.isEmpty(mesg)) {
            Account account = BaseApplication.getAppComponent().getSourceManager().getAccount();
            String alias = account.getAlias();
            String acc = account.getAccount();
            return String.format(getString(R.string.Tap3_FriendsAdd_RequestContents), TextUtils.isEmpty(alias) ? acc : alias);
        } else {
            return mesg;
        }
    }

    @Override
    public void showResultDialog(RxEvent.CheckAccountCallback callback) {
        if (callback.code == JError.ErrorFriendAlready | callback.isFriend) {
            ToastUtil.showToast(getString(R.string.Tap3_Added));
            getActivity().getSupportFragmentManager().popBackStack(MineFriendInformationFragment.class.getSimpleName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else if (callback.code == JError.ErrorFriendToSelf) {
            ToastUtil.showToast(getString(R.string.Tap3_FriendsAdd_NotYourself));
        } else {
            presenter.sendRequest(friendContextItem.friendRequest.account, getSendMesg());
        }
    }

    @Override
    public void showSendReqHint() {
        LoadingDialog.showLoading(getActivity(), getString(R.string.submiting));
    }

    @Override
    public void hideSendReqHint() {
        LoadingDialog.dismissLoading();
    }

    /**
     * 网络状态变化
     *
     * @param state
     */
    @Override
    public void onNetStateChanged(int state) {
        if (state == -1) {
            hideSendReqHint();
            ToastUtil.showNegativeToast(getString(R.string.NO_NETWORK_1));
        }
    }

    /**
     * 发送添加请求的结果
     *
     * @param code
     */
    @Override
    public void sendReqBack(int code) {
        hideSendReqHint();
        if (code == JError.ErrorOK) {
            ToastUtil.showToast(getString(R.string.Tap3_FriendsAdd_Contacts_InvitedTips));
            getActivity().getSupportFragmentManager().popBackStack(MineFriendInformationFragment.class.getSimpleName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else if (code == JError.ErrorFriendToSelf) {
            ToastUtil.showNegativeToast(getString(R.string.Tap3_FriendsAdd_NotYourself));
        } else {
            ToastUtil.showNegativeToast(getString(R.string.SUBMIT_FAIL));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null) presenter.start();
    }

    @OnClick({R.id.tv_toolbar_right, R.id.tv_toolbar_icon})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_toolbar_right:
                if (NetUtils.getJfgNetType() == 0) {
                    ToastUtil.showToast(getString(R.string.NoNetworkTips));
                    return;
                }
                showSendReqHint();
                presenter.checkAccount(friendContextItem.friendRequest.account);
                break;
            case R.id.tv_toolbar_icon:
                getActivity().getSupportFragmentManager().popBackStack(MineFriendInformationFragment.class.getSimpleName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                break;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null) {
            presenter.stop();
        }
    }

    @OnClick(R.id.iv_mine_add_contact_clear_text)
    public void onClick() {
        etMineAddContactMesg.setText("");
    }
}
