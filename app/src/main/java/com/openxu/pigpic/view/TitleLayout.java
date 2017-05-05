package com.openxu.pigpic.view;

import android.content.Context;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.openxu.pigpic.R;

public class TitleLayout extends LinearLayout {

	private RelativeLayout rellTitle;
	private TextView tvTitle,tvRight1;
	private ImageView btnLeft1;
	private ImageView btnRight1;
	private RelativeLayout rellTitleRoot, title_re_bg;
	
	private Context mContext;

	private void initView(Context context) {
		if (isInEditMode()) {
			return;
		}
		mContext = context;
		rellTitle = (RelativeLayout) View.inflate(context,
				R.layout.title_layout, null);
		addView(rellTitle);
		rellTitleRoot = (RelativeLayout) rellTitle
				.findViewById(R.id.rellTitleRoot);
		title_re_bg = (RelativeLayout) rellTitle
		        .findViewById(R.id.title_re_bg);
		tvTitle = (TextView) rellTitle.findViewById(R.id.tvTitle);
		btnLeft1 = (ImageView) rellTitle.findViewById(R.id.btnLeft1);
		btnLeft1.setVisibility(View.GONE);
		btnRight1 = (ImageView) rellTitle.findViewById(R.id.btnRight1);
		btnRight1.setVisibility(View.GONE);
		tvRight1 = (TextView) rellTitle.findViewById(R.id.tvRight1);
		tvRight1.setVisibility(View.GONE);
		TextPaint tp =tvTitle.getPaint();
		tp.setFakeBoldText(true);
	}

	public void setRight1ClickListener(OnClickListener l) {
		// btnRight1.setVisibility(View.VISIBLE);
		btnRight1.setOnClickListener(l);
	}
	public void setTvRight1ClickListener(OnClickListener l) {
		// btnRight1.setVisibility(View.VISIBLE);
		tvRight1.setOnClickListener(l);
	}
	public void setTitleBgColor(int color) {
	    title_re_bg.setBackgroundColor(color);
	}
	
	/**
	 *  @function:
	 *  @author Terry.chen  
	 *  @date 2013-12-30 下午5:14:20
	 *  @param resid
	 *  @deprecated
	 */
	public void setTitleBg(int resid) {
		title_re_bg.setBackgroundResource(resid);
	}

	public void setTitleName(int textResId) {
		tvTitle.setText(textResId);
	}

	public void setTitleColor(int color) {
		tvTitle.setTextColor(color);
	}

	public void setTitleName(String titleName) {
		tvTitle.setText(titleName);
	}

	public void setRight1(int drawableId) {
		btnRight1.setImageResource(drawableId);
	}
	public void setTvRight1(String str) {
		tvRight1.setText(str);
	}
	public void setTvRight1Size(float textSize) {
		tvRight1.setTextSize(textSize);
	}
	public String getTvRight1() {
		return tvRight1.getText().toString().trim();
	}
	public void setLeft1(int drawableId) {
		btnLeft1.setImageResource(drawableId);
	}

	public String getTitleName() {
		return (String) tvTitle.getText();
	}

	public void setLeft1Show(boolean isShow) {
		if (isShow) {
			btnLeft1.setVisibility(View.VISIBLE);
		} else {
			btnLeft1.setVisibility(View.INVISIBLE);
		}
	}

	public void setRight1Show(boolean isShow) {
		if (isShow) {
			btnRight1.setVisibility(View.VISIBLE);
		} else {
			btnRight1.setVisibility(View.INVISIBLE);
		}
	}
	
	public void setTvRight1Show(boolean isShow) {
		if (isShow) {
			tvRight1.setVisibility(View.VISIBLE);
		} else {
			tvRight1.setVisibility(View.INVISIBLE);
		}
	}

	public void setLeft1Listener(OnClickListener l) {
		btnLeft1.setOnClickListener(l);
	}

	public TitleLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public TitleLayout(Context context) {
		super(context);
		initView(context);
	}

}
