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

    private View view;
    private TextView title;
    private ImageView image;
    private TextView distance;
    private TextView distanceUnit;
    public ImageButton like;


    public ProductViewHolder(View itemView) {
        super(itemView);

        view = itemView.findViewById(R.id.product);
        title = itemView.findViewById(R.id.product_title);
        image = itemView.findViewById(R.id.product_image);
        distance = itemView.findViewById(R.id.product_distance);
        distanceUnit = itemView.findViewById(R.id.product_distance_unit);
        like = itemView.findViewById(R.id.product_like);
    }

    public void bindToPost(Context context, Product product, int position, View.OnClickListener likeClickListener) {
        title.setText(product.title);
        //TODO: placeholder and error
        Glide.with(context).load(product.images.get(0)).centerCrop().into(image);
        //Glide.with(context).load(product.imageurl).placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher).into(productImageView);

        int product_distance = product.distance;

        if (product_distance < 1000) {
            distance.setText(String.valueOf(product_distance));
        } else {
            product_distance /= 1000;
            distance.setText(String.valueOf(product_distance));
            distanceUnit.setText(R.string.kilometers);

        }

        like.setOnClickListener(likeClickListener);
    }
}