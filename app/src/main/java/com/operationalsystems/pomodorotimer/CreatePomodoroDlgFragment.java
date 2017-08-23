package com.operationalsystems.pomodorotimer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Dialog to start a new pomodoro as part of an existing event.
 */

public class CreatePomodoroDlgFragment extends DialogFragment {
    public static final String BUNDLE_KEY_EVENTNAME = "EventName";
    public static final String BUNDLE_KEY_POMODORONAME = "PomodoroName";
    public static final String BUNDLE_KEY_ACTIVITY_LENGTH = "ActivityMinutes";
    public static final String BUNDLE_KEY_BREAK_LENGTH = "BreakMinutes";

    public static class CreatePomodoroParams {
        String pomodoroName;
        int activityMinutes;
        int breakMinutes;
    }

    public interface CreatePomodoroListener {
        public void doCreatePomodoro(CreatePomodoroDlgFragment.CreatePomodoroParams params);
    }

    @BindView(R.id.label_event_name) TextView eventName;
    @BindView(R.id.edit_pomodoro_name) EditText pomodoroName;
    @BindView(R.id.pick_activity_length) NumberPicker activityLengthPicker;
    @BindView(R.id.pick_break_length) NumberPicker breakLengthPicker;
    private CreatePomodoroParams params;
    private CreatePomodoroListener listener;

    public CreatePomodoroDlgFragment() {
        this.params = new CreatePomodoroParams();
    }

    public void setListener(CreatePomodoroListener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton(R.string.create_event_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // extract data and send to consumer
                params.pomodoroName = CreatePomodoroDlgFragment.this.pomodoroName.getText().toString();
                params.activityMinutes = CreatePomodoroDlgFragment.this.activityLengthPicker.getValue();
                params.breakMinutes = CreatePomodoroDlgFragment.this.breakLengthPicker.getValue();
                listener.doCreatePomodoro(CreatePomodoroDlgFragment.this.params);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setTitle(R.string.create_pomodoro_title);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.start_timer_content, null);
        ButterKnife.bind(this, view);
        initializeValues();

        builder.setView(view);

        return builder.create();
    }

    private void initializeValues() {
        Bundle b = getArguments();
        String eventName = b.getString(BUNDLE_KEY_EVENTNAME);
        String pomodoroName = b.getString(BUNDLE_KEY_POMODORONAME);
        int activityMinutes = b.getInt(BUNDLE_KEY_ACTIVITY_LENGTH, 25);
        int breakMinutes = b.getInt(BUNDLE_KEY_BREAK_LENGTH, 5);
        if (eventName != null) {
            this.eventName.setText(eventName);
        } else {
            this.eventName.setText(R.string.placeholder_event_nane);
        }
        if (pomodoroName != null) {
            this.pomodoroName.setText(pomodoroName);
        }
        this.activityLengthPicker.setValue(activityMinutes);
        this.breakLengthPicker.setValue(breakMinutes);
    }
}
