package com.shotball.project.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.shotball.project.R;
import com.shotball.project.models.Product;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductViewHolder> {
    private final Activity activity;
    private final List<Product> productsList;

    public ProductAdapter(Activity activity, List<Product> productsList) {
        this.activity = activity;
        this.productsList = productsList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ProductViewHolder(activity, viewGroup);
    }

    @Override
    public void onBindViewHolder(ProductViewHolder productViewHolder, int i) {
        productViewHolder.bind(activity, productsList.get(i));
    }

    @Override
    public int getItemCount() {
        return productsList.size();
    }
}

class ProductViewHolder extends RecyclerView.ViewHolder {
    private final CardView productView;
    private final TextView productNameView;
    private final ImageView productImageView;

    public ProductViewHolder(Context context, ViewGroup parent) {
        super(LayoutInflater.from(context).inflate(R.layout.product_item, parent, false));
        ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
        itemView.setLayoutParams(layoutParams);
        productImageView = itemView.findViewById(R.id.product_image);
        productNameView = itemView.findViewById(R.id.product_name);
        productView = itemView.findViewById(R.id.product);
        //productView.setOnClickListener(clickListener);
    }

    /*private final View.OnClickListener clickListener =
            v -> ItemActivity.createItemActivityIntent(v.getContext(), (Product) v.getTag());
     */

    public void bind(Context context, Product product) {
        productNameView.setText(product.title);
        productView.setTag(product);
        //TODO: placeholder and error
        Glide.with(context).load(product.imageurl).into(productImageView);
        //Glide.with(context).load(product.imageurl).placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher).into(productImageView);
    }
}
