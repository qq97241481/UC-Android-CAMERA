package com.tal.camera;

import java.util.Iterator;
import java.util.List;
import cn.com.xpai.core.Manager;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.ECLAIR)
public class TALAndroid extends Activity {

	private SurfaceView mPreview = null;
	private static String TAG = "TALAndroid";
	private static TALAndroid instance = null;

	private static Menu menu = null;

	static String lastPictureFileName = null;

	static MainHandler mainHandler;

	static Menu getMenu() {
		return menu;
	}
	
	private OrientationEventListener orientationListener;

	public final static int MENU_UPLOAD_PICTURE = 20004;
	public final static int MENU_UPLOAD_VF_WHOLE = 20013;
	public final static int MENU_UPLOAD_VF = 20014;
	public final static int MENU_TRANSCODER = 20015;
	public static TALAndroid getInstance() {
		return instance;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Hide the window title.
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		instance = this;
		//DialogFactory.register(this);
		TALHandler.register(this);
		Config.load(this);

		Bundle bundle = this.getIntent().getExtras();
		String mCamera = bundle.getString("camera");

		if (0 != Manager.init(this, TALHandler.getInstance())) {
			Log.e(TAG, "init core manager failed");
		}
		//以下强制明文认证
		//Manager.forceAuthenMode(Manager.HANDSHAKE_CLEAR_PASSWORD);
		Manager.setVideoFpsRange(20, 20);
		List<Manager.Resolution> resList = Manager.getSupportedVideoResolutions();
		if (null != resList && resList.size() > 0) {
			if (0 == Config.videoWidth || 0 == Config.videoHeight) {
				//默认使用适中的分辨率
				Manager.Resolution res = resList.get((int)(resList.size()/2));
				Config.videoWidth = res.width;
				Config.videoHeight = res.height;
				Config.videoBitRate = res.width;
			}
		} else {
			Log.e(TAG, "cannto get supported resolutions");
		}
		Manager.setVideoResolution(Config.videoWidth, Config.videoHeight);
		
		List<Camera.Size> pictureSizeList = Manager.getSupportedPictureSizes();
		if (pictureSizeList != null) {
			Iterator<Camera.Size> it = pictureSizeList.iterator();
			while(it.hasNext()) {
				Camera.Size size = it.next();
				Log.i(TAG, String.format("support picuture size %dx%d", size.width, size.height));
			}
		}
		Manager.setPictureSize(1920,1080);
		/*设置网络自适应 true为开启 false为关闭*/
		Manager.setNetWorkingAdaptive(Config.isOpenNetWorkingAdaptive);
		Manager.setAudioRecorderParams(Config.audioEncoderType, Config.channel, Config.audioSampleRate, Config.audioBitRate);
        //保持屏幕常亮
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		Message msg = new Message();
		msg.what = TALHandler.MSG_SHOW_CONNECTION_DIALOG;
		TALHandler.getInstance().sendMessage(msg);
		setContentView(R.layout.main);
		mPreview = (SurfaceView) findViewById(R.id.preview_view);
		mPreview.setZOrderMediaOverlay(false);
		mPreview.setZOrderOnTop(false);

		if(mCamera.equals("picture")){
			mainHandler = new MainHandler(this,false);
		}else{
			mainHandler = new MainHandler(this,true);
		}
		//竖屏拍摄模式
//		if (!Manager.forcePortrait(true)) {
//			Log.w(TAG, "force portrait record fail");
//		}
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			Manager.forcePortrait(false);
			Log.e(TAG, "横屏拍摄 111");
		} else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			Manager.forcePortrait(true);
			Log.e(TAG, "竖屏拍摄 222");
		}
		Message msg1 = new Message();
		msg1.what = MainHandler.MSG_UPDATE_INFO;
		mainHandler.sendMessage(msg1);
	}
	
	public void onConfigurationChanged(Configuration newConfig) {
	    // TODO Auto-generated method stub
	    super.onConfigurationChanged(newConfig);
	    if (Manager.getRecordStatus() == Manager.RecordStatus.IDLE) {//在未录制状态
	        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
	            Log.e(TAG, "竖屏");
	            Manager.stopPreview();
	            Manager.forcePortrait(true);
	            Manager.startPreview();
	        } else {
	            Log.e(TAG, "横屏");
	            Manager.stopPreview();
	            Manager.forcePortrait(false);
	            Manager.startPreview();
	        }
	    } else {
	        Toast.makeText(this, "正处于录制状态无法进行横竖屏切换", Toast.LENGTH_LONG).show();
	    }
	}

	/* Creates the menu items */
	@Override
	public boolean onCreateOptionsMenu(Menu m) {
		menu = m;
		menu.add(0, MENU_UPLOAD_PICTURE, 0, "上传照片");
		menu.add(0, MENU_UPLOAD_VF_WHOLE, 0, "上传离线录制的文件");
		menu.add(0, MENU_UPLOAD_VF, 0, "续传视频文件");
		menu.add(0, MENU_TRANSCODER, 0, "测试视频变换");
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu m) {
		super.onPrepareOptionsMenu(m);
		if (Manager.RecordStatus.IDLE != Manager.getRecordStatus()) {
			m.findItem(MENU_UPLOAD_PICTURE).setEnabled(false);
			m.findItem(MENU_UPLOAD_VF).setEnabled(false);
			m.findItem(MENU_UPLOAD_VF_WHOLE).setEnabled(false);
		} else {
			m.findItem(MENU_UPLOAD_PICTURE).setEnabled(true);
			m.findItem(MENU_UPLOAD_VF).setEnabled(true);
			m.findItem(MENU_UPLOAD_VF_WHOLE).setEnabled(true);
		}
		if (Manager.isConnected() && 
				Manager.RecordStatus.IDLE != Manager.getRecordStatus()) {
			m.findItem(MENU_UPLOAD_PICTURE).setEnabled(false);
			m.findItem(MENU_UPLOAD_VF).setEnabled(false);
		}

		if (!Manager.isConnected()) {
			m.findItem(MENU_UPLOAD_PICTURE).setEnabled(false);
			m.findItem(MENU_UPLOAD_VF).setEnabled(false);
		}

		if (lastPictureFileName == null) {
			m.findItem(MENU_UPLOAD_PICTURE).setEnabled(false);
		}
		
		return true;
	}

	/* Handles item selections */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_UPLOAD_PICTURE:
			if (null != lastPictureFileName) {
				Manager.uploadFile(lastPictureFileName);
				Log.v(TAG, "upload file name:" + lastPictureFileName);
			} else {
				Message msg = new Message();
				msg.what = TALHandler.SHOW_MESSAGE;
				Bundle bdl = new Bundle();
				bdl.putString(TALHandler.MSG_CONTENT, "未找到最近拍摄的照片!");
			}
			return true;
		case MENU_UPLOAD_VF_WHOLE:
			Intent intent = new Intent(this, FileChooser.class);
			startActivityForResult(intent, 0);
			return true;
		case MENU_UPLOAD_VF:
			intent = new Intent(this, FileChooser.class);
			startActivityForResult(intent, 1);
			return true;
		case MENU_TRANSCODER:
			intent = new Intent(this, FileChooser.class);
			intent .putExtra("Flag", "transcode");
			startActivity(intent);
			return true;
		}
		return false;
	}

	protected void onDestroy() {
		Log.i(TAG, "mini app destroy");
		super.onDestroy();
		/*XPHandler.getInstance().exitApp();
		Manager.deInit();
		super.onDestroy();
		System.exit(0);*/
		Manager.deInit();
		finish();
	}

	/* 覆盖 onActivityResult() */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent i) {
		switch (resultCode) {
		case RESULT_OK:
			/* 取得来自Activity2的数据，并显示于画面上 */
			Bundle b = i.getExtras();
			String file_name = b.getString("file_name");
			Log.i(TAG, "Get file name:" + file_name);
			if(!Manager.isConnected()) {
				Toast.makeText(this, "上传离线视频文件,请先连接视频服务器!", Toast.LENGTH_LONG).show();
				return;
			}
			// Manager.uploadVideoFile(..., false)
			// 第二个参数为 false代表新上传一个文件, 服务器总是将上传的数据存为一个新的视频文件
			// 第二个参数为 true 代表续传
			if (!Manager.uploadVideoFile(file_name, requestCode == 1, null)) {
				// todo 错误处理
				Log.w(TAG, "Upload file failed.");
			}
			break;
		default:
			break;
		}
	}

    @Override
    protected void onResume() {
        super.onResume();
        Message msg1 = new Message();
        msg1.what = MainHandler.MSG_UPDATE_INFO;
        mainHandler.sendMessage(msg1);
    }
}
