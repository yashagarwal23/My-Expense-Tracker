package com.example.oolabproject2;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class emiCalculator extends AppCompatActivity {

    public static float amount;
    public static float rate;
    public static float tenure_;
    public  static float total;
    EditText mEdit1;
    EditText mEdit2;
    EditText mEdit3;
    TextView totatPayOut;
    TextView totalIntPayOut;
    TextView loanEmiOut;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emi_calculator);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /*getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finish();
            }
        });*/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //getActionBar().setTitle("EMI CALCULATOR");

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        /*TextView textView = (TextView) findViewById(R.id.heading);
        textView.setText("EMI CALCULATOR");*/
        TextView loanAmt = (TextView) findViewById(R.id.loanAmount);
        loanAmt.setText("LOAN AMOUNT");
        TextView interestRt = (TextView) findViewById(R.id.interestRate);
        interestRt.setText("INTEREST RATE");
        TextView tenure = (TextView) findViewById(R.id.tenure);
        tenure.setText("TENURE(month)");
        TextView  loanEmi= (TextView) findViewById(R.id.loanEmi);
        loanEmi.setText("LOAN EMI");
        TextView totalIntPay = (TextView) findViewById(R.id.totalInterestPayable);
        totalIntPay.setText("TOTAL INTEREST PAYABLE");
        TextView totatPay = (TextView) findViewById(R.id.totalPayment);
        totatPay.setText("TOTAL PAYMENT");
        totatPayOut = (TextView) findViewById(R.id.totalPaymentOut);
        //totatPayOut.setText("0");
        totalIntPayOut = (TextView) findViewById(R.id.totalInterestPayableOut);
        //totalIntPayOut.setText("0");
        loanEmiOut = (TextView) findViewById(R.id.loanEmiOut);
        //loanEmiOut.setText("0");


        Button mButton;
        mButton = (Button)findViewById(R.id.button1);

        mButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mEdit1   = (EditText)findViewById(R.id.editText1);
                mEdit2   = (EditText)findViewById(R.id.editText2);
                //mEdit2.getText().toString();
                mEdit3   = (EditText)findViewById(R.id.editText3);
                //mEdit3.getText().toString();
                try {
                    amount = Long.parseLong(mEdit1.getText().toString());
                } catch(NumberFormatException nfe) {
                    System.out.println("Could not parse " + nfe);
                }
                try {
                    rate = Float.parseFloat(mEdit2.getText().toString());
                } catch(NumberFormatException nfe) {
                    System.out.println("Could not parse " + nfe);
                }
                try {
                    tenure_ = Long.parseLong(mEdit3.getText().toString());
                } catch(NumberFormatException nfe) {
                    System.out.println("Could not parse " + nfe);
                }
                double interestPerMonth=(double)(rate)/(12.0*100.0);
                double emi=(amount*interestPerMonth*(Math.pow(interestPerMonth+1,tenure_)))/((Math.pow((interestPerMonth+1),tenure_))-1);
                loanEmiOut.setText(""+(long)(emi));
                totalIntPayOut.setText(""+(long)(emi*tenure_-amount));
                totatPayOut.setText(""+(long)(emi*tenure_));
                total=(float)(emi*tenure_);

                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
        /*Intent myIntent = new Intent(MainActivity.this, emiCalculator.class);
        //myIntent.putExtra("key", value); //Optional parameters
        MainActivity.this.startActivity(myIntent);*/
        /*try {
            amount = Long.parseLong(mEdit1.getText().toString());
        } catch(NumberFormatException nfe) {
            System.out.println("Could not parse " + nfe);
        }
        try {
            rate = Float.parseFloat(mEdit2.getText().toString());
        } catch(NumberFormatException nfe) {
            System.out.println("Could not parse " + nfe);
        }
        try {
            tenure_ = Long.parseLong(mEdit2.getText().toString());
        } catch(NumberFormatException nfe) {
            System.out.println("Could not parse " + nfe);
        }
        totatPayOut.setText(mEdit1.getText().toString());*/
        Button mButton2;
        mButton2 = (Button)findViewById(R.id.button2);
        mButton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(emiCalculator.this, piechart.class);
                //myIntent.putExtra("key", value); //Optional parameters
                emiCalculator.this.startActivity(myIntent);
                /*mEdit1   = (EditText)findViewById(R.id.editText1);
                mEdit2   = (EditText)findViewById(R.id.editText2);
                //mEdit2.getText().toString();
                mEdit3   = (EditText)findViewById(R.id.editText3);
                //mEdit3.getText().toString();
                try {
                    amount = Long.parseLong(mEdit1.getText().toString());
                } catch(NumberFormatException nfe) {
                    System.out.println("Could not parse " + nfe);
                }
                try {
                    rate = Double.parseDouble(mEdit2.getText().toString());
                } catch(NumberFormatException nfe) {
                    System.out.println("Could not parse " + nfe);
                }
                try {
                    tenure_ = Long.parseLong(mEdit3.getText().toString());
                } catch(NumberFormatException nfe) {
                    System.out.println("Could not parse " + nfe);
                }
                double interestPerMonth=(double)(rate)/(12.0*100.0);
                double emi=(amount*interestPerMonth*(Math.pow(interestPerMonth+1,tenure_)))/((Math.pow((interestPerMonth+1),tenure_))-1);
                loanEmiOut.setText(""+(long)(emi));
                totalIntPayOut.setText(""+(long)(emi*tenure_-amount));
                totatPayOut.setText(""+(long)(emi*tenure_));*/
            }
        });
    }

    public static float getAmount() {
        return amount;
    }

    public static float getTotal() {
        return total;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
