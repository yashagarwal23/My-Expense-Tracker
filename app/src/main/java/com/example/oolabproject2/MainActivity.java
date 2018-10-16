package com.example.oolabproject2;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import com.example.oolabproject2.ExpenseModel.Expense;
import com.example.oolabproject2.ExpenseModel.ExpensesRecyclerViewAdapter;
import com.example.oolabproject2.ExpenseModel.RecurringExpense;
import com.example.oolabproject2.calendar.CalendarFragment;
import com.example.oolabproject2.db.DB;
import com.example.oolabproject2.helper.ParameterKeys;
import com.example.oolabproject2.helper.Parameters;
import com.example.oolabproject2.helper.RecurringExpenseDeleteType;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    RecyclerView recyclerView;
    CalendarFragment caldroidFragment;
    View noExpenseView;
    DB db;
    TextView budgetLineAmount;
    ExpensesRecyclerViewAdapter expensesViewAdapter;
    private BroadcastReceiver receiver;
    RelativeLayout relativeLayout;

    private static final int ACTION_SNACKBAR_LENGTH = 5000;
    public static final int ADD_EXPENSE_ACTIVITY_CODE = 101;
    public static final String INTENT_EXPENSE_DELETED = "intent.expense.deleted";
    public static final String INTENT_RECURRING_EXPENSE_DELETED = "intent.expense.monthly.deleted";
    public static final String INTENT_SHOW_WELCOME_SCREEN = "intent.welcomscreen.show";
    private static final String RECYCLE_VIEW_SAVED_DATE = "recycleViewSavedDate";
    public static final String INTENT_SHOW_ADD_EXPENSE = "intent.addexpense.show";
    public final static String INTENT_SHOW_ADD_RECURRING_EXPENSE = "intent.addrecurringexpense.show";
    public static final String INTENT_REDIRECT_TO_PREMIUM_EXTRA = "intent.extra.premiumshow";
    public static final String INTENT_REDIRECT_TO_SETTINGS_EXTRA = "intent.extra.redirecttosettings";

    public final static String CENTER_X_KEY           = "centerX";
    public final static String CENTER_Y_KEY           = "centerY";
    public final static String ANIMATE_TRANSITION_KEY = "animate";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = new DB(getApplicationContext());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        final FloatingActionMenu menu = (FloatingActionMenu)findViewById(R.id.fam);
        menu.setClosedOnTouchOutside(true);

        FloatingActionButton Addfab = (FloatingActionButton)findViewById(R.id.add_fab);
        Addfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(MainActivity.this,EditExpenseActivity.class);
                intent1.putExtra("isEdit",false);

                menu.close(false);
                startActivity(intent1);
            }
        });

        FloatingActionButton AddRecurringfab = (FloatingActionButton)findViewById(R.id.add_recurring_fab);
        AddRecurringfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(MainActivity.this,EditRecurringExpense.class);
                intent2.putExtra("isEdit",false);
                menu.close(false);
                startActivity(intent2);
            }
        });

        System.out.println("Inside On Create1");

        recyclerView = findViewById(R.id.expenseRecyclerView);
        noExpenseView = findViewById(R.id.emptyExpensesRecyclerViewPlaceholder);
        budgetLineAmount = (TextView)findViewById(R.id.budgetAmount);
        relativeLayout = (RelativeLayout)findViewById(R.id.main_activity_parent);

        initialiseCalendarView();
        initRecycleView(savedInstanceState);
        initialiseBroadcastReceiver();
    }

    private void initialiseBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_EXPENSE_DELETED);
        filter.addAction(INTENT_RECURRING_EXPENSE_DELETED);
//        filter.addAction(SelectCurrencyFragment.CURRENCY_SELECTED_INTENT);
        filter.addAction(INTENT_SHOW_WELCOME_SCREEN);
        filter.addAction(Intent.ACTION_VIEW);
        filter.addAction("iabStatusChanged");
//        TODO correct this
//        filter.addAction(EasyBudget.INTENT_IAB_STATUS_CHANGED);

        receiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                if( INTENT_EXPENSE_DELETED.equals(intent.getAction()) )
                {
                    final Expense expense = (Expense) intent.getParcelableExtra("expense");
                    System.out.println("I am deleting expense");
                    if( db.deleteExpense(expense) )
                    {
                        final int position = expensesViewAdapter.removeExpense(expense);
                        updateBalanceDisplayForDay(expensesViewAdapter.getDate());
                        caldroidFragment.refreshView();

                        Snackbar snackbar = Snackbar.make(relativeLayout, expense.isRevenue() ? R.string.income_delete_snackbar_text : R.string.expense_delete_snackbar_text, Snackbar.LENGTH_LONG);
                        snackbar.setAction(R.string.undo, new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                db.persistExpense(expense, true);

                                if( caldroidFragment.getSelectedDate().equals(expense.getDate()) )
                                {
                                    expensesViewAdapter.addExpense(expense, position);
                                }

                                updateBalanceDisplayForDay(caldroidFragment.getSelectedDate());
                                caldroidFragment.refreshView();
                            }
                        });
                        snackbar.setActionTextColor(ContextCompat.getColor(MainActivity.this, R.color.snackbar_action_undo));
                        //noinspection ResourceType
                        snackbar.setDuration(ACTION_SNACKBAR_LENGTH);
                        snackbar.show();
                    }
                    else
                    {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle(R.string.expense_delete_error_title)
                                .setMessage(R.string.expense_delete_error_message)
                                .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                    }

                }
                else if( INTENT_RECURRING_EXPENSE_DELETED.equals(intent.getAction()) )
                {
                    final Expense expense = intent.getParcelableExtra("expense");
                    final RecurringExpenseDeleteType deleteType = RecurringExpenseDeleteType.fromValue(intent.getIntExtra("deleteType", RecurringExpenseDeleteType.ALL.getValue()));

                    if( deleteType == null )
                    {
                        showGenericRecurringDeleteErrorDialog();
                        return;
                    }

                    if( expense.getAssociatedRecurringExpense() == null )
                    {
                        showGenericRecurringDeleteErrorDialog();
                        return;
                    }

                    // Check that if the user wants to delete series before this one, there are actually series to delete
                    if( deleteType == RecurringExpenseDeleteType.TO && !db.hasExpensesForRecurringExpenseBeforeDate(expense.getAssociatedRecurringExpense(), expense.getDate()) )
                    {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle(R.string.recurring_expense_delete_first_error_title)
                                .setMessage(getResources().getString(R.string.recurring_expense_delete_first_error_message))
                                .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        dialog.dismiss();
                                    }
                                })
                                .show();

                        return;
                    }

                    new DeleteRecurringExpenseTask(expense.getAssociatedRecurringExpense(), expense, deleteType,db).execute();
                }
//                TODO complete SelectCurrencyFragment
//                else if( SelectCurrencyFragment.CURRENCY_SELECTED_INTENT.equals(intent.getAction()) )
//                {
//                    refreshAllForDate(expensesViewAdapter.getDate());
//                }
//                TODO implement welcome acitvity
//                else if( INTENT_SHOW_WELCOME_SCREEN.equals(intent.getAction()) )
//                {
//                    Intent startIntent = new Intent(MainActivity.this, WelcomeActivity.class);
//                    ActivityCompat.startActivityForResult(MainActivity.this, startIntent, WELCOME_SCREEN_ACTIVITY_CODE, null);
//                }
                else if( Intent.ACTION_VIEW.equals(intent.getAction()) ) // App invites referrer
                {
                    if( AppInviteReferral.hasReferral(intent) )
                    {
                        updateInvitationStatus(intent);
                    }
                }
//                else if( EasyBudget.INTENT_IAB_STATUS_CHANGED.equals(intent.getAction()) )
//                {
//                    invalidateOptionsMenu();
//                }
            }
        };

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver, filter);

        if( getIntent() != null )
        {
//            TODO implement Settings and Premium
//            openSettingsIfNeeded(getIntent());
            openMonthlyReportIfNeeded(getIntent());
//            openPremiumIfNeeded(getIntent());
            openAddExpenseIfNeeded(getIntent());
            openAddRecurringExpenseIfNeeded(getIntent());
        }
    }

//    private void openSettingsIfNeeded(Intent intent)
//    {
//        if( intent.getBooleanExtra(INTENT_REDIRECT_TO_SETTINGS_EXTRA, false) )
//        {
//            Intent startIntent = new Intent(this, SettingsActivity.class);
//            ActivityCompat.startActivityForResult(MainActivity.this, startIntent, SETTINGS_SCREEN_ACTIVITY_CODE, null);
//        }
//    }
    private void openMonthlyReportIfNeeded(Intent intent)
    {
        try
        {
            Uri data = intent.getData();
            if( data != null && "true".equals(data.getQueryParameter("monthly")) )
            {
                Intent startIntent = new Intent(this, MonthlyReportActivity.class);
                startIntent.putExtra(MonthlyReportActivity.FROM_NOTIFICATION_EXTRA, true);
                ActivityCompat.startActivity(MainActivity.this, startIntent, null);
            }
        }
        catch (Exception e)
        {
        }
    }

//    private void openPremiumIfNeeded(Intent intent)
//    {
//        if( intent.getBooleanExtra(INTENT_REDIRECT_TO_PREMIUM_EXTRA, false) )
//        {
//            Intent startIntent = new Intent(this, SettingsActivity.class);
//            startIntent.putExtra(SettingsActivity.SHOW_PREMIUM_INTENT_KEY, true);
//
//            ActivityCompat.startActivityForResult(this, startIntent, SETTINGS_SCREEN_ACTIVITY_CODE, null);
//        }
//    }
    private void openAddExpenseIfNeeded(Intent intent)
    {
        if( intent.getBooleanExtra(INTENT_SHOW_ADD_EXPENSE, false) )
        {
            Intent startIntent = new Intent(this, EditExpenseActivity.class);
            startIntent.putExtra("date", new Date().getTime());

            ActivityCompat.startActivityForResult(this, startIntent, ADD_EXPENSE_ACTIVITY_CODE, null);
        }
    }
    private void openAddRecurringExpenseIfNeeded(Intent intent)
    {
        if( intent.getBooleanExtra(INTENT_SHOW_ADD_RECURRING_EXPENSE, false) )
        {
            Intent startIntent = new Intent(this, EditRecurringExpense.class);
            startIntent.putExtra("dateStart", new Date().getTime());

            ActivityCompat.startActivityForResult(this, startIntent, ADD_EXPENSE_ACTIVITY_CODE, null);
        }
    }

    private void updateInvitationStatus(Intent intent)
    {
        try
        {
            String invitationId = AppInviteReferral.getInvitationId(intent);
            if( invitationId != null && !invitationId.isEmpty() )
            {
                String existingId = Parameters.getInstance(getApplicationContext()).getString(ParameterKeys.INVITATION_ID);
                if( existingId == null )
                {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.app_invite_welcome_title)
                            .setMessage(R.string.app_invite_welcome_message)
                            .setPositiveButton(R.string.app_invite_welcome_cta, new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {

                                }
                            })
                            .show();
                }

                Parameters.getInstance(getApplicationContext()).putString(ParameterKeys.INVITATION_ID, invitationId);
//                ((EasyBudget) getApplication()).trackInvitationId(invitationId);
            }

            Uri data = intent.getData();
            String source = data.getQueryParameter("type");
            String referrer = data.getQueryParameter("referrer");

            if( source != null )
            {
                Parameters.getInstance(getApplicationContext()).putString(ParameterKeys.INSTALLATION_SOURCE, source);
            }

            if( referrer != null )
            {
                Parameters.getInstance(getApplicationContext()).putString(ParameterKeys.INSTALLATION_REFERRER, referrer);
            }
        }
        catch (Exception e)
        {
        }
    }

    private void showGenericRecurringDeleteErrorDialog()
    {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.recurring_expense_delete_error_title)
                .setMessage(R.string.recurring_expense_delete_error_message)
                .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                })
                .show();
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

        if( balance <= 0 )
        {
            budgetLineAmount.setBackgroundResource(R.color.budget_red);
        }
        else if( balance < Parameters.getInstance(getApplicationContext()).getInt(ParameterKeys.LOW_MONEY_WARNING_AMOUNT, 100) )
        {
            budgetLineAmount.setBackgroundResource(R.color.budget_orange);
        }
        else
        {
            budgetLineAmount.setBackgroundResource(R.color.budget_green);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        refreshAllForDate(new Date());
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


    public class DeleteRecurringExpenseTask extends AsyncTask<Void, Integer, Boolean>
    {
        /**
         * Dialog used to display loading to the user
         */
        private ProgressDialog dialog;

        /**
         * The expense deleted by the user
         */
        private final Expense expense;
        /**
         * The recurring expense associated with the expense deleted by the user
         */
        private final RecurringExpense recurringExpense;
        /**
         * Type of delete
         */
        private final RecurringExpenseDeleteType deleteType;

        /**
         * Expenses to restore if delete is successful and user cancels it
         */
        @Nullable
        private List<Expense> expensesToRestore;
        /**
         * Recurring expense to restore if delete is successful and user cancels it
         */
        @Nullable
        private RecurringExpense recurringExpenseToRestore;

        // ------------------------------------------->

        private DB db;

        public DeleteRecurringExpenseTask(@NonNull RecurringExpense recurringExpense, @NonNull Expense expense, @NonNull RecurringExpenseDeleteType deleteType, DB db)
        {
            this.recurringExpense = recurringExpense;
            this.expense = expense;
            this.deleteType = deleteType;
            this.db = db;
        }

        // ------------------------------------------->

        @Override
        protected Boolean doInBackground(Void... nothing)
        {
            switch (deleteType)
            {
                case ALL:
                {
                    recurringExpenseToRestore = recurringExpense;
                    expensesToRestore = db.getAllExpenseForRecurringExpense(recurringExpense);

                    boolean expensesDeleted = db.deleteAllExpenseForRecurringExpense(recurringExpense);
                    if( !expensesDeleted )
                    {
                        return false;
                    }

                    boolean recurringExpenseDeleted = db.deleteRecurringExpense(recurringExpense);
                    if( !recurringExpenseDeleted )
                    {
                        return false;
                    }

                    break;
                }
                case FROM:
                {
                    expensesToRestore = db.getAllExpensesForRecurringExpenseFromDate(recurringExpense, expense.getDate());

                    boolean expensesDeleted = db.deleteAllExpenseForRecurringExpenseFromDate(recurringExpense, expense.getDate());
                    if( !expensesDeleted )
                    {
                        return false;
                    }

                    break;
                }
                case TO:
                {
                    expensesToRestore = db.getAllExpensesForRecurringExpenseBeforeDate(recurringExpense, expense.getDate());

                    boolean expensesDeleted = db.deleteAllExpenseForRecurringExpenseBeforeDate(recurringExpense, expense.getDate());
                    if( !expensesDeleted )
                    {
                        return false;
                    }

                    break;
                }
                case ONE:
                {
                    expensesToRestore = new ArrayList<>(1);
                    expensesToRestore.add(expense);

                    boolean expenseDeleted = db.deleteExpense(expense);
                    if( !expenseDeleted )
                    {
                        return false;
                    }

                    break;
                }
            }

            return true;
        }

        @Override
        protected void onPreExecute()
        {
            // Show a ProgressDialog
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setIndeterminate(true);
            dialog.setTitle(R.string.recurring_expense_delete_loading_title);
            dialog.setMessage(getResources().getString(R.string.recurring_expense_delete_loading_message));
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            // Dismiss the dialog
            dialog.dismiss();

            if (result)
            {
                // Refresh and show confirm snackbar
                refreshAllForDate(expensesViewAdapter.getDate());
                Snackbar snackbar = Snackbar.make(relativeLayout, R.string.recurring_expense_delete_success_message, Snackbar.LENGTH_LONG);

                if( expensesToRestore != null ) // just in case..
                {
                    snackbar.setAction(R.string.undo, new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            new CancelDeleteRecurringExpenseTask(expensesToRestore, recurringExpenseToRestore).execute();
                        }
                    });
                }

                snackbar.setActionTextColor(ContextCompat.getColor(MainActivity.this, R.color.snackbar_action_undo));

                //noinspection ResourceType
                snackbar.setDuration(ACTION_SNACKBAR_LENGTH);
                snackbar.show();
            }
            else
            {
                showGenericRecurringDeleteErrorDialog();
            }
        }
    }
    private class CancelDeleteRecurringExpenseTask extends AsyncTask<Void, Void, Boolean>
    {
        /**
         * Dialog used to display loading to the user
         */
        private ProgressDialog dialog;

        /**
         * List of expenses to restore
         */
        private final List<Expense> expensesToRestore;
        /**
         * Recurring expense to restore (will be null if delete type != ALL)
         */
        private final RecurringExpense recurringExpenseToRestore;

        // ------------------------------------------->

        /**
         *
         * @param expensesToRestore The deleted expenses to restore
         * @param recurringExpenseToRestore the deleted recurring expense to restore
         */
        private CancelDeleteRecurringExpenseTask(@NonNull List<Expense> expensesToRestore, @Nullable RecurringExpense recurringExpenseToRestore)
        {
            this.expensesToRestore = expensesToRestore;
            this.recurringExpenseToRestore = recurringExpenseToRestore;
        }

        // ------------------------------------------->

        @Override
        protected void onPreExecute()
        {
            // Show a ProgressDialog
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setIndeterminate(true);
            dialog.setTitle(R.string.recurring_expense_restoring_loading_title);
            dialog.setMessage(getResources().getString(R.string.recurring_expense_restoring_loading_message));
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params)
        {
            if( recurringExpenseToRestore != null )
            {
                if( !db.addRecurringExpense(recurringExpenseToRestore) )
                {
                    return false;
                }
            }

            for(Expense expense: expensesToRestore)
            {
                if( !db.persistExpense(expense, true) )
                {
                    return false;
                }
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            // Dismiss the dialog
            dialog.dismiss();

            if (result)
            {
                // Refresh and show confirm snackbar
                refreshAllForDate(expensesViewAdapter.getDate());
                Snackbar.make(relativeLayout, R.string.recurring_expense_restored_success_message, Snackbar.LENGTH_LONG).show();
            }
            else
            {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.recurring_expense_restore_error_title)
                        .setMessage(getResources().getString(R.string.recurring_expense_restore_error_message))
                        .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        }
    }
}
