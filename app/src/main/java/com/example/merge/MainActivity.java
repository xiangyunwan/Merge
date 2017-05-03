package com.example.merge;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends Activity implements OnClickListener {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		findViewById(R.id.btn_torecord).setOnClickListener(this);
		findViewById(R.id.btn_append).setOnClickListener(this);
		findViewById(R.id.btn_audio_and_video).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_torecord:
			startActivity(new Intent(this, RecordWithRecordActivity.class));
			break;
		case R.id.btn_append:
			startActivity(new Intent(this, RecordAppendActivity.class));
			break;
		case R.id.btn_audio_and_video:
			startActivity(new Intent(this, AudioWithVideoActivity.class));
			break;
		}
	}

}
