tempc.para=
[
    {'name':'用户名','type':'id','width':'10%','val':'v.id'},
    {'name':'性别','type':'sex','width':'10%','val':"v.sex==1?'男':'女'"},
    {'name':'角色','type':'roleName','width':'20%','val':'v.roleName'}
];
/*edit js*/
if (window.location.href.indexOf("/edit.html")!=-1) {
    tempc.edit_form = `
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
			<label class="form-label col-xs-4 col-sm-3"><span class="c-red">*</span>角色：</label>
			<div class="formControls col-xs-8 col-sm-9">
			    <span class="select-box"><select name="role" class="select" id="roles"></select></span>
            </div>
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
        <input type='hidden' name="roleName" id='roleName' value =''/>
        <input type='hidden' name="old_id" id='old_id' value =''/>
	</form>
    `;

    var user = "";
    tempc.edit_back = function (data) {
        $("#id").val(data.id);
        $("#old_id").val(data.id);
        $("#password").val(data.password);
        $("#sex-" + data.sex).attr("checked", true);
        $("#status").val(data.status);
        user = data;
    }

    tempc.edit_rules = {
        id: {
            required: true,
            minlength: 2,
            maxlength: 16
        },
        sex: {
            required: true,
        },
        password: {
            required: true,
        },roles:{
            required:true
        }
    };
    $(function () {
        lx.ajax('sys/list/role?', function (data) {
            for (var i in data) {
                var m = data[i];
                if (i == 0 || m.id == user.role) {
                    $("#roleName").val(m.name);
                }
                $("#roles").append(`<option value="${m.id}" ${m.id == user.role ? 'selected' : ''}>${m.name}</option>`);
            }
        });

        $('#roles').change(function () {
            $("#roleName").val($(this).children('option:selected').html());
        });
    });
}