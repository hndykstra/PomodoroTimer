package com.operationalsystems.pomodorotimer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.operationalsystems.pomodorotimer.data.Event;
import com.operationalsystems.pomodorotimer.data.EventMember;
import com.operationalsystems.pomodorotimer.data.PomodoroEventContract;
import com.operationalsystems.pomodorotimer.data.PomodoroFirebaseHelper;
import com.operationalsystems.pomodorotimer.data.User;
import com.operationalsystems.pomodorotimer.util.Promise;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Recycler list adapter for event member list.
 */

public class EventMemberListAdapter extends RecyclerView.Adapter<EventMemberListAdapter.EventMemberItem> {

    private Event theEvent;
    private PomodoroFirebaseHelper database;
    private List<User> members = Collections.emptyList();

    public EventMemberListAdapter(final Event theEvent, final PomodoroFirebaseHelper database) {
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
        if (ev != null) {
            Promise[] promises = new Promise[ev.getMembers().size()];
            int count = 0;
            for (String uid : ev.getMembers().keySet()) {
                final String thisUid = uid;
                promises[count++] = database.queryUser(uid);
            }
            Promise.all(promises).then(new Promise.PromiseReceiver() {
                @Override
                public Object receive(Object t) {
                    List<User> users = new ArrayList<>();
                    Object[] array = (Object[])t;
                    for (int i=0 ; i < array.length ; ++i) {
                        users.add((User)array[i]);
                    }
                    Collections.sort(users, new Comparator<User>() {
                        @Override
                        public int compare(User o1, User o2) {
                            return o1.getDisplayName().compareTo(o2.getDisplayName());
                        }
                    });
                    members = users;
                    notifyDataSetChanged();

                    return t;
                }
            });
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

        void bind(User member) {
            this.memberName.setText(member.getDisplayName());
        }
    }
}
