/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 *
 * https://www.greenscreens.io
 *
 */
package io.greenscreens.jt400.programs.qsys.qdcrdevd;

import com.ibm.as400.access.AS400DataType;

import io.greenscreens.jt400.annotations.JT400Format;
import io.greenscreens.jt400.interfaces.IJT400Format;

/**
 * 	Basic device information.
 */
@JT400Format(length = 104)
public class DEVD0100 implements IJT400Format {

	@JT400Format(offset = 0, type = AS400DataType.TYPE_BIN4)
	protected int bytesReturned;

	@JT400Format(offset = 4, type = AS400DataType.TYPE_BIN4)
	protected int bytesAvailable;

	@JT400Format(offset = 8, length = 7)
	protected String date;

	@JT400Format(offset = 15, length = 6)
	protected String time;

	@JT400Format(offset = 21, length = 10)
	protected String deviceName;

	@JT400Format(offset = 31, length = 10)
	protected String deviceCategory;

	@JT400Format(offset = 41, length = 10)
	protected String onlineAtIPL;

	@JT400Format(offset = 51, length = 50)
	protected String description;

	@JT400Format(offset = 101, length = 3)
	protected String reserve;

	public int getBytesReturned() {
		return bytesReturned;
	}

	public void setBytesReturned(int bytesReturned) {
		this.bytesReturned = bytesReturned;
	}

	public int getBytesAvailable() {
		return bytesAvailable;
	}

	public void setBytesAvailable(int bytesAvailable) {
		this.bytesAvailable = bytesAvailable;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getDeviceCategory() {
		return deviceCategory;
	}

	public void setDeviceCategory(String deviceCategory) {
		this.deviceCategory = deviceCategory;
	}

	public String getOnlineAtIPL() {
		return onlineAtIPL;
	}

	public void setOnlineAtIPL(String onlineAtIPL) {
		this.onlineAtIPL = onlineAtIPL;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getReserve() {
		return reserve;
	}

	public void setReserve(String reserve) {
		this.reserve = reserve;
	}

	@Override
	public String toString() {
		return "DEVD0100 [bytesReturned=" + bytesReturned + ", bytesAvailable=" + bytesAvailable + ", date=" + date
				+ ", time=" + time + ", deviceName=" + deviceName + ", deviceCategory=" + deviceCategory
				+ ", onlineAtIPL=" + onlineAtIPL + ", description=" + description + ", reserve=" + reserve + "]";
	}

}
