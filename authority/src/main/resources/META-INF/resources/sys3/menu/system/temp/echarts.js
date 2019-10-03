if (window.location.href.indexOf("/list.html")!=-1) {
    tempc.para =
        [
            {'name': 'ID', 'type': 'id', 'width': '4%', 'val': 'v.id'},
            {'name': '模板名', 'type': 'name', 'width': '6%', 'val': "v.name"},
            //{'name': '模板参数', 'type': 'para', 'width': '6%', 'val': "v.para"},
            //{'name': '模板', 'type': 'temp', 'width': '8%', 'val': 'v.temp'}
        ];
    /*edit js*/
}else if (window.location.href.indexOf("/edit.html")!=-1) {
    document.write('<script src="/sys/js/echarts.min.js" type="text/javascript" charset="utf-8"></script>');
    //全屏
    var index = parent.layer.getFrameIndex(window.name);
    parent.layer.full(index);

    tempc.edit_form = `
    <form action="" method="post" class="form form-horizontal" id="form-member-add">
		<div class="row cl">
			<label class="form-label col-xs-1 col-sm-1"><span class="c-red">*</span>模板名：</label>
			<div class="formControls col-xs-10 col-sm-10">
				<input type="text" class="input-text" value="" placeholder="" id="name" name="name">
			</div>
		</div>
		<div class="row cl">
		    <label class="form-label col-xs-1 col-sm-1"><span class="c-red">*</span>参数：</label>
			<table class="table table-border table-bordered table-striped col-xs-10 col-sm-10">
                <thead>
                    <tr class="text-c">
                        <th width="80%">默认参数</th>
                        <th width="20%">操作</th>
                    </tr>
                </thead>
                <tbody id="tbody" onmouseover="change()">
                    <tr class="text-c">
                        <td><textarea class="textarea" placeholder="参数示例" style="height: 100px"></textarea></td>
                        <td class="td-manage">
                            <a onclick="javascript:add(this)"><i class="Hui-iconfont">&#xe600;</i>添加</a>
                            <a onclick="javascript:del(this)"><i class="Hui-iconfont">&#xe6a1;</i>删除</a>
                            <a onclick="javascript:down(this)"><i class="Hui-iconfont">&#xe674;</i>down</a>
                            <a onclick="javascript:up(this)"><i class="Hui-iconfont">&#xe679;</i>up</a>
                        </td>
                    </tr>
                </tbody>
            </table>
		</div>
		<div class="row cl">
			<label class="form-label col-xs-1 col-sm-1"><span class="c-red">*</span>模板：</label>
			<div class="formControls col-xs-5 col-sm-5">
                <textarea class="textarea" placeholder="说点什么..." onblur="change()" style="height: 500px" rows="20" cols="20" name="temp" id="temp"></textarea>
            </div>
            <div id="main" class="formControls col-xs-5 col-sm-5" style="width: 600px;height:500px;"></div>
		</div>
		<div class="row cl">
			<div class="col-xs-8 col-sm-9 col-xs-offset-6 col-sm-offset-4">
				<input class="btn btn-primary radius" type="submit" value="&nbsp;&nbsp;提交&nbsp;&nbsp;">
			</div>
		</div>
        <input type='hidden' name="status" id='status' value ='1'/>
        <input type='hidden' name="id" id='id' value =''/>
        <input type='hidden' name="para" id='para' value =''/>
	</form>
    `;
    tempc.edit_back = function (data) {
        $("#id").val(data.id);
        $("#name").val(data.name);
        $("#para").val(data.para);
        $("#temp").val(data.temp);
        $("#status").val(data.status);
        var para = JSON.parse(data.para);
        $("#tbody").html('');
        for (var i in para){
            var v = para[i];
            $("#tbody").append(`<tr class="text-c">
                        <td><textarea class="textarea" placeholder="参数示例" style="height: 100px">${v}</textarea></td>
                        <td class="td-manage">
                            <a onclick="javascript:add(this)"><i class="Hui-iconfont">&#xe600;</i>添加</a>
                            <a onclick="javascript:del(this)"><i class="Hui-iconfont">&#xe6a1;</i>删除</a>
                            <a onclick="javascript:down(this)"><i class="Hui-iconfont">&#xe674;</i>down</a>
                            <a onclick="javascript:up(this)"><i class="Hui-iconfont">&#xe679;</i>up</a>
                        </td>
                    </tr>`)
        }
    }

    tempc.edit_rules = {
        para: {
            required: true
        },
        temp: {
            required: true,
        }
    };
    var myChart;
    $(function(){
        // 基于准备好的dom，初始化echarts实例
        myChart = echarts.init(document.getElementById('main'));
    })
    function change(){
        debugger;
        var str = $("#temp").val();
        var trs = $("#tbody tr");
        var para = [];
        trs.each(function(i){
            var p = $(this).children('td').eq(0).children("textarea").val();
            para.push(p);
            if(str){
                str = lx.replaceAll(str,'#'+i+"#",p);
            }
        });
        $("#para").val(JSON.stringify(para));
        if (str){
            // 指定图表的配置项和数据
            var option = lx.toJSON(str);
            // 使用刚指定的配置项和数据显示图表。
            myChart.clear();
            myChart.setOption(option);
        }
    }
    function add(t){
        $(t).parents("tr").after(
                    `<tr class="text-c">
                        <td><textarea class="textarea" placeholder="参数示例" style="height: 100px"></textarea></td>
                        <td class="td-manage">
                            <a onclick="javascript:add(this)"><i class="Hui-iconfont">&#xe600;</i>添加</a>
                            <a onclick="javascript:del(this)"><i class="Hui-iconfont">&#xe6a1;</i>删除</a>
                            <a onclick="javascript:down(this)"><i class="Hui-iconfont">&#xe674;</i>down</a>
                            <a onclick="javascript:up(this)"><i class="Hui-iconfont">&#xe679;</i>up</a>
                        </td>
                    </tr>`);
    }
    function del(t){
        if($("#tbody tr").length>1){
            $(t).parents("tr").remove();
        }
    }
    function down(t){
        var next = $(t).parents("tr").next("tr");
        if (next) {
            next.after($(t).parents("tr"));
        }
    }
    function up(t){
        if ($(t).parents("tr").prevAll().length>0){
            $(t).parents("tr").prev("tr").before($(t).parents("tr"));
        }
    }
}