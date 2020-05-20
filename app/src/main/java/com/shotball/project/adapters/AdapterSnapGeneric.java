package com.shotball.project.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.shotball.project.R;
import com.shotball.project.activities.AddProductActivity;
import com.shotball.project.models.Image;

import java.util.ArrayList;
import java.util.List;

public class AdapterSnapGeneric extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnItemClickListener {
        void onProductImageClick(Image image, int position);
        void onAddButtonClick();
    }

    private final int VIEW_ITEM = 1;
    private final int VIEW_ADD = 0;

    private List<Image> items;

    private Context ctx;
    private int layout_id;
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public AdapterSnapGeneric(Context context, int layout_id) {
        ctx = context;
        this.items = new ArrayList<>();
        this.layout_id = layout_id;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_ITEM) {
            View v = inflater.inflate(layout_id, parent, false);
            vh = new OriginalViewHolder(v);
        } else {
            View v = inflater.inflate(R.layout.item_add_button, parent, false);
            vh = new AddButtonViewHolder(v);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        Image image = items.get(position);
        if (holder instanceof OriginalViewHolder) {
            OriginalViewHolder view = (OriginalViewHolder) holder;
            //TODO: placeholder and error
            Glide.with(ctx).load(image.image).centerCrop().into(view.image);
            view.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onProductImageClick(items.get(position), position);
                    }
                }
            });
        } else {
            AddButtonViewHolder view = (AddButtonViewHolder) holder;
            view.imagesAddButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onAddButtonClick();
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return this.items.get(position).addButton ? VIEW_ADD : VIEW_ITEM;
    }

    public Image getItem(int position) {
        return items.get(position);
    }

    public void insertData(List<Image> items) {
        removeAddButton();
        final int positionStart = getItemCount();
        final int itemCount = items.size();
        this.items.addAll(items);
        notifyItemRangeInserted(positionStart, itemCount);
        addButton();
    }

    public void removeAt(int position) {
        items.remove(position);
        notifyDataSetChanged();
        addButton();
    }


    public void replaceItem(final Image newItem, final int position) {
        items.set(position, newItem);
        notifyItemChanged(position);
    }

    public void addButton() {
        if (items.size() != AddProductActivity.MAX_IMAGES) {
            if (items.size() == 0 || getItemViewType(items.size() - 1) != VIEW_ADD) {
                items.add(new Image(true));
                notifyItemInserted(items.size());
            }
        }
    }

    private void removeAddButton() {
        if (items.size() == 1 || getItemViewType(items.size() - 1) == VIEW_ADD) {
            items.remove(items.size() - 1);
            notifyItemRemoved(items.size());
        }
    }

    public static class OriginalViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        View lyt_parent;

        OriginalViewHolder(View v) {
            super(v);
            image = v.findViewById(R.id.image);
            lyt_parent = v.findViewById(R.id.lyt_parent);
        }
    }

    public static class AddButtonViewHolder extends RecyclerView.ViewHolder {
        ImageButton imagesAddButton;

        AddButtonViewHolder(View v) {
            super(v);
            imagesAddButton = v.findViewById(R.id.images_add_button);
        }
    }

}