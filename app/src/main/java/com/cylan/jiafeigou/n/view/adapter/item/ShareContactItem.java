package com.cylan.jiafeigou.n.view.adapter.item;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.databinding.FragmentShareToContactItemBinding;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

/**
 * Created by yanzhendong on 2017/6/28.
 */

public class ShareContactItem extends AbstractItem<ShareContactItem, AbstractBindingViewHolder<FragmentShareToContactItemBinding>> {
    public String name;
    public String phone;
    public String email;
    public boolean shared = false;
    public int contactType;//0:分享给联系人;1:添加联系人

    public String getAccount() {
        return TextUtils.isEmpty(phone) ? email : phone;
    }


    @Override
    public int getType() {
        return 0;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.fragment_share_to_contact_item;
    }

    @Override
    public AbstractBindingViewHolder<FragmentShareToContactItemBinding> getViewHolder(View v) {
        FragmentShareToContactItemBinding itemBinding = FragmentShareToContactItemBinding.bind(v);
        return new AbstractBindingViewHolder<>(itemBinding);
    }

    @Override
    public void bindView(AbstractBindingViewHolder<FragmentShareToContactItemBinding> holder, List<Object> payloads) {
        super.bindView(holder, payloads);
        FragmentShareToContactItemBinding viewDataBinding = holder.getViewDataBinding();
        viewDataBinding.tvContactname.setText(name);
        viewDataBinding.tvContactphone.setText(TextUtils.isEmpty(phone) ? email : phone);
        viewDataBinding.tvContactshare.setText(contactType == 1 ? R.string.Button_Add : shared ? R.string.Tap3_ShareDevice_Shared : R.string.Tap3_ShareDevice_Button);
        viewDataBinding.tvContactshare.setBackgroundResource(contactType == 1 ? R.drawable.btn_accept_add_request_shape : shared ? 0 : R.drawable.btn_accept_add_request_shape);
        viewDataBinding.tvContactshare.setTextColor(contactType == 1 ? Color.parseColor("#4b9fd5") : shared ? Color.parseColor("#ADADAD") : Color.parseColor("#4b9fd5"));
        viewDataBinding.tvContactshare.setEnabled(contactType == 1 || !shared);//如果是添加,则自己也不应该禁用掉,
    }
}
