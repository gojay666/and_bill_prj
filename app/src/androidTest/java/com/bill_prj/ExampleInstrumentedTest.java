package com.bill_prj;

import android.content.Context;

import androidx.room.Room;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.bill_prj.data.AppDatabase;
import com.bill_prj.data.dao.BillDao;
import com.bill_prj.data.dao.UserDao;
import com.bill_prj.data.entity.User;
import com.bill_prj.data.entity.BillEntity;
import com.bill_prj.data.entity.AccountEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Instrumented tests for Room database operations.
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    private AppDatabase database;
    private UserDao userDao;
    private BillDao billDao;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        userDao = database.userDao();
        billDao = database.billDao();
    }

    @After
    public void tearDown() {
        database.close();
    }

    @Test
    public void testAppName() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.bill_prj", appContext.getPackageName());
    }

    @Test
    public void testUserInsertAndQuery() {
        User user = new User("testuser", "password123", "13800138000");
        long id = userDao.insert(user);
        assertTrue("User ID should be > 0", id > 0);

        User queried = userDao.findByIdSync(id);
        assertNotNull("Queried user should not be null", queried);
        assertEquals("testuser", queried.getUsername());
        assertEquals("13800138000", queried.getPhone());
    }

    @Test
    public void testUserPhoneUniqueness() {
        User user1 = new User("user1", "pass1", "13800138000");
        userDao.insert(user1);

        User user2 = new User("user2", "pass2", "13800138000");
        userDao.insert(user2); // Should replace due to OnConflictStrategy.REPLACE

        int count = userDao.countByPhone("13800138000");
        assertEquals("Phone should be unique", 1, count);
    }

    @Test
    public void testUserLogin() {
        User user = new User("loginuser", "mypassword", "13900139000");
        userDao.insert(user);

        User loggedIn = userDao.login("loginuser", "mypassword");
        assertNotNull("Login should succeed", loggedIn);
        assertEquals("loginuser", loggedIn.getUsername());
    }

    @Test
    public void testUserLoginWithWrongPassword() {
        User user = new User("secureuser", "correctpass", "13700137000");
        userDao.insert(user);

        User failed = userDao.login("secureuser", "wrongpass");
        assertNull("Login with wrong password should fail", failed);
    }

    @Test
    public void testBillInsertAndQueryByUserId() {
        // First create a user
        User user = new User("billuser", "pass", "13600136000");
        long userId = userDao.insert(user);

        // Insert some bills
        for (int i = 0; i < 3; i++) {
            BillEntity bill = new BillEntity();
            bill.setUserId(userId);
            bill.setType(i % 2 == 0); // alternate income/expense
            bill.setCategory(i % 2 == 0 ? "工资" : "餐饮");
            bill.setAmount(100.0 * (i + 1));
            bill.setAccountId(1L);
            bill.setNote("Test bill " + i);
            bill.setDate(System.currentTimeMillis() - i * 86400000L);
            bill.setCreatedAt(System.currentTimeMillis());
            billDao.insert(bill);
        }

        // Query bills by user
        List<BillEntity> bills = billDao.findByDateRangeSync(userId, 0, Long.MAX_VALUE);
        assertNotNull("Bills list should not be null", bills);
        assertEquals("Should have 3 bills", 3, bills.size());
    }

    @Test
    public void testBillDateRangeQuery() {
        User user = new User("daterange", "pass", "13500135000");
        long userId = userDao.insert(user);

        long now = System.currentTimeMillis();
        long yesterday = now - 86400000L;

        BillEntity billToday = new BillEntity();
        billToday.setUserId(userId);
        billToday.setAmount(50.0);
        billToday.setType(false);
        billToday.setCategory("餐饮");
        billToday.setDate(now);
        billDao.insert(billToday);

        BillEntity billYesterday = new BillEntity();
        billYesterday.setUserId(userId);
        billYesterday.setAmount(100.0);
        billYesterday.setType(false);
        billYesterday.setCategory("交通");
        billYesterday.setDate(yesterday);
        billDao.insert(billYesterday);

        // Query only today's bills
        List<BillEntity> todayBills = billDao.findByDateRangeSync(userId, now - 1000, now + 1000);
        assertEquals("Should have 1 bill today", 1, todayBills.size());
        assertEquals(50.0, todayBills.get(0).getAmount(), 0.001);
    }

    @Test
    public void testBillTotalCalculation() {
        User user = new User("totalcalc", "pass", "13400134000");
        long userId = userDao.insert(user);

        long now = System.currentTimeMillis();

        // Add income
        BillEntity income1 = new BillEntity();
        income1.setUserId(userId);
        income1.setType(true);
        income1.setAmount(5000.0);
        income1.setCategory("工资");
        income1.setDate(now);
        billDao.insert(income1);

        BillEntity income2 = new BillEntity();
        income2.setUserId(userId);
        income2.setType(true);
        income2.setAmount(1000.0);
        income2.setCategory("兼职");
        income2.setDate(now);
        billDao.insert(income2);

        // Add expense
        BillEntity expense1 = new BillEntity();
        expense1.setUserId(userId);
        expense1.setType(false);
        expense1.setAmount(200.0);
        expense1.setCategory("餐饮");
        expense1.setDate(now);
        billDao.insert(expense1);

        // Verify totals
        Double totalIncome = billDao.getTotalByDateRangeAndTypeSync(userId, 0, Long.MAX_VALUE, true);
        assertNotNull("Total income should not be null", totalIncome);
        assertEquals(6000.0, totalIncome, 0.001);

        Double totalExpense = billDao.getTotalByDateRangeAndTypeSync(userId, 0, Long.MAX_VALUE, false);
        assertNotNull("Total expense should not be null", totalExpense);
        assertEquals(200.0, totalExpense, 0.001);
    }

    @Test
    public void testEmptyDatabase() {
        List<BillEntity> bills = billDao.findByDateRangeSync(999, 0, Long.MAX_VALUE);
        assertNotNull("Empty query should return empty list", bills);
        assertTrue("Empty result should have size 0", bills.isEmpty());
    }
}
