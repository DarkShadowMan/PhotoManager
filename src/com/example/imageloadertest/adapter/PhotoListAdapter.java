package com.example.imageloadertest.adapter;
import java.util.List;





import com.example.imageloadertest.R;
import com.example.imageloadertest.photoInfo.AlbumInfo;
import com.example.imageloadertest.utils.MyThumbnailsUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Context;
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
			paramView = mInflater.inflate(R.layout.listview_item, null);
			ImageView image=(ImageView) paramView.findViewById(R.id.listview_img);
			TextView text=(TextView) paramView.findViewById(R.id.listview_dir_name);
			TextView num=(TextView) paramView.findViewById(R.id.listview_file_num);
			holder.image=image;
			holder.text=text;
			holder.num=num;
			paramView.setTag(holder);
		}else{
			holder = (ViewHolder) paramView.getTag();
		}
		
		
		ImageLoader.getInstance().displayImage(MyThumbnailsUtil.MapgetHashValue(albumInfo.getImage_id(),albumInfo.getPath_file()), 
				holder.image, options);
		holder.text.setText(albumInfo.getName_album());
		holder.num.setText("("+list.get(paramInt).getList().size()+"张)");
		return paramView;
	}
	public class ViewHolder{
		public ImageView image;
		public TextView text;
		public TextView num;
	}
}
