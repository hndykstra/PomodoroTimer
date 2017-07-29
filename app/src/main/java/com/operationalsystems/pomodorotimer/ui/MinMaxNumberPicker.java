package com.operationalsystems.pomodorotimer.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.NumberPicker;

import com.operationalsystems.pomodorotimer.R;

/**
 * Adds XML capability to set min and max values
 */

public class MinMaxNumberPicker extends NumberPicker {
    public MinMaxNumberPicker(Context context) {
        super(context);
    }

    public MinMaxNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MinMax, 0, 0);
        setAttributes(a);
    }

    private void setAttributes(TypedArray a) {
        try {
            this.setMinValue(a.getInt(R.styleable.MinMax_min, 5));
            this.setMaxValue(a.getInt(R.styleable.MinMax_max, 120));
            this.setValue(this.getMinValue());
        } finally {
            a.recycle();
        }
    }
}
