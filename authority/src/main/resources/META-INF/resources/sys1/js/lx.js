/**
 * Created by 游林夕 on 2019/9/12.
 */
//vue双向数据绑定的原理几个关键点
//1.observe 数据劫持
//2.Dep 消息订阅器（收集订阅者，发布消息）
//3.Watcher （订阅者）
//4.Compile HTML模板解析器
//5.Vue 入口函数

//1.observe 对数据进行拦截
function observe(data) {
    if (!data || typeof data !== 'object') { return; }
    Object.keys(data).forEach(function (key) {
        defineReactive(data, key, data[key]);
    });
}
function defineReactive(data, key, value) {
    observe(value);//如果子属性为object也进行遍历监听
    let dep = new Dep();//每一个key都会有一个dep实例来管理自己的订阅者
    Object.defineProperty(data, key, {
        configurable: false,
        enumerable: true,
        get: function () {
            if (Dep.target) { //在Watcher初始化实例的时候回触发对应属性的get函数 此时将对应的watcher添加到对应的subs中
                dep.addSub(Dep.target);
            }
            return value;
        },
        set: function (newValue) {
            if (value === newValue) {
                return;
            }
            value = newValue;
            dep.notice();
        }
    })
}
//2.Dep消息订阅器（收集订阅者，发布消息）
function Dep() {
    this.subs = [];
}
Dep.prototype = {
    addSub: function (sub) {
        this.subs.push(sub);
    },
    notice: function () {
        this.subs.forEach(function (sub) {
            sub.update();
        })
    }
}
Dep.target = null;//临时缓存watcher
//3. Watcher 观察者
function Watcher(vm, key, callback) {
    this.callback = callback;
    this.vm = vm;
    this.key = key;
    this.value = this.get(); //触发属性的get函数，然后添加到对应的消息订阅器上
}
Watcher.prototype = {
    update: function () {
        let value = this.vm[this.key];//这里虽然也会触发get函数 但是不会再次添加观察者到消息订阅器中
        let oldValue = this.value;
        if (oldValue !== value) {
            this.callback.call(this.vm, value, oldValue);
        }
    },
    get: function () {
        Dep.target = this;//缓存下watcher自己
        let value = this.vm[this.key];//在第一次new Watcher执行到这里的时候 会触发get函数，此时会添加watcher到相应的sub中
        Dep.target = null;//添加成功
        return value;
    }
}
//4.compile 模板解析器
function Compile(el, vm) {
    this.vm = vm;
    this.$el = document.querySelector(el);
    if (this.$el) {
        this.$fragment = this.createFragment(this.$el);//初始化dom片段，防止频繁的操作dom
        this.compileElement(this.$fragment);//解析节点
        this.$el.appendChild(this.$fragment);
    }
}
Compile.prototype = {
    createFragment: function (el) {
        let fragment = document.createDocumentFragment();
        let child;
        while (child = el.firstChild) {
            fragment.appendChild(child);//firstChild和firstElementChild区别 若文档已存在了该节点，则会先删除，然后在插入到新的位置
        }
        return fragment;
    }, compileElement: function (el) {
        let childNodes = el.childNodes;// childNodes获取元素节点和文本节点，children只获取元素节点
        let self = this;
        Array.from(childNodes).forEach(function (node) {
            let reg = /\{\{(.*)\}\}/;
            if (node.nodeType === 1) {
                self.compile(node);//按元素节点处理
            } else if (node.nodeType === 3 && reg.test(node.textContent)) {
                self.compileText(node, RegExp.$1);//按文本节点处理
            }
        });
    },
    compileText: function (node, exp) {
        let text = this.vm[exp];
        node.textContent = text;//更新文本节点的值
        new Watcher(this.vm, exp, function (value) {//生成订阅器并绑定更新函数, model => view
            node.textContent = value;
        });
    }, compile: function (node) {
        let nodeAttrs = node.attributes;//解析元素节点的属性
        for (let attr of  nodeAttrs) {
            let attrName = attr.name;
            if (attrName.includes('v-')) {//判断是否是规范的指令，v-开头
                let exp = attr.value;
                let dir = attrName.slice(2);
                if (dir.includes('on')) { //判断是什么指令，事件指令？还是普通指令
                    compileUtil.eventHander(node, this.vm, exp, dir);//根据事件指令集进行处理
                } else {
                    compileUtil[dir](node, this.vm, exp);//这里假设是最简单的v-model指令
                }
            }
        }
        this.compileElement(node);//继续解析该元素的子节点
    }
}
let compileUtil = {
    model: function (node, vm, exp) {
        node.value = vm[exp];//初始化值
        node.addEventListener('input', function (event) {//view => model
            vm[exp] = event.target.value;
        })
        new Watcher(vm, exp, function (value) {//modal => view
            node.value = value;
        })
        node.nodeValue = vm[exp];//强制触发对应的get函数，来通知其他的观察者更新数据
    },
    eventHander: function (node, vm, exp, dir) {
        let eventName = dir.slice(3);//截取事件名称，click
        node.addEventListener(eventName, function (event) {//给指定的节点绑定事件监听
            vm[exp]();
        })
    }
}
function proxy(vm, data, str) {
    Object.keys(data).forEach(function (key) {
        if (typeof (data[key]) == 'object') {//如果是对象继续解析
            str += key + ".";
            proxy(vm, data[key], str);
        } else {//否则就绑定事件
            vm.proxyKey(data, str + key, key)
        }
    });
}
//5.Vue函数入口
function Vue(options) {
    if (!this instanceof Vue) {//检测是否通过new关键调用Vue
        alert('please use Vue by "new" key word!') //如果是当成普通函数调用，this=>window
    }
    let vm = this;
    vm.data = options.data;
    vm.methods = options.methods;
    let data = options.data;
    let methods = options.methods;
    proxy(vm, vm.data, '');//将vm.key代理到vm.data.key
    proxy(vm, vm.methods, ''); //将vm.key代理到vm.$methods.key
    observe(data);
    vm.$compile = new Compile(options.el, vm);
}
Vue.prototype = {
    proxyKey: function (targetObj, key, kk) {
        let vm = this;
        Object.defineProperty(vm, key, {
            configurable: false,
            enumerable: true,
            get: function () {
                return targetObj[kk];
            }, set: function (newValue) {
                targetObj[kk] = newValue;
            }
        })
    }
}
class Router {//路由 地址id , 配置
    constructor(path,options) {
        this.$option = options;
        this.$path = path;
        let that = this;
        window.addEventListener('load', this.updateView.bind(this), false);
        window.addEventListener('hashchange', this.updateView.bind(this), false);
    }
    updateView() {// 更新试图
        const currentUrl = window.location.hash.slice(1) || '/' ;
        View.setHTMLWithScript(document.getElementById(this.$path),View.getHtml(this.$option[currentUrl]));
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