package com.operationalsystems.pomodorotimer;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.operationalsystems.pomodorotimer.data.PomodoroFirebaseHelper;
import com.operationalsystems.pomodorotimer.data.Team;
import com.operationalsystems.pomodorotimer.data.TeamMember;
import com.operationalsystems.pomodorotimer.data.User;
import com.operationalsystems.pomodorotimer.util.Promise;

import org.w3c.dom.Text;

import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TeamManageActivity extends AppCompatActivity {
    public static final String STORE_TEAM_DOMAIN = "teamDomain";

    private class AuthListener implements FirebaseAuth.AuthStateListener {

        @Override
        public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null) {
                onLogin(currentUser);
            } else {
                onLogout();
            }
        }
    }

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseUser theUser;
    private PomodoroFirebaseHelper database;

    @BindView(R.id.team_coordinator) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.text_team_owner) TextView teamOwner;
    @BindView(R.id.text_team_name) TextView teamName;
    @BindView(R.id.recycler_team_members) RecyclerView teamMembers;
    @BindView(R.id.toolbar) Toolbar toolbar;

    private TeamMemberAdapter adapter;
    private String teamDomain;
    private Team team;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_manage);
        ButterKnife.bind(this);
        toolbar.setTitle(R.string.title_activity_team_manage);
        setSupportActionBar(toolbar);

        auth = FirebaseAuth.getInstance();
        authListener = new AuthListener();

        RecyclerView.LayoutManager lm = new GridLayoutManager(this, 1);
        teamMembers.setLayoutManager(lm);

        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        } else {
            this.teamDomain = getIntent().getStringExtra(TeamManageActivity.STORE_TEAM_DOMAIN);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle inState) {
        String teamDomain = inState.getString(STORE_TEAM_DOMAIN);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (team != null)
            outState.putString(STORE_TEAM_DOMAIN, team.getDomainName());
    }

    @Override
    public void onPause() {
        super.onPause();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
        theUser = null;
        database = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        auth.addAuthStateListener(authListener);
    }

    private void onLogin(FirebaseUser user) {
        this.theUser = user;
        database = new PomodoroFirebaseHelper();
        adapter = new TeamMemberAdapter(null, database, new TeamMemberAdapter.RoleChangeListener() {
            @Override
            public boolean onRoleChange(String uid, TeamMember member, TeamMember.Role newValue) {
                return processRoleChange(uid, member, newValue);
            }
        });
        this.teamMembers.setAdapter(adapter);
        initialize();
    }

    private void initialize() {
        if (teamDomain != null && teamDomain.length() > 0) {
            this.database.queryTeam(teamDomain).then(new Promise.PromiseReceiver() {
                @Override
                public Object receive(Object t) {
                    team = (Team)t;
                    bindTeam();
                    return t;
                }
            });
        }
    }

    private boolean processRoleChange(final String uid, final TeamMember member, final TeamMember.Role newRole) {
        if (newRole == TeamMember.Role.None) {
            // delete the team member and return false;
            database.quitTeam(team.getDomainName(), uid);
            return false;
        } else if (newRole != member.getRole()) {
            // save the change and return true;
            final TeamMember.Role oldRole = member.getRole();
            member.setRole(newRole);
            database.joinTeam(team.getDomainName(), uid, newRole, theUser.getUid())
                .orElse(new Promise.PromiseCatcher() {
                    @Override
                    public void catchError(Object reason) {
                        Snackbar.make(coordinatorLayout, R.string.msg_team_update_failed, Snackbar.LENGTH_LONG);
                        member.setRole(oldRole);
                        adapter.updateMember(uid, member);
                    }
                });
            return true;
        }
        // otherwise just return true
        return true;
    }

    private void onLogout() {
        // can't stay here if we are logged out
        this.adapter.setTeam(null);
        theUser = null;
        database = null;
        NavUtils.navigateUpFromSameTask(this);
    }

    private void bindTeam() {
        if (team != null) {
            teamName.setText(team.getDomainName());
            database.queryUser(team.getOwnerUid()).then(new Promise.PromiseReceiver() {
                @Override
                public Object receive(Object t) {
                    User u = (User) t;
                    teamOwner.setText(getString(R.string.label_team_owner, u.getDisplayName()));
                    return t;
                }
            });
        }
        adapter.setTeam(team);
    }
}
