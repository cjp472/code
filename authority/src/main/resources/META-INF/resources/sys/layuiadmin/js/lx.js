/**
 基本工具
 */
var lx = {
    /* 服务器根 */
    wwwroot: ''
    /* 回到顶部  */
    ,returntop:function(){
        $("html,body").animate({scrollTop:0},1000);
    }
    ,/* json to string */
    O2String: function(O) {
        try {
            var j = JSON.stringify(O);
            return j;
        } catch (e) {
            var S = [];
            var J = "";
            if (Object.prototype.toString.apply(O) === '[object Array]') {
                for (var i = 0; i < O.length; i++)
                    S.push(this.O2String(O[i]));
                J = '[' + S.join(',') + ']';
            } else if (Object.prototype.toString.apply(O) === '[object Date]') {
                J = "new Date(" + O.getTime() + ")";
            } else if (Object.prototype.toString.apply(O) === '[object RegExp]' || Object.prototype.toString.apply(O) === '[object Function]') {
                J = O.toString();
            } else if (Object.prototype.toString.apply(O) === '[object Object]') {
                for (var i in O) {
                    O[i] = typeof(O[i]) == 'string' ? '"' + O[i] + '"' : (typeof(O[i]) === 'object' ? this.O2String(O[i]) : O[i]);
                    S.push(i + ':' + O[i]);
                }
                J = '{' + S.join(',') + '}';
            }
            return J;
        }
    },
    ajax: function(url, funcallback,data) {
        var that = this;
        var newurl = this.wwwroot + url;
        if(newurl.indexOf("?") >= 0) {
            newurl += "&token=" + $.cookie('token');
        }else {
            newurl += "?token=" + $.cookie('token');
        }
        $.ajax(newurl, {
                async: false,
                cache: false,
                dataType: "json",
                data: data,
                type: 'POST',
                processData: true,
                success: function(data) {
                    if(that.login(data)){
                        funcallback(data);
                    }
                }
            });
    }
    ,login_open:false
    ,login:function(data){
        if (data.success == '0'){//报错了
            layer.alert(data.msg,{icon:5})
            return false;
        } else if (data.success != '9') {//登录失效了
            return true;
        }
        var that = this;
        if (!that.login_open){
            that.login_open = true;
            layer.prompt({title: '登录',area: ['250px', '240px'], formType: 0}, function(name, index){
                lx.ajax('/login?loginname='+name+'&password='+$("#layui-layer-content_pass").val()+'&code='+$("#layui-layer-content_code").val(),function(data) {
                    if ("success" == data.result) {
                        $.cookie('token',data.token);
                    }
                    location.replace(location.href);
                });
                that.login_open = false;
                layer.close(index);
            });
            $(".layui-layer-content").append(`
            <input type="password" id="layui-layer-content_pass" class="layui-layer-input" placeholder="密码" value="" style="margin-top: 6px;">
            <p style="margin: 6px 0 0 12.5px">
                <input type="text" id="layui-layer-content_code" style="border: 1px solid #e6e6e6; padding-left: 10px;width:44%;height:36px;" placeholder="验证码" value="0000">
                <img src="${lx.wwwroot+"/login/code"}" onclick="changeCode()" id="codeImg" style="width:44%;height:37px;margin-letf: 6px;">
            </p>
            <script>
                function changeCode() {
                    $("#codeImg").attr("src", lx.wwwroot+"/login/code?t=" + new Date().getTime());
                }
            </script>
            `);
        }
        return false;
    },
    sys_service: function(data, funcallback) {
        var newurl = this.wwwroot + 'sys/service';
        this.ajax(newurl,funcallback,data);
    }
    /* 分析地址参数等 */
    ,request: function(paras) {
        var url = location.href;
        var paraString = url.substring(url.indexOf("?") + 1, url.length).split("&");
        var paraObj = {}
        for (i = 0; j = paraString[i]; i++) {
            paraObj[j.substring(0, j.indexOf("=")).toLowerCase()] = decodeURIComponent(j.substring(j.indexOf("=") + 1, j.length));
        }
        var returnValue = paraObj[paras.toLowerCase()];
        if (typeof(returnValue) == "undefined") {
            return "";
        } else {
            return returnValue;
        }
    },isFunc: function (funcName) {
        try {
            if (typeof(eval(funcName))=="function") {
                return true;
            }
        } catch (e) {
        }
        return false;
    },toJSON:function(str){
        return eval('(' + str + ')');
    },replaceAll: function (str,FindText, RepText) {
        var regExp = new RegExp(FindText, "g");
        return str.replace(regExp, RepText);
    }

};
//用来更新视图
let View = {
    //将html 设置到指定位置并执行script
    setHTMLWithScript: function(container, rawHTML){//设置html
        container.innerHTML = rawHTML;
        const scripts = container.querySelectorAll('script');
        return Array.prototype.slice.apply(scripts).reduce((chain, script) => {
                return chain.then(() => this.runScript(script));
    }, Promise.resolve());
    }
    ,runScript: function (script){
        return new Promise((reslove, rejected) => {
                const newScript = document.createElement('script');// 直接 document.head.appendChild(script) 是不会生效的，需要重新创建一个
        newScript.innerHTML = script.innerHTML;// 获取 inline script
        const src = script.getAttribute('src'); // 存在 src 属性的话
        if (src) newScript.setAttribute('src', src);
        newScript.onload = () => reslove();// script 加载完成和错误处理
        newScript.onerror = err => rejected();
        document.head.appendChild(newScript);
        document.head.removeChild(newScript);
        if (!src) {
            reslove();// 如果是 inline script 执行是同步的
        }
    });
    }
    ,getHtml:function (option){
        if(!option){alert("页面不存在!");}
        if(option.content){
            return option.content;
        }else {
            var newurl = option.url;
            if(newurl.indexOf("?") >= 0) {
                newurl += "&t=" + new Date().getTime();
            }else {
                newurl += "?t=" + new Date().getTime();
            }
            var htmlobj= $.ajax({url:newurl,async:false});
            var dataString = htmlobj.responseText;
            if(option.cache){
                option.content = dataString;
            }
            return dataString;
        }
    }
}
//获取当前网址，如： http://localhost:8083/uimcardprj/share/meun.jsp
var curWwwPath = window.document.location.href;
//获取主机地址之后的目录，如： uimcardprj/share/meun.jsp
var pathName = window.document.location.pathname;
if("/" == pathName){
    lx.wwwroot = window.document.location.href;
}else {
    var pos = curWwwPath.indexOf(pathName);
//获取主机地址，如： http://localhost:8083
    var localhostPaht = curWwwPath.substring(0, pos);
//获取带"/"的项目名，如：/uimcardprj
    var projectName = pathName.substring(0, pathName.substr(1).indexOf('/') + 1);
    lx.wwwroot = (localhostPaht)+"/";
}
var layui_fun={
    arrayToTree: function(treeArr, pid,func) {
        let temp = [];
        if(treeArr){
            treeArr.forEach((item, index) => {
                if (item.pid == pid) {
                    if(func){
                        func(item);
                    }
                    var ls = layui_fun.arrayToTree(treeArr, item.id,func);
                    if (ls.length > 0) {
                        item.children = ls;
                    }
                    temp.push(item);
                }
            });
        }
        return temp;
    }
}
//日期
if($("#start_end").size()>0){
    layui.laydate.render({
        elem: '#start_end'
        ,range: true
    });
}
//日期
if($("#s_datatime").size()>0){
    layui.laydate.render({
        elem: '#s_datatime'
        ,type: 'datetime'
    });
}
//日期
if($("#n_datatime").size()>0){
    layui.laydate.render({
        elem: '#n_datatime'
        ,type:'datetime'
        ,ready: function(date){
            $(".layui-laydate-footer [lay-type='datetime'].laydate-btns-time").click();
            $(".laydate-main-list-1 .layui-laydate-content li ol li:last-child").click();
            $(".layui-laydate-footer [lay-type='date'].laydate-btns-time").click();
        }
    });
}
if($("#start_end_time").size()>0){
    layui.laydate.render({
        elem: '#start_end_time'
        ,type: 'datetime'
        ,range: true
        ,ready: function(date){
            $(".layui-laydate-footer [lay-type='datetime'].laydate-btns-time").click();
            $(".laydate-main-list-1 .layui-laydate-content li ol li:last-child").click();
            $(".layui-laydate-footer [lay-type='date'].laydate-btns-time").click();
        }
    });
}

//对表格的封装
if($("#table_temp_id").size()>0){
    var temp_fun = {
        //密码框
        password_fun:function (d) {
            return `******`;
        }
        //单选
        ,checkbox_fun:function (d) {
            return `<input type="checkbox" name="${this.field}" lay-filter="checkbox" lay-skin="switch" lay-text="${this.lay_text}" ${d[this.field] == 1 ? ' checked' : ''}>`;
        }
        //日期 format
        ,date_fun:function (d) {
            return ` <input type="text" name="${this.field}" format ="${this.format}"  class="layui-input layui-input-date" value="${layui.util.toDateString(d[this.field],this.format)}" >`
        }
        //下拉选 data
        ,select_fun:function (d){
            var options = "";
            for(var i in this.data){
                var v = this.data[i];
                options +=`<option value="${v.id}" ${d[this.field] == v.id ? ' selected="selected"' : ''} >${v.name}</option>`;
            }
            return '<a lay-event="type"></a><select name="'+this.field+'" lay-filter="select" lay-search><option value="">请选择</option>' + options + '</select>';
        }
        //联表操作 data
        ,link_func:function (d) {
            for(var i in this.data){
                var v = this.data[i];
                if (d[this.field] == v.k){
                    return  v.v;
                }
            }
            return '未知';
        }
        ,sex_func:function (d) {
            return d[this.field]==1?`<button class="layui-btn layui-btn-xs">${this.t1}</button>`: `<button class="layui-btn layui-btn-primary layui-btn-xs" lay-event="add_p">${this.t2}</button>`;
        }
    }
    window.dataObj = {
        //表格
        table_temp:{
            elem:"#table_temp_id"
            ,url:''
            ,parseData: function(res){ //res 即为原始返回的数据
                if (lx.login(res)) {
                    res.rows = dataObj.rows_filter(res.rows);//执行过滤
                    var lv = $("#table_temp_search_id").val();//获取搜索框
                    if (lv){
                        res.rows = res.rows.filter(function (v) {
                            return JSON.stringify(v).indexOf(lv) != -1;
                        });
                    }
                    return res;
                }
            }
            ,response: {
                statusName: 'success' //规定数据状态的字段名称，默认：code
                ,statusCode: 1 //规定成功的状态码，默认：0
                ,msgName: 'msg' //规定状态信息的字段名称，默认：msg
                ,countName: 'count' //规定数据总数的字段名称，默认：count
                ,dataName: 'rows' //规定数据列表的字段名称，默认：data
            }
            ,toolbar:true
            ,cols:[[]]
            ,page:!0
            ,limit:10
            ,limits:[10,15,20,25,30,50,100,200,500,1000,10000]
            ,text:"对不起，加载出现异常！"
        }
        ,rows_filter:function (rows) { return rows; }
        ,search:function () {
            layui.table.reload('table_temp_id');
        }
        ,id:'id'
        ,del:function (data) {
            layer.msg('调用删除');
        }
        ,delAll:function (checkData) {
            layer.msg('调用删除全部');
        }
        ,edit:function (data) {
            layer.msg('调用修改');
        }
        ,edit_area:['550px', '550px']
        ,add_content:''
        ,add_area:['550px', '550px']
        ,del_success:function () {
            layui.table.reload('table_temp_id');
            layer.msg('已执行');
        }
    }
    layui.config({
        base: '/sys/layuiadmin/' //静态资源所在路径
    }).extend({
        index: 'lib/index' //主入口模块
    }).use(['index', 'contlist', 'table'], function(){
        var table = layui.table,form = layui.form;

        if(dataObj.table_temp.url.indexOf("?") >= 0) {
            dataObj.table_temp.url += "&token=" + $.cookie('token');
        }else {
            dataObj.table_temp.url += "?token=" + $.cookie('token');
        }
        table.render(dataObj.table_temp);
        //监听搜索
        form.on('submit(table_temp_id)', function(data){
            if (dataObj.table_temp.page){
                table.reload('table_temp_id', {page:{curr:1},where: data.field});
            }else{
                table.reload('table_temp_id', {where: data.field});
            }
        });
        //监听工具条 和单元格
        table.on('tool(table_temp_id)', function(obj) {
            var data = obj.data;
            if (obj.event === 'del') {
                layer.confirm('确认删除?', function (index) { dataObj.del(data); });
            } else {
                if(dataObj[obj.event+"_func"]){
                    dataObj[obj.event+"_func"](data);
                }else{
                    layer.open({
                        type: 2
                        ,title: '修改'
                        ,content: dataObj[obj.event](data)
                        ,maxmin: true
                        ,area: dataObj[obj.event+'_area']
                        ,btn: ['确定', '取消']
                        ,yes: function(index, layero){
                            //点击确认触发 iframe 内容中的按钮提交
                            var submit = layero.find('iframe').contents().find("#layuiadmin-app-form-submit");
                            submit.click();
                        }
                    });
                }
            }
        });
        var active = {
            batchdel: function(){
                var checkStatus = table.checkStatus('table_temp_id')
                    ,checkData = checkStatus.data; //得到选中的数据
                if(checkData.length === 0){
                    return layer.msg('请选择数据');
                }
                dataObj.delAll(checkData);
            },
            add: function(){
                layer.open({
                    type: 2
                    ,title: '添加'
                    ,content: dataObj.add_content
                    ,maxmin: true
                    ,area: dataObj.add_area
                    ,btn: ['确定', '取消']
                    ,yes: function(index, layero){
                        //点击确认触发 iframe 内容中的按钮提交
                        var submit = layero.find('iframe').contents().find("#layuiadmin-app-form-submit");
                        submit.click();
                    }
                });
            }
        };

        $('.layui-btn.layuiadmin-btn-list').on('click', function(){
            var type = $(this).data('type');
            active[type] ? active[type].call(this) : '';
        });
    });
}

