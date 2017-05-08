package com.openxu.pigpic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.openxu.pigpic.bean.ImageBean;
import com.openxu.pigpic.bean.ImageBucket;
import com.openxu.pigpic.bean.ImageItem;
import com.openxu.pigpic.util.AlbumHelper;
import com.openxu.pigpic.util.BitmapCache;
import com.openxu.pigpic.util.DensityUtil;
import com.openxu.pigpic.util.LogUtil;

import java.io.File;
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
 */
public class ChoosePhotoActivity extends UploadPicBaseActivity implements View.OnClickListener{
	private String TAG = "ChoosePhotoActivity";

	private Context mContext;

	private ImageView iv_back;
	private TextView tv_scan, tv_upload;
	private GridView gridview;
	private GridAdapter gridAdapter;
	private int numColumns = 4;
	private int columnWidth;   //gridView每列的宽度
	private int max_number;

	private AlbumHelper helper;
	protected ImageLoader imageLoader;

	private Bitmap optionBtmap = null;

	private ArrayList<String> addedPath;

	private int text_scan_color_unenable;
	private int text_scan_color_enable;

	private DisplayImageOptions options;

	BitmapCache cache;

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
		iv_back = (ImageView) findViewById(R.id.iv_back);
		gridview = (GridView) findViewById(R.id.gridview);
		tv_scan = (TextView) findViewById(R.id.tv_scan);
		tv_upload = (TextView) findViewById(R.id.tv_upload);

		text_scan_color_unenable = mContext.getResources().getColor(R.color.choosepic_scantext_unenable);
		text_scan_color_enable = mContext.getResources().getColor(R.color.choosepic_scantext_enable);
		tv_scan.setTextColor(text_scan_color_unenable);
		tv_upload.setEnabled(false);
	}

	protected void initData() {
		imageLoader = ImageLoader.getInstance();

		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		int screenWidth = displayMetrics.widthPixels;
//		int columnWidth = gridview.getColumnWidth();  //lev 13
//		int horizontalSpacing = gridview.getHorizontalSpacing();
		int horizontalSpacing = DensityUtil.dip2px(mContext,5);
		columnWidth = (screenWidth - (horizontalSpacing*(numColumns-1)))/numColumns;

		max_number = getIntent().getIntExtra("max_number", 1);

		// 初始化数据，所有图片应在281张以内
		chooseItem.add(0);
		// imageLoader配置

		cache = new BitmapCache();

		optionBtmap = BitmapFactory.decodeResource(getResources(),R.drawable.choosepic_icon_def);
		helper = AlbumHelper.getHelper();
		helper.init(getApplicationContext());
		
		gridAdapter = new GridAdapter();
		gridview.setAdapter(gridAdapter);

		addedPath = new ArrayList<>();

		options = new DisplayImageOptions.Builder().cacheOnDisc()
				.showImageOnLoading(R.drawable.choosepic_icon_def)
				.showImageForEmptyUri(R.drawable.choosepic_icon_def)
				.showImageOnFail(R.drawable.choosepic_icon_def)
				.bitmapConfig(Bitmap.Config.RGB_565)
				.cacheOnDisk(false)
				.cacheInMemory(true)
				.build();

		getImages();
		
	}


	protected void setListener() {

		iv_back.setOnClickListener(this);
		tv_scan.setOnClickListener(this);
		tv_upload.setOnClickListener(this);

	}
	@Override
	public void onClick(View v){
		switch (v.getId()){
			case R.id.iv_back:
				finish();
				break;
			case R.id.tv_upload:
				Intent dataIntent = new Intent();
				dataIntent.putStringArrayListExtra("addedPath", addedPath);
				setResult(RESULT_OK, dataIntent);
				finish();
				break;
			case R.id.tv_scan:
				Intent intent = new Intent(mContext, PicScanActivity.class);
				intent.putStringArrayListExtra("imgs", addedPath);
				startActivityForResult(intent, 100);
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode==100){
			addedPath = data.getStringArrayListExtra("imgs");
			gridAdapter.notifyDataSetChanged();
			mYhandler.sendEmptyMessage(0);
		}
	}

	/**
	 * 利用ContentProvider扫描手机中的图片，此方法在运行在子线程中
	 */
	private void getImages() {
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			Toast.makeText(this, "暂无外部存储", Toast.LENGTH_SHORT).show();
			return;
		}
		new ScanTask().execute();
	}

	class ScanTask extends AsyncTask<Void, Void, List<ImageBucket>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
		@Override
		protected List<ImageBucket> doInBackground(Void... arg0) {
			List<ImageBucket> imagesBucketList = helper.getImagesBucketList(false);
			return imagesBucketList;
		}
		
		@Override
		protected void onPostExecute(List<ImageBucket> result) {
//			LogUtil.e(TAG, "系统中所有图片数量="+helper.totalItems.size());
			gridAdapter.setData(helper.totalItems);
		}
	}

	/**
	 * 组装分组界面GridView的数据源，因为我们扫描手机的时候将图片信息放在HashMap中 所以需要遍历HashMap将数据组装成List
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

	class GridAdapter extends BaseAdapter {
		LayoutInflater inflater;
		private ArrayList<ImageItem> imageList;

		public GridAdapter() {
			imageList = new ArrayList<>();
			inflater = LayoutInflater.from(ChoosePhotoActivity.this);
		}
		
		BitmapCache.ImageCallback callback = new BitmapCache.ImageCallback() {
			@Override
			public void imageLoad(ImageView imageView, Bitmap bitmap,
					Object... params) {
				if (imageView != null && bitmap != null) {
					String url = (String) params[0];
					if (url != null && url.equals(imageView.getTag())) {
						imageView.setImageBitmap(bitmap);
					} else {
						Log.e(TAG, "callback, bmp not match");
					}
				} else {
					Log.e(TAG, "callback, bmp null");
				}
			}
		};

		public void setData(ArrayList<ImageItem> strs) {
			if (null != strs) {
				imageList.clear();
				imageList.addAll(strs);
				notifyDataSetChanged();
			}
		}

		@Override
		public int getCount() {
			return imageList.size();
		}

		@Override
		public ImageItem getItem(int position) {
			return imageList.get(position);
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup arg2) {
			GridHolder gridHolder = null  ;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.choosepic_grid_item, null);
				gridHolder = new GridHolder();
				gridHolder.iv_image = (ImageView) convertView.findViewById(R.id.iv_image);
				gridHolder.rl_item = (RelativeLayout) convertView.findViewById(R.id.rl_item);
				gridHolder.rl_selected = (RelativeLayout) convertView.findViewById(R.id.rl_selected);
				ViewGroup.LayoutParams params = gridHolder.rl_item.getLayoutParams();
				params.width = columnWidth;
				params.height = columnWidth;
				gridHolder.rl_item.setLayoutParams(params);
				convertView.setTag(gridHolder);
			} else {
				gridHolder = (GridHolder) convertView.getTag();
			}
			final ImageItem imageItem = getItem(position);
			String thumb_path = imageItem.thumbnailPath;
			String img_path = imageItem.imagePath;
			gridHolder.iv_image.setTag(img_path);
			if(!TextUtils.isEmpty(thumb_path)){
				imageLoader.displayImage("file://" + thumb_path, gridHolder.iv_image, options);
			}else if(!TextUtils.isEmpty(img_path)){
				imageLoader.displayImage("file://" + img_path, gridHolder.iv_image, options);
			}
			gridHolder.rl_item.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if(addedPath.contains(imageItem.imagePath)) {
						addedPath.remove(imageItem.imagePath);
						view.findViewById(R.id.rl_selected).setVisibility(View.GONE);
					} else {
						if(addedPath.size()>=max_number){
							Toast.makeText(mContext,"最多选取"+max_number+"张图片",Toast.LENGTH_SHORT).show();
							return;
						}
						addedPath.add(imageItem.imagePath);
						view.findViewById(R.id.rl_selected).setVisibility(View.VISIBLE);
					}
					mYhandler.sendEmptyMessage(0);
				}
			});
			gridHolder.rl_selected.setVisibility(addedPath.contains(imageItem.imagePath)
					?View.VISIBLE:View.GONE);

			return convertView;
		}

		class GridHolder {
			ImageView iv_image;
			RelativeLayout rl_item, rl_selected;
		}

	}

	Handler mYhandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				if(addedPath.size()>0){
					tv_upload.setText("上传("+addedPath.size()+"/"+max_number+")");
				}else{
					tv_upload.setText("上传");
				}
				tv_scan.setTextColor(addedPath.size()>0?text_scan_color_enable:text_scan_color_unenable);
				tv_upload.setEnabled(addedPath.size()>0?true:false);
				tv_scan.setEnabled(addedPath.size()>0?true:false);
				break;
			default:
				break;
			}
		}
	};

	private ArrayList<Integer> chooseItem = new ArrayList<Integer>();

}
