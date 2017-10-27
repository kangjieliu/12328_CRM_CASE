<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
<%@ include file="/ut_controls/PageHeaderMeta.jspf"%>
<link href="./style_query/treeview.css" rel="stylesheet" type="text/css">
<script type="text/javascript" language="javascript">
	$(document)
			.ready(
					function() {
						var caseType=fun_querystring("casetype");
						var caseid=fun_querystring("caseid");
						var nType=fun_querystring("nType");
						var table=fun_querystring("table");		
						var Url_para ="caseid="+caseid+"&casetype="+caseType+"&table="+table;
						
						var strUrl = rootURL
								+ "/ut_case/case_transto_left_tree_print.aspx?"+Url_para;
						$('#divContent').load(strUrl);
					});

	var m_SelectedNode = null;

	//点击节点，在主窗口显示，strTitle：标题  strUrl：连接地址  strNodeId：节点号（tnNode.Value）
	function OnTreeNodeClick(strNodeId, strTitle, strUrl) {
		//SelectNode($("table a span[id=" + strNodeId+"]"));
		if (strNodeId !== "root") {//如果是root不改变样式,否则太难看  
			SelectNode($("#" + strNodeId)); //取出 Span
		}

		var frmMain = parent.frm_right_telbook_internal;
		if (frmMain == null)
			return;
		if (strUrl == "#")
			return;
		frmMain.document.location = strUrl;
	}

	//选择节点，修改背景色
	function SelectNode(node) {
		//if(node=="#root") return;//add by zhangyr 20161207 主节点点击不改变样式,改变后太难看了

		if (node == null)
			return;
		if (m_SelectedNode != null)
			m_SelectedNode.attr("class", ""); //去掉上一次选择的节点的选择样式
		node.attr("class", "TreeView_SelectedNodeStyle"); //给新选择的节点加上选择样式
		m_SelectedNode = node;
	}

	// 树展开收缩方法 
	function Treeview_ExpandCollapse(nodeId) {
		var ul = $(nodeId).next();
		if (!ul.length || ul[0].tagName.toLowerCase() != "ul")
			return;
		if (ul[0].children.length < 1) //未加载子节点
			TreeView_PopulateNode(ul[0].id);
		ul.toggle();
		$(nodeId).toggleClass("open");
	}

	//展开子节点 
	function TreeView_PopulateNode(nodeId) {
		var strUrl = rootURL
				+ "/ut_case/case_transto_left_tree_print.aspx?strNodeId="
				+ nodeId;
		$('#' + nodeId).load(strUrl);
	}
</script>
<style type="text/css">
body {
	background: #f9f9f9;
	height: 100%;
	font-size: 14px;
}

.myTreeView {
	text-decoration: none;
}

.myTreeView td {
	height: 28px;
	line-height: 28px;
}

.myTreeView td a {
	text-decoration: none;
	color: #444;
}

.myTreeView td a:hover {
	font-weight: 500;
	color: #2772C2;
}

.myTreeView td a span {
	border: 0px;
	cursor: pointer;
	padding: 5px 4px 4px 4px; /*行间距*/
}
</style>
</head>
<body>
	<form id="frmBody" method="post">     
		<div id="divBody" align="center">
			<table width="100%" cellspacing="0" cellpadding="0" border="0">
				<tr>
					<td colspan="2"></td>
				</tr>
				<tr>
					<td width="5px" valign="top"></td>
					<td style="padding-right: 10px; vertical-align: top;">
						<div id="divContent" align="left">
							<%--<div id="tvList" style="height: 100%; width: 98%;">${tvList}</div> --%>
							<div id="divContent" align="left" class="myTreeView "></div>
						</div>
					</td>
				</tr>
			</table>
		</div>
	</form>
</body>
</html>

