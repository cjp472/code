//参数为需要展示的列信息 name中文名 width:宽度 val 表达式
function Temp(){}
Temp.prototype.para=[];
//list 子类需要导入的css,js 代码块
Temp.prototype.ls_css="";
Temp.prototype.ls_js="";
//是否显示dataTable
Temp.prototype.showDataTable = true;
//list 实现head代码块
Temp.prototype.ls_thead = function(){
    var tds = ``;
    for (var i in this.para){
        var v = this.para[i];
        tds+=`<th width="${v.width? v.width:40}">${v.name}</th>`;
    }
    var tr=`<tr class="text-c">
        <th width="5%"><input type="checkbox" name="" value=""></th>`
        +tds+
        `<th width="12%">修改时间</th>
        <th width="4%">状态</th>
        <th width="8%">操作</th>
    </tr>`;
   return tr;

}

//list 遍历生成tr之前
Temp.prototype.showTrPost=function (data){};
//list 实现tr代码块 传入遍历的数据
Temp.prototype.get_tr=function (v){
    var tds = ``;
    for (var i in this.para){
        var vr = this.para[i];
        tds+=`<td>${eval(vr.val)}</td>`;
    }
    var tr = `<tr class="text-c">
				<td><input type="checkbox" value="${v.id}" name="test"></td>`
        +tds+
        `<td>${v.u_time}</td>
				<td class="td-status"><span class="label ${v.status==0?'label-warning':'label-success'}  radius">${v.status==0?'已停用':'已启用'}</span></td>
				<td class="td-manage">
				    <a style="text-decoration:none" href="javascript:;" onClick="${v.status==0?'member_start':'member_stop'}(this,'${v.id}')"  title="${v.status==0?'启用':'停用'}"><i class="Hui-iconfont">${v.status==0?'&#xe615;':'&#xe631;'}</i></a>
				    <a title="编辑" href="javascript:;" onclick="member_edit('编辑','${v.id}','','510')" class="ml-5" style="text-decoration:none"><i class="Hui-iconfont">&#xe6df;</i></a>
				    <a title="删除" href="javascript:;" onclick="member_del(this,'${v.id}')" class="ml-5" style="text-decoration:none"><i class="Hui-iconfont">&#xe6e2;</i></a>
				</td>
			</tr>`;
    return tr;
};

//edit 子类需要导入的css,js 代码块
Temp.prototype.ed_css="";
Temp.prototype.ed_js="";
//edit 表单部分
Temp.prototype.edit_form="";
//修改时 获取当前id的信息
Temp.prototype.edit_back=function(data){};
//表单验证
Temp.prototype.edit_rules={};

function Tempc(){};
Tempc.prototype = new Temp();
var tempc = new Tempc();


