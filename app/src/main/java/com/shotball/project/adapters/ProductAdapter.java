package com.shotball.project.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.shotball.project.R;
import com.shotball.project.Utils.ItemAnimation;
import com.shotball.project.activities.ProductActivity;
import com.shotball.project.models.Product;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductViewHolder> {

    private Context mContext;
    private final List<Product> productsList;

    public ProductAdapter(Context context, List<Product> productsList) {
        mContext = context;
        this.productsList = productsList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        return new ProductViewHolder(inflater.inflate(R.layout.product_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder viewHolder, final int position) {
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ProductActivity.class);
                intent.putExtra("PRODUCT_KEY", productsList.get(position).key);
                mContext.startActivity(intent);
            }
        });
        viewHolder.bindToPost(mContext, productsList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return productsList.size();
    }
}

class ProductViewHolder extends RecyclerView.ViewHolder {
    private final TextView nameView;
    private final ImageView imageView;

    public ProductViewHolder(View itemView) {
        //super(LayoutInflater.from(context).inflate(R.layout.product_item, parent, false));
        super(itemView);

        imageView = itemView.findViewById(R.id.product_image);
        nameView = itemView.findViewById(R.id.product_name);
        //productView.setOnClickListener(clickListener);
    }

    /*private final View.OnClickListener clickListener =
            v -> ItemActivity.createItemActivityIntent(v.getContext(), (Product) v.getTag());
     */

    public void bindToPost(Context context, Product product, int position) {
        setAnimation(itemView, position);
        nameView.setText(product.title);
        //TODO: placeholder and error
        Glide.with(context).load(product.image).into(imageView);
        //Glide.with(context).load(product.imageurl).placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher).into(productImageView);
    }

    private int lastPosition = -1;

    public void setAnimation(View view, int position) {
        if (position > lastPosition) {
            ItemAnimation.animate(view, position, ItemAnimation.FADE_IN);
            lastPosition = position;
        }
    }
}
