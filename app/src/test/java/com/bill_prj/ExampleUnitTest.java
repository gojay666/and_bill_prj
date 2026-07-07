package com.bill_prj;

import com.bill_prj.data.BillType;
import com.bill_prj.data.AccountType;
import com.bill_prj.data.TimeFilterType;
import com.bill_prj.utils.DateUtils;
import com.bill_prj.utils.NumberUtils;
import com.bill_prj.utils.EncryptionUtils;
import com.bill_prj.data.entity.BillEntity;
import com.bill_prj.data.entity.User;
import com.bill_prj.data.entity.AccountEntity;
import com.bill_prj.data.entity.BudgetEntity;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for bill tracking application.
 */
public class ExampleUnitTest {

    // ==================== BillType Tests ====================

    @Test
    public void billType_has18Types() {
        assertEquals(18, BillType.getTotalCount());
    }

    @Test
    public void billType_has6IncomeTypes() {
        assertEquals(6, BillType.getIncomeTypes().size());
    }

    @Test
    public void billType_has12ExpenseTypes() {
        assertEquals(12, BillType.getExpenseTypes().size());
    }

    @Test
    public void billType_salaryIsIncome() {
        assertTrue(BillType.SALARY.isIncome());
    }

    @Test
    public void billType_foodIsExpense() {
        assertFalse(BillType.FOOD.isIncome());
    }

    @Test
    public void billType_fromDisplayName_findsCorrectType() {
        assertEquals(BillType.FOOD, BillType.fromDisplayName("餐饮"));
        assertEquals(BillType.SALARY, BillType.fromDisplayName("工资"));
        assertEquals(BillType.OTHER_EXPENSE, BillType.fromDisplayName("未知类型"));
    }

    @Test
    public void billType_allTypes_haveNonEmptyDisplayNames() {
        for (BillType type : BillType.values()) {
            assertNotNull("Display name should not be null for " + type.name(), type.getDisplayName());
            assertTrue("Display name should not be empty for " + type.name(),
                    type.getDisplayName().length() > 0);
        }
    }

    @Test
    public void billType_allTypes_haveColorHex() {
        for (BillType type : BillType.values()) {
            assertNotNull("Color hex should not be null for " + type.name(), type.getColorHex());
            assertTrue("Color hex should start with # for " + type.name(),
                    type.getColorHex().startsWith("#"));
        }
    }

    // ==================== AccountType Tests ====================

    @Test
    public void accountType_has6Types() {
        assertEquals(6, AccountType.values().length);
    }

    @Test
    public void accountType_fromDisplayName_works() {
        assertEquals(AccountType.CASH, AccountType.fromDisplayName("现金"));
        assertEquals(AccountType.WECHAT, AccountType.fromDisplayName("微信"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void accountType_fromInvalidName_throwsException() {
        AccountType.fromDisplayName("不存在的类型");
    }

    // ==================== TimeFilterType Tests ====================

    @Test
    public void timeFilterType_has6Values() {
        assertEquals(6, TimeFilterType.values().length);
    }

    // ==================== DateUtils Tests ====================

    @Test
    public void dateUtils_getTodayStart_isBeforeTodayEnd() {
        long start = DateUtils.getTodayStart();
        long end = DateUtils.getTodayEnd();
        assertTrue("Today start should be before today end", start < end);
    }

    @Test
    public void dateUtils_getWeekStart_isBeforeWeekEnd() {
        long start = DateUtils.getWeekStart();
        long end = DateUtils.getWeekEnd();
        assertTrue("Week start should be before week end", start < end);
    }

    @Test
    public void dateUtils_getMonthStart_isBeforeMonthEnd() {
        long start = DateUtils.getMonthStart();
        long end = DateUtils.getMonthEnd();
        assertTrue("Month start should be before month end", start < end);
    }

    @Test
    public void dateUtils_getYearStart_isBeforeYearEnd() {
        long start = DateUtils.getYearStart();
        long end = DateUtils.getYearEnd();
        assertTrue("Year start should be before year end", start < end);
    }

    @Test
    public void dateUtils_formatDate_returnsNonEmptyString() {
        String formatted = DateUtils.formatDate(System.currentTimeMillis(), "yyyy-MM-dd");
        assertNotNull(formatted);
        assertTrue(formatted.length() > 0);
    }

    @Test
    public void dateUtils_isToday_returnsTrueForCurrentTime() {
        assertTrue(DateUtils.isToday(System.currentTimeMillis()));
    }

    @Test
    public void dateUtils_formatDateTime_returnsValidFormat() {
        String result = DateUtils.formatDateTime(System.currentTimeMillis());
        assertNotNull(result);
        assertTrue(result.contains(":"));
    }

    @Test
    public void dateUtils_dateString_formatIsCorrect() {
        String dateStr = DateUtils.getDateString(System.currentTimeMillis());
        assertTrue("Date string should match YYYY-MM-DD format",
                dateStr.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    // ==================== NumberUtils Tests ====================

    @Test
    public void numberUtils_formatAmount_addsCommas() {
        String result = NumberUtils.formatAmount(1234567.89);
        assertTrue("Should contain comma separators", result.contains(","));
    }

    @Test
    public void numberUtils_formatAmount_hasTwoDecimalPlaces() {
        String result = NumberUtils.formatAmount(100.0);
        assertTrue("Should end with .00", result.endsWith(".00"));
    }

    @Test
    public void numberUtils_formatAmountWithSymbol_incomeShowsPlus() {
        String result = NumberUtils.formatAmountWithSymbol(500.00, true);
        assertTrue("Income should show +", result.startsWith("+"));
    }

    @Test
    public void numberUtils_formatAmountWithSymbol_expenseShowsMinus() {
        String result = NumberUtils.formatAmountWithSymbol(300.00, false);
        assertTrue("Expense should show -", result.startsWith("-"));
    }

    @Test
    public void numberUtils_parseAmount_handlesCommas() {
        double result = NumberUtils.parseAmount("1,234.56");
        assertEquals(1234.56, result, 0.001);
    }

    @Test
    public void numberUtils_formatPercentage_works() {
        String result = NumberUtils.formatPercentage(45.25);
        assertEquals("45.25%", result);
    }

    // ==================== EncryptionUtils Tests ====================

    @Test
    public void encryptionUtils_encryptPassword_returnsNonEmptyHash() {
        String hash = EncryptionUtils.encryptPassword("MyPassword123");
        assertNotNull(hash);
        assertTrue("Hash should not be empty", hash.length() > 0);
    }

    @Test
    public void encryptionUtils_verifyPassword_works() {
        String password = "testPassword123";
        String hash = EncryptionUtils.encryptPassword(password);
        assertTrue("Verification should succeed", EncryptionUtils.verifyPassword(password, hash));
        assertFalse("Wrong password should fail", EncryptionUtils.verifyPassword("wrong", hash));
    }

    @Test
    public void encryptionUtils_samePassword_differentHashes() {
        String hash1 = EncryptionUtils.encryptPassword("password");
        String hash2 = EncryptionUtils.encryptPassword("password");
        assertEquals("Deterministic hash should match", hash1, hash2);
    }

    @Test
    public void encryptionUtils_encryptDecrypt_roundTrip() {
        String original = "SensitiveData123!";
        String key = "TestKey16Bytes!!";
        String encrypted = EncryptionUtils.encrypt(original, key);
        assertNotNull(encrypted);
        assertNotEquals("Encrypted should differ from original", original, encrypted);

        String decrypted = EncryptionUtils.decrypt(encrypted, key);
        assertEquals("Decrypted should match original", original, decrypted);
    }

    // ==================== Entity Tests ====================

    @Test
    public void userEntity_constructor_setsFieldsCorrectly() {
        User user = new User("testuser", "hashedPassword", "13800138000");
        assertEquals("testuser", user.getUsername());
        assertEquals("hashedPassword", user.getPassword());
        assertEquals("13800138000", user.getPhone());
        assertTrue("CreatedAt should be set", user.getCreatedAt() > 0);
        assertTrue("UpdatedAt should be set", user.getUpdatedAt() > 0);
    }

    @Test
    public void userEntity_defaultConstructor_createsEmptyUser() {
        User user = new User();
        assertNotNull(user);
    }

    @Test
    public void billEntity_constructor_setsFields() {
        BillEntity bill = new BillEntity();
        bill.setUserId(1L);
        bill.setType(false); // expense
        bill.setCategory("餐饮");
        bill.setAmount(35.50);
        bill.setAccountId(1L);
        bill.setNote("午餐");
        bill.setDate(System.currentTimeMillis());
        bill.setCreatedAt(System.currentTimeMillis());

        assertEquals(1L, bill.getUserId());
        assertFalse(bill.isType());
        assertEquals("餐饮", bill.getCategory());
        assertEquals(35.50, bill.getAmount(), 0.001);
        assertEquals("午餐", bill.getNote());
    }

    @Test
    public void accountEntity_constructor_setsFields() {
        AccountEntity account = new AccountEntity();
        account.setUserId(1L);
        account.setName("我的钱包");
        account.setType("现金");
        account.setBalance(1000.00);

        assertEquals("我的钱包", account.getName());
        assertEquals("现金", account.getType());
        assertEquals(1000.00, account.getBalance(), 0.001);
    }

    @Test
    public void budgetEntity_constructor_setsFields() {
        BudgetEntity budget = new BudgetEntity();
        budget.setUserId(1L);
        budget.setType("MONTHLY");
        budget.setAmount(5000.00);
        budget.setUsedAmount(0);

        assertEquals("MONTHLY", budget.getType());
        assertEquals(5000.00, budget.getAmount(), 0.001);
        assertEquals(0, budget.getUsedAmount(), 0.001);
    }

    @Test
    public void budgetEntity_defaultUsedAmountIsZero() {
        BudgetEntity budget = new BudgetEntity();
        assertEquals(0, budget.getUsedAmount(), 0.001);
    }

    // ==================== Data Consistency Tests ====================

    @Test
    public void incomeCategories_areNotInExpenseList() {
        List<BillType> expenseTypes = BillType.getExpenseTypes();
        for (BillType incomeType : BillType.getIncomeTypes()) {
            assertFalse("Income type should not be in expense list: " + incomeType.name(),
                    expenseTypes.contains(incomeType));
        }
    }

    @Test
    public void allCategoryTypesAreCovered() {
        int incomeCount = BillType.getIncomeTypes().size();
        int expenseCount = BillType.getExpenseTypes().size();
        assertEquals("Income + Expense should equal total",
                BillType.getTotalCount(), incomeCount + expenseCount);
    }
}
