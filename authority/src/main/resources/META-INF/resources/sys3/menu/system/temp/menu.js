if (window.location.href.indexOf("/list.html")!=-1){
    tempc.ls_thead = function(){
        var tr = `
            <tr class="text-c">
                <th width="3%"></th>
                <th width="5%">ID</th>
                <th width="10%">菜单名</th>
                <th width="10%">类型</th>
                <th width="5%">父菜id</th>
                <th width="10%">父菜单名</th>
                <th width="30%">菜单地址</th>
                <th width="10%">状态</th>
                <th width="20%">操作</th>
            </tr>
            `;
        return tr;
    }
    /*list js*/
    tempc. get_tr = function(v){
        return `<tr class="text-c pid_${v.pid}" attr_pid="${v.pid}" id="${v.id}" ${v.pid==0?``:`style="display: none"`} onclick="showAndHide(this)">
				<td><i  class="Hui-iconfont">&#xe600;</i></td>
				<td>${v.id}</td>
				<td>${v.type!=3?`<u style="cursor:pointer" class="btn btn-link" onclick="member_add('添加','','510','${v.id}')">${v.name}</u>`:`${v.name}`}</td>
                <td>${v.type==3?`<i class="btn btn-warning radius size-S">按钮</i>`: v.type==2?`<i class="btn btn-success radius size-S">菜单</i>`:`<i class="btn btn-primary radius size-S">目录</i>`}</td>
				<td>${v.pid}</td>
				<td>${v.c}</td>
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
    tempc.showDataTable = false;
    function showAndHide(e){
        var ls = $('.pid_'+ e.id);//获取子类
        var pid = $("#"+e.id).attr("attr_pid");//获取pid
        if (ls.length == 0){
            return;
        }
        var show = false;//是否需要显示
        show = $("#"+e.id).css("display")!="none" && ls.eq(0).css("display")=="none";//自己是开的 且子关闭 时去展开
        if (show){
            $("#"+e.id+">td:first>i").html("&#xe6a1;");
            ls.each(function (){//展开子
               $(this).show();
            });
            ls = $(".pid_"+pid);//获取所有pid一样的关闭
            ls.each(function (){
                $(this).hide();
            });
            $("#"+e.id).show();//自己显示
        }else{
            $("#"+e.id+">td:first>i").html("&#xe600;");
            ls.each(function(){//关闭子类
                $(this).hide();
                $(this).trigger("click"); //关闭子类
            });
            if($("#"+e.id).css("display")!="none"){//当前是展开的
                ls = $(".pid_"+pid);//开启当前所有pid 一样的
                ls.each(function (){
                    $(this).show();
                });
            }
        }
    }

}



/*edit js*/
if (window.location.href.indexOf("/edit.html")!=-1){
    /*edit 表单*/
    tempc. edit_form = `
    <form action="" method="post" class="form form-horizontal" id="form-member-add">
		<div class="row cl">
			<label class="form-label col-xs-4 col-sm-3"><span class="c-red">*</span>名称：</label>
			<div class="formControls col-xs-8 col-sm-9">
				<input type="text" class="input-text" value="" placeholder="" id="name" name="name">
			</div>
		</div>
		<div class="row cl">
			<label class="form-label col-xs-4 col-sm-3"><span class="c-red">*</span>类型：</label>
			<div class="formControls col-xs-8 col-sm-9 skin-minimal">
				<div class="radio-box" onclick="ml()" id="types-1">
					<input name="type" type="radio" id="type-1" value="1">
					<label for="type-1">目录</label>
				</div>
				<div class="radio-box" onclick="cd()" id="types-2">
					<input type="radio" id="type-2" value="2" name="type" checked>
					<label for="type-2">菜单</label>
				</div>
				<div class="radio-box" onclick="an()" id="types-3">
					<input type="radio" id="type-3" value="3" name="type">
					<label for="type-3">按钮</label>
				</div>
			</div>
		</div>
		<div class="row cl">
			<label class="form-label col-xs-4 col-sm-3"><span class="c-red">*</span>地址：</label>
			<div class="formControls col-xs-8 col-sm-9" id="dz">
				<input type="text" class="input-text" value="0" placeholder="" id="address" name="address" readonly>
			</div>
		</div>
		<div class="row cl">
			<label class="form-label col-xs-4 col-sm-3"><span class="c-red">*</span>排序：</label>
			<div class="formControls col-xs-8 col-sm-9">
				<input type="text" class="input-text" value="1" placeholder="排序" id="order" name="order">
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
    tempc. edit_back =function(data){
        $("#name").val(data.name);
        $("#p_name").val(data.p_name);
        $("#address").val(data.address);
        if(data.type == "1"){//菜单
            $("#types-2").hide();
            $("#types-3").hide();
            $("#type-1").trigger("click")
        }else if(data.type == '2'){
            $("#types-1").hide();
            $("#types-3").hide();
            $("#type-2").trigger("click")
        }else{
            $("#types-1").hide();
            $("#types-2").hide();
            $("#type-3").trigger("click")
        }
        $("#id").val(data.id);
        $("#pid").val(data.pid);
        $("#status").val(data.status);
        if (data.pid != '0'){
            $("#address").removeAttr('readonly');
        }
        address=data.address;
    }

    function ml(){
        $("#address").attr('readonly',true);
    }
    function cd(){
        $("#address").removeAttr('readonly');
    }
    function an(){
        $("#address").removeAttr('readonly');
    }
    tempc. edit_rules = {
        name:{
            required:true
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
        if(data.type == "1"){
            $("#types-3").hide();
        }else if(data.type = '2'){
            $("#types-1").hide();
            $("#types-2").hide();
            $("#type-3").trigger("click")
        }
    }
}
