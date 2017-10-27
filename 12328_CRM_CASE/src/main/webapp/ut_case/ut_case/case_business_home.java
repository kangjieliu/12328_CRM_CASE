package com.CallThink.ut_case;

import java.util.HashMap;

import javax.servlet.http.HttpSession;

import org.springframework.ui.Model;

import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmCode.UltraCRM_Page;
import com.CallThink.ut_homepage.widget.myCase;
import com.ToneThink.ctsTools.WebUI.my_Field;
import com.ToneThink.ctsTools.WebUI.my_SearchField;
import com.ToneThink.ctsTools.WebUI.my_ToolStrip;
import com.ToneThink.ctsTools.WebUI.my_dataGrid;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.myString;
import com.ToneThink.ctsTools.myUtility.pmMap;

public class case_business_home extends UltraCRM_Page{
	
	private String pTableName = "CRM_TEMPLATE";// 选择文本模板
    private String m_Filter_search = "";            //本页面人工选择的查询条件，会话需要保存在Session中 页面刷新时，该条件清空 
    my_SearchField mySearch = new my_SearchField(3);
    private String pContent = "";
    private String pMenu_id = "";
    private String pCaseId = "";
//    caseId = Load_vs("pCaseId");
    my_dataGrid mydg = new my_dataGrid(51);
    my_ToolStrip myToolBar = new my_ToolStrip();
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
                 pMenu_id = Functions.ht_Get_strValue("menu_id", htQuery);
                 //?cmd=Relation {menu_id=815}
                 pCaseId = Functions.ht_Get_strValue("caseId", htQuery);
                
                 pContent = Functions.ht_Get_strValue("content", htQuery);
                 
                 if (nHistory > 0)  //编辑页面返回 
                 {
                     String strFilter_temp = Load_ss("pFilter_search" );
                     if (strFilter_temp.length()> 0) m_Filter_search = strFilter_temp;
                     mydg.nRestore_history = 1;
                 }                 
              }
             Save_vs("pMenu_id", pMenu_id);
             Save_ss("pFilter_search", m_Filter_search); //add by gaoww 20140604 存入Session 变量，解决高级查询后，如果直接双击卡片页，再翻页，就会将之前查询结果显示出来
             Save_ss("pContent" , pContent);  
//             Save_ss("caseId" , caseId); 
             session.setAttribute("pCaseId", pCaseId);
         }
    	 else 
    	 {
    		 pMenu_id = Load_vs("pMenu_id");
             m_Filter_search = Load_ss("pFilter_search" ); //add by gaoww 20140528
             pContent = Load_vs("pContent" ); //add by 20170914
//             pCaseId = Load_vs("caseId" ); //add by 20170914
             pCaseId = (String)session.getAttribute("pCaseId");
    	 }
        
        String strFilter ="";
      	
      	myFld_Query_custom.render(model);
       	myToolBar.render(model);
       	mySearch.render(model);
      	mydg.render(model);     	
    }	
	public void myToolBar_btnItemClick(Object sender, String name, String parms, int nparms) {
		String strNewUrl, strHtml = "";
		int nType = 0;
		String strTiltle = "";

		strNewUrl = myString.Format("~/ut_case/case_business_home.aspx?caseid={0}&table={1}&casetype={2}&nType={3}",
				"", "", "", "");
		
			if (nType == 0)
				strNewUrl = myString.Format("~/ut_case/case_business_home.aspx?caseid={0}&table={1}&casetype={2}",
						"", "", "");
			else if (nType == 1)
				strNewUrl = myString.Format("~/ut_case/case_business_home.aspx?caseid={0}&table={1}&casetype={2}",
						"", "", "");
			else if (nType == 2)
				strNewUrl = myString.Format("~/ut_case/case_business_home.aspx?caseid={0}&table={1}&casetype={2}",
						"", "", "");
		
		// strHtml = String.Format("fun_open('{0}','{1}',900,600);",
		// strNewUrl, strTiltle); //add by zhaoj 20130902 显示为弹出框的形式
//		strHtml = myString.Format("fun_open('{0}','{1}',900,600);", strNewUrl, strTiltle); // add
																							// by
																							// zhaoj
																							// 20130902
																							// 显示为弹出框的形式

//		Functions.js_exec(strHtml);
	}
}
