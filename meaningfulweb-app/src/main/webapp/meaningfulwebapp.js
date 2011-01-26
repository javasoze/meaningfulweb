var doExtract = function (url, callback) {
	$.getJSON('get-meaning', {'url': url}, callback);
};

/*
function removeAllChildren(elem){
	if ( elem.hasChildNodes() ){
		while (elem.childNodes.length >= 1 )
		{
			elem.removeChild( elem.firstChild );	   
		} 
	}
}

function doExtract(){
	document.getElementById('doExtract').disable=true;
	var resultDiv = document.getElementById('extractionresult');
	removeAllChildren(resultDiv);
	var url = document.getElementById('url').value;
	$.getJSON("get-meaning?url="+url,renderResult);
	document.getElementById('doExtract').disable=false;
}



function renderResult(jsonObj){
	var resultDiv = document.getElementById('extractionresult');
	
	var tableNode = document.createElement('table');
	tableNode.setAttribute('align','center');
	tableNode.setAttribute('border','1');
	resultDiv.appendChild(tableNode);
	var rowNode = document.createElement("tr");
	tableNode.appendChild(rowNode);
	var imgCell = document.createElement("td");
	rowNode.appendChild(imgCell);
	
	var imgNode = document.createElement("img");
	imgCell.appendChild(imgNode);
	imgNode.setAttribute('src',jsonObj.image);
	
	var subTableCell = document.createElement(td);
	rowNode.appendChild(subTableCell);
	var subTableNode = document.createElement("table");
	subTableNode.setAttribute('width','100%');
	subTableNode.setAttribute('border','1');
	subTableCell.appendChild(subTableNode);
	
	
	
}*/
