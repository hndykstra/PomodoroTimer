package com.operationalsystems.pomodorotimer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.operationalsystems.pomodorotimer.data.EventMember;
import com.operationalsystems.pomodorotimer.data.PomodoroEventContract;

import java.util.Collections;
import java.util.List;

/**
 * Recycler list adapter for event member list.
 */

public class EventMemberListAdapter extends RecyclerView.Adapter<EventMemberListAdapter.EventMemberItem> {
    private List<EventMember> members = Collections.emptyList();



    @Override
    public EventMemberItem onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.member_item, parent, false);
        return new EventMemberItem(v);
    }

    @Override
    public void onBindViewHolder(EventMemberItem holder, int position) {
        if (position < 0 || position >= members.size()) {
            throw new IndexOutOfBoundsException(String.valueOf(position));
        }
        holder.bind(members.get(position));
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public void setEventMembers(List<EventMember> members) {
        this.members = members != null ? members : Collections.<EventMember>emptyList();
    }

    class EventMemberItem extends RecyclerView.ViewHolder {

        private TextView memberName;

        EventMemberItem(View itemView) {
            super(itemView);
            memberName = (TextView) itemView.findViewById(R.id.text_member_name);
        }

        void bind(EventMember member) {
            this.memberName.setText(member.getMemberUid());
        }
    }
}
