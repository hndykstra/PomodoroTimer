package com.operationalsystems.pomodorotimer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

/**
 * Fragment for creating a quick dialog to create a new event
 */

public class CreateEventDlgFragment extends DialogFragment {
    public static final String BUNDLE_KEY_EVENTNAME = "EventName";
    public static final String BUNDLE_KEY_ACTIVITY_LENGTH = "ActivityMinutes";
    public static final String BUNDLE_KEY_BREAK_LENGTH = "BreakMinutes";

    public static class CreateEventParams {
        String eventName;
        int activityMinutes;
        int breakMinutes;
    }

    public interface CreateEventListener {
        public void doCreateEvent(CreateEventParams params);
    }

    private EditText eventName;
    private NumberPicker activityLengthPicker;
    private NumberPicker breakLengthPicker;
    private CreateEventParams params;
    private CreateEventListener listener;

    public CreateEventDlgFragment() {
        this.params = new CreateEventParams();
    }

    public void setListener(CreateEventListener l) {
        this.listener = l;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton(R.string.create_event_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // extract data and send to consumer
                params.eventName = CreateEventDlgFragment.this.eventName.getText().toString();
                params.activityMinutes = CreateEventDlgFragment.this.activityLengthPicker.getValue();
                params.breakMinutes = CreateEventDlgFragment.this.breakLengthPicker.getValue();
                listener.doCreateEvent(CreateEventDlgFragment.this.params);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setTitle(R.string.create_event_title);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_event_content, null);
        eventName = (EditText) view.findViewById(R.id.edit_event_name);
        activityLengthPicker = (NumberPicker) view.findViewById(R.id.pick_activity_length);
        breakLengthPicker = (NumberPicker) view.findViewById(R.id.pick_break_length);
        initializeValues();

        builder.setView(view);

        return builder.create();
    }

    private void initializeValues() {
        Bundle b = getArguments();
        String name = b.getString(BUNDLE_KEY_EVENTNAME);
        int activityMinutes = b.getInt(BUNDLE_KEY_ACTIVITY_LENGTH, 25);
        int breakMinutes = b.getInt(BUNDLE_KEY_BREAK_LENGTH, 5);
        if (name != null) {
            this.eventName.setText(name);
        }
        this.activityLengthPicker.setValue(activityMinutes);
        this.breakLengthPicker.setValue(breakMinutes);
    }
}
