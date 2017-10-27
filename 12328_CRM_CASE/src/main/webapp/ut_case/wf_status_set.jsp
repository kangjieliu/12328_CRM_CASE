<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>设定工单状态</title>
        <%@ include file="/ut_controls/PageHeaderMeta.jspf" %>
 
   <script type="text/javascript" language="javascript">
        function myToorBar_btnItemClick(strName) {
            var bRet = true;
            if (strName == "Save") {
                //bRet = confirm("确实要保存现有信息吗？");
            }
            return bRet;
        }
    </script>
 
  <style type="text/css">
  
  #divProcess
  {
   margin:10px;
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
  
  .wfcmd_btnStyle 
  {
  	 height:26px;
 	 font-size: 15px;
	 font-weight:200;
	 color: #444444;
	 margin: 3px 2px 3px 2px;
	 padding-left: 3px;
	 padding-right: 3px;
	 background: #ece9d8 url(images/form_button_bg.gif) repeat-x bottom left;
	 border: 1px solid #BED5F3 !important;
  }
  
  .wfcmd_btnStyle:hover
  {
	cursor: pointer;
	background: #ece9d8 url(images/form_button_hover.gif) repeat-x bottom left;
	color: Blue;
	border: 1px solid #f2ca58 !important;
  }
  .wfcmd_btnStyle_dis 
  {
  	 height:26px;
 	 font-size: 15px;
	 font-weight:200;
	 color: #444444;
	 margin: 3px 2px 3px 2px;
	 padding-left: 3px;
	 padding-right: 3px;
	 background: #ece9d8 url(images/form_button_disable.gif) repeat-x bottom left;
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
         <div id="divProcess">
            <div id="plProcess"  style="height:auto;width:100%;">${plProcess}</div>	
         </div>
         <div id="divHandle">
          <div id="plHandle"  style="width:100%;"><br />
             <div id="plRecv"  style="display:block" >
                 <fieldset style="height:auto; width:80%; text-align:centert; border: 1px solid Silver;">
                    <legend style="text-align: left;font-size:11pt;width:auto;">变更状态</legend>
                          <div id="plSubmit"   >
           	     	        <table  width="80%" border="0" cellpadding="0" cellspacing="0"  style="padding-top:15px">
                            <tr >
                                <td align="left"  style="width:120px;">               
                                   <input id="btnSubmit" name="btnSubmit" value=" 提交 " onclick="return myToorBar_btnItemClick('Submit');" class="wfcmd_btnStyle_dis" type="submit">      
                                </td>
                                <td align="left">
                                     ${cboSubmitGhid}  &nbsp;下一状态-接收人
                                 </td>
                            </tr>  
                          </table>
                       </div><br />
                       <div id="plBackward"  style="display:block">
           	              <table  width="60%" border="0" cellpadding="0" cellspacing="0">
                            <tr valign="top">
                                <td align="left" style="width:120px;">               
                                   <input  id="btnBackward" name="btnBackward" value=" 退回 " onclick="return myToorBar_btnItemClick('Backward');" class="wfcmd_btnStyle_dis" type="submit">      
                                 </td>
                                <td align="left">
                                    ${cboProcess_back} &nbsp;接收状态&nbsp;&nbsp;
                                    ${cboBackGhid} &nbsp;接收人
                                </td>
                            </tr>  
                          </table>
                       </div><br />
                       <div id="plSkip"  style="display:block">
           	              <table  width="60%" border="0" cellpadding="0" cellspacing="0">
                            <tr valign="top">
                                <td align="left" style="width:120px;">               
                                    <input  id="btnSkip" name="btnSkip" value=" 跳转 " onclick="return myToorBar_btnItemClick('Skip');"class="wfcmd_btnStyle_dis" type="submit">      
                                </td>
                                <td align="left">
                                    ${cboProcess} &nbsp;接收状态&nbsp;&nbsp;
                                    ${cboSkipGhid}&nbsp;接收人
                                </td>    
                            </tr>
                          </table>
                       </div>
                       <div id="plUpdate"  style="display:block;padding-top:15px">
           	              <table  width="60%" border="0" cellpadding="0" cellspacing="0">
                            <tr valign="top" >
                              <td align="left" style="width:120px;">               
                                   <input  id="btnUpdate" name="btnUpdate" value=" 变更 " onclick="return myToorBar_btnItemClick('Update');"class="wfcmd_btnStyle_dis" type="submit">   
                              </td>
                                <td align="left">
                                    ${txtExpDate} &nbsp;超时日期&nbsp;&nbsp;
                                    ${cboUpdateGhid}&nbsp;接收人
                                </td>
                            </tr>                            
                          </table>                           
                       </div>
                       <div id="plFinish" style="display:block;" >
           	              <table  width="60%" border="0" cellpadding="0" cellspacing="0">
                            <tr valign="top">
                                <td align="left" style="width:120px;">       
                                  <input name="btnComplete" value=" 完成 " onclick="return myToorBar_btnItemClick('Complete');" id="btnComplete" class="wfcmd_btnStyle_dis" type="submit">
                               </td>
                                <td align="left">
                                </td>
                            </tr>
                          </table> 
                       </div>
                   </fieldset>
             </div>     
           </div>
         </div>
  	     <div id="prompt_msg" style="color:#3366CC;" ><span id="lblRem">${lblRem }</span></div>
    </div>
    </form>
</body>
</html>


