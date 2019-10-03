
/**/
function getRootPath_web() {
    //获取当前网址，如： http://localhost:8083/uimcardprj/share/meun.jsp
    var curWwwPath = window.document.location.href;
    //获取主机地址之后的目录，如： uimcardprj/share/meun.jsp
    var pathName = window.document.location.pathname;
    var pos = curWwwPath.indexOf(pathName);
    //获取主机地址，如： http://localhost:8083
    var localhostPaht = curWwwPath.substring(0, pos);
    //获取带"/"的项目名，如：/uimcardprj
    var projectName = pathName.substring(0, pathName.substr(1).indexOf('/') + 1);
    return (localhostPaht)+"/";
}
document.write(`
<script language=javascript src='/sys/js/layer.js'></script>
<script type="text/javascript" src="/sys/js/H-ui.min.js"></script>
<script type="text/javascript" src="/sys/js/H-ui.admin.js"></script>
`);

/**
 基本工具
 */
var lx = {
    /* 服务器根 */
    wwwroot: getRootPath_web()
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
        if(url.indexOf("?") >= 0) {
            newurl += "&t=" + new Date().getTime();
        }else {
            newurl += "?t=" + new Date().getTime();
        }
        // newurl+="&token="+ $.cookie('token');
        $.ajax(newurl, {
                async: false,
                cache: false,
                dataType: "json",
                data: data,
                type: 'POST',
                processData: true,
                success: function(data) {
                    if (data.success == '0'){
                        layer.alert(data.msg,{icon:5})
                    } else if (data.success == '9') {
                        that.login();
                    }else{
                        funcallback(data);
                    }
                }
            });
    },login:function(){
        layer.prompt({title: '输入用户名，并确认',area: ['250px', '240px'], formType: 0}, function(name, index){
            lx.ajax('/login?loginname='+name+'&password='+$("#layui-layer-content_pass").val()+'&code='+$("#layui-layer-content_code").val(),function(data) {
                if ("success" == data.result) {
                    $.cookie('token',data.token);
                }
                location.replace(location.href);
            });
            layer.close(index);
        });
        $(".layui-layer-content").append(`
            <input type="password" id="layui-layer-content_pass" class="layui-layer-input" placeholder="密码" value="" style="margin-top: 6px;">
            <input type="text" id="layui-layer-content_code" style="width:45%;height:40px;margin-top: 6px;" placeholder="验证码" value="0000">
            <img src="${lx.wwwroot+"/login/code"}" id="codeImg" style="width:45%;margin: 6px 0 0 6px;">
            `);
    },
    sys_service: function(data, funcallback) {
        var newurl = this.wwwroot + 'sys/service';
        this.ajax(newurl,funcallback,data);
    },
    /**
     *   打开新的弹出窗口
     * @param title  窗口 标题
     * @param type  类型   frame   content
     * @param url  页面地址
     * @param width  宽度
     * @param height  高度
     */
    openNewBox:function(title,type,url,width,height){
        window.box = new window.top.Box({ //实例化顶层窗体的弹窗
            title:title, //弹窗标题
            type: type, //弹窗类型 content|frame , 默认 content
            src: url, //content 的内容
            width: width, //弹窗宽度
            height: height //弹窗高度
        });
        box.open();//显示弹窗
    }	,/* 分析地址参数等 */
    request: function(paras) {
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

