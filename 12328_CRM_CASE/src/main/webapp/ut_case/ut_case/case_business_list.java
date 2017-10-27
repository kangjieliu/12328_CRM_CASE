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
    private String pDomainCode = "";
    private String pDomain = "";
    
    my_dataGrid mydg = new my_dataGrid(51);
    my_Field myFld = new my_Field(1);
    my_ToolStrip myToolBar = new my_ToolStrip();
    private String m_Submit_res;   //提交返回结果    
     
	public void Page_Load (Object sender, Model model)
	{
		if(IsPostBack == false)// 正被首次加载和访问
		{			
			pmMap res = fun_main.QuerySplit(Request);
			int rc = res.nRet;
			if(rc > 0)
			{
				HashMap htQuery = res.htRet;
				pTypeId = Functions.ht_Get_strValue("NodeId", htQuery);
				pPnode = Functions.ht_Get_strValue("Pnode", htQuery);
			}
			Save_vs("pTypeId", pTypeId);
			Save_vs("pPnode", pPnode);
		} 
		else
		{
			pTypeId = Load_vs("pTypeId");
			pPnode = Load_vs("pPnode");
		}      

		InitToolbar();
		Fillin_frame();
		FillText();
	    myToolBar.render(model);
        myFld.render(model);
	}

	 private void Fillin_grid()
     {
        int i = 0;
        mydg.SetTable(m_strTableName);
        mydg.SetConnStr(pmSys.conn_crm);
        mydg.SetCaption("业务领域列表");
        mydg.SetPageSize(9);
        mydg.SetPagerMode(2);
        mydg.fill_fld(i++, "编号", "AUTOID", 0,0);
        mydg.fill_fld(i++, "业务事项编码", "MATTERS_CODE", 8, 0);   //modify by zhaoj 20130905 把link改为textbox
        mydg.fill_fld(i++, "业务事项", "MATTERS", 20, 0);

        mydg.RowDataFilled = this;
        mydg.fill_header("dgvList", "AUTOID", m_strFilter_base());
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

    public void mydg_RowDataFilled(Object sender, int rows)
    {	
    	 if (rows < 0) return;//表头行，不处理  
    	 String strMattersCode = mydg.get_cell(rows, "MATTERS_CODE");
    	 String strName = mydg.get_cell(rows, "MATTERS");
    	 int nCol = mydg.get_idx("MATTERS");      
         if(nCol>=0){
    		 String get_MattersCode = Get_Domain(strMattersCode);
    		 if(get_MattersCode.length()>0){
    			 String[] split = get_MattersCode.split(";");
    			 pDomainCode=split[0];
    			 pDomain=split[1];
    		 }
    		 String strHtml = myString.Format("<a href='#this' onclick=\"Fill_text('{0}','{1}','{2}','{3}');\">{0}</a>", strName,strMattersCode,pDomainCode,pDomain);
    		 mydg.set_cell(rows, nCol, strHtml); 
         }
    }
    
    private void FillText() {
		HashMap htTemp;
		my_odbc pTable = new my_odbc(pmSys.conn_crm);
		String strSql = myString.Format("SELECT * FROM {0} WHERE TYPEID='{1}' AND PNODE='{2}'", m_strTableName, pTypeId, pPnode);
		pmMap res = pTable.my_odbc_find(strSql, true);
		htTemp = res.htRet;
		int rc = res.nRet;
		pTable.my_odbc_disconnect();

		if (pTypeId.equals("root"))
			Functions.ht_SaveEx("TYPEID", pTypeId, htTemp);
		else
			Functions.ht_SaveEx("TYPEID", Functions.Substring(pTypeId, pTypeId.length() - 3, pTypeId.length()), htTemp);

		myFld.Load(htTemp);
	}
	 public void Fillin_Field()
     {
        String strCaseName = "";
        myFld.fill_fld("运输编号", "DOMAIN_CODE", 30, 0);
        myFld.fill_fld("运输方式", "DOMAIN", 30, 0);
        myFld.fill_fld("业务事项编码", "MATTERS_CODE", 30, 0);  //add by zhaoj 20130829
        myFld.fill_fld("业务事项", "MATTERS", 30, 0);

        myFld.fill_Panel("gbEdit");
        myFld.set_readonly("DOMAIN_CODE");
        myFld.set_readonly("DOMAIN");
        myFld.set_readonly("MATTERS_CODE");
        //myFld.get_item("EMAIL").Width = 603;

        myFld.set_item_text("DOMAIN_CODE", pDomainCode);
        myFld.set_item_text("DOMAIN", pDomain);
        my_odbc pTable = new my_odbc(pmSys.conn_crm);
//        if(pDomainCode.isEmpty()){
//        	int rc = pTable.my_odbc_find("CRM_DIC_DOMAIN_MATTERS","MATTERS_CODE='" + pDomainCode + "'");
//        	pTable.my_odbc_disconnect();
//        }
    }

    private void InitToolbar()
    {
    	myToolBar.fill_fld("新增", "AddNew");
		myToolBar.fill_fld("取消", "Cancel");
		myToolBar.fill_fld_confirm("保存", "Save", "确定要保存该目录信息吗？");
		myToolBar.fill_fld_confirm("删除", "Delete", "确定要删除此目录吗？");

		if (pTypeId.length() == 0 || pTypeId.equals("root")) {
			myToolBar.set_readonly("Save", true);
			myToolBar.set_readonly("Delete", true);
		}
    }

    public void myToolBar_btnItemClick(Object sender, String name, String parms, int nparms)
    { 
    	my_odbc pTable = new my_odbc(pmSys.conn_crm);
		HashMap htTemp = myFld.Save();
		if (name.equals("AddNew")) {
			myToolBar.set_readonly("Save", false);
			myToolBar.set_readonly("Delete", false);
			myFld.set_readonly("KNAME", false);

			String strTypeId = "";
			if (pTypeId.length() == 0 || pTypeId.equals("root")) {
				for (int i = 1; i < 1000; i++) {
					String strFilter = String.format("TYPEID='%03d' AND PNODE='%s'", i, "root");
					int rc = pTable.my_odbc_find(m_strTableName, strFilter);
					pTable.my_odbc_disconnect();
					if (rc == 1) {
						continue;
					} else {
						strTypeId = String.format("%03d", i);
						break;
					}
				}
			} else {
				for (int i = 1; i < 1000; i++) {
					String strFilter = String.format("TYPEID='%s-%03d' AND PNODE='%s'", pTypeId, +i, pTypeId);
					int rc = pTable.my_odbc_find(m_strTableName, strFilter);
					pTable.my_odbc_disconnect();
					if (rc == 1) {
						continue;
					} else {
						strTypeId = String.format("%03d", i);
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
				strTypeId = strPnode + "-" + strCurrentId;


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
				strTypeId = strPnode + "-" + strCurrentId;

			String strFilter = myString.Format("(TYPEID='{0}' AND PNODE='{1}') OR (PNODE LIKE '{0}%')", strTypeId, strPnode);

			int rc = pTable.my_odbc_delete(m_strTableName, strFilter);

		}
		pTable.my_odbc_disconnect();
    }

    //基本的显示条件
    private String m_strFilter_base()
    {	
    	String strFilter = "1=1";
    	if(!pDomainCode.isEmpty()){
    		strFilter += " AND DOMAIN_CODE = "+pDomainCode;
    	}
        return strFilter;
    }
    public static String Get_Domain(String strOrgCode)
    {
        String strKey_kvdb = "Get_Domain";
        DataTable dtTemp = pmInfo.myKvdb.Get(strKey_kvdb);// as DataTable;
        if (dtTemp == null)
        {
            my_odbc mydb = new my_odbc(pmSys.conn_crm);
            pmList pm=  mydb.my_odbc_find("SELECT DOMAIN_CODE,DOMAIN,MATTERS_CODE FROM CRM_DIC_DOMAIN_MATTERS", 0);
            mydb.my_odbc_disconnect();
            dtTemp = pm.dtRet;
            pmInfo.myKvdb.Setex(strKey_kvdb, dtTemp, 3600); //将取出的表放入缓存，缓存时长按实际需要设定
        }
        String strReturn = "";
        DataTable myRow = dtTemp.select("MATTERS_CODE='" + strOrgCode + "'");
        if (myRow.getCount() > 0){
            strReturn = Functions.dtCols_strValue(myRow,0, "DOMAIN_CODE");
            strReturn +=";"+ Functions.dtCols_strValue(myRow,0, "DOMAIN");
            }
        return strReturn;
    }
}
