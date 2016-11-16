package com.example.imageloadertest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Thumbnails;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.GridView;

import com.example.imageloadertest.adapter.PhotoListAdapter;
import com.example.imageloadertest.photoInfo.AlbumInfo;
import com.example.imageloadertest.photoInfo.PhotoInfo;
import com.example.imageloadertest.utils.MyThumbnailsUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class PhotoList extends Fragment {
	
	public interface OnPageLodingClickListener {  
		public void onPageLodingClickListener(List<PhotoInfo> list, int position);  
	}
	private OnPageLodingClickListener onPageLodingClickListener;
	
	PhotoGrid photoGrid;
	PhotoList photoList;
	//ListView listview1;
	GridView gridView;
	ContentResolver cr;
	static List<AlbumInfo> listImageInfo = new ArrayList<AlbumInfo>();
	PhotoListAdapter listAdapter;
	DisplayImageOptions options;
	FragmentManager manager;
	FragmentTransaction transaction;

	@SuppressLint("CommitTransaction")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		/*View view = inflater.inflate(R.layout.listview_fragment, container,
				false);*/
		View view = inflater.inflate(R.layout.album_grid_fragment,container,false);


		//listview1 = (ListView) view.findViewById(R.id.listview1);
		gridView = (GridView)view.findViewById(R.id.main_grid);
		
		
		manager=getFragmentManager();
		cr = getActivity().getContentResolver();
		/**
		 * 使用ImageLoader之前先进行初始化配置
		 */
		// 创建默认的ImageLoader配置参数
		// ImageLoaderConfiguration
		// configuration=ImageLoaderConfiguration.createDefault(this);
		ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(
				getActivity()).writeDebugLogs().build();
		ImageLoader.getInstance().init(configuration);
		options = new DisplayImageOptions.Builder().cacheInMemory(true)
				.cacheOnDisc(true).bitmapConfig(Bitmap.Config.RGB_565).build();

		// 开启异步加载
		new ImageAsyncTask().execute();

		
		Log.d("logd", "onCreateView");
		return view;
	}

	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		Log.d("logd", "onActivityCreated");

		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				/**
				 * 进入子界面
				 */						
				
				onPageLodingClickListener.onPageLodingClickListener(listImageInfo.get(position).getList(),position);

			}
		});
		
		
	}
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		if(onPageLodingClickListener==null){
			onPageLodingClickListener = (OnPageLodingClickListener)activity;
		}
	}
	

	class ImageAsyncTask extends AsyncTask<Void, Void, Object> {

		@Override
		protected Object doInBackground(Void... params) {

			// 根据内容提供者获取缩略图
			MyThumbnailsUtil.clear();
			String[] projection = { Thumbnails._ID, Thumbnails.IMAGE_ID,
					Thumbnails.DATA };
			Cursor cursor1 = cr.query(Thumbnails.EXTERNAL_CONTENT_URI,
					projection, null, null, null);

			/**
			 * 支持的格式： String imageUri = "http://site.com/image.png"; // from Web
			 * String imageUri = "file:///mnt/sdcard/image.png"; // from SD card
			 * String imageUri = "content://media/external/audio/albumart/13";
			 * // from content provider String imageUri = "assets://image.png";
			 * // from assets String imageUri = "drawable://" +
			 * R.drawable.image; // from drawables (only images, non-9patch)
			 */

			if (cursor1 != null && cursor1.moveToFirst()) {
				int image_id;
				String image_path;
				int image_idColumn = cursor1
						.getColumnIndex(Thumbnails.IMAGE_ID);// 缩略图所对应图片的 id，依赖于
																// images 表 _id
																// 字段，可建立外键
				int dataColumn = cursor1.getColumnIndex(Thumbnails.DATA);// 获取到缓存图片绝对路径
				do {
					image_id = cursor1.getInt(image_idColumn);
					image_path = cursor1.getString(dataColumn);
					MyThumbnailsUtil.put(image_id, "file://" + image_path);
				} while (cursor1.moveToNext());
			}

			// 获取原图
			// 最后一个参数相当于：MediaStore.Images.Media.DATE_MODIFIED + "desc"
			Cursor cursor = cr.query(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null,
					null, "date_modified DESC");
			String _path = "_data";// 图片绝对路径
			String _album = "bucket_display_name";// 直接包含图片的文件夹就是该图片的
													// bucket，就是文件夹名
			HashMap<String, AlbumInfo> myhash = new HashMap<String, AlbumInfo>();
			AlbumInfo albumInfo = null;
			PhotoInfo photoInfo = null;
			if (cursor != null && cursor.moveToFirst()) {
				do {
					int index = 0;
					int _id = cursor.getInt(cursor.getColumnIndex("_id"));
					String path = cursor
							.getString(cursor.getColumnIndex(_path));
					String album = cursor.getString(cursor
							.getColumnIndex(_album));// 文件夹名
					List<PhotoInfo> stringList = new ArrayList<PhotoInfo>();
					photoInfo = new PhotoInfo();// 一张图片对应一个photoInfo对象
					if (myhash.containsKey(album)) {
						albumInfo = myhash.remove(album);
						if (listImageInfo.contains(albumInfo))
							index = listImageInfo.indexOf(albumInfo);
						photoInfo.setImage_id(_id);
						photoInfo.setPath_file("file://" + path);// 在ImageLoader显示的格式要求
						photoInfo.setPath_absolute(path);
						albumInfo.getList().add(photoInfo);// 添加到albumInfo对象中的list中,一个相册拥有所属图片的list集合
						listImageInfo.set(index, albumInfo);// 保持当前文件夹目录文件是最新扫描到的，显示第一张图片
						myhash.put(album, albumInfo);
					} else {
						albumInfo = new AlbumInfo();
						stringList.clear();
						photoInfo.setImage_id(_id);
						photoInfo.setPath_file("file://" + path);// 在ImageLoader显示的格式要求
						photoInfo.setPath_absolute(path);
						stringList.add(photoInfo);
						albumInfo.setImage_id(_id);
						albumInfo.setPath_file("file://" + path);// 在ImageLoader显示的格式要求
						albumInfo.setPath_absolute(path);
						albumInfo.setName_album(album);
						albumInfo.setList(stringList);
						listImageInfo.add(albumInfo);// 得到所有相册的集合列表
						myhash.put(album, albumInfo);
					}
				} while (cursor.moveToNext());
			}
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);

			if (this != null) {

				listAdapter = new PhotoListAdapter(getActivity(),
						listImageInfo, options);
				gridView.setAdapter(listAdapter);

			}

		}

	}
}
