var DataSourceTree = function(options) {
	this._data 	= options.data;
	this._delay = options.delay;
}
DataSourceTree.prototype.data = function(options, callback) {
	var self = this;
	var $data = null;
	if(!("name" in options) && !("type" in options)){
		$data = this._data;
		callback({ data: $data });
		return;
	}
	else if("type" in options && options.type == "folder") {
		if("additionalParameters" in options && "children" in options.additionalParameters)
			$data = options.additionalParameters.children;
		else $data = {}
	}
	if($data != null)
		setTimeout(function(){callback({ data: $data });} , parseInt(Math.random() * 500) + 200);
};
var tree_data = {
	'data_1' : {name: '数据源一', type: 'folder', 'icon-class':'red'}	,
	'data_2' : {name: '数据源二', type: 'folder', 'icon-class':'orange'}	,
	'data_3' : {name: '数据源三', type: 'folder', 'icon-class':'blue'}	,
	'data_4' : {name: '数据源四', type: 'folder', 'icon-class':'green'}	,
	'data_5' : {name: '数据源五', type: 'folder'}	,
	'data_6' : {name: '<i class="icon-file-text grey"></i>关于我们', type: 'item'},
	'data_7' : {name: '<i class="icon-book blue"></i>联系我们', type: 'item'}
}
tree_data['data_2']['additionalParameters'] = {
	'children' : [
		{name: '<i class="icon-music blue"></i>测试数据1', type: 'item'},
		{name: '<i class="icon-music blue"></i>测试数据2', type: 'item'},
		{name: '<i class="icon-music blue"></i>测试数据3', type: 'item'},
		{name: '<i class="icon-music blue"></i>测试数据4', type: 'item'},
		{name: '<i class="icon-music blue"></i>测试数据5', type: 'item'}
	]
}
tree_data['data_3']['additionalParameters'] = {
	'children' : [
		{name: '<i class="icon-film blue"></i>测试数据6', type: 'item'},
		{name: '<i class="icon-film blue"></i>测试数据7', type: 'item'},
		{name: '<i class="icon-film blue"></i>测试数据8', type: 'item'},
		{name: '<i class="icon-film blue"></i>测试数据9', type: 'item'},
		{name: '<i class="icon-film blue"></i>测试数据10', type: 'item'}
	]
}
tree_data['data_1']['additionalParameters'] = {
	'children' : {
		'data_1.1' : {name: '数据源一/1', type: 'folder', 'icon-class':'pink'},
		'data_1.2' : {name: '数据源一/2', type: 'folder', 'icon-class':'pink'}
	}
}
tree_data['data_1']['additionalParameters']['children']['data_1.1']['additionalParameters'] = {
	'children' : [
		{name: '<i class="icon-picture green"></i>测试数据11', type: 'item'},
		{name: '<i class="icon-picture green"></i>测试数据12', type: 'item'},
		{name: '<i class="icon-picture green"></i>测试数据13', type: 'item'},
		{name: '<i class="icon-picture green"></i>测试数据14', type: 'item'}
	]
}
tree_data['data_1']['additionalParameters']['children']['data_1.2']['additionalParameters'] = {
	'children' : [
		{name: '<i class="icon-picture green"></i>测试数据15', type: 'item'},
		{name: '<i class="icon-picture green"></i>测试数据16', type: 'item'},
		{name: '<i class="icon-picture green"></i>测试数据17', type: 'item'},
		{name: '<i class="icon-picture green"></i>测试数据18', type: 'item'},
		{name: '<i class="icon-picture green"></i>测试数据19', type: 'item'},
		{name: '<i class="icon-picture green"></i>测试数据20', type: 'item'}
	]
}


tree_data['data_4']['additionalParameters'] = {
	'children' : [
		{name: '<i class="icon-file-text red"></i>测试数据21', type: 'item'},
		{name: '<i class="icon-file-text grey"></i>测试数据22', type: 'item'},
		{name: '<i class="icon-file-text grey"></i>测试数据23', type: 'item'},
		{name: '<i class="icon-file-text red"></i>测试数据24', type: 'item'},
		{name: '<i class="icon-file-text grey"></i>测试数据25', type: 'item'}
	]
}

tree_data['data_5']['additionalParameters'] = {
	'children' : [
		{name: '<i class="icon-archive brown"></i>测试数据26', type: 'item'},
		{name: '<i class="icon-archive brown"></i>测试数据27', type: 'item'},
		{name: '<i class="icon-archive brown"></i>测试数据28', type: 'item'},
		{name: '<i class="icon-archive brown"></i>测试数据29', type: 'item'}
	]
}
var treeDataSource = new DataSourceTree({data: tree_data});