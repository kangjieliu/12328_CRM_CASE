<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>list</title>
<%@ include file="/ut_controls/PageHeaderMeta.jspf"%>
 <script language="javascript" type="text/javascript" src="${rootURL}/ATClient/ATClient.js"></script>
  <script type="text/javascript" language="javascript">
  function btnToolBarClick(strName) {
      var bReturn = false;
      if (strName == "Save") {
    	  bReturn = true;
          return bReturn;
      }
      return bReturn;
  }
  //add by gaoww 20150511 改为由父窗体关闭
  function window_close() {
      if (window.parent != null)
          window.parent.window_close();
  }
  
  function Fill_text(MATTERS,MATTERS_CODE,pDomainCode,pDomain)
  {     
	  $("#MATTERS_CODE").val(MATTERS_CODE);
	  $("#MATTERS").val(MATTERS);
	  $("#DOMAIN_CODE").val(pDomainCode);
	  $("#DOMAIN").val(pDomain);
  }
</script>
    <style type="text/css" >
      html,body
      {
  	    background: #EFEFEF;
   	    height:100%;
   	    overflow:hidden;
     }
    </style>
</head>
<body>
 <form id="frmBody"  method="post">     
       <div id="divBody" align="center">
           <div id="divEdit" >
              <div id="gbEdit"  style="height:100%;width:98%;">${gbEdit}</div>
             </div>
           <div id="divToolbar">
                <div id="plCommand" style="width:100%;height:auto;text-align:left;">${plCommand}</div>
           </div>           
            <div id="divContent">
                  <div>${dgvList}</div>
             </div>
        </div>
    </form>
</body>
</html>