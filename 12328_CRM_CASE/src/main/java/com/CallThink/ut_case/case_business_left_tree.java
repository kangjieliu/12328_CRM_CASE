package com.CallThink.ut_case;

import javax.servlet.http.HttpServletRequest;

import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.pmAgent_info;
import com.CallThink.base.pmClass.pmSys;
import com.CallThink.base.pmCode.UltraCRM_Page;
import com.ToneThink.DataTable.DataRow;
import com.ToneThink.DataTable.DataTable;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.myString;
import com.ToneThink.ctsTools.myUtility.pmRet;
import com.ToneThink.web.filter.HttpContext;
/**
 * 
 * @author Liukj
 * @date 20171023
 * @Description 业务领域管理 左边树
 */
public class case_business_left_tree extends UltraCRM_Page{
	public int pType = 0;

	private String pTableName = "CRM_DICT_BUSINESS";
	
	// [begin] 显示目录内容
	private HttpServletRequest Request;
	pmAgent_info pmAgent;
//	String strPnode ="";
	
	public case_business_left_tree(String nType) {
		Request = HttpContext.Request();
		pmAgent = fun_main.GetParm();
	}

	// 生成根目录
	public String getTreeView_root(String strHolder) {
		
		String strNewUrl = "case_business_list.aspx";
		String strText = myString.Format("<span onclick=\"OnTreeNodeClick('{0}','{1}','{2}')\">{3}</span>", "root",
				"桂林交通管理局", strNewUrl, "桂林交通管理局");
		StringBuilder sbHtml = new StringBuilder();
		sbHtml.append(myString.Format("<ul id=\"{0}\" class=\"navBox\">", strHolder));
		sbHtml.append(myString.Format("<li class=\"no\"><a class=\"root\" id=\"root\" href=\"{0}\">{1}</a></li>", "",
				"桂林交通管理局"));
		DataTable dtRet = SubMenu("root");
		for (DataRow row : dtRet.Rows()) {
			sbHtml.append(getNodeHtml(row, 1));
		}
		sbHtml.append("</ul>");
		return sbHtml.toString();
	}

	// 生成节点=strNodeId 的子目录
	public String getTreeView_child(String strNodeId) {
		StringBuilder sbHtml = new StringBuilder();
		strNodeId = Functions.Substring(strNodeId, "nodes_", "");
		DataTable dtRet = SubMenu(strNodeId);
		for (DataRow row : dtRet.Rows()) {
			sbHtml.append(getNodeHtml(row, 2));
		}
		return sbHtml.toString();
	}

	// 每个父节点生成一个alink，子节点放在ul下
	private String getNodeHtml(DataRow row, int nLevel) {
		String strReturn = "";

		boolean bHasChilds = false;

		String strNewUrl, strTarget;
		String strNodeName, strNodeID, strTemp;
		int nCnt_childNode;
		int nFind = 0;
		StringBuilder mNode = new StringBuilder();

		strNodeID = Functions.atos(row.getValue("TYPEID"));
		strNodeName = Functions.atos(row.getValue("KNAME"));
//		strPnode = Functions.atos(row.getValue("PNODE"));
		nCnt_childNode = Functions.atoi(row.getValue("RET_CNT"));
		if (nCnt_childNode > 0)
			bHasChilds = true;
		
		pmRet<Integer, String[]> res = fun_CreateNode_NavigateUrl(strNodeID);
		// if(res.nRet>1) bHasChilds =true;
		strNewUrl = res.oRet[0];
		strTarget = res.oRet[1];
		strNewUrl = myString.Format("OnTreeNodeClick('{0}','{1}','{2}')", strNodeID, strNodeName, strNewUrl);
		mNode.append(myString.Format("<li id=\"{0}\" class=\"curbg\" style=\"border:none;\">", strNodeID));
		if (nLevel == 1) // root
		{
			if (bHasChilds) // 目录树，下面包含子节点，点击时折叠
			{
				strTemp = myString
						.Format("<a class=\"folder\" onclick=\"Treeview_ExpandCollapse(this)\" href=\"javascript:{0}\"> "
								+ "<span class=\"icon\"></span>" + "{1} </a>", strNewUrl, strNodeName);
			} else // 目录叶，下面没有子节点
			{
				strTemp = myString.Format("<a id=\"{0}\" onclick=\"{1}\" href=\"#;\" title=\"{2}\"> " + "{2} </a>",
						strNodeID, strNewUrl, strNodeName);
			}
			mNode.append(strTemp);
			nFind = 1;
		} else { // 2级以上
			
			if (bHasChilds) // 目录树，下面包含子节点，点击时折叠
			{
//				strTemp = myString.Format("<a class=\"folder\" onclick=\"Treeview_ExpandCollapse(this)\" href=\"javascript:{0}\"> " + "<span class=\"icon\"></span>" + "{1} </a>", strNewUrl, strNodeName);
				strTemp = myString.Format("<a class=\"folder\" onclick=\"Treeview_ExpandCollapse(this)\" style=\"margin-left: 15px;\"  href=\"javascript:{0}\"> " + "{1} </a>", strNewUrl, strNodeName);
			} else // 目录叶，下面没有子节点
			{
//				strTemp = myString.Format("<a id=\"{0}\" onclick=\"{1}\" href=\"#;\"  title=\"{2}\"> " + "{2} </a>", strNodeID, strNewUrl, strNodeName);
				strTemp = myString.Format("<a id=\"{0}\" onclick=\"{1}\" href=\"#;\" style=\"margin-left: 15px;\" title=\"{2}\"> " + "{2} </a>", strNodeID, strNewUrl, strNodeName);
			}
			mNode.append(strTemp);
			nFind = 1;
		}
			
		if (nFind == 0)
			return "";
		strReturn = mNode.toString();

		if (bHasChilds == true) {
			strReturn += myString.Format("<ul id=\"nodes_{0}\" style=\"display: none; \"  >", strNodeID);
			strReturn += "</ul>";
		}
		return strReturn;
	}
	// [end]

	/// <summary>
	/// 读取子菜单项-二、三级目录
	/// </summary>
	/// <param name="strID">CRM_MENU_BS-ID</param>
	/// <param name="strText">CRM_MENU_BS-Text</param>
	/// <returns></returns>//DOMAIN, DOMAIN_CODE
	private DataTable SubMenu(String strID) {
		String strSql = "";

		strSql = myString.Format("SELECT TYPEID,KNAME,PNODE,(SELECT COUNT(*) FROM {0} WHERE PNODE=a.TYPEID) AS RET_CNT FROM {0} a WHERE PNODE='{1}';", pTableName, strID);

		DataTable dtRet = Functions.dt_GetTable(strSql, "", pmSys.conn_crm);
		
		return dtRet;
	}

	/// <summary>
	/// 生成节点链接地址、目标窗口
	/// </summary>
	/// <param name="strNodeId">节点编号，对应“知识库类型编号”</param>
	/// <param name="strNewUrl">返回-NavigateUrl</param>
	/// <param name="strTarget">返回-目标窗口</param>
	/// <returns>-1：失败 0：没有记录 1：一条记录 >1：多条记录 </returns>
	private pmRet<Integer, String[]> fun_CreateNode_NavigateUrl(String strNodeId) {		int nReturn = -1;
		String strNewUrl, strTarget = "";
		String strPnode ="";
		
		if(strNodeId.indexOf("-")>=0)
			strPnode = strNodeId.substring(0, strNodeId.lastIndexOf("-"));
		else 
			strPnode="root";
		
		strNewUrl = myString.Format("/ut_case/case_business_list.aspx?NodeId={0}&Pnode={1}", strNodeId, strPnode);
		///ut_case/case_business_list.aspx?NodeId=02&Pnode=root
		String[] mRet = new String[] { strNewUrl, strTarget };
		return new pmRet<Integer, String[]>(nReturn, mRet);
	}

}
