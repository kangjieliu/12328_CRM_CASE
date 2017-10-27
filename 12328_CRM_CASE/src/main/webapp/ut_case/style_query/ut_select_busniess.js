//########################################################################################
// Copyright (C) 2000, ToneThink.Soft  All Rights Reserved. 
// 文件创建时间：2016-07-01
//   文件创建人：peng
// 文件功能描述：强讯科技-通用的JS函数
//             
//     维护记录：
// 
//2016-07-11 peng
//          使用treeview 形式实现 下拉多选
//########################################################################################

//用法：
//  config = { key: 'uid_member' };
//  $("#RECVNAME").ut_tree_load(config);
(function ($) {
    document.write('<link href="'+rootURL + '/ui_common/jquery_ui/ztree/css/zTreeStyle.css" rel="stylesheet" type="text/css" />');
    document.write('<script src="'+rootURL + '/ui_common/jquery_ui/ztree/jquery.ztree.all.min.js" type="text/javascript"></sc' + 'ript>');
    var tv_options = {
        check: {
            enable: true,
           chkStyle: "radio"
        },
        data: {
            simpleData: {
                enable: true
            },
            key: {
                title: "title"
            }
        },
        callback: {
            beforeClick: beforeClick,
            onCheck: onCheck
        },
        view: {
            showLine: false
        }
    };

    //保存treeinfo 相关信息
    var tv_treeinfo = { name: '', tree_id: '', tree_wrap: '', formt: 'A', sep: ';',leaf:'L',callback:null }; //name-控件名
    var tv_treeinfo_set = function (strName_input) {
        tv_treeinfo.name = strName_input;
        tv_treeinfo.tree_id = strName_input + "_tvList";
        tv_treeinfo.tree_wrap = strName_input + "_tvList_wrap";
    }

    var Fld_tree_init = function (strName_input, jnConfig, nWidth, strFormat) {
        tv_treeinfo_set(strName_input);

        if (myUtil.json_key(jnConfig, "table") == true) {
            if ((jnConfig.table == "sp_uid") || (jnConfig.table == "sp_uid_online"))
                tv_treeinfo.leaf = "RL";
        }
        //FF&SS&LL  
        if (strFormat != null) {
            var nPos = strFormat.indexOf("&");
            if (nPos > 0) {
                var strSep = strFormat.substr(nPos + 1);
                strFormat = strFormat.substr(0, nPos);
                nPos = strSep.indexOf("&");
                if (nPos > 0) {
                    tv_treeinfo.leaf = strSep.substr(nPos + 1);
                    strSep = strSep.substr(0, nPos);
                }
                tv_treeinfo.sep = strSep;
            }
            tv_treeinfo.formt = strFormat;
        }

        if (nWidth == null) nWidth = 350;
        var TreeViewTpl =
           '<div id="' + tv_treeinfo.tree_wrap + '" style="display:none; position: absolute;">' +
           '<ul id="' + tv_treeinfo.tree_id + '" class="ztree" style="margin-top:0; width:' + nWidth + 'px; height: 300px;"></ul>' +
           '</div>';

        $(document.body).append(TreeViewTpl);

        if (myUtil.json_key(jnConfig, "callback") == true) {
            tv_treeinfo.callback = jnConfig.callback;
        }
        //else {
        //    if (myUtil.json_key(jnConfig, "key") == true) {
        //        strUrl = "/ut_http/treeview_select.ashx?type=" + jnConfig.key;
        //    }
        //}
        /*
        */
        if (myUtil.json_key(jnConfig, "data") == true) {
            var myTree = $.fn.zTree.init($("#" + tv_treeinfo.tree_id), tv_options, jnConfig.data);
            Fld_tree_on_TreeLoad(strName_input);
        }
        else {
            var strUrl =  rootURL+"/ut_case/treeview_select.ashx?opts=" + myUtil.base64encode(myUtil.from_json(jnConfig))+"&cmd="+strName_input;
            $.ajax({
                url: strUrl,
                type: 'GET',
                dataType: "json", //可以是text，如果用text，返回的结果为字符串；如果需要json格式的，可设置为json
                success: function (data) {
                    var myTree = $.fn.zTree.init($("#" + tv_treeinfo.tree_id), tv_options, data);
                    Fld_tree_on_TreeLoad(strName_input);
                },
                error: function (msg) {
                    console.log('树加载异常,' + msg);
                }
            });
        }
    }

    //装载完毕，根据hdnRecvName值，恢复选项
    function Fld_tree_on_TreeLoad(strName_input) {
        var strTemp = $("#" + strName_input).val();
        if (strTemp.length < 1) return;
        strTemp = strTemp.replace(/[(][^)]+[)]/g, "");  //去掉(xxx) 里的内容
        var ayDest = strTemp.split(tv_treeinfo.sep);

        var treeId = tv_treeinfo.tree_id;
        var myTree = $.fn.zTree.getZTreeObj(treeId);
        var myNodes_all = myTree.transformToArray(myTree.getNodes()); //获取 zTree 的全部节点数据
        var strItem;
        for (var i = 0; i < myNodes_all.length; i++) {
            strItem = myNodes_all[i].title;
            if ($.inArray(strItem, ayDest) != -1)   //确定第一个参数在数组中的位置(如果没有找到则返回 -1 )
            {
                myTree.checkNode(myNodes_all[i], true, true);
            }
        }
    }

    function beforeClick(treeId, treeNode) {
        var myTree = $.fn.zTree.getZTreeObj(treeId);
        myTree.checkNode(treeNode, !treeNode.checked, null, true);
        return false;
    }

    //根据选择，显示内容
    function onCheck(e, treeId, treeNode) {
        var myTree = $.fn.zTree.getZTreeObj(treeId);

        //var nSelectType = tv_treeinfo.type;  //1-ALL|一级菜单  2-ALL|一级菜单|二级菜单... 

        var myNodes_checked = myTree.getCheckedNodes(true); //获取勾选节点,ALL,G01,8601

        //title 保存id-A  name 保存name-B
        var ayId_checked = new Array(), ayKey_checked = new Array(), ayName_checked = new Array();
        for (var i = 0; i < myNodes_checked.length; i++) {
            if (myNodes_checked[i].getCheckStatus().half == false) {//完全勾选
                //if (nSelectType == 1) {
                //    if (myNodes_checked[i].children != null) continue;//只选择叶
                //}
                //else {
                //    if (ayId_checked.indexOf(myNodes_checked[i].title) >= 0) continue;  //已经存在
                //}
                //if (ayId_checked.indexOf(myNodes_checked[i].title) >= 0) continue;  //已经存在
                if (tv_treeinfo.leaf == "L") {
                    if (myNodes_checked[i].children != null) continue;//只选择叶
                }
                else {
                    if (myNodes_checked[i].level > 1)
                        if (ayId_checked.indexOf(myNodes_checked[i].pId) >= 0) continue;  //父节点已经包含集合里
                }
                ayId_checked.push(myNodes_checked[i].id);           //保存ID-编号
                ayKey_checked.push(myNodes_checked[i].title);       //保存ID-工号
                ayName_checked.push(myNodes_checked[i].name);       //保存显示值
                //if ((tv_treeinfo.leaf == "L")&&(tv_treeinfo.callback==null))
                if (myNodes_checked[i].title == "ALL") {
                    if (tv_treeinfo.callback == null) break;
                    else {
                        ayId_checked.pop();
                        ayKey_checked.pop();
                    }
                }
            }
        }

        var strRecvUid = ayId_checked.join(tv_treeinfo.sep);  //join 把数组中的所有元素放入一个字符串,元素通过;进行分隔
        var strFormat = tv_treeinfo.formt;

        if (strFormat == "B")
            strRecvUid = ayName_checked.join(tv_treeinfo.sep);  //join 把数组中的所有元素放入一个字符串,元素通过;进行分隔
        else if ((strFormat.indexOf("A") >= 0) || (strFormat.indexOf("B") >= 0)) {
            var ayTemp = new Array()
            for (var i = 0; i < ayKey_checked.length; i++) {
                if (ayKey_checked[i].length < 1) continue;
                ayTemp.push(strFormat.replace("A", ayKey_checked[i]).replace("B", ayName_checked[i]));
            }
            strRecvUid = ayTemp.join(tv_treeinfo.sep);
        }

        var FldId = tv_treeinfo.name;
        $("#" + FldId).val(strRecvUid); //HiddenField

        //if (myUtil.fun_exist("ut_select_result") == true) {
        //    ut_select_result(FldId, strRecvUid);
        //}
        var fn = tv_treeinfo.callback;
        if (typeof fn === 'function') {
            fn.call(this, FldId, strRecvUid);
        }
    }

    //显示treeview
    var Fld_tree_show = function (strName_input) {
        //var strName_key = "#" + strName_input;
        tv_treeinfo_set(strName_input);

        var Fld_master = $("#" + strName_input);
        var Offset = Fld_master.offset();
        $("#" + tv_treeinfo.tree_wrap).css({ left: Offset.left + "px", top: Offset.top + Fld_master.outerHeight() + "px" }).slideDown("fast");
        $("body").on("mousedown", Fld_tree_onBodyDown);
        //展开第一级,不起作用，后台处理
        /*
        var treeId = tv_treeinfo.tree_id;
        var myTree = $.fn.zTree.getZTreeObj(treeId);
        var zNode = myTree.getNodesByParam("level", 0, null);
        myTree.expandNode(zNode, true, false, true);
        var zNodes = myTree.getNodesByParam("level", 1, null);
        for (var i = 0; i < zNodes.length; i++) {
            myTree.expandNode(zNodes[i], true, false, true);
        }
        */
    }

    function Fld_tree_onBodyDown(event) {
        var strId = event.target.id;
        if (strId.indexOf(tv_treeinfo.tree_id) >= 0) return;
        Fld_tree_hide(strId);
    }

    function Fld_tree_hide(strId) {
        $("#" + tv_treeinfo.tree_wrap).fadeOut("fast");
        $("body").off("mousedown", Fld_tree_onBodyDown);
        if (strId.indexOf(tv_treeinfo.name + "_btn") < 0) { //按了12类型button，如：RECVNAME_btn ，由ut_tree_show()处理
            m_show = 0;
        }
    }

    //回调函数
    //function ut_select_result(strName,data) {
    //    alert(myUtil.from_json(data));
    //}

    //支持三种方式：
    //1-多级多选（叶+根） 2-多级多选（叶)  3、单级多选 
 
    //strName_input: 控件名，控件必须是TextBox 类型
    //     jnConfig：数据库参数 { table: 'xx', field: 'xx', filter: 'xx', conn: 'crm',data:json}
    //               table：表名,保留字：sp_group,显示管理组 sp_uid，显示管理组-所属成员 sp_uid_online，显示管理组-所属成员(电话状态)
    //               field：A或 A,B（A,B代表数据库字段）
    //               filter：查询条件，可选 
    //               conn：crm、callthink、cdr,数据库连接串
    //               data：json 格式数据，可选
    //       nWidth: 下拉框宽度。默认值：0-取TextBox 控件长度
    //    strFormat：显示格式：X[&Y][&Z],X可以是：A、B、A(B), 默认值：A   
    //                                   Y可选项,多选项之间的分隔符，默认值：;
    //                                   Z可选项,选中多级树的内容，L-叶，不包含根  RL-(叶+根,根全选，不再包含叶）单级多选默认值：L
    //     callback：回调函数 
    $.ut_tree_load = function (strName_input, jnConfig, nWidth, strFormat, callback) {
        //不过要注意：
        //.css("width")会带 单位， 例子中 会输出 ： 120px；
        //.width（）则不带单位 ， 输出 116；
        if ((nWidth == null) || (nWidth == 0)) nWidth = $("#" + strName_input).width() * 1.05;
        jnConfig = jnConfig || {};
        jnConfig.callback = callback;
        Fld_tree_init(strName_input, jnConfig, nWidth, strFormat);
    }
    var m_show = 0;
    $.ut_tree_show = function (strName_input) {
        if (m_show == 1) {
            m_show = 0;
            return;
        }
        Fld_tree_show(strName_input);
        m_show = 1;
    }

})(jQuery);
