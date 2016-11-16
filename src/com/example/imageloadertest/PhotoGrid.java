package com.example.imageloadertest;

import java.util.ArrayList;
import java.util.List;

import com.example.imageloadertest.adapter.PhotoGridAdapter;
import com.example.imageloadertest.photoInfo.PhotoInfo;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class PhotoGrid extends Fragment {

	public interface OnPhotoClickListener {  
		public void onPhotoClickListener(String path_absolute, int listPosition, int gridPosition);  
	}
	
	private OnPhotoClickListener onPhotoClickListener;
	
	PhotoGridAdapter adapter;
	GridView gridview1;
	List<PhotoInfo> list;
	DisplayImageOptions options;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		
		View view = inflater.inflate(R.layout.thumbnail_gridview_fragment, container,
				false);
		gridview1 = (GridView) view.findViewById(R.id.grid_view);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		options = new DisplayImageOptions.Builder().cacheInMemory(true)
				.cacheOnDisc(true).bitmapConfig(Bitmap.Config.RGB_565).build();

		Bundle args = getArguments();

		final int listPosition = args.getInt("position");

		list = new ArrayList<PhotoInfo>();
		list.addAll(PhotoList.listImageInfo.get(listPosition).getList());//一个文件夹里所有图片列表

		adapter = new PhotoGridAdapter(getActivity(), list, options);
		gridview1.setAdapter(adapter);
		
		gridview1.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int gridPosition, long id) {
				onPhotoClickListener.onPhotoClickListener(list.get(gridPosition).getPath_absolute(),listPosition,gridPosition);
				
			}
		});
		
		

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if(onPhotoClickListener==null){
			onPhotoClickListener=(OnPhotoClickListener)activity;
		}
	}
}
