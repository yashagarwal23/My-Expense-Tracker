package com.example.oolabproject2;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.oolabproject2.ExpenseModel.Expense;
import com.example.oolabproject2.ExpenseModel.RecurringExpense;
import com.example.oolabproject2.db.DB;
import com.example.oolabproject2.helper.RecurringExpenseType;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

public class EditRecurringExpense extends AppCompatActivity {

    private EditText mDescriptionEditText;
    private EditText mAmountEditText;
    private Button saveButton;
    private Button dateButton;

    private DB db;

    private TextView expenseType;

    private RecurringExpense expense;

    private Date date;
    Spinner spinner;

    private boolean isIncome = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recurring_expense);
        db = new DB(getApplicationContext());


        date = new Date(getIntent().getLongExtra(("date"),System.currentTimeMillis()));

        mDescriptionEditText = (EditText)findViewById(R.id.description_edittext);
        mAmountEditText = (EditText)findViewById(R.id.amount_edittext);
        dateButton = (Button)findViewById(R.id.date_button);

        spinner = (Spinner)findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.interval_spinner_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        if(!getIntent().getBooleanExtra("isEdit",false)) {
            setTitle("Add Recurring Expense");
        }
        else {
            expense = getIntent().getParcelableExtra("expense");
            isIncome = expense.isRevenue();
            date = expense.getRecurringDate();

            setTitle(isIncome ? R.string.title_activity_edit_income : R.string.title_activity_edit_expense);
        }

        setUpButtons();
        setUpTextViews();
        setUpDateButton();
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }

    private void setUpTextViews()
    {
//        ((TextInputLayout) findViewById(R.id.amount_inputlayout)).setHint(getResources().getString(R.string.amount, CurrencyHelper.getUserCurrency(this).getSymbol()));

        if( expense != null )
        {
            mDescriptionEditText.setText(expense.getTitle());
            mDescriptionEditText.setSelection(mDescriptionEditText.getText().length()); // Put focus at the end of the text
        }
        //        UIHelper.preventUnsupportedInputForDecimals(mAmountEditText);

        if( expense != null )
        {
            mAmountEditText.setText(Double.toString(expense.getAmount()));
        }

        if( expense != null )
        {
            spinner.setSelection(expense.getType().ordinal(), false);
        }
        else
        {
            spinner.setSelection(2, false);
        }
    }

    private void setExpenseTypeTextViewLayout()
    {
        if( isIncome )
        {
            expenseType.setText(R.string.income);
            expenseType.setTextColor(ContextCompat.getColor(this, R.color.budget_green));

            setTitle((isEdit())?"Edit Recurring Income" : "Add Recurring Income");
        }
        else
        {
            expenseType.setText(R.string.payment);
            expenseType.setTextColor(ContextCompat.getColor(this, R.color.budget_red));

            setTitle((isEdit()) ? "Edit Recurring Expense" : "Add Recurring Expense");
        }
    }

    private void setUpButtons()
    {
        expenseType = (TextView) findViewById(R.id.expense_type_tv);

        SwitchCompat expenseTypeSwitch = (SwitchCompat) findViewById(R.id.expense_type_switch);
        expenseTypeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                isIncome = isChecked;
                setExpenseTypeTextViewLayout();
            }
        });

        // Init value to checked if already a revenue (can be true if we are editing an expense)
        if( isIncome )
        {
            expenseTypeSwitch.setChecked(true);
            setExpenseTypeTextViewLayout();
        }

        saveButton = (Button) findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if( validateInputs() )
                {
                    double value = Double.parseDouble(mAmountEditText.getText().toString());

                    RecurringExpense expense = new RecurringExpense(mDescriptionEditText.getText().toString(), isIncome? -value : value, date, getIntervalTypeFromSpinner(spinner.getSelectedItemPosition()));

                    new SaveRecurringExpenseTask().execute(expense);
                }
            }
        });
    }

    private RecurringExpenseType getIntervalTypeFromSpinner(int selectedItemPosition) {
        switch (selectedItemPosition)
        {
            case 0:
                return RecurringExpenseType.WEEKLY;
            case 1:
                return RecurringExpenseType.BI_WEEKLY;
            case 2:
                return RecurringExpenseType.MONTHLY;
            case 3:
                return RecurringExpenseType.YEARLY;
        }

        return null;
    }

    private boolean validateInputs()
    {
        boolean ok = true;

        String description = mDescriptionEditText.getText().toString();
        if( description.trim().isEmpty() )
        {
            mDescriptionEditText.setError(getResources().getString(R.string.no_description_error));
            ok = false;
        }

        String amount = mAmountEditText.getText().toString();
        if( amount.trim().isEmpty() )
        {
            mAmountEditText.setError(getResources().getString(R.string.no_amount_error));
            ok = false;
        }
        else
        {
            try
            {
                double value = Double.parseDouble(amount);
                if( value <= 0 )
                {
                    mAmountEditText.setError(getResources().getString(R.string.negative_amount_error));
                    ok = false;
                }
            }
            catch(Exception e)
            {
                mAmountEditText.setError(getResources().getString(R.string.invalid_amount));
                ok = false;
            }
        }

        return ok;
    }

    private boolean isEdit()
    {
        return getIntent().hasExtra("expense");
    }

    private void setUpDateButton()
    {
        dateButton = (Button) findViewById(R.id.date_button);
        removeButtonBorder(); // Remove border on lollipop

        updateDateButtonDisplay();

        dateButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DatePickerDialogFragment fragment = new DatePickerDialogFragment(date, new DatePickerDialog.OnDateSetListener()
                {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
                    {
                        Calendar cal = Calendar.getInstance();

                        cal.set(Calendar.YEAR, year);
                        cal.set(Calendar.MONTH, monthOfYear);
                        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        date = cal.getTime();
                        updateDateButtonDisplay();
                    }
                });

                fragment.show(getSupportFragmentManager(), "datePicker");

            }
        });
    }
    private void removeButtonBorder() {
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP )
        {
            dateButton.setOutlineProvider(null);
        }
    }
    private void updateDateButtonDisplay()
    {
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd yyyy", Locale.getDefault());
        dateButton.setText(formatter.format(date));
    }

    private class SaveRecurringExpenseTask extends AsyncTask<RecurringExpense, Integer, Boolean>
    {
        /**
         * Dialog used to display loading to the user
         */
        private ProgressDialog dialog;

        @Override
        protected Boolean doInBackground(RecurringExpense... expenses)
        {
            for (RecurringExpense expense : expenses)
            {
                boolean inserted = db.addRecurringExpense(expense);
                if( !inserted )
                {
//                    Logger.getLogger(false, "Error while inserting recurring expense into DB: addRecurringExpense returned false");
                    return false;
                }

                if( !flattenExpensesForRecurringExpense(expense) )
                {
//                    Logger.getLogger(false, "Error while flattening expenses for recurring expense: flattenExpensesForRecurringExpense returned false");
                    return false;
                }
            }

            return true;
        }

        private boolean flattenExpensesForRecurringExpense(@NonNull RecurringExpense expense)
        {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            switch (expense.getType())
            {
                case WEEKLY:
                    // Add up to 5 years of expenses
                    for (int i = 0; i < 12*4*5; i++)
                    {
                        boolean expenseInserted = db.persistExpense(new Expense(expense.getTitle(), expense.getAmount(), cal.getTime(), expense));
                        if (!expenseInserted)
                        {
//                            Logger.error(false, "Error while inserting expense for recurring expense into DB: persistExpense returned false");
                            return false;
                        }

                        cal.add(Calendar.WEEK_OF_YEAR, 1);

//                        TODO if we have an end date
//                        if (dateEnd != null && cal.getTime().after(dateEnd)) // If we have an end date, stop to that one
//                        {
//                            break;
//                        }
                    }
                    break;
                case BI_WEEKLY:
                    // Add up to 5 years of expenses
                    for (int i = 0; i < 12*4*5; i++)
                    {
                        boolean expenseInserted = db.persistExpense(new Expense(expense.getTitle(), expense.getAmount(), cal.getTime(), expense));
                        if (!expenseInserted)
                        {
//                            Logger.error(false, "Error while inserting expense for recurring expense into DB: persistExpense returned false");
                            return false;
                        }

                        cal.add(Calendar.WEEK_OF_YEAR, 2);
//                        TODO if we have an end date
//                        if (dateEnd != null && cal.getTime().after(dateEnd)) // If we have an end date, stop to that one
//                        {
//                            break;
//                        }
                    }
                    break;
                case MONTHLY:
                    // Add up to 10 years of expenses
                    for (int i = 0; i < 12 * 10; i++)
                    {
                        boolean expenseInserted = db.persistExpense(new Expense(expense.getTitle(), expense.getAmount(), cal.getTime(), expense));
                        if (!expenseInserted)
                        {
//                            Logger.error(false, "Error while inserting expense for recurring expense into DB: persistExpense returned false");
                            return false;
                        }

                        cal.add(Calendar.MONTH, 1);

//                        TODO if we have an end date

//                        if (dateEnd != null && cal.getTime().after(dateEnd)) // If we have an end date, stop to that one
//                        {
//                            break;
//                        }
                    }
                    break;
                case YEARLY:
                    // Add up to 100 years of expenses
                    for (int i = 0; i < 100; i++)
                    {
                        boolean expenseInserted = db.persistExpense(new Expense(expense.getTitle(), expense.getAmount(), cal.getTime(), expense));
                        if (!expenseInserted)
                        {
//                            Logger.error(false, "Error while inserting expense for recurring expense into DB: persistExpense returned false");
                            return false;
                        }

                        cal.add(Calendar.YEAR, 1);

//                        TODO if we have an end date
//                        if (dateEnd != null && cal.getTime().after(dateEnd)) // If we have an end date, stop to that one
//                        {
//                            break;
//                        }
                    }
                    break;
            }

            return true;
        }

//        @Override
//        protected void onPreExecute()
//        {
//            // Show a ProgressDialog
//            dialog = new ProgressDialog(RecurringExpenseEditActivity.this);
//            dialog.setIndeterminate(true);
//            dialog.setTitle(R.string.recurring_expense_add_loading_title);
//            dialog.setMessage(getResources().getString(isRevenue ? R.string.recurring_income_add_loading_message : R.string.recurring_expense_add_loading_message));
//            dialog.setCanceledOnTouchOutside(false);
//            dialog.setCancelable(false);
//            dialog.show();
//        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            // Dismiss the dialog
            dialog.dismiss();

            if (result)
            {
                setResult(RESULT_OK);
                finish();
            }
            else
            {

//                TODO set up new ALertDialog
//                new AlertDialog.Builder(EditRecurringExpense.this)
//                        .setTitle("ERROR!!")
//                        .setMessage(getResources().getString(R.string.recurring_expense_add_error_message))
//                        .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener()
//                        {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which)
//                            {
//                                dialog.dismiss();
//                            }
//                        })
//                        .show();
            }
        }
    }

}
