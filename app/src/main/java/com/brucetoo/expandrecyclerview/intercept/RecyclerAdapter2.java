package com.brucetoo.expandrecyclerview.intercept;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.brucetoo.expandrecyclerview.R;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Bruce Too
 * On 26/06/2017.
 * At 18:16
 */

public class RecyclerAdapter2 extends RecyclerView.Adapter<RecyclerAdapter2.ViewHolder> {

    public ArrayList<NotificationBean> datas = null;
    public Context context;

    public RecyclerAdapter2(ArrayList<NotificationBean> datas, Context context) {
        this.datas = datas;
        this.context = context;
    }

    @Override
    public RecyclerAdapter2.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler, parent, false);
        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        view.setBackgroundColor(color);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerAdapter2.ViewHolder holder, int position) {
        final NotificationBean nb = datas.get(position);
//        holder.icon.setImageBitmap(nb.iconBitmap);
//        Drawable drawable = NotificationUtils.getDrawable(context, nb.packageName);
        holder.icon.setImageDrawable(nb.iconDrawable);
        holder.title.setText(nb.finalTitle);
        holder.desc.setText(nb.finalDesc);
        holder.time.setText(nb.when);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotificationUtils.startOriginIntent(context, nb);
            }
        });
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView icon;
        public TextView title;
        public TextView desc;
        public TextView time;

        public ViewHolder(View view) {
            super(view);
            icon = (ImageView) view.findViewById(R.id.icon);
            title = (TextView) view.findViewById(R.id.title);
            desc = (TextView) view.findViewById(R.id.desc);
            time = (TextView) view.findViewById(R.id.time);
        }
    }
}
