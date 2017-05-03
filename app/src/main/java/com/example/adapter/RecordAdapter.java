package com.example.adapter;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.bean.RecordBean;
import com.example.merge.R;

public class RecordAdapter extends ListItemAdapter<RecordBean> {

	private OnChooseListener chooseListener;

	public RecordAdapter(Context context) {
		super(context);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		Holder holder = null;
		if (convertView == null) {
			holder = new Holder();
			convertView = View.inflate(context, R.layout.record_item, null);
			holder.tv = (TextView) convertView
					.findViewById(R.id.record_item_tv);
			holder.ll = (LinearLayout) convertView
					.findViewById(R.id.record_item_ll);

			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}

		final RecordBean bean = myList.get(position);

		holder.tv.setText(bean.getName());
		if (0 == bean.getState()) {
			holder.ll.setBackgroundResource(R.drawable.record_ll_bg_white);
		} else {
			holder.ll.setBackgroundResource(R.drawable.record_ll_bg_blue);
		}
		holder.ll.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				chooseListener.onChoose(position, bean.getState(),bean.getPath());
			}
		});
		return convertView;
	}

	class Holder {
		private TextView tv;
		private LinearLayout ll;
	}

	public interface OnChooseListener {
		void onChoose(int position, int state,String path);
	}

	public void setOnChooseListener(OnChooseListener listener) {
		this.chooseListener = listener;
	}
}
