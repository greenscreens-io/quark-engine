/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 *
 * https://www.greenscreens.io
 *
 */
package io.greenscreens.jt400.programs.qsys.qdcrdevd;

import io.greenscreens.jt400.annotations.JT400Format;

@JT400Format(length = 177)
public class DEVD0300 extends DEVD0100 {

	@JT400Format(offset = 104, length = 10)
	protected String remoteLocationName;

	@JT400Format(offset = 114, length = 10)
	protected String controllerName;

	@JT400Format(offset = 124, length = 10)
	protected String jobName;

	@JT400Format(offset = 134, length = 10)
	protected String userName;

	@JT400Format(offset = 144, length = 6)
	protected String jobNumber;

	@JT400Format(offset = 150, length = 10)
	protected String queueName;

	@JT400Format(offset = 160, length = 10)
	protected String queueLibrary;

	@JT400Format(offset = 170, length = 7)
	protected String lastActivity;

	public String getRemoteLocationName() {
		return remoteLocationName;
	}

	public void setRemoteLocationName(String remoteLocationName) {
		this.remoteLocationName = remoteLocationName;
	}

	public String getControllerName() {
		return controllerName;
	}

	public void setControllerName(String controllerName) {
		this.controllerName = controllerName;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getJobNumber() {
		return jobNumber;
	}

	public void setJobNumber(String jobNumber) {
		this.jobNumber = jobNumber;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public String getQueueLibrary() {
		return queueLibrary;
	}

	public void setQueueLibrary(String queueLibrary) {
		this.queueLibrary = queueLibrary;
	}

	public String getLastActivity() {
		return lastActivity;
	}

	public void setLastActivity(String lastActivity) {
		this.lastActivity = lastActivity;
	}

}
