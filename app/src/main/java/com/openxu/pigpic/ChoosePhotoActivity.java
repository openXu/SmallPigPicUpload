package com.openxu.pigpic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.openxu.pigpic.bean.ImageBean;
import com.openxu.pigpic.bean.ImageBucket;
import com.openxu.pigpic.bean.ImageItem;
import com.openxu.pigpic.util.AlbumHelper;
import com.openxu.pigpic.util.BitmapCache;
import com.openxu.pigpic.util.DensityUtil;
import com.openxu.pigpic.util.LogUtil;
import com.openxu.pigpic.util.PickPhotoUtil;
import com.openxu.pigpic.util.ToastAlone;
import com.openxu.pigpic.view.TitleLayout;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * author : ox
 * create at : 2017/5/5 22:13
 * project : midzs119
 * class name : ChoosePhotoActivity
 * version : 1.0
 * class describe：选择相片
 *
 * 此类专用于选择一张图片的情况
 */
public class ChoosePhotoActivity extends Activity {
	private String TAG = "ChoosePhotoActivity";
	private Context mContext;
	private GridView gridview;
	TextView group_text, total_text;
	private ListView group_listview;
	AlbumHelper helper;
	protected ImageLoader imageLoader;
	// 所有的图片
	private final static int SCAN_FOLDER_OK = 2;
	private RelativeLayout list_layout;
	private ListAdapter listAdapter;
//	private int limit_count ;
	
	private TitleLayout photo_title;

	private Bitmap optionBtmap = null;

	private int columnWidth;   //gridView每列的宽度

	//	private List<String> list;
	private String tempCameraPath; //拍照临时文件
	private String tempDir;
	private int max_number;
	private ArrayList<String> takePics;
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		LogUtil.v(TAG, "拍照放回-------------");
		if(resultCode == RESULT_OK) {
			switch (requestCode) {
			case PickPhotoUtil.PickPhotoCode.PICKPHOTO_TAKE:
				File fi = new File("");
				PickPhotoUtil.getInstance().takeResult(this, data, fi);
				addedPath.clear();
				takePics.clear();
				takePics.add(tempCameraPath);
				Intent dataIntent = new Intent();
				dataIntent.putStringArrayListExtra("addedPath", addedPath);
				dataIntent.putStringArrayListExtra("takePics", takePics);
				setResult(RESULT_OK, dataIntent);
				ChoosePhotoActivity.this.finish();
				break;

			default:
				break;
			}
		}
	}

	ArrayList<ImageItem> nowImageItems = new ArrayList<ImageItem>();
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case SCAN_FOLDER_OK:
				try {
//					dialogUtil.dismissProgressDialog();
					// 获取到mAllImgs；并显示到数据中
					GridAdapter gridAdatper1 = new GridAdapter();
					gridAdatper1.setData(nowImageItems);
					gridview.setAdapter(gridAdatper1);
					gridAdatper1 = null;
				} catch (Exception e) {
					// TODO: handle exception
				}
				break;
			}
		}

	};


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		initView();
		initData();
		setListener();
	}

	private  void initView() {
		setContentView(R.layout.activity_choose_photo);
		gridview = (GridView) findViewById(R.id.gridview);
		group_text = (TextView) findViewById(R.id.group_text);
		total_text = (TextView) findViewById(R.id.total_text);
		group_listview = (ListView) findViewById(R.id.group_listview);
		photo_title = (TitleLayout) findViewById(R.id.photo_title);
		list_layout = (RelativeLayout) findViewById(R.id.list_layout);


		// imageLoader配置
		DisplayImageOptions imageOptions = new DisplayImageOptions.Builder()
				.cacheInMemory(true).cacheOnDisc(true).build();
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				this).defaultDisplayImageOptions(imageOptions)
				.discCacheFileNameGenerator(new Md5FileNameGenerator())
				.memoryCacheSize(2 * 1024 * 1024)
				.memoryCache(new WeakMemoryCache())
				.build();
		imageLoader = ImageLoader.getInstance();
		imageLoader.init(config);
	}

	protected void initData() {
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		int screenWidth = displayMetrics.widthPixels;
		LogUtil.v(TAG, "屏幕宽：" + screenWidth);  //屏幕宽高：1080 * 1920
//		int columnWidth = gridview.getColumnWidth();  //lev 13
		int numColumns = 3;
//		int horizontalSpacing = gridview.getHorizontalSpacing();
		int horizontalSpacing = DensityUtil.dip2px(mContext,2);
		columnWidth = (screenWidth - (horizontalSpacing*(numColumns-1)))/numColumns;
		LogUtil.v(TAG, "gridview列数量=" + numColumns +
				" columnWidth=" +columnWidth);  //屏幕宽高：1080 * 1920

		max_number = getIntent().getIntExtra("max_number", 1);
		tempDir = getIntent().getStringExtra("temp_dir");
		ArrayList<String> selectedPics = getIntent().getStringArrayListExtra("addedPath");
		takePics = getIntent().getStringArrayListExtra("takePics");
		LogUtil.e(TAG, "tempDir:"+tempDir);
		LogUtil.e(TAG, "selectedPics:"+selectedPics);

		// 初始化数据，所有图片应在281张以内
		chooseItem.add(0);
		// imageLoader配置

		cache = new BitmapCache();

		optionBtmap = BitmapFactory.decodeResource(
				getResources(),
				R.drawable.ic_image_loadfail);
		helper = AlbumHelper.getHelper();
		helper.init(getApplicationContext());
		
		addedPath = new ArrayList<String>();
		listAdapter = new ListAdapter();
		group_listview.setAdapter(listAdapter);
		//将已经选中的照片路径标记为选中
		if(selectedPics!=null &&selectedPics.size()>0)
			addedPath.addAll(selectedPics);

//		limit_count = Constant.WGH_MAX_PIC-addedPath.size();

//		total_text.setText(addedPath.size()+"/"+Constant.WGH_MAX_PIC+"张");
		total_text.setText(addedPath.size()+"/"+max_number+"张");

		photo_title.setLeft1Show(true);
		photo_title.setLeft1(R.drawable.selector_btn_back);
		photo_title.setTitleName("图片");
		photo_title.setTvRight1Show(true);
		photo_title.setTvRight1("确定");

		options = new DisplayImageOptions.Builder().cacheOnDisc()
				.showImageOnLoading(R.drawable.ic_image_loadfail)
				.showImageForEmptyUri(R.drawable.ic_image_loadfail)
				.showImageOnFail(R.drawable.ic_image_loadfail)
				.bitmapConfig(Bitmap.Config.RGB_565)
				.cacheOnDisk(false)
				.cacheInMemory(true)
				.build();

		getImages();
		
	}

	protected void setListener() {

		photo_title.setLeft1Listener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				ChoosePhotoActivity.this.finish();
			}
		});
		photo_title.setTvRight1ClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(!addedPath.isEmpty()) {
					//addedPath返回给上个页面-----这里只选择相册里的
					Intent dataIntent = new Intent();
					dataIntent.putStringArrayListExtra("addedPath", addedPath);
					dataIntent.putStringArrayListExtra("takePics", takePics);
					setResult(RESULT_OK, dataIntent);
					ChoosePhotoActivity.this.finish();
				} else {
					ToastAlone.show( "请选择照片");
					return;
				}
			}
		});

		
		group_text.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (list_layout.getVisibility() == View.VISIBLE) {
//					list_layout.startAnimation(toDown);
					group_listview.setVisibility(View.GONE);
					TranslateAnimation animation = new TranslateAnimation(
			                Animation.RELATIVE_TO_SELF, 0,
			                Animation.RELATIVE_TO_SELF, 0,
			                Animation.RELATIVE_TO_SELF, 0,
			                Animation.RELATIVE_TO_SELF, 1);
			        animation.setDuration(300);
			        group_listview.startAnimation(animation);
					list_layout.setVisibility(View.GONE);
				} else {
					list_layout.setVisibility(View.VISIBLE);
					group_listview.setVisibility(View.VISIBLE);
			        TranslateAnimation animation = new TranslateAnimation(
			                Animation.RELATIVE_TO_SELF, 0,
			                Animation.RELATIVE_TO_SELF, 0,
			                Animation.RELATIVE_TO_SELF, 1,
			                Animation.RELATIVE_TO_SELF, 0);

			        animation.setDuration(300);
			        group_listview.startAnimation(animation);
//					list_layout.startAnimation(toUp);
				}
			}
		});


		group_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						// 点击刷新对应的视图
						if (chooseItem.get(0) == position) {
							// 不做操作，返回
							list_layout.setVisibility(View.GONE);

						} else {
							chooseItem.clear();
							chooseItem.add(position);
							listAdapter.notifyDataSetChanged();
							list_layout.setVisibility(View.GONE);

							// 获取到mAllImgs；并显示到数据中
							GridAdapter gridAdatper = new GridAdapter();
							gridAdatper.setData(new ArrayList<ImageItem>());
							gridview.setAdapter(gridAdatper);
							gridAdatper = null;

							// 得到当前的来刷新
							if (0 == position) {
								new ScanTask().execute();
								group_text.setText("所有图片");
							} else {
								// 刷新当前的GridView
//								dialogUtil.showProgressDialog();
								nowImageItems.clear();
								ImageBucket imageBucket = helper.getImagesBucketList(false).get(position - 1);
								if(null != imageBucket && !imageBucket.imageList.isEmpty()) {
									nowImageItems.addAll(imageBucket.imageList);
								}
								mHandler.sendEmptyMessageDelayed(SCAN_FOLDER_OK, 1000);
//								// 通知Handler扫描图片完成
//								getFolderImages(imageBean.getFa_filepath());
								group_text.setText(beans.get(position-1).bucketName);
							}


						}

					}
				});
		
		gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				if(chooseItem.get(0) == 0 && 0 == position) {
					//调用系统相机
					//判断已经选择的图片是否满足最大数
//					if(addedPath.size() >= max_number) {
////						ToastAlone.show(WghmidPhotoActivity.this, "最多选"+Constant.WGH_MAX_PIC+"张，请取消后再点击拍照");
//						addedPath.clear();
//						if(oldSelectedIndex > 0){
//							View childAt = gridview.getChildAt(oldSelectedIndex- gridview.getFirstVisiblePosition());
//							try{
//							  ((ImageView)childAt.findViewById(R.id.grid_img)).setImageResource(R.drawable.friends_sends_pictures_select_icon_unselected);
//							}catch(Exception e){
//								e.printStackTrace();
//							}
//						}
//					}

					tempCameraPath = tempDir + "/"+ System.currentTimeMillis() + ".jpg";
					Log.e(TAG, "拍照临时文件："+tempCameraPath);
					try {
						//调用系统相机拍照
						PickPhotoUtil.getInstance().takePhoto(
								ChoosePhotoActivity.this, "tempUser", tempCameraPath);
					}catch (Exception e){
						e.printStackTrace();
						showPermissionDialog();
					}
				}
			}
		});
	
	}
	/**
	 * 提示相机权限被拒绝
	 */
	private void showPermissionDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("提示")
				.setMessage("无相机使用权限，若希望继续此功能请到设置中开启相机权限。")
				.setPositiveButton("知道了", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {

					}
				}).show();

	}
	@Override
	protected void onDestroy() {
		/*try {
			dialogUtil.dismissProgressDialog();
		} catch (Exception e) {
		}*/
		super.onDestroy();

	}
	

	class ScanTask extends AsyncTask<Void, Void, List<ImageBucket>> {

		@Override
		protected void onPreExecute() {
//			dialogUtil.showProgressDialog();
			super.onPreExecute();
		}
		
		@Override
		protected List<ImageBucket> doInBackground(Void... arg0) {
			List<ImageBucket> imagesBucketList = helper.getImagesBucketList(false);
			return imagesBucketList;
		}
		
		@Override
		protected void onPostExecute(List<ImageBucket> result) {
			super.onPostExecute(result);
//			dialogUtil.dismissProgressDialog();
			//显示到Adapter上面
			//首先显示所有
			GridAdapter gridAdapter = new GridAdapter();
			LogUtil.e(TAG, "系统中所有图片数量="+helper.totalItems.size());
			gridAdapter.setData(helper.totalItems);
			gridview.setAdapter(gridAdapter);
			gridAdapter = null;
			if(null != result) {
				listAdapter.setData(result);
			}
		}
	}

	/**
	 * 利用ContentProvider扫描手机中的图片，此方法在运行在子线程中
	 */
	private void getImages() {
		LogUtil.v(TAG, "扫描系统图片-----------");
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			Toast.makeText(this, "暂无外部存储", Toast.LENGTH_SHORT).show();
			return;
		}
		new ScanTask().execute();
	}

	/**
	 * 组装分组界面GridView的数据源，因为我们扫描手机的时候将图片信息放在HashMap中 所以需要遍历HashMap将数据组装成List
	 * 
	 * @return
	 */
	private ArrayList<ImageBean> subGroupOfImage(
			HashMap<String, ArrayList<String>> gruopMap) {
		if (gruopMap.size() == 0) {
			return null;
		}
		ArrayList<ImageBean> list = new ArrayList<ImageBean>();
		Iterator<Map.Entry<String, ArrayList<String>>> it = gruopMap.entrySet()
				.iterator();
		ImageBean ig0 = new ImageBean();
		ig0.setFolderName("所有图片");
		ig0.setImageCounts(0);
		ig0.setTopImagePath("");
		list.add(0, ig0);
		while (it.hasNext()) {
			Map.Entry<String, ArrayList<String>> entry = it.next();
			ImageBean mImageBean = new ImageBean();
			String key = entry.getKey();
			List<String> value = entry.getValue();
			File dir_file = new File(key);
			mImageBean.setFolderName(dir_file.getName());
			mImageBean.setImageCounts(value.size());
			mImageBean.setTopImagePath(value.get(0));// 获取该组的第一张图片
			mImageBean.setFa_filepath(key);
			list.add(mImageBean);
		}

		return list;

	}

	private ArrayList<String> addedPath = null;


	private DisplayImageOptions options;

	//被选中的照片布局
	private int oldSelectedIndex;
	BitmapCache cache;
	// gridview的Adapter
	class GridAdapter extends BaseAdapter {
		// 根据三种不同的布局来应用
		final int VIEW_TYPE = 2;
		final int TYPE_1 = 0;
		final int TYPE_2 = 1;
		LayoutInflater inflater;
		private ArrayList<ImageItem> gridStrings;
		/**
		 * 用来存储图片的选中情况
		 */

		public GridAdapter() {
			gridStrings = new ArrayList<ImageItem>();
			inflater = LayoutInflater.from(ChoosePhotoActivity.this);
		}
		
		BitmapCache.ImageCallback callback = new BitmapCache.ImageCallback() {
			@Override
			public void imageLoad(ImageView imageView, Bitmap bitmap,
					Object... params) {
				if (imageView != null && bitmap != null) {
					String url = (String) params[0];
					if (url != null && url.equals((String) imageView.getTag())) {
						((ImageView) imageView).setImageBitmap(bitmap);
					} else {
						Log.e("cxm", "callback, bmp not match");
					}
				} else {
					Log.e("cxm", "callback, bmp null");
				}
			}
		};

		public void setData(ArrayList<ImageItem> strs) {
			if (null != strs) {
				gridStrings.clear();
				gridStrings.addAll(strs);
				notifyDataSetChanged();
			}
		}

		@Override
		public int getCount() {
			return gridStrings.size();
		}

		@Override
		public ImageItem getItem(int position) {
			if (chooseItem.get(0) == 0) {
				return gridStrings.get(position - 1);
			} else {
				LogUtil.e(TAG, "获取某一个图片position="+position+",path="+gridStrings.get(position));
				return gridStrings.get(position);
			}
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public int getItemViewType(int position) {
			if (chooseItem.get(0) == 0) {
				if (position == 0) {
					return TYPE_1;
				} else {
					return TYPE_2;
				}
			} else {
				return TYPE_2;
			}
		}

		@Override
		public int getViewTypeCount() {
			if (chooseItem.get(0) == 0) {
				return VIEW_TYPE;
			} else {
				return 1;
			}
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup arg2) {
			GridHolder gridHolder = null  ;
			PhotoHolder photoHodler = null;
			int type = getItemViewType(position);
			if (convertView == null) {
				switch (type) {
				case TYPE_1:
					// 显示拍照
					photoHodler = new PhotoHolder();
					convertView = inflater.inflate(R.layout.take_photo, null);
					photoHodler.grid_item_layout = (LinearLayout)convertView.findViewById(R.id.grid_item_layout);
					ViewGroup.LayoutParams params = photoHodler.grid_item_layout.getLayoutParams();
					params.width = columnWidth;
					params.height = columnWidth;
					photoHodler.grid_item_layout.setLayoutParams(params);
					convertView.setTag(photoHodler);
					break;
				case TYPE_2:
					convertView = inflater.inflate(R.layout.grid_item, null);
					gridHolder = new GridHolder();
					gridHolder.grid_image = (ImageView) convertView.findViewById(R.id.grid_image);
					gridHolder.grid_img = (ImageView) convertView.findViewById(R.id.grid_img);
					gridHolder.grid_item_layout = (RelativeLayout) convertView.findViewById(R.id.grid_item_layout);
					params = gridHolder.grid_item_layout.getLayoutParams();
					params.width = columnWidth;
					params.height = columnWidth;
					gridHolder.grid_item_layout.setLayoutParams(params);
					convertView.setTag(gridHolder);
					break;
				default:
					break;
				}
			} else {
				switch (type) {
				case TYPE_1:
					// 显示拍照
					photoHodler = (PhotoHolder) convertView.getTag();
					break;
				case TYPE_2:
					gridHolder = (GridHolder) convertView.getTag();
					break;
				default:
					break;
				}
			}

			if (type == TYPE_2) {
				// 判断是否已经添加
				String thumb_path = getItem(position).thumbnailPath;
				String img_path = getItem(position).imagePath;
				gridHolder.grid_image.setTag(img_path);
//				cache.displayBmp(gridHolder.grid_image, thumb_path, img_path,callback, optionBtmap);
				LogUtil.i(TAG, "展示图片："+thumb_path);
				LogUtil.i(TAG, "---展示图片："+img_path);
				if(!TextUtils.isEmpty(thumb_path)){
					imageLoader.displayImage("file://" + thumb_path, gridHolder.grid_image, options);

				}else if(!TextUtils.isEmpty(img_path)){
					imageLoader.displayImage("file://" + img_path, gridHolder.grid_image, options);
				}
				gridHolder.grid_item_layout.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if(addedPath.contains(getItem(position).imagePath)) {
							//已经包含这个path了，取消选中状态
							addedPath.remove(getItem(position).imagePath);
							((ImageView)view.findViewById(R.id.grid_img)).setImageResource(R.drawable.friends_sends_pictures_select_icon_unselected);
						} else {
							//如果点击的照片没有被选中，应该置为选中状态
							addedPath.clear();
							takePics.clear();
							addedPath.add(getItem(position).imagePath);
							((ImageView)view.findViewById(R.id.grid_img)).setImageResource(R.drawable.friends_sends_pictures_select_icon_selected);
							if(oldSelectedIndex > 0){
								View childAt = gridview.getChildAt(oldSelectedIndex- gridview.getFirstVisiblePosition());
								try{
									((ImageView)childAt.findViewById(R.id.grid_img)).setImageResource(R.drawable.friends_sends_pictures_select_icon_unselected);
								}catch(Exception e){
									e.printStackTrace();
								}
							}
							oldSelectedIndex = position;
						}
						mYhandler.sendEmptyMessage(0);
					}
				});
				if (addedPath.contains(getItem(position).imagePath)) {
					// 已经添加过了
					gridHolder.grid_img.setImageResource(R.drawable.friends_sends_pictures_select_icon_selected);
					oldSelectedIndex = position;
				} else {
					gridHolder.grid_img.setImageResource(R.drawable.friends_sends_pictures_select_icon_unselected);
				}
			}

			return convertView;
		}

		class PhotoHolder {
			LinearLayout grid_item_layout;
		}

		class GridHolder {
			ImageView grid_image;
			public ImageView grid_img;
			RelativeLayout grid_item_layout;
		}

	}
	
	Handler mYhandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
//				total_text.setText(addedPath.size()+"/"+Constant.WGH_MAX_PIC+"张");
				total_text.setText(addedPath.size()+"/"+max_number+"张");
				break;

			default:
				break;
			}
		}
	};

	private ArrayList<Integer> chooseItem = new ArrayList<Integer>();
	private ArrayList<ImageBucket> beans;

	class ListAdapter extends BaseAdapter {
		LayoutInflater inflater;

		public ListAdapter() {
			inflater = LayoutInflater.from(ChoosePhotoActivity.this);
			beans = new ArrayList<ImageBucket>();
		}
		
		BitmapCache.ImageCallback listallback = new BitmapCache.ImageCallback() {
			@Override
			public void imageLoad(ImageView imageView, Bitmap bitmap,
					Object... params) {
				if (imageView != null && bitmap != null) {
					String url = (String) params[0];
					if (url != null && url.equals((String) imageView.getTag())) {
						((ImageView) imageView).setImageBitmap(bitmap);
					} else {
						Log.e("cxm", "list--callback, bmp not match");
					}
				} else {
					Log.e("cxm", "list--callback, bmp null");
				}
			}
		};

		public void setData(List<ImageBucket> listBeans) {
			if (listBeans != null) {
				beans.clear();
				beans.addAll(listBeans);
				notifyDataSetChanged();
			}
		}

		@Override
		public int getCount() {
			return beans.size();
		}

		@Override
		public ImageBucket getItem(int arg0) {
			return beans.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup arg2) {
			final ListViewHolder listHoder;
//			ImageBucket imageBean = beans.get(position);
			if (convertView == null) {
				listHoder = new ListViewHolder();
				convertView = inflater.inflate(R.layout.list_item, null);
				listHoder.myimage_view = (ImageView) convertView
						.findViewById(R.id.myimage_view);
				listHoder.choose_img = (ImageView) convertView
						.findViewById(R.id.choose_img);
				listHoder.folder_text = (TextView) convertView
						.findViewById(R.id.folder_text);
				listHoder.count_text = (TextView) convertView
						.findViewById(R.id.count_text);
				convertView.setTag(listHoder);
			} else {
				listHoder = (ListViewHolder) convertView.getTag();
			}
			int cho_posi = chooseItem.get(0);
			if (position == cho_posi) {
				// 相等则显示
				listHoder.choose_img.setVisibility(View.VISIBLE);
			} else {
				listHoder.choose_img.setVisibility(View.GONE);
			}
//			String img_path = "";
//			if (position == 0) {
//				img_path = beans.get(1).getTopImagePath();
//				listHoder.count_text.setVisibility(View.GONE);
//			} else {
//				img_path = imageBean.getTopImagePath();
//				listHoder.count_text.setVisibility(View.VISIBLE);
//				listHoder.count_text.setText(imageBean.getImageCounts()+"张");
//			}
//			listHoder.folder_text.setText(imageBean.getFolderName());
//			imageLoader.displayImage("file://" + img_path,
//					listHoder.myimage_view, options);
			if (position == 0) {
				if(!beans.isEmpty()) {
					List<ImageItem> imageList = beans.get(0).imageList;
					if(!imageList.isEmpty()) {
						ImageItem imageItem = imageList.get(0);
						listHoder.myimage_view.setTag(imageItem.imagePath);

						cache.displayBmp(listHoder.myimage_view, imageItem.thumbnailPath, imageItem.imagePath,
								listallback, optionBtmap);
					}
				}
				listHoder.folder_text.setText("所有图片");
				listHoder.count_text.setVisibility(View.GONE);
			} else {
				if(!beans.isEmpty()) {
					List<ImageItem> imageList = beans.get(position -1).imageList;
					if(!imageList.isEmpty()) {
						ImageItem imageItem = imageList.get(0);
						listHoder.myimage_view.setTag(imageItem.imagePath);
						cache.displayBmp(listHoder.myimage_view, imageItem.thumbnailPath, imageItem.imagePath,
								listallback, optionBtmap);
					}
				}
				listHoder.count_text.setVisibility(View.VISIBLE);
				listHoder.count_text.setText(beans.get(position -1).count+"张");
				listHoder.folder_text.setText(beans.get(position -1).bucketName);
			}
			return convertView;
		}

		class ListViewHolder {
			ImageView myimage_view;
			ImageView choose_img;
			TextView folder_text, count_text;
		}

	}
}
