package com.CallThink.ut_case;

import java.util.HashMap;
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
 * @Description 设定模板 修改页
 */
public class case_template_edit extends UltraCRM_Page {
	private String pOp = "";
	private String m_TableName = "CRM_TEMPLATE";
	private String pTempName = "";
	my_Field myFld = new my_Field(1);
	my_ToolStrip myToolBar = new my_ToolStrip();

	public void Page_Load(Object sender, Model model) {
		//正被首次加载和访问
		if (IsPostBack == false)
        {
            pmMap res = fun_main.QuerySplit(Request);
            int rc=res.nRet;
            if(rc>0)
            {
                 HashMap htQuery = res.htRet;     
                 pOp = Functions.ht_Get_strValue("cmd", htQuery);
                 pTempName = Functions.ht_Get_strValue("pTempName",  htQuery);
            }
            Save_vs("pOp", pOp);
            Save_vs("pTempName", pTempName);
        }else{
        	pOp = Load_vs("pOp");
        	pTempName = Load_vs("pTempName");
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
		myFld.fill_fld("模板名称", "NAME", 60, "*");
		myFld.fill_fld("模板类型", "TEMP_TYPE", 60, 1, true, true);
		myFld.set_list("TEMP_TYPE", "满意,基本满意,不满意");
		myFld.fill_fld("业务类型", "CASE_TYPE", 60, 1, true, true);
		myFld.set_list("CASE_TYPE", "SELECT CASETYPE AS CASETYPE,CASE_NAME AS CASENAME FROM CRM_CASE_TABLE","CASETYPE,CASENAME",pmSys.conn_crm);
		myFld.fill_fld("排序号", "SEQUENECE", 60, "*");
		myFld.fill_fld("参数个数", "PARAMS", 60, "*设置模板内容括号");
		myFld.fill_fld("模板内容", "CONTENT", 500, 10);
		
		myFld.fill_Panel("gbEdit");
	}
	/**
	 * 数据填充
	 */
	public void FillText_Default() {
		if (IsPostBack)
			return;
		my_odbc pOpidk = new my_odbc(pmSys.conn_crm);
		if(pOp.equals("AddNew")){
		}else{
			String strSql = myString.Format("SELECT * FROM {0} WHERE NAME='{1}'", m_TableName, pTempName);// (数据更新)
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

	private void InitToolbar() {//设置按钮
		if(pOp.equals("AddNew")){
			myToolBar.fill_fld("新建并保存", "Save", 0, 10);
		}else{
			myToolBar.fill_fld("保存", "Save", 0, 10);
		}
		myToolBar.fill_fld(fun_main.Term("LBL_Return"), "Return",0,10);
		myToolBar.fill_toolStrip("plCommand");
		myToolBar.btnItemClick = this;
	}

	public void myToolBar_btnItemClick(Object sender, String name, String parms, int nparms) {
		if (name.equals("Save")) {
			if (myFld.get_item_text("NAME").length() < 1&&myFld.get_item_text("CONTENT").length() < 1) {
				Functions.MsgBox("模板名称和模板内容不能为空!");
				return;
			}
			my_odbc pOpidk = new my_odbc(pmSys.conn_crm);
			HashMap htOpidk = myFld.Save();
			String strID=myFld.get_item_value("NAME");
			String strSql = myString.Format("SELECT * FROM {0} WHERE NAME='{1}'", m_TableName, strID);
			int rs = pOpidk.my_odbc_find(strSql);
			if(pOp.equals("AddNew")){
				if(rs>0){
					Functions.MsgBox("模板名称已存在!");
					return;
				}else{
					pOpidk.my_odbc_addnew(m_TableName, htOpidk);
					pOpidk.my_odbc_disconnect();
					Functions.MsgBox("模板添加成功!");
				}
			}else{
				int cord = pOpidk.my_odbc_update(m_TableName, htOpidk, "NAME='" + strID + "'");
				if(cord>0)
				Functions.MsgBox("模板修改成功!");
			}
			pOpidk.my_odbc_disconnect();
		} else  if (name .equals("Return"))  //2008.08 增加完成，返回页面后，重新刷新列表页面
        {
        	String strReturn_url = LastPageUrl_additem("history", "1");
            Functions.Redirect(strReturn_url);
        }
	}
}
