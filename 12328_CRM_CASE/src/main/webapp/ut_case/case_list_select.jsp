<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
    <title>所属工单</title>
      <%@ include file="/ut_controls/PageHeaderMeta.jspf" %>
      
      <script type="text/javascript" language="javascript">
      
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
