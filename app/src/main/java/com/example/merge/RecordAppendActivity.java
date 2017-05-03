package com.example.merge;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.coremedia.iso.boxes.Container;
import com.example.adapter.RecordAdapter;
import com.example.adapter.RecordAdapter.OnChooseListener;
import com.example.bean.RecordBean;
import com.example.view.ProgressButton;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

/**
 * 音频拼接
 *
 * @author 帽檐遮不住阳光
 * @data 2016/5/26
 *
 */
public class RecordAppendActivity extends Activity implements OnClickListener,
		OnChooseListener {

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
	private File appendFile = null; // 拼接文件夹

	String recordPath = "/sdcard/merge/record/"; // 录音的路径
	String decodePath = "/sdcard/merge/decode/"; // 解码的路径
	String appendPath = "/sdcard/merge/append/";// 拼接的路径

	private int count = 0; // 这是用来给录音起名字的
	private ProgressDialog dialog;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record_append);

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

		appendFile = new File(appendPath);
		if (!appendFile.exists()) {
			appendFile.mkdirs();
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


				PackageManager pm = getPackageManager();
				boolean permission = (PackageManager.PERMISSION_GRANTED == pm
						.checkPermission("android.permission.RECORD_AUDIO",
								"com.example.merge"));
				System.out.println("permission=============="+permission);
				if (permission) {
					// 有权限

				} else {
					// 无权限
				}

				int num = 0;
				for (int i = 0; i < list.size(); i++) {
					if (1 == list.get(i).getState()) {
						num++;
					}
				}

				if (num < 2) {
					Toast.makeText(getApplicationContext(), "至少选择两条录音才能拼接",
							Toast.LENGTH_SHORT).show();
				} else {
					dialog.setMessage("合音中...");
					dialog.show();
					append(num);
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
		} else {
			list.get(position).setState(0);
		}
		adapter.setList(list);
	}

	/**
	 * 拼接多段Mp4
	 *
	 * @param num
	 */
	private void append(int num) {

		// 把选中的item的path存到一个List里
		List<String> phthList = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++) {
			if (1 == list.get(i).getState()) {
				phthList.add(list.get(i).getPath());
			}
		}

		try {
			List<Movie> inMovies = new ArrayList<Movie>();
			for (String videoUri : phthList) {
				inMovies.add(MovieCreator.build(videoUri));
			}

			List<Track> audioTracks = new LinkedList<Track>();
			for (Movie m : inMovies) {
				for (Track t : m.getTracks()) {
					if (t.getHandler().equals("soun")) {
						audioTracks.add(t);
					}
				}
			}

			Movie result = new Movie();

			if (!audioTracks.isEmpty()) {
				result.addTrack(new AppendTrack(audioTracks
						.toArray(new Track[audioTracks.size()])));
			}

			Container con = new DefaultMp4Builder().build(result);

			@SuppressWarnings("resource")
			FileChannel fc = new RandomAccessFile(appendFile.getAbsolutePath()
					+ "/appendAudio.mp4", "rw").getChannel();
			con.writeContainer(fc);
			fc.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		// 将所有item点击状态设置为未点击并刷新adapter
		for (int i = 0; i < list.size(); i++) {
			list.get(i).setState(0);
		}
		adapter.setList(list);

		Toast.makeText(
				getApplicationContext(),
				"拼接完成,并存储在" + appendFile.getAbsolutePath() + "/appendAudio.mp4",
				Toast.LENGTH_SHORT).show();
		dialog.dismiss();
	}

}
