package com.shotball.project.viewHolders;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.shotball.project.R;
import com.shotball.project.models.Product;

public class ProductViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "ProductViewHolder";

    private TextView title;
    private ImageView image;
    public ImageButton like;

    public ProductViewHolder(View itemView) {
        super(itemView);

        title = itemView.findViewById(R.id.product_title);
        image = itemView.findViewById(R.id.product_image);
        like = itemView.findViewById(R.id.product_like);
    }

    public void bindToPost(Context context, Product product, int position, View.OnClickListener likeClickListener) {
        title.setText(product.title);
        //TODO: placeholder and error
        Glide.with(context).load(product.image).centerCrop().into(image);
        //Glide.with(context).load(product.imageurl).placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher).into(productImageView);
        like.setOnClickListener(likeClickListener);
    }
}