<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
    <title>工单资料列表</title>
      <%@ include file="/ut_controls/PageHeaderMeta.jspf" %>
      
      <script type="text/javascript" language="javascript">
        //peng 20140620 处理页面的查询功能
        $(function() {
            if ($("#hidn_nQuery").val() == 1)
                $("#divQuery").show();
            var nType_search = $("#hidn_nSearch").val();
            if (nType_search == 0) {
                $("#divSearch").show();
                $("#divSearch_custom").hide();
            }
            else {
                $("#divSearch").hide();
                $("#divSearch_custom").show();
            }
        });

        function btnToolBarClick(strName) {
            var bReturn = false;
            if (strName == "Query") {
                $("#divQuery").toggle();
                if ($("#hidn_nQuery").val() == 1)
                    $("#hidn_nQuery").val(0)
                else
                    $("#hidn_nQuery").val(1)
            }
            else if (strName == "ReturnQuickSearch") {
                $("#divSearch").show();
                $("#divSearch_custom").hide();
                $("#hidn_nSearch").val(0);
            }
            else if(strName=="AddNew")
            {
            	var pType = fun_querystring("ntype");
            	window.location.href="case_edit.aspx?cmd=AddNew&ntype="+pType+"&caseid_rel=";
                return false;
            }
            else if(strName=="Delete")
            {
            	if (confirm("确实要删除所选工单资料吗？") == true) {
                    bReturn = true;
            	}       
            }
            else if(strName =="Query_Save")
            {
            	   var strTemp = $("#SDATE_CUSTOM").val();
                   strPattern = /^\d{4}-\d{2}-\d{2}$/;
                   if ((strTemp.length > 0) && (strTemp.search(strPattern) == -1)) {
                       alert("您输入的开始日期有误，请重新输入！");
                       return false;
                   }

                   strTemp = $("#EDATE_CUSTOM").val();
                   if ((strTemp.length > 0) && (strTemp.search(strPattern) == -1)) {
                       alert("您输入的结束日期有误，请重新输入！");
                       return false;
                   }

                   var sdate = $("#SDATE_CUSTOM").val();
                   var edate = $("#EDATE_CUSTOM").val();
                   if (sdate > edate) {
                       alert("您输入结束日期小于开始日期，请重新输入！")
                       return false;
                   }
                   bReturn = true;
            }
            else bReturn = true;
            return bReturn;
        }
        
        function open_view(strUrl)
        {
        	window.location.href=strUrl;            
        }

        function mySearch_FieldLinkClicked(strName) {
            if (strName == "QuickSearch") {
                var strTemp = $("#SDATE").val();
                strPattern = /^\d{4}-\d{2}-\d{2}$/;
                if ((strTemp.length > 0) && (strTemp.search(strPattern) == -1)) {
                    alert("您输入的开始日期有误，请重新输入！");
                    return false;
                }

                strTemp = $("#EDATE").val();
                if ((strTemp.length > 0) && (strTemp.search(strPattern) == -1)) {
                    alert("您输入的结束日期有误，请重新输入！");
                    return false;
                }

                var sdate = $("#SDATE").val();
                var edate = $("#EDATE").val();
                if (sdate > edate) {
                    alert("您输入结束日期小于开始日期，请重新输入！")
                    return false;
                }

//delete by gaoww 20140804 可以只输入查询日期范围，不输入查询条件
//                strTemp = $("#UNAME").val();
//                strPattern = /^.+$/;
//                //if ((strTemp.length > 0) && (strTemp.search(strPattern) == -1)) {
//                if (strTemp.search(strPattern) == -1) {
//                    alert("请输入查询条件！");
//                    return false;
//                }
               return true;
            }
            else if (strName == "Reset") {
                $("#SDATE_SDATE").val("");
                $("#EDATE_EDATE").val("");
                //$("#TEL").val("");
                //$("#CASENAME").val("");
                $("#UNAME").val("");
                return false;
            }
            else if (strName == "CustomSearch") {
                $("#divSearch").hide();
                $("#divSearch_custom").show();
                $("#hidn_nSearch").val(1);
                return false;
            }   
           return true;
        }            
    </script>

</head>
<body>
     <form id="frmBody"  method="post">     
         <div id="divCommand">
                <div id="plCommand" style="width:100%;height:auto;">${plCommand}</div>
	     </div>
         <div id="divQuery" style="display: none;">
	          <div id="divSearch" style="display: block;">
	            <div id="plSeach" style="width:100%;">${plSeach}</div>
	          </div>  
	           <div id="divSearch_custom" style="display: none;">
	              <div id="plCommand"  Height="20px" Width="100%">${plCommand_custom}</div>
                  <div id="plEdit"  Width="100%">${plEdit}</div>
	          </div>
       </div>
        <div id="divContent">
            <div>${dgvList}</div>
        </div>
        <%--
     <input id="hidn_nQuery" type="hidden" size="1" name="hidn_nQuery" value="0" runat="server" />
     <input id="hidn_nSearch" type="hidden" size="1" name="hidn_nSearch" value="0" runat="server" />--%>
    </form>
</body>
</html>
