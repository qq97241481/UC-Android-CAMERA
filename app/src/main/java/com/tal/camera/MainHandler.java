package com.tal.camera;

import java.util.List;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.tal.camera.demo.player.FilelistActivity;
import cn.com.xpai.core.Manager;
import cn.com.xpai.core.Manager.CameraID;

class MainHandler extends Handler {
	Button btnChangeCamera = null;
	Button btnRecordPause = null;
	RecordButton btnRecord = null;
	TextView txtDuration = null;
	TextView txtFps = null;
	TextView txtNetSpeed = null;
	TextView txtCache = null;
	TextView txtBytesSent = null;
	Button btnPlayer = null;
	Button btnSetting = null;
	Button btnTakePicture = null;
	PopListView settingMenu = null;
	Activity activity = null;
	Animation netAnimation = null;
	Animation	pauseAnimation = null;
	boolean function;

	public final static int MSG_UPDATE_INFO = 11000;
	public final static int MSG_SWITCH_BTN_VISIBILITY = 11001;
	public final static int MSG_NETWORK_CONNECTED = 11002;
	public final static int MSG_NETWORK_DISCONNECT = 11003;
	public final static int MSG_RECODER_BTN_VISIBLE= 11004;
	public final static int MSG_RECODER_BTN_UNVISIBLE= 11005;
	public final static int MSG_SETTING_VISIBILITY= 11006;

	private final static String TAG = "MainHandler";
	private int currentZoomLevel = 0, maxZoomLevel = 0;
	private static MainHandler instance = null;
	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case MSG_UPDATE_INFO:
			Manager.startPreview();
			updateRunInfo();
			break;
		case MSG_SWITCH_BTN_VISIBILITY:
			break;
		case MSG_NETWORK_CONNECTED:
			Toast.makeText(activity.getBaseContext(), "成功建立网络连接",
					Toast.LENGTH_SHORT).show();
			/*btnConn.setBackgroundResource(R.drawable.link);
			if (btnConn.getAnimation() != null && btnConn.getAnimation().hasStarted()) {
				btnConn.getAnimation().cancel();
				btnConn.getAnimation().reset();
			}*/
			break;
		case MSG_NETWORK_DISCONNECT:
			Toast.makeText(activity.getBaseContext(), "失去网络连接, error:" + msg.arg1,
					Toast.LENGTH_SHORT).show();
			/*btnConn.setBackgroundResource(R.drawable.link_break);
			if (btnConn.getAnimation() == null || btnConn.getAnimation().hasEnded()) {
				btnConn.startAnimation(netAnimation);
			}*/
			break;
		case MSG_RECODER_BTN_VISIBLE:
			btnRecordPause.setVisibility(View.VISIBLE);
			break;
		case MSG_RECODER_BTN_UNVISIBLE:
			btnRecordPause.setVisibility(View.INVISIBLE);
			break;
		case MSG_SETTING_VISIBILITY:
			settingMenu.view.setVisibility(View.INVISIBLE);
			break;
		default:
			super.handleMessage(msg);
		}
	}
	private MainHandler(){}
	public static MainHandler getInstance() {
		if (instance == null) {
			instance = TALAndroid.mainHandler;
		}
		return instance;
	}

	final private void updateRunInfo() {
		if (Manager.RecordStatus.RECORDING == Manager.getRecordStatus()) {
			txtDuration.setVisibility(View.VISIBLE);
			txtFps.setVisibility(View.VISIBLE);
			txtDuration.setText(String.format("Duration: %.2f",
					(float) Manager.getRecordDuration() / 1000));
			txtFps.setText(String.format("FPS: %d", Manager.getCurrentFPS()));
		} 
		btnRecord.update();
		txtNetSpeed.setText(String.format("Net: %.2f KBps",
				(float) Manager.getUploadingSpeed() / 1024));
		txtCache.setText(String.format("Cache: %.2f KByte",
				(float) Manager.getCacheRemaining() / 1024));
		txtBytesSent.setText(String.format("Sent: %.2f KByte", 
				(float) Manager.getBytesSent() / 1024));
	}

	private View.OnClickListener btnConnListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			if (Manager.isConnected()) {
				// 调用对话框，询问是否要断开连接
				String title = "断开网络连接";
				if (Manager.RecordStatus.RECORDING == Manager.getRecordStatus()) {
					title = "模拟断网测试";
				}
				DialogFactory.confirmDialog("模拟断网", "确认要断开网络连接？",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Manager.disconnect();
							}
						});
			} else {
				// 调用建立连接对话框
				DialogFactory.getInstance(DialogFactory.CONNECTION_DIALOG)
						.show();
			}
		}
	};
	
	private View.OnClickListener btnPauseListen = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			switch (Manager.getRecordStatus()) {
			case RECORDING:
				Manager.pauseRecord();
				btnRecordPause.startAnimation(pauseAnimation);
				//switchBtnVisibility();
				break;
			case PAUSE:
				Manager.resumeRecord();
				pauseAnimation.cancel();
				pauseAnimation.reset();
				//switchBtnVisibility();
				break;
			default:
			}
		}
	};

	private View.OnClickListener btnTakePictureListener = new OnClickListener() {
		
		Camera.Size picSize = null;
		@Override
		public void onClick(View arg0) {
			if (Manager.isPreviewing()) {
				if (null == picSize) {
					List <Camera.Size> sizeList = Manager.getSupportedPictureSizes();
					if (null != sizeList) {
						for (int i= 0; i < sizeList.size(); i++) {
							Camera.Size pictureSize = sizeList.get(i);
							//Demo中只简单地在日志中打印出支持的拍照图片的大小，
							//实际应用中可以提前通过此API提供一个列表选项给用户选择，并保存用户的设置
							Log.i(TAG,String.format("support picture size :%dx%d",pictureSize.width, pictureSize.height));
							if (null == picSize) {
								picSize = pictureSize;
							} else {
								//Demo选取最小尺寸的拍照图片大小
								if (picSize.width > pictureSize.width) {
									picSize = pictureSize;
								}
							}
						}
					} else {
						Log.w(TAG, "Can't supported take picture");
					}
				}
				if (null != picSize) {
					Manager.takePicture("/sdcard/xpai", Config.photoWidth, Config.photoHeight);
				}
			}
		}
	};
	
	private View.OnClickListener btnPreviewListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			if (Manager.isPreviewing()) {
				Manager.stopPreview();
			} else {
				Manager.startPreview();
			}
			//switchBtnVisibility();
		}
	};
	
	private View.OnClickListener btnPlayerListen = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			Intent intent = new Intent(activity, FilelistActivity.class);
			activity.startActivity(intent);

		}
	};
	
	private View.OnClickListener btnSettingListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (null == settingMenu) {
				settingMenu = new PopListView(activity, new SettingItemAdapter(activity , function), "设置选项" ,function);
			}
			settingMenu.showAtLocation(v, Gravity.CENTER, 30, 10);
		}
	};
	
	private View.OnClickListener btnChangeCameraListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			boolean ret = false;
			if (Manager.getCurrentCameraId() == CameraID.CAMERA_BACK) {
				ret = Manager.switchCamera(CameraID.CAMERA_FRONT);
			} else {
				ret = Manager.switchCamera(CameraID.CAMERA_BACK);
			}
		}
	};

	/**
	 *
	 * @param activity
	 * @param functiuon 控制是进行录像还是进行拍照，true表示录像，false表示拍照
     */
	public MainHandler(Activity activity , boolean functiuon) {
		this.function = functiuon;
		btnChangeCamera = (Button) activity.findViewById(R.id.btn_change_camera);
		btnRecord = (RecordButton)activity.findViewById(R.id.btn_record);
		txtDuration = (TextView) activity.findViewById(R.id.txt_record_duration);
		txtFps = (TextView) activity.findViewById(R.id.txt_frame_rate);
		txtNetSpeed = (TextView) activity.findViewById(R.id.txt_net_speed);
		txtBytesSent = (TextView) activity.findViewById(R.id.txt_bytes_sent);
		txtCache = (TextView) activity.findViewById(R.id.txt_cache_remain);
		btnPlayer = (Button) activity.findViewById(R.id.btn_player);
		btnSetting = (Button) activity.findViewById(R.id.btn_setting);
		if(!functiuon){
			btnRecord.setVisibility(View.INVISIBLE);
		}
		btnPlayer.setOnClickListener(btnPlayerListen);
		btnSetting.setOnClickListener(btnSettingListener);
		btnChangeCamera.setOnClickListener(btnChangeCameraListener);
		btnRecordPause = (Button) activity.findViewById(R.id.btn_record_pause);
		btnRecordPause.setOnClickListener(btnPauseListen);
		btnTakePicture = (Button) activity.findViewById(R.id.btn_take_picture);
		btnTakePicture.setOnClickListener(btnTakePictureListener);

		this.activity = activity;
		
		settingMenu = null;
		
		netAnimation = new FlickAnimation(500);
		//btnConn.startAnimation(netAnimation);

		pauseAnimation = new FlickAnimation(200);
		
	   /* ZoomControls zoomControls = (ZoomControls) activity.findViewById(R.id.camera_zoom_control);
	    if(Manager.isZoomSupported()) {
	        maxZoomLevel = Manager.getMaxZoomLevel();
	        zoomControls.setIsZoomInEnabled(true);
	        zoomControls.setIsZoomOutEnabled(true);
	        zoomControls.setOnZoomInClickListener( new OnClickListener() {
	        	@Override
	        	public void onClick(View v) {
	        		if(currentZoomLevel++ < maxZoomLevel) {
	        			Manager.setZoom(currentZoomLevel, true);
	        		} else {
	        			currentZoomLevel = maxZoomLevel;
	        			Toast.makeText(MainHandler.this.activity.getBaseContext(), "画面已放至最大！", Toast.LENGTH_SHORT)
	    				.show();
	        		}
	        	}
	        });

	        zoomControls.setOnZoomOutClickListener(new OnClickListener() {
	        	@Override
	        	public void onClick(View v) {
	        		if(currentZoomLevel > 0) {
	        			Manager.setZoom(currentZoomLevel--, true);
	        		} else {
	        			Toast.makeText(MainHandler.this.activity.getBaseContext(), "画面已缩至最小！", Toast.LENGTH_SHORT)
	    				.show();
	        		}
	        	}
	        });  
	     } else {
	         zoomControls.setVisibility(View.GONE);
	     }*/
	}
	
	//闪烁动画
	class FlickAnimation extends AlphaAnimation {
		FlickAnimation(int duration) {
			super(1, 0); 
			setDuration(duration); // duration - half a second
			setInterpolator(new LinearInterpolator()); 
			setRepeatCount(Animation.INFINITE); // Repeat animation
			setRepeatMode(Animation.REVERSE); 
		}
	}
}