tempc.para=
    [
        {'name':'角色名','width':'20%','val':'v.name'},
        {'name':'权限','width':'20%','val':"'**************'"}
    ];
if (window.location.href.indexOf("/edit.html")!=-1){
    /*edit 表单*/
    tempc. edit_form = `
    <form action="" method="post" class="form form-horizontal" id="form-member-add">
		<div class="row cl">
			<label class="form-label col-xs-4 col-sm-3"><span class="c-red">*</span>角色名:</label>
			<div class="formControls col-xs-8 col-sm-9">
				<input type="text" class="input-text" value="" placeholder="" id="name" name="name">
			</div>
		</div>
		<div class="row cl">
			<label class="form-label col-xs-4 col-sm-3"><span class="c-red">*</span>类型：</label>
			<div class="formControls col-xs-8 col-sm-9 skin-minimal"  onclick="anniu()">
				<div class="radio-box" id="types-1">
					<input name="type" type="radio" id="type-1" value="1"">
					<label for="type-1">所有</label>
				</div>
				<div class="radio-box"  id="types-2">
					<input type="radio" id="type-2" value="2" name="type"" checked>
					<label for="type-2">自定义</label>
				</div>
			</div>
		</div>
		<div class="col-sm-3 col-sm-offset-3">
			<div id="trees">
			    <div id="tree" class="ztree"></div>
			</div>
		</div>

		
		<div class="row cl">
			<div class="col-xs-8 col-sm-9 col-xs-offset-4 col-sm-offset-3">
				<input onclick="dj()" class="btn btn-primary radius" type="submit" value="&nbsp;&nbsp;提交&nbsp;&nbsp;">
			</div>
		</div>
		<input type='hidden' name="menus" id='menus' value =''/>
		<input type='hidden' name="btns" id='btns' value =''/>
        <input type='hidden' name="status" id='status' value ='1'/>
        <input type='hidden' name="id" id='id' value =''/>
	</form>
    `;

    function anniu(){
        if($("input[name='type']:checked").val()=='1'){
            $("#trees").hide();
        }else{
            $("#trees").show();
        }

    }
    $(function() {
        var setting = {
            check: {enable: true},
            data: {simpleData: {enable: true}}
        };
        lx.ajax('sys/list/menu?',function (nodes) {
            debugger;
            for (var v in nodes){
                var i = nodes[v];
                i.pId= i.pid;
                i.open=true;
                if(role.indexOf(i.id) != -1 || role_m.indexOf(i.address)!=-1){
                    i.checked=true;
                }
            }
            $.fn.zTree.init($("#tree"), setting, nodes);
        });
    });


    var role = "";
    var role_m="";
    tempc. edit_back = function(data){
        $("#name").val(data.name);
        $("#menus").val(data.menus);
        $("#btns").val(data.btns);
        $("#id").val(data.id);
        $("#status").val(data.status);
        if (data.type=="1"){
            $("#trees").hide();
        }else{
            $("#trees").show();
        }
        $("#type-"+data.type).attr("checked",true);
        role = data.menus;
        role_m = data.btns;
    }
    tempc. edit_rules = {
        name:{
            required:true
        },
    };
    function dj() {
        if($("input[name='type']:checked").val()=='1'){//所有权限
            $('#menus').val("#menus#");
            $('#btns').val("#btns#");
        }else{
            var treeObj = $.fn.zTree.getZTreeObj("tree"),
                nodes = treeObj.getCheckedNodes(true),
                menus = "",
                btns="";
            for (var i = 0; i < nodes.length; i++) {
                if(nodes[i].type=='3'){
                    btns += nodes[i].address + ",";
                }else{
                    menus += nodes[i].id + ",";
                }
            }
            $('#menus').val(menus);
            $('#btns').val(btns);
        }

    }



}
