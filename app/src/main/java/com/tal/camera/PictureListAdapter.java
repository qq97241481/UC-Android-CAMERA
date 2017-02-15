package com.tal.camera;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;
import java.util.List;
import cn.com.xpai.core.Manager;

import static android.R.attr.id;

public class PictureListAdapter extends BaseAdapter {

	private Context context;
	private List<Camera.Size> list;
	SettingItemAdapter settingAdapter;
	private Activity activity;

	public PictureListAdapter(Activity activity, SettingItemAdapter setting) {
		context = activity.getBaseContext();
		this.activity = activity;
		settingAdapter = setting;
	}

	@Override
	public int getCount() {
		list = Manager.getSupportedPictureSizes();
		if (list == null) {
			return 0;
		}
		return list.size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int position, View convert_view, ViewGroup parent) {
		Camera.Size resolution = list.get(position);
		ItemViewCache viewCache = null;
		if (convert_view == null) {
			convert_view = LayoutInflater.from(context).inflate(R.layout.radio_btn_item, null, true);
			viewCache = new ItemViewCache();
			viewCache.txtName = (TextView) convert_view.findViewById(R.id.txt_name);
			viewCache.radioBtn = (RadioButton) convert_view.findViewById(R.id.radio_btn);
			convert_view.setTag(viewCache);
		} else {
			viewCache = (ItemViewCache) convert_view.getTag();
		}
		viewCache.radioBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Camera.Size res = (Camera.Size) v.getTag();
				Config.photoWidth = res.width;
				Config.photoHeight = res.height;
				Config.save();
				notifyDataSetChanged();
				settingAdapter.notifyDataSetChanged();

				/*MainHandler mainHandler = MainHandler.getInstance();
				Message msg1 = new Message();
				msg1.what = MainHandler.MSG_SETTING_VISIBILITY;
				mainHandler.sendMessage(msg1);*/

			}
		});
		viewCache.radioBtn.setTag(resolution);
		ItemViewCache cache = (ItemViewCache) convert_view.getTag();
		String res_str = String.format("%dx%d", resolution.width,
				resolution.height);
		cache.txtName.setText(res_str);
		if (resolution.width == Config.photoWidth
				&& resolution.height == Config.photoHeight) {
			cache.radioBtn.setChecked(true);
		} else {
			cache.radioBtn.setChecked(false);
		}
		return convert_view;
	}
	
	private static class ItemViewCache {
		TextView txtName;
		RadioButton radioBtn;
	}

}
