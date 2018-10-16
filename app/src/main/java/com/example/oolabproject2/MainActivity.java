package com.example.oolabproject2;

import android.content.Intent;
import android.os.Bundle;

import com.example.oolabproject2.ExpenseModel.ExpensesRecyclerViewAdapter;
import com.example.oolabproject2.calendar.CalendarFragment;
import com.example.oolabproject2.db.DB;
import com.example.oolabproject2.helper.CurrencyHelper;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    RecyclerView recyclerView;
    CalendarFragment caldroidFragment;
    View noExpenseView;
    DB db;
    TextView budgetLineAmount;
    ExpensesRecyclerViewAdapter expensesViewAdapter;

    public static final int ADD_EXPENSE_ACTIVITY_CODE = 101;
    public static final String INTENT_EXPENSE_DELETED = "intent.expense.deleted";
    public static final String INTENT_RECURRING_EXPENSE_DELETED = "intent.expense.monthly.deleted";
    public static final String INTENT_SHOW_WELCOME_SCREEN = "intent.welcomscreen.show";
    private static final String RECYCLE_VIEW_SAVED_DATE = "recycleViewSavedDate";
    public static final String INTENT_SHOW_ADD_EXPENSE = "intent.addexpense.show";
    public final static String INTENT_SHOW_ADD_RECURRING_EXPENSE = "intent.addrecurringexpense.show";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        System.out.println("Inside On Create2");

        db = new DB(getApplicationContext());

        System.out.println("Inside On Create3");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        final FloatingActionMenu menu = (FloatingActionMenu)findViewById(R.id.fam);

        FloatingActionButton Addfab = (FloatingActionButton)findViewById(R.id.add_fab);
        Addfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(MainActivity.this,EditExpenseActivity.class);
                intent1.putExtra("isEdit",false);

                startActivity(intent1);
//                menu.collapse();
            }
        });

        FloatingActionButton AddRecurringfab = (FloatingActionButton)findViewById(R.id.add_recurring_fab);
        AddRecurringfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(MainActivity.this,EditRecurringExpense.class);
                intent2.putExtra("isEdit",false);
                startActivity(intent2);
            }
        });

        System.out.println("Inside On Create1");
//        Log.e("vjbjavbjsb","Inside On Create log1");

        recyclerView = findViewById(R.id.expenseRecyclerView);
        noExpenseView = findViewById(R.id.emptyExpensesRecyclerViewPlaceholder);
        budgetLineAmount = (TextView)findViewById(R.id.budgetAmount);

        initialiseCalendarView();
        initRecycleView(savedInstanceState);
    }

    private void initRecycleView(Bundle savedInstanceState) {
        recyclerView = (RecyclerView) findViewById(R.id.expenseRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Date date = new Date();
        if( savedInstanceState != null && savedInstanceState.containsKey(RECYCLE_VIEW_SAVED_DATE) )
        {
            Date savedDate = (Date) savedInstanceState.getSerializable(RECYCLE_VIEW_SAVED_DATE);
            if ( savedDate != null )
            {
                date = savedDate;
            }
        }

        System.out.println("Inside InitRecycleView");
        Log.e(MainActivity.class.getCanonicalName(),"Inside InitRecycleView");

        expensesViewAdapter = new ExpensesRecyclerViewAdapter(this, db, date);
        recyclerView.setAdapter(expensesViewAdapter);

        System.out.println("Outside InitRecycleView");

        refreshRecyclerViewForDate(date);
        updateBalanceDisplayForDay(date);

        System.out.println("Completed OnCreate");
    }

    private void updateBalanceDisplayForDay(@NonNull Date day)
    {
        double balance = 0; // Just to keep a positive number if balance == 0
        balance -= db.getBalanceForDay(day);

        SimpleDateFormat format = new SimpleDateFormat(getResources().getString(R.string.account_balance_date_format), Locale.getDefault());

        String formatted = getResources().getString(R.string.account_balance_format, format.format(day));
        if( formatted.endsWith(".:") ) //FIXME it's ugly!!
        {
            formatted = formatted.substring(0, formatted.length() - 2) + ":"; // Remove . at the end of the month (ex: nov.: -> nov:)
        }
        else if( formatted.endsWith(". :") ) //FIXME it's ugly!!
        {
            formatted = formatted.substring(0, formatted.length() - 3) + " :"; // Remove . at the end of the month (ex: nov. : -> nov :)
        }

        budgetLineAmount.setText(Double.toString(balance));

//        if( balance <= 0 )
//        {
//            budgetLineContainer.setBackgroundResource(R.color.budget_red);
//        }
//        else if( balance < Parameters.getInstance(getApplicationContext()).getInt(ParameterKeys.LOW_MONEY_WARNING_AMOUNT, EasyBudget.DEFAULT_LOW_MONEY_WARNING_AMOUNT) )
//        {
//            budgetLineContainer.setBackgroundResource(R.color.budget_orange);
//        }
//        else
//        {
//            budgetLineContainer.setBackgroundResource(R.color.budget_green);
//        }
    }


    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }

    private CaldroidListener getCaldroidListener() {
        final CaldroidListener listener = new CaldroidListener() {
            @Override
            public void onSelectDate(Date date, View view) {
                refreshAllForDate(date);
            }

            @Override
            public void onLongClickDate(Date date, View view) {

                // TODO set this method

                Intent startAddIncomeIntent = new Intent(MainActivity.this, EditExpenseActivity.class);
                startAddIncomeIntent.putExtra("date",date.getTime());


            }
        };
        return listener;
    }

    private void refreshAllForDate(@NonNull Date date)
    {
        refreshRecyclerViewForDate(date);
        updateBalanceDisplayForDay(date);
        caldroidFragment.setSelectedDates(date, date);
        caldroidFragment.refreshView();
    }

    private void refreshRecyclerViewForDate(@NonNull Date date)
    {
        expensesViewAdapter.setDate(date, db);

        if( db.hasExpensesForDay(date) )
        {
            recyclerView.setVisibility(View.VISIBLE);
            noExpenseView.setVisibility(View.GONE);
        }
        else
        {
            recyclerView.setVisibility(View.GONE);
            noExpenseView.setVisibility(View.VISIBLE);
        }
        System.out.println("Outside refreshRecyclerViewForDate");
    }

    private void initialiseCalendarView() {
        caldroidFragment = new CalendarFragment();
        Bundle args = new Bundle();
        Calendar cal = Calendar.getInstance();
        args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
        args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
        args.putBoolean(CaldroidFragment.ENABLE_SWIPE,true);
        args.putBoolean(CaldroidFragment.ENABLE_CLICK_ON_DISABLED_DATES, false);
        args.putBoolean(CaldroidFragment.SQUARE_TEXT_VIEW_CELL, false);
        args.putInt(CaldroidFragment.THEME_RESOURCE, R.style.caldroid_style);
        caldroidFragment.setArguments(args);

        caldroidFragment.setCaldroidListener(getCaldroidListener());

        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.calendarView, caldroidFragment);
        t.commit();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.emi_calculator) {
            Intent myIntent = new Intent(MainActivity.this, emiCalculator.class);
            //myIntent.putExtra("key", value); //Optional parameters
            MainActivity.this.startActivity(myIntent);

        } /*else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
