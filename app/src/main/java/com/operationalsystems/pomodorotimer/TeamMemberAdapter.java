package com.operationalsystems.pomodorotimer;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;
import com.operationalsystems.pomodorotimer.data.PomodoroFirebaseHelper;
import com.operationalsystems.pomodorotimer.data.Team;
import com.operationalsystems.pomodorotimer.data.TeamMember;
import com.operationalsystems.pomodorotimer.data.User;
import com.operationalsystems.pomodorotimer.util.Promise;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Recycler adapter for team members. TODO: this really needs both the TeamMember object and the user object?
 */

public class TeamMemberAdapter extends RecyclerView.Adapter<TeamMemberAdapter.TeamMemberItem> {
    public interface RoleChangeListener {
        /** Return false to veto the change? */
        public boolean onRoleChange(TeamMember member, TeamMember.Role newValue);
    }

    private RoleChangeListener listener;
    private PomodoroFirebaseHelper database;
    private Team boundTeam;
    private List<User> members = Collections.emptyList();

    public TeamMemberAdapter(final Team theTeam, final PomodoroFirebaseHelper database, RoleChangeListener roleChangeListener) {
        super();
        listener = roleChangeListener;
        this.database = database;
        setTeam(theTeam);
    }

    public void updateMember(TeamMember member) {
        String uid = member.getMemberUid();
        for (int i=0 ; i < members.size() ; ++i) {
            if (uid.equals(members.get(i).getUid())) {
                notifyItemChanged(i);
                break;
            }
        }
    }
    public void setTeam(Team team) {
        this.boundTeam = team;

        if (boundTeam != null) {
            int size = team.getMembers().size();
            Promise[] promises = new Promise[size];
            int count = 0;
            for (String uid : boundTeam.getMembers().keySet()) {
                final String thisUid = uid;
                promises[count++] = database.queryUser(uid);
            }
            Promise.all(promises).then(new Promise.PromiseReceiver() {
                @Override
                public Object receive(Object t) {
                    User[] users = (User[])t;
                    members = new ArrayList<User>();
                    members.addAll(Arrays.asList(users));
                    Collections.sort(members, new Comparator<User>() {
                        @Override
                        public int compare(User o1, User o2) {
                            TeamMember member1 = boundTeam.findTeamMember(o1.getUid());
                            TeamMember member2 = boundTeam.findTeamMember(o2.getUid());
                            int compare = member1.getRole().compareTo(member2.getRole());
                            if (compare == 0)
                                compare = o1.getDisplayName().compareTo(o2.getDisplayName());
                            return compare;
                        }
                    });
                    notifyDataSetChanged();
                    return t;
                }
            });
        } else {
            members = Collections.emptyList();
            notifyDataSetChanged();
        }
    }

    @Override
    public TeamMemberItem onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.member_role_item, parent, false);
        return new TeamMemberItem(v);
    }

    @Override
    public void onBindViewHolder(TeamMemberItem holder, int position) {
        if (position < 0 || position >= members.size()) {
            throw new IndexOutOfBoundsException(String.valueOf(position));
        }
        User u = members.get(position);
        TeamMember asMember = boundTeam.findTeamMember(u.getUid());
        holder.bind(u, asMember);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    private void notifyRoleChange(int itemPosition, TeamMember member, TeamMember.Role newValue) {
        // listener's reesponsibility to apply the change or toaster why not
        if (listener.onRoleChange(member, newValue))
            this.notifyItemChanged(itemPosition);
    }

    public class TeamMemberItem extends RecyclerView.ViewHolder
            implements AdapterView.OnItemSelectedListener {

        @BindView(R.id.text_member_name) TextView memberName;
        @BindView(R.id.dropdown_role) Spinner roleDropdown;
        private TeamMember boundMember;
        private User boundUser;

        public TeamMemberItem(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            ArrayAdapter<TeamMember.Role> roleArrayAdapter = new ArrayAdapter<TeamMember.Role>(itemView.getContext(), R.layout.support_simple_spinner_dropdown_item, TeamMember.Role.values());
            roleDropdown.setAdapter(roleArrayAdapter);
            roleDropdown.setOnItemSelectedListener(this);
        }

        void bind(User user, TeamMember member) {
            boundUser = user;
            boundMember = member;
            memberName.setText(user.getDisplayName());
            @SuppressWarnings("unchecked")
            ArrayAdapter<TeamMember.Role> adapter = (ArrayAdapter<TeamMember.Role>) roleDropdown.getAdapter();
            int roleItem = -1;
            if (boundMember != null) {
                roleItem = adapter.getPosition(boundMember.getRole());
            } else {
                roleItem = adapter.getPosition(TeamMember.Role.None);
            }
            roleDropdown.setSelection(roleItem);
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            @SuppressWarnings("unchecked")
            ArrayAdapter<TeamMember.Role> roles = (ArrayAdapter<TeamMember.Role>) roleDropdown.getAdapter();
            TeamMember.Role roleSelected = roles.getItem(position);
            notifyRoleChange(getAdapterPosition(), boundMember, roleSelected);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            @SuppressWarnings("unchecked")
            ArrayAdapter<TeamMember.Role> roles = (ArrayAdapter<TeamMember.Role>) roleDropdown.getAdapter();
            notifyRoleChange(getAdapterPosition(), boundMember, null);
        }
    }
}
