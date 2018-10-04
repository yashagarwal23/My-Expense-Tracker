package com.example.oolabproject2.ExpenseModel;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import com.example.oolabproject2.helper.DateHelper;
import com.example.oolabproject2.helper.RecurringExpenseType;

import java.util.Date;

public class RecurringExpense implements Parcelable
{
    /**
     * DB id of this expense (can be null)
     */
    private Long id;
    /**
     * Title of this expense when created
     */
    private String  title;
    /**
     * Start date of this recurring expense (Should not be updated)
     */
    private Date recurringDate;
    /**
     * Amount of this expense when created
     */
    private double  amount;
    /**
     * Is this expense modified (Not implemented yet)
     */
    private boolean modified = false;
    /**
     * Type of recurring expense
     */

    @NonNull
    private final RecurringExpenseType type;

// ---------------------------------->

    /**
     *
     * @param title
     * @param startAmount
     * @param recurringDate
     * @param type
     */
    public RecurringExpense(@NonNull String title, double startAmount, @NonNull Date recurringDate, @NonNull RecurringExpenseType type)
    {
        if (startAmount == 0)
        {
            throw new IllegalArgumentException("startAmount should be != 0");
        }

        this.amount = startAmount;
        this.title = title;
        this.recurringDate = DateHelper.cleanDate(recurringDate);
        this.type = type;
    }

    /**
     *
     * @param id
     * @param title
     * @param startAmount
     * @param recurringDate
     * @param type
     * @param modified
     */
    public RecurringExpense(Long id, @NonNull String title, double startAmount, @NonNull Date recurringDate, @NonNull RecurringExpenseType type, boolean modified)
    {
        this(title, startAmount, recurringDate, type);

        this.id = id;
        this.modified = modified;
    }

    /**
     *
     * @param in
     */
    private RecurringExpense(Parcel in)
    {
        id = (Long) in.readValue(Long.class.getClassLoader());
        title = in.readString();
        recurringDate = new Date(in.readLong());
        amount = in.readDouble();
        modified = in.readByte() != 0;
        type = RecurringExpenseType.valueOf(in.readString());
    }

// ---------------------------------->

    /**
     *
     * @return
     */
    @NonNull
    public String getTitle()
    {
        return title;
    }

    /**
     *
     * @return
     */
    @NonNull
    public Date getRecurringDate()
    {
        return recurringDate;
    }

    /**
     *
     * @return
     */
    public double getAmount()
    {
        return amount;
    }

    /**
     *
     * @return
     */
    public boolean isModified()
    {
        return modified;
    }

    /**
     *
     * @param id
     */
    public void setId(Long id)
    {
        this.id = id;
    }

    /**
     *
     * @return
     */
    public Long getId()
    {
        return id;
    }

    /**
     *
     * @return
     */

    public boolean isRevenue()
    {
        return amount < 0;
    }
    @NonNull
    public RecurringExpenseType getType()
    {
        return type;
    }

// -------------------------------->

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeValue(id);
        dest.writeString(title);
        dest.writeLong(recurringDate.getTime());
        dest.writeDouble(amount);
        dest.writeByte((byte) (modified ? 1 : 0));
        dest.writeString(type.name());
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    public static final Creator<RecurringExpense> CREATOR = new Creator<RecurringExpense>()
    {
        @Override
        public RecurringExpense createFromParcel(Parcel in)
        {
            return new RecurringExpense(in);
        }

        @Override
        public RecurringExpense[] newArray(int size)
        {
            return new RecurringExpense[size];
        }
    };
}
