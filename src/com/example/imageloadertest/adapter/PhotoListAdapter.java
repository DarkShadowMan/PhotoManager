package com.example.imageloadertest.adapter;
import java.util.List;


import com.example.imageloadertest.MyImageView;
import com.example.imageloadertest.R;
import com.example.imageloadertest.photoInfo.AlbumInfo;
import com.example.imageloadertest.utils.MyThumbnailsUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Context;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PhotoListAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private List<AlbumInfo> list;
	private ViewHolder holder;
	DisplayImageOptions options;
	private Point mPoint = new Point(0, 0);//用来封装ImageView的宽和高的对象

	public PhotoListAdapter(Context context,List<AlbumInfo> list, DisplayImageOptions options){
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
	public View getView(int paramInt, View paramView, ViewGroup paramViewGroup) {
		AlbumInfo albumInfo = list.get(paramInt);
		if(null==paramView){
			holder = new ViewHolder();
			paramView = mInflater.inflate(R.layout.list_view_item, null);
			MyImageView image=(MyImageView) paramView.findViewById(R.id.group_image);
			TextView text=(TextView) paramView.findViewById(R.id.group_title);
			TextView num=(TextView) paramView.findViewById(R.id.group_count);
			holder.mImageView = image;
			holder.mTextViewTitle=text;
			holder.mTextViewCounts=num;
			paramView.setTag(holder);
		}else{
			holder = (ViewHolder) paramView.getTag();
		}
		
		
		ImageLoader.getInstance().displayImage(MyThumbnailsUtil.MapgetHashValue(albumInfo.getImage_id(),albumInfo.getPath_file()), 
				holder.mImageView, options);
		holder.mTextViewTitle.setText(albumInfo.getName_album());
		holder.mTextViewCounts.setText(Integer.toString(list.get(paramInt).getList().size()));
		return paramView;
	}

	public static class ViewHolder{
		public MyImageView mImageView;
		public TextView mTextViewTitle;
		public TextView mTextViewCounts;
	}
}
