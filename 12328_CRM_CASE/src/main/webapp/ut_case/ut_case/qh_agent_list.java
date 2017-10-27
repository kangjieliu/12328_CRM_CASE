package com.CallThink.ut_case;

import java.util.HashMap;
import java.util.List;

import org.springframework.ui.Model;

import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.pmSys;
import com.CallThink.base.pmCode.UltraCRM_Page;
import com.ToneThink.ctsTools.WebUI.my_Field;
import com.ToneThink.ctsTools.WebUI.my_ToolStrip;
import com.ToneThink.ctsTools.WebUI.my_dataGrid;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.myString;
import com.ToneThink.ctsTools.myUtility.pmMap;
/**
 * @author Liukj
 * @date 20170914
 * @Description 行政划区管理页面 列表
 */
public class qh_agent_list extends UltraCRM_Page {
	private String pTableName = "DICT_JT_ORG";
	private String pOrg_code = "";
	private String pOrg_level = "";

	my_dataGrid mydg = new my_dataGrid(51);
	my_Field myFld = new my_Field(1);
	my_ToolStrip myToolBar = new my_ToolStrip();

	public void Page_Load(Object sender, Model model) {
		if (IsPostBack == false)// 正被首次加载和访问
		{
			pmMap res = fun_main.QuerySplit(Request);
			int rc = res.nRet;
			if (rc > 0) {
				HashMap htQuery = res.htRet;
				pOrg_code = Functions.ht_Get_strValue("pOrg_code", htQuery);
				pOrg_level = Functions.ht_Get_strValue("pOrg_level", htQuery);
			}
			Save_vs("pOrg_code", pOrg_code);
			Save_vs("pOrg_level", pOrg_level);
		} else {
			pOrg_code = Load_vs("pOrg_code");
			pOrg_level = Load_vs("pOrg_level");
		}
		InitToolbar();
		Fillin_grid();

		myToolBar.render(model);
		myFld.render(model);
		mydg.render(model);
	}

	private void Fillin_grid() {
		int i = 0;
		mydg.SetTable(pTableName);
		mydg.SetConnStr(pmSys.conn_crm);
		mydg.SetCaption("桂林市交通运输局表");
		mydg.SetPageSize(9);
		mydg.SetPagerMode(2);
		mydg.fill_fld(i++, "", "SELECT", 4, 9, "radio");
		mydg.fill_fld(i++, "编号", "ORG_CODE", 8, 0);
		mydg.fill_fld(i++, "(区/县)名", "ORG_NAME", 8, 0); 
		mydg.fill_fld(i++, "管辖单位", "REL_NAME", 20, 0);

		mydg.RowDataFilled = this;
		mydg.fill_header("dgvList", "ORG_CODE", m_strOrder_by());
	}

	public void mydg_RowDataFilled(Object sender, int rows) {
		if (rows < 0)
			return;// 表头行，不处理
		String strMattersCode = mydg.get_cell(rows, "ORG_CODE");
		String strName = mydg.get_cell(rows, "ORG_NAME");
		int nCol = mydg.get_idx("ORG_NAME");
		if (nCol >= 0) {
			String strUrl = myString.Format("qh_agent_edit.aspx?pOrgCode={0}", strMattersCode);
			String strHtml = myString.Format("<a href='#this' onclick=\"open_view('{0}','{1}');\">{1}</a>", strUrl,
					strName);
			mydg.set_cell(rows, nCol, strHtml);
		}
	}

	private void InitToolbar() {
		myToolBar.fill_fld("增加", "AddNew", 0, 10);
		myToolBar.fill_fld("删除", "Delete", 0, 10);
		myToolBar.fill_toolStrip("plCommand");
		myToolBar.btnItemClick = this;
	}
	
	public void myToolBar_btnItemClick(Object sender, String name, String parms, int nparms) {
		if (name.equals("Delete")) {
			List<String> alSelect = mydg.GetSelectedKey("ORG_CODE");
			if (alSelect.size() <= 0) {
				Functions.MsgBox("请先选中要删除的记录！");
				return;
			}
			int nCnt = 0;
			for (int index = 0; index < alSelect.size(); index++) {
				String strId = alSelect.get(index);
				my_odbc pTable = new my_odbc(pmSys.conn_crm);
				int rc = pTable.my_odbc_delete(pTableName, "ORG_CODE='" + strId + "'");
				if (rc > 0)
					nCnt++;
				pTable.my_odbc_disconnect();
			}
			if (nCnt > 0) {
				mydg.refresh("ORG_CODE", m_strOrder_by());
				Functions.MsgBox("记录删除成功！");
			}
		}
	}

	// 基本的显示条件
	private String m_strOrder_by() {
		return " ORDER BY ORG_CODE ";
	}
}
