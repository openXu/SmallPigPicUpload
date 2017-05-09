/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.openxu.pigpic.view;

import android.content.Context;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.WrapperListAdapter;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.openxu.pigpic.R;
import com.openxu.pigpic.UploadPicActivity;
import com.openxu.pigpic.bean.UploadPic;
import com.openxu.pigpic.callback.ImageViewEventCallBack;
import com.openxu.pigpic.util.DensityUtil;
import com.openxu.pigpic.util.LogUtil;

import java.util.ArrayList;

/**
 * A {@link GridView} that supports adding header rows in a
 * very similar way to {@link ListView}.
 * See {@link HeaderGridView#addHeaderView(View, Object, boolean)}
 */
public class HeaderGridView extends GridView {
    private static final String TAG = "HeaderGridView";

    /**
     * A class that represents a fixed view in a list, for example a header at the top
     * or a footer at the bottom.
     */
    private static class FixedViewInfo {
        /** The view to add to the grid */
        public View view;
        public ViewGroup viewContainer;
        /** The data backing the view. This is returned from {@link ListAdapter#getItem(int)}. */
        public Object data;
        /** <code>true</code> if the fixed view should be selectable in the grid */
        public boolean isSelectable;
    }

    private ArrayList<FixedViewInfo> mHeaderViewInfos = new ArrayList<FixedViewInfo>();

    public HeaderGridView(Context context) {
        super(context);
        initHeaderGridView();
    }

    public HeaderGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initHeaderGridView();
    }

    public HeaderGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initHeaderGridView();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        ListAdapter adapter = getAdapter();
        if (adapter != null && adapter instanceof HeaderViewGridAdapter) {
            ((HeaderViewGridAdapter) adapter).setNumColumns(getNumColumns());
        }
    }

    @Override
    public void setClipChildren(boolean clipChildren) {
       // Ignore, since the header rows depend on not being clipped
    }

    /**
     * Add a fixed view to appear at the top of the grid. If addHeaderView is
     * called more than once, the views will appear in the order they were
     * added. Views added using this call can take focus if they want.
     * <p>
     * NOTE: Call this before calling setAdapter. This is so HeaderGridView can wrap
     * the supplied cursor with one that will also account for header views.
     *
     * @param v The view to add.
     * @param data Data to associate with this view
     * @param isSelectable whether the item is selectable
     */
    public void addHeaderView(View v, Object data, boolean isSelectable) {
        ListAdapter adapter = getAdapter();

        if (adapter != null && ! (adapter instanceof HeaderViewGridAdapter)) {
            throw new IllegalStateException(
                    "Cannot add header view to grid -- setAdapter has already been called.");
        }

        FixedViewInfo info = new FixedViewInfo();
        FrameLayout fl = new FullWidthFixedViewLayout(getContext());
        fl.addView(v);
        info.view = v;
        info.viewContainer = fl;
        info.data = data;
        info.isSelectable = isSelectable;
        mHeaderViewInfos.add(info);

        // in the case of re-adding a header view, or adding one later on,
        // we need to notify the observer
        if (adapter != null) {
            ((HeaderViewGridAdapter) adapter).notifyDataSetChanged();
        }
    }

    /**
     * Add a fixed view to appear at the top of the grid. If addHeaderView is
     * called more than once, the views will appear in the order they were
     * added. Views added using this call can take focus if they want.
     * <p>
     * NOTE: Call this before calling setAdapter. This is so HeaderGridView can wrap
     * the supplied cursor with one that will also account for header views.
     *
     * @param v The view to add.
     */
    public void addHeaderView(View v) {
        addHeaderView(v, null, true);
    }

    public int getHeaderViewCount() {
        return mHeaderViewInfos.size();
    }

    /**
     * Removes a previously-added header view.
     *
     * @param v The view to remove
     * @return true if the view was removed, false if the view was not a header
     *         view
     */
    public boolean removeHeaderView(View v) {
        if (mHeaderViewInfos.size() > 0) {
            boolean result = false;
            ListAdapter adapter = getAdapter();
            if (adapter != null && ((HeaderViewGridAdapter) adapter).removeHeader(v)) {
                result = true;
            }
            removeFixedViewInfo(v, mHeaderViewInfos);
            return result;
        }
        return false;
    }

    private void removeFixedViewInfo(View v, ArrayList<FixedViewInfo> where) {
        int len = where.size();
        for (int i = 0; i < len; ++i) {
            FixedViewInfo info = where.get(i);
            if (info.view == v) {
                where.remove(i);
                break;
            }
        }
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if (mHeaderViewInfos.size() > 0) {
            HeaderViewGridAdapter hadapter = new HeaderViewGridAdapter(mHeaderViewInfos, adapter);
            int numColumns = getNumColumns();
            if (numColumns > 1) {
                hadapter.setNumColumns(numColumns);
            }
            super.setAdapter(hadapter);
        } else {
            super.setAdapter(adapter);
        }
    }

    private class FullWidthFixedViewLayout extends FrameLayout {
        public FullWidthFixedViewLayout(Context context) {
            super(context);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int targetWidth = HeaderGridView.this.getMeasuredWidth()
                    - HeaderGridView.this.getPaddingLeft()
                    - HeaderGridView.this.getPaddingRight();
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(targetWidth,
                    MeasureSpec.getMode(widthMeasureSpec));
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    /**
     * ListAdapter used when a HeaderGridView has header views. This ListAdapter
     * wraps another one and also keeps track of the header views and their
     * associated data objects.
     *<p>This is intended as a base class; you will probably not need to
     * use this class directly in your own code.
     */
    private static class HeaderViewGridAdapter implements WrapperListAdapter, Filterable {

        // This is used to notify the container of updates relating to number of columns
        // or headers changing, which changes the number of placeholders needed
        private final DataSetObservable mDataSetObservable = new DataSetObservable();

        private final ListAdapter mAdapter;
        private int mNumColumns = 1;

        // This ArrayList is assumed to NOT be null.
        ArrayList<FixedViewInfo> mHeaderViewInfos;

        boolean mAreAllFixedViewsSelectable;

        private final boolean mIsFilterable;

        public HeaderViewGridAdapter(ArrayList<FixedViewInfo> headerViewInfos, ListAdapter adapter) {
            mAdapter = adapter;
            mIsFilterable = adapter instanceof Filterable;

            if (headerViewInfos == null) {
                throw new IllegalArgumentException("headerViewInfos cannot be null");
            }
            mHeaderViewInfos = headerViewInfos;

            mAreAllFixedViewsSelectable = areAllListInfosSelectable(mHeaderViewInfos);
        }

        public int getHeadersCount() {
            return mHeaderViewInfos.size();
        }

        @Override
        public boolean isEmpty() {
            return (mAdapter == null || mAdapter.isEmpty()) && getHeadersCount() == 0;
        }

        public void setNumColumns(int numColumns) {
            if (numColumns < 1) {
                throw new IllegalArgumentException("Number of columns must be 1 or more");
            }
            if (mNumColumns != numColumns) {
                mNumColumns = numColumns;
                notifyDataSetChanged();
            }
        }

        private boolean areAllListInfosSelectable(ArrayList<FixedViewInfo> infos) {
            if (infos != null) {
                for (FixedViewInfo info : infos) {
                    if (!info.isSelectable) {
                        return false;
                    }
                }
            }
            return true;
        }

        public boolean removeHeader(View v) {
            for (int i = 0; i < mHeaderViewInfos.size(); i++) {
                FixedViewInfo info = mHeaderViewInfos.get(i);
                if (info.view == v) {
                    mHeaderViewInfos.remove(i);

                    mAreAllFixedViewsSelectable = areAllListInfosSelectable(mHeaderViewInfos);

                    mDataSetObservable.notifyChanged();
                    return true;
                }
            }

            return false;
        }

        @Override
        public int getCount() {
            if (mAdapter != null) {
                return getHeadersCount() * mNumColumns + mAdapter.getCount();
            } else {
                return getHeadersCount() * mNumColumns;
            }
        }

        @Override
        public boolean areAllItemsEnabled() {
            if (mAdapter != null) {
                return mAreAllFixedViewsSelectable && mAdapter.areAllItemsEnabled();
            } else {
                return true;
            }
        }

        @Override
        public boolean isEnabled(int position) {
            // Header (negative positions will throw an ArrayIndexOutOfBoundsException)
            int numHeadersAndPlaceholders = getHeadersCount() * mNumColumns;
            if (position < numHeadersAndPlaceholders) {
                return (position % mNumColumns == 0)
                        && mHeaderViewInfos.get(position / mNumColumns).isSelectable;
            }

            // Adapter
            final int adjPosition = position - numHeadersAndPlaceholders;
            int adapterCount = 0;
            if (mAdapter != null) {
                adapterCount = mAdapter.getCount();
                if (adjPosition < adapterCount) {
                    return mAdapter.isEnabled(adjPosition);
                }
            }

            throw new ArrayIndexOutOfBoundsException(position);
        }

        @Override
        public Object getItem(int position) {
            // Header (negative positions will throw an ArrayIndexOutOfBoundsException)
            int numHeadersAndPlaceholders = getHeadersCount() * mNumColumns;
            if (position < numHeadersAndPlaceholders) {
                if (position % mNumColumns == 0) {
                    return mHeaderViewInfos.get(position / mNumColumns).data;
                }
                return null;
            }

            // Adapter
            final int adjPosition = position - numHeadersAndPlaceholders;
            int adapterCount = 0;
            if (mAdapter != null) {
                adapterCount = mAdapter.getCount();
                if (adjPosition < adapterCount) {
                    return mAdapter.getItem(adjPosition);
                }
            }

            throw new ArrayIndexOutOfBoundsException(position);
        }

        @Override
        public long getItemId(int position) {
            int numHeadersAndPlaceholders = getHeadersCount() * mNumColumns;
            if (mAdapter != null && position >= numHeadersAndPlaceholders) {
                int adjPosition = position - numHeadersAndPlaceholders;
                int adapterCount = mAdapter.getCount();
                if (adjPosition < adapterCount) {
                    return mAdapter.getItemId(adjPosition);
                }
            }
            return -1;
        }

        @Override
        public boolean hasStableIds() {
            if (mAdapter != null) {
                return mAdapter.hasStableIds();
            }
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Header (negative positions will throw an ArrayIndexOutOfBoundsException)
            int numHeadersAndPlaceholders = getHeadersCount() * mNumColumns ;
            if (position < numHeadersAndPlaceholders) {
                View headerViewContainer = mHeaderViewInfos
                        .get(position / mNumColumns).viewContainer;
                if (position % mNumColumns == 0) {
                    return headerViewContainer;
                } else {
                    if (convertView == null) {
                        convertView = new View(parent.getContext());
                    }
                    // We need to do this because GridView uses the height of the last item
                    // in a row to determine the height for the entire row.
                    convertView.setVisibility(View.INVISIBLE);
                    convertView.setMinimumHeight(headerViewContainer.getHeight());
                    return convertView;
                }
            }

            // Adapter
            final int adjPosition = position - numHeadersAndPlaceholders;
            int adapterCount = 0;
            if (mAdapter != null) {
                adapterCount = mAdapter.getCount();
                if (adjPosition < adapterCount) {
                    return mAdapter.getView(adjPosition, convertView, parent);
                }
            }

            throw new ArrayIndexOutOfBoundsException(position);
        }

        @Override
        public int getItemViewType(int position) {
            int numHeadersAndPlaceholders = getHeadersCount() * mNumColumns;
            if (position < numHeadersAndPlaceholders && (position % mNumColumns != 0)) {
                // Placeholders get the last view type number
                return mAdapter != null ? mAdapter.getViewTypeCount() : 1;
            }
            if (mAdapter != null && position >= numHeadersAndPlaceholders) {
                int adjPosition = position - numHeadersAndPlaceholders;
                int adapterCount = mAdapter.getCount();
                if (adjPosition < adapterCount) {
                    return mAdapter.getItemViewType(adjPosition);
                }
            }

            return AdapterView.ITEM_VIEW_TYPE_HEADER_OR_FOOTER;
        }

        @Override
        public int getViewTypeCount() {
            if (mAdapter != null) {
                return mAdapter.getViewTypeCount() + 1;
            }
            return 2;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {
            mDataSetObservable.registerObserver(observer);
            if (mAdapter != null) {
                mAdapter.registerDataSetObserver(observer);
            }
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            mDataSetObservable.unregisterObserver(observer);
            if (mAdapter != null) {
                mAdapter.unregisterDataSetObserver(observer);
            }
        }

        @Override
        public Filter getFilter() {
            if (mIsFilterable) {
                return ((Filterable) mAdapter).getFilter();
            }
            return null;
        }

        @Override
        public ListAdapter getWrappedAdapter() {
            return mAdapter;
        }

        public void notifyDataSetChanged() {
            mDataSetObservable.notifyChanged();
        }
    }










    private int numColumns = 3;
    private int columnWidth;   //gridView每列的宽度
    private int screenWidth;
    private int firstItemH = 150;
    private int gridSpacing = 10;

    public static int STATUS_SHOW = 1;
    public static int STATUS_EIDT = 2;
    public int status = STATUS_SHOW;   //状态

    private LayoutInflater layoutInflater;
    private ViewGroup headerView;
    private MyPicHearderGridAdapter gridAdapter;

    private String house_id;

    private void initHeaderGridView() {
        super.setClipChildren(false);
        gridSpacing = DensityUtil.dip2px(getContext(),gridSpacing);
        firstItemH = DensityUtil.dip2px(getContext(), firstItemH);
        layoutInflater = LayoutInflater.from(getContext());
    }
    public void setInitParam(String house_id, int screenWidth){
        columnWidth = (screenWidth - (gridSpacing*(numColumns-1)))/numColumns;
        this.screenWidth = screenWidth;
        this.house_id = house_id;
    }


    private UploadPic firstUploadPic;
    public void showGridView(ArrayList <UploadPic> mergeList){
//        gridView.removeAllViews();
        GridHolder firstHolder;
        if(headerView==null){
            headerView = (ViewGroup) layoutInflater.inflate(R.layout.uploadpic_grid_item, null);
            firstHolder = new GridHolder();
            firstHolder.rl_item = (RelativeLayout)headerView.findViewById(R.id.rl_item);
            firstHolder.proview = (UploadPicProView)headerView.findViewById(R.id.proview);
            firstHolder.iv_image = (ImageView)headerView.findViewById(R.id.iv_image);
            firstHolder.iv_fial = (ImageView)headerView.findViewById(R.id.iv_fial);
            firstHolder.iv_del = (ImageView)headerView.findViewById(R.id.iv_del);
            firstHolder.rl_zz = (RelativeLayout)headerView.findViewById(R.id.rl_zz);

            headerView.setTag(firstHolder);
            addHeaderView(headerView);
        }else{
            firstHolder = (GridHolder)headerView.getTag();
        }
        ViewGroup.LayoutParams params = firstHolder.rl_item.getLayoutParams();
        if(mergeList==null || mergeList.size()<=0){
            params.width = 0;
            params.height = 0;
        }else{
            params.width = screenWidth - gridSpacing*2;
            params.height = firstItemH;
            firstUploadPic = mergeList.remove(0);
            bindData(true, firstUploadPic, firstHolder);
            if(gridAdapter==null) {
                gridAdapter = new MyPicHearderGridAdapter();
                setAdapter(gridAdapter);
            }
            gridAdapter.setData(mergeList);
        }
        firstHolder.rl_item.setLayoutParams(params);

    }

    /**
     * 遍历所有子控件，根据设置的tag(key)找到对应的控件刷新
     * @param picBean
     */
    public void refreshChild(UploadPic picBean){
        if(!house_id.equals(picBean.getHouse_id())){
            return;
        }
        if(headerView!=null){
            Object obj = headerView.getTag();
            if(obj!=null && obj instanceof GridHolder){
                GridHolder firstHolder = (GridHolder)headerView.getTag();
                obj = firstHolder.rl_item.getTag();
                if(obj!=null && obj instanceof Integer){
                    int key = (int)obj;
                    if(key==picBean.getKey()){
                        bindData(true, picBean, firstHolder);
                        return;
                    }
                }
            }
        }
        if(gridAdapter!=null){
            int count = getChildCount();
            for(int i = 0; i<count; i++){
                View view = getChildAt(i);
                Object obj = view.getTag();
                if(obj!=null && obj instanceof GridHolder){
                    GridHolder holder = (GridHolder)obj;
                    int key = (int)holder.rl_item.getTag();
                    if(key==picBean.getKey()){
                        bindData(false, picBean, holder);
                        return;
                    }
                }
            }
        }
    }

    /**
     * 事件处理
     */
    private ImageViewEventCallBack callback;
    public void setEventCallBack(ImageViewEventCallBack callback){
        this.callback = callback;
    }
    /**
     * 设置编辑状态
     * @param status
     */
    public void setEditStatus(int status){
        this.status = status;
        if(headerView!=null && firstUploadPic!=null){
            GridHolder firstHolder = (GridHolder)headerView.getTag();
            bindData(true, firstUploadPic, firstHolder);
        }
        if(gridAdapter!=null){
            gridAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 适配器
     */
    class MyPicHearderGridAdapter extends BaseAdapter {
        LayoutInflater inflater;
        private ArrayList<UploadPic> picList;

        public MyPicHearderGridAdapter() {
            picList = new ArrayList<>();
            inflater = LayoutInflater.from(getContext());
        }

        public void setData(ArrayList<UploadPic> list) {
            if (null != list) {
                picList.clear();
                picList.addAll(list);
                notifyDataSetChanged();
            }
        }

        @Override
        public int getCount() {
            return picList.size();
        }

        @Override
        public UploadPic getItem(int position) {
            return picList.get(position);
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup arg2) {
            GridHolder gridHolder = null  ;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.uploadpic_grid_item, null);
                gridHolder = new GridHolder();
                gridHolder.rl_item = (RelativeLayout)convertView.findViewById(R.id.rl_item);
                ViewGroup.LayoutParams params = gridHolder.rl_item.getLayoutParams();
                params.width = columnWidth;
                params.height = columnWidth;
                gridHolder.rl_item.setLayoutParams(params);

                gridHolder.proview = (UploadPicProView)convertView.findViewById(R.id.proview);
                gridHolder.iv_image = (ImageView)convertView.findViewById(R.id.iv_image);
                gridHolder.iv_fial = (ImageView)convertView.findViewById(R.id.iv_fial);
                gridHolder.iv_del = (ImageView)convertView.findViewById(R.id.iv_del);
                gridHolder.rl_zz = (RelativeLayout)convertView.findViewById(R.id.rl_zz);

                convertView.setTag(gridHolder);
            } else {
                gridHolder = (GridHolder) convertView.getTag();
            }
            final UploadPic picBean = getItem(position);
            bindData(false, picBean, gridHolder);
            return convertView;
        }
    }
    class GridHolder {
        UploadPicProView proview;
        ImageView iv_image, iv_fial, iv_del;
        RelativeLayout rl_item, rl_zz;
    }

    /**
     * 绑定数据到控件上
     * @param picBean
     * @param gridHolder
     */
    private void bindData(final boolean isFirst, final UploadPic picBean, GridHolder gridHolder){
        gridHolder.rl_item.setTag(picBean.getKey());
        gridHolder.rl_zz.setOnClickListener(null);
        if(!TextUtils.isEmpty(picBean.getUrl())){
            //从服务器上获取的图片
            picBean.setStatus(UploadPic.STATUS_SUCC);
            ImageLoader.getInstance().displayImage(picBean.getUrl(), gridHolder.iv_image);
//            LogUtil.v(TAG, "展示服务器上图片:"+picBean.getUrl());
        }else{
            ImageLoader.getInstance().displayImage("file://" + picBean.getPath(), gridHolder.iv_image);
        }

        int status = picBean.getStatus();
        switch (status){
            case UploadPic.STATUS_UPLODING:
                gridHolder.rl_zz.setVisibility(View.VISIBLE);
                gridHolder.proview.setVisibility(View.VISIBLE);
                gridHolder.iv_fial.setVisibility(View.GONE);
                gridHolder.proview.setProcess(picBean.getProgress());
                break;
            case UploadPic.STATUS_SUCC:
                gridHolder.proview.setVisibility(View.GONE);
                gridHolder.iv_fial.setVisibility(View.GONE);
                gridHolder.rl_zz.setVisibility(View.GONE);
                break;
            case UploadPic.STATUS_FAIL:
                gridHolder.rl_zz.setVisibility(View.VISIBLE);
                gridHolder.proview.setVisibility(View.GONE);
                gridHolder.iv_fial.setVisibility(View.VISIBLE);
                //只有非编辑状态下才能点击重新上传
                if(this.status == STATUS_SHOW){
                    gridHolder.rl_zz.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //重新上传
                            callback.onReUpload(picBean);
                        }
                    });
                }

                break;
        }

        //编辑状态
        if(this.status == STATUS_EIDT){
            gridHolder.iv_del.setVisibility(View.VISIBLE);
            gridHolder.iv_del.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(callback!=null){
                        //删除
                        callback.onDelete(picBean);
                    }
                }
            });
        }else{
            gridHolder.iv_del.setVisibility(View.GONE);
            gridHolder.iv_del.setOnClickListener(null);
        }
    }


}
