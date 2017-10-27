<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
    <title>弹屏工单资料列表</title>
      <%@ include file="/ut_controls/PageHeaderMeta.jspf" %>
      
      <script type="text/javascript" language="javascript">
      /*$(document).ready(function() {
      	 var strUrl = getCookie("redirect_url");      
      	 strUrl = strUrl.replace(/"/,"");      	 
      	 if(strUrl.length>3)
         {
         	 window.location.href=strUrl;         
         }
      });*/
      
    //处理Toolbar 的按键事件 
      function btnToolBarClick(strName)
       {  
         var bReturn = false;
         if(strName=="AddNew"){
        	var nPos = $("#Select_CaseType").val().indexOf("(");
        	var nType = 0;
        	if(nPos>=0)
        		nType= $("#Select_CaseType").val().substring(0,nPos);
        	var caller=fun_querystring("caller");
        	var info=fun_querystring("info");
        	var callid=fun_querystring("callid");
            var strUrl = "popup_case_edit.aspx?cmd=AddNew&caseid=&ntype="+nType+"&caller="+caller+"&info="+info+"&callid="+callid;
            window.location.href=strUrl;      
         }
         else if(strName == "Save") //保存工单  
         {
            if(confirm("确实要修改工单资料吗？")==true)
            {
              bReturn = true;
            }
         }
         return bReturn;
       }

       //处理Toolbar 的按键查询事件 
       function fun_query(strKey) {
           fun_open("~/ut_tools/frmSearch_popup.aspx?key=" + strKey, '查询工单资料', 700, 420)
       }

       //查询结果返回结果
       function onReceive_dialog_response(strRet) {
           window.location.href = strRet;
       }
       
       function open_view(strUrl)
       {
       	window.location.href=strUrl;            
       }
    </script>

</head>
<body>
   <form id="frmBody"  method="post">         
	      <div id="divCommand" >
	             <div id="plCommand" style="width:100%;height:auto;">${plCommand}</div>
	      </div>         
           <div id="divContent">
           		<div>${dgvList}</div>
          </div>		           
   </form>
</body>
</html>
