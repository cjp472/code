if($("#lx_comm_type").size()>0){
    lx.ajax("/app/list/config?lx_status=1&lx_p_k=comm_type",function (data) {
        $.each(data.rows, function (index, item) {
            $("#lx_comm_type").append(new Option(item.v, item.v));// 下拉菜单里添加元素
        });
        layui.form.render("select");
    })
}