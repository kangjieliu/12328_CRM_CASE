<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title></title>
    <%@ include file="/ut_controls/PageHeaderMeta.jspf" %>
    <%@ include file="/ut_controls/PageHeader_ui.jspf"%>
   <script language="javascript" type="text/javascript">
        $(document).ready(function() {
          var clientHeight = document.documentElement.clientHeight+30; //$(document).height();
          //alert(clientHeight);
          $("#frm_left_qreply").height(clientHeight);
          $("#frm_main").height(clientHeight);
      });
    </script>

    <style type="text/css">
     html,body
      {
        background: #EFEFEF;
        height:100%;
        overflow:auto;
     }
    .left 
     { 
        position:absolute; 
        left:0px; 
        top:0px; 
        bottom:0px; 
        width:180px;
    }
   .main 
    { 
      position:absolute; 
      left:181px; 
      top:0px; 
      bottom:0px; 
      right:0px;
     }
   .left iframe, .main iframe 
     { 
       width:100%; 
       height:600px;
     }
  </style>
</head>
    <body>
    <form id="frmBody"  method="post">
    <div id="divBody" style=" background:green;">
      <div class="left">
   <div title="服务号菜单管理" class="myKn_tree_left">
                                <%@ include file="case_business_left_tree.jspf"%>
                            </div>
      </div>
      <div class="main">
        <iframe frameborder="0" id="frm_main" name="frm_main" src="case_business_list.aspx"
                        style="width: 100%; height: 100%"></iframe>
      </div>
    </div>
    </form>
</body>
</html>


