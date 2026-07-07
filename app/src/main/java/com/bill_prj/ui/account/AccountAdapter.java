package com.bill_prj.ui.account;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bill_prj.R;
import com.bill_prj.data.entity.AccountEntity;
import com.bill_prj.utils.NumberUtils;

import java.util.List;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {

    public interface OnAccountLongClickListener {
        void onAccountLongClick(AccountEntity account, int position);
    }

    private List<AccountEntity> accountList;
    private OnAccountLongClickListener longClickListener;

    public AccountAdapter(List<AccountEntity> accountList) {
        this.accountList = accountList;
    }

    public void setOnAccountLongClickListener(OnAccountLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void updateAccounts(List<AccountEntity> newAccounts) {
        this.accountList.clear();
        this.accountList.addAll(newAccounts);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_account, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        AccountEntity account = accountList.get(position);
        holder.bind(account, position);
    }

    @Override
    public int getItemCount() {
        return accountList.size();
    }

    class AccountViewHolder extends RecyclerView.ViewHolder {

        private final View viewAccountIcon;
        private final TextView tvAccountIconLetter;
        private final TextView tvAccountName;
        private final TextView tvAccountType;
        private final TextView tvAccountBalance;

        AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            viewAccountIcon = itemView.findViewById(R.id.viewAccountIcon);
            tvAccountIconLetter = itemView.findViewById(R.id.tvAccountIconLetter);
            tvAccountName = itemView.findViewById(R.id.tvAccountName);
            tvAccountType = itemView.findViewById(R.id.tvAccountType);
            tvAccountBalance = itemView.findViewById(R.id.tvAccountBalance);
        }

        void bind(AccountEntity account, int position) {
            tvAccountName.setText(account.getName());
            tvAccountType.setText(getAccountTypeDisplay(account.getType()));
            tvAccountBalance.setText(NumberUtils.formatCurrency(account.getBalance()));

            // Set the first character of the account name as icon letter
            String name = account.getName();
            if (name != null && !name.isEmpty()) {
                tvAccountIconLetter.setText(String.valueOf(name.charAt(0)));
            } else {
                tvAccountIconLetter.setText("钱");
            }

            // Color the circle based on account type
            int colorResId = getAccountColor(account.getType());
            viewAccountIcon.setBackgroundResource(colorResId);

            // Long press for edit/delete
            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onAccountLongClick(account, position);
                }
                return true;
            });
        }

        private String getAccountTypeDisplay(String type) {
            if (type == null) return "";
            switch (type) {
                case "cash": return "现金";
                case "bank": return "银行卡";
                case "credit_card": return "信用卡";
                case "alipay": return "支付宝";
                case "wechat": return "微信支付";
                default: return type;
            }
        }

        private int getAccountColor(String type) {
            if (type == null) return R.drawable.ic_category_other;
            switch (type) {
                case "cash": return R.drawable.ic_category_food;
                case "bank": return R.drawable.ic_category_salary;
                case "credit_card": return R.drawable.ic_category_entertainment;
                case "alipay": return R.drawable.ic_category_shopping;
                case "wechat": return R.drawable.ic_category_education;
                default: return R.drawable.ic_category_other;
            }
        }
    }
}
