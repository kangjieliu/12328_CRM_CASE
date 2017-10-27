package com.CallThink.ut_case;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.Model;

import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.pmSys;
import com.CallThink.base.pmCode.UltraCRM_Page;
import com.ToneThink.ctsTools.WebUI.my_Field;
import com.ToneThink.ctsTools.WebUI.my_ToolStrip;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.myString;
import com.ToneThink.ctsTools.myUtility.pmMap;
/**
 * 
 * @author Liukj
 * @date 20170914
 * @Description 行政划区管理页面 修改
 */
public class qh_agent_edit extends UltraCRM_Page {
	private String pOp = "";
	private String pTableName = "DICT_JT_ORG";
	private String pOrgCode = "";
	my_Field myFld = new my_Field(1);
	my_ToolStrip myToolBar = new my_ToolStrip();

	public void Page_Load(Object sender, Model model) {
		if (IsPostBack == false)// 正被首次加载和访问
		{
			pmMap res = fun_main.QuerySplit(Request);
			int rc = res.nRet;
			if (rc > 0) {
				HashMap htQuery = res.htRet;
				pOp = Functions.ht_Get_strValue("cmd", htQuery);
				pOrgCode = Functions.ht_Get_strValue("pOrgCode", htQuery);
			}
			Save_vs("pOp", pOp);
			Save_vs("pOrgCode", pOrgCode);
		} else {
			pOp = Load_vs("pOp");
			pOrgCode = Load_vs("pOrgCode");
		}
		InitToolbar();
		Fillin_Field();
		FillText_Default();
		myToolBar.render(model);
		myFld.render(model);
	}
	/**
	 * 设置显示样式
	 */
	private void Fillin_Field() {
		myFld.SetLabelAlign("left");
		myFld.fill_fld("编号", "ORG_CODE", 30, 0);
		myFld.fill_fld("(区/县)名", "ORG_NAME", 30, 0);
		myFld.fill_fld("管辖单位", "REL_NAME", 30, 0); 
		if (!pOp.equals("AddNew")) {
			myFld.set_readonly("ORG_CODE");
			myFld.set_readonly("ORG_NAME");
		}
		myFld.fill_Panel("gbEdit");
	}
	/**
	 * 数据填充
	 */
	public void FillText_Default() {
		if (IsPostBack)
			return;
		my_odbc pOpidk = new my_odbc(pmSys.conn_crm);
		if (pOp.equals("AddNew")) {
		} else {
			String strSql = myString.Format("SELECT * FROM {0} WHERE ORG_CODE='{1}'", pTableName, pOrgCode);// (数据更新)
			pmMap res = pOpidk.my_odbc_find(strSql, true);
			pOpidk.my_odbc_disconnect(); // 断开数据库连接
			int rc = res.nRet;
			HashMap htRet = res.htRet;
			if (rc == 1)// 数据填充 回显
			{
				myFld.Load(htRet);
			}
		}
	}

	private void InitToolbar() {// 设置按钮
		if (pOp.equals("AddNew")) {
			myToolBar.fill_fld("新建并保存", "Save", 0, 10);
		} else {
			myToolBar.fill_fld("保存", "Save", 0, 10);
		}
		myToolBar.fill_fld(fun_main.Term("LBL_Return"), "Return", 0, 10);
		myToolBar.fill_toolStrip("plCommand");
		myToolBar.btnItemClick = this;
	}

	public void myToolBar_btnItemClick(Object sender, String name, String parms, int nparms) {
		if (name.equals("Save")) {
			if (StringUtils.isBlank(myFld.get_item_text("ORG_CODE"))
					& StringUtils.isBlank(myFld.get_item_text("ORG_NAME"))) {
				Functions.MsgBox("编号和(区/县)名不能为空!");
				return;
			}
			my_odbc pOpidk = new my_odbc(pmSys.conn_crm);
			HashMap htOpidk = myFld.Save();
			String strID = myFld.get_item_value("ORG_CODE");
			String strSql = myString.Format("SELECT * FROM {0} WHERE ORG_CODE='{1}'", pTableName, strID);
			int rs = pOpidk.my_odbc_find(strSql);
			if (pOp.equals("AddNew")) {
				if (rs > 0) {
					Functions.MsgBox("编号已存在!");
					return;
				} else {
					pOpidk.my_odbc_addnew(pTableName, htOpidk);
					pOpidk.my_odbc_disconnect();
					Functions.MsgBox("添加成功!");
				}
			} else {
				int cord = pOpidk.my_odbc_update(pTableName, htOpidk, "ORG_CODE='" + strID + "'");
				if (cord > 0)
					Functions.MsgBox("修改成功!");
			}
			pOpidk.my_odbc_disconnect();
		} else if (name.equals("Return")) {//  增加完成，返回页面后，重新刷新列表页面
			String strReturn_url = LastPageUrl_additem("history", "1");
			Functions.Redirect(strReturn_url);
		}
	}
}
