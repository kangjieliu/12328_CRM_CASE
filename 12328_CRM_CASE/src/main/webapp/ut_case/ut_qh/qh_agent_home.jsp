<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>业务领域</title>
<%@ include file="/ut_controls/PageHeaderMeta.jspf"%>
<%@ include file="/ut_controls/PageHeader_ui.jspf"%>
<script language="javascript" type="text/javascript">
   
	$(document).ready(function() {
				 
		//左侧树地址
		var Url_left=rootURL+"/ut_case/ut_qh/qh_agent_left_tree.aspx" ;
		//var Url_left=rootURL+"/ut_case/case_business_left_tree.aspx"+Url_para ;
		document.getElementById("frm_left").src=Url_left; 
		//右侧页面地址
		var Url_right=rootURL+"/ut_case/ut_qh/qh_agent_list.aspx" ;
		//var Url_right=rootURL+"/ut_case/case_business_listaspx" + Url_para;
          
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