package com.shotball.project.viewHolders;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.shotball.project.R;
import com.shotball.project.fragments.AccountFragment;
import com.shotball.project.models.Product;
import com.shotball.project.utils.TextUtil;

public class MyProductViewHolder extends RecyclerView.ViewHolder {

    private TextView title;
    private ImageView image;
    private TextView likes;
    private Button deleteProduct;

    public MyProductViewHolder(View itemView) {
        super(itemView);

        title = itemView.findViewById(R.id.product_title);
        image = itemView.findViewById(R.id.product_image);
        likes = itemView.findViewById(R.id.product_likes);
        deleteProduct = itemView.findViewById(R.id.product_delete);
    }

    public void bind(final Context context, final Product product, AccountFragment.OnProductSelectedListener listener) {
        title.setText(product.getTitle());
        likes.setText(String.valueOf(product.likeCount));
        String imageUrl = product.getImages().get(0);

        try {
            if (TextUtil.isUrl(imageUrl)) {
                Glide.with(context).load(product.images.get(0)).centerCrop().into(image);
            } else {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images").child(product.getKey()).child(imageUrl);
                Glide.with(context).load(storageReference).centerCrop().into(image);
            }
        } catch (IllegalArgumentException ignored) { }

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onProductSelected(product);
            }
        });
        deleteProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDeleteClicked(product.getKey());
            }
        });
    }

}