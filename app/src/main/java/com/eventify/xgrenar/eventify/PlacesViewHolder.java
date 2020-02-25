package com.eventify.xgrenar.eventify;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class PlacesViewHolder extends RecyclerView.ViewHolder {

    View view;

    public PlacesViewHolder(@NonNull View itemView) {
        super(itemView);

        view = itemView;
    }

    public void setDetails(Context ctx, String type) {
        TextView place = view.findViewById(R.id.place);

        place.setText(type);
    }
}
