package com.shotball.project.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shotball.project.R;
import com.shotball.project.Utils.ItemAnimation;
import com.shotball.project.models.Product;
import com.shotball.project.viewHolders.ProductViewHolder;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductViewHolder> {

    public interface OnProductSelectedListener {
        void onProductSelected(Product product);
        void onLikeClicked(String productKey);
    }

    private OnProductSelectedListener mListener;

    private Context mContext;
    private final List<Product> productsList;

    public ProductAdapter(Context context, OnProductSelectedListener listener) {
        mContext = context;
        mListener = listener;
        productsList = new ArrayList<>();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ProductViewHolder(inflater.inflate(R.layout.item_product, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(mContext, productsList.get(position), mListener);
        //setAnimation(viewHolder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return productsList.size();
    }

    public void clear() {
        productsList.clear();
        notifyDataSetChanged();
    }

    public void add(Product product) {
        productsList.add(product);
        notifyDataSetChanged();
    }

    public void addAll(List<Product> list) {
        productsList.addAll(list);
        notifyDataSetChanged();
    }

    private int lastPosition = -1;

    public void setAnimation(View view, int position) {
        if (position > lastPosition) {
            ItemAnimation.animate(view, position, ItemAnimation.FADE_IN);
            lastPosition = position;
        }
    }

}