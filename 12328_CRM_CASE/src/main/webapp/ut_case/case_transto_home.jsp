<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>员工通信录</title>
<%@ include file="/ut_controls/PageHeaderMeta.jspf"%>
<%@ include file="/ut_controls/PageHeader_ui.jspf"%>
<script language="javascript" type="text/javascript">
   
	$(document).ready(function() {
		var caseType=fun_querystring("casetype");
		var caseid=fun_querystring("caseid");
		var nType=fun_querystring("nType");
		var table=fun_querystring("table");		
		var Url_para ="?caseid="+caseid+"&casetype="+caseType+"&table="+table;
				 
		//左侧树地址
		var Url_left=rootURL+"/ut_case/case_transto_left_tree.aspx"+Url_para ;
		document.getElementById("frm_left").src=Url_left; 
		//右侧页面地址
		var Url_right=rootURL+"/ut_case/case_transto_uid.aspx" + Url_para;
         if(nType ==1)
        	Url_right = rootURL+"/ut_case/case_transto_sms.aspx" + Url_para;
        else if (nType == 2)
        	Url_right = rootURL+"/ut_case/case_transto_email.aspx" + Url_para;
        else if (nType == 3) //add by gaoww 20151229 增加发送微信通知
        	Url_right = rootURL+"/ut_case/case_transto_wx.aspx" + Url_para;
         document.getElementById("frm_main").src=Url_right; 
		var clientHeight = document.documentElement.clientHeight + 430; //$(document).height();
		//alert(clientHeight);
		$("#frm_left").height(clientHeight);
		$("#frm_main").height(clientHeight);
	});
</script>

<style type="text/css">
html, body {
	background: #EFEFEF;
	height: 100%;
	overflow: auto;
}

.left {
	position: absolute;
	left: 0px;
	top: 0px;
	bottom: 0px;
	width: 180px;
}

.main {
	position: absolute;
	left: 180px;
	top: 0px;
	bottom: 0px;
	right: 0px;
}

.left iframe, .main iframe {
	width: 100%;
	height: 600px;
}
</style>
</head>
<body>
	<form id="frmBody">
		<div id="divBody" style="background: green;">
			<div class="left">
			 <iframe id="frm_left" name="frm_left_telbook_internal" src="${Url_left}" frameborder="no"></iframe>
			</div>
			<div class="main">
				<iframe id="frm_main" name="frm_right_telbook_internal" src="${Url_left}"  frameborder="no"></iframe>
			</div>
		</div>
	</form>
</body>
</html>