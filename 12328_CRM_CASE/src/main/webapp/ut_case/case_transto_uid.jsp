<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>转发工单至座席</title>
<%@ include file="/ut_controls/PageHeaderMeta.jspf"%>
 <script language="javascript" type="text/javascript" src="${rootURL}/ATClient/ATClient.js"></script>
  <script type="text/javascript" language="javascript">
  function btnToolBarClick(strName) {
      var bReturn = false;
      if (strName == "Update") {
          return;
          /*var frmTop = top.document; //top.Details.document;
          if (frmTop == null) return;
          var myUid = frmTop.frmLogin.agent_uid.value;
          var recv_uid = document.getElementById("RECV_SEATID");
          if (recv_uid == null) return;
          frmTop.ut_atocx.ATTranMsg(myUid, recv_uid, Sendmsg);*/ //delete by gaoww 20110630 改由后端代码处理发送消息
      }
      else if (strName == "Close") {
          window.close();
      }
      return bReturn;
  }
  //add by gaoww 20150511 改为由父窗体关闭
  function window_close() {
      if (window.parent != null)
          window.parent.window_close();
  }
  
  function Fill_text(GHID,REAL_NAME)
  {
	  $("#RECV_SEATNAME").val(REAL_NAME);
	  $("#RECV_SEATID").val(GHID);
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