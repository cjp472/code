
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


/**
 基本工具
 */
var lx = {
    /* 用户对像 */
    userinfo: new Object()
    ,
    /* 服务器根 */
    wwwroot: getRootPath_web()
    ,
    /* 服务器根 */
    wwwroot_image: getRootPath_web()
    ,
    /* 回到顶部  */
    returntop:function(){
        $("html,body").animate({scrollTop:0},1000);
    }
    ,
    showerrinfo:function(msg)
    {
        try
        {
            $.messager.alert('提示', msg, 'info');
        }catch(e)
        {
            alert(msg);
        }
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
    ajax: function(url, funcallback) {
        var timestamp = new Date().getTime();
        var newurl = this.wwwroot + url;
        if(url.indexOf("?") >= 0)
        {
            newurl += "&t=" + timestamp;
        }else
        {
            newurl += "?t=" + timestamp;
        }
        // newurl+="&token="+ $.cookie('token');
        $.ajax(
            newurl, {
                async: false,
                cache: false,
                dataType: "json",
                data: '',
                type: 'POST',
                processData: true,
                headers: {
                }
                , complete: function(e, xhr, settings){
                }, success: function(data, textStatus, jqXHR) {
                    if (data.success == '0'){
                        alert(data.msg);
                    } else if (data.success == '9') {
                        window.location.href = "/login.html";
                    }else{
                        funcallback(data);
                    }
                }
            }
        ).error(function(jqXHR, textStatus, errorThrown) {
        });
    },
    sys_service: function(data, funcallback) {
        var timestamp = new Date().getTime();
        var newurl = this.wwwroot + 'sys/service?token='+ $.cookie('token');
        $.ajax(
            newurl, {
                async: false,
                cache: false,
                dataType: "json",
                data: data,
                type: 'POST',
                processData: true,
                headers: {
                }
                , complete: function(e, xhr, settings){
                }, success: function(data, textStatus, jqXHR) {
                    if (data.success == '0'){
                        alert(data.msg);
                    } else if (data.success == '9') {
                        window.location.href = "/login.html";
                    }else{
                        funcallback(data);
                    }
                }
            }
        ).error(function(jqXHR, textStatus, errorThrown) {
        });
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
    }
};

