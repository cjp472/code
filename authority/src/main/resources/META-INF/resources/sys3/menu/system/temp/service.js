tempc.para=
    [
        {'name':'key','width':'20%','val':'v.id'},
        {'name':'类名','width':'20%','val':"v.cls"},
        {'name':'方法名','width':'20%','val':"v.method"},
        {'name':'备注','width':'10%','val':'v.methodName'}
    ];

/*edit js*/
if (window.location.href.indexOf("/edit.html")!=-1){
    /*edit 表单*/
    tempc.edit_form = `
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
			<label class="form-label col-xs-4 col-sm-3"><span class="c-red">*</span>备注:</label>
			<div class="formControls col-xs-8 col-sm-9">
				<input type="text" class="input-text" value="" placeholder="" id="methodName" name="methodName">
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

    tempc.edit_back= function(data){
        $("#cls").val(data.cls);
        $("#method").val(data.method);
        $("#id").val(data.id);
        $("#old_id").val(data.id);
        $("#status").val(data.status);
        $("#methodName").val(data.methodName);
    }
    tempc.edit_rules = {
        id:{
            required:true
        },
        cls:{
            required:true,
        },
        method:{
            required:true,
        },methodName:{
            required:true,
        }
    };
}
