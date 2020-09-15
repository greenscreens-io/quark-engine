/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 */

/**
 * Browser native compression
 */
class Streams {

	// 'deflate' or 'gzip'

	static isAvailable() {
		return typeof CompressionStream !== 'undefined';
	}

	static async compress(text, encoding = 'gzip') {
		let byteArray = new TextEncoder().encode(text);
		let cs = new CompressionStream(encoding);
		let writer = cs.writable.getWriter();
		writer.write(byteArray);
		writer.close();
		return new Response(cs.readable).arrayBuffer();
	}

	static async decompress(byteArray, encoding = 'gzip') {
		let cs = new DecompressionStream(encoding);
		let writer = cs.writable.getWriter();
		writer.write(byteArray);
		writer.close();
		let arrayBuffer = await new Response(cs.readable).arrayBuffer();
		return new TextDecoder().decode(arrayBuffer);
	}

}
