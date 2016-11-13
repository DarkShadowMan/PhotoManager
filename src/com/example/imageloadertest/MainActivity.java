package com.example.imageloadertest;

import java.util.List;

import com.example.imageloadertest.PhotoGrid.OnPhotoClickListener;
import com.example.imageloadertest.PhotoList.OnPageLodingClickListener;
import com.example.imageloadertest.photoInfo.PhotoInfo;

import android.R.integer;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class MainActivity extends FragmentActivity implements OnPageLodingClickListener,OnPhotoClickListener{

	static PhotoGrid photoGrid;
	PhotoList photoList;
	FragmentTransaction transaction;
	static FragmentManager manager;
	static int backInt=0;//返回的状态
	RelativeLayout rl_container;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	
		rl_container=(RelativeLayout) findViewById(R.id.container);
		
		manager=getSupportFragmentManager();
		photoList = new PhotoList();
		
		FragmentTransaction transaction = manager.beginTransaction();
		transaction.add(R.id.container, photoList);
		transaction.addToBackStack(null);

		transaction.commit();
		
	}

	@Override
	public void onPageLodingClickListener(List<PhotoInfo> list,int position) {
		
		transaction=manager.beginTransaction();
		photoGrid = new PhotoGrid();
		Bundle args = new Bundle();			
		
//		args.putCharSequenceArrayList("list",PhotoList.listImageInfo.get(position).getList());
		args.putInt("position", position);
		
		photoGrid.setArguments(args);
		transaction = manager.beginTransaction();
		transaction.hide(photoList).commit();//隐藏当前的fragment
		transaction = manager.beginTransaction();
		transaction.add(R.id.container, photoGrid);

		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		transaction.addToBackStack(null);
		transaction.commit();
		Log.d("logd","onPageLodingClickListener");
		backInt++;
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK&&backInt==0){
			PhotoList.listImageInfo.clear();//清除数据
			finish();
		}else if(keyCode == KeyEvent.KEYCODE_BACK&&backInt==1){
			backInt--;
			FragmentTransaction transaction = manager.beginTransaction();
			transaction.show(photoList).commit();
			manager.popBackStack(backInt, 0);
		}else if(keyCode == KeyEvent.KEYCODE_BACK&&backInt==2){
			//container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
			backInt--;
			FragmentTransaction transaction = manager.beginTransaction();
			transaction.show(photoGrid).commit();
			manager.popBackStack(backInt, 0);
		}
		return false;
	}

	/**
	 * 点击相片缩略图打开大图
	 */
	@Override
	public void onPhotoClickListener(String path_absolute, int listPosition,int gridPosition) {
		
		//rl_container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
		FragmentTransaction transaction = manager.beginTransaction();
		ImageMatrix img = new ImageMatrix();
		Bundle args = new Bundle();
		args.putSerializable("path_absolute", path_absolute);//点击的图片绝对路径
		args.putInt("gridPosition",gridPosition);//点击的文件夹内图片位置
		args.putInt("listPosition",listPosition);//点击的文件夹列表位置
		img.setArguments(args);
		transaction = manager.beginTransaction();
		transaction.hide(photoGrid).commit();
		transaction = manager.beginTransaction();
//		transaction.add(R.id.container, img);
		
		ViewPagerFragmet viewPagerFragmet=new ViewPagerFragmet();
		viewPagerFragmet.setArguments(args);
		transaction.add(R.id.container, viewPagerFragmet);//改成ViewPagerFragment
		
		
		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		transaction.addToBackStack(null);
		// Commit the transaction
		transaction.commit();
		backInt++;
		
	}
	
	

}
