package com.shotball.project.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.shotball.project.R;
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

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROGRESS = 0;

    private final List<Product> items;

    private Context ctx;
    private int item_per_display;

    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;
    private OnProductSelectedListener onProductSelectedListener;

    public ProductAdapter(Context context, int item_per_display) {
        ctx = context;
        items = new ArrayList<>();
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
        final Product product = items.get(position);

        if (holder instanceof ProductViewHolder) {
            ProductViewHolder view = (ProductViewHolder) holder;
            view.bind(ctx, product, onProductSelectedListener);
        } else {
            ((ProgressViewHolder) holder).progress_bar.setIndeterminate(true);
        }

        if (product.progress) {
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            layoutParams.setFullSpan(true);
        } else {
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            layoutParams.setFullSpan(false);
        }
    }

    public void setOnProductSelectedListener(OnProductSelectedListener onProductSelectedListener) {
        this.onProductSelectedListener = onProductSelectedListener;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).progress ? VIEW_PROGRESS : VIEW_ITEM;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        lastItemViewDetector(recyclerView);
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void insertData(List<Product> items) {
        setLoaded();
        final int positionStart = getItemCount();
        final int itemCount = items.size();
        this.items.addAll(items);
        notifyItemRangeInserted(positionStart, itemCount);
    }

    private void setLoaded() {
        loading = false;
        for (int i = getItemCount() - 1; i >= 0 ; i--) {
            if (items.get(i).progress) {
                items.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

    public void setLoading() {
        if (getItemCount() != 0) {
            items.add(new Product(true));
            // Triggers a warning
            notifyItemInserted(getItemCount() - 1);
            loading = true;
        }
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
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

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progress_bar;

        ProgressViewHolder(View v) {
            super(v);
            progress_bar = v.findViewById(R.id.progress_bar);
        }
    }

}