<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>转发工单至Email</title>
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
  
  function Fill_text(EMAIL)
  {	  
	  var strVal_old = $("#EMAIL").val();
      var strVal_new = strJoin(strVal_old, EMAIL);
      $("#EMAIL").val(strVal_new);
  }
  
//合并两个字符串，并去重 （$.unique 3.0 后弃用）
  function strJoin(strVal_old, strVal_new) {
      var strReturn = ""; //strVal_old + ";" + strVal_new;

      if ((strVal_old.length > 0) && (strVal_new.length > 0)) {
          var alVal_old = strVal_old.split(';');
          var alVal_new = strVal_new.split(';');
          try {
              for (var i = 0, length = alVal_new.length; i < length; i++) {
                  var bFind = 0;
                  var tmp = alVal_new[i];
                  $.each(alVal_old, function (key, val) {
                      if (val == tmp) {
                          bFind = 1;
                          return false;
                      }
                  });

                  if (bFind == 0)
                      alVal_old.push(tmp);
              }
              strReturn = alVal_old.join(';');
          }
          catch (e) { strReturn = strVal_old + ";" + strVal_new; }
      }
      else
          strReturn = (strVal_old.length > 0) ? strVal_old : strVal_new;
      return strReturn;
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