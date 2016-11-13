package com.example.imageloadertest.adapter;
import java.util.List;

import com.example.imageloadertest.R;
import com.example.imageloadertest.photoInfo.PhotoInfo;
import com.example.imageloadertest.utils.MyThumbnailsUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
public class PhotoGridAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private List<PhotoInfo> list;
	private ViewHolder viewHolder;
	private int width;
	DisplayImageOptions options;
	public PhotoGridAdapter(Context context,List<PhotoInfo> list, DisplayImageOptions options){
		DisplayMetrics dm = new DisplayMetrics();
		((FragmentActivity)context).getWindowManager().getDefaultDisplay().getMetrics(dm);
		width=dm.widthPixels/3;
		mInflater = LayoutInflater.from(context);
		this.list = list;
		this.options=options;
	}
	@Override
	public int getCount() {
		return list.size();
	}
	@Override
	public Object getItem(int paramInt) {
		return list.get(paramInt);
	}
	@Override
	public long getItemId(int paramInt) {
		return paramInt;
	}
	@Override
	public View getView(int paramInt, View convertView, ViewGroup paramViewGroup) {
		PhotoInfo photoInfo = list.get(paramInt);
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.gridview_item, null);
			ImageView imageView=(ImageView)convertView.findViewById(R.id.iv_thumbnail);
			viewHolder.image = imageView;
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		LayoutParams layoutParams = viewHolder.image.getLayoutParams();
		layoutParams.width = width;
		layoutParams.height = width;
		viewHolder.image.setLayoutParams(layoutParams);
		if(photoInfo!=null){
			ImageLoader.getInstance().displayImage(MyThumbnailsUtil.MapgetHashValue(photoInfo.getImage_id(),photoInfo.getPath_file()), 
					viewHolder.image,options);
		}
		return convertView;
	}
	public class ViewHolder{
		public ImageView image;
	}
}
