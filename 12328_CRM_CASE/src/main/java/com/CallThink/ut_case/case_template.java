package com.CallThink.ut_case;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.Model;

import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.pmSys;
import com.CallThink.base.pmCode.UltraCRM_Page;
import com.ToneThink.ctsTools.WebUI.my_Field;
import com.ToneThink.ctsTools.WebUI.my_SearchField;
import com.ToneThink.ctsTools.WebUI.my_ToolStrip;
import com.ToneThink.ctsTools.WebUI.my_dataGrid;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.myString;
import com.ToneThink.ctsTools.myUtility.pmMap;
/**
 * 
 * @author Liukj
 * @date 20170914
 * @Description 设定模板页
 */
public class case_template extends UltraCRM_Page {
	private String pTableName = "CRM_TEMPLATE";// 选择文本模板
	private String m_Filter_search = ""; // 本页面人工选择的查询条件，会话需要保存在Session中
	my_SearchField mySearch = new my_SearchField(3);
//	private String pContent = "";
	private String pMenu_id = "";
	private String pCaseId = "";
	private String pKey = "";//add by xutt 20171012
	my_dataGrid mydg = new my_dataGrid(51);
	my_ToolStrip myToolBar = new my_ToolStrip();
	my_Field myFld_Query_custom = new my_Field(3);
//	HttpSession session = Request.getSession();

	public void Page_Load(Object sender, Model model) {
		pmAgent = fun_main.GetParm();
		if (IsPostBack == false)// 正被首次加载和访问
		{
			int nHistory = 0;
			pmMap res = fun_main.QuerySplit(Request);
			int rc = res.nRet;
			if (rc > 0) {
				HashMap htQuery = res.htRet;
				nHistory = Functions.atoi(Functions.ht_Get_strValue("history", htQuery));
				pMenu_id = Functions.ht_Get_strValue("menu_id", htQuery);
				pCaseId = Functions.ht_Get_strValue("caseId", htQuery);
				pKey = Functions.ht_Get_strValue("key", htQuery);
				if (nHistory > 0) // 编辑页面返回
				{
					String strFilter_temp = Load_ss("pFilter_search");
					if (strFilter_temp.length() > 0)
						m_Filter_search = strFilter_temp;
					mydg.nRestore_history = 1;
				}
			}
			Save_vs("pMenu_id", pMenu_id);
			Save_ss("pFilter_search", m_Filter_search);
			Save_ss("pCaseId", pCaseId);
			Save_vs("pKey" , pKey);  
		} else {
			pMenu_id = Load_vs("pMenu_id");
			m_Filter_search = Load_ss("pFilter_search");
			pCaseId = Load_ss("pCaseId");
			pKey = Load_vs("pKey" ); //add by 20170914
		}
		InitToolbar();
		Fillin_SearchField();
		Fillin_grid();

		Fillin_SearchField_custom();
		myToolBar.render(model);
		mySearch.render(model);
		mydg.render(model);
	}

	private void InitToolbar() {
		myToolBar.fill_fld("Separator", "Separator1", 0, 3);
		myToolBar.fill_fld("查询", "Query", 0, 10);
		myToolBar.fill_fld("Separator", "Separator2", 0, 3);
		if (!pMenu_id.isEmpty()) {
			myToolBar.fill_fld("增加", "AddNew", 0, 10);
			myToolBar.fill_fld("删除", "Delete", 0, 10);
		} else {
			myToolBar.fill_fld("确认添加", "Add");
			myToolBar.fill_fld(fun_main.Term("LBL_CANCEL"), "Cancel", "return fun_close('" + pCaseId + "')");
		}
		myToolBar.fill_toolStrip("plCommand");
		myToolBar.btnItemClick = this;
	}

	public void myToolBar_btnItemClick(Object sender, String name, String parms, int nparms) {
		String strFilter = "";
		if (name.equals("Delete")) {
			List<String> alSelect = mydg.GetSelectedKey("NAME");
			if (alSelect.size() <= 0) {
				Functions.MsgBox("请先选中要删除的记录！");
				return;
			}
			int nCnt = 0;
			for (int index = 0; index < alSelect.size(); index++) {
				String strId = alSelect.get(index);
				my_odbc pTable = new my_odbc(pmSys.conn_crm);
				int rc = pTable.my_odbc_delete(pTableName, "NAME='" + strId + "'");
				if (rc > 0)
					nCnt++;
				pTable.my_odbc_disconnect();
			}
			if (nCnt > 0) {
				mydg.refresh("NAME", GetFilter_search() + m_strOrder_by());
				Functions.MsgBox("记录删除成功！");
			}
		} else if (name.equals("Add")) {
			List<String> contentList = mydg.GetSelectedKey("CONTENT");
			for (String content : contentList) {
				strFilter += content;
			}
			strFilter = strFilter.replaceAll("\r\n", "|");
			// Json
			strFilter = myString.Format("[{Idlg:'{0}',ucontent:'{1}',type:'CONTENT',key:'{2}'}]", pCaseId, strFilter,pKey);
			Functions.js_exec("onSelect(" + strFilter + ")");
		}
	}

	private void Fillin_SearchField() {// 查询框
		mySearch.SetWidth(pmAgent.content_width - 200);
		mySearch.SetMaxLabelLenth(80);
		mySearch.SetMaxLabelLenth_col2(60);
		mySearch.funName_OnClientClick("mySearch_FieldLinkClicked");
		mySearch.fill_fld("模板名称", "NAME", 50, 0, true, true);
		mySearch.fill_fld("模板内容", "CONTENT", 50, 0, true, true);
		ArrayList alButton_ex = new ArrayList();
		mySearch.fill_fld_button("查询", "QuickSearch", null, true, alButton_ex, "left");
		mySearch.fill_Panel("plSeach");
		mySearch.FieldLinkClicked = this;
	}

	// 填充列表
	private void Fillin_grid() {
		int i = 0;
		mydg.SetConnStr(pmSys.conn_crm);
		mydg.SetTable(pTableName);
		mydg.SetPageSize(pmAgent.page_maxline - 2);
		mydg.fill_fld(i++, "", "SELECT", 4, 9, "radio");
		mydg.fill_fld(i++, "模板名称", "NAME", 22, 8, "CMDNAME=Link;NULLAS=[null]");// 模板名称
		mydg.fill_fld(i++, "模板内容", "CONTENT", 20);// 模板内容
		mydg.fill_header("dgvList", "AUTOID", GetFilter_search() + m_strOrder_by());
		mydg.RowDataFilled = this;
	}

	@Override
	public void mydg_RowDataFilled(Object sender, int rows) {
		if (rows < 0)
			return; // 表头行，不处理
		int nCol = mydg.get_idx("NAME");
		String strName = mydg.get_cell(rows, "NAME");
		if (nCol >= 0 && !pMenu_id.isEmpty()) {
			String strUrl = myString.Format("case_template_edit.aspx?pTempName={0}", strName);
			String strHtml = myString.Format("<a href='#this' onclick=\"open_view('{0}','{1}');\">{1}</a>", strUrl,
					strName);
			mydg.set_cell(rows, nCol, strHtml);
		}
	}

	// 根据输入的参数查询 查询sql拼接
	private String GetFilter_search() {
		String strname = mySearch.get_item_value("NAME");
		String strcontent = mySearch.get_item_value("CONTENT");
		String sName = StringUtils.trimToNull(strname);
		String sContent = StringUtils.trimToNull(strcontent);
		String strFilter_search = "( ";
		if (sName == null && sContent == null) {
			return "";
		}
		if (sName != null) {
			strFilter_search += " NAME like '%" + sName + "%' ";
		}
		if (sName == null && sContent != null) {
			strFilter_search += "CONTENT like '%" + sContent + "%'";
		} else {
			strFilter_search += "OR CONTENT like '%" + sContent + "%'";
		}
		strFilter_search += ")";
		return strFilter_search;
	}

	public void mySearch_FieldLinkClicked(Object sender, String name, String parms, int nparms, int ntype) {
		if (name.equals("QuickSearch")) {
			String strFilter_search = GetFilter_search();
			if (StringUtils.isBlank(strFilter_search)) {
				mydg.refresh("AUTOID", m_strOrder_by());
				mydg.Goto_NewPageIndex(1);
				return;
			}
			// 变量，用于从编辑页面返回时恢复显示
			mydg.refresh("AUTOID", strFilter_search + m_strOrder_by());
			mydg.Goto_NewPageIndex(1);
		}
	}

	// 排序规则
	private String m_strOrder_by() {
		String strOrderby = " ORDER BY AUTOID DESC";
		return strOrderby;
	}
	//查询  显示和隐藏
	private void Fillin_SearchField_custom() {
		myFld_Query_custom.fill_fld("hidn_nQuery", "hidn_nQuery", 0); // 控制是否显示查询div，0-不显示，1-显示
		// //控制显示的查询方式：0-快速查询，1-高级查询
		myFld_Query_custom.fill_Panel("plEdit");
		myFld_Query_custom.set_item_value("hidn_nQuery", "0");
	}
}
