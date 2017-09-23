package com.operationalsystems.pomodorotimer;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.operationalsystems.pomodorotimer.data.Event;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
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

    void bind(Event event) {
        this.boundEvent = event;
        titleView.setText(event.getName());
        titleView.setEnabled(event.isActive());
    }
}
