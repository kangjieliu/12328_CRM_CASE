<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ taglib uri="/ctsControl" prefix="cts"%>
<!DOCTYPE html>
<html>
<head>
    <title>工单资料</title>
      <%@ include file="/ut_controls/PageHeaderMeta.jspf" %>
     <script type="text/javascript" src="${rootURL }/ui_common/javascript/ut_softcall.js"></script>
      
     <script type="text/javascript" language="javascript">
       $(function() {
    	   var hide_more = getCookie("hide_more");
    	   if(hide_more == "1")
    		   document.getElementById("tpCase_more_header").style.display = "none";
       });
       
      //处理Toolbar 的按键事件
        function btnToolBarClick(strName) {
            var bReturn = false;
            if (strName == "Save") //保存工单  
            {
                if (confirm("确定保存模板内容吗？") == true) {
                    bReturn = true;
                }
            }else if(strName == "Return"){
            	bReturn = true;
            }
            return bReturn;
       }    
        
        </script>

</head>
<body>
    <form id="frmBody"  method="post">
	     <div id="divBody">
			        <div id="divContent" >
			            <div id="plCommand"  style="width:100%;height:auto;">${plCommand}</div>
		             </div>
		             <div id="gbEdit"  style="height:100%;width:98%;">${gbEdit}</div>
        </div>
    </form>
</body>
</html>
