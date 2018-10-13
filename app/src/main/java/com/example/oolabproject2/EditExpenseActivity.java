package com.example.oolabproject2;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.oolabproject2.ExpenseModel.Expense;
import com.example.oolabproject2.db.DB;
//import com.example.oolabproject2.db.DB;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditExpenseActivity extends AppCompatActivity {

    private EditText mDescriptionEditText;
    private EditText mAmountEditText;
    private Button saveButton;
    private Button dateButton;

    private DB db;

    private TextView expenseType;

    private Expense expense;

    private Date date;

    private boolean isIncome = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_expense);

        date = (getIntent().hasExtra("date")) ? new Date(getIntent().getLongExtra(("date"),System.currentTimeMillis())) : new Date(System.currentTimeMillis());

        mDescriptionEditText = (EditText)findViewById(R.id.description_edittext);
        mAmountEditText = (EditText)findViewById(R.id.amount_edittext);
        dateButton = (Button)findViewById(R.id.date_button);

        if(!getIntent().getBooleanExtra("isEdit",false)) {
            setTitle("Add Expense");
            expense = null;
        } else {
            expense = getIntent().getParcelableExtra("expense");
            isIncome = expense.isRevenue();
            date = expense.getDate();

            System.out.println("I am here!!!!");

            setTitle(isIncome ? R.string.title_activity_edit_income : R.string.title_activity_edit_expense);
        }

        setUpButtons();
        setUpTextViews();
        setUpDateButton();

        db = new DB(getApplicationContext());
    }

    private void setUpDateButton()
    {
        dateButton = (Button) findViewById(R.id.date_button);

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

    private void updateDateButtonDisplay()
    {
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd yyyy", Locale.getDefault());
        dateButton.setText(formatter.format(date));
    }



    private void setUpTextViews() {
        if( expense != null )
        {
            mDescriptionEditText.setText(expense.getTitle());
            mDescriptionEditText.setSelection(mDescriptionEditText.getText().length()); // Put focus at the end of the text
        }
//        UIHelper.preventUnsupportedInputForDecimals(amountEditText);

        if( expense != null )
        {
            mAmountEditText.setText(isIncome ? Double.toString(-expense.getAmount()) : Double.toString(expense.getAmount()));
        }
    }

    private boolean isEdit()
    {
        return getIntent().hasExtra("expense");
    }

    private void setUpButtons()
    {
        expenseType = (TextView) findViewById(R.id.expense_type_tv);
        saveButton = (Button)findViewById(R.id.save_button);

        SwitchCompat expenseTypeSwitch = (SwitchCompat) findViewById(R.id.expense_type_switch);
        expenseTypeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                isIncome = isChecked;
                setExpenseTypeTextViewAndLayout();
            }
        });

        // Init value to checked if already a revenue (can be true if we are editing an expense)
        if(isIncome)
        {
            expenseTypeSwitch.setChecked(true);
            setExpenseTypeTextViewAndLayout();
        }

        saveButton = (Button) findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (validateInputs())
                {
                    Double value = Double.parseDouble(mAmountEditText.getText().toString());
                    System.out.println("value : "+value);

                    Expense expenseToSave;
                    if (expense == null)
                    {
                        // TODO check (-value)
                        expenseToSave = new Expense(mDescriptionEditText.getText().toString(), isIncome ? -value : value, date);
                    }
                    else
                    {
                        expenseToSave = expense;
                        expenseToSave.setTitle(mDescriptionEditText.getText().toString());
                        expenseToSave.setAmount(isIncome ? -value : value);
                        expenseToSave.setDate(date);
                    }

                    System.out.println("Expense1 \n" + "Description : " + expenseToSave.getTitle() + "\n Date : " + expenseToSave.getDate().toString());
                    Log.e(EditExpenseActivity.class.getCanonicalName(),"Expense1 \n" + "Description : " + expenseToSave.getTitle() + "\n Date : " + expenseToSave.getDate().toString() + "\nAmount : " + expenseToSave.getAmount());

                    boolean b = db.persistExpense(expenseToSave);
                    System.out.println("Expense Saved or not : " + b);
                    if(b)
                        Toast.makeText(EditExpenseActivity.this, getExpenseType(), Toast.LENGTH_SHORT).show();

                    setResult(RESULT_OK);

                    finish();
                }
            }
        });
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
                double value = Double.valueOf(amount);
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

    private void setExpenseTypeTextViewAndLayout()
    {
        if(isIncome)
        {
            expenseType.setText(R.string.income);
            expenseType.setTextColor(ContextCompat.getColor(this, R.color.budget_green));

            setTitle(isEdit() ? R.string.title_activity_edit_income : R.string.title_activity_add_income);
        }
        else
        {
            expenseType.setText(R.string.payment);
            expenseType.setTextColor(ContextCompat.getColor(this, R.color.budget_red));

            setTitle(isEdit() ? R.string.title_activity_edit_expense : R.string.title_activity_add_expense);
        }
    }

    private String getExpenseType() {
        if(isIncome) {
            return (isEdit() ? "Income Edited" : "New Income Added");
        } else {
            return (isEdit() ? "Expense Edited" : "New Expense Added");
        }
    }

}
