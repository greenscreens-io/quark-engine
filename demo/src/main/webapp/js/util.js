/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 */


/**
 * Convert hex string to int array
 *
 * @param str
 * @returns
 */
function hex2ab(str) {

	let a = [];

	for (let i = 0; i < str.length; i += 2) {
		a.push(parseInt("0x" + str.substr(i, 2), 16));
	}

	return a;
}

/**
 * Convert string to int array
 *
 * @param
 * 	 str - string to convert
 *
 * @returns
 * 	  ArrayBuffer of ints
 */
function str2ab(str) {

	let buf = new ArrayBuffer(str.length);
	let bufView = new Uint8Array(buf);

	for (let i = 0, strLen = str.length; i < strLen; i++) {
		bufView[i] = str.charCodeAt(i);
	}

	return buf;
}

/**
 * Convert array of ints into hex string
 *
 * @param
 * 	buffer - buffer is an ArrayBuffer
 *
 * @returns
 * 	string in hex format
 */
function buf2hex(buffer) {
	return Array.prototype.map.call(new Uint8Array(buffer), x => ('00' + x.toString(16)).slice(-2)).join('');
}

/**
 * Convert int array (utf8 encoded) to string
 *
 * @param data
 * @returns
 */
function stringFromUTF8Array(data) {

	let extraByteMap = [1, 1, 1, 1, 2, 2, 3, 0];
	let count = data.length;
	let str = "";

	for (let index = 0; index < count;) {

		let ch = data[index++];
		if (ch & 0x80) {

			let extra = extraByteMap[(ch >> 3) & 0x07];
			if (!(ch & 0x40) || !extra || ((index + extra) > count)) {
				return null;
			}

			ch = ch & (0x3F >> extra);
			for (; extra > 0; extra -= 1) {

				let chx = data[index++];
				if ((chx & 0xC0) != 0x80) {
					return null;
				}

				ch = (ch << 6) | (chx & 0x3F);
			}

		}

		str += String.fromCharCode(ch);
	}

	return str;
}
