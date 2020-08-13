/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 *
 * https://www.greenscreens.io
 *
 */
package io.greenscreens.demo3;

public class SpoolData {

	private String outq;
	private String sysName;
	private String spoolName;
	private int number;
	private String jobName;
	private String jobUser;
	private String jobNumber;

	public SpoolData() {}

	public String getOutq() {
		return outq;
	}

	public void setOutq(String outq) {
		this.outq = outq;
	}

	public String getSysName() {
		return sysName;
	}

	public void setSysName(String sysName) {
		this.sysName = sysName;
	}

	public String getSpoolName() {
		return spoolName;
	}

	public void setSpoolName(String spoolName) {
		this.spoolName = spoolName;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getJobUser() {
		return jobUser;
	}

	public void setJobUser(String jobUser) {
		this.jobUser = jobUser;
	}

	public String getJobNumber() {
		return jobNumber;
	}

	public void setJobNumber(String jobNumber) {
		this.jobNumber = jobNumber;
	}

	private SpoolData(Builder builder) {
		this.outq = builder.outq;
		this.sysName = builder.sysName;
		this.spoolName = builder.spoolName;
		this.number = builder.number;
		this.jobName = builder.jobName;
		this.jobUser = builder.jobUser;
		this.jobNumber = builder.jobNumber;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {

		private String outq;
		private String sysName;
		private String spoolName;
		private int number;
		private String jobName;
		private String jobUser;
		private String jobNumber;

		private Builder() {}

		public Builder withOutq(String outq) {
			this.outq = outq;
			return this;
		}

		public Builder withSysName(String sysName) {
			this.sysName = sysName;
			return this;
		}

		public Builder withSpoolName(String spoolName) {
			this.spoolName = spoolName;
			return this;
		}

		public Builder withNumber(int number) {
			this.number = number;
			return this;
		}

		public Builder withJobName(String jobName) {
			this.jobName = jobName;
			return this;
		}

		public Builder withJobUser(String jobUser) {
			this.jobUser = jobUser;
			return this;
		}

		public Builder withJobNumber(String jobNumber) {
			this.jobNumber = jobNumber;
			return this;
		}

		public SpoolData build() {
			return new SpoolData(this);
		}

	}

}
