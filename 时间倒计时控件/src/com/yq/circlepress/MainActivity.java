package com.yq.circlepress;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

	private RoundProgressBar iv_progress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		long progress = System.currentTimeMillis() + 1 * 5 * 1000;
		setContentView(R.layout.activity_main);
		iv_progress = (RoundProgressBar) findViewById(R.id.iv_progress);
		iv_progress.setEndTime(progress);
	}

}
