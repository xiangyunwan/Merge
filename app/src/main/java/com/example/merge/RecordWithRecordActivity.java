package com.example.merge;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.adapter.RecordAdapter;
import com.example.adapter.RecordAdapter.OnChooseListener;
import com.example.bean.RecordBean;
import com.example.util.AudioDecoder;
import com.example.util.AudioEncoder;
import com.example.util.MD5Util;
import com.example.util.MultiAudioMixer;
import com.example.util.MultiAudioMixer.OnAudioMixListener;
import com.example.view.ProgressButton;

/***
 * 音频合音
 *
 * @author 帽檐遮不住阳光
 * @data 2016/5/24
 *
 */
public class RecordWithRecordActivity extends Activity implements
		OnClickListener, OnChooseListener {

	public final static int STARTRECORD = 0X1111111;// 开始录音
	public final static int STOPRECORD = 0X1111112;// 停止录音
	public final static int DAOJISHI = 0X1111113;// 显示Progress的进度
	public final static int OVERRECORD = 0X1111114;// 录音结束
	private boolean canOnclick = true;// 录声音的时候防止你们乱点

	private TimerTask timeTask;
	private Timer timer;
	private int duration = 2000;// 录音总时间 这里是每次减3算的

	private Button btn_begin, btn_gomix;
	private ProgressButton progress;
	private ListView recordListView;

	private RecordAdapter adapter;

	private List<RecordBean> list = new ArrayList<RecordBean>();
	private MediaRecorder mRecorder;

	private File recordFile = null; // 录音文件夹
	private File decodeFile = null; // 解码文件夹
	private File mixFile = null; // 合音文件夹

	String recordPath = "/sdcard/merge/record/"; // 录音的路径
	String decodePath = "/sdcard/merge/decode/"; // 解码的路径
	String mixPath = "/sdcard/merge/mix/";// 合音的路径

	private int count = 0; // 这是用来给录音起名字的
	private ProgressDialog dialog;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record_with_record);

		btn_begin = (Button) findViewById(R.id.btn_begin);
		btn_begin.setOnClickListener(this);
		btn_begin.setText("开始录音");
		findViewById(R.id.btn_gomix).setOnClickListener(this);
		progress = (ProgressButton) findViewById(R.id.progress);
		timer = new Timer();

		recordListView = (ListView) findViewById(R.id.recordListView);
		adapter = new RecordAdapter(this);
		recordListView.setAdapter(adapter);
		adapter.setOnChooseListener(this);

		dialog = new ProgressDialog(this);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);

		recordFile = new File(recordPath);
		if (!recordFile.exists()) {
			recordFile.mkdirs();
		}

		decodeFile = new File(decodePath);
		if (!decodeFile.exists()) {
			decodeFile.mkdirs();
		}

		mixFile = new File(mixPath);
		if (!mixFile.exists()) {
			mixFile.mkdirs();
		}


	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case STARTRECORD:
					// 开始录音
					startRecord();
					duration = 2000;
					canOnclick = false;
					btn_begin.setText("停止录音");
					progress.setMax(duration);
					createTimeTask();
					timer.scheduleAtFixedRate(timeTask, 0, 10);
					break;
				case STOPRECORD:
					// 结束录音
					stopRecord();
					timeTask.cancel();
					progress.setProgress(0);
					btn_begin.setText("开始录音");
					canOnclick = true;
					break;
				case DAOJISHI:
					progress.setProgress(msg.arg1);
					break;
				case OVERRECORD:
					// 录音结束
					stopRecord();
					progress.setProgress(0);
					btn_begin.setText("开始录音");
					canOnclick = true;
					break;
			}
		}
	};

	private void createTimeTask() {
		if (timeTask != null) {
			timeTask.cancel();
		}
		timeTask = new TimerTask() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				duration = duration - 3;
				Message msg = new Message();
				msg.what = DAOJISHI;
				msg.arg1 = duration;
				mHandler.sendMessage(msg);
				if (duration <= 0) {
					this.cancel();
					mHandler.sendEmptyMessage(OVERRECORD);
				}
			}
		};

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case R.id.btn_begin:
				if (canOnclick) {
					// 开始录音
					mHandler.sendEmptyMessage(STARTRECORD);
				} else {
					// 结束录音
					mHandler.sendEmptyMessage(STOPRECORD);
				}
				break;
			case R.id.btn_gomix:
				int num = 0;
				for (int i = 0; i < list.size(); i++) {
					if (1 == list.get(i).getState()) {
						num++;
					}
				}

				if (num < 2) {
					Toast.makeText(getApplicationContext(), "至少选择两条录音才能合音",
							Toast.LENGTH_SHORT).show();
				} else {
					dialog.setMessage("合音中...");
					dialog.show();
					new MixTask(num).execute();
				}
				break;
		}
	}

	/**
	 * 开始录音
	 */
	private void startRecord() {
		count++;
		mRecorder = new MediaRecorder();
		mRecorder.reset();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC); // 设置声音来源
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);// 设置所录制的音视频文件的格式
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);// 设置所录制的声音的编码格式
		mRecorder.setAudioEncodingBitRate(96000);// 比特率
		mRecorder.setAudioChannels(2);// 通道
		mRecorder.setAudioSamplingRate(44100);// 采样率
		mRecorder.setOutputFile(recordFile.getAbsolutePath() + "/record"
				+ count + ".mp4");// 设置录制的音频文件的保存位置
		try {
			mRecorder.prepare();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mRecorder.start();
	}

	/**
	 * 录音结束
	 */
	private void stopRecord() {
		mRecorder.stop();
		mRecorder = null;
		RecordBean bean = new RecordBean();
		bean.setId(count);
		bean.setName(count + ".mp4");
		bean.setState(0);
		bean.setPath(recordFile.getAbsolutePath() + "/record" + count + ".mp4");
		list.add(bean);
		adapter.setList(list);
	}

	@Override
	public void onChoose(int position, int state, String path) {
		// TODO Auto-generated method stub
		if (0 == state) {
			list.get(position).setState(1);
			dialog.setMessage("解码中...");
			dialog.show();
			new DecodeTask(path, position).execute();
		} else {
			list.get(position).setState(0);
		}
		adapter.setList(list);
	}

	/**
	 * 解码
	 *
	 */
	class DecodeTask extends AsyncTask<Void, Double, Boolean> {

		private String fileUrl;
		private int position;

		DecodeTask(String url, int p) {
			fileUrl = url;
			position = p;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				// 解码后的路径
				String decodeFilePath = decodeFile.getAbsolutePath()
						+ "/decode" + MD5Util.getMD5Str(fileUrl);
				// 将解码后的路径保存在list中,方便后面取值
				list.get(position).setDecodePath(decodeFilePath);
				// 解码
				AudioDecoder audioDec = AudioDecoder
						.createDefualtDecoder(fileUrl);
				fileUrl = decodeFilePath;
				audioDec.decodeToFile(decodeFilePath);
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			dialog.cancel();
		}
	}

	/**
	 * 合音
	 *
	 */
	class MixTask extends AsyncTask<Void, Double, Boolean> {

		private int size;

		MixTask(int num) {
			size = num;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			String rawAudioFile = null;

			// 将需要合音的音频解码后的文件放到数组里
			File[] rawAudioFiles = new File[size];
			StringBuilder sbMix = new StringBuilder();
			int index = 0;

			for (int i = 0; i < list.size(); i++) {
				if (1 == list.get(i).getState()) {
					rawAudioFiles[index++] = new File(list.get(i)
							.getDecodePath());
					sbMix.append(i + "");
				}
			}

			// 最终合音的路径
			final String mixFilePath = mixFile.getAbsolutePath() + "/mix"
					+ MD5Util.getMD5Str(sbMix.toString());

			// 下面的都是合音的代码
			try {
				MultiAudioMixer audioMixer = MultiAudioMixer.createAudioMixer();

				audioMixer.setOnAudioMixListener(new OnAudioMixListener() {

					FileOutputStream fosRawMixAudio = new FileOutputStream(
							mixFilePath);

					@Override
					public void onMixing(byte[] mixBytes) throws IOException {
						fosRawMixAudio.write(mixBytes);
					}

					@Override
					public void onMixError(int errorCode) {
						try {
							if (fosRawMixAudio != null)
								fosRawMixAudio.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

					@Override
					public void onMixComplete() {
						try {
							if (fosRawMixAudio != null)
								fosRawMixAudio.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

				});
				audioMixer.mixAudios(rawAudioFiles);
				rawAudioFile = mixFilePath;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			AudioEncoder accEncoder = AudioEncoder
					.createAccEncoder(rawAudioFile);
			String finalMixPath = mixFile.getAbsolutePath() + "/finalMix.aac";
			accEncoder.encodeToFile(finalMixPath);
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			Toast.makeText(getApplicationContext(), "合音成功", Toast.LENGTH_SHORT)
					.show();
			dialog.cancel();
		}
	}
}
