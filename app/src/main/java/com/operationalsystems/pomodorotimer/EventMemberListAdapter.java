package com.operationalsystems.pomodorotimer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.operationalsystems.pomodorotimer.data.Event;
import com.operationalsystems.pomodorotimer.data.EventMember;
import com.operationalsystems.pomodorotimer.data.PomodoroEventContract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Recycler list adapter for event member list.
 */

public class EventMemberListAdapter extends RecyclerView.Adapter<EventMemberListAdapter.EventMemberItem> {

    private Event theEvent;
    private List<String> members = Collections.emptyList();

    public EventMemberListAdapter(final Event theEvent) {
        setEvent(theEvent);
    }

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

    public void setEvent(final Event ev) {
        this.theEvent = ev;
        this.members = new ArrayList<>();
        if (ev != null) {
            this.members.addAll(ev.getMembers().values());
            Collections.sort(this.members);
        }
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    class EventMemberItem extends RecyclerView.ViewHolder {

        private TextView memberName;

        EventMemberItem(View itemView) {
            super(itemView);
            memberName = (TextView) itemView.findViewById(R.id.text_member_name);
        }

        void bind(String member) {
            this.memberName.setText(member);
        }
    }
}
