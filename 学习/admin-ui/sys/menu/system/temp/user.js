if (window.location.href.indexOf("/list.html")!=-1){
    /*list table标题*/
    list_thead = `
    <tr class="text-c">
        <th width="25"><input type="checkbox" name="" value=""></th>
        <th width="100">用户名</th>
        <th width="40">性别</th>
        <th width="40">科室</th>
        <th width="40">角色</th>
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
				<td>${v.sex==1?'男':'女'}</td>
				<td>${v.deptName}</td>
				<td>${v.roleName}</td>
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
			<label class="form-label col-xs-4 col-sm-3"><span class="c-red">*</span>用户名：</label>
			<div class="formControls col-xs-8 col-sm-9">
				<input type="text" class="input-text" value="" placeholder="" id="id" name="id">
			</div>
		</div>
		<div class="row cl">
			<label class="form-label col-xs-4 col-sm-3"><span class="c-red">*</span>性别：</label>
			<div class="formControls col-xs-8 col-sm-9 skin-minimal">
				<div class="radio-box">
					<input name="sex" type="radio" id="sex-1" value="1" checked>
					<label for="sex-1">男</label>
				</div>
				<div class="radio-box">
					<input type="radio" id="sex-2" value="2" name="sex">
					<label for="sex-2">女</label>
				</div>
			</div>
		</div>
		<div class="row cl">
			<label class="form-label col-xs-4 col-sm-3"><span class="c-red">*</span>科室：</label>
			<div class="formControls col-xs-8 col-sm-9"> <span class="select-box">
				<select name="dept" class="select" id="depts"></select>
				</span> </div>
		</div>
		<div class="row cl">
			<label class="form-label col-xs-4 col-sm-3"><span class="c-red">*</span>角色：</label>
			<div class="formControls col-xs-8 col-sm-9"> <span class="select-box">
				<select name="role" class="select" id="roles"></select>
				</span> </div>
		</div>
		<div class="row cl">
			<label class="form-label col-xs-4 col-sm-3"><span class="c-red">*</span>密码：</label>
			<div class="formControls col-xs-8 col-sm-9">
				<input type="password" class="input-text" value="" placeholder="" id="password" name="password">
			</div>
		</div>
		<div class="row cl">
			<div class="col-xs-8 col-sm-9 col-xs-offset-4 col-sm-offset-3">
				<input class="btn btn-primary radius" type="submit" value="&nbsp;&nbsp;提交&nbsp;&nbsp;">
			</div>
		</div>
        <input type='hidden' name="status" id='status' value ='1'/>
        <input type='hidden' name="deptName" id='deptName' value =''/>
        <input type='hidden' name="roleName" id='roleName' value =''/>
        <input type='hidden' name="old_id" id='old_id' value =''/>
	</form>
    `;
    var user = "";
    function edit_back(data){
        $("#id").val(data.id);
        $("#old_id").val(data.id);
        $("#password").val(data.password);
        $("#sex-"+data.sex).attr("checked",true);
        $("#status").val(data.status);
        user = data;
    }

    var edit_rules = {
        id:{
            required:true,
            minlength:2,
            maxlength:16
        },
        sex:{
            required:true,
        },
        password:{
            required:true,
        },
    };
    $(function () {
        lx.ajax('sys/list/dept?',function (data) {
            for (var i in data) {
                var m = data[i];
                if (i == 0 ||m.id == user.dept){
                    $("#deptName").val(m.name);
                }
                $("#depts").append(`<option value="${m.id}" ${m.id == user.dept?'selected':''}>${m.name}</option>`);
            }
        });

        $('#depts').change(function() {
            $("#deptName").val( $(this).children('option:selected').html());
        });

        lx.ajax('sys/list/role?',function (data) {
            for (var i in data) {
                var m = data[i];
                if (i == 0 ||m.id == user.role){
                    $("#roleName").val(m.name);
                }
                $("#roles").append(`<option value="${m.id}" ${m.id == user.role?'selected':''}>${m.name}</option>`);
            }
        });

        $('#roles').change(function() {
            $("#roleName").val( $(this).children('option:selected').html());
        });
    });
}
