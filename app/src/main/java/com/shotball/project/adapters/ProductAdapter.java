package com.shotball.project.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.shotball.project.R;
import com.shotball.project.Utils.ItemAnimation;
import com.shotball.project.models.Product;
import com.shotball.project.viewHolders.ProductViewHolder;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnProductSelectedListener {
        void onProductSelected(Product product);
        void onLikeClicked(String productKey);
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int current_page);
    }

    private static final String TAG = "ProductAdapter";

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROGRESS = 0;

    private Context mContext;
    private final List<Product> productsList;
    private int item_per_display = 0;

    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener = null;

    private OnProductSelectedListener mListener;

    public ProductAdapter(Context context, int item_per_display, OnProductSelectedListener listener) {
        mContext = context;
        mListener = listener;
        productsList = new ArrayList<>();
        this.item_per_display = item_per_display;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_ITEM) {
            View v = inflater.inflate(R.layout.item_product, parent, false);
            vh = new ProductViewHolder(v);
        } else {
            View v = inflater.inflate(R.layout.item_progress, parent, false);
            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        //setAnimation(viewHolder.itemView, position);
        final Product s = productsList.get(position);
        if (holder instanceof ProductViewHolder) {
            ProductViewHolder view = (ProductViewHolder) holder;
            view.bind(mContext, productsList.get(position), mListener);
        } else {
            ((ProgressViewHolder) holder).progress_bar.setIndeterminate(true);
        }

        if (s.progress) {
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            layoutParams.setFullSpan(true);
        } else {
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            layoutParams.setFullSpan(false);
        }
    }

    @Override
    public int getItemCount() {
        return productsList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return this.productsList.get(position).progress ? VIEW_PROGRESS : VIEW_ITEM;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        lastItemViewDetector(recyclerView);
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void insertData(List<Product> items) {
        setLoaded();
        int positionStart = getItemCount();
        int itemCount = items.size();
        productsList.addAll(items);
        notifyItemRangeInserted(positionStart, itemCount);
    }

    public void setLoaded() {
        loading = false;
        for (int i = 0; i < getItemCount(); i++) {
            if (productsList.get(i).progress) {
                productsList.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

    public void setLoading() {
        if (getItemCount() != 0) {
            this.productsList.add(new Product(true));
            notifyItemInserted(getItemCount() - 1);
            loading = true;
        }
    }

    public void clear() {
        productsList.clear();
        notifyDataSetChanged();
    }

    public void add(Product product) {
        productsList.add(product);
        notifyDataSetChanged();
    }

    public void addAll(List<Product> list) {
        productsList.addAll(list);
        notifyDataSetChanged();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    private void lastItemViewDetector(RecyclerView recyclerView) {
        if (recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
            final StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int lastPos = getLastVisibleItem(layoutManager.findLastVisibleItemPositions(null));
                    if (!loading && lastPos == getItemCount() - 1 && onLoadMoreListener != null) {
                        int current_page = getItemCount() / item_per_display;
                        onLoadMoreListener.onLoadMore(current_page);
                        loading = true;
                    }
                }
            });
        }
    }

    private int getLastVisibleItem(int[] into) {
        int last_idx = into[0];
        for (int i : into) {
            if (last_idx < i) last_idx = i;
        }
        return last_idx;
    }

    private int lastPosition = -1;

    public void setAnimation(View view, int position) {
        if (position > lastPosition) {
            ItemAnimation.animate(view, position, ItemAnimation.FADE_IN);
            lastPosition = position;
        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progress_bar;

        public ProgressViewHolder(View v) {
            super(v);
            progress_bar = v.findViewById(R.id.progress_bar);
        }
    }

}