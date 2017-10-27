<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>list</title>
<%@ include file="/ut_controls/PageHeaderMeta.jspf"%>
 <script language="javascript" type="text/javascript" src="${rootURL}/ATClient/ATClient.js"></script>
  <script type="text/javascript" language="javascript">
  function btnToolBarClick(strCmd) {
      
      return true;
  }
  
  //add by Liukj 20171024
  function Update() {
	  parent.location.reload();
  }

</script>
    
</head>
<body>
    <form id="frmBody">
        <div id="divCommand">
            <div id="plCommand" style="width: 100%; height: auto;">${plCommand}</div>
            <div id="plEdit" style="width: 100%; height: auto;">${plEdit}</div>
        </div>
    </form>
</body>
</html>