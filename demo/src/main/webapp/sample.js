/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 */

/**
 * Demo showing remote API calls through WebSocket or http/s fetch  
 *
 * NOTE: Engine.init should be used only once to initialize 
 *       Here it is used in every function only for testing purposes  
 * 
 * api - used to retrieve API definitions 
 * ws - used for API service call
 * 
 *  NOTE: if *api* used as a service call, 
 *  http/s protocol will be used instead of WebSocket protocol.
 *  
 *   This engine supports both channels
 */

const isSecured  = location.protocol == "https:";
const wsProtocol = isSecured ? 'wss':'ws';
const ws    = `${wsProtocol}://${location.host}/demo/socket`;
const api   = `${location.origin}/demo/api`;
const msg   = 'This is a test for encrypting đšžćčĐŠŽČ';

/**
 * Empty call without network channels
 */
async function test1() {

    await Generator.build();
    Generator.on('call', function(o, cb){o.result={success:true, data: {}};cb(null, o)});

    try {
        let res = await io.greenscreens.Demo.hello('John Doe');
        console.log(res);
    } catch (e) {
        console.log(e);
    }
}

/**
 * Sample to initialize engine with Promises call
 * Connector is http servlet 
 */
function test2() {

    Engine.init({api: api, service:api})
    .then(() => {
        console.log('Engine ready');
        return io.greenscreens.Demo.hello('John Doe');
    })
    .then((o) => {
        console.log('API call result');
        console.log(o);
    })
    .catch((e) => {
        console.log(e.message || e.msg || e);
    });    

}

/**
 * Sample to initialize engine with async function
 * Connector is http servlet
 */
async function test3() {

    await Engine.init({api: api, service:api});

    let data = await io.greenscreens.Demo.hello('John Doe');

    console.log(data);
}

/**
 * Test JSON ecryption
 */
async function test4() {

    if (!Security.isActive()) {
       await Generator.build(); 
    }

    let enc = await Security.encrypt(msg);
    console.log(enc);
}

/**
 * Test Getting user data
 * Connector is WebSocket service
 */
async function test5() {

    await Engine.init({api: api, service:ws});

    let data = await io.greenscreens.Demo.saveUser('John Doe', 'john.doe@acme.com');
    console.log(data);
}

/**
 * Test Getting list of users
 * Connector is WebSocket service
 */
async function test6() {

    await Engine.init({api: api, service:ws});

    let data = await io.greenscreens.Demo.listUsers();
    console.log(data);
}


/**
 * Test multipel calls
 * Connector is WebSocket service
 */
async function test7() {

    await Engine.init({api: api, service:ws});

	let i = 10;
	while(i--) {
      let data = await io.greenscreens.Demo.hello('John Doe call: ' + i);
      console.log(data);		
	}

}
