package com.openxu.pigpic.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore.Audio.Albums;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Images.Thumbnails;
import android.util.Log;


import com.openxu.pigpic.bean.ImageBucket;
import com.openxu.pigpic.bean.ImageItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class AlbumHelper {
	final String TAG = getClass().getSimpleName();
	Context context;
	ContentResolver cr;

	// 缩略图<原图id, 缩略图路径>
	HashMap<String, String> thumbnailList = new HashMap<>();
	List<HashMap<String, String>> albumList = new ArrayList<>();
	// 所有图片文件夹<文件夹id, 文件夹内图片集合>
	HashMap<String, ImageBucket> bucketList = new HashMap<>();
	//所有图片的集合（包含缩略图和原图路径）
	public ArrayList<ImageItem> totalItems = new ArrayList<>();

	private static AlbumHelper instance;

	private AlbumHelper() {
	}

	public static AlbumHelper getHelper() {
		if (instance == null) {
			instance = new AlbumHelper();
		}
		return instance;
	}

	/**
	 * ��ʼ��
	 * 
	 * @param context
	 */
	public void init(Context context) {
		if (this.context == null) {
			this.context = context;
			cr = context.getContentResolver();
		}
	}

	/**
	 * 查询缩略图表
	 */
	private void getThumbnail() {
		String[] projection = { Thumbnails._ID, Thumbnails.IMAGE_ID,
				Thumbnails.DATA };
		Cursor cursor = cr.query(Thumbnails.EXTERNAL_CONTENT_URI, projection,
				null, null, null);
		LogUtil.d(TAG, ">>>>查询缩略图"+cursor.getCount()+"张");
		getThumbnailColumnData(cursor);
	}

	/**
	 * 解析缩略图游标
	 * @param cur
	 */
	private void getThumbnailColumnData(Cursor cur) {
		if (cur.moveToFirst()) {
			int _id;
			int image_id;
			String image_path;
			int _idColumn = cur.getColumnIndex(Thumbnails._ID);
			int image_idColumn = cur.getColumnIndex(Thumbnails.IMAGE_ID);
			int dataColumn = cur.getColumnIndex(Thumbnails.DATA);

			do {
				// Get the field values
				_id = cur.getInt(_idColumn);
				image_id = cur.getInt(image_idColumn);
				image_path = cur.getString(dataColumn);

				// Do something with the values.
				// Log.i(TAG, _id + " image_id:" + image_id + " path:"
				// + image_path + "---");
				// HashMap<String, String> hash = new HashMap<String, String>();
				// hash.put("image_id", image_id + "");
				// hash.put("path", image_path);
				// thumbnailList.add(hash);
				LogUtil.i(TAG, "---解析缩略图：image_id="+image_id + ", image_path=" + image_path + "---");
				thumbnailList.put("" + image_id, image_path);
			} while (cur.moveToNext());
		}
	}

	/**
	 * �õ�ԭͼ
	 */
	void getAlbum() {
		String[] projection = { Albums._ID, Albums.ALBUM, Albums.ALBUM_ART,
				Albums.ALBUM_KEY, Albums.ARTIST, Albums.NUMBER_OF_SONGS };
		Cursor cursor = cr.query(Albums.EXTERNAL_CONTENT_URI, projection, null,
				null, null);
		getAlbumColumnData(cursor);

	}

	/**
	 * �ӱ�����ݿ��еõ�ԭͼ
	 * 
	 * @param cur
	 */
	private void getAlbumColumnData(Cursor cur) {
		if (cur.moveToFirst()) {
			int _id;
			String album;
			String albumArt;
			String albumKey;
			String artist;
			int numOfSongs;

			int _idColumn = cur.getColumnIndex(Albums._ID);
			int albumColumn = cur.getColumnIndex(Albums.ALBUM);
			int albumArtColumn = cur.getColumnIndex(Albums.ALBUM_ART);
			int albumKeyColumn = cur.getColumnIndex(Albums.ALBUM_KEY);
			int artistColumn = cur.getColumnIndex(Albums.ARTIST);
			int numOfSongsColumn = cur.getColumnIndex(Albums.NUMBER_OF_SONGS);

			do {
				// Get the field values
				_id = cur.getInt(_idColumn);
				album = cur.getString(albumColumn);
				albumArt = cur.getString(albumArtColumn);
				albumKey = cur.getString(albumKeyColumn);
				artist = cur.getString(artistColumn);
				numOfSongs = cur.getInt(numOfSongsColumn);

				// Do something with the values.
				Log.i(TAG, _id + " album:" + album + " albumArt:" + albumArt
						+ "albumKey: " + albumKey + " artist: " + artist
						+ " numOfSongs: " + numOfSongs + "---");
				HashMap<String, String> hash = new HashMap<String, String>();
				hash.put("_id", _id + "");
				hash.put("album", album);
				hash.put("albumArt", albumArt);
				hash.put("albumKey", albumKey);
				hash.put("artist", artist);
				hash.put("numOfSongs", numOfSongs + "");
				albumList.add(hash);

			} while (cur.moveToNext());

		}
	}

	/**
	 * �Ƿ񴴽���ͼƬ��
	 */
	boolean hasBuildImagesBucketList = false;

	/**
	 * �õ�ͼƬ��
	 */
	void buildImagesBucketList() {
		LogUtil.d(TAG, ">>>>开始查找图片");
		long startTime = System.currentTimeMillis();
		int total_count = 0;
		// 获取缩略图
		getThumbnail();

		// 实际图片需要查询的字段
		String columns[] = new String[] { Media._ID, Media.BUCKET_ID,
				Media.PICASA_ID, Media.DATA, Media.DISPLAY_NAME, Media.TITLE,
				Media.SIZE, Media.BUCKET_DISPLAY_NAME };
		// 查询实际图片（时间降序）
		Cursor cur = cr.query(Media.EXTERNAL_CONTENT_URI, columns, null, null,
				Media.DATE_ADDED+" desc");
		if (cur.moveToFirst()) {
			int photoIDIndex = cur.getColumnIndexOrThrow(Media._ID);           //原图id
			int photoPathIndex = cur.getColumnIndexOrThrow(Media.DATA);        //原图路径
			int photoNameIndex = cur.getColumnIndexOrThrow(Media.DISPLAY_NAME);
			int photoTitleIndex = cur.getColumnIndexOrThrow(Media.TITLE);
			int photoSizeIndex = cur.getColumnIndexOrThrow(Media.SIZE);
			int bucketDisplayNameIndex = cur
					.getColumnIndexOrThrow(Media.BUCKET_DISPLAY_NAME);
			int bucketIdIndex = cur.getColumnIndexOrThrow(Media.BUCKET_ID);
			int picasaIdIndex = cur.getColumnIndexOrThrow(Media.PICASA_ID);
			int totalNum = cur.getCount();

			do {
				String _id = cur.getString(photoIDIndex);
				String name = cur.getString(photoNameIndex);
				String path = cur.getString(photoPathIndex);
				String title = cur.getString(photoTitleIndex);
				String size = cur.getString(photoSizeIndex);
				String bucketName = cur.getString(bucketDisplayNameIndex);
				String bucketId = cur.getString(bucketIdIndex);
				String picasaId = cur.getString(picasaIdIndex);

				LogUtil.i(TAG, "---解析原图：_id="+_id + ", bucketId=" + bucketId + ", picasaId="
						+ picasaId + " name=" + name + " path=" + path
						+ " title=" + title + " size=" + size + " bucket="
						+ bucketName + "---");

				ImageBucket bucket = bucketList.get(bucketId);
				if (bucket == null) {
					bucket = new ImageBucket();
					bucketList.put(bucketId, bucket);
					bucket.imageList = new ArrayList<>();
					bucket.bucketName = bucketName;
				}
				bucket.count++;
				ImageItem imageItem = new ImageItem();
				imageItem.imageId = _id;
				imageItem.imagePath = path;
				imageItem.thumbnailPath = thumbnailList.get(_id);
				total_count++;
				if(total_count < 281) {
					totalItems.add(imageItem);
				}
				bucket.imageList.add(imageItem);

			} while (cur.moveToNext());
		}

		Iterator<Entry<String, ImageBucket>> itr = bucketList.entrySet()
				.iterator();
		while (itr.hasNext()) {
			Entry<String, ImageBucket> entry = itr.next();
			ImageBucket bucket = entry.getValue();
			LogUtil.w(TAG, "文件夹id="+entry.getKey() + ", 名称=" + bucket.bucketName + ", 图片数量="
					+ bucket.count);
			for (int i = 0; i < bucket.imageList.size(); ++i) {
				ImageItem image = bucket.imageList.get(i);
				LogUtil.w(TAG, "文件夹图片id=" + image.imageId + ", 路径=" + image.imagePath
						+ ", 缩略图路径=" + image.thumbnailPath);
			}
		}
		hasBuildImagesBucketList = true;
		long endTime = System.currentTimeMillis();
		LogUtil.v(TAG, "<<<<查找图片完毕，总耗时: " + (endTime - startTime) + " ms");
	}

	/**
	 * �õ�ͼƬ��
	 * 
	 * @param refresh
	 * @return
	 */
	public List<ImageBucket> getImagesBucketList(boolean refresh) {
		if (refresh || (!refresh && !hasBuildImagesBucketList)) {
			buildImagesBucketList();
		}
		List<ImageBucket> tmpList = new ArrayList<ImageBucket>();
		Iterator<Entry<String, ImageBucket>> itr = bucketList.entrySet()
				.iterator();
		while (itr.hasNext()) {
			Entry<String, ImageBucket> entry = itr.next();
			tmpList.add(entry.getValue());
		}
		return tmpList;
	}

	/**
	 * �õ�ԭʼͼ��·��
	 * 
	 * @param image_id
	 * @return
	 */
	String getOriginalImagePath(String image_id) {
		String path = null;
		Log.i(TAG, "---(^o^)----" + image_id);
		String[] projection = { Media._ID, Media.DATA };
		Cursor cursor = cr.query(Media.EXTERNAL_CONTENT_URI, projection,
				Media._ID + "=" + image_id, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
			path = cursor.getString(cursor.getColumnIndex(Media.DATA));

		}
		return path;
	}

}