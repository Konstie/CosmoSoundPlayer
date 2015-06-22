package com.cosmosound.app;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class DrawerNavigationAdapter extends RecyclerView.Adapter<DrawerNavigationAdapter.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private String mMenuTitles[];
    private int mIcons[];

    private String name;
    Context context;

    DrawerNavigationAdapter(String titles[], int icons[],
                            String name, Context passedCtx) {
        mMenuTitles = titles;
        mIcons = icons;
        this.name = name;
        this.context = passedCtx;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_list_item, parent, false);
            return new ViewHolder(v, viewType, context);
        } else if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_header, parent, false);
            return new ViewHolder(v, viewType, context);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(DrawerNavigationAdapter.ViewHolder holder, int position) {
        if (holder.holderID == 1) {
            holder.textView.setText(mMenuTitles[position - 1]);
            holder.imageView.setImageResource(mIcons[position - 1]);
        } else {
            holder.name.setText(this.name);
        }
    }

    @Override
    public int getItemCount() {
        return mMenuTitles.length + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return TYPE_HEADER;
        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        int holderID;

        TextView textView;
        ImageView imageView;
        TextView name;
        Context ctx;

        public ViewHolder(View itemView, int ViewType, Context ctx) {
            super(itemView);
            this.ctx = ctx;
            itemView.setClickable(true);
            itemView.setOnClickListener(this);

            if (ViewType == TYPE_ITEM) {
                textView = (TextView) itemView.findViewById(R.id.rowText);
                imageView = (ImageView) itemView.findViewById(R.id.rowIcon);
                holderID = 1;
            } else {
                name = (TextView) itemView.findViewById(R.id.name);
                holderID = 0;
            }
        }

        @Override
        public void onClick(View v) {
            Toast.makeText(ctx, "The clicked item is: " + getPosition(), Toast.LENGTH_SHORT).show();
        }
    }
}