if (window.location.href.indexOf("/list.html")!=-1){
    /*list table标题*/
    list_thead = `
    <tr class="text-c">
        <th width="25"><input type="checkbox" name="" value=""></th>
        <th width="40">ID</th>
        <th width="80">菜单名</th>
        <th width="40">父菜id</th>
        <th width="80">父菜单名</th>
        <th width="100">菜单地址</th>
        <th width="70">状态</th>
        <th width="80">操作</th>
    </tr>
    `;


    /*list js*/
    function get_tr(v){
        return `<tr class="text-c">
				<td><input type="checkbox" value="${v.id}" name="test"></td>
				<td>${v.id}</td>
				<td>${v.pid?`<u style="cursor:pointer" class="btn btn-link" onclick="member_add('添加','','510','${v.id}')">${v.name}</u>`:`${v.name}`}</td>
				<td>${v.pid}</td>
				<td><span class="label ${v.pid==0?'label-warning':'label-important'}  radius">${v.p_name}</span></td>
				<td>${v.address}</td>
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
if (window.location.href.indexOf("/edit.html")!=-1){
    /*edit 表单*/
    var edit_form = `
    <form action="" method="post" class="form form-horizontal" id="form-member-add">
		<div class="row cl">
			<label class="form-label col-xs-4 col-sm-3"><span class="c-red">*</span>菜单名：</label>
			<div class="formControls col-xs-8 col-sm-9">
				<input type="text" class="input-text" value="" placeholder="" id="name" name="name">
			</div>
		</div>
		<div class="row cl">
			<label class="form-label col-xs-4 col-sm-3"><span class="c-red">*</span>地址：</label>
			<div class="formControls col-xs-8 col-sm-9">
				<input type="text" class="input-text" value="0" placeholder="" id="address" name="address" readonly>
			</div>
		</div>
		
		<div class="row cl">
			<label class="form-label col-xs-4 col-sm-3"><span class="c-red">*</span>父菜单名：</label>
			<div class="formControls col-xs-8 col-sm-9">
				<input type="text" class="input-text" value="一级菜单" placeholder="" id="p_name" name="p_name" readonly>
			</div>
		</div>
		<div class="row cl">
			<div class="col-xs-8 col-sm-9 col-xs-offset-4 col-sm-offset-3">
				<input class="btn btn-primary radius" type="submit" value="&nbsp;&nbsp;提交&nbsp;&nbsp;">
			</div>
		</div>
        <input type='hidden' name="status" id='status' value ='1'/>
        <input type='hidden' name="id" id='id' value =''/>
        <input type='hidden' name="pid" id='pid' value ='0'/>
	</form>
    `;
    function edit_back(data){
        $("#name").val(data.name);
        $("#p_name").val(data.p_name);
        $("#address").val(data.address);
        $("#type-"+data.type).attr("checked",true);
        $("#id").val(data.id);
        $("#pid").val(data.pid);
        $("#status").val(data.status);
        if (data.pid != '0'){
            $("#address").removeAttr('readonly');
        }
    }

    var edit_rules = {
        name:{
            required:true,
            minlength:2,
            maxlength:16
        },
        address:{
            required:true,
        },
    };
    /*新增子菜单时使用*/
    $(function () {
        var pid = lx.request("pid");
        if (pid){
            lx.ajax('sys/obj/'+menu+'?id='+pid,edit_back_p);
        }
    })
    function edit_back_p(data){
        $("#p_name").val(data.name);
        $("#pid").val(data.id);
        $("#address").removeAttr('readonly');
    }
}
