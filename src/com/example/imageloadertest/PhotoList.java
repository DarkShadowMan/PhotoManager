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
		 * ʹ��ImageLoader֮ǰ�Ƚ��г�ʼ������
		 */
		// ����Ĭ�ϵ�ImageLoader���ò���
		// ImageLoaderConfiguration
		// configuration=ImageLoaderConfiguration.createDefault(this);
		ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(
				getActivity()).writeDebugLogs().build();
		ImageLoader.getInstance().init(configuration);
		options = new DisplayImageOptions.Builder().cacheInMemory(true)
				.cacheOnDisc(true).bitmapConfig(Bitmap.Config.RGB_565).build();

		// �����첽����
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
				 * �����ӽ���
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

			// ���������ṩ�߻�ȡ����ͼ
			MyThumbnailsUtil.clear();
			String[] projection = { Thumbnails._ID, Thumbnails.IMAGE_ID,
					Thumbnails.DATA };
			Cursor cursor1 = cr.query(Thumbnails.EXTERNAL_CONTENT_URI,
					projection, null, null, null);

			/**
			 * ֧�ֵĸ�ʽ�� String imageUri = "http://site.com/image.png"; // from Web
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
						.getColumnIndex(Thumbnails.IMAGE_ID);// ����ͼ����ӦͼƬ�� id��������
																// images �� _id
																// �ֶΣ��ɽ������
				int dataColumn = cursor1.getColumnIndex(Thumbnails.DATA);// ��ȡ������ͼƬ����·��
				do {
					image_id = cursor1.getInt(image_idColumn);
					image_path = cursor1.getString(dataColumn);
					MyThumbnailsUtil.put(image_id, "file://" + image_path);
				} while (cursor1.moveToNext());
			}

			// ��ȡԭͼ
			// ���һ�������൱�ڣ�MediaStore.Images.Media.DATE_MODIFIED + "desc"
			Cursor cursor = cr.query(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null,
					null, "date_modified DESC");
			String _path = "_data";// ͼƬ����·��
			String _album = "bucket_display_name";// ֱ�Ӱ���ͼƬ���ļ��о��Ǹ�ͼƬ��
													// bucket�������ļ�����
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
							.getColumnIndex(_album));// �ļ�����
					List<PhotoInfo> stringList = new ArrayList<PhotoInfo>();
					photoInfo = new PhotoInfo();// һ��ͼƬ��Ӧһ��photoInfo����
					if (myhash.containsKey(album)) {
						albumInfo = myhash.remove(album);
						if (listImageInfo.contains(albumInfo))
							index = listImageInfo.indexOf(albumInfo);
						photoInfo.setImage_id(_id);
						photoInfo.setPath_file("file://" + path);// ��ImageLoader��ʾ�ĸ�ʽҪ��
						photoInfo.setPath_absolute(path);
						albumInfo.getList().add(photoInfo);// ��ӵ�albumInfo�����е�list��,һ�����ӵ������ͼƬ��list����
						listImageInfo.set(index, albumInfo);// ���ֵ�ǰ�ļ���Ŀ¼�ļ�������ɨ�赽�ģ���ʾ��һ��ͼƬ
						myhash.put(album, albumInfo);
					} else {
						albumInfo = new AlbumInfo();
						stringList.clear();
						photoInfo.setImage_id(_id);
						photoInfo.setPath_file("file://" + path);// ��ImageLoader��ʾ�ĸ�ʽҪ��
						photoInfo.setPath_absolute(path);
						stringList.add(photoInfo);
						albumInfo.setImage_id(_id);
						albumInfo.setPath_file("file://" + path);// ��ImageLoader��ʾ�ĸ�ʽҪ��
						albumInfo.setPath_absolute(path);
						albumInfo.setName_album(album);
						albumInfo.setList(stringList);
						listImageInfo.add(albumInfo);// �õ��������ļ����б�
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
