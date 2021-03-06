package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.FriendBean;
import com.cylan.jiafeigou.support.superadapter.IMulItemViewType;
import com.cylan.jiafeigou.support.superadapter.SuperAdapter;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;
import com.cylan.jiafeigou.utils.ContextUtils;

import java.util.List;

/**
 * 作者：zsl
 * 创建时间：2016/9/13
 * 描述：
 */
public class ShareToContactAdapter extends SuperAdapter<FriendBean> {

    private onShareLisenter lisenter;

    public interface onShareLisenter {
        void isShare(FriendBean item);
    }

    public void setOnShareLisenter(onShareLisenter lisenter) {
        this.lisenter = lisenter;
    }

    public ShareToContactAdapter(Context context, List<FriendBean> items, IMulItemViewType<FriendBean> mulItemViewType) {
        super(context, items, mulItemViewType);
    }

    @Override
    public void onBind(final SuperViewHolder holder, int viewType, final int layoutPosition, final FriendBean item) {
        holder.setText(R.id.tv_contactname, "".equals(item.alias) ? "" : item.alias);
        holder.setText(R.id.tv_contactphone, item.account);

        TextView shareBtn = holder.getView(R.id.tv_contactshare);

        if (item.isCheckFlag == 1) {
            shareBtn.setTextColor(Color.parseColor("#ADADAD"));
            shareBtn.setText(ContextUtils.getContext().getString(R.string.Tap3_ShareDevice_Shared));
            shareBtn.setBackground(null);
            shareBtn.setEnabled(false);
        } else {
            shareBtn.setTextColor(Color.parseColor("#4b9fd5"));
            shareBtn.setText(ContextUtils.getContext().getString(R.string.Tap3_ShareDevice_Button));
            shareBtn.setBackground(ContextUtils.getContext().getResources().getDrawable(R.drawable.btn_accept_add_request_shape));
            shareBtn.setEnabled(true);
        }

        holder.setOnClickListener(R.id.tv_contactshare, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lisenter != null) {
                    lisenter.isShare(item);
                }
            }
        });
    }

    @Override
    protected IMulItemViewType<FriendBean> offerMultiItemViewType() {
        return new IMulItemViewType<FriendBean>() {
            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public int getItemViewType(int position, FriendBean bean) {
                return 0;
            }

            @Override
            public int getLayoutId(int viewType) {
                return R.layout.fragment_share_to_contact_item;
            }
        };
    }

}
