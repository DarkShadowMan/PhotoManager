package com.example.imageloadertest;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.FloatMath;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
public class ImageMatrix extends Fragment {
	private ImageView ivShow;
	private String path;
	
	float downX=0,downY=0;//按下时X,Y的坐标
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		ivShow=(ImageView) getView().findViewById(R.id.iv_show);
		ivShow.setOnTouchListener(new TouchListener());

		Bundle args = getArguments();
		path=args.getString("path_absolute");
		Bitmap bm=BitmapFactory.decodeFile(path);
		
		ivShow.setImageBitmap(bm);
		
	}
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.imgmatrix, container, false);
	}
	class TouchListener implements OnTouchListener{
		private int mode=0;		//记录是拖拉照片模式还是放大缩小照片模式
		private static final int MODE_DRAG=1;		//拖拉照片模式
		private static final int MODE_ZOOM=2;		//放大缩小照片模式
		
		private PointF startPointF=new PointF();	//用于记录开始时候的坐标位置
		
		private Matrix matrix=new Matrix();			//用于记录拖拉图片移动的坐标位置
		private Matrix currentMatrix=new Matrix();	//用于记录图片要进行拖拉时候的坐标位置
		
		private float startDis;						//两个手指的开始距离
		private PointF midPointF;					//两个手指的中间点
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			 /** 通过与运算保留最后八位 MotionEvent.ACTION_MASK = 255 */ 
			switch (event.getAction()&MotionEvent.ACTION_MASK) {
			// 手指压下屏幕  触发
			case MotionEvent.ACTION_DOWN:
			
				mode=MODE_DRAG;
				// 记录ImageView当前的移动位置 
				currentMatrix.set(ivShow.getImageMatrix());
				startPointF.set(event.getX(),event.getY());
				
				
				break;
			// 手指在屏幕上移动，改事件会被不断触发 
			case MotionEvent.ACTION_MOVE:
				if(mode==MODE_DRAG){
					float dx=event.getX()-startPointF.x;// 得到x轴的移动距离 
					float dy=event.getY()-startPointF.y;// 得到x轴的移动距离
					// 在没有移动之前的位置上进行移动
					matrix.set(currentMatrix);  
					matrix.postTranslate(dx, dy);
				}else if(mode==MODE_ZOOM){		// 放大缩小图片
					float endDis= distance(event);// 结束距离 
					if(endDis>10f){		// 两个手指并拢在一起的时候像素大于10
						float scale=endDis/startDis;	// 得到缩放倍数 
						matrix.set(currentMatrix);
						matrix.postScale(scale, scale,midPointF.x,midPointF.y);
					}
				}
				break;
				// 手指离开屏幕
			case MotionEvent.ACTION_UP:
				
				// 当触点离开屏幕，但是屏幕上还有触点(手指)
			case MotionEvent.ACTION_POINTER_UP:
				mode=0;
				break;
				// 当屏幕上已经有触点(手指)，再有一个触点压下屏幕 
			case MotionEvent.ACTION_POINTER_DOWN:
				mode=MODE_ZOOM;
				/** 计算两个手指间的距离 */  
				startDis=distance(event);
				/** 计算两个手指间的中间点 */  
				if(startDis>10f){// 两个手指并拢在一起的时候像素大于10   
					midPointF = mid(event);  
				    //记录当前ImageView的缩放倍数   
					currentMatrix.set(ivShow.getImageMatrix());  
				}
				break;
			}
			ivShow.setImageMatrix(matrix);
			return true;
		}
		private float distance(MotionEvent event){
			float dx=event.getX(1)-event.getX(0);
			float dy=event.getY(1)-event.getY(0);
			/** 使用勾股定理返回两点之间的距离 */
			return FloatMath.sqrt(dx*dx+dy*dy);
		}
		 /** 计算两个手指间的中间点 */ 
		private  PointF mid(MotionEvent event){
			float midX=(event.getX(1)+event.getX(0))/2;
			float midY=(event.getY(1)+event.getY(0))/2;
			return new PointF(midX,midY);
		}
	}
}
