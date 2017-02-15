package com.tal.camera;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class PopListView extends PopupWindow {
	View view;
	ListView listView;
	Activity activity;
	private PopupWindow popupWindow;
	
	PopListView(Activity activity, ListAdapter la, String title , boolean function) {
		super(activity.getBaseContext());
		view =  (View)activity.getLayoutInflater().inflate(R.layout.setting_menu, (ViewGroup)activity.findViewById(R.id.main_layout), false);
		listView = (ListView)view.findViewById(R.id.menu_list);
		view.setOnTouchListener(touchListener);
		listView.setAdapter(la);
		if(function){
			if (la instanceof SettingItemAdapter) {
				listView.setOnItemClickListener((SettingItemAdapter)la);
			}
		}else{
            if (la instanceof SettingItemAdapter) {
                listView.setOnItemClickListener((SettingItemAdapter)la);
            }
			Toast.makeText(activity,"拍照",Toast.LENGTH_SHORT).show();
		}
		setContentView(view);
		setFocusable(true);
        setTouchable(true);
		setAnimationStyle(R.style.AnimationFade);
		this.setWidth(LayoutParams.WRAP_CONTENT);
		this.setHeight(LayoutParams.WRAP_CONTENT);
		//listview的wrap_content无效，必须手动计算并设定listview宽度
		listView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		listView.getLayoutParams().width = listView.getMeasuredWidth();
		((TextView)view.findViewById(R.id.txt_title)).setText(title);
	}
	

	
	OnTouchListener touchListener = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			 if (isShowing()) {
				 dismiss();
			 }
			 return true;
		}
	};
}
