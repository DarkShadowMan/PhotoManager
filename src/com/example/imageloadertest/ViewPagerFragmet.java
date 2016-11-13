package com.example.imageloadertest;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;




import java.util.List;

import com.example.imageloadertest.adapter.ViewPagerAdapter;
import com.example.imageloadertest.photoInfo.PhotoInfo;
import com.example.imageloadertest.weiget.MyViewPager;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Notification.Style;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.FloatMath;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;

public class ViewPagerFragmet extends Fragment implements OnPageChangeListener,OnTouchListener{

	private List<Matrix> matrixs;
	private Matrix matrix; // 初始化的matrix
	private Matrix savedMatrix = new Matrix();
	private boolean isInited = true; // 是否已经初始化
	private boolean isZoomed; // 是否缩放
	private boolean isRightExceed, isLeftExceed; // 右边偏移，左边偏移
	private int position; // 当前view位置
	private int mpagerPosition;//当前viewpager的位置
	private int viewPosition;//当前viewpager的位置
	private float x_down, y_down; // 触摸屏开始按下的点
	private float dist = 1f;
	private PointF mid = new PointF(); // X,Y 坐标量

	
	
	
	static final int NONE = 0;// 初始状态
	static final int DRAG = 1;// 拖动
	static final int ZOOM = 2;// 缩放
	private int mode = NONE; // 模式
	
	private List<Bitmap> bitmaps;//文件夹内所有图片生成的bitmap集合
	private float[] p;

	private MyViewPager mPager;

	private int mPagerWid, mPagerHeig; // mpager 宽和高
	private HashMap<Integer, ImageView> imageViews;
	private String path;//图片的绝对路径
	private int gridPosition,listPosition;//图片的当前位置
	private List<View> views;//最终呈现显示的View
	private List<PhotoInfo> listPics;//一个文件夹里的所有图片列表
	private float autoFitMinScale;
	private ViewPagerAdapter viewPagerAdapter;

	Handler handler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				mPager.setCurrentItem(position + 1);// 切换到后一页卡
				break;
			case 1:
				mPager.setCurrentItem(position - 1);// 切换到前一页卡
				break;
			}
		};
	};
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {	
		
		return inflater.inflate(R.layout.my_view_pager,container,false);
	}
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mPager=(MyViewPager) getView().findViewById(R.id.mPager);
		
		
		// 计算mPager
		ViewTreeObserver vObserver = mPager.getViewTreeObserver();
		vObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@SuppressWarnings("deprecation")
			public void onGlobalLayout() {
				// TODO Auto-generated method stub
				mPager.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				mPagerWid = mPager.getWidth();
				mPagerHeig = mPager.getHeight();
			
				try {
					initImage();
					
				} catch (IOException e) {
					
					e.printStackTrace();
				}// 初始化适配器中的item 以动态方式生成 设置每个item中的图片
				
			}
		});
		
		
		
	}
	
	

	@SuppressLint("UseSparseArrays")
	private void initImage() throws IOException {
		bitmaps = new ArrayList<Bitmap>();
		matrixs = new ArrayList<Matrix>();
		
		imageViews = new HashMap<Integer, ImageView>();// 存放图片
		
		//从上activity传过来的数据
		Bundle args = getArguments();
		path=args.getString("path_absolute");//取得当前显示图片的绝对路径
		gridPosition=args.getInt("gridPosition");//取得文件夹内图片当前位置
		listPosition=args.getInt("listPosition");//点击的文件夹列表位置
		
		Log.d("logd","path="+path+",listPosition="+listPosition+",gridPosition="+gridPosition);
		
		
		views = new ArrayList<View>();//要呈现显示的view
		listPics = new ArrayList<PhotoInfo>();
		listPics.addAll(PhotoList.listImageInfo.get(listPosition).getList());//一个文件夹里所有图片列表
		
		
		for (int i = 0; i < listPics.size(); i++) {
			/**
			 * 适配中的每个item布局 建议直接动态生成
			 */
			LinearLayout linearLayout = new LinearLayout(getActivity());
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			layoutParams.gravity = Gravity.CENTER;
			linearLayout.setLayoutParams(layoutParams);
			
			/**
			 * imageview
			 */
			final ImageView imageView = new ImageView(getActivity());
			imageViews.put(i, imageView);
			imageView.setScaleType(ScaleType.MATRIX); // 设置ScaleType为MATRIX
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			layoutParams.gravity = Gravity.CENTER;
			imageView.setLayoutParams(params);// imageview 属性
			
			//Bitmap bitmap=BitmapFactory.decodeFile(listPics.get(i).getPath_absolute());
			
			//-----------------对bitmap进行处理防止OOM-------------------
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(
					new File(listPics.get(i).getPath_absolute())));
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(in, null, options);
			in.close();
	        // 生成压缩的图片  
			Bitmap bitmap = null;
			int j=0;
			while (true) {
				if ((options.outWidth >> j <= 1000)
						&& (options.outHeight >> j <= 1000)) {
					Log.d("logd",j+"---");
					 // 重新取得流，注意：这里一定要再次加载，不能二次使用之前的流！  
					in = new BufferedInputStream(
							new FileInputStream(new File(listPics.get(i).getPath_absolute())));
					// 这个参数表示 新生成的图片为原始图片的几分之一。  Math.pow(x,y)这个函数是求x的y次方
	                options.inSampleSize = (int) Math.pow(2.0D, j);  
	                // 这里之前设置为了true，所以要改为false，否则就创建不出图片  
	                options.inJustDecodeBounds = false; 
					bitmap = BitmapFactory.decodeStream(in, null, options);
					
					break;
				}
				j += 1;
				Log.d("logd","j="+j+"");
			}			
			//-----------------对bitmap进行处理防止OOM-------------------

			
			
			
			//设置viewpager
			autoFitMinScale = Math.min((float) mPagerWid / bitmap.getWidth(),
					(float) mPagerHeig / bitmap.getHeight()); // 取每张图片最小比例
			
			matrix = new Matrix();
			matrix.postScale(autoFitMinScale, autoFitMinScale); // 把每张图片最小比例进行缩放
		    imageView.setTag(bitmap);

		   // Log.d("logd", "autoFitMinScale:"+autoFitMinScale);
		    
			center(imageView, isInited); // 将图片初始化居中  
			p = new float[9];
			matrix.getValues(p);// 矩阵值数组  关于矩阵不熟的话 多去看看高数
			
			imageView.setImageBitmap(bitmap);
			
			//添加到数组
			bitmaps.add(bitmap);
			matrixs.add(matrix);
			
			imageView.setImageMatrix(matrix);//Imageview 设置matrix
			linearLayout.addView(imageView);
			views.add(linearLayout);//views容器添加item
			imageView.setOnTouchListener(this);//图片触屏监听
		}
		
		viewPagerAdapter = new ViewPagerAdapter(views);
		mPager.setAdapter(viewPagerAdapter);
		mPager.setPageMargin(gridPosition);//设置当前页卡,显示当前点击的图片页码
		mPager.setCurrentItem(gridPosition);

		mpagerPosition = position;
		mPager.setOnPageChangeListener(this);
	}
	
	/**
	 * 将图片初始化居中  
	 * @param imgView
	 * @param isInited
	 */
	protected void center(ImageView imgView, boolean isInited) { // 初始化居中 图片拖动居中
		Matrix m = new Matrix();
		RectF rect = null;
		if (isInited) { // 当图片初始化过
			Bitmap mybitBitmap = (Bitmap) imgView.getTag();
			m.set(matrix);
			rect = new RectF(0, 0, mybitBitmap.getWidth(),
					mybitBitmap.getHeight());
		} else { // 当图片进行缩放拖拉操作后
			m.set(matrixs.get(mpagerPosition));
			rect = new RectF(0, 0, bitmaps.get(mpagerPosition).getWidth(),
					bitmaps.get(mpagerPosition).getHeight());
		}
		m.mapRect(rect);
		float height = rect.height();
		float width = rect.width();
		float deltaX = 0, deltaY = 0;
		if (true) {
			// 图片小于屏幕大小，则居中显示。大于屏幕，上方留空则往上移，下放留空则往下移
			int screenHeight = mPagerHeig;
			if (height < screenHeight) {
				deltaY = (screenHeight - height) / 2 - rect.top;
			} else if (rect.top > 0) {
				// topExcee=rect.top;//上偏移量
				deltaY = -rect.top;
			} else if (rect.bottom < screenHeight) {

				deltaY = imgView.getHeight() - rect.bottom;
				// bottomExcee=deltaY;//下偏移量
			}
		}
		if (true) {
			int screenWidth = mPagerWid;
			if (width < screenWidth) {
				deltaX = (screenWidth - width) / 2 - rect.left;
			} else if (rect.left > 0) { // 左边偏移量大于0
				deltaX = -rect.left;
				if (deltaX <= -screenWidth / 3) {
					if (mpagerPosition > 0) {
						isLeftExceed = true;
					} else if (mpagerPosition == 0) {
						isLeftExceed = false;
					}
				} else {
					isLeftExceed = false;
				}
			} else if (rect.right < screenWidth) {
				deltaX = screenWidth - rect.right;
				if (deltaX >= screenWidth / 3) { // 右边偏移量大于等于屏幕分辨率宽度的三分之一时，恢复父控件viewpager的拖动事件，再强制跳转到下一页卡

					
					///------------------------翻页注意--------------------------->>>>>>
					
					if (mpagerPosition < listPics.size() - 1) { // 若页卡为最后一页卡
															// 则不支持跳转到下一页
						isRightExceed = true;
					}

				} else if (deltaX <= screenWidth / 3) {
					isRightExceed = false;
				}
			}
		}
		if (isInited) {
			matrix.postTranslate(deltaX, deltaY);
		} else {
			// if (deltaY>mPagerHeig/3) {
			// }
			matrixs.get(mpagerPosition).postTranslate(deltaX, deltaY);//回位
		}
	}


	@Override
	public void onPageScrollStateChanged(int arg0) {
		
		
	}


	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		
		
	}


	@Override
	public void onPageSelected(int arg0) {
		// 初始化
		mpagerPosition = arg0;
		mode = NONE;
		isZoomed = false;
		mPager.setTouchIntercept(false);
		if (mpagerPosition > 0) {
			isRightExceed = false;
			isLeftExceed = false;
		}
		
	}


	
	float startX, startY;
	long lastClickTime;
	float bigScale = 3f; // 默认放大倍数
	Boolean isBig = false; // 是否是放大状态
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {

		ImageView image = (ImageView) v;
		switch (event.getAction() & MotionEvent.ACTION_MASK) { // 主点按下
		case MotionEvent.ACTION_DOWN:
			mode = NONE;
			if (isZoomed) { // 在图片放大的形式下可进行拖动
				mode = DRAG;
				x_down = event.getX();//获取x点
				y_down = event.getY();//获取y点
				savedMatrix.set(matrixs.get(mpagerPosition));//savedMatrix 局部缓存
			}
			
			///////////
			startX = event.getRawX();
			startY = event.getRawY();
			if (event.getPointerCount() == 1) {
				// 如果两次点击时间间隔小于一定值，则默认为双击事件
				if (event.getEventTime() - lastClickTime < 300) {
					if (isBig) {
						matrixs.get(mpagerPosition).setScale(autoFitMinScale,
								autoFitMinScale);
						isZoomed = false;
						mPager.setTouchIntercept(false);
						isBig=false;
					}else {
						Toast.makeText(getActivity(), "双击", 1).show();
						//matrix.setScale(autoFitMinScale*2, autoFitMinScale*2);
						matrixs.get(mpagerPosition).postScale(2f, 2f,mid.x, mid.y);
						savedMatrix.set(matrixs.get(mpagerPosition));
						mode = ZOOM;
						isBig=true;
					}
					
				}

			}
			//Log.d("logd", "lastClickTime:"+lastClickTime+",getEventTime:"+event.getEventTime());
			lastClickTime = event.getEventTime(); 
			
			////////////
	        
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			dist = spacing(event); // 如果连续两点距离大于10，判定为多点模式
			if (spacing(event) > 10f) {
				savedMatrix.set(matrixs.get(mpagerPosition)); // matrix 默认0,0
				midPoint(mid, event); // 中点的X,Y
				mode = ZOOM;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (mode == DRAG) { // 拖动模式
				matrixs.get(mpagerPosition).set(savedMatrix);
				matrixs.get(mpagerPosition).postTranslate(
						event.getX() - x_down, event.getY() - y_down);
			} else if (mode == ZOOM) { // 缩放模式
				float newDist = spacing(event);
				if (newDist > 10f) {
					matrixs.get(mpagerPosition).set(savedMatrix);
					float tScale = newDist / dist;
					matrixs.get(mpagerPosition).postScale(tScale, tScale,
							mid.x, mid.y);
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			Log.d("logd",(event.getEventTime()-lastClickTime)+"");//点击退出
			if (event.getEventTime()-lastClickTime<70) {
				isBig=false;
				getFragmentManager().popBackStack();
				MainActivity.backInt--;
				FragmentTransaction transaction = MainActivity.manager.beginTransaction();
				transaction.show(MainActivity.photoGrid).commit();
				MainActivity.manager.popBackStack(MainActivity.backInt, 0);
			}
		case MotionEvent.ACTION_POINTER_UP:
			mode = NONE;
			if (isRightExceed) { // 切换后 初始化前一页卡
				matrixs.get(mpagerPosition).setValues(p);//初始化当前页卡
				/**
				 * 不建议new Message 以handler.obtainMessage(0)形式从MessageQue队列中获取个对象 
				 * 不能在主线程切换页卡 否则会造成前一页卡无法初始化 用handler来切换
				 */
				handler.sendMessageDelayed(handler.obtainMessage(0),100);
			}
			if (isLeftExceed) { // 切换后  初始化后一页卡
				if (mpagerPosition > 0) {
					matrixs.get(mpagerPosition).setValues(p);
					handler.sendMessageDelayed(handler.obtainMessage(1),100);
				}
			}
			break;
		}

		image.setImageMatrix(matrixs.get(mpagerPosition));
		CheckView(image); // 限制图片放大或缩小到一定的比例，且对父控件mpager Intercept事件是否进行禁止

		return true;
	}
	
	// 计算两点的距离
	private float spacing(MotionEvent event) {
		float x = event.getX(1) - event.getX(0);
		float y = event.getY(1) - event.getY(0);
		return FloatMath.sqrt(x * x + y * y);
	}

	// 取手势中心点
	private void midPoint(PointF mid, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		mid.set(x / 2, y / 2);
	}
	
	private void CheckView(ImageView imageView) {
		float pi[] = new float[9];
		matrixs.get(mpagerPosition).getValues(pi);

		if (mode == ZOOM) {
			if (pi[0] == autoFitMinScale) {//如果当前缩放倍数 等于初始化的倍数 则恢复父控件切换
				mPager.setTouchIntercept(true);
			} else if (pi[0] < autoFitMinScale) {//如果当前缩放倍数  小于 初始化的倍数 则初始化图片
				matrixs.get(mpagerPosition).setScale(autoFitMinScale,
						autoFitMinScale);
				isZoomed = false;
				mPager.setTouchIntercept(false);
			} else if (pi[0] > autoFitMinScale) {//如果当前缩放倍数 大于初始化的倍数 则处于缩放状态
				mPager.setTouchIntercept(true);
				isZoomed = true;
				if (pi[0] > 3 * autoFitMinScale) {
					// matrixs.get(position).setScale(3*autoFitMinScale,
					// 3*autoFitMinScale);
				}
			}
		}
		center(imageView, false);//对图片偏移量 调整
	}
	
	
	
   
}
