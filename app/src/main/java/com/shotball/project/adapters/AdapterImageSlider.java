package com.shotball.project.adapters;

import android.app.Activity;
import android.content.Context;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.shotball.project.R;
import com.shotball.project.utils.TextUtil;

import java.util.List;

public class AdapterImageSlider extends PagerAdapter {

    private Activity act;
    private List<String> items;
    private String key;

    private OnItemClickListener onItemClickListener;

    private interface OnItemClickListener {
        void onItemClick(View view, Image obj);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public AdapterImageSlider(Activity activity, List<String> items, String key) {
        this.act = activity;
        this.items = items;
        this.key = key;
    }

    @Override
    public int getCount() {
        return this.items.size();
    }

    public String getItem(int pos) {
        return items.get(pos);
    }

    public void setItems(List<String> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        final String o = items.get(position);
        LayoutInflater inflater = (LayoutInflater) act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.item_slider_image, container, false);

        ImageView image = v.findViewById(R.id.image);

        if (TextUtil.isUrl(o)) {
            //TODO: placeholder and error
            Glide.with(act).load(o).centerCrop().into(image);
        } else {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images/" + key + "/" + o);
            Glide.with(act).load(storageReference).centerCrop().into(image);
        }

        container.addView(v);

        return v;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }
}
