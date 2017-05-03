package com.example.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class ListItemAdapter<T> extends BaseAdapter {
	protected Context context;
	protected List<T> myList;

	public ListItemAdapter(Context context) {
		this.context = context;
	}

	/**
	 * 得到适配器所有数据
	 *
	 * @return
	 */
	public List<T> getList() {
		return myList;
	}

	/**
	 * 为适配器刷新数据。并且重新唤醒适配器
	 *
	 * @param list
	 */
	public void setList(List<T> list) {
		this.myList = list;
		notifyDataSetChanged();
	}

	/**
	 * 为适配器添加一个List<T>的全部数据。并且重新唤醒适配器
	 *
	 * @param list
	 */
	public void addList(List<T> list) {
		if (myList == null) {
			myList = new ArrayList<T>();
		}
		myList.addAll(list);// 此方法是将传进来的List<T>全部数据条目添加到myList<T>中
		notifyDataSetChanged();
	}

	/**
	 * 为适配器添加一个泛型条目T
	 *
	 * @param t
	 */
	public void addItem(T t) {
		if (myList == null) {
			myList = new ArrayList<T>();
		}
		myList.add(t);
		notifyDataSetChanged();
	}

	/**
	 * 为适配器删除一个泛型条目T
	 *
	 * @param t
	 */
	public void deleteItem(int position) {
		if (myList != null) {
			myList.remove(position);
			notifyDataSetChanged();
		} else {
			// 删除失败 适配器为空
		}

	}

	/**
	 * 为适配器添加一个泛型条目T
	 *
	 * @param t
	 */
	public void changeItem(T t, int index) {
		if (myList == null) {
			myList = new ArrayList<T>();
		}
		myList.set(index, t);
		notifyDataSetChanged();
	}

	/**
	 * 判断适配器数据是否为空
	 *
	 * @return
	 */
	public Boolean isNull() {
		return myList == null || myList.size() == 0;
	}

	@Override
	public int getCount() {
		return null == myList ? 0 : myList.size();
	}

	@Override
	public Object getItem(int position) {
		return myList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public abstract View getView(int position, View convertView,
								 ViewGroup parent);

}
