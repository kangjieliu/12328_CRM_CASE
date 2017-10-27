package com.CallThink.ut_service.admin;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.pmAgent_info;
import com.CallThink.base.pmClass.pmSys;
import com.CallThink.base.pmCode.UltraCRM_Page;
import com.ToneThink.DataTable.DataRow;
import com.ToneThink.DataTable.DataTable;
import com.ToneThink.ctsTools.dbHelper.DbaseType;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.myString;
import com.ToneThink.ctsTools.myUtility.pmList;
import com.ToneThink.ctsTools.myUtility.pmMap;
import com.ToneThink.ctsTools.myUtility.pmRet;
import com.ToneThink.web.filter.HttpContext;

public class ghid_skill_left_tree extends UltraCRM_Page{
	private String pTableName = "DICT_ORG_CODE";

	private String themeURL = "";
	private String m_treeId = "";

	// [begin] 显示目录内容
	private HttpServletRequest Request;
	pmAgent_info pmAgent;
	private String pOp="";
	
	public ghid_skill_left_tree(int nType) {
		Request = HttpContext.Request();
		pmAgent = fun_main.GetParm();
		themeURL = Functions.getSession("themeURL");
		
		   if (IsPostBack == false)//正被首次加载和访问
           {
               HashMap htQuery;
               pmMap pm = fun_main.QuerySplit(Request);
               int rc=pm.nRet;
               htQuery=pm.htRet;
               if (rc > 0)
               {
                   pOp = Functions.ht_Get_strValue("cmd", htQuery);
               }
               Save_vs("pOp", pOp);
           }
           else
           {
               pOp = Load_vs("pOp");
           }
	}

	// 生成根目录
	public String getTreeView_root (String strHolder)
	{
		String strNodeName = "全部";

		String strNewUrl = myString.Format("ghid_skill_right.aspx?cmd=Edit&key=root&from=menu");
		 if(pOp.equals("pos"))
             strNewUrl = myString.Format("engr_pos.aspx?cmd=Edit&key=root&from=menu");
		 
		String strText = myString.Format("<span onclick=\"OnTreeNodeClick('{0}','{1}','{2}')\">{3}</span>", "root", strNodeName, strNewUrl, strNodeName);

		StringBuilder sbHtml = new StringBuilder();
		sbHtml.append(myString.Format("<ul id=\"{0}\" class=\"navBox\">", strHolder));
		sbHtml.append(myString.Format("<li class=\"no\"><a class=\"root\" id=\"root\" href=\"{0}\">{1}</a></li>", "#", strText));
		DataTable dtRet = TabMenu();// .dt_GetTable(pTableName,strFileter,pmSys.conn_crm);
		for(DataRow row : dtRet.Rows())
		{
			sbHtml.append(getNodeHtml(row, 1));
		}
		sbHtml.append("</ul>");
		return sbHtml.toString();
	}

	// 生成节点=strNodeId 的子目录
	public String getTreeView_child (String strNodeId)
	{
		StringBuilder sbHtml = new StringBuilder();
		strNodeId = Functions.Substring(strNodeId, "nodes_", "");
		DataTable dtRet = SubMenu(strNodeId, "");
		for(DataRow row : dtRet.Rows())
		{
			sbHtml.append(getNodeHtml(row, 2));
		}
		return sbHtml.toString();
	}

	// 每个父节点生成一个alink，子节点放在ul下
	private String getNodeHtml (DataRow row, int nLevel)
	{
		String strReturn = "";

		boolean bHasChilds = false;

		String strNewUrl, strTarget;
		String strNodeName, strNodeID, strTemp;
		int nCnt_childNode;

		strNodeID = Functions.atos(row.getValue("GROUPS"));
		strNodeName = Functions.atos(row.getValue("GNAME"));
		nCnt_childNode = Functions.atoi(row.getValue("nCNT"));
		if(nCnt_childNode > 0) bHasChilds = true;

		pmRet<Integer, String[]> res = fun_CreateNode_NavigateUrl(strNodeID,bHasChilds);
		// if(res.nRet>1) bHasChilds =true;
		strNewUrl = res.oRet[0];
		strTarget = res.oRet[1];
		strNewUrl = myString.Format("OnTreeNodeClick('{0}','{1}','{2}')", strNodeID, strNodeName, strNewUrl);

		int nFind = 0;
		StringBuilder mNode = new StringBuilder();
		mNode.append(myString.Format("<li id=\"{0}\" class=\"curbg\" style=\"border:none;\">", strNodeID));
		
		if(nLevel == 1) // root
		{
			if(bHasChilds) // 目录树，下面包含子节点，点击时折叠
			{
				strTemp = myString.Format("<a class=\"folder\" onclick=\"Treeview_ExpandCollapse(this)\" href=\"javascript:{0}\"> " + "<span class=\"icon\"></span>" + "{1} </a>", strNewUrl, strNodeName);
			} else // 目录叶，下面没有子节点
			{
				strTemp = myString.Format("<a id=\"{0}\" onclick=\"{1}\" href=\"#;\" title=\"{2}\"> " + "{2} </a>", strNodeID, strNewUrl, strNodeName);
			}
			mNode.append(strTemp);
			nFind = 1;
		} else
		{ // 2级以上
			if(bHasChilds) // 目录树，下面包含子节点，点击时折叠
			{
				strTemp = myString.Format("<a class=\"folder\" onclick=\"Treeview_ExpandCollapse(this)\" href=\"javascript:{0}\"> " + "{1} </a>", strNewUrl, strNodeName);
			} else // 目录叶，下面没有子节点
			{
				strTemp = myString.Format("<a id=\"{0}\" onclick=\"{1}\" href=\"#;\" title=\"{2}\"> " + "{2} </a>", strNodeID, strNewUrl, strNodeName);
			}
			mNode.append(strTemp);
			nFind = 1;
		}

		if(nFind == 0) return "";
		strReturn = mNode.toString();

		if(bHasChilds == true)
		{
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
	private DataTable TabMenu ()
	{
		return SubMenu("root", "");
	}

	/// <summary>
	/// 读取子菜单项-二、三级目录
	/// </summary>
	/// <param name="strID">CRM_MENU_BS-ID</param>
	/// <param name="strText">CRM_MENU_BS-Text</param>
	/// <returns></returns>
	private DataTable SubMenu (String strID, String strText)
	{
		String strSql = "";
		String strFilter = "(REL_CODE='" + strID + "')";
         if (strID.equals("root")) strFilter = "(ORG_LEVEL=1)";
         
         strSql = myString.Format("SELECT ORG_CODE AS GROUPS,ORG_NAME AS GNAME,(SELECT COUNT(*) FROM {0} WHERE REL_CODE=OPGP.ORG_CODE) as nCNT FROM {0} OPGP WHERE {1} ORDER BY ORG_CODE", pTableName, strFilter);
         
         my_odbc pTable = new my_odbc(pmSys.conn_crm);
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
	private pmRet<Integer, String[]> fun_CreateNode_NavigateUrl (String strNodeId,boolean bHasChilds)
	{
		int nReturn = -1;
		String strNewUrl, strTarget;

		strNewUrl = "#";
		strTarget = "ghid_skill_right";
		strNewUrl = "ghid_skill_right.aspx?cmd=Edit&key=" + strNodeId + "&from=menu";
		  if (pOp.equals("pos") )
              strNewUrl = "engr_pos.aspx?cmd=Edit&key=" + strNodeId + "&from=menu";

		String[] mRet = new String[] { strNewUrl, strTarget };
		return new pmRet<Integer, String[]>(nReturn, mRet);
	}
}
