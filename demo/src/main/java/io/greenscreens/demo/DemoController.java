/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 */
package io.greenscreens.demo;

import java.util.ArrayList;
import java.util.List;

import io.greenscreens.ext.ExtJSObjectResponse;
import io.greenscreens.ext.ExtJSResponse;
import io.greenscreens.ext.ExtJSResponseList;
import io.greenscreens.ext.annotations.ExtJSAction;
import io.greenscreens.ext.annotations.ExtJSDirect;
import io.greenscreens.ext.annotations.ExtJSMethod;

/**
 * Example controller class mapped to JavaScript functions
 */
@ExtJSDirect(paths = { DemoURLConstants.WSOCKET, DemoURLConstants.API })
@ExtJSAction(namespace = DemoURLConstants.NAMESPACE, action = "Demo")
public class DemoController {

	@ExtJSMethod("hello")
	public ExtJSResponse helloWorld(final String name) {
		
		return ExtJSResponse.Builder.create()
				.setStatus(true)
				.setMessage("Hello ".concat(name))
				.build();
	}

	@ExtJSMethod("saveUser")
	public ExtJSObjectResponse<UserModel> save(final String name, final String email) {

		final UserModel model = getUser(name, email);
		
		return ExtJSObjectResponse.Builder.create(UserModel.class)
				.setStatus(true)
				.setData(model)
				.build();
	}
	
	@ExtJSMethod("listUsers")
	public ExtJSResponseList<UserModel> query() {
		
		final List<UserModel> list = new ArrayList<>();
		
		list.add(getUser("John Doe", "john.doe@acme.com"));
		list.add(getUser("Jane Doe", "jane.doe@acme.com"));
		list.add(getUser("Mark Smith", "mark.smith@acme.com"));
		
		return ExtJSResponseList.Builder.create(UserModel.class)
				.setStatus(true)
				.setData(list)
				.build();	
	}
	
	private UserModel getUser(final String name, final String email) {
		final UserModel model = new UserModel();
		model.setId(System.currentTimeMillis());
		model.setName(name);
		model.setEmail(email);
		return model;
	}
	
}
