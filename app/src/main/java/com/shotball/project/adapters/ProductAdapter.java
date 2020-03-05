package com.shotball.project.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.toolbox.NetworkImageView;
import com.shotball.project.R;
import com.shotball.project.Utils.ImageRequester;
import com.shotball.project.models.Product;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductViewHolder> {
    private final Activity activity;
    private final List<Product> productsList;
    public ImageRequester imageRequester;

    public ProductAdapter(Activity activity, List<Product> productsList, ImageRequester imageRequester) {
        this.activity = activity;
        this.productsList = productsList;
        this.imageRequester = imageRequester;
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ProductViewHolder(activity, viewGroup);
    }

    @Override
    public void onBindViewHolder(ProductViewHolder productViewHolder, int i) {
        productViewHolder.bind(activity, productsList.get(i), imageRequester);
    }

    @Override
    public int getItemCount() {
        return productsList.size();
    }
}

class ProductViewHolder extends RecyclerView.ViewHolder {
    private final CardView productView;
    private final TextView productNameView;
    private final NetworkImageView productImageView;

    public ProductViewHolder(Context context, ViewGroup parent) {
        super(LayoutInflater.from(context).inflate(R.layout.product_item, parent, false));
        ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
        itemView.setLayoutParams(layoutParams);
        productImageView = itemView.findViewById(R.id.productImage);
        productNameView = itemView.findViewById(R.id.productName);
        productView = itemView.findViewById(R.id.product);
        //productView.setOnClickListener(clickListener);
    }

    /*private final View.OnClickListener clickListener =
            v -> ItemActivity.createItemActivityIntent(v.getContext(), (Product) v.getTag());
     */

    public void bind(Context context, Product product, ImageRequester imageRequester) {
        imageRequester.setImageFromurl(productImageView, product.imageurl);
        productNameView.setText(product.title);
        productView.setTag(product);
        //Glide.with(context).load(product.imageurl).placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher).into(productImageView);
    }
}
