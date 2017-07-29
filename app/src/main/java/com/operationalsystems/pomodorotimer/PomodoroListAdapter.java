package com.operationalsystems.pomodorotimer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.operationalsystems.pomodorotimer.data.Pomodoro;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Recycler adapter for list of pomodoros associated with an event
 */

public class PomodoroListAdapter extends RecyclerView.Adapter<PomodoroListAdapter.PomodoroItem> {

    private List<Pomodoro> pomodoroList = Collections.emptyList();

    @Override
    public PomodoroItem onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.pomodoro_item, parent, false);

        return new PomodoroItem(v);
    }

    @Override
    public void onBindViewHolder(PomodoroItem holder, int position) {
        if (position < 0 || position > pomodoroList.size()) {
            throw new IndexOutOfBoundsException(String.valueOf(position));
        }

        holder.bind(pomodoroList.get(position));
    }

    @Override
    public int getItemCount() {
        return pomodoroList.size();
    }

    public void setPomodoroList(List<Pomodoro> pomodoros) {
        this.pomodoroList = pomodoros != null ? pomodoros : Collections.<Pomodoro>emptyList();
        notifyDataSetChanged();
    }

    public class PomodoroItem extends RecyclerView.ViewHolder {
        private TextView titleView;
        private TextView activityView;
        private TextView breakView;
        private Pomodoro boundPomodoro;

        public PomodoroItem(View itemView) {
            super(itemView);

            titleView = (TextView) itemView.findViewById(R.id.text_pomodoro_name);
            activityView = (TextView) itemView.findViewById(R.id.text_pomodoro_activity);
            breakView = (TextView) itemView.findViewById(R.id.text_pomodoro_break);
        }

        public void bind(Pomodoro pomodoro) {
            this.boundPomodoro = pomodoro;
            titleView.setText(pomodoro.getName());
            Date start = pomodoro.getStartDt();
            Date breakTime = pomodoro.getEndDt();
            Date end = pomodoro.getEndDt();
            String activityDuration = "*";
            String breakDuration = "*";
            if (breakTime != null) {
                long secDiff = (breakTime.getTime() - start.getTime()) / 1000L;
                long minDiff = secDiff / 60L;
                secDiff = secDiff - (minDiff * 60);
                long hrDiff = minDiff / 60L;
                minDiff = minDiff - (hrDiff * 60);
                activityDuration = String.format("%d:%02d", hrDiff, minDiff);
            }
            if (breakTime != null && end != null) {
                long secDiff = (end.getTime() - breakTime.getTime()) / 1000L;
                long minDiff = secDiff / 60L;
                secDiff = secDiff - (minDiff * 60);
                long hrDiff = minDiff / 60L;
                minDiff = minDiff - (hrDiff * 60);
                breakDuration = String.format("%d:%02d", hrDiff, minDiff);
            }
            String activityFormat = itemView.getContext().getString(R.string.pomodoro_activity_time, activityDuration);
            String breakFormat = itemView.getContext().getString(R.string.pomodoro_break_time, breakDuration);
            this.activityView.setText(activityFormat);
            this.breakView.setText(breakFormat);
        }
    }
}
