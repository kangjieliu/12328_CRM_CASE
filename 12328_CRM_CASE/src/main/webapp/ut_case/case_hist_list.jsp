<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
    <title>工单历史记录</title>
      <%@ include file="/ut_controls/PageHeaderMeta.jspf" %>
      
      <script type="text/javascript" language="javascript">
      //处理Toolbar 的按键查询事件 
      function fun_query(strKey) {
          fun_open("~/ut_tools/frmSearch_popup.aspx?key=" + strKey, '查询工单资料', 700, 420)
      }
     
      function btnToolBarClick(strName) {
          var bReturn = false;
          if (strName == "Delete") {
        		if (confirm("确实删除选中的记录吗？") == true) {
                    bReturn = true;
            	}     
          }
          else
        	  bReturn = true;
          return   bReturn;
          
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
      <div id="divContent" >
             <div id="divCommand" style="width:100%;height:auto;">${plCommand}</div>
      </div>         
       <div id="divContent">
         <div>${dgvList}</div>
       </div>
 </form>
</body>
</html>
