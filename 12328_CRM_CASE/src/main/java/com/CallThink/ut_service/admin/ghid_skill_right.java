///########################################################################################
/// Copyright (C) 2000, ToneThink.Soft  All Rights Reserved. 
/// 文件创建时间：2015-09-18
///   文件创建人：gaoww
/// 文件功能描述：座席技能管理
///     调用格式：
///      
///     维护记录：2015-09-18 create by gaoww
///#########################################################################################
package com.CallThink.ut_service.admin;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import org.springframework.ui.Model;
import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.pmSys;
import com.CallThink.base.pmCode.UltraCRM_Page;

import com.ToneThink.DataTable.DataTable;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.pmList;
import com.ToneThink.ctsTools.myUtility.pmMap;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.myString;
import com.ToneThink.ctsTools.WebUI.my_ToolStrip;
import com.ToneThink.ctsTools.WebUI.my_dataGrid;
import com.ToneThink.ctsTools.WebUI.my_Field;
import com.ToneThink.ctsTools.WebUI.my_SearchField;

    public class ghid_skill_right extends UltraCRM_Page
    {
        private String m_TableName = "SM_BUSS_GHID_SKILL";
        private String pTableName = "DICT_PO_CATEGORY";//默认产品类型
        private String pTableNameCode = "DICT_ORG_GHID_REAL";
        private String pSql = "";
        my_ToolStrip myToolBar = new my_ToolStrip();
        my_dataGrid mydg_select = new my_dataGrid(50);
        my_dataGrid mydg_unselect = new my_dataGrid(50);
        my_SearchField mySearch = new my_SearchField(4);
        my_Field myFld=new my_Field(1);

        DataTable dtSelect=new DataTable();
        DataTable dtUnSelect=new DataTable();
        boolean pBool = false;
        private String pOrg_code = "root"; //服务机构代码
        private String m_Ghid = ""; //选中座席工号

        private String pFrom = "menu"; //页面打开来源，menu:从菜单打开,ghid:从座席资料打开
        public void Page_Load(Object sender, Model model)
        {
            pmAgent = fun_main.GetParm();
            if (!IsPostBack)//正被首次加载时访问
            {
                HashMap htQuery;
                pmMap res = fun_main.QuerySplit(Request); htQuery = res.htRet;
                int rc = res.nRet;
                if (rc > 0)
                {
                    pOrg_code = Functions.ht_Get_strValue("key", htQuery);
                    if (htQuery.containsKey("ghid"))
                        m_Ghid = Functions.ht_Get_strValue("ghid", htQuery);
                    pFrom = Functions.ht_Get_strValue("from", htQuery);
                }
                
                Save_vs("pOrg_code", pOrg_code);
                Save_vs("m_Ghid", m_Ghid);
                Save_vs("pSql", pSql);
                Save_vs("pFrom", pFrom);
                Save_vs("dtSelect", dtSelect);
                Save_vs("dtUnSelect",dtUnSelect);
            }
            else
            {
                pOrg_code = Load_vs("pOrg_code");
                m_Ghid = Load_vs("m_Ghid");
                pSql = Load_vs("pSql");
                pFrom = Load_vs("pFrom");
                dtSelect=Load_vs("dtSelect", DataTable.class);
                dtUnSelect=Load_vs("dtUnSelect", DataTable.class);
            }

        Init_Data();		
		
		if (!IsPostBack) {
			rdGhid_SelectedIndexChanged();
		}		
		Fillin_grid();
		Fillin_Field();		
		InitToolbar();		
		
		myToolBar.render(model);
		mySearch.render(model);
		myFld.render(model);
		mydg_select.render(model);
		mydg_unselect.render(model);
		
}
        private void InitToolbar()
        {
            myToolBar.fill_fld("添加", "Add");
            myToolBar.fill_fld("Separator", "Separator1", 0, 3);
            myToolBar.fill_fld("移除", "Remove");
            myToolBar.fill_fld("Separator", "Separator2", 0, 3);
            myToolBar.fill_fld_confirm("保存主观评分", "Save", "确定保存客观评分？");
            myToolBar.btnItemClick = this;// MyToorBar_btnItemClick;
            myToolBar.fill_toolStrip("plCommand");
        }
        private void Fillin_Field()
        {
            mySearch.SetWidth(pmAgent.content_width);
            mySearch.SetMaxLabelLenth(90);
            mySearch.SetMaxLabelLenth_col2(90);
            mySearch.funName_OnClientClick("mySearch_FieldLinkClicked");


            mySearch.fill_fld("类型", "BTYPE", 20, 1);
            //未处理代码
            //mySearch.get_item_cbo("BTYPE").AutoPostBack = true;
            mySearch.set_list("BTYPE", "产品类型,服务类型");
            mySearch.set_item_value("BTYPE", "0");//页面加载选中产品类型
            mySearch.fill_fld("分类", "CLASSIFY", 20, 1);
            mySearch.set_list("CLASSIFY", "SELECT FLD_ID,FLD_NAME FROM DICT_PO_CATEGORY", "FLD_ID,FLD_NAME", pmSys.conn_crm);//默认加载出产品类型的细类
            //未处理代码
            //mySearch.get_item_cbo("CLASSIFY").AutoPostBack = true;
            mySearch.fill_fld("", "btnBTYPE", 20,7); 
            mySearch.fill_fld("", "btnCLASSIFY", 20,7);
            
            mySearch.fill_Panel("plSeach");
            mySearch.FieldLinkClicked = this;
        }

        @Override
		public void mySearch_FieldLinkClicked(Object sender, String name, String parms, int nparms, int ntype) {	
            if (parms.equals("") && nparms == -1) return;
            int nBtype = Functions.atoi(mySearch.get_item_value("BTYPE"));//由类型加载分类
            String strBtype = mySearch.get_item_value("BTYPE");
            if (!pBool)
            {
                if (name.equals("btnBTYPE"))
                {
                	mySearch.set_item_value("CLASSIFY", "");
                	mySearch.set_list("CLASSIFY", "");
                    //mySearch.get_item_cbo("CLASSIFY").Text = "";//清除细类选项
                    //mySearch.get_item_cbo("CLASSIFY").Items.Clear();
                    if (strBtype.length() != 0)
                    {
                        if (nBtype == 0) 
                        {
                            pTableName = "DICT_PO_CATEGORY";
                            mySearch.set_list("CLASSIFY", "SELECT FLD_ID,FLD_NAME FROM " + pTableName, "FLD_ID,FLD_NAME", pmSys.conn_crm);                   
                        }                      
                    }                   
                    pBool = true;
                    Refresh_Grid_unSelect();
                }
            }
            if (!pBool)
            {
                if (name.equals("btnCLASSIFY"))
                {
                    Refresh_Grid_unSelect();                    
                }
                pBool = true;
            }
        }

        public void myToolBar_btnItemClick(Object sender, String name, String parms, int nparms)
        {

            //添加
            int nBtype = Functions.atoi(mySearch.get_item_value("BTYPE"));//由类型加载分类               
            if (name.equals("Add"))
            {
                if (m_Ghid.length() == 0)
                {
                    Functions.MsgBox("请先选择工程师！");
                    return;
                }
                if (mydg_unselect.RowCount() <= 0) return;
                List<String> alRet = mydg_unselect.GetSelectedKey("FLD_ID");
                if (alRet.size() == 0)
                {
                    Functions.MsgBox("请先选中要添加的技能！");
                    return;
                }
                my_odbc pTable = new my_odbc(pmSys.conn_crm);
                int nCountSucc = 0;
                int nCountFal = 0;
                DataTable dtTemp = Functions.dt_GetTable("SELECT REAL_NAME FROM  CTS_OPIDK" + " WHERE GHID='" + m_Ghid + "'", "", pmSys.conn_callthink);//工程师工号对应名字
                String strEngName = "";
                if (dtTemp.getCount() > 0)
                {
                    strEngName = Functions.drCols_strValue(dtTemp.Rows().get(0), "REAL_NAME");
                }
                //由工程师编号对应所属机构代码
                DataTable dtCode = Functions.dt_GetTable("SELECT ORG_CODE FROM " + pTableNameCode + " WHERE REL_GHID='" + m_Ghid + "'", "", pmSys.conn_crm);
                String strCode = "";
                if (dtCode.getCount() > 0)
                {
                    strCode = Functions.drCols_strValue(dtCode.Rows().get(0), "ORG_CODE");
                }
                for (int i = 0; i < mydg_unselect.RowCount(); i++)
                {
                    if (mydg_unselect.isSelected(i))
                    {
                        String strId = mydg_unselect.get_cell(i, "FLD_ID");
                        HashMap htTemp = new HashMap();
                        Functions.ht_SaveEx("BUSS_ID", strId,htTemp);
                        Functions.ht_SaveEx("GHID", m_Ghid,htTemp);
                        Functions.ht_SaveEx("BTYPE", nBtype,htTemp);
                        Functions.ht_SaveEx("SKILL_LEVEL1", 0,htTemp);
                        Functions.ht_SaveEx("OP_NAME", strEngName,htTemp);
                        Functions.ht_SaveEx("ORG_CODE", strCode,htTemp);
                        DataTable dtT = Functions.dt_GetTable("SELECT * FROM " + m_TableName + " WHERE BUSS_ID='" + strId + "' AND GHID='" + m_Ghid + "' AND BTYPE='"+nBtype+"'", "", pmSys.conn_crm);
                        int nRtn = 0;
                        if (dtT.getCount() <= 0)//没有这个技能才添加
                        {
                            nRtn = pTable.my_odbc_addnew(m_TableName, htTemp);
                        }
                        pTable.my_odbc_disconnect();
                        if (nRtn == 1)
                        {
                            nCountSucc++;
                        }
                        else if (nRtn == -1)
                        {
                            nCountFal++;
                        }
                    }
                }
                if (nCountSucc > 0 && nCountFal == 0)
                    Functions.MsgBox("技能添加","技能添加成功！");
                else if (nCountSucc == 0 && nCountFal > 0)
                    Functions.MsgBox("技能添加", "技能添加失败！");
                else
                    Functions.MsgBox("技能添加", "技能添加成功" + nCountSucc + "条，失败" + nCountFal + "条！");
                //添加完后刷新数据表
                Refresh_Grid_Select();
                Refresh_Grid_unSelect();
            }
            //移除
            else if (name.equals("Remove"))
            {
                //从该工程师的技能中删除选中技能
                if (m_Ghid.length() == 0)
                {
                    Functions.MsgBox("请先选择工程师！");
                    return;
                }
                if (mydg_select.RowCount() <= 0) return;
                List<String> alRet = mydg_select.GetSelectedKey("BUSS_ID");
                if (alRet.size() == 0)
                {
                    Functions.MsgBox("请先选中要移除的技能！");
                    return;
                }
                int nCountSucc = 0;
                int nCountFal = 0;
                for (int i = 0; i < mydg_select.RowCount(); i++)
                {
                    if (mydg_select.isSelected(i))
                    {
                        String strId = mydg_select.get_cell(i, "BUSS_ID");
                        my_odbc mydb = new my_odbc(pmSys.conn_crm);
                        String strBtype = mydg_select.get_cell(i, "BTYPE");
                        String strFilter = myString.Format("BUSS_ID='{0}' AND GHID='{1}' AND BTYPE='" + strBtype + "'", strId, m_Ghid);
                        int nRtn = mydb.my_odbc_delete(m_TableName, strFilter);
                        if (nRtn == 1)
                        {
                            nCountSucc++;
                        }
                        else
                        {
                            nCountFal++;
                        }
                        mydb.my_odbc_disconnect();
                    }
                }
                if (nCountSucc > 0 && nCountFal == 0)
                    Functions.MsgBox("技能移除", "技能移除成功！");
                else if (nCountSucc == 0 && nCountFal > 0)
                    Functions.MsgBox("技能移除", "技能移除失败！");
                else
                    Functions.MsgBox("技能移除", "技能成功移除" + nCountSucc + "条，失败" + nCountFal + "条！");
                //添加完后刷新数据表
                Refresh_Grid_Select();
                Refresh_Grid_unSelect();
            }
            //修改主观评分保存
            else if (name.equals("Save"))
            {
                if (mydg_select.RowCount() <= 0) return;
                my_odbc pTable = new my_odbc(pmSys.conn_crm);
                for (int i = 0; i < mydg_select.RowCount(); i++)
                {
                    String strBussId = mydg_select.get_cell(i, "BUSS_ID");
                    String strGhid = mydg_select.get_cell(i, "GHID");
                    String strBtype = mydg_select.get_cell(i, "BTYPE");
                    int rc = pTable.my_odbc_find(m_TableName, "BUSS_ID='" + strBussId + "' AND GHID='" + strGhid + "' AND BTYPE='" + strBtype + "'");
                    if (rc == 1)
                    {
                        String strSubCode =mydg_select.get_cell(i, "SKILL_LEVEL") ;//(dgvSelect.Rows[i].Cells[6].FindControl("SKILL_LEVEL") as TextBox).Text.trim();
                        int nRtn = pTable.my_odbc_update(m_TableName, "SKILL_LEVEL='" + strSubCode + "'", "BUSS_ID='" + strBussId + "' AND GHID='" + strGhid + "' AND BTYPE='" + strBtype + "'");
                        if (nRtn == -1)
                        {
                            Functions.MsgBox("保存失败！");
                            break;
                        }
                        pTable.my_odbc_disconnect();
                    }
                }
                Functions.MsgBox("保存成功！");
                //选择工程师刷新列表
                Refresh_Grid_Select();
            }
        }
        private void Fillin_grid()
        {
            mydg_select.SetTable(m_TableName);
            mydg_select.SetSelectStr( "SELECT * FROM " + m_TableName + " WHERE 1>1");
            mydg_select.SetPageSize(10);
            mydg_select.SetPagerMode(2);

            mydg_select.SetCaption( "工程师服务技能表");
            mydg_select.SetConnStr(pmSys.conn_crm);

            int i = 0;
            mydg_select.fill_fld(i++, "选择", "SELECT", 5, 9);
            mydg_select.fill_fld(i++, "姓名", "OP_NAME", 0);
            mydg_select.fill_fld(i++, "姓名", "GHID", 10, 1);
            mydg_select.set_cols_cbo_list("GHID", "SELECT GHID,REAL_NAME FROM CTS_OPIDK", "GHID,REAL_NAME", pmSys.conn_callthink);

            mydg_select.fill_fld(i++, "品类/服务类型", "BUSS_ID", -1, 1);
            //mydg_select.set_cols_cbo_list("BUSS_ID", "SELECT FLD_ID,FLD_NAME FROM DICT_PO_CATEGORY", "FLD_ID,FLD_NAME", pmSys.conn_crm);
            mydg_select.fill_fld(i++, "类型", "BTYPE", -1, 1);
            mydg_select.set_cols_cbo_list("BTYPE", "产品类型,服务类型");
            mydg_select.fill_fld(i++, "技能客观评分", "SKILL_LEVEL1", -1);
            mydg_select.fill_fld(i++, "技能主观评分", "SKILL_LEVEL", 12, 7);
            mydg_select.RowDataFilled = this;// new RowDataFilledEventHandler(mydg_select_RowDataFilled);
            //mydg_select.fill_header("dgvSelect");
            mydg_select.fill_header("dgvSelect",dtSelect);
            
            //************************************************************************
            mydg_unselect.SetTable(pTableName);
            //mydg_unselect.SetSelectStr( "SELECT * FROM " + pTableName);
            mydg_unselect.SetPageSize(10);
            mydg_unselect.SetPagerMode(2);

            mydg_unselect.SetCaption("服务类型表");
            mydg_unselect.SetConnStr(pmSys.conn_crm);

            int j = 0;
            mydg_unselect.fill_fld(j++, "选择", "SELECT", 5, 9);
            mydg_unselect.fill_fld(j++, "品牌代码", "FLD_ID", -1);
            mydg_unselect.fill_fld(j++, "品牌名称", "FLD_NAME", -1);
            mydg_unselect.fill_fld(j++, "说明", "NOTE", -1);
           // mydg_unselect.fill_header("dgvUnSelect", "FLD_ID", "1>1");
            mydg_unselect.fill_header("dgvUnSelect", dtUnSelect);
        }

	@Override
	public void mydg_RowDataFilled(Object sender, int rows) {
		if (sender.equals(mydg_select)) {
			mydg_select_RowDataFilled(sender, rows);
		}
	}
		private void mydg_select_RowDataFilled(Object sender, int rows)
        {
            if (rows < 0) return; //表头行，不处理 
            int nBtype = Functions.atoi(mydg_select.get_cell(rows, "BTYPE"));           
            int cols = mydg_select.get_idx("BUSS_ID");
            String strBussId = mydg_select.get_cell(rows, "BUSS_ID");
            if (cols >= 0)//对应不同的类型
            {
            	String strCell = "";
                if (nBtype == 0)
                {
                	String strTable = "";
                    if (strBussId.length() <= 2)
                        strTable = "DICT_PO_CATEGORY";
                    else
                        strTable = "DICT_PO_CATEGORY2";
                    String strSql = myString.Format("SELECT FLD_NAME FROM {0} WHERE FLD_ID='{1}'", strTable, strBussId);
                    DataTable dtTemp = Functions.dt_GetTable(strSql, "", pmSys.conn_crm);
                    if (dtTemp.Rows().Count() > 0)
                    {
                        strCell = Functions.dtCols_strValue(dtTemp, "FLD_NAME");
                    }
                }
                else if (nBtype > 0)
                {
                	String strTable = "";
                    if (strBussId.length() <= 2)
                        strTable = "DICT_SVC_CATEGORY";
                    else
                        strTable = "DICT_SVC_CATEGORY2";
                    String strSql = myString.Format("SELECT FLD_NAME FROM {0} WHERE FLD_ID='{1}'", strTable, strBussId);
                    DataTable dtTemp = Functions.dt_GetTable(strSql, "", pmSys.conn_crm);
                    if (dtTemp.Rows().Count() > 0)
                    {
                        strCell = Functions.dtCols_strValue(dtTemp, "FLD_NAME");
                    }
                }
                //e.Row.Cells[cols].Text = strCell;
                mydg_select.set_cell(rows, cols, strCell);
            }
        }
        private void Init_Data()
        {
            DataTable dtTemp = new DataTable();
            String fld_value1 = "", fld_value2 = "", strSql = "", strFilter = "";
            //初始化工单类型
            if (pOrg_code == "root")
                strFilter = "1=1";
            else
                strFilter = " ORG_CODE='" + pOrg_code + "'";

            strSql = "SELECT REL_NAME,REL_GHID FROM DICT_ORG_GHID_REAL WHERE " + strFilter + " ORDER BY REL_GHID";

            my_odbc pTemp = new my_odbc(pmSys.conn_crm);
            pmList pm = pTemp.my_odbc_find(strSql, 0);
            pTemp.my_odbc_disconnect();
            dtTemp=pm.dtRet;
           // rdGhid.Items.Clear();
           // int rows = 0, nPos = -1;
            
            LinkedHashMap<String, String> linkedHashMap=new LinkedHashMap<>();
            for (int rows = 0; rows < dtTemp.Rows().Count(); rows++)
            {
                fld_value1 = Functions.dtCols_strValue(dtTemp, rows, "REL_NAME");
                fld_value2 = Functions.dtCols_strValue(dtTemp, rows, "REL_GHID");
                //rdGhid.Items.Add(fld_value1 + "(" + fld_value2 + ")");
                //rdGhid.Items[rows].Value = fld_value2.ToString();
                linkedHashMap.put( fld_value2,fld_value1 + "(" + fld_value2 + ")");
            }

		myFld.SetMaxLabelLenth(0);
		myFld.SetMaxLabelLenth_col2(0);
		myFld.fill_fld("", "Radio_GHID", 20, 2);
		myFld.set_list("Radio_GHID", linkedHashMap);
		myFld.fill_fld("", "btnRadio", 20,7);
	
		if (pFrom != "ghid" && dtTemp.Rows().Count()>0) {
			String strGHid= Functions.dtCols_strValue(dtTemp, 0, "REL_GHID");
			myFld.set_item_value("Radio_GHID", strGHid);
		}
		
		myFld.FieldLinkClicked=this;
		myFld.fill_Panel("rdGhid");
	}

	@Override
	public void myFld_FieldLinkClicked(Object sender, String name, String parms, int nparms, int ntype) {
		if (name.equals("btnRadio")) {
			rdGhid_SelectedIndexChanged();
		}
	}
        
		protected void rdGhid_SelectedIndexChanged()
        {
            if (pFrom.equals("menu") )
            {
                m_Ghid =myFld.get_item_value("Radio_GHID"); //rdGhid.SelectedValue;
                Save_vs("m_Ghid", m_Ghid);
            }
            Refresh_Grid_Select();
            Refresh_Grid_unSelect();      
            mydg_select.refresh(dtSelect);
            mydg_unselect.refresh(dtUnSelect);
        }

        private void Refresh_Grid_Select()
        {
            String strSql = myString.Format("SELECT * FROM {0} WHERE GHID='{1}'", m_TableName, m_Ghid);
            //mydg_select.refresh(strSql);          
            my_odbc pTable=new my_odbc(pmSys.conn_crm);
            pmList pm=pTable.my_odbc_find(strSql,0);
            pTable.my_odbc_disconnect();
            dtSelect=pm.dtRet;
            Save_vs("dtSelect", dtSelect);
            if (IsPostBack) {
                mydg_select.refresh(dtSelect);
			}
            
            //System.out.println("dtSelect:"+fun_json.DataTable_toJson(dtSelect));
        }

        private void Refresh_Grid_unSelect()
        {
            int nBtype = Functions.atoi(mySearch.get_item_value("BTYPE"));//由类型加载分类
            String strClassify = mySearch.get_item_value("CLASSIFY");
           
            String strTableName = "DICT_PO_CATEGORY"; //品类
            if (nBtype == 1)
                strTableName = "DICT_SVC_CATEGORY"; //服务类型
            if (strClassify != "")
                strTableName = "DICT_PO_CATEGORY2"; //细类
            String strFilter = myString.Format("FLD_ID  NOT IN (SELECT BUSS_ID AS FLD_ID FROM {0} WHERE GHID='{1}' AND BTYPE={2})", m_TableName, m_Ghid, nBtype);//默认刷新
            if (strClassify != "")
                strFilter += " AND FLD_ID LIKE '" + strClassify + "%'";
           
            //mydg_unselect.SetTable(strTableName);
            //mydg_unselect.refresh("FLD_ID", strFilter);
            String strSql=myString.Format("SELECT * FROM {0} WHERE {1}", strTableName,strFilter);
            my_odbc pTable=new my_odbc(pmSys.conn_crm);
            pmList pm=pTable.my_odbc_find(strSql,0);
            pTable.my_odbc_disconnect();
            dtUnSelect=pm.dtRet;
            Save_vs("dtUnSelect", dtUnSelect);
            if (IsPostBack) {
                mydg_unselect.refresh(dtUnSelect);
			}
            
            //System.out.println("dtUnSelect:"+fun_json.DataTable_toJson(dtUnSelect));
        }
    }

