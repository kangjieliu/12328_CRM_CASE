package com.CallThink.ut_case;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpSession;

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

public class case_tracelog_list extends UltraCRM_Page{
	private String pTableName = "EVN_TRACE_LOG";// 选择文本模板
    private String pCaseId = "";
    private String ntype = "";
//    caseId = Load_vs("pCaseId");
    my_dataGrid mydg = new my_dataGrid(51);
    my_Field myFld_Query_custom = new my_Field(3);   	 
    HttpSession session = Request.getSession();
    
    public void Page_Load(Object sender, Model model) {
    	pmAgent = fun_main.GetParm();
    	 if (IsPostBack == false)//正被首次加载和访问
         {
    		 int nHistory = 0;
             pmMap res = fun_main.QuerySplit(Request);
             int rc=res.nRet;
             if(rc>0)
             {
                 HashMap htQuery = res.htRet;      
                 nHistory = Functions.atoi(Functions.ht_Get_strValue("history", htQuery));
                 pCaseId = Functions.ht_Get_strValue("caseid", htQuery);
                 ntype = Functions.ht_Get_strValue("ntype", htQuery);
                 if (nHistory > 0)  //编辑页面返回 
                 {
                 }                 
              }
             Save_ss("ntype" , ntype);  
             Save_ss("caseid" , pCaseId); 
//             session.setAttribute("pCaseId", pCaseId);
         }
    	 else 
    	 {
             ntype = Load_vs("ntype" ); 
             pCaseId = Load_vs("caseid" ); 
//             pCaseId = (String)session.getAttribute("pCaseId");
    	 }
        
      	Fillin_grid();
      	mydg.render(model);     	
    }	
    
    //填充列表
    private void Fillin_grid()
    {
    	int i = 0;
		mydg.SetConnStr(pmSys.conn_crm);
		mydg.SetTable(pTableName);
		mydg.SetPageSize(pmAgent.page_maxline - 2);
		mydg.fill_fld(i++, "AUTOID", "AUTOID", 0);
        mydg.fill_fld(i++, "操作", "SUBJECT", 30);
        mydg.fill_fld(i++, "动作-描述", "DESP", -1);           
        mydg.fill_fld(i++, "日期时间", "SDATE", 20);
        mydg.fill_fld(i++, "操作员", "GHID", 18, 1);
        mydg.set_cols_cbo_list("GHID", "SELECT GHID,REAL_NAME FROM  CTS_OPIDK", "GHID,REAL_NAME", pmSys.conn_callthink);
        mydg.fill_header("dgvList", "AUTOID", m_strFilter()+m_strOrder_by());
        mydg.RowDataFilled = this;
    }
    
    private String m_strFilter() {
    	String strFilter = "";
    	strFilter ="CASEID = '" + pCaseId+"'";
    	return strFilter;
    }
    
    //排序规则
    private String m_strOrder_by()
    {
            String strOrderby = " ORDER BY AUTOID DESC";
            return strOrderby;
    }
}
