package com.example.merge;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AACTrackImpl;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

/***
 * 音频与视频合并
 *
 * @author 帽檐遮不住阳光
 *
 * @data  2016/5/27
 *
 */
public class AudioWithVideoActivity extends Activity implements OnClickListener {

	private String audioPath = "";// 音频的SD卡路径 可以随便找一个手机上的aac文件的路径
	private String videoPath = "";// 视频的SD卡路径 可以随便找一个手机上视频的路径
	private String mergePath = "";// 合成的视频保存的路径
	private Movie countVideo;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_audio_with_video);

		findViewById(R.id.btn).setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case R.id.btn:
				if ("".equals(audioPath) || "".equals(videoPath)) {
					Toast.makeText(getApplicationContext(), "音频或视频路径为空,请自行加上",
							Toast.LENGTH_SHORT).show();
				} else {
					merge();
				}
				break;
		}
	}

	private void merge() {
		try {
			countVideo = MovieCreator.build(videoPath);
			AACTrackImpl aacTrackOriginal = new AACTrackImpl(
					new FileDataSourceImpl(audioPath));

			countVideo.addTrack(aacTrackOriginal);

			Container out = new DefaultMp4Builder().build(countVideo);
			FileOutputStream fos;

			// 保存的位置
			fos = new FileOutputStream(new File(mergePath));
			out.writeContainer(fos.getChannel());
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
