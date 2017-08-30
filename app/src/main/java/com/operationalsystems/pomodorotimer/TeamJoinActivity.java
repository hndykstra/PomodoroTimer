package com.operationalsystems.pomodorotimer;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.operationalsystems.pomodorotimer.data.PomodoroFirebaseHelper;
import com.operationalsystems.pomodorotimer.data.Team;
import com.operationalsystems.pomodorotimer.data.TeamMember;
import com.operationalsystems.pomodorotimer.util.Promise;

import java.util.Collections;
import java.util.EnumSet;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class TeamJoinActivity extends AppCompatActivity {
    private static final String LOG_TAG = "TeamJoinActivity";
    private static final String STORE_FILTER = "teamFilter";
    private static final String STORE_TEAM_DOMAIN = "teamDomain";

    private static final EnumSet<TeamMember.Role> ADMIN_ROLES = EnumSet.of(TeamMember.Role.Admin, TeamMember.Role.Owner);

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

    @BindView(R.id.edit_search_team) EditText teamSearch;
    @BindView(R.id.action_search) Button searchButton;
    @BindView(R.id.action_join) Button joinButton;
    @BindView(R.id.recycler_teams) RecyclerView teamRecycler;
    @BindView(R.id.text_team_found) TextView teamFound;
    @BindView(R.id.team_coordinator) CoordinatorLayout layout;
    @BindView(R.id.toolbar) Toolbar toolbar;

    private Team activeTeam;
    private TeamMember activeMembership;
    private ChildEventListener myTeamsListener;

    private MyTeamsListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_join);
        ButterKnife.bind(this);
        toolbar.setTitle(R.string.title_create_join_team);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        auth = FirebaseAuth.getInstance();
        authListener = new AuthListener();

        RecyclerView.LayoutManager lm = new GridLayoutManager(this, 1);
        teamRecycler.setLayoutManager(lm);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCreateNewTeam();
            }
        });

        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle inState) {
        String filter = inState.getString(STORE_FILTER);
        if (filter != null) {
            teamSearch.setText(filter);
        }
        String teamDomain = inState.getString(STORE_TEAM_DOMAIN);
        if (teamDomain != null && teamDomain.length() > 0) {
            database.queryTeam(teamDomain).then(new Promise.PromiseReceiver() {
                @Override
                public Object receive(Object o) {
                    Team t = (Team)o;
                    if (t != null) {
                        activeMembership = activeTeam.findTeamMember(theUser.getUid());
                    }
                    bindActiveTeam(t);
                    return o;
                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(STORE_FILTER, teamSearch.getText().toString());
        if (activeTeam != null) {
            outState.putString(STORE_TEAM_DOMAIN, activeTeam.getDomainName());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
        if (myTeamsListener != null) {
            this.database.unsubscribeUserTeams(theUser.getUid(), myTeamsListener);
        }
        theUser = null;
        myTeamsListener = null;
        database = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        auth.addAuthStateListener(authListener);
    }

    @OnClick(R.id.action_search)
    public void searchTeams() {
        String searchTerm = this.teamSearch.getText().toString();
        // filter should already be applied in OnTextChanged
        database.queryTeam(searchTerm).then(new Promise.PromiseReceiver() {
            @Override
            public Object receive(Object t) {
                Team team = (Team)t;
                bindActiveTeam(team);
                return t;
            }
        });
    }

    @OnClick(R.id.action_join)
    public void teamAction() {
        final String teamName = activeTeam.getDomainName();
        if (activeTeam != null) {
            if (activeMembership != null) {
                if (ADMIN_ROLES.contains(activeMembership.getRole())) {
                    Intent manageIntent = new Intent(this, TeamManageActivity.class);
                    manageIntent.putExtra(TeamManageActivity.STORE_TEAM_DOMAIN, activeTeam.getDomainName());
                    startActivity(manageIntent);
                }
            } else {
                database.joinTeam(teamName, theUser.getUid(), TeamMember.Role.Applied, theUser.getUid())
                    .then(new Promise.PromiseReceiver() {
                        @Override
                        public Object receive(Object t) {
                            return database.queryTeam(teamName);
                        }
                    })
                    .then(new Promise.PromiseReceiver() {
                        @Override
                        public Object receive(Object t) {
                            Team team = (Team)t;
                            adapter.updateTeam(team);
                            // update UI state because team state changed
                            bindActiveTeam(team);
                            return null;
                        }
                    });
            }
        }
    }

    @OnTextChanged(R.id.edit_search_team)
    public void filterChanged() {
        if (adapter != null) {
            String filter = this.teamSearch.getText().toString();
            adapter.filter(filter);
            if (filter != null && filter.length() > 0)
                selectFirst();
        }
    }

    public void showCreateNewTeam() {
        String newTeamName = "";
        if (this.activeTeam == null) {
            newTeamName = this.teamSearch.getText().toString();
        }
        Bundle args = new Bundle();
        args.putString(CreateTeamDlgFragment.BUNDLE_KEY_TEAM_NAME, newTeamName);

        CreateTeamDlgFragment dlg = new CreateTeamDlgFragment();
        dlg.setArguments(args);
        dlg.setListener(new CreateTeamDlgFragment.CreateTeamListener() {
            @Override
            public void doCreateTeam(CreateTeamDlgFragment.CreateTeamParams params) {
                createTeam(params.teamName);
            }
        });
        dlg.show(getFragmentManager(), "CreateTeamDlgFragment");
    }

    private void onLogin(FirebaseUser user) {
        this.theUser = user;
        database = new PomodoroFirebaseHelper();
        adapter = new MyTeamsListAdapter(database, new MyTeamsListAdapter.TeamSelectionListener() {
            @Override
            public void teamSelected(Team team) {
                bindActiveTeam(team);
            }
        });
        adapter.setUser(theUser.getUid());
        String filterTerm = this.teamSearch.getText().toString();
        adapter.filter(filterTerm);
        if (filterTerm != null && filterTerm.length() > 0)
        {
            selectFirst();
        }
        this.teamRecycler.setAdapter(adapter);
        adapter.setTeams(Collections.<Team>emptyList());
        initializeSearchState();
    }

    private void onLogout() {
        // can't stay here if we are logged out
        NavUtils.navigateUpFromSameTask(this);
        this.adapter.clear();
        if (myTeamsListener != null) {
            this.database.unsubscribeUserTeams(theUser.getUid(), myTeamsListener);
        }
        theUser = null;
        myTeamsListener = null;
        database = null;
    }

    private void createTeam(final String domainName) {
        Team team = new Team(domainName, theUser.getUid());
        database.createTeam(team)
        // TODO what to do when done? Listener should be notified so no-op now
            .orElse(new Promise.PromiseCatcher() {
                @Override
                public void catchError(Object reason) {
                    Log.e(LOG_TAG, "Failed to create team: " + reason.toString());
                    Snackbar.make(layout, R.string.team_create_failed, Snackbar.LENGTH_LONG);
                }
            });
    }

    private void initializeSearchState() {
        // query user's teams and populate the recycler - include the filter if any
        myTeamsListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousSibling) {
                String teamKey = dataSnapshot.getKey();
                Log.d(LOG_TAG, "onChildAdded " + teamKey);
                database.queryTeam(teamKey).then(new Promise.PromiseReceiver() {
                    @Override
                    public Object receive(Object t) {
                        Team team = (Team)t;
                        adapter.addTeam(team);
                        return t;
                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String teamKey = dataSnapshot.getKey();
                Log.d(LOG_TAG, "onChildChanged " + teamKey);
                database.queryTeam(teamKey).then(new Promise.PromiseReceiver() {
                    @Override
                    public Object receive(Object t) {
                        Team team = (Team)t;
                        adapter.updateTeam(team);
                        return t;
                    }
                });
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(LOG_TAG, "onChildRemoved " + dataSnapshot.getKey());
                adapter.removeTeam(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                // no-op
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // no-op
                Log.d(LOG_TAG, "subscribe onCancelled");
            }
        };

        database.subscribeUserTeams(theUser.getUid(), myTeamsListener);
        // if there is an active team, then populate the relevant areas and set up the button action
        bindActiveTeam(this.activeTeam);
    }

    private void bindActiveTeam(Team team) {
        this.activeTeam = team;
        this.activeMembership = team != null ? team.findTeamMember(theUser.getUid()) : null;
        String filter = this.teamSearch.getText().toString();
        boolean filtered = filter != null && filter.length() > 0;
        boolean isMember = this.activeMembership != null;
        boolean isAdmin = isMember && ADMIN_ROLES.contains(this.activeMembership.getRole());
        if (activeTeam == null) {
            if (filtered) {
                this.teamFound.setText(getString(R.string.team_search_not_found));
            } else {
                this.teamFound.setText(getString(R.string.team_search_not_selected));
            }
            this.joinButton.setEnabled(false);
            this.joinButton.setText(getString(R.string.join_no_team));
        } else {
            this.teamFound.setText(getString(R.string.team_search_found, activeTeam.getDomainName()));
            if (isMember) {
                if (isAdmin) {
                    this.joinButton.setEnabled(true);
                    this.joinButton.setText(getString(R.string.join_team_admin));
                } else if (activeMembership.getRole() == TeamMember.Role.Applied){
                    this.joinButton.setEnabled(false);
                    this.joinButton.setText(getString(R.string.join_not_accepted));
                } else {
                    this.joinButton.setEnabled(false);
                    this.joinButton.setText(getString(R.string.join_already_member));
                }
            } else {
                this.joinButton.setEnabled(true);
                this.joinButton.setText(getString(R.string.join_new));
            }
        }
    }

    private void selectFirst() {
        if (this.adapter.getItemCount() > 0) {
            this.adapter.selectItem(0);
        }
    }
}
