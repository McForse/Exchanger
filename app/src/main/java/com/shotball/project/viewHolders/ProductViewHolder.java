package com.shotball.project.viewHolders;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.shotball.project.R;
import com.shotball.project.adapters.ProductAdapter;
import com.shotball.project.models.Product;

import java.util.Objects;

public class ProductViewHolder extends RecyclerView.ViewHolder {

    private TextView title;
    private ImageView image;
    private TextView distance;
    private TextView distanceUnit;
    private ImageButton likeButton;

    public ProductViewHolder(View itemView) {
        super(itemView);

        title = itemView.findViewById(R.id.product_title);
        image = itemView.findViewById(R.id.product_image);
        distance = itemView.findViewById(R.id.product_distance);
        distanceUnit = itemView.findViewById(R.id.product_distance_unit);
        likeButton = itemView.findViewById(R.id.product_like);
    }

    public void bind(final Context context, final Product product, final ProductAdapter.OnProductSelectedListener listener) {
        title.setText(product.title);
        //TODO: placeholder and error
        Glide.with(context).load(product.images.get(0)).centerCrop().into(image);
        //Glide.with(context).load(product.imageurl).placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher).into(productImageView);

        int product_distance = round10(product.distance);

        if (product_distance < 1000) {
            distance.setText(String.valueOf(product_distance));
        } else {
            distance.setText(String.valueOf((float) product_distance / 1000));
            distanceUnit.setText(R.string.kilometers);

        }

        setLikeButton(context, product, false);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onProductSelected(product);
                }
            }
        });

        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    setLikeButton(context, product, true);
                    listener.onLikeClicked(product.getKey());
                }
            }
        });
    }

    private void setLikeButton(Context context, Product product, boolean update) {
        if (update) {
            if (product.getLikes().containsKey(getUid())) {
                likeButton.setImageResource(R.drawable.ic_favorite_border);
                likeButton.setColorFilter(context.getColor(R.color.black));
                product.getLikes().remove(getUid());
            } else {
                likeButton.setImageResource(R.drawable.ic_favorite);
                likeButton.setColorFilter(context.getColor(R.color.red_400));
                product.getLikes().put(getUid(), true);
            }
        } else {
            if (product.getLikes().containsKey(getUid())) {
                likeButton.setImageResource(R.drawable.ic_favorite);
                likeButton.setColorFilter(context.getColor(R.color.red_400));
            } else {
                likeButton.setImageResource(R.drawable.ic_favorite_border);
                likeButton.setColorFilter(context.getColor(R.color.black));
            }
        }
    }

    private int round10(int value) {
        return value / 10 * 10;
    }

    private String getUid() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            return null;
        }
    }

}