package com.CallThink.ut_case;

import java.util.HashMap;
import java.util.List;

import org.springframework.ui.Model;

import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.pmSys;
import com.CallThink.base.pmCode.UltraCRM_Page;
import com.ToneThink.ctsTools.WebUI.my_ToolStrip;
import com.ToneThink.ctsTools.WebUI.my_dataGrid;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.pmMap;

public class case_trace_log  extends UltraCRM_Page
{ 
	 my_dataGrid mydg = new my_dataGrid(50);
     my_ToolStrip myToolBar = new my_ToolStrip();
     private String pCaseid = "";
     private String pType = ""; //add by gaoww 20160901 增加工单类型
     private String pFilter = "";
     private String pTableName = "CRM_CASE_TRACE";
        	 
    public void Page_Load(Object sender, Model model) {
    	if (IsPostBack == false)//正被首次加载和访问 
        {
    		pmMap res = fun_main.QuerySplit(Request);
            int rc=res.nRet;
            if(rc>0)
            {
            	HashMap htQuery = res.htRet;      
                /*注:为保证查询后返回该页面的工单类型不变，必须在crm_menu_bs中，将工单页面的参数设为“ntype=”，否则在进入页面时会使用查询的filter字段记录工单类型，查询后记录工单类型的字段会被查询条件覆盖，导致总返回到工单类型为0的列表汇总*/
                pCaseid = Functions.ht_Get_strValue("caseid", htQuery);
                pFilter = Functions.ht_Get_strValue("filter", htQuery);
                pType = Functions.ht_Get_strValue("casetype", htQuery);
            }
            Save_vs("pCaseid", pCaseid);
            Save_vs("pFilter", pFilter);
            Save_vs("pType", pType);
        }
        else
        {
            pCaseid = Load_vs("pCaseid");
            pFilter = Load_vs("pFilter");
            pType = Load_vs("pType");
        }
    	 

        if (pCaseid.isEmpty()) //如果工单编号为null则表示是工单轨迹管理，显示按钮，否则只显示列表
        {
        	InitToolbar();
        	myToolBar.render(model);
         }
        
        String strFilter ="";
      	strFilter = m_strFilter_base() + m_strOrder_by();;
      	Fillin_grid(strFilter);
     	mydg.render(model);
    }	
  
    private void Fillin_grid(String strFilter)
    {
        int i = 0;
        mydg.SetTable(pTableName);
        mydg.SetConnStr(pmSys.conn_crm);
        mydg.SetPageSize(25);

        if (pCaseid.isEmpty())
            mydg.fill_fld(i++, "选择", "SELECT", 5, 9);
        mydg.fill_fld(i++, "AUTOID", "AUTOID", 0);
        mydg.fill_fld(i++, "工单编号", "CASEID", 16);
        mydg.fill_fld(i++, "工单名称", "CASENAME", -1);
        mydg.fill_fld(i++, "工单类型", "CASETYPE", 0, 1);
        mydg.set_cols_cbo_list("CASETYPE", "SELECT * FROM CRM_CASE_TABLE", "CASETYPE,CASE_NAME", pmSys.conn_crm);
        mydg.fill_fld(i++, "工作流实例ID", "INSTANCEID", 0);
        mydg.fill_fld(i++, "工单状态", "STATUS", 0);

        mydg.fill_fld(i++, "转入日期", "SDATE_RECV", 16);
        mydg.fill_fld(i++, "发送人", "GHID_PREV", 8);

        mydg.fill_fld(i++, "前一环节", "PS_PREV", 10, 1);
        mydg.set_cols_cbo_list("PS_PREV", "SELECT * FROM CRM_CASE_PROCESS WHERE CASETYPE='"+pType +"'", "PROCESS_ID,PROCESS_NAME", pmSys.conn_crm);
        mydg.fill_fld(i++, "当前环节", "PROCESS", 10, 1);
        mydg.set_cols_cbo_list("PROCESS", "SELECT * FROM CRM_CASE_PROCESS WHERE CASETYPE='" + pType + "'", "PROCESS_ID,PROCESS_NAME", pmSys.conn_crm);
        mydg.fill_fld(i++, "环节状态", "PS_STATUS_RECV", 12, 1);
        mydg.set_cols_cbo_list("PS_STATUS_RECV", "0-未签收,1-已签收,2-拒签收,3-已提交,4-被退回");

        //mydg.fill_fld(i++, "转出日期", "SDATE_SEND", 16);
        mydg.fill_fld(i++, "业务员", "GHID", 8);
        mydg.fill_fld(i++, "解决时限", "DATE_EXP", 0);
        mydg.fill_fld(i++, "是否超时", "TIMEOUT", 8, 1);
        mydg.fill_fld(i++, "超时时长", "TIMEOUTLEN", 8);
        mydg.set_cols_cbo_list("TIMEOUT", "否,是");

        mydg.fill_header("dgvList", "AUTOID", strFilter);
    }

    private void InitToolbar()
    { 
        myToolBar.fill_fld(fun_main.Term("LBL_Search"), "Find",0,10);
        myToolBar.fill_fld(fun_main.Term("LBL_LIST_ALL"), "Refresh",0,10);
        myToolBar.fill_fld("Separator", "Separator2", 0, 3);
        //myToolBar.fill_fld_confirm("删除", "Delete", "确实要删除选中的记录吗？");
        myToolBar.fill_fld("删除", "Delete", 0,10);

        myToolBar.fill_toolStrip("plCommand");
        myToolBar.btnItemClick = this;//new btnClickEventHandler(myToolBar_btnItemClick);
    }


   
    public void myToolBar_btnItemClick(Object sender, String name, String parms, int nparms)
    {   
        if (name.equals("Refresh"))
        {
            //mydg.refresh("AUTOID", "1=1 ORDER BY AUTOID DESC");
            String strFilter = m_strFilter_base() + m_strOrder_by();
            mydg.refresh("AUTOID", strFilter);
        }
        else if (name.equals("Delete"))
        {
            int nPos = -1;
        	List<String>  alRet = mydg.GetSelectedKey("CASEID");            
        	for (String strAutoid: alRet)
            {
                nPos = 1;
                my_odbc pTable = new my_odbc(pmSys.conn_crm);
                pTable.my_odbc_delete(pTableName, "AUTOID='" + strAutoid + "'");
                pTable.my_odbc_disconnect();
            }
            if (nPos == -1)
                Functions.MsgBox("请先选中要删除的记录！");
            else
            {
            	String strFilter = m_strFilter_base() + m_strOrder_by();
                mydg.refresh("AUTOID", strFilter);
                Functions.MsgBox("提示", "选中记录删除成功！");
            }
        }
    }

    //基本的显示条件
    private String m_strFilter_base()
    {
    	String strFilter = "";
        strFilter = pFilter;
        if (pCaseid.isEmpty())
        {
            strFilter = "1>1";
        }
        else
        {
            if (strFilter.length() > 0) strFilter = " AND ";
            strFilter = "(CASEID='" + pCaseid + "')";
        }
        return strFilter;
    }

    //排序规则
    private String m_strOrder_by()
    {
    	String strOrderby = " ORDER BY AUTOID DESC";
        return strOrderby;
    }
}



