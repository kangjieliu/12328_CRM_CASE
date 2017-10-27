<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
<title>文本模板</title>
<%@ include file="/ut_controls/PageHeaderMeta.jspf"%>
<script type="text/javascript" language="javascript">
	function onSelect(strRet) {
		var dgRet = frameElement.lhgDG;
		dgRet.curWin.onReceive_dialog_response(strRet);
		dgRet.cancel();
	}
	function btnToolBarClick(strName) {
		var bReturn = false;
		if (strName == "Query") {
			$("#divQuery").toggle();
			if ($("#hidn_nQuery").val() == 1)
				$("#hidn_nQuery").val(0)
			else
				$("#hidn_nQuery").val(1)
		} else if (strName == "Delete") {
			if (confirm("确定要删除所选模板吗？") == true) {
				bReturn = true;
			}
		} else if (strName == "AddNew") {
			window.location.href = "case_template_edit.aspx?cmd=AddNew";
			return false;
		}
		return bReturn;
	}
	function open_view(strUrl) {
		window.location.href = strUrl;
	}
	function mySearch_FieldLinkClicked(strName) {
		if (strName == "QuickSearch") {
			var strNm = $("#NAME").val();
			var strCn = $("#CONTENT").val();
			return true;
		}
	}
</script>

</head>
<body>
	<form id="frmBody" method="post">
		<div id="divCommand">
			<div id="plCommand" style="width: 100%; height: auto;">${plCommand}</div>
		</div>
		<div id="divQuery" style="display: none;">
			<div id="divSearch" style="display: block;">
				<div id="plSeach" style="width: 100%;">${plSeach}</div>
			</div>
			<div id="divSearch_custom" style="display: none;">
				<div id="plEdit" Width="100%">${plEdit}</div>
			</div>
		</div>
		<div id="divContent">
			<div>${dgvList}</div>
		</div>
	</form>
</body>
</html>
