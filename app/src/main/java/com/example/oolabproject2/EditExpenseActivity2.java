package com.example.oolabproject2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.oolabproject2.ExpenseModel.Expense;

import java.util.Date;

public class EditExpenseActivity2 extends AppCompatActivity {

    private EditText mDescriptionEditText;
    private EditText mAmountEditText;
    private Button saveButton;
    private Button dateButton;

    private TextView expenseType;
    private Expense expense;
    private Date date;
    private boolean isIncome = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_expense2);

//        date = (getIntent().hasExtra("date")) ? new Date(getIntent().getLongExtra(("date"),System.currentTimeMillis())) : new Date(System.currentTimeMillis());
//
//        mDescriptionEditText = (EditText)findViewById(R.id.description_edittext);
//        mAmountEditText = (EditText)findViewById(R.id.amount_edittext);
//        dateButton = (Button)findViewById(R.id.date_button);
//
//        if(!getIntent().getBooleanExtra("isEdit",false)) {
//            setTitle("Add Expense");
//            expense = null;
//        }
//        else {
//            expense = getIntent().getParcelableExtra("expense");
//            isIncome = expense.isRevenue();
//            date = expense.getDate();
//
//            setTitle(isIncome ? R.string.title_activity_edit_income : R.string.title_activity_edit_expense);
//        }
//
//        setUpButtons();
//        setUpTextViews();
//        setUpDateButton();

    }
}

//    private void setUpButtons()
//    {
//        expenseType = (TextView) findViewById(R.id.expense_type_tv);
//        saveButton = (Button)findViewById(R.id.save_button);
//
//        SwitchCompat expenseTypeSwitch = (SwitchCompat) findViewById(R.id.expense_type_switch);
//        expenseTypeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
//        {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
//            {
//                isIncome = isChecked;
//                setExpenseTypeTextViewAndLayout();
//            }
//        });
//
//        // Init value to checked if already a revenue (can be true if we are editing an expense)
//        if(isIncome)
//        {
//            expenseTypeSwitch.setChecked(true);
//            setExpenseTypeTextViewAndLayout();
//        }
//
//        saveButton = (Button) findViewById(R.id.save_button);
//        saveButton.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
//                if (validateInputs())
//                {
//                    double value = Double.parseDouble(mAmountEditText.getText().toString());
//
//                    Expense expenseToSave;
//                    if (expense == null)
//                    {
//                        // TODO check (-value)
//                        expenseToSave = new Expense(mDescriptionEditText.getText().toString(), isIncome ? -value : value, date);
//                    }
//                    else
//                    {
//                        expenseToSave = expense;
//                        expenseToSave.setTitle(mDescriptionEditText.getText().toString());
//                        expenseToSave.setAmount(isIncome ? -value : value);
//                        expenseToSave.setDate(date);
//                    }
//
//                    boolean b = db.persistExpense(expenseToSave);
//                    System.out.println("Expense Saved or not : " + b);
//                    if(b)
//                        Toast.makeText(EditExpenseActivity.this, getExpenseType(), Toast.LENGTH_SHORT).show();
//
//                    setResult(RESULT_OK);
//
//                    finish();
//                }
//            }
//        });
//    }
//
//}
