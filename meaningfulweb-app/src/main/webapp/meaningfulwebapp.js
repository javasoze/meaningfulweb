var doExtract = function (url, callback) {
	$.getJSON('get-meaning', {'url': url}, callback);
};

