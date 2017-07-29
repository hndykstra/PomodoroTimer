package com.operationalsystems.pomodorotimer;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.operationalsystems.pomodorotimer.data.Event;
import com.operationalsystems.pomodorotimer.data.PomodoroEventContract;

import java.util.ArrayList;
import java.util.List;

/**
 * Recycler list adapter for the event list view.
 */

public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.EventItem> {

    public interface EventSelectionListener {
        void eventSelected(Event event);
    }

    private EventSelectionListener listener;
    private List<Event> eventList = new ArrayList<>();

    @Override
    public EventItem onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int resourceId = R.layout.event_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(resourceId, parent, false);

        return new EventItem(v);
    }

    @Override
    public void onBindViewHolder(EventItem holder, int position) {
        if (position <0 || position >= eventList.size())
            throw new IllegalArgumentException("Requested event item not found");

        holder.bind(eventList.get(position));
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public void setEvents(List<Event> events) {
        this.eventList = (events != null ? events : new ArrayList<Event>());

        notifyDataSetChanged();
    }

    public EventListAdapter(EventSelectionListener listener) {
        this.listener = listener;
    }

    public class EventItem extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView titleView;
        private Event boundEvent;

        public EventItem(View itemView) {
            super(itemView);

            titleView = (TextView) itemView.findViewById(R.id.eventItemTitleView);
            itemView.setOnClickListener(this);
        }

        public void bind(Event event) {
            this.boundEvent = event;
            titleView.setText(event.getName());
            titleView.setEnabled(event.isActive());
        }

        @Override
        public void onClick(View v) {
            EventListAdapter.this.listener.eventSelected(boundEvent);
        }
    }
}
