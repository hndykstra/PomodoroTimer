package com.operationalsystems.pomodorotimer;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.operationalsystems.pomodorotimer.data.Event;
import com.operationalsystems.pomodorotimer.data.Pomodoro;
import com.operationalsystems.pomodorotimer.data.PomodoroFirebaseHelper;
import com.operationalsystems.pomodorotimer.data.Team;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Recycler adapter for list of teams.
 */

public class MyTeamsListAdapter extends RecyclerView.Adapter<MyTeamsListAdapter.TeamItem> {
    private static final String LOG_TAG = "MyTeamsListAdapter";

    public interface TeamSelectionListener {
        void teamSelected(Team team);
    }

    private PomodoroFirebaseHelper database;
    private List<Team> teams = new ArrayList<>();
    private String filter = null;
    private List<Team> filteredTeams = new ArrayList<>();
    private TeamSelectionListener selectionListener;
    private int selectedItem;

    public MyTeamsListAdapter(PomodoroFirebaseHelper database, TeamSelectionListener selectionListener) {
        this.database = database;
        this.selectionListener = selectionListener;
    }

    @Override
    public TeamItem onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.team_item, parent, false);

        return new TeamItem(v);
    }

    @Override
    public void onBindViewHolder(final TeamItem holder, int position) {
        final Team team = filteredTeams.get(position);
        holder.bind(team);
        holder.itemView.setSelected(position == selectedItem);
        Log.d(LOG_TAG, "item view selected = " + holder.itemView.isSelected());
        Log.d(LOG_TAG, "bind item card to " + holder.itemCard.getCardBackgroundColor().toString());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectItem(holder.getAdapterPosition());
                MyTeamsListAdapter.this.selectionListener.teamSelected(team);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredTeams.size();
    }

    public void setTeams(Collection<Team> teams) {
        this.teams.clear();
        this.teams.addAll(teams);
        applyFilter();
        selectItem(-1);
    }

    public void clear() {
        this.teams.clear();
    }

    public void filter(String teamName) {
        this.filter = teamName;
        applyFilter();
    }

    public void addTeam(Team t) {
        this.teams.add(t);
        if (checkTeamFilter(t)) {
            this.filteredTeams.add(t);
            this.notifyItemInserted(filteredTeams.size() - 1);
        }
    }

    public void updateTeam(Team t) {
        // for now, assuming that renaming a team means removing the old one and creating new
        // but update could potentially change displayed data
        // so contains (based on domain name equality) will work here
        int index = teams.indexOf(t);
        if (index != -1) {
            teams.set(index, t);
            int filteredIndex = filteredTeams.indexOf(t);
            if (filteredIndex != -1) {
                filteredTeams.set(filteredIndex, t);
                this.notifyItemChanged(filteredIndex);
            }
        }
    }

    public void removeTeam(String teamDomain) {
        for (int index = 0 ; index < teams.size() ; ++index) {
            Team t = teams.get(index);
            if (t.getDomainName().equals(teamDomain)) {
                teams.remove(index);
                int filteredIndex = filteredTeams.indexOf(t);
                if (filteredIndex != -1) {
                    filteredTeams.remove(filteredIndex);
                    this.notifyItemRemoved(filteredIndex);
                }
                break;
            }
        }
    }

    public void selectItem(int itemPosition) {
        Log.d(LOG_TAG, "selecting item " + itemPosition);
        if (selectedItem >= 0 && selectedItem < getItemCount()) {
            notifyItemChanged(selectedItem);
        }
        selectedItem = itemPosition;
        if (selectedItem >= 0 && selectedItem < getItemCount()) {
            notifyItemChanged(selectedItem);
        }
    }

    private boolean checkTeamFilter(Team t) {
        return (this.filter == null || this.filter.length() == 0
            || t.getDomainName().toLowerCase().startsWith(this.filter.toLowerCase()));
    }

    private void applyFilter() {
        if (this.filter == null || this.filter.length() == 0) {
            if (!compareLists(teams, filteredTeams)) {
                this.filteredTeams.clear();
                this.filteredTeams.addAll(this.teams);
                this.notifyDataSetChanged();
            }
        } else {
            List<Team> newTeams = new ArrayList<>();
            for (Team team : this.teams) {
                // would like to implement this with .contains but since firebase
                // query doesn't do this, it would be inconsistent and confusing
                if (checkTeamFilter(team)) {
                    newTeams.add(team);
                }
            }
            if (!compareLists(newTeams, filteredTeams)) {
                this.filteredTeams = newTeams;
                notifyDataSetChanged();
            }
        }
    }

    // true if lists are the same objects in same order
    private boolean compareLists(List<Team> list1, List<Team> list2) {
        if (list1.size() != list2.size())
            return false;

        for (int i=0 ; i < list1.size() ; ++i) {
            if (!list1.get(i).equals(list2.get(i)))
                return false;
        }

        return true;
    }

    public class TeamItem extends RecyclerView.ViewHolder {
        @BindView(R.id.text_team_name) TextView nameView;
        @BindView(R.id.text_team_owner) TextView ownerView;
        @BindView(R.id.item_card) CardView itemCard;
        private Team boundTeam;

        TeamItem(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        void bind(Team team) {
            this.boundTeam = team;
            nameView.setText(team.getDomainName());
            ownerView.setText(team.getOwnerUid());
        }
    }
}
