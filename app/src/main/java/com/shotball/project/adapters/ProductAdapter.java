package com.shotball.project.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.shotball.project.R;
import com.shotball.project.Utils.ItemAnimation;
import com.shotball.project.activities.ProductActivity;
import com.shotball.project.models.Product;
import com.shotball.project.viewHolders.ProductViewHolder;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductViewHolder> {

    private static final String TAG = "ProductAdapter";

    private Context mContext;
    private final List<Product> productsList;

    private int lastPosition = -1;

    public ProductAdapter(Context context, List<Product> productsList) {
        mContext = context;
        this.productsList = productsList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.item_product, viewGroup, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ProductViewHolder view, final int position) {
            final ProductViewHolder viewHolder = (ProductViewHolder) view;
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                /*ActivityOptions option = ActivityOptions.makeSceneTransitionAnimation((Activity) mContext,
                                Pair.create(viewHolder.itemView, "product_image"),
                                Pair.create(viewHolder.itemView, "product_title"));*/
                    Intent intent = new Intent(mContext, ProductActivity.class);
                    intent.putExtra(ProductActivity.EXTRA_PRODUCT_KEY, productsList.get(position).key);
                    //mContext.startActivity(intent, option.toBundle());
                    mContext.startActivity(intent);
                }
            });

            if (productsList.get(position).likes.containsKey(getUid())) {
                viewHolder.like.setImageResource(R.drawable.ic_favorite);
                viewHolder.like.setColorFilter(mContext.getColor(R.color.red_400));

            } else {
                viewHolder.like.setImageResource(R.drawable.ic_favorite_border);
                viewHolder.like.setColorFilter(mContext.getColor(R.color.black));
            }

            viewHolder.bindToPost(mContext, productsList.get(position), position, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (productsList.get(position).likes.containsKey(getUid())) {
                        viewHolder.like.setImageResource(R.drawable.ic_favorite_border);
                        viewHolder.like.setColorFilter(mContext.getColor(R.color.black));
                        productsList.get(position).likes.remove(getUid());
                    } else {
                        viewHolder.like.setImageResource(R.drawable.ic_favorite);
                        viewHolder.like.setColorFilter(mContext.getColor(R.color.red_400));
                        productsList.get(position).likes.put(getUid(), true);
                    }
                    // Need to write to both places the post is stored
                    DatabaseReference globalPostRef = FirebaseDatabase.getInstance().getReference().child("products").child(productsList.get(position).key);

                    // Run two transactions
                    onLikeClicked(globalPostRef);
                }
            });
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

    public void addAll(List<Product> list) {
        productsList.addAll(list);
        notifyDataSetChanged();
    }

    private void onLikeClicked(DatabaseReference postRef) {
        postRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Product p = mutableData.getValue(Product.class);
                if (p == null) {
                    return Transaction.success(mutableData);
                }

                if (p.likes.containsKey(getUid())) {
                    // Unstar the post and remove self from stars
                    p.likeCount = p.likeCount - 1;
                    p.likes.remove(getUid());
                } else {
                    // Star the post and add self to stars
                    p.likeCount = p.likeCount + 1;
                    p.likes.put(getUid(), true);
                }

                // Set value and report transaction success
                mutableData.setValue(p);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                Log.d(TAG, "productTransaction:onComplete:" + databaseError);
            }
        });
    }

    public void setAnimation(View view, int position) {
        if (position > lastPosition) {
            ItemAnimation.animate(view, position, ItemAnimation.FADE_IN);
            lastPosition = position;
        }
    }

    private void showLoadingView(LoadingViewHolder viewHolder, int position) {
        //ProgressBar would be displayed

    }

    private class LoadingViewHolder extends RecyclerView.ViewHolder {

        ProgressBar progressBar;

        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}