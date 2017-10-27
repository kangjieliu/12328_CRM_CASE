package com.CallThink.ut_case;

import java.util.HashMap;

import org.springframework.ui.Model;

import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.pmSys;
import com.CallThink.base.pmCode.UltraCRM_Page;
import com.ToneThink.DateTime.DateTime;
import com.ToneThink.ctsTools.WebUI.my_Field;
import com.ToneThink.ctsTools.WebUI.my_ToolStrip;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.myString;
import com.ToneThink.ctsTools.myUtility.pmMap;

public class case_hist_edit  extends UltraCRM_Page 
{ 
	 private String pId = "";  //工单历史记录表中的autoid值
     private String pOp = "addnew";
     private int pType = 0;
     private String pTableName = "CRM_CASE_HIST";

     my_ToolStrip myToolBar = new my_ToolStrip();
     my_Field myFld = new my_Field(2);
     
     private String m_Submit_res;   //提交返回结果    
     	
     public void Page_Load(Object sender, Model model)
     {
    	 if (IsPostBack == false)//正被首次加载和访问
         {
             //2010.05 peng
    		 pmMap res = fun_main.QuerySplit(Request);
     		 int rc=res.nRet;
             if(rc>0)
             { 
            	 HashMap htQuery = res.htRet;      
         	     pOp = Functions.ht_Get_strValue("cmd", htQuery);
                 pType = Functions.atoi(Functions.ht_Get_strValue("casetype", htQuery));
                 pId = Functions.ht_Get_strValue("autoid", htQuery);
             }
             Save_vs("pOp", pOp);
             Save_vs("pType", pType);
             Save_vs("autoid", pId);
         }
         else
         {
             pOp = Load_vs("pOp");
             pType = Functions.atoi(Load_vs("pType"));
             pId = Load_vs("autoid");                
         }

         if (pType != 0)
             pTableName += String.valueOf(pType);         
         
          Fillin_Field();                  	
          InitToolbar();   	
          myToolBar.render(model);
          myFld.render(model);
     }
     
     public void Fillin_Field()
     {
         myFld.fill_fld("编号", "AUTOID", 20, 0, false, false);
         myFld.fill_fld("工单编号", "CASEID", 20, 0,false,true);
         myFld.fill_fld("客户编号", "USERID", 20, 0);
         myFld.fill_fld("客户名称", "UNAME", 100, 0);
         myFld.fill_fld("呼叫编号", "CALLID", 20, 0);
         myFld.fill_fld("座席工号", "GHID", 20, 1);
         myFld.set_list("GHID", "SELECT GHID,REAL_NAME FROM CTS_OPIDK","GHID,REAL_NAME",pmSys.conn_callthink);
         myFld.fill_fld("操作日期", "SDATE", 20, 0);
         myFld.fill_fld("详细信息", "DESP", 500);
         myFld.fill_Panel ("gbEdit");
         myFld.Load(pTableName, "AUTOID='" + pId + "'", pmSys.conn_crm);
     }

     private void InitToolbar()
     {
         myToolBar.fill_fld("录音文件", "case_log",0,10);
         myToolBar.fill_fld(fun_main.Term("LBL_Return"), "Return",0,10);
         myToolBar.fill_toolStrip("plCommand"); 
         myToolBar.btnItemClick = this; // new btnClickEventHandler(myToolBar_btnItemClick);
     }

     public void myToolBar_btnItemClick(Object sender, String name, String parms, int nparms)
     { 
         if (name.equals("case_log"))
         {
             String strCallid = myFld.get_item_text("CALLID");
             String strSDate = myFld.get_item_text("SDATE");
             if (Functions.IsDateTime(strSDate) == true)
                 strSDate = Functions.ConvertStrToDateTime(strSDate).ToString("yyyyMMdd");
             else
                 strSDate = DateTime.NowString("yyyyMMdd");
             //this.Response.Redirect(String.Format(pmSys.bsCRM_homepage + "/ut_calllog/frmRecord_edit.aspx?cmd=CALLID&filter=sdate={0};callid={1};", strSDate, strCallid));
             Functions.Redirect(myString.Format(pmSys.rootURL+"/ut_calllog/frmRecord_edit.aspx?cmd=CALLID&filter=sdate={0};callid={1};", strSDate, strCallid)); //modify by gaoww 20160108 改为相对路径
         }
         else if (name.equals("Return"))
         {
             Functions.Redirect(LastPageUrl());
         }
     }

}
