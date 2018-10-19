package com.example.oolabproject2.ExpenseModel;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.oolabproject2.EditExpenseActivity;
import com.example.oolabproject2.EditRecurringExpense;
import com.example.oolabproject2.MainActivity;
import com.example.oolabproject2.R;
import com.example.oolabproject2.db.DB;
import com.example.oolabproject2.helper.CurrencyHelper;
import com.example.oolabproject2.helper.RecurringExpenseDeleteType;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class ExpensesRecyclerViewAdapter extends RecyclerView.Adapter<ExpensesRecyclerViewAdapter.ViewHolder>
{
    private List<Expense> expenses;
    private Date date;
    private Activity activity;

    public ExpensesRecyclerViewAdapter(@NonNull Activity activity, @NonNull DB db, @NonNull Date date)
    {
        this.activity = activity;
        this.date = date;
        this.expenses = db.getExpensesForDay(date);
    }

    public Date getDate()
    {
        return date;
    }

    public void setDate(@NonNull Date date, @NonNull DB db)
    {
        this.date = date;
        this.expenses = db.getExpensesForDay(date);
        notifyDataSetChanged();
    }

    public int removeExpense(Expense expense)
    {
        Iterator<Expense> expenseIterator = expenses.iterator();
        int position = 0;
        while( expenseIterator.hasNext() )
        {
            Expense shownExpense = expenseIterator.next();
            if( shownExpense.getId().equals(expense.getId()) )
            {
                expenseIterator.remove();
                notifyItemRemoved(position);
                return position;
            }

            position++;
        }

        return -1;
    }

    public void addExpense(Expense expense, int position)
    {
        expenses.add(position, expense);
        notifyItemRangeInserted(position, 1);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycleview_expense_cell, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i)
    {
        final Expense expense = expenses.get(i);

        viewHolder.expenseTitleTextView.setText(expense.getTitle());
        viewHolder.expenseAmountTextView.setText(CurrencyHelper.getFormattedCurrencyString(viewHolder.view.getContext(), -expense.getAmount()));
        viewHolder.expenseAmountTextView.setTextColor(ContextCompat.getColor(viewHolder.view.getContext(), expense.isRevenue() ? R.color.budget_green : R.color.budget_red));
        viewHolder.recurringIndicator.setVisibility(expense.isRecurring() ? View.VISIBLE : View.GONE);
        viewHolder.positiveIndicator.setImageResource(expense.isRevenue() ? R.drawable.ic_label_green : R.drawable.ic_label_red);

        if( expense.isRecurring() )
        {
            assert expense.getAssociatedRecurringExpense() != null;
            switch (expense.getAssociatedRecurringExpense().getType())
            {
                case WEEKLY:
                    viewHolder.recurringIndicatorTextview.setText(viewHolder.view.getContext().getString(R.string.weekly));
                    break;
                case BI_WEEKLY:
                    viewHolder.recurringIndicatorTextview.setText(viewHolder.view.getContext().getString(R.string.bi_weekly));
                    break;
                case MONTHLY:
                    viewHolder.recurringIndicatorTextview.setText(viewHolder.view.getContext().getString(R.string.monthly));
                    break;
                case YEARLY:
                    viewHolder.recurringIndicatorTextview.setText(viewHolder.view.getContext().getString(R.string.yearly));
                    break;
            }
        }

        final View.OnClickListener onClickListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (expense.isRecurring())
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle(expense.isRevenue() ? R.string.dialog_edit_recurring_income_title : R.string.dialog_edit_recurring_expense_title);
                    builder.setItems(expense.isRevenue() ? R.array.dialog_edit_recurring_income_choices : R.array.dialog_edit_recurring_expense_choices, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            switch (which)
                            {
                                case 0: // Edit this one
                                {
                                    RecurringExpense r = expense.getAssociatedRecurringExpense();
                                    System.out.println("RExpense : Date : " + r.getRecurringDate() +  " Amount : " + r.getAmount());
                                    Intent startIntent = new Intent(viewHolder.view.getContext(), EditRecurringExpense.class);
                                    startIntent.putExtra("date", (expense.getDate()).getTime());
                                    startIntent.putExtra("expense", expense);
                                    startIntent.putExtra("isEdit",true);
                                    ActivityCompat.startActivityForResult(activity, startIntent, MainActivity.ADD_EXPENSE_ACTIVITY_CODE, null);
                                    break;
                                }
                                case 1: // Delete this one
                                {
                                    // Send notification to inform views that this expense has been deleted
                                    Intent intent = new Intent(MainActivity.INTENT_RECURRING_EXPENSE_DELETED);
                                    intent.putExtra("expense", expense);
                                    intent.putExtra("deleteType", RecurringExpenseDeleteType.ONE.getValue());
                                    LocalBroadcastManager.getInstance(activity.getApplicationContext()).sendBroadcast(intent);

                                    break;
                                }
                                case 2: // Delete from
                                {
                                    // Send notification to inform views that this expense has been deleted
                                    Intent intent = new Intent(MainActivity.INTENT_RECURRING_EXPENSE_DELETED);
                                    intent.putExtra("expense", expense);
                                    intent.putExtra("deleteType", RecurringExpenseDeleteType.FROM.getValue());
                                    LocalBroadcastManager.getInstance(activity.getApplicationContext()).sendBroadcast(intent);

                                    break;
                                }
                                case 3: // Delete up to
                                {
                                    // Send notification to inform views that this expense has been deleted
                                    Intent intent = new Intent(MainActivity.INTENT_RECURRING_EXPENSE_DELETED);
                                    intent.putExtra("expense", expense);
                                    intent.putExtra("deleteType", RecurringExpenseDeleteType.TO.getValue());
                                    LocalBroadcastManager.getInstance(activity.getApplicationContext()).sendBroadcast(intent);

                                    break;
                                }
                                case 4: // Delete all
                                {
                                    // Send notification to inform views that this expense has been deleted
                                    Intent intent = new Intent(MainActivity.INTENT_RECURRING_EXPENSE_DELETED);
                                    intent.putExtra("expense", expense);
                                    intent.putExtra("deleteType", RecurringExpenseDeleteType.ALL.getValue());
                                    LocalBroadcastManager.getInstance(activity.getApplicationContext()).sendBroadcast(intent);

                                    break;
                                }
                            }
                        }
                    });
                    builder.show();
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle(expense.isRevenue() ? R.string.dialog_edit_income_title : R.string.dialog_edit_expense_title);
                    builder.setItems(expense.isRevenue() ? R.array.dialog_edit_income_choices : R.array.dialog_edit_expense_choices, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            switch (which)
                            {
                                case 0: // Edit expense
                                {
                                    Intent startIntent = new Intent(viewHolder.view.getContext(), EditExpenseActivity.class);
                                    startIntent.putExtra("date", expense.getDate().getTime());
                                    startIntent.putExtra("expense", expense);
                                    startIntent.putExtra("isEdit",true);
                                    ActivityCompat.startActivityForResult(activity, startIntent, MainActivity.ADD_EXPENSE_ACTIVITY_CODE, null);
                                    break;
                                }
                                case 1: // Delete
                                {
                                    // Send notification to inform views that this expense has been deleted
                                    Intent intent = new Intent(MainActivity.INTENT_EXPENSE_DELETED);
                                    intent.putExtra("expense", expense);
                                    System.out.println("I am in Recycler Adapter Deleting expense");
                                    LocalBroadcastManager.getInstance(activity.getApplicationContext()).sendBroadcast(intent);
                                    break;
                                }
                            }
                        }
                    });
                    builder.show();
                }

            }
        };

        viewHolder.view.setOnClickListener(onClickListener);

        viewHolder.view.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                onClickListener.onClick(v);
                return true;
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return expenses.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public final TextView expenseTitleTextView;
        public final TextView expenseAmountTextView;
        public final ViewGroup recurringIndicator;
        public final TextView recurringIndicatorTextview;
        public final ImageView positiveIndicator;
        public final View view;

        public ViewHolder(View v)
        {
            super(v);

            view = v;
            expenseTitleTextView = (TextView) v.findViewById(R.id.expense_title);
            expenseAmountTextView = (TextView) v.findViewById(R.id.expense_amount);
            recurringIndicator = (ViewGroup) v.findViewById(R.id.recurring_indicator);
            recurringIndicatorTextview = (TextView) v.findViewById(R.id.recurring_indicator_textview);
            positiveIndicator = (ImageView) v.findViewById(R.id.positive_indicator);
        }
    }
}
