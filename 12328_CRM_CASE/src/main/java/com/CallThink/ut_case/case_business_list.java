package com.CallThink.ut_case;


import java.util.HashMap;
import java.util.List;

import org.springframework.ui.Model;

import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.pmSys;
import com.CallThink.base.pmCode.UltraCRM_Page;
import com.CallThink.base.support.pmInfo;
import com.CallThink.ut_case.pmModel_case.case_info;
import com.CallThink.ut_case.pmModel_case.case_set_info;
import com.CallThink.ut_case.pmModel_case.fun_case;
import com.ToneThink.DataTable.DataRow;
import com.ToneThink.DataTable.DataTable;
import com.ToneThink.DateTime.DateTime;
import com.ToneThink.ctsTools.WebUI.my_Field;
import com.ToneThink.ctsTools.WebUI.my_ToolStrip;
import com.ToneThink.ctsTools.WebUI.my_dataGrid;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.myString;
import com.ToneThink.ctsTools.myUtility.pmList;
import com.ToneThink.ctsTools.myUtility.pmMap;

public class case_business_list extends UltraCRM_Page
{

    private String m_strTableName = "CRM_DICT_BUSINESS";
    private String pTypeId = ""; // 节点编号
	private String pPnode = ""; // 父节点号

	my_ToolStrip myToolBar = new my_ToolStrip();
	my_Field myFld = new my_Field(1);

	public void Page_Load(Object sender, Model model) {
		if (IsPostBack == false) {
			HashMap htQuery;
			pmMap res = fun_main.QuerySplit(Request);
			htQuery = res.htRet;
			int rc = res.nRet;
			if (rc > 0) {
				pTypeId = Functions.ht_Get_strValue("NodeId", htQuery);
				pPnode = Functions.ht_Get_strValue("Pnode", htQuery);
			}

			Save_vs("pTypeId", pTypeId);
			Save_vs("pPnode", pPnode);
		} else {
			pTypeId = Load_vs("pTypeId");
			pPnode = Load_vs("pPnode");
		}

		InitToolbar();
		Fillin_frame();
		FillText();
		myToolBar.render(model);
		myFld.render(model);
	}

	private void InitToolbar() {
		myToolBar.fill_fld("新增", "AddNew");
		myToolBar.fill_fld("取消", "Cancel");
		myToolBar.fill_fld_confirm("保存", "Save", "确定要保存该目录信息吗？");
		myToolBar.fill_fld_confirm("删除", "Delete", "确定要删除此目录吗？");

		if (pTypeId.length() == 0 || pTypeId.equals("root")) {
			myToolBar.set_readonly("Save", true);
			myToolBar.set_readonly("Delete", true);
		}

		myToolBar.fill_toolStrip("plCommand");
		myToolBar.btnItemClick = this;
	}

	public void myToolBar_btnItemClick(Object sender, String name, String parms, int nparms) {
		my_odbc pTable = new my_odbc(pmSys.conn_crm);
		HashMap htTemp = myFld.Save();
		if (name.equals("AddNew")) {
			myToolBar.set_readonly("Save", false);
			myToolBar.set_readonly("Delete", false);
			myFld.set_readonly("KNAME", false);

			String strTypeId = "";
			if (pTypeId.length() == 0 || pTypeId.equals("root")) {
				for (int i = 1; i < 1000; i++) {
					String strFilter = String.format("TYPEID='%02d' AND PNODE='%s'", i, "root");
					int rc = pTable.my_odbc_find(m_strTableName, strFilter);
					pTable.my_odbc_disconnect();
					if (rc == 1) {
						continue;
					} else {
						strTypeId = String.format("%02d", i);
						break;
					}
				}
			} else {
				for (int i = 1; i < 1000; i++) {
					String strFilter = String.format("TYPEID='%s-%02d' AND PNODE='%s'", pTypeId, +i, pTypeId);
					int rc = pTable.my_odbc_find(m_strTableName, strFilter);
					pTable.my_odbc_disconnect();
					if (rc == 1) {
						continue;
					} else {
						strTypeId = pTypeId +"-"+ String.format("%02d", i);
						break;
					}
				}
			}
			myFld.setReload(true);
			if (pTypeId.length() == 0 || pTypeId.equals("root"))
				myFld.set_item_value("PNODE", "root");
			else
				myFld.set_item_value("PNODE", pTypeId);
			myFld.set_item_value("TYPEID", strTypeId);
			myFld.set_item_value("KNAME", "");
		} else if (name.equals("Cancel")) {
			myToolBar.Clear();
			myFld.Clear();

			InitToolbar();
			Fillin_frame();
			FillText();
		} else if (name.equals("Save")) // 分为新增后保存，以及更改保存
		{
			String strPnode = myFld.get_item_value("PNODE");
			String strCurrentId = myFld.get_item_value("TYPEID");
			String strTypeId = "";
			if (strPnode.length() == 0 || strPnode.equals("root"))
				strTypeId = strCurrentId;
			else
				strTypeId = strCurrentId;


			Functions.ht_SaveEx("TYPEID", strTypeId, htTemp);

			String strFilter = myString.Format("TYPEID='{0}' AND PNODE='{1}'", strTypeId, strPnode);
			int rc = pTable.my_odbc_find(m_strTableName, strFilter);
			pTable.my_odbc_disconnect();
			if (rc == 1) {
				rc = pTable.my_odbc_update(m_strTableName, htTemp, strFilter);
				pTable.my_odbc_disconnect();
			} else {
				rc = pTable.my_odbc_addnew(m_strTableName, htTemp);
				pTable.my_odbc_disconnect();
			}

			if (rc == 1)
			{
//				AddUpdateLog(pTable, htTemp, strFilter);
				Functions.js_exec("Update();");
			}
			else
				Functions.MsgBox("记录保存失败！");
		} else if (name.equals("Delete")) {
			if (pTypeId.length() == 0 || pTypeId.equals("root")) {
				Functions.MsgBox("请先选择需要删除的快捷回复！");
				return;
			}

			String strPnode = myFld.get_item_value("PNODE");
			String strCurrentId = myFld.get_item_value("TYPEID");
			String strTypeId = "";
			if (strPnode.length() == 0 || strPnode.equals("root"))
				strTypeId = strCurrentId;
			else
				strTypeId = strCurrentId;

			String strFilter = myString.Format("(TYPEID='{0}' AND PNODE='{1}') OR (PNODE LIKE '{0}%')", strTypeId, strPnode);

			int rc = pTable.my_odbc_delete(m_strTableName, strFilter);

			if (rc > 0) {
				Functions.js_exec("Update();");
			} else {
				Functions.MsgBox("记录删除失败，请检查此快捷回复是否存在！");
			}
		} 

		pTable.my_odbc_disconnect();
	}

	// add by zhangyr 20170818 增加快捷回复的 轨迹
	private void AddUpdateLog(my_odbc mydb, HashMap htSave, String strFilter) {
		String strTableName = "WMC_CHAT_QREPLY_LOG";
		// 1.从数据库中抓取出旧数据
		HashMap htUpdate = new HashMap<>();
		pmMap pm = mydb.my_odbc_find(myString.Format("SELECT * FROM {0} WHERE {1}", m_strTableName, strFilter), true);
		mydb.my_odbc_disconnect();
		htUpdate = pm.htRet;
		// 2. 将新数据赋值
		htUpdate.put("TYPEID_NEW", Functions.ht_Get_strValue("TYPEID", htSave));
		htUpdate.put("KNAME_NEW", Functions.ht_Get_strValue("KNAME", htSave));
		htUpdate.put("PNODE_NEW", Functions.ht_Get_strValue("PNODE", htSave));
		
		mydb.my_odbc_addnew(strTableName, htUpdate);
		mydb.my_odbc_disconnect();
	}

	private void Fillin_frame() {
		myFld.fill_fld("父目录号", "PNODE", 50, 0, false);
		myFld.fill_fld("当前节点", "TYPEID", 50, 0, false);
		myFld.fill_fld("名称", "KNAME", 550);

		if (pTypeId.length() == 0 || pTypeId.equals("root")) {
			myFld.set_readonly("KNAME", true);
		}

		myFld.fill_Panel("plEdit");
	}

	private void FillText() {
		HashMap htTemp;
		my_odbc pTable = new my_odbc(pmSys.conn_crm);
		String strSql = myString.Format("SELECT * FROM {0} WHERE TYPEID='{1}' AND PNODE='{2}'", m_strTableName, pTypeId, pPnode);
		pmMap res = pTable.my_odbc_find(strSql, true);
		htTemp = res.htRet;
		int rc = res.nRet;
		pTable.my_odbc_disconnect();

		myFld.Load(htTemp);
	}
}
