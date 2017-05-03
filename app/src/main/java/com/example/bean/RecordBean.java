package com.example.bean;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

public class RecordBean implements Serializable {

	private int id;
	private String name;// 录音的名字
	private String path;// 录音的路径
	private int state;// 是否选中 0:未选中 1:已选中
	private String decodePath;// 解码后的路径

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getDecodePath() {
		return decodePath;
	}

	public void setDecodePath(String decodePath) {
		this.decodePath = decodePath;
	}

}
