/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 *
 * https://www.greenscreens.io
 *
 */
package io.greenscreens.demo;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.greenscreens.quark.cdi.Required;
import io.greenscreens.quark.ext.ExtJSObjectResponse;
import io.greenscreens.quark.ext.ExtJSResponse;
import io.greenscreens.quark.ext.ExtJSResponseList;
import io.greenscreens.quark.ext.annotations.ExtJSAction;
import io.greenscreens.quark.ext.annotations.ExtJSDirect;
import io.greenscreens.quark.ext.annotations.ExtJSMethod;

/**
 * Example controller class mapped to JavaScript functions
 */
@ExtJSDirect(paths = {   DemoURLConstants.WSOCKET, DemoURLConstants.API })
@ExtJSAction(namespace = DemoURLConstants.NAMESPACE, action = "Demo")
public class DemoController {

	@ExtJSMethod( value = "helloUnsafe", encrypt = false)
	public ExtJSResponse helloWorldUnsafe(final String name) {
		ExtJSResponse resp = new ExtJSResponse(true, null);
		resp.setMsg("Hello ".concat(name));
		return resp;
	}

	@ExtJSMethod("hello")
	public ExtJSResponse helloWorld(@Required final String name) {
		ExtJSResponse resp = new ExtJSResponse(true, null);
		resp.setMsg("Hello ".concat(name));
		return resp;
	}

	@ExtJSMethod(value = "saveUser", validate = true)
	public ExtJSObjectResponse<UserModel> save(
			@NotNull @NotBlank
			@Size(min = 5, max = 20, message = "User must be between 5 and 20 characters long")
			final String name,

			@NotNull @NotBlank @Email
			final String email) {

		final ExtJSObjectResponse<UserModel> resp = new ExtJSObjectResponse<>(true, null);

		final UserModel model = getUser(name, email);
		resp.setData(model);

		return resp;
	}

	@ExtJSMethod("listUsers")
	public ExtJSResponseList<UserModel> query() {

		final List<UserModel> list = new ArrayList<>();

		list.add(getUser("John Doe", "john.doe@acme.com"));
		list.add(getUser("Jane Doe", "jane.doe@acme.com"));
		list.add(getUser("Mark Smith", "mark.smith@acme.com"));

		final ExtJSResponseList<UserModel> resp = new ExtJSResponseList<>(true);
		resp.setData(list);
		return resp;
	}

	private UserModel getUser(final String name, final String email) {
		final UserModel model = new UserModel();
		model.setId(System.currentTimeMillis());
		model.setName(name);
		model.setEmail(email);
		return model;

	}

}
