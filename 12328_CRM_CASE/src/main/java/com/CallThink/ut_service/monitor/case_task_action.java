///########################################################################################
/// Copyright (C) 2000, ToneThink.Soft  All Rights Reserved. 
///#########################################################################################
/// 文件创建时间：2015-10-16
///   文件创建人：xutt
/// 文件功能描述：任务单反馈页面
///     调用格式：
///     
///     维护记录：
/// 2015.10.16：create by xutt     
/// 2015.11.09：modify by gaoww 调整表结构、显示内容和查询语句
///#########################################################################################
package com.CallThink.ut_service.monitor;

import org.springframework.ui.Model;
import com.CallThink.base.pmClass.pmSys;
import com.CallThink.base.pmCode.UltraCRM_Page;

import com.ToneThink.DataTable.DataTable;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.pmList;
import com.ToneThink.ctsTools.WebUI.my_ToolStrip;
import com.ToneThink.ctsTools.WebUI.my_dataGrid;

public class case_task_action extends UltraCRM_Page {
	my_dataGrid mydg = new my_dataGrid(51);
	my_ToolStrip myToolBar = new my_ToolStrip();

	public void Page_Load(Object sender, Model model) {
		InitToolbar();
		Fillin_grid();

		myToolBar.render(model);
		mydg.render(model);
	}

	private void InitToolbar() {
		myToolBar.fill_fld("刷新", "Query_all");
		myToolBar.fill_toolStrip("plCammand");
		myToolBar.btnItemClick = this;
	}

	public void myToolBar_btnItemClick(Object sender, String name, String parms, int nparms) {
		if (name.equals("Query_all")) {
			DataTable dtTemp = new DataTable();
			my_odbc mydb = new my_odbc(pmSys.conn_crm);
			pmList res = mydb.my_odbc_find("SELECT TOP 100 * FROM SM_TASK_TRACE_LOG WHERE CASEID!='' ORDER BY SDATE DESC", 0);
			dtTemp = res.dtRet;
			mydb.my_odbc_disconnect();
			if (dtTemp.getCount() > 0) {
				mydg.refresh(dtTemp);
			}
		}
	}

	private void Fillin_grid() {
		mydg.ID("fankui");
		mydg.SetTable("SM_TASK_TRACE_LOG");
		mydg.SetSelectStr("SELECT TOP 100 * FROM SM_TASK_TRACE_LOG WHERE 1>1");
		mydg.SetPageSize(100);
		mydg.PagerVisibale = false;
		mydg.SetCaption("任务单反馈列表");
		mydg.SetConnStr(pmSys.conn_crm);
		int i = 0;
		mydg.fill_fld(i++, "编号", "AUTOID", 0);
		mydg.fill_fld(i++, "操作时间", "SDATE", 20);
		mydg.fill_fld(i++, "环节", "PROCESS", 10);
		mydg.fill_fld(i++, "状态", "STATUS", 10);
		mydg.fill_fld(i++, "工单类型", "CASETYPE", 0);
		mydg.fill_fld(i++, "进展说明", "NOTE", -1);
		mydg.fill_fld(i++, "操作员", "OP_NAME", 20, 1);
		mydg.set_cols_cbo_list("OP_NAME", "SELECT GHID AS OP_NAME,REAL_NAME AS REAL_NAME FROM CTS_OPIDK","OP_NAME,REAL_NAME", pmSys.conn_callthink);
		mydg.fill_fld(i++, "操作员所属机构", "ORG_CODE", 30, 1);
		mydg.set_cols_cbo_list("ORG_CODE", "SELECT ORG_CODE,ORG_NAME FROM DICT_ORG_CODE", "ORG_CODE,ORG_NAME",pmSys.conn_crm);
		mydg.RowDataFilled = this;
		mydg.fill_header("dgvList", "AUTOID", "SUBMIT_FROM='1' ORDER BY SDATE DESC");
	}

	public void mydg_RowDataFilled(Object sender, int rows) {
		if (rows < 0) return;
			
		String strCasetype = mydg.get_cell(rows, "CASETYPE");
		String strProcess = mydg.get_cell(rows, "PROCESS");
		String strStatus = mydg.get_cell(rows, "STATUS");
		int cols = mydg.get_idx("PROCESS");
		String fld_value = "";
		if (cols >= 0) {
			my_odbc pTable = new my_odbc(pmSys.conn_crm);
			int rc = pTable.my_odbc_find("SELECT PROCESS_NAME FROM CRM_CASE_PROCESS WHERE CASETYPE='" + strCasetype+ "' AND PROCESS_ID='" + strProcess + "'");
			if (rc > 0) {
				fld_value = pTable.my_odbc_result("PROCESS_NAME");
				mydg.set_cell(rows, cols, fld_value);
			}
			pTable.my_odbc_disconnect();

		}
		cols = mydg.get_idx("STATUS");
		if (cols >= 0) {
			my_odbc pTable = new my_odbc(pmSys.conn_crm);
			int rc = pTable.my_odbc_find("SELECT STATUS_NAME FROM CRM_CASE_STATUS WHERE CASETYPE='" + strCasetype+ "' AND STATUS_ID='" + strProcess + "'");
			if (rc > 0) {
				fld_value = pTable.my_odbc_result("STATUS_NAME");
				mydg.set_cell(rows, cols, fld_value);
			}
			pTable.my_odbc_disconnect();
		}
	}

	/// <summary>
	/// 定时刷新
	/// </summary>
	/// <param name="sender"></param>
	/// <param name="e"></param>
	protected void Timer_5min_Tick() {

		DataTable dtTemp = new DataTable();
		my_odbc mydb = new my_odbc(pmSys.conn_crm);
		pmList res = mydb.my_odbc_find("SELECT TOP 100 * FROM SM_TASK_TRACE_LOG WHERE SUBMIT_FROM='1' ORDER BY SDATE DESC", 0);
		dtTemp = res.dtRet;
		mydb.my_odbc_disconnect();
		if (dtTemp.getCount() > 0) {
			mydg.refresh(dtTemp);
		}

	}

}
