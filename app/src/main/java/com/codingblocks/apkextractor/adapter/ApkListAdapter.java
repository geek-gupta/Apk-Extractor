package com.codingblocks.apkextractor.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.codingblocks.apkextractor.model.ApkListModel;
import com.codingblocks.apkextractor.R;

import java.util.ArrayList;

/**
 * Created by Nipun on 7/7/17.
 */

public class ApkListAdapter extends RecyclerView.Adapter<ApkListAdapter.APKViewHolder> {

    Context context;
    ArrayList<ApkListModel> apkListModels;
    OnItemClickListener ocl;


    public ApkListAdapter(Context context, ArrayList<ApkListModel> apkListModels, OnItemClickListener ocl) {
        this.context = context;
        this.apkListModels = apkListModels;
        this.ocl = ocl;
    }


    @Override
    public APKViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.apk_list_item, parent, false);
        return new APKViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final APKViewHolder holder, final int position) {

        final ApkListModel model = apkListModels.get(position);
        holder.tvname.setText(model.getName());
        holder.imgIcon.setImageDrawable(model.getIconPath());
        holder.onItemClickListener = ocl;
        holder.tvname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.onItemClickListener.onItemClick(position, model);
            }
        });
        holder.imgIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.onItemClickListener.onItemClick(position, model);
            }
        });

    }

    @Override
    public int getItemCount() {
        if (apkListModels == null)
            return 0;

        return apkListModels.size();
    }

    public interface OnItemClickListener {
        public void onItemClick(int position, ApkListModel model);

    }

    public static class APKViewHolder extends RecyclerView.ViewHolder {

        TextView tvname;
        ImageView imgIcon;
        OnItemClickListener onItemClickListener;

        public APKViewHolder(View itemView) {
            super(itemView);
            tvname = (TextView) itemView.findViewById(R.id.app_name);
            imgIcon = (ImageView) itemView.findViewById(R.id.app_icon_img);

            //onItemClickListener = ocl;
        }
    }
}
