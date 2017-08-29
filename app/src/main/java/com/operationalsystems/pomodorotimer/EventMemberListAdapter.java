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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

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
        if (members.size() == 0 && position == 0) {
            holder.bind(null);
        } else {
            if (position < 0 || position >= members.size()) {
                throw new IndexOutOfBoundsException(String.valueOf(position));
            }
            holder.bind(members.get(position));
        }
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
                    Object[] array = (Object[])t;
                    members = new ArrayList<>();
                    for (Object o : array) {
                        members.add((User)o);
                    }
                    Collections.sort(members, new Comparator<User>() {
                        @Override
                        public int compare(User o1, User o2) {
                            return o1.getDisplayName().compareTo(o2.getDisplayName());
                        }
                    });
                    notifyDataSetChanged();

                    return t;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return members.size() == 0 ? 1 : members.size();
    }

    class EventMemberItem extends RecyclerView.ViewHolder {

        @BindView(R.id.text_member_name) TextView memberName;

        EventMemberItem(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(User member) {
            if (member == null) {
                String placeholder = itemView.getContext().getString(R.string.member_placeholder);
                this.memberName.setText(placeholder);
            } else {
                this.memberName.setText(member.getDisplayName());
            }
        }
    }
}
