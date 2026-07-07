package com.bill_prj.ui.settings;

import android.app.AlertDialog;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bill_prj.R;
import com.bill_prj.data.AppDatabase;
import com.bill_prj.data.dao.CategoryDao;
import com.bill_prj.data.entity.CategoryEntity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.concurrent.Executors;

public class CategoryManageActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tabIncome, tabExpense, tvCategoryCount;
    private View tabIndicator;
    private RecyclerView rvCategories;
    private FloatingActionButton fabAdd;

    private CategoryManageAdapter adapter;
    private CategoryDao categoryDao;
    private boolean showingIncome = true;
    private long currentUserId = 1;

    private static final String[] EMOJIS = {
            "\uD83D\uDE0A", "\uD83D\uDE0D", "\uD83D\uDC4D", "\uD83D\uDC4E", "\uD83D\uDC4F", "\u2728",
            "\uD83D\uDCAA", "\uD83C\uDFC3", "\uD83D\uDE80", "\uD83D\uDE8B", "\uD83D\uDE95", "\u2708\uFE0F",
            "\uD83C\uDF7D\uFE0F", "\uD83C\uDF55", "\u2615", "\uD83C\uDFA5", "\uD83C\uDFB5", "\uD83D\uDCF1",
            "\uD83D\uDCBB", "\uD83D\uDCDA", "\uD83C\uDFE0", "\uD83C\uDFE1", "\uD83C\uDF17", "\u2600\uFE0F"
    };

    private static final String[] COLORS = {
            "#FF5722", "#E91E63", "#9C27B0", "#2196F3", "#00BCD4", "#4CAF50",
            "#FF9800", "#795548", "#607D8B", "#F44336", "#E040FB", "#3F51B5"
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_manage);

        categoryDao = AppDatabase.getInstance(this).categoryDao();

        initViews();
        setupToolbar();
        setupTabs();
        setupFab();

        // Ensure default categories exist (handles fresh install and migration)
        AppDatabase.ensureDefaultCategories(this);

        // Wait briefly for defaults, then load
        tabIndicator.postDelayed(() -> loadCategories(true), 100);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabIncome = findViewById(R.id.tab_income);
        tabExpense = findViewById(R.id.tab_expense);
        tabIndicator = findViewById(R.id.tab_indicator);
        tvCategoryCount = findViewById(R.id.tv_category_count);
        rvCategories = findViewById(R.id.rv_categories);
        fabAdd = findViewById(R.id.fab_add);

        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CategoryManageAdapter(null);
        adapter.setOnDeleteClickListener(this::deleteCategory);
        adapter.setOnItemClickListener(this::showEditCategoryDialog);
        rvCategories.setAdapter(adapter);
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupTabs() {
        tabIncome.setOnClickListener(v -> {
            if (!showingIncome) {
                showingIncome = true;
                loadCategories(true);
                updateTabIndicator();
            }
        });
        tabExpense.setOnClickListener(v -> {
            if (showingIncome) {
                showingIncome = false;
                loadCategories(false);
                updateTabIndicator();
            }
        });
    }

    private void setupFab() {
        fabAdd.setOnClickListener(v -> showAddCategoryDialog(null));
    }

    private void loadCategories(boolean isIncome) {
        Executors.newSingleThreadExecutor().execute(() -> {
            String type = isIncome ? "income" : "expense";
            List<CategoryEntity> data = categoryDao.getCategoriesSync(currentUserId, type);

            runOnUiThread(() -> {
                adapter.updateData(data);
                tvCategoryCount.setText("共 " + data.size() + " 个分类");

                int activeColor = ContextCompat.getColor(this, R.color.md_primary);
                int inactiveColor = ContextCompat.getColor(this, R.color.text_secondary);
                tabIncome.setTextColor(isIncome ? activeColor : inactiveColor);
                tabExpense.setTextColor(isIncome ? inactiveColor : activeColor);

                updateTabIndicator();
            });
        });
    }

    private void updateTabIndicator() {
        tabIndicator.post(() -> {
            int halfWidth = rvCategories.getWidth() / 2;
            if (halfWidth > 0) {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) tabIndicator.getLayoutParams();
                params.width = halfWidth;
                tabIndicator.setLayoutParams(params);
                tabIndicator.setTranslationX(showingIncome ? 0 : halfWidth);
            }
        });
    }

    // ── Add Category ──

    private void showAddCategoryDialog(@Nullable CategoryEntity existing) {
        boolean isEditing = existing != null;
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null);
        EditText etName = dialogView.findViewById(R.id.et_category_name);
        GridLayout glEmojis = dialogView.findViewById(R.id.gl_emojis);
        GridLayout glColors = dialogView.findViewById(R.id.gl_colors);

        if (isEditing) {
            etName.setText(existing.getName());
        }

        final String[] selectedEmoji = {isEditing ? existing.getEmoji() : EMOJIS[0]};
        final String[] selectedColor = {isEditing ? existing.getColor() : COLORS[0]};

        buildEmojiGrid(glEmojis, selectedEmoji, selectedEmoji[0]);
        buildColorGrid(glColors, selectedColor, selectedColor[0]);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle(isEditing ? "编辑分类" : "添加分类")
                .setPositiveButton(isEditing ? "保存" : "添加", (d, which) -> {
                    String name = etName.getText().toString().trim();
                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(this, "请输入分类名称", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    final String finalName = name;
                    Executors.newSingleThreadExecutor().execute(() -> {
                        if (isEditing) {
                            // Update existing
                            existing.setName(finalName);
                            existing.setEmoji(selectedEmoji[0]);
                            existing.setColor(selectedColor[0]);
                            categoryDao.update(existing);
                            runOnUiThread(() -> {
                                loadCategories(showingIncome);
                                Toast.makeText(this, "分类已更新", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            // Check duplicate (runs on background thread, safe)
                            CategoryEntity dup = categoryDao.findByName(currentUserId, finalName);
                            if (dup != null) {
                                runOnUiThread(() ->
                                        Toast.makeText(this, "该分类已存在", Toast.LENGTH_SHORT).show());
                                return;
                            }

                            CategoryEntity category = new CategoryEntity();
                            category.setUserId(currentUserId);
                            category.setName(finalName);
                            category.setType(showingIncome ? "income" : "expense");
                            category.setEmoji(selectedEmoji[0]);
                            category.setColor(selectedColor[0]);
                            category.setDefault(false);
                            category.setCreatedAt(System.currentTimeMillis());

                            categoryDao.insert(category);
                            runOnUiThread(() -> {
                                loadCategories(showingIncome);
                                Toast.makeText(this, "分类已添加", Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                })
                .setNegativeButton("取消", null)
                .create()
                .show();
    }

    // ── Edit Category (click on item) ──

    private void showEditCategoryDialog(CategoryEntity category) {
        showAddCategoryDialog(category);
    }

    // ── Delete Category ──

    private void deleteCategory(CategoryEntity category) {
        new AlertDialog.Builder(this)
                .setTitle("删除分类")
                .setMessage("确定要删除分类「" + category.getName() + "」吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        categoryDao.delete(category);
                        runOnUiThread(() -> loadCategories(showingIncome));
                    });
                    Toast.makeText(this, "分类已删除", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // ── Emoji Grid ──

    private void buildEmojiGrid(GridLayout grid, String[] selectedOut, String preselected) {
        grid.removeAllViews();
        for (int i = 0; i < EMOJIS.length; i++) {
            TextView tv = new TextView(this);
            tv.setText(EMOJIS[i]);
            tv.setTextSize(24f);
            tv.setGravity(android.view.Gravity.CENTER);
            int size = (int) (48 * getResources().getDisplayMetrics().density);
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = size;
            lp.height = size;
            lp.setMargins(4, 4, 4, 4);
            tv.setLayoutParams(lp);

            // Pre-select if matches
            if (EMOJIS[i].equals(preselected)) {
                tv.setBackgroundResource(R.drawable.shape_rounded_border);
            }

            final int index = i;
            tv.setOnClickListener(v -> {
                for (int j = 0; j < grid.getChildCount(); j++) {
                    grid.getChildAt(j).setBackground(null);
                }
                tv.setBackgroundResource(R.drawable.shape_rounded_border);
                selectedOut[0] = EMOJIS[index];
            });

            grid.addView(tv);
        }
    }

    // ── Color Grid ──

    private void buildColorGrid(GridLayout grid, String[] selectedOut, String preselected) {
        grid.removeAllViews();
        for (int i = 0; i < COLORS.length; i++) {
            View colorView = new View(this);
            int size = (int) (40 * getResources().getDisplayMetrics().density);
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = size;
            lp.height = size;
            lp.setMargins(6, 6, 6, 6);
            colorView.setLayoutParams(lp);

            int colorInt;
            try {
                colorInt = android.graphics.Color.parseColor(COLORS[i]);
            } catch (Exception e) {
                colorInt = 0xFF94A3B8;
            }

            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(colorInt);
            circle.setStroke(3, 0xFFCCCCCC);
            colorView.setBackground(circle);

            // Pre-select if matches
            if (COLORS[i].equals(preselected)) {
                circle.setStroke(4, 0xFF333333);
            }

            final int index = i;
            colorView.setOnClickListener(v -> {
                for (int j = 0; j < grid.getChildCount(); j++) {
                    View child = grid.getChildAt(j);
                    GradientDrawable bg = (GradientDrawable) child.getBackground().mutate();
                    bg.setStroke(3, 0xFFCCCCCC);
                }
                GradientDrawable bg = (GradientDrawable) colorView.getBackground().mutate();
                bg.setStroke(4, 0xFF333333);
                selectedOut[0] = COLORS[index];
            });

            grid.addView(colorView);
        }
    }
}
