package com.bill_prj.ui.bill;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bill_prj.R;
import com.bill_prj.data.AppDatabase;
import com.bill_prj.data.dao.AccountDao;
import com.bill_prj.data.entity.AccountEntity;
import com.bill_prj.data.entity.BillEntity;
import com.bill_prj.utils.SharedPrefsManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BillsFragment extends Fragment {

    private BillViewModel billViewModel;
    private RecyclerView recyclerView;
    private BillAdapter billAdapter;
    private TextView tvEmpty;
    private SharedPrefsManager prefsManager;

    // Action bar views
    private LinearLayout llSelectionActionBar;
    private TextView tvSelectedCount;
    private Button btnDeleteSelected;
    private Button btnCancelSelection;

    // Cached bill list for batch operations
    private List<BillEntity> currentBillList = new ArrayList<>();

    private final RecyclerView.AdapterDataObserver selectionObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            updateActionBar();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_bills, container, false);

        recyclerView = root.findViewById(R.id.rv_bills);
        tvEmpty = root.findViewById(R.id.tv_empty);
        prefsManager = new SharedPrefsManager(requireContext());

        // Initialize action bar views
        llSelectionActionBar = root.findViewById(R.id.ll_selection_action_bar);
        tvSelectedCount = root.findViewById(R.id.tv_selected_count);
        btnDeleteSelected = root.findViewById(R.id.btn_delete_selected);
        btnCancelSelection = root.findViewById(R.id.btn_cancel_selection);

        billAdapter = new BillAdapter();
        billAdapter.registerAdapterDataObserver(selectionObserver);
        billAdapter.setOnItemClickListener(bill -> {
            Intent intent = new Intent(getActivity(), BillDetailActivity.class);
            intent.putExtra("bill_id", bill.getId());
            startActivity(intent);
        });
        billAdapter.setOnItemLongClickListener(bill -> {
            enterSelectionMode(bill);
            return true;
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(billAdapter);

        billViewModel = new ViewModelProvider(requireActivity()).get(BillViewModel.class);

        loadAccountNames();

        setupObservers();
        setupActionBarListeners();
        loadBills();

        root.findViewById(R.id.fab_add_bill).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AddBillActivity.class));
        });

        return root;
    }

    private void loadAccountNames() {
        new Thread(() -> {
            AccountDao accountDao = AppDatabase.getInstance(requireContext()).accountDao();
            long userId = prefsManager.getUserId();
            List<AccountEntity> accounts = accountDao.findByUserIdSync(userId);
            Map<Long, String> nameMap = new HashMap<>();
            if (accounts != null) {
                for (AccountEntity a : accounts) {
                    nameMap.put(a.getId(), a.getName());
                }
            }
            Map<Long, String> finalMap = nameMap;
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> billAdapter.setAccountNames(finalMap));
            }
        }).start();
    }

    private void setupObservers() {
        billViewModel.getBills().observe(getViewLifecycleOwner(), bills -> {
            if (bills != null) {
                currentBillList = new ArrayList<>(bills);
            }
            updateBillsList(bills);
        });

        billViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupActionBarListeners() {
        btnDeleteSelected.setOnClickListener(v -> {
            Set<Long> selectedIds = billAdapter.getSelectedIds();
            int count = selectedIds.size();
            if (count == 0) return;

            new AlertDialog.Builder(requireContext())
                    .setTitle("确认删除")
                    .setMessage("确定要删除选中的" + count + "条账单吗？")
                    .setPositiveButton("确定", (dialog, which) -> {
                        List<BillEntity> toDelete = new ArrayList<>();
                        for (BillEntity bill : currentBillList) {
                            if (selectedIds.contains(bill.getId())) {
                                toDelete.add(bill);
                            }
                        }
                        billViewModel.deleteBills(toDelete);
                        exitSelectionMode();
                        Toast.makeText(requireContext(), "已删除" + count + "条账单", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });

        btnCancelSelection.setOnClickListener(v -> {
            exitSelectionMode();
        });
    }

    private void enterSelectionMode(BillEntity longPressedBill) {
        billAdapter.setSelectionMode(true);
        billAdapter.toggleSelection(longPressedBill.getId());
        llSelectionActionBar.setVisibility(View.VISIBLE);
    }

    private void exitSelectionMode() {
        billAdapter.clearSelection();
        llSelectionActionBar.setVisibility(View.GONE);
    }

    private void updateActionBar() {
        int count = billAdapter.getSelectedCount();
        tvSelectedCount.setText("已选择" + count + "项");
        btnDeleteSelected.setText("删除选中(" + count + ")");
    }

    private void loadBills() {
        long userId = prefsManager.getUserId();
        Calendar startCal = Calendar.getInstance();
        startCal.set(Calendar.DAY_OF_MONTH, 1);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);

        Calendar endCal = Calendar.getInstance();
        endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH));
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        endCal.set(Calendar.MILLISECOND, 999);

        billViewModel.loadBills(userId, startCal.getTimeInMillis(), endCal.getTimeInMillis());
    }

    private void updateBillsList(List<BillEntity> bills) {
        if (bills == null || bills.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            billAdapter.submitList(bills);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBills();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (billAdapter != null) {
            billAdapter.unregisterAdapterDataObserver(selectionObserver);
        }
    }
}
