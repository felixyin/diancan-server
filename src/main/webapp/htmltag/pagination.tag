${tagBody!}
<script type="text/javascript" src="/www/script/jquery.pagination.js"></script>
<script type="text/javascript">
$(function(){
	var limit = ${page.pageSize!10};
	var totalRow = ${page.totalRow!0};
	var current_page = ${page.pageNumber!1};
	## var params_url="";
	## if(!empty("params")){
		## var array = strutil.split(params,",");
		## for(a in array){
			## if(_value(a)!=null){
			## params_url=params_url+"&"+a+"="+_value(a);
			## }
		## }
	## }
	jQuery("#Pagination").pagination(totalRow, {
		num_edge_entries: 2, //边缘页数
		num_display_entries: 5, //主体页数
		items_per_page:limit, //每页显示1项
		current_page:current_page,
		prev_text:'上一页',
		next_text:'下一页',
		prev_show_always:${prev_show_always!false},
		next_show_always:${next_show_always!false},
		link_to:encodeURI('?p=__id__${params_url!}'),
		callback:function(){}
	});	
});
</script>