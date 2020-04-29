package com.shotball.project.viewHolders;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.shotball.project.R;
import com.shotball.project.models.ExchangeModel;
import com.shotball.project.models.Product;
import com.shotball.project.utils.TextUtil;

public class ExchangeViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "ExchangeViewHolder";

    ImageView productImage;
    TextView productTitle;
    TextView productDescription;
    ImageView toProductImage;
    TextView toProductTitle;
    Button acceptButton;
    Button refuseButton;

    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    public ExchangeViewHolder(View itemView) {
        super(itemView);
        productImage = itemView.findViewById(R.id.product_image);
        productTitle = itemView.findViewById(R.id.product_title);
        productDescription = itemView.findViewById(R.id.product_description);
        toProductImage = itemView.findViewById(R.id.to_product_image);
        toProductTitle = itemView.findViewById(R.id.to_product_title);
        acceptButton = itemView.findViewById(R.id.product_exchange_accept);
        refuseButton = itemView.findViewById(R.id.product_exchange_refuse);
    }

    public void bind(final Context ctx, final ExchangeModel model) {
        if (model.getStatus() != 0) {
            itemView.findViewById(R.id.product_exchange_actions_container).setVisibility(View.GONE);
        }

        ValueEventListener productListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Product product = dataSnapshot.getValue(Product.class);
                if (product == null) return;
                productTitle.setText(product.getTitle());
                productDescription.setText(product.getDescription());
                String image = product.getImages().get(0);

                try {
                    if (TextUtil.isUrl(image)) {
                        //TODO: placeholder and error
                        Glide.with(ctx).load(image).centerCrop().into(productImage);
                    } else {
                        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images").child(product.getKey()).child(image);
                        Glide.with(ctx).load(storageReference).centerCrop().into(productImage);
                    }
                } catch (IllegalArgumentException ignored) { }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadProduct:onCancelled", databaseError.toException());
            }
        };

        ValueEventListener toProductListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Product product = dataSnapshot.getValue(Product.class);
                if (product == null) return;
                toProductTitle.setText(product.getTitle());
                String image = product.getImages().get(0);

                try {
                    if (TextUtil.isUrl(image)) {
                        //TODO: placeholder and error
                        Glide.with(ctx).load(image).centerCrop().into(toProductImage);
                    } else {
                        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images").child(product.getKey()).child(image);
                        Glide.with(ctx).load(storageReference).centerCrop().into(toProductImage);
                    }
                } catch (IllegalArgumentException ignored) { }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadProduct:onCancelled", databaseError.toException());
            }
        };
        mDatabase.child("products").child(model.what_exchange).addListenerForSingleValueEvent(productListener);
        mDatabase.child("products").child(model.exchange_for).addListenerForSingleValueEvent(toProductListener);

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptExchange(model);
            }
        });

        refuseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refuseExchange(model);
            }
        });
    }

    private void acceptExchange(final ExchangeModel model) {
        model.setStatus(1);
        mDatabase.child("exchanges").child("accepted").child(model.getKey()).setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mDatabase.child("exchanges").child("proposed").child(model.getKey()).removeValue();
            }
        });
    }

    private void refuseExchange(final ExchangeModel model) {
        model.setStatus(2);
        mDatabase.child("exchanges").child("refused").child(model.getKey()).setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mDatabase.child("exchanges").child("proposed").child(model.getKey()).removeValue();
            }
        });
    }

}