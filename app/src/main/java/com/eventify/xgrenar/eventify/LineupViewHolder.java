package com.eventify.xgrenar.eventify;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class LineupViewHolder extends RecyclerView.ViewHolder {

    View view;

    public LineupViewHolder(@NonNull View itemView) {
        super(itemView);

        view = itemView;
    }

    public void setDetails(Context ctx, String dsc, String time) {
        TextView lineup = view.findViewById(R.id.lineup);

        lineup.setText(time + " - " + dsc);
    }
}
