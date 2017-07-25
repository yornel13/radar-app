package com.guardias.yornel.gpslocation.app;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.guardias.yornel.gpslocation.R;
import com.guardias.yornel.gpslocation.entity.Position;
import com.guardias.yornel.gpslocation.util.DateUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yornel on 25/7/2017.
 */

public class PositionAdapter extends RecyclerView.Adapter<PositionAdapter.ViewHolder> {

    private List<Position> positions;
    private OnCardViewClick listener;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private OnCardViewClick listener;

        CardView cardView;
        TextView positionName;
        TextView positionTime;


        public ViewHolder(View itemView, OnCardViewClick listener) {
            super(itemView);
            this.listener = listener;

            cardView = (CardView)itemView.findViewById(R.id.position_card_view);
            positionName = (TextView)itemView.findViewById(R.id.position_name);
            positionTime = (TextView)itemView.findViewById(R.id.position_time);

            cardView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.position_card_view:
                    listener.onClick(getAdapterPosition());
                    break;
                default:
                    System.out.println("case default");
                    break;
            }
        }
    }

    public PositionAdapter(List<Position> positions, OnCardViewClick listener) {
        this.positions = positions;
        this.listener = listener;
    }

    @Override
    public PositionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.position_item, parent, false);

        ViewHolder vh = new ViewHolder(v, listener);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Position item =  positions.get(position);

        holder.positionName.setText(item.getControlPosition().getPlaceName());
        holder.positionTime.setText(DateUtil.getHora(item.getTime()));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return positions.size();
    }

    public void replaceAll(List<Position> model) {
        this.positions = new ArrayList<>(model);
        notifyDataSetChanged();
    }

    public List<Position> getPositions() {
        return this.positions;
    }

    public interface OnCardViewClick {

        void onClick(int position);

    }
}
