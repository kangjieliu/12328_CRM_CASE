<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>查看工单踪迹</title>
        <%@ include file="/ut_controls/PageHeaderMeta.jspf" %>
    <style type="text/css">
        
  #divProcess
  {
   margin:10px 0 10px;
  }      
  .Process_btnStyle_done 
  {
  	 height:30px;
 	 font-size: 15px;
	 margin: 3px 2px 3px 2px;
	 padding-left: 3px;
	 padding-right: 3px;
 	 background-color: #80b000;    
	 border: 1px solid #719C00 !important;
  }
  .Process_btnStyle_select {
  	 height:30px;
 	 font-size: 15px;
	 margin: 3px 2px 3px 2px;
	 padding-left: 3px;
	 padding-right: 3px;
 	 background-color: #009C74;    
	 border: 1px solid #007050 !important;
  }
  .Process_btnStyle_dis {
  	 height:30px;
 	 font-size: 15px;
	 margin: 3px 2px 3px 2px;
	 padding-left: 3px;
	 padding-right: 3px;
 	 background-color: #E1E1E1;    
	 border: 1px solid #A4A4A6 !important;
  }
  </style>
</head>
<body>
    <form id="form1"  method="post">
    <div id="divBody" align="center">
         <div id="divInfo_case">
 	           <div id="plInfo_case"  style="height:100%;width:100%;">${plInfo_case}</div>		 
         </div>
         <div id="divProcess" >
            <div id="plProcess"  style="height:auto;width:100%;">${plProcess}</div>	
         </div>
         <div id="divHandle" >
           <div>${dgvList}</div> 
         </div>
  	     <div id="prompt_msg" style="color:#3366CC;" >
  	           <span id="lblRem">${lblRem }</span>
  	      </div>
    </div>
    </form>
</body>
</html>


