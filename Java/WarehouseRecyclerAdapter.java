package com.cs360.warehousemanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmResults;

public class WarehouseRecyclerAdapter extends RecyclerView.Adapter<WarehouseRecyclerAdapter.MyViewHolder> {

    private final RecyclerViewInterface myInterface;
    private Context context;
    private List<Warehouse> warehouses;
    private HashSet<Integer> selectedWarehouses = new HashSet<>();

    // constructor
    public WarehouseRecyclerAdapter(Context context, RecyclerViewInterface myInterface, List<Warehouse> warehouses) {
        this.context = context;
        this.myInterface = myInterface;
        this.warehouses = warehouses;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private CheckBox warehouseCheckBox;
        private TextView addressView;

        public MyViewHolder(final View view, RecyclerViewInterface myInterface) {
            super(view);
            warehouseCheckBox = view.findViewById(R.id.warehouseHolder);
            addressView = view.findViewById(R.id.addressHolder);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // get adapter position
                    int position = getAdapterPosition();

                    if (position != RecyclerView.NO_POSITION) {
                        myInterface.onItemClick(position);
                    }
                }
            });
        }

    }

    public void updateData(Warehouse warehouse){
        this.warehouses.add(warehouse);
        notifyDataSetChanged(); // Refresh RecyclerView
    }

    public void removeData(Warehouse warehouse) {
        this.warehouses.remove(warehouse);
        notifyDataSetChanged();
    }

    // Method to get selected selectedWarehouse positions
    public HashSet<Integer> getSelectedItems() {
        return selectedWarehouses;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View warehouseView = LayoutInflater.from(parent.getContext()).inflate(R.layout.warehouse_view_holder, parent, false);
        return new MyViewHolder(warehouseView, myInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull WarehouseRecyclerAdapter.MyViewHolder holder, int position) {
        holder.warehouseCheckBox.setText(String.valueOf(warehouses.get(position).getName()));
        holder.warehouseCheckBox.setChecked(selectedWarehouses.contains(position)); // Restore checked state
        holder.addressView.setText(" @ " + warehouses.get(position).getAddress());

        // Handle checkbox clicks
        holder.warehouseCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedWarehouses.add(position);
            } else {
                selectedWarehouses.remove(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (warehouses == null) {
            return 0;
        }
        return warehouses.size();
    }
}

