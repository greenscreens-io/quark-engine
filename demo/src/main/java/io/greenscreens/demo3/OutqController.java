/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 *
 * https://www.greenscreens.io
 *
 */
package io.greenscreens.demo3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.as400.access.OutputQueue;
import com.ibm.as400.access.OutputQueueList;
import com.ibm.as400.access.PrintObject;
import com.ibm.as400.access.PrintParameterList;
import com.ibm.as400.access.SpooledFile;
import com.ibm.as400.access.SpooledFileList;

import io.greenscreens.demo.DemoURLConstants;
import io.greenscreens.demo1.Authenticated;
import io.greenscreens.demo1.SystemI;
import io.greenscreens.ext.ExtJSResponse;
import io.greenscreens.ext.ExtJSResponseList;
import io.greenscreens.ext.annotations.ExtJSAction;
import io.greenscreens.ext.annotations.ExtJSDirect;
import io.greenscreens.ext.annotations.ExtJSMethod;

/**
 * Example controller class to work with OUTQ's
 *
 */
@ExtJSDirect(paths = { DemoURLConstants.WSOCKET, DemoURLConstants.API })
@ExtJSAction(namespace = DemoURLConstants.NAMESPACE, action = "OUTQ")
public class OutqController {

	private static final Logger LOG = LoggerFactory.getLogger(OutqController.class);

	@Inject @Authenticated
	SystemI as400;

	/**
	 * Example listing all spool files,
	 * one might set filter to limit number of returned data
	 * @param outq
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@ExtJSMethod("spools")
	public ExtJSResponseList<SpoolData> spools(final String outq) {

		final ExtJSResponseList.Builder<SpoolData> builder = ExtJSResponseList.Builder.create(SpoolData.class);

		final SpooledFileList list = new SpooledFileList(as400);

		try {

			list.setQueueFilter(outq);
			final Enumeration<SpooledFile> enums = list.getObjects();

			final Collection<SpoolData> data = new ArrayList<>();
			builder.setData(data);

			while (enums.hasMoreElements()) {
				SpooledFile file = enums.nextElement();
				data.add(convert(file));
			}

		} catch (Exception e) {
			builder.setMessage(e.getMessage());
			LOG.error(e.getMessage());
			LOG.debug(e.getMessage(), e);
		} finally {
			list.close();
		}

		return builder.build();
	}

	/**
	 * List all OUTQ's available to the currently authenticated user
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@ExtJSMethod("list")
	public ExtJSResponseList<String> list() {

		final ExtJSResponseList.Builder<String> builder = ExtJSResponseList.Builder.create(String.class);

		final OutputQueueList list = new OutputQueueList(as400);

		try {
			final Enumeration<OutputQueue> enums = list.getObjects();
			final Collection<String> data = new ArrayList<String>();

			while (enums.hasMoreElements()) {
				OutputQueue queue = enums.nextElement();
				data.add(queue.getPath());
			}

			builder.setStatus(true).setData(data);
		} catch (Exception e) {
			builder.setMessage(e.getMessage());
			LOG.error(e.getMessage());
			LOG.debug(e.getMessage(), e);
		} finally {
			list.close();
		}

		return builder.build();
	}

	/**
	 * Remove spool files from given OUTQ
	 * @param outq
	 * @param allUsers
	 * @return
	 */
	@ExtJSMethod("clear")
	public ExtJSResponse clear(final String outq, final boolean allUsers) {

		ExtJSResponse.Builder builder = ExtJSResponse.Builder.create();

		try {
			final PrintParameterList params = new PrintParameterList();
			final OutputQueue queue = new OutputQueue(as400, outq);

			if (allUsers) {
				params.setParameter(PrintObject.ATTR_JOBUSER, "*ALL");
			} else {
				params.setParameter(PrintObject.ATTR_JOBUSER, "*CURRENT");
			}

			queue.clear(params);
			builder.setStatus(true);
		} catch (Exception e) {
			builder.setMessage(e.getMessage());
			LOG.error(e.getMessage());
			LOG.debug(e.getMessage(), e);
		}

		return builder.build();
	}

	static SpoolData convert(final SpooledFile file) {

		return SpoolData.builder()
			.withSysName(file.getJobSysName())
			.withSpoolName(file.getName())
			.withNumber(file.getNumber())
			.withJobName(file.getJobName())
			.withJobNumber(file.getJobNumber())
			.withJobUser(file.getJobUser())
			.build();
	}

}
