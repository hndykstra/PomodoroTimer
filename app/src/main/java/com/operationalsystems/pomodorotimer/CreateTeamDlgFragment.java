package com.operationalsystems.pomodorotimer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Populate a dialog for creating a new team.
 */

public class CreateTeamDlgFragment extends DialogFragment {
    public static final String BUNDLE_KEY_TEAM_NAME = "NewTeamName";

    public static class CreateTeamParams {
        public String teamName;
    }

    public interface CreateTeamListener {
        public void doCreateTeam(CreateTeamParams params);
    }

    @BindView(R.id.edit_team_name) EditText teamName;
    CreateTeamParams params = null;
    CreateTeamListener listener = null;

    public CreateTeamDlgFragment() {
        this.params = new CreateTeamParams();
    }

    public void setListener(CreateTeamListener l) {
        this.listener = l;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton(R.string.create_event_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // extract data and send to consumer
                params.teamName = CreateTeamDlgFragment.this.teamName.getText().toString();
                listener.doCreateTeam(params);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setTitle(R.string.create_team_title);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dlg_create_team, null);
        ButterKnife.bind(this, view);
        initializeValues();

        builder.setView(view);

        return builder.create();
    }

    private void initializeValues() {
        Bundle b = getArguments();
        String name = b.getString(BUNDLE_KEY_TEAM_NAME);
        if (name != null) {
            teamName.setText(name);
        }
    }
}
