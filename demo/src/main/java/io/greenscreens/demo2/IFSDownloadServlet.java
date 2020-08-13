/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 *
 * https://www.greenscreens.io
 *
 */
package io.greenscreens.demo2;

import java.io.IOException;
import java.util.Base64;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.IFSFile;
import com.ibm.as400.access.IFSFileInputStream;

/**
 * Servlet for downloading IFS file
 */
@WebServlet("/download")
public class IFSDownloadServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

    public IFSDownloadServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		final HttpSession session = request.getSession();
		final AS400 as4oo = (AS400) session.getAttribute(AS400.class.getCanonicalName());

		if (as4oo == null || !as4oo.isUsePasswordCache()) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not authenticated");
			return;
		}

		try {

			String path = request.getParameter("p");

			if (path == null) {
				throw new Exception("Requested path invalid");
			}

			path = new String(Base64.getDecoder().decode(path), "UTF-8");

			final IFSFile file = new IFSFile(as4oo, path);

			if (!file.exists()) {
				throw new Exception("File does not exists");
			}

			doDownload(response, file);

		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
		}
	}

	void doDownload(final HttpServletResponse resp, final IFSFile file) throws Exception {

		resp.setContentType("application/octet-stream");
		resp.setHeader("Content-Disposition", "filename=\""+file.getName()+"\"");

		final IFSFileInputStream fis = new IFSFileInputStream(file);

		try {
			FileUtil.copyStream(fis, resp.getOutputStream());
		} finally {
			FileUtil.close(fis);
		}

	}

}
