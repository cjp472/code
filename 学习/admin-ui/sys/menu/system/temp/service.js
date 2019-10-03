if (window.location.href.indexOf("/list.html")!=-1){
    /*list table标题*/
    list_thead = `
    <tr class="text-c">
        <th width="25"><input type="checkbox" name="" value=""></th>
        <th width="40">key</th>
        <th width="40">类名</th>
        <th width="40">方法名</th>
        <th width="130">修改时间</th>
        <th width="70">状态</th>
        <th width="100">操作</th>
    </tr>
    `;
    /*list js*/
    function get_tr(v){
        return `<tr class="text-c">
				<td><input type="checkbox" value="${v.id}" name="test"></td>
				<td>${v.id}</td>
				<td>${v.cls}</td>
				<td>${v.method}</td>
				<td>${v.u_time}</td>
				<td class="td-status"><span class="label ${v.status==0?'label-warning':'label-success'}  radius">${v.status==0?'已停用':'已启用'}</span></td>
				<td class="td-manage">
				    <a style="text-decoration:none" href="javascript:;" onClick="${v.status==0?'member_start':'member_stop'}(this,'${v.id}')"  title="${v.status==0?'启用':'停用'}"><i class="Hui-iconfont">${v.status==0?'&#xe615;':'&#xe631;'}</i></a>
				    <a title="编辑" href="javascript:;" onclick="member_edit('编辑','${v.id}','','510')" class="ml-5" style="text-decoration:none"><i class="Hui-iconfont">&#xe6df;</i></a>
				    <a title="删除" href="javascript:;" onclick="member_del(this,'${v.id}')" class="ml-5" style="text-decoration:none"><i class="Hui-iconfont">&#xe6e2;</i></a>
				</td>
			</tr>
        `;
    };
}



/*edit js*/
else if (window.location.href.indexOf("/edit.html")!=-1){
    /*edit 表单*/
    var edit_form = `
    <form action="" method="post" class="form form-horizontal" id="form-member-add">
		<div class="row cl">
			<label class="form-label col-xs-4 col-sm-3"><span class="c-red">*</span>key:</label>
			<div class="formControls col-xs-8 col-sm-9">
				<input type="text" class="input-text" value="" placeholder="" id="id" name="id">
			</div>
		</div>
		<div class="row cl">
			<label class="form-label col-xs-4 col-sm-3"><span class="c-red">*</span>类名:</label>
			<div class="formControls col-xs-8 col-sm-9">
				<input type="text" class="input-text" value="" placeholder="" id="cls" name="cls">
			</div>
		</div>
		<div class="row cl">
			<label class="form-label col-xs-4 col-sm-3"><span class="c-red">*</span>方法名:</label>
			<div class="formControls col-xs-8 col-sm-9">
				<input type="text" class="input-text" value="" placeholder="" id="method" name="method">
			</div>
		</div>
		<div class="row cl">
			<div class="col-xs-8 col-sm-9 col-xs-offset-4 col-sm-offset-3">
				<input class="btn btn-primary radius" type="submit" value="&nbsp;&nbsp;提交&nbsp;&nbsp;">
			</div>
		</div>
        <input type='hidden' name="status" id='status' value ='1'/>
        <input type='hidden' name="old_id" id='old_id' value =''/>
	</form>
    `;

    function edit_back(data){
        $("#cls").val(data.cls);
        $("#method").val(data.method);
        $("#id").val(data.id);
        $("#old_id").val(data.id);
        $("#status").val(data.status);
    }
    var edit_rules = {
        id:{
            required:true,
            minlength:2,
            maxlength:50
        },
        cls:{
            required:true,
        },
        method:{
            required:true,
        },
    };
}
