/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 *
 * https://www.greenscreens.io
 *
 */
package io.greenscreens.demo2;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.as400.access.IFSFile;

import io.greenscreens.cdi.Required;
import io.greenscreens.demo.DemoURLConstants;
import io.greenscreens.demo1.Authenticated;
import io.greenscreens.demo1.SystemI;
import io.greenscreens.demo2.WebFile.TYPE;
import io.greenscreens.ext.ExtJSResponse;
import io.greenscreens.ext.ExtJSResponseList;
import io.greenscreens.ext.annotations.ExtJSAction;
import io.greenscreens.ext.annotations.ExtJSDirect;
import io.greenscreens.ext.annotations.ExtJSMethod;

/**
 * Example controller class to work with IFS file system
 *
 * await Engine.init({api: api, service:api});
 * await io.greenscreens.AS400.login('as400.acme.com', 'QSECOFR', 'QSECOFR')
 * files = await io.greenscreens.IFS.list('/')
 * await io.greenscreens.AS400.logout()
 *
 */
@ExtJSDirect(paths = { DemoURLConstants.WSOCKET, DemoURLConstants.API })
@ExtJSAction(namespace = DemoURLConstants.NAMESPACE, action = "IFS")
public class IFSController {

	private static final Logger LOG = LoggerFactory.getLogger(IFSController.class);

	@Inject @Authenticated
	SystemI as400;

	/**
	 * List ifs data based on given path.
	 * @param path
	 * @return
	 */
	@ExtJSMethod("list")
	public ExtJSResponseList<WebFile> login(@Required final String path) {

		final ExtJSResponseList.Builder<WebFile> resp = ExtJSResponseList.Builder.create(WebFile.class);

		try {

			final IFSFile root = new IFSFile(as400, path);
			final IFSFile [] list = root.listFiles();

			final Collection<WebFile> data = new ArrayList<WebFile>();
			for (IFSFile file : list) {
				WebFile webFile = new WebFile();
				webFile.setCreated(file.created());
				webFile.setName(file.getName());
				webFile.setPath(file.getAbsolutePath());
				webFile.setType(file.isDirectory() ? TYPE.FOLDER : TYPE.FILE);
				data.add(webFile);
			}

			resp.setData(data);
			resp.setStatus(true);

		} catch (Exception e) {
			resp.setMessage(e.getMessage());
			LOG.error(e.getMessage());
			LOG.debug(e.getMessage(), e);
		}

		return resp.build();
	}

	@ExtJSMethod("rename")
	public ExtJSResponse rename(final String path, final String name) {

		return new ExtJSResponse(true, null);
	}

	@ExtJSMethod("remove")
	public ExtJSResponse remove(final String path) {

		return new ExtJSResponse(true, null);
	}

	@ExtJSMethod("move")
	public ExtJSResponse move(final String from, final String to) {

		return new ExtJSResponse(true, null);
	}

	@ExtJSMethod("copy")
	public ExtJSResponse copy(final String from, final String to) {

		return new ExtJSResponse(true, null);
	}

}
