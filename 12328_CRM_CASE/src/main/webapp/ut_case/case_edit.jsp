<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ taglib uri="/ctsControl" prefix="cts"%>
<!DOCTYPE html>
<html>
<head>
    <title>工单资料</title>
      <%@ include file="/ut_controls/PageHeaderMeta.jspf" %>
       <%@ include file="/ut_controls/PageHeader_editor.jspf"%>
   
     <script type="text/javascript" src="${rootURL }/ui_common/javascript/ut_softcall.js"></script>
      <script type="text/javascript" src="${rootURL }/ATClient/ATClient.js"></script>
       <script type="text/javascript" src="${rootURL }/ui_widget/ut_widget.js?v=a"></script>
       <script type="text/javascript" src="${rootURL }/ut_case/style_query/ut_select_busniess.js"></script>
     <script type="text/javascript" language="javascript">
       $(function() {
           var hide_more = getCookie("hide_more");
           if(hide_more == "1")
               document.getElementById("tpCase_more_header").style.display = "none";
       });
       
       // 打开弹出框返回的消息 add by liukj 20170922
       function onReceive_dialog_response(strRet) {
           var nIdlg = strRet[0].Idlg;
           fun_close(nIdlg)
           var strContent =strRet[0].ucontent;
           //去除所有空格,冒号:换行
           strContent = strContent.replace(/\s/g,'')
           strContent = strContent.replace(/\:/g,':\r\n')
           strContent = strContent.replace(/\|/g,'\r\n')
           
           //add by xutt 20171012
           var strKey =strRet[0].key;
           if(strKey=="reaction"){
        	   $("#REACTION_CONTENT").val(strContent); 
           }
           else if(strKey=="reply"){
        	   $("#REPLY_CONTENT").val(strContent);         	   
         }
       }
     
      //处理Toolbar 的按键事件
        function btnToolBarClick(strName) {
            var bReturn = false;
            if (strName == "Save") //保存工单  
            {
                if (confirm("确实要保存工单资料吗？") == true) {
                    bReturn = true;
                }
            }
            else if(strName=="Delete")
            {
                if (confirm("确实要删除工单资料吗？") == true) {
                    bReturn = true;
                }               
            }
            else if(strName=="SendNote1")
            {
                var caseid= $("#CASEID").val();
                var casetype=$("#CASETYPE").val();
                var strNoteInfo = "CASEID=" + caseid + ";PTYPE=" + casetype + ";";
                var strNewUrl =  rootURL+"/ut_personality/ut_priv_note/note_msg_send.aspx?cmd=send&ntype=3&nTaskType=2&taskinfo=" + strNoteInfo;
                fun_open(strNewUrl,"发送公文通知",1000,600);
               // bReturn=false;
            }
            else if (strName=="SetNotify1")
            {
                var caseid= $("#CASEID").val();
                var casetype=$("#CASETYPE").val();
                var strNoteInfo = "CASEID=" + caseid + ";PTYPE=" + casetype + ";";
                var strNewUrl =  rootURL+ "/ut_personality/Task_notice_edit.aspx?cmd=notify&taskinfo=" + strNoteInfo;                
                fun_open(strNewUrl,"设定任务提醒",1000,600);
                bReturn=false;
            }
            else bReturn=true;
            
            return bReturn;
       }    
        
        function Set_WorkFlow(strParm) {
            //alert(strParm);
            //var strUrl = "/proj_case/ut_case/case_wf.aspx?CASEID="+$('#CASEID').val() +"&CASETYPE=1&CASETABLE=CRM_CASE";
            var strUrl;
            if (strParm.indexOf("&WF=1") > 0) {
                strUrl = "./ut_case/wf_process_set.aspx?caseid=" + $('#CASEID').val() + strParm;
                win_open(strUrl, "设定工作流", 1000, 525);
            }
            else if (strParm.indexOf("&WF=0") > 0) {
                strUrl = "./ut_case/wf_status_set.aspx?caseid=" + $('#CASEID').val() + strParm;
                win_open(strUrl, "设定工单流转", 800,525);
            }
            else {
                strUrl = "./ut_case/case_wf.aspx?caseid=" + $('#CASEID').val() + strParm;
                win_open(strUrl, "设定工作流", 430, 525);
            }
            return false;
        }

        function View_WorkFlow(strParm) {
            var strUrl;
            if (strParm.indexOf("&WF=1") > 0) {
                strUrl = "./ut_case/wf_trace_view.aspx?caseid=" + $('#CASEID').val() + strParm;
                win_open(strUrl, "工单跟踪", 1000, 525);
            }
            else { /* //if (strParm.indexOf("&WF=0") > 0)   */             
                strUrl = "./ut_case/wf_trace_view.aspx?caseid=" + $('#CASEID').val() + strParm;
                win_open(strUrl, "工单跟踪", 800, 525);
            }
            return false;
        }

        //处理myFld 的按键事件
        function myFld_FieldLinkClicked(strName, strParm, nType) {
            var bReturn = true;           
            if ((nType == 11) || (nType == 12)) //11  打开数据字典
            {
                if (strParm != "") {
                    strParm = strParm.replace(/;/g, "&");  //g，将替换所有匹配的子串
                    var dlg_id = fun_open("./ut_tools/dictionary.aspx?" + strParm, '数据字典', 700, 420)
                    bReturn = false;
                }
            }
            else if ((nType == 6) || (nType == 16)) { //add by gaoww 20151208
                if (strParm == "view") //查看原图
                {
                    var strUrl = $('#' + strName).attr("src");
                    var strTitle = "查看原图"
                    open(strUrl, strTitle);
                }
                else if (strParm == "upload") //16  Image 上传图片，回调后台代码进行上传
                {
                    bReturn = true;
                }
            }
            if (strName == "TEL") //选择呼叫电话
            {
                //modify by gaoww 20160427 配合20160427以后的ctstools，修改对加密字段在前台获取实际值的方法
                var myCalled = $("#" + strName).attr("tag")
                if (myCalled != undefined) {
                    myCalled = base64decode(myCalled);
                }
                else
                    myCalled = $("#" + strName).val();
                at_placecall(myCalled);
                bReturn = false;
            }
            else if (strName == "CALLER") //选择呼叫来电号码
            {
                //modify by gaoww 20160427 配合20160427以后的ctstools，修改对加密字段在前台获取实际值的方法
                var myCalled = $("#" + strName).attr("tag")
                if (myCalled != undefined) {
                    myCalled = base64decode(myCalled);
                }
                else
                    myCalled = $("#" + strName).val();
                at_placecall(myCalled);
                bReturn = false;
            }
            else if (strName == "MOBILENO")//选择呼叫手机
            {
                //modify by gaoww 20160427 配合20160427以后的ctstools，修改对加密字段在前台获取实际值的方法
                var myCalled = $("#" + strName).attr("tag")
                if (myCalled != undefined) {
                    myCalled = base64decode(myCalled);
                }
                else
                    myCalled = $("#" + strName).val();
                at_placecall(myCalled);
                bReturn = false;
            }
            else if(strName == "REACTION_CONTENT"){//弹出工单反应内容文本模板 add by liukj 20170922
                
                    var dlg_id = fun_open("ut_case/case_template.aspx?key=reaction&caseId=" + $('#CASEID').val(), '反应内容', 700, 420);
                    bReturn = false;
                
            } 
            else if(strName == "REPLY_CONTENT"){//弹出工单答复内容文本模板 add by xutt 20171012
                
                var dlg_id = fun_open("ut_case/case_template.aspx?key=reply&caseId=" + $('#CASEID').val(), '答复内容', 700, 420);
                bReturn = false;
            } 
            else if (strName == "BUSINESS")//add by Liukj 20171025 加载树
            {
            	$("#BUSINESS2").val("");
            	$("#BUSINESS3").val("");
            	return false;
            }
            else if (strName == "BUSINESS2")//add by Liukj 20171025 加载树
            {
            	$("#BUSINESS3").val("");
            	var str2 = $("#BUSINESS").val();
                /* if(str2.length<=0) str2="";
                else  str2="PNODE='"+str2+"' AND "; */
                config = { table: 'CRM_DICT_BUSINESS',  field:' TYPEID,KNAME ' ,filter:str2, conn: 'crm'};
                $.ut_tree_load("BUSINESS2", config,0,"A(B)&;&RL");
                $.ut_tree_show(strName);
                bReturn = false;
            }
            else if (strName == "BUSINESS3")//add by Liukj 20171025 加载树
            {
            	var str3 = $("#BUSINESS2").val();
            	if(str3.length<=0) str3="";
                else{
                	var index = str3.indexOf("(");
                	str3 = str3.substring(0,index);
                }
            	config = { table: 'CRM_DICT_BUSINESS',  field:' TYPEID,KNAME ' ,filter:str3, conn: 'crm'};
                $.ut_tree_load("BUSINESS3", config,0,"A(B)&;&RL");
                $.ut_tree_show(strName);
                bReturn = false;
            }
            else if(strName == "COUNTY"){
            	var ZB_CASEID = $("#ZB_CASEID").val();
                ZB_CASEID = "JT"+strParm+ZB_CASEID.substring(8);
                $("#ZB_CASEID").val(ZB_CASEID);
                bReturn = false;
            }
            return bReturn;
        }
        //add by XUTT 20170922 在主框架下打开新页面
        function Add_MainTab(strTitle, strUrl, strKey) {
            if (window.parent != undefined) {
                window.parent.AddNewFrameTab(strTitle, strUrl);
             }
            else {
                fun_open(strUrl, strTitle, 1000, 600)
            }
        }
        
        
        //处理 myFld_FieldLinkClicked 打开弹出框返回的消息 add by xutt 20150116
        function onReceive_dialog_response_ext(strRet) {
            var nIdlg = strRet[0].Idlg;
            fun_close(nIdlg)
            var nType = fun_querystring("ntype");
            var strUid = strRet[0].kid;
            var login_userid = strRet[0].uid_scr;
            var caseid= $("#CASEID").val();
            var strKey = strRet[0].key;
            //填写转人
           $("#RETURN_GHID").val(strUid);
            SoftCall(strUid, login_userid, nType, strKey,caseid);    
        }
        // add by xutt  
        function SoftCall(strUid_dest, login_userid, nType, strKey, caseid) {
            var Sendmsg;
          //  if (strKey == "Return_Deal") { //退回单
         //       Sendmsg = "Return_Deal;FROM=" + login_userid + ";TO=" + strUid_dest + ";MSG=" + caseid + ";TYPE=" + nType + ";";
         //       ATSendMsg(login_userid, strUid_dest, Sendmsg);
        //    }
           if (strKey == "Transfer") 
            {   //转办单
                Sendmsg = "TRANSFER;FROM=" + login_userid + ";TO=" + strUid_dest + ";MSG=" + caseid + ";TYPE=" + nType + ";";
                ATSendMsg(login_userid, strUid_dest, Sendmsg);
                alert("转办成功！");
            }
        }
        </script>

</head>
<body>
    <form id="frmBody"  method="post">
         <div id="divBody">
              <cts:ToggleControl id="tpCase_edit" title="工单详细资料" >         
                    <div id="divContent" >
                        <div id="plCommand"  style="width:100%;height:auto;">${plCommand}</div>
                        <div id="gbEdit"  style="height:100%;width:98%;">${gbEdit}</div>
                     </div>
             </cts:ToggleControl>
             <cts:ToggleControl id="tpCase_more" title="工单相关信息" collapsed="false" >
                 <div id="divContent" >
                        ${plRelation}
                 </div>        
            </cts:ToggleControl>        
        </div>
    </form>
</body>
</html>

