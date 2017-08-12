package com.operationalsystems.pomodorotimer;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.operationalsystems.pomodorotimer.data.Event;
import com.operationalsystems.pomodorotimer.data.PomodoroEventContract;

import java.util.ArrayList;
import java.util.List;

/**
 * Recycler list adapter for the event list view.
 */
public class EventListAdapter extends FirebaseRecyclerAdapter<Event, EventListAdapter.EventItem> {

    public interface EventSelectionListener {
        void eventSelected(Event event);
    }

    private EventSelectionListener listener;

    public EventListAdapter(DatabaseReference reference, EventSelectionListener listener) {
        super(Event.class, R.layout.event_item, EventListAdapter.EventItem.class, reference);
        this.listener = listener;
    }

    @Override
    public void populateViewHolder(final EventItem holder, final Event event, final int position) {
        if (event.getKey() == null) {
            event.setKey(getRef(position).getKey());
        }

        holder.bind(event);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventListAdapter.this.listener.eventSelected(event);
            }
        });
    }

    public Event getItem(int position) {
        Event ev = (Event)super.getItem(position);
        ev.setKey(this.getRef(position).getKey());
        return ev;
    }

    public String getUniqueEventName(final String baseName) {
        final ArrayList<String> names = new ArrayList<>();
        for (int i = 0 ; i < getItemCount() ; ++i) {
            Event ev = this.getItem(i);
            names.add(ev.getName());
        }

        int count = 0;
        String eventName = baseName;
        while (names.contains(eventName)) {
            ++count;
            eventName = String.format("%s - %02d", baseName, count);
        }
        return eventName;
    }

    static class EventItem extends RecyclerView.ViewHolder {

        private TextView titleView;
        private Event boundEvent;

        public EventItem(View itemView) {
            super(itemView);

            titleView = (TextView) itemView.findViewById(R.id.eventItemTitleView);
        }

        void bind(Event event) {
            this.boundEvent = event;
            titleView.setText(event.getName());
            titleView.setEnabled(event.isActive());
        }
    }
}
