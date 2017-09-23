package com.operationalsystems.pomodorotimer;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.operationalsystems.pomodorotimer.data.Event;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Just a view holder for the event list.
 * Created by Hans on 9/22/2017.
 */
public class EventItem extends RecyclerView.ViewHolder {

    @BindView(R.id.eventItemTitleView)
    TextView titleView;
    private Event boundEvent;

    public EventItem(View itemView) {
        super(itemView);

        ButterKnife.bind(this, itemView);
    }

    /**
     * Binds data in this holder to the event object. Package access.
     * @param event
     */
    void bind(Event event) {
        this.boundEvent = event;
        titleView.setText(event.getName());
        titleView.setEnabled(event.isActive());
    }
}
