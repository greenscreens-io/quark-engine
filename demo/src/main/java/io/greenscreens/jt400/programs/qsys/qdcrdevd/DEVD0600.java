/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 *
 * https://www.greenscreens.io
 *
 */
package io.greenscreens.jt400.programs.qsys.qdcrdevd;

import io.greenscreens.jt400.annotations.JT400Format;

/**
 * Definition for DEVD0600 format - partial.
 * One can define only required data.
 *
 * Detailed information for device category *DSP
 */
@JT400Format(length = 1124)
public class DEVD0600 extends DEVD0100 {

	@JT400Format(offset = 892, length = 10)
	protected String jobName;

	@JT400Format(offset = 902, length = 10)
	protected String userName;

	@JT400Format(offset = 912, length = 10)
	protected String jobNumber;

	@JT400Format(offset = 918, length = 10)
	protected String currentMessageQueue;

	@JT400Format(offset = 928, length = 10)
	protected String currentMessageQueueLibrary;

	@JT400Format(offset = 877, length = 15)
	protected String ipAddress;

	@Override
	public String toString() {
		return "DEVD0600 [jobName=" + jobName + ", userName=" + userName + ", jobNumber=" + jobNumber
				+ ", currentMessageQueue=" + currentMessageQueue + ", currentMessageQueueLibrary="
				+ currentMessageQueueLibrary + ", ipAddress=" + ipAddress + "]";
	}

}
