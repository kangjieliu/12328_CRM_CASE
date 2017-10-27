package com.CallThink.ut_case;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.pmAgent_info;
import com.CallThink.base.pmClass.pmSys;
import com.ToneThink.DataTable.DataRow;
import com.ToneThink.DataTable.DataTable;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.myString;
import com.ToneThink.ctsTools.myUtility.pmList;
import com.ToneThink.ctsTools.myUtility.pmMap;
import com.ToneThink.ctsTools.myUtility.pmRet;
import com.ToneThink.web.filter.HttpContext;
/**
 * 
 * @author Liukj
 * @date 20170914
 * @Description 行政划区管理页面 左边树
 */
@SuppressWarnings("unchecked")
public class qh_agent_left_tree {
	public int pType = 0;
	private String m_Url_right = "";
	private String pTableName = "DICT_JT_ORG";

	// [begin] 显示目录内容
	private HttpServletRequest Request;
	pmAgent_info pmAgent;

	public qh_agent_left_tree(int nType) {
		Request = HttpContext.Request();
		pmAgent = fun_main.GetParm();

		pmMap res = fun_main.QuerySplit(Request);
		int rc = res.nRet;
		if (rc > 0) {
			HashMap htQuery = res.htRet;
			pType = Functions.atoi(Functions.ht_Get_strValue("nType", htQuery));
		}
		String strUrl = "qh_agent_list.aspx";
		m_Url_right = myString.Format(strUrl);
	}

	// 生成根目录
	public String getTreeView_root(String strHolder) {
		String strNodeName = "全部";
		String strNewUrl = m_Url_right;
		String strText = myString.Format("<span onclick=\"OnTreeNodeClick('{0}','{1}','{2}')\">{3}</span>", "root",
				strNodeName, strNewUrl, strNodeName);
		StringBuilder sbHtml = new StringBuilder();
		sbHtml.append(myString.Format("<ul id=\"{0}\" class=\"navBox\">", strHolder));
		sbHtml.append(myString.Format("<li class=\"no\"><a class=\"root\" id=\"root\" href=\"{0}\">{1}</a></li>", "#",
				strText));
		DataTable dtRet = TabMenu();// .dt_GetTable(pTableName,strFileter,pmSys.conn_crm);
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
		DataTable dtRet = SubMenu(strNodeId, "");
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
		strNodeID = Functions.atos(row.getValue("ORG_CODE"));
		strNodeName = Functions.atos(row.getValue("ORG_NAME"));
		nCnt_childNode = Functions.atoi(row.getValue("nCNT"));
		if (nCnt_childNode > 1)
			bHasChilds = true;
		pmRet<Integer, String[]> res = fun_CreateNode_NavigateUrl(strNodeID, bHasChilds);
		strNewUrl = res.oRet[0];
		strTarget = res.oRet[1];
		strNewUrl = myString.Format("OnTreeNodeClick('{0}','{1}','{2}')", strNodeID, strNodeName, strNewUrl);
		int nFind = 0;
		StringBuilder mNode = new StringBuilder();
		mNode.append(myString.Format("<li id=\"{0}\" class=\"curbg\" style=\"border:none;\">", strNodeID));

		if (nLevel == 1) // root
		{
			// 目录叶，下面没有子节点
			strTemp = myString.Format("<a id=\"{0}\" onclick=\"{1}\" href=\"#;\" title=\"{2}\"> " + "{2} </a>",
					strNodeID, strNewUrl, strNodeName);
			mNode.append(strTemp);
			nFind = 1;
		}
		if (nFind == 0)
			return "";
		strReturn = mNode.toString();
		if (bHasChilds == true) {
			strReturn += myString.Format("<ul id=\"nodes_{0}\" style=\"display: none;\">", strNodeID);
			strReturn += "</ul>";
		}
		return strReturn;
	}

	// [end]
	/// <summary>
	/// 读取主菜单项-一级目录
	/// </summary>
	/// <returns></returns>
	private DataTable TabMenu() {
		return SubMenu("root", "");
	}

	/// <summary>
	/// 读取子菜单项-二、三级目录
	/// </summary>
	/// <param name="strID">CRM_MENU_BS-ID</param>
	/// <param name="strText">CRM_MENU_BS-Text</param>
	/// <returns></returns>//DOMAIN, DOMAIN_CODE
	private DataTable SubMenu(String strID, String strText) {
		String strSql = "";
		String strFilter = "";
		if (strID.equals("root"))
			strFilter = "ORG_LEVEL=3";
		my_odbc pTable = new my_odbc(pmSys.conn_crm);

		strSql = myString.Format(
				"SELECT ORG_CODE,ORG_NAME,COUNT(ORG_CODE) AS nCNT FROM {0} WHERE {1} ORDER BY ORG_CODE", pTableName,
				strFilter);

		pmList res = pTable.my_odbc_find(strSql, 0);
		pTable.my_odbc_disconnect();
		return res.dtRet;
	}

	/// <summary>
	/// 生成节点链接地址、目标窗口
	/// </summary>
	/// <param name="strNodeId">节点编号，对应“知识库类型编号”</param>
	/// <param name="strNewUrl">返回-NavigateUrl</param>
	/// <param name="strTarget">返回-目标窗口</param>
	/// <returns>-1：失败 0：没有记录 1：一条记录 >1：多条记录 </returns>
	private pmRet<Integer, String[]> fun_CreateNode_NavigateUrl(String strNodeId, boolean bHasChilds) {
		int nReturn = -1;
		String strNewUrl, strTarget;
		strNewUrl = "#";
		strTarget = "frm_right_telbook_internal";
		String strFilter = myString.Format("REL_CODE LIKE '{0}%'", strNodeId);

		my_odbc pTable = new my_odbc(pmSys.conn_crm);
		String strSql = myString.Format("SELECT TOP 1 ORG_CODE,ORG_LEVEL FROM DICT_JT_ORG WHERE {0}", strFilter);
		pmList res = pTable.my_odbc_find(strSql, 0);
		pTable.my_odbc_disconnect();
		if (res.nRet == 1) {
			DataTable dtRet = res.dtRet;
			String strOrg_level = Functions.dtCols_strValue(dtRet, 0, "ORG_LEVEL");
			strNewUrl = m_Url_right + "?pOrg_code=" + strNodeId + "& pOrg_level=" + strOrg_level;
			nReturn = dtRet.getCount();
		}
		String[] mRet = new String[] { strNewUrl, strTarget };
		return new pmRet<Integer, String[]>(nReturn, mRet);
	}
}
