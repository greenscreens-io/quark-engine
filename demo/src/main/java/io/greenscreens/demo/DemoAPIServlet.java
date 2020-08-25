/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 */
package io.greenscreens.demo;

import javax.servlet.annotation.WebServlet;
import io.greenscreens.quark.web.APIServlet;

/**
 * Servlet to render API structure
 */
@WebServlet(DemoURLConstants.API)
public class DemoAPIServlet extends APIServlet {

	private static final long serialVersionUID = 1L;

}
