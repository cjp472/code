if (window.location.href.indexOf("/list.html")!=-1){
    /*list table标题*/
    list_thead = `
    <tr class="text-c">
        <th width="25"><input type="checkbox" name="" value=""></th>
        <th width="100">科室名</th>
        <th width="40">权限</th>
        <th width="130">修改时间</th>
        <th width="70">状态</th>
        <th width="100">操作</th>
    </tr>
    `;
    /*list js*/
    function get_tr(v){
        return `<tr class="text-c">
				<td><input type="checkbox" value="${v.id}" name="test"></td>
				<td>${v.name}</td>
				<td>**************</td>
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
			<label class="form-label col-xs-4 col-sm-3"><span class="c-red">*</span>科室名:</label>
			<div class="formControls col-xs-8 col-sm-9">
				<input type="text" class="input-text" value="" placeholder="" id="name" name="name">
			</div>
		</div>
		<div class="row cl">
			<label class="form-label col-xs-4 col-sm-3">权限角色：</label>
			<div class="formControls col-xs-8 col-sm-9">
				<dl class="permission-list">
					<dt>
						<label><input type="checkbox" value="" name="user-Character-0" id="checkAll" onclick="checkAlls()">全部选中</label>
					</dt>
					<dd id="menu">
					</dd>
				</dl>
			</div>
		</div>
		
		<div class="row cl">
			<div class="col-xs-8 col-sm-9 col-xs-offset-4 col-sm-offset-3">
				<input onclick="dj()" class="btn btn-primary radius" type="submit" value="&nbsp;&nbsp;提交&nbsp;&nbsp;">
			</div>
		</div>
		<input type='hidden' name="role" id='role' value =''/>
        <input type='hidden' name="status" id='status' value ='1'/>
        <input type='hidden' name="id" id='id' value =''/>
	</form>
    `;
    var role = "";
    function edit_back(data){
        $("#name").val(data.name);
        $("#role").val(data.role);
        $("#id").val(data.id);
        $("#status").val(data.status);
        role = data.role;
    }
    var edit_rules = {
        name:{
            required:true,
            minlength:2,
            maxlength:16
        },
    };
    //提交时给权限赋值
    function dj() {
        var strSel="";
        //获取所有勾选的url
        var choice=document.getElementsByName("choice");
        for(var i=0;i<choice.length;i++){
            if (choice[i].checked){
                if (strSel.length==0){
                    strSel += $(choice[i]).val();
                }else{
                    strSel += ","+$(choice[i]).val();
                }
                //获取增删改查权限
                var bt =document.getElementsByClassName("bid_"+$(choice[i]).val());
                for(var j=0;j<bt.length;j++){
                    if (bt[j].checked){
                        strSel += $(bt[j]).val();
                    }
                }
            }
        }
        $('#role').val(strSel);
    }

    $(function () {
        lx.ajax('sys/list/menu?',function (data) {
            for (var i in data){
                var m = data[i];
                if (m.pid == '0'){
                    var p = `
                    <dl class="cl permission-list2">
							<dt>
								<label class=""><input ${role.indexOf(m.id)!=-1?'checked':''} onclick="setC('cid_${m.id}')" id="cid_${m.id}" type="checkbox" class="checknum" value="${m.id}" name="choice">${m.name}</label>
							</dt>
							<dd id="id_${m.id}">
							</dd>
						</dl>
                `;
                    $('#menu').append(p);
                }else{
                    var c = `<label class="btn_id" value="${m.id}"><input ${role.indexOf(m.id)!=-1?'checked':''} onclick="setP('cid_${m.pid}')" type="checkbox" class="checknum  cid_${m.pid}" value="${m.id}" name="choice">
                               ${m.name}
                               <span style="display: none; border:1px solid #ddd;" id="bid_${m.id}">
                                    <label><input ${role!=''&&role.indexOf(m.id)!=-1&&(role.substring(role.indexOf(m.id)+5).indexOf('a')==-1||role.substring(role.indexOf(m.id)+5).indexOf('a')>4)?'':'checked'}  type="checkbox" class="bid_${m.id}" value="a">增</label>
                                    <label><input ${role!=''&&role.indexOf(m.id)!=-1&&(role.substring(role.indexOf(m.id)+5).indexOf('b')==-1||role.substring(role.indexOf(m.id)+5).indexOf('b')>4)?'':'checked'}  type="checkbox" class="bid_${m.id}" value="b">删</label>
                                    <label><input ${role!=''&&role.indexOf(m.id)!=-1&&(role.substring(role.indexOf(m.id)+5).indexOf('c')==-1||role.substring(role.indexOf(m.id)+5).indexOf('c')>4)?'':'checked'}  type="checkbox" class="bid_${m.id}" value="c">改</label>
                                    <label><input ${role!=''&&role.indexOf(m.id)!=-1&&(role.substring(role.indexOf(m.id)+5).indexOf('d')==-1||role.substring(role.indexOf(m.id)+5).indexOf('d')>4)?'':'checked'}  type="checkbox" class="bid_${m.id}" value="d">查</label>
                                </span>
                        </label>`;
                    $('#id_'+m.pid).append(c);
                }
            }
        });
        //显示按钮权限
        $(".btn_id").hover(function () {
            $("#bid_"+$(this).attr('value')).show();
        },function () {
            $("#bid_"+$(this).attr('value')).hide();
        });
    });


}
