<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>设定工单状态</title>
        <%@ include file="/ut_controls/PageHeaderMeta.jspf" %>
    <style type="text/css">
        
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
 	        <%--<asp:Panel ID="plInfo_case" Height="100%" Width="100%" ></asp:Panel> --%>
 	         <div id="plInfo_case"  style="height:100%;width:98%;">${plInfo_case}</div>		 
         </div>
         <div id="divProcess">
            <asp:Panel ID="plProcess" Height="40px" Width="100%" ></asp:Panel>
         </div>
         <div id="divHandle">
            <asp:Panel ID="plHandle"  Height="0px" Width="100%">
              <br />
             <asp:Panel ID="plSend"  Enabled="true" Visible="true" >
                 <fieldset style="height:auto; width:80%; text-align:centert; border: 1px solid Silver;">
                    <legend style="text-align: left;font-size:11pt">变更状态</legend>
                       <asp:Panel ID="plSubmit"  Enabled="true" Visible="true" >
           	              <table  width="60%" border="0" cellpadding="0" cellspacing="0"  style="padding-top:15px">
                            <tr >
                                <td align="left"  style="width:25%;">               
                                   <%--<asp:Button ID="btnSubmit" CssClass="wfcmd_btnStyle_dis" Text=" 提交 " onclick="btnSubmit_Click"   />--%>
                                   <input name="btnSubmit" value=" 提交 " onclick="return myToorBar_btnItemClick('Submit');" id="btnSubmit" class="wfcmd_btnStyle_dis" type="submit">      
                                </td>
                                <td align="left">
                                   <asp:DropDownList ID="cboSubmitGhid"  Height="24px" Width="155px" Font-Size="10pt">
                                   </asp:DropDownList>
                                    &nbsp;下一状态-接收人
                                </td>
                            </tr>  
                          </table>
                       </asp:Panel><br />
                       <asp:Panel ID="plBackward"  Enabled="true" Visible="true" >
           	              <table  width="60%" border="0" cellpadding="0" cellspacing="0">
                            <tr valign="top">
                                <td align="left" style="width:25%;">               
                                  <%--<asp:Button ID="btnBackward" CssClass="wfcmd_btnStyle_dis" Text=" 退回 " onclick="btnBackward_Click"   />--%>
                                  <input name="btnBackward" value=" 退回 " onclick="return myToorBar_btnItemClick('Backward');" id="btnBackward" class="wfcmd_btnStyle_dis" type="submit">
                                </td>
                                <td align="left">
                                    <asp:DropDownList ID="cboProcess_back"  Height="24px" Width="155px" Font-Size="10pt" AutoPostBack="True" onselectedindexchanged="cboProcess_back_SelectedIndexChanged"> 
                                    </asp:DropDownList>&nbsp;接收状态<br />
                                    
                                    <asp:DropDownList ID="cboBackGhid"  Height="24px" Width="155px" Font-Size="10pt">
                                    </asp:DropDownList>&nbsp;接收人
                                </td>
                            </tr>  
                          </table>
                       </asp:Panel><br />
                       <asp:Panel ID="plSkip"  Enabled="true" Visible="true" >
           	              <table  width="60%" border="0" cellpadding="0" cellspacing="0">
                            <tr valign="top">
                                <td align="left" style="width:25%;">               
                                  <%--<asp:Button ID="btnSkip" CssClass="wfcmd_btnStyle_dis" Text=" 跳转 " onclick="btnSkip_Click"   />--%>
                                  <input name="btnSkip" value=" 跳转 " onclick="return myToorBar_btnItemClick('Skip');" id="btnSkip" class="wfcmd_btnStyle_dis" type="submit">
                                </td>
                                <td align="left">
                                    <asp:DropDownList ID="cboProcess"  Height="24px" Width="155px" Font-Size="10pt" AutoPostBack="True" onselectedindexchanged="cboProcess_SelectedIndexChanged">
                                    </asp:DropDownList>&nbsp;接收状态<br />
                                    
                                    <asp:DropDownList ID="cboSkipGhid"  Height="24px" Width="155px" Font-Size="10pt"> 
                                    </asp:DropDownList>&nbsp;接收人
                                </td>    
                            </tr>
                          </table>
                       </asp:Panel>
                       <asp:Panel ID="plUpdate"  Enabled="true" Visible="true"   style="padding-top:15px">
           	              <table  width="60%" border="0" cellpadding="0" cellspacing="0">
                            <tr valign="top" >
                                <td align="left" style="width:25%;">               
                                  <%--<asp:Button ID="btnUpdate" CssClass="wfcmd_btnStyle_dis" Text=" 变更 " onclick="btnUpdate_Click"   />--%>
                                  <input name="btnUpdate" value=" 变更 " onclick="return myToorBar_btnItemClick('Update');" id="btnUpdate" class="wfcmd_btnStyle_dis" type="submit">                                 
                               </td>
                                <td align="left">
                                    <asp:TextBox ID="txtExpDate" Width="147px"  Height="18px" Font-Size="10pt" AutoPostBack="True"></asp:TextBox>&nbsp;超时日期<br />
                                    <asp:DropDownList ID="cboUpdateGhid"  Height="24px" Width="155px" Font-Size="10pt"> 
                                    </asp:DropDownList>&nbsp;接收人 
                                </td>
                            </tr>                            
                          </table>                           
                       </asp:Panel>
                        <asp:Panel ID="plFinish"  Enabled="true" Visible="true" >
           	              <table  width="60%" border="0" cellpadding="0" cellspacing="0">
                            <tr valign="top">
                                <td align="left" style="width:25%;">       
                                 <%--<asp:Button ID="btnComplete" CssClass="wfcmd_btnStyle_dis" Text=" 完成 " onclick="btnComplete_Click"   />--%>
                                  <input name="btnComplete" value=" 完成 " onclick="return myToorBar_btnItemClick('Complete');" id="btnComplete" class="wfcmd_btnStyle_dis" type="submit">
                               </td>
                                <td align="left">
                                   
                                </td>
                            </tr>
                            
                          </table> 
                          
                       </asp:Panel>
                       
                   </fieldset>
             </asp:Panel>     
           </asp:Panel>
         </div>
  	     <div id="prompt_msg" style="color:#3366CC;" ><asp:Label ID="lblRem"  Text=""></asp:Label></div>
    </div>
    </form>
</body>
</html>


