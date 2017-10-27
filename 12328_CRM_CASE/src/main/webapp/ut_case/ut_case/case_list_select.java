package com.CallThink.ut_case;

import java.util.HashMap;

import org.springframework.ui.Model;

import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.pmSys;
import com.CallThink.base.pmCode.UltraCRM_Page;
import com.CallThink.ut_case.pmModel_case.fun_case;
import com.ToneThink.DataTable.DataTable;
import com.ToneThink.ctsTools.WebUI.my_ToolStrip;
import com.ToneThink.ctsTools.WebUI.my_dataGrid;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.myString;
import com.ToneThink.ctsTools.myUtility.pmMap;

public class case_list_select  extends UltraCRM_Page
{ 
	//private int pType = pmSys.casetype_default;
    private String pOp = "";
    private String pUserid = "";
    private int p_Find_user = 1; //是否已有用户，<1-新用户，1-已有 add by gaoww 20150422 
    private String pTableName = "CRM_CASE"; //add by gaoww 20161118

    private String m_Fields = "";  //列表显示的字段

    my_ToolStrip myToolBar = new my_ToolStrip();
    my_dataGrid mydg = new my_dataGrid(100);
    
    public void Page_Load(Object sender, Model model) {
    	if (IsPostBack == false)//正被首次加载和访问
        {
            //由不同(Page)调用，显示所属工单,格式：?cmd=xxx&
            //cmd 的含义： 
            // "Relation_corp"      公司所属工单，userid=公司ID  
            // "Relation"      联系人所属工单，userid=联系人ID 
            // "View"	       //显示用户，userid=查询条件	caller=无意义

    		pmMap res = fun_main.QuerySplit(Request);
            int rc=res.nRet;
            if(rc>0)
            {
                HashMap htQuery = res.htRet;      
                pOp = Functions.ht_Get_strValue("cmd", htQuery);
                pUserid = Functions.ht_Get_strValue("userid", htQuery);
                p_Find_user = Functions.atoi(Functions.ht_Get_strValue("find_user", htQuery));
            }
            Save_vs("pOp", pOp);
            Save_vs("pUserid", pUserid);
            Save_vs("p_Find_user", p_Find_user);
        }
        else
        {
            pOp = Load_vs("pOp");
            pUserid = Load_vs("pUserid");
            p_Find_user =Functions.atoi(Load_vs("p_Find_user"));
            pTableName = Load_vs("pTableName");
        }
    	
    	InitToolbar();            	
       	Fillin_grid();      
  	    if (p_Find_user == 1) //add by gaoww 20150422已有客户时，遍历所有工单
           Fillin_data();
  	    
  	   myToolBar.render(model);
       mydg.render(model);
    }	
  
    //#region 处理工单
    //填充工单列表、详细信息框架
    private void Fillin_grid()
    {
        int i = 0;

        //string strTable_case = "CRM_CASE";// myCase.TableName;

        //mydg.SetTable = strTable_case;
        //mydg.SetSelectStr = "SELECT * FROM " + strTable_case + " WHERE 1<>1";
        mydg.SetTable(pTableName);
        mydg.SetSelectStr("SELECT * FROM " + pTableName + " WHERE 1<>1");
        mydg.SetCaption("最近工单资料");
        mydg.SetConnStr(pmSys.conn_crm);

        //mydg.SetPageSize = 14;
        mydg.AllowSorting(0);
        //mydg.AllowRowsNumVisibale(false);
        mydg.SetPagerMode(2);

        String fld_format = "CMDNAME=Link;NULLAS=[null]"; //专为BS修改
        //mydg.fill_fld(i, fld_name, fld_value, fld_width, 8, fld_format);
        mydg.fill_fld(i++, "工单编号", "CASEID", 22, 8, fld_format);
        mydg.fill_fld(i++, "工单名称", "CASENAME", -1, 8, fld_format);
        mydg.fill_fld(i++, "工单类型", "CASETYPE", 15, 1);
        mydg.set_cols_cbo_list("CASETYPE", "SELECT * FROM CRM_CASE_TABLE", "CASE_NAME", pmSys.conn_crm);
        mydg.fill_fld(i++, "用户名称", "UNAME", 22, 0);
        mydg.fill_fld(i++, "联系电话", "TEL", 15, 0);
        mydg.fill_fld(i++, "进展情况", "STATUS", 12, 1);
        mydg.set_cols_cbo_list("STATUS", "未完成,已完成");
        mydg.fill_fld(i++, "建立日期", "SDATE", 22, 5, "G(SDATE,STIME)");
        mydg.fill_fld(i++, "建立时间", "STIME", 0);
        mydg.fill_fld(i++, "业务员", "GHID", 10);
        mydg.fill_fld(i++, "TABLE_NAME", "TABLE_NAME", 0);
        mydg.fill_header("dgvList");
        //dgvCase.RowEnter += new System.Windows.Forms.DataGridViewCellEventHandler(this.dgvCase_RowEnter);
        mydg.RowDataFilled = this; // new RowDataFilledEventHandler(mydg_RowDataFilled);
        //mydg.CellLinkClicked += new CellLinkClickedEventHandler(dgvCase_CellLinkClicked); //delete by gaoww 20161116 改为在rowdatafilled中处理链接，避免翻页后再点击，会刷新到第一页，打开的内容和点击内容不符
    }

    public void mydg_RowDataFilled(Object sender, int rows)
    {
        if (rows < 0) return; //表头行，不处理
        int nCol = 0;
        String fld_value = "";
        //add by gaoww 20160226 增加按权限号码隐藏功能
        if (pmAgent.c_Levels.check_authority("phone_number_hidden") == true)
        {
            nCol = mydg.get_idx("TEL");
            fld_value = mydg.get_cell(rows, "TEL");
            fld_value = fun_main.phone_number_hidden(fld_value);
            mydg.set_cell(rows, nCol, fld_value);
        }

        //add by gaoww 20161116 改为在rowdatafilled中处理链接，避免翻页后再点击，会刷新到第一页，打开的内容和点击内容不符
        nCol = mydg.get_idx("CASEID");            
        String strCaseid = mydg.get_cell(rows, "CASEID");
        int nType = Functions.atoi(mydg.get_cell(rows, "CASETYPE"));
        int nStatus = Functions.atoi(mydg.get_cell(rows, "STATUS"));
        String strNewUrl=myString.Format("~/ut_case/case_edit.aspx?cmd=Edit&caseid={0}&ntype={1}&status={2}&from=relation", strCaseid, String.valueOf(nType),String.valueOf(nStatus));
        String strHtml = myString.Format("<a href='#this' onclick=\"fun_open('{0}','{1}',900,600);\">{1}</a>", strNewUrl, strCaseid);
        mydg.set_cell(rows, nCol, strHtml);

        nCol = mydg.get_idx("CASENAME");
        String strCaseName = mydg.get_cell(rows, "CASENAME");
        strHtml = myString.Format("<a href='#this' onclick=\"fun_open('{0}','{1}',900,600);\">{1}</a>", strNewUrl, strCaseName);
        mydg.set_cell(rows, nCol, strHtml);
    }

    private void Fillin_data()
    {
    	m_Fields="CASEID,CASENAME,CASETYPE,UNAME,TEL,STATUS,SDATE,STIME,GHID";
        String strFilter = "";
        if (pOp.equals("Relation_corp"))
        {
            DataTable dtCust_Person = Functions.dt_GetTable("SELECT USERID FROM CRM_CUSTOMER", "FIRMID='" + pUserid + "'", pmSys.conn_crm);
            if (dtCust_Person.getCount() > 0)
            {
                for(int rows=0;rows<dtCust_Person.getCount();rows++)
                {
                	if(strFilter .isEmpty())
                    strFilter = "'" +Functions.dtCols_strValue(dtCust_Person, rows, "USERID")+ "'";
                	else
                		 strFilter += ",'" +Functions.dtCols_strValue(dtCust_Person, rows, "USERID")+ "'";
                }
                if(strFilter.isEmpty())
                	strFilter ="1>1";
                else 
                	strFilter = "USERID IN("+strFilter+")";
            }
        }
        else
            strFilter = "USERID='" + pUserid + "'";

        DataTable dtRet = fun_case.get_case_by_filter(strFilter + " ORDER BY SDATE DESC,STIME DESC", m_Fields, pmSys.conn_crm);
        if (dtRet.getCount() > 0)
        {
            //add by gaoww 20161118 根据查出结果的表名来赋值mydg的Table，避免两者不一致，导致翻页报错问题
            pTableName = dtRet.getTableName();
            Save_vs("pTableName", pTableName);
            mydg.SetTable(pTableName);
            mydg.refresh(dtRet);
        }
    }

    /*private void dgvCase_CellLinkClicked(object sender, string text, int rows, int cols)
    {
        if (dgvCase.Rows.Count <= 0) return;
        string caseid = mydg.get_cell(rows, "CASEID");
        if (caseid == "") return;
        int nType = Functions.atoi(mydg.get_cell(rows, "CASETYPE"));
        int nStatus = Functions.atoi(mydg.get_cell(rows, "STATUS"));
        this.Response.Redirect(String.Format("case_edit.aspx?cmd=Edit&caseid={0}&ntype={1}&status={2}&from=relation", caseid, nType.ToString(), nStatus.ToString()));
    }*/

    private void InitToolbar()
    { 
        myToolBar.fill_fld(fun_main.Term("LBL_LIST_ALL"), "Query_all");
        if (pOp.toLowerCase().startsWith("relation") == false) //modify by gaoww 20141103 增加判断，企业和联系人查所属工单时不显示返回按钮
        {
            myToolBar.fill_fld("Separator", "Separator1", 0, 3);
            myToolBar.fill_fld(fun_main.Term("LBL_Return"), "Return");//, "return fun_return(0);");
        }
        myToolBar.fill_toolStrip("plCommand");
        myToolBar.btnItemClick = this;//new btnClickEventHandler(myToolBar_btnItemClick);
    }

    public void myToolBar_btnItemClick(Object sender, String name, String parms, int nparms)
    {   
        if (name.equals("Query_all"))
        {
            DataTable dtRet = fun_case.get_case_by_filter("USERID='" + pUserid + "'", m_Fields, pmSys.conn_crm);
            mydg.refresh(dtRet);
        }
        else if (name.equals("Return"))
        {
            //Response.Redirect(LastPageUrl);
        }
    }

    
}



