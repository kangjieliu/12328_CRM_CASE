<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>班长座席</title>
   <%@ include file="/ut_controls/PageHeaderMeta.jspf" %>
     <script type="text/javascript" language="javascript">
     //add by xutt 20150116 返回消息
     function onBtnUpdate(strRet) {
       
      
   
         var dgRet = frameElement.lhgDG;
         dgRet.curWin.onReceive_dialog_response_ext(strRet);
        // var nIdlg = fun_querystring("nIdlg");
      //   fun_close(nIdlg);
         dgRet.cancel();
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