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
	private Matrix matrix; // ��ʼ����matrix
	private Matrix savedMatrix = new Matrix();
	private boolean isInited = true; // �Ƿ��Ѿ���ʼ��
	private boolean isZoomed; // �Ƿ�����
	private boolean isRightExceed, isLeftExceed; // �ұ�ƫ�ƣ����ƫ��
	private int position; // ��ǰviewλ��
	private int mpagerPosition;//��ǰviewpager��λ��
	private int viewPosition;//��ǰviewpager��λ��
	private float x_down, y_down; // ��������ʼ���µĵ�
	private float dist = 1f;
	private PointF mid = new PointF(); // X,Y ������

	
	
	
	static final int NONE = 0;// ��ʼ״̬
	static final int DRAG = 1;// �϶�
	static final int ZOOM = 2;// ����
	private int mode = NONE; // ģʽ
	
	private List<Bitmap> bitmaps;//�ļ���������ͼƬ���ɵ�bitmap����
	private float[] p;

	private MyViewPager mPager;

	private int mPagerWid, mPagerHeig; // mpager ��͸�
	private HashMap<Integer, ImageView> imageViews;
	private String path;//ͼƬ�ľ���·��
	private int gridPosition,listPosition;//ͼƬ�ĵ�ǰλ��
	private List<View> views;//���ճ�����ʾ��View
	private List<PhotoInfo> listPics;//һ���ļ����������ͼƬ�б�
	private float autoFitMinScale;
	private ViewPagerAdapter viewPagerAdapter;

	Handler handler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				mPager.setCurrentItem(position + 1);// �л�����һҳ��
				break;
			case 1:
				mPager.setCurrentItem(position - 1);// �л���ǰһҳ��
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
		
		
		// ����mPager
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
				}// ��ʼ���������е�item �Զ�̬��ʽ���� ����ÿ��item�е�ͼƬ
				
			}
		});
		
		
		
	}
	
	

	@SuppressLint("UseSparseArrays")
	private void initImage() throws IOException {
		bitmaps = new ArrayList<Bitmap>();
		matrixs = new ArrayList<Matrix>();
		
		imageViews = new HashMap<Integer, ImageView>();// ���ͼƬ
		
		//����activity������������
		Bundle args = getArguments();
		path=args.getString("path_absolute");//ȡ�õ�ǰ��ʾͼƬ�ľ���·��
		gridPosition=args.getInt("gridPosition");//ȡ���ļ�����ͼƬ��ǰλ��
		listPosition=args.getInt("listPosition");//������ļ����б�λ��
		
		Log.d("logd","path="+path+",listPosition="+listPosition+",gridPosition="+gridPosition);
		
		
		views = new ArrayList<View>();//Ҫ������ʾ��view
		listPics = new ArrayList<PhotoInfo>();
		listPics.addAll(PhotoList.listImageInfo.get(listPosition).getList());//һ���ļ���������ͼƬ�б�
		
		
		for (int i = 0; i < listPics.size(); i++) {
			/**
			 * �����е�ÿ��item���� ����ֱ�Ӷ�̬����
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
			imageView.setScaleType(ScaleType.MATRIX); // ����ScaleTypeΪMATRIX
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			layoutParams.gravity = Gravity.CENTER;
			imageView.setLayoutParams(params);// imageview ����
			
			//Bitmap bitmap=BitmapFactory.decodeFile(listPics.get(i).getPath_absolute());
			
			//-----------------��bitmap���д����ֹOOM-------------------
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(
					new File(listPics.get(i).getPath_absolute())));
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(in, null, options);
			in.close();
	        // ����ѹ����ͼƬ  
			Bitmap bitmap = null;
			int j=0;
			while (true) {
				if ((options.outWidth >> j <= 1000)
						&& (options.outHeight >> j <= 1000)) {
					Log.d("logd",j+"---");
					 // ����ȡ������ע�⣺����һ��Ҫ�ٴμ��أ����ܶ���ʹ��֮ǰ������  
					in = new BufferedInputStream(
							new FileInputStream(new File(listPics.get(i).getPath_absolute())));
					// ���������ʾ �����ɵ�ͼƬΪԭʼͼƬ�ļ���֮һ��  Math.pow(x,y)�����������x��y�η�
	                options.inSampleSize = (int) Math.pow(2.0D, j);  
	                // ����֮ǰ����Ϊ��true������Ҫ��Ϊfalse������ʹ�������ͼƬ  
	                options.inJustDecodeBounds = false; 
					bitmap = BitmapFactory.decodeStream(in, null, options);
					
					break;
				}
				j += 1;
				Log.d("logd","j="+j+"");
			}			
			//-----------------��bitmap���д����ֹOOM-------------------

			
			
			
			//����viewpager
			autoFitMinScale = Math.min((float) mPagerWid / bitmap.getWidth(),
					(float) mPagerHeig / bitmap.getHeight()); // ȡÿ��ͼƬ��С����
			
			matrix = new Matrix();
			matrix.postScale(autoFitMinScale, autoFitMinScale); // ��ÿ��ͼƬ��С������������
		    imageView.setTag(bitmap);

		   // Log.d("logd", "autoFitMinScale:"+autoFitMinScale);
		    
			center(imageView, isInited); // ��ͼƬ��ʼ������  
			p = new float[9];
			matrix.getValues(p);// ����ֵ����  ���ھ�����Ļ� ��ȥ��������
			
			imageView.setImageBitmap(bitmap);
			
			//��ӵ�����
			bitmaps.add(bitmap);
			matrixs.add(matrix);
			
			imageView.setImageMatrix(matrix);//Imageview ����matrix
			linearLayout.addView(imageView);
			views.add(linearLayout);//views�������item
			imageView.setOnTouchListener(this);//ͼƬ��������
		}
		
		viewPagerAdapter = new ViewPagerAdapter(views);
		mPager.setAdapter(viewPagerAdapter);
		mPager.setPageMargin(gridPosition);//���õ�ǰҳ��,��ʾ��ǰ�����ͼƬҳ��
		mPager.setCurrentItem(gridPosition);

		mpagerPosition = position;
		mPager.setOnPageChangeListener(this);
	}
	
	/**
	 * ��ͼƬ��ʼ������  
	 * @param imgView
	 * @param isInited
	 */
	protected void center(ImageView imgView, boolean isInited) { // ��ʼ������ ͼƬ�϶�����
		Matrix m = new Matrix();
		RectF rect = null;
		if (isInited) { // ��ͼƬ��ʼ����
			Bitmap mybitBitmap = (Bitmap) imgView.getTag();
			m.set(matrix);
			rect = new RectF(0, 0, mybitBitmap.getWidth(),
					mybitBitmap.getHeight());
		} else { // ��ͼƬ������������������
			m.set(matrixs.get(mpagerPosition));
			rect = new RectF(0, 0, bitmaps.get(mpagerPosition).getWidth(),
					bitmaps.get(mpagerPosition).getHeight());
		}
		m.mapRect(rect);
		float height = rect.height();
		float width = rect.width();
		float deltaX = 0, deltaY = 0;
		if (true) {
			// ͼƬС����Ļ��С���������ʾ��������Ļ���Ϸ������������ƣ��·�������������
			int screenHeight = mPagerHeig;
			if (height < screenHeight) {
				deltaY = (screenHeight - height) / 2 - rect.top;
			} else if (rect.top > 0) {
				// topExcee=rect.top;//��ƫ����
				deltaY = -rect.top;
			} else if (rect.bottom < screenHeight) {

				deltaY = imgView.getHeight() - rect.bottom;
				// bottomExcee=deltaY;//��ƫ����
			}
		}
		if (true) {
			int screenWidth = mPagerWid;
			if (width < screenWidth) {
				deltaX = (screenWidth - width) / 2 - rect.left;
			} else if (rect.left > 0) { // ���ƫ��������0
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
				if (deltaX >= screenWidth / 3) { // �ұ�ƫ�������ڵ�����Ļ�ֱ��ʿ�ȵ�����֮һʱ���ָ����ؼ�viewpager���϶��¼�����ǿ����ת����һҳ��

					
					///------------------------��ҳע��--------------------------->>>>>>
					
					if (mpagerPosition < listPics.size() - 1) { // ��ҳ��Ϊ���һҳ��
															// ��֧����ת����һҳ
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
			matrixs.get(mpagerPosition).postTranslate(deltaX, deltaY);//��λ
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
		// ��ʼ��
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
	float bigScale = 3f; // Ĭ�ϷŴ���
	Boolean isBig = false; // �Ƿ��ǷŴ�״̬
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {

		ImageView image = (ImageView) v;
		switch (event.getAction() & MotionEvent.ACTION_MASK) { // ���㰴��
		case MotionEvent.ACTION_DOWN:
			mode = NONE;
			if (isZoomed) { // ��ͼƬ�Ŵ����ʽ�¿ɽ����϶�
				mode = DRAG;
				x_down = event.getX();//��ȡx��
				y_down = event.getY();//��ȡy��
				savedMatrix.set(matrixs.get(mpagerPosition));//savedMatrix �ֲ�����
			}
			
			///////////
			startX = event.getRawX();
			startY = event.getRawY();
			if (event.getPointerCount() == 1) {
				// ������ε��ʱ����С��һ��ֵ����Ĭ��Ϊ˫���¼�
				if (event.getEventTime() - lastClickTime < 300) {
					if (isBig) {
						matrixs.get(mpagerPosition).setScale(autoFitMinScale,
								autoFitMinScale);
						isZoomed = false;
						mPager.setTouchIntercept(false);
						isBig=false;
					}else {
						Toast.makeText(getActivity(), "˫��", 1).show();
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
			dist = spacing(event); // �����������������10���ж�Ϊ���ģʽ
			if (spacing(event) > 10f) {
				savedMatrix.set(matrixs.get(mpagerPosition)); // matrix Ĭ��0,0
				midPoint(mid, event); // �е��X,Y
				mode = ZOOM;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (mode == DRAG) { // �϶�ģʽ
				matrixs.get(mpagerPosition).set(savedMatrix);
				matrixs.get(mpagerPosition).postTranslate(
						event.getX() - x_down, event.getY() - y_down);
			} else if (mode == ZOOM) { // ����ģʽ
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
			Log.d("logd",(event.getEventTime()-lastClickTime)+"");//����˳�
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
			if (isRightExceed) { // �л��� ��ʼ��ǰһҳ��
				matrixs.get(mpagerPosition).setValues(p);//��ʼ����ǰҳ��
				/**
				 * ������new Message ��handler.obtainMessage(0)��ʽ��MessageQue�����л�ȡ������ 
				 * ���������߳��л�ҳ�� ��������ǰһҳ���޷���ʼ�� ��handler���л�
				 */
				handler.sendMessageDelayed(handler.obtainMessage(0),100);
			}
			if (isLeftExceed) { // �л���  ��ʼ����һҳ��
				if (mpagerPosition > 0) {
					matrixs.get(mpagerPosition).setValues(p);
					handler.sendMessageDelayed(handler.obtainMessage(1),100);
				}
			}
			break;
		}

		image.setImageMatrix(matrixs.get(mpagerPosition));
		CheckView(image); // ����ͼƬ�Ŵ����С��һ���ı������ҶԸ��ؼ�mpager Intercept�¼��Ƿ���н�ֹ

		return true;
	}
	
	// ��������ľ���
	private float spacing(MotionEvent event) {
		float x = event.getX(1) - event.getX(0);
		float y = event.getY(1) - event.getY(0);
		return FloatMath.sqrt(x * x + y * y);
	}

	// ȡ�������ĵ�
	private void midPoint(PointF mid, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		mid.set(x / 2, y / 2);
	}
	
	private void CheckView(ImageView imageView) {
		float pi[] = new float[9];
		matrixs.get(mpagerPosition).getValues(pi);

		if (mode == ZOOM) {
			if (pi[0] == autoFitMinScale) {//�����ǰ���ű��� ���ڳ�ʼ���ı��� ��ָ����ؼ��л�
				mPager.setTouchIntercept(true);
			} else if (pi[0] < autoFitMinScale) {//�����ǰ���ű���  С�� ��ʼ���ı��� ���ʼ��ͼƬ
				matrixs.get(mpagerPosition).setScale(autoFitMinScale,
						autoFitMinScale);
				isZoomed = false;
				mPager.setTouchIntercept(false);
			} else if (pi[0] > autoFitMinScale) {//�����ǰ���ű��� ���ڳ�ʼ���ı��� ��������״̬
				mPager.setTouchIntercept(true);
				isZoomed = true;
				if (pi[0] > 3 * autoFitMinScale) {
					// matrixs.get(position).setScale(3*autoFitMinScale,
					// 3*autoFitMinScale);
				}
			}
		}
		center(imageView, false);//��ͼƬƫ���� ����
	}
	
	
	
   
}
