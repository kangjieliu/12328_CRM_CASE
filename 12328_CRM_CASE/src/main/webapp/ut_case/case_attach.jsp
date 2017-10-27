<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
    <title>工单附件</title>
      <%@ include file="/ut_controls/PageHeaderMeta.jspf" %>
      
      <script type="text/javascript" language="javascript">
      $(function() {
           $("#divAttach").hide();
          });

          function btnToolBarClick(strName) {
              var bReturn = false;
              if (strName == "ShowUpload") {
                  $("#divAttach").toggle();
              }
              else 
            	  bReturn=true;
              return bReturn;
          }
          
          function myFld_FieldLinkClicked(strName, strParm, nType) {
       	     var bReturn = false;
       	     console.log("myFld_FieldLinkClicked="+strName+ strParm+ nType);
       	     if ((strName == "ATTACH"))
       	     {
       	    		 bReturn =true; 	 
       	     }
       	     return bReturn;
          }


    </script>

</head>
<body>
    <form id="frmBody"  method="post">
	     <div id="divContent" >
	         <div id="divCommand" style="width:100%;height:auto;">${plCommand}</div>
         </div>         
         <div id="divAttach">
           	<div id="gbEdit"  style="height:100%;width:98%;">${gbEdit}</div>         
         </div>            
         <div id="divList" >
            <div>${dgvList}</div>
         </div>          
    </form>
</body>
</html>
