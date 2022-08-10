
# [Quark Engine](https://quark.greenscreens.ltd/).

Just as quarks are building blocks which glues subatomic particles into atoms,
Green Screens Quark Engine is a small, lite and fast elementary building block between web and Java server side.

NOTE: For project modules, please refer to individual project repositories.

  [Quark Engine - Java](https://github.com/greenscreens-io/quark-java)
  [Quark Engine - Browser](https://github.com/greenscreens-io/quark-web)
  [Quark Engine - NodeJS](https://github.com/greenscreens-io/quark-node)
  [Quark Engine - Demo App](https://github.com/greenscreens-io/quark-demo)

The Quark Engine is a JavaScript and Java web library to enable dynamic remote calls of exposed Java Methods.

Instead of making rest or plain JSON calls, Quark Engine is simplifying this process by allowing
to call Java Server Classes and methods as they as running locally in the browser without
programmers worrying about underlying REST/WebSocket data structures.

Supported channels are WebSocket and HTTP/S operations. Data is automatically encrypted
by the browser Crypto API protecting JSON data structures even TLS/SSL is not used.

Base concept is to create Controller classes annotated with Ext* Annotations which will
instruct CDI engine and provided WebSocket or Servlet what to expose.

On front end part, all what is required is to include small JavaScript lib, part of the Quark Engine.
JavaScript engine is only 7KB in size, and its main purpose is to retrieve signature list of defined exposed
Java Classes/Methods, then to generate and link internal calling mechanism.

To see how to use it, visit project web page [here](https://quark.greenscreens.ltd/).

&copy; Green Screens Ltd. 2016 - 2022
