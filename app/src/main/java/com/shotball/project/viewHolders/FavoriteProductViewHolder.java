package com.shotball.project.viewHolders;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.shotball.project.R;
import com.shotball.project.models.Product;
import com.shotball.project.utils.TextUtil;

public class FavoriteProductViewHolder extends RecyclerView.ViewHolder {

    private TextView title;
    private ImageView image;
    private TextView distance;
    private TextView distanceUnit;
    private Button openProduct;
    public MaterialCardView viewForeground;

    public FavoriteProductViewHolder(View itemView) {
        super(itemView);

        title = itemView.findViewById(R.id.product_title);
        image = itemView.findViewById(R.id.product_image);
        distance = itemView.findViewById(R.id.product_distance);
        distanceUnit = itemView.findViewById(R.id.product_distance_unit);
        openProduct = itemView.findViewById(R.id.product_open);
        viewForeground = itemView.findViewById(R.id.product_favourite_card);
    }

    public void bind(final Context context, final Product product, View.OnClickListener OnProductSelectedListener) {
        title.setText(product.title);
        String imageUrl = product.images.get(0);

        try {
            if (TextUtil.isUrl(imageUrl)) {
                Glide.with(context).load(product.images.get(0)).centerCrop().into(image);
                //Glide.with(context).load(product.imageurl).placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher).into(productImageView);
            } else {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images").child(product.getKey()).child(imageUrl);
                Glide.with(context).load(storageReference).centerCrop().into(image);
            }
        } catch (IllegalArgumentException ignored) {

        }

        if (product.distance < 0) {
            distance.setText("-");
        } else {
            int product_distance = round10(product.distance);

            if (product_distance < 1000) {
                distance.setText(String.valueOf(product_distance));
            } else {
                distance.setText(String.valueOf((float) product_distance / 1000));
                distanceUnit.setText(R.string.kilometers);
            }
        }

        viewForeground.setOnClickListener(OnProductSelectedListener);
        openProduct.setOnClickListener(OnProductSelectedListener);
    }

    private int round10(int value) {
        return value / 10 * 10;
    }

}