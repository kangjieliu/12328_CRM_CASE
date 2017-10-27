package com.CallThink.ut_case;

import java.util.HashMap;
import java.util.List;

import org.springframework.ui.Model;

import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.pmSys;
import com.CallThink.base.pmCode.UltraCRM_Page;
import com.CallThink.ut_case.pmModel_case.case_info;
import com.CallThink.ut_case.pmModel_case.case_set_info;
import com.CallThink.ut_case.pmModel_case.fun_case;
import com.ToneThink.DateTime.DateTime;
import com.ToneThink.ctsTools.WebUI.my_Field;
import com.ToneThink.ctsTools.WebUI.my_ToolStrip;
import com.ToneThink.ctsTools.WebUI.my_dataGrid;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.myString;
import com.ToneThink.ctsTools.myUtility.pmMap;


public class case_transto_sms extends UltraCRM_Page
{

    private String pTableName = "CRM_CASE";
    private String pCaseId = "";
    private int pCaseType = 0;
    private String pGroup = ""; //业务组号，add by gaoww 20150511 增加按业务组显示
   
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
				 pCaseId = Functions.ht_Get_strValue("caseid", htQuery);
                 pTableName = Functions.ht_Get_strValue("table", htQuery);
                 pCaseType = Functions.atoi(Functions.ht_Get_strValue("casetype", htQuery));
                 pGroup = Functions.ht_Get_strValue("group", htQuery);
			}
			 Save_vs("caseid", pCaseId);
             Save_vs("table", pTableName);
             Save_vs("casetype", pCaseType);
             Save_vs("pGroup", pGroup);
		} 
		else
		{
			 pCaseId = Load_vs("caseid");
             pTableName = Load_vs("table");
             pCaseType = Functions.atoi(Load_vs("casetype"));
             pGroup = Load_vs("pGroup");
		}      

		InitToolbar();
		String strFilter=m_strFilter();
		Fillin_grid(strFilter);
		Fillin_Field();
		
	    myToolBar.render(model);
        myFld.render(model);
     	mydg.render(model);
	}

	 private void Fillin_grid(String strFilter)
     {
        int i = 0;
        mydg.SetTable("cts_opidk");
        mydg.SetSelectStr("SELECT * FROM CTS_OPIDK WHERE 1>1");
        mydg.SetConnStr(pmSys.conn_callthink);
        mydg.SetCaption("座席人员资料列表");
        mydg.SetPageSize(9);
        mydg.SetPagerMode(2);
       
        //mydg.AllowRowsNumVisibale( true);       
        mydg.fill_fld(i++, "工号", "GHID", 8, 0);   //modify by zhaoj 20130905 把link改为textbox
        mydg.fill_fld(i++, "姓名", "REAL_NAME", 20, 0);

        mydg.fill_fld(i++, "邮件地址", "EMAIL", 40, 0);
        mydg.fill_fld(i++, "手机", "MOBILENO", -1, 0);
        
        mydg.RowDataFilled = this;
        mydg.fill_header("dgvList", "GHID", strFilter);
    }


	 public void Fillin_Field()
     {
        String strCaseName = "";
        myFld.fill_fld("工单编号", "CASEID", 30, 0);
        myFld.fill_fld("工单主题", "CASENAME", 50, 0);
        myFld.fill_fld("发短信至电话", "MOBILENO", 200, 10, true, false, "", "", "* 多个短信用\";\"隔开");  //modify by zhaoj 20130829
        myFld.fill_fld("短信内容", "BODY", 100, 0);
        myFld.fill_Panel("gbEdit");
        
        myFld.set_readonly("CASEID");
        myFld.set_readonly("CASENAME");
        //myFld.get_item("MOBILENO").Width = 603;

        
        myFld.set_item_text("CASEID", pCaseId);
        my_odbc pTable = new my_odbc(pmSys.conn_crm);
        int rc = pTable.my_odbc_find(pTableName, "CASEID='" + pCaseId + "'");
        strCaseName =pTable.my_odbc_result("CASENAME");
        myFld.set_item_text("CASENAME", strCaseName);
        myFld.set_item_text("BODY", "您好，此短信为CRM的工单转发短信通知，发送人：" + pmAgent.name + "，工单主题：" + strCaseName + "，请及时查看处理！");
        pTable.my_odbc_disconnect();

    }

    private void InitToolbar()
    {
    	myToolBar.fill_fld("确定转发", "ok");
        myToolBar.fill_toolStrip("plCommand");
        //myToolBar.btnItemClick += new btnClickEventHandler(myToolBar_btnItemClick);
    }

    public void myToolBar_btnItemClick(Object sender, String name, String parms, int nparms)
    { 
    	String strResult = "FAIL";
	    String strData = "";
	    String szDesUid = "", strMsg = "", strMsg_Wrong = "";
        if (name.equals("ok"))
        {
        	szDesUid = Request.getParameter("MOBILENO");
            strMsg = myFld.get_item_text("BODY");

            if (szDesUid .isEmpty())
            {
                Functions.MsgBox("转发的移动电话号码为空，转发失败！");
                return;
            }
            if (strMsg.isEmpty())
            {
                Functions.MsgBox("工单内容为空，转发失败！");
                return;
            }
      
            String[] strSms = szDesUid.split(";");  //modify by zhaoj 20130829
            for (String sms : strSms)
            {
            	 if(Functions.IsTeleNum(sms))
                    Addto_Sms_Send(sms, strMsg);
            }
            if (strMsg_Wrong.length() > 0)
            {
                myFld.set_item_text("MOBILENO", strMsg_Wrong);
                Functions.MsgBox("手机号码<" + strMsg_Wrong + ">格式不正确！");
                return;
            }
            strResult = "OK";
    	    strData = "转发工单成功！";
    	  	m_Submit_res = fun_main.getResult(name, strResult, strData);    	    
            Functions.js_exec("javascript:window_close();");   //modify by gaoww 20150511 改为由父窗体关闭

        }
    }

    public void mydg_RowDataFilled(Object sender, int rows)
    {
    	 if (rows < 0) return;
    	 String strMobileNol = mydg.get_cell(rows, "MOBILENO");
    	 String strUid = mydg.get_cell(rows, "GHID");
         if (strMobileNol .isEmpty()) return;
         
         int nCol = mydg.get_idx("GHID");      
         if(nCol>=0)
         {        	  
         	   String strHtml = myString.Format("<a href='#this' onclick=\"Fill_text('{1}');\">{0}</a>", strUid,strMobileNol);
         	   mydg.set_cell(rows, nCol, strHtml); 
         }
         
         nCol = mydg.get_idx("MOBILENO");      
         if(nCol>=0)
         {        	  
         	   String strHtml = myString.Format("<a href='#this' onclick=\"Fill_text('{1}');\">{1}</a>", strUid,strMobileNol);
         	   mydg.set_cell(rows, nCol, strHtml); 
         }
    }

  

    /// <summary>
    /// 发送短信息
    /// </summary>
    /// <param name="tel">单个电话号码</param>
    /// <param name="strMsg">短信息内容</param>
    private void Addto_Sms_Send(String tel, String strMsg)
    {
    	String strDate, strTime;
        strDate = DateTime.NowString("yyyyMMdd");
        strTime = DateTime.NowString("HHmmss");
        String body_to = strMsg.trim();
        my_odbc myodbc = new my_odbc(pmSys.conn_callthink); 
        String strCallid = DateTime.NowString("yyyyMMddHHmmss") +Functions.get_random(4,1);
        myodbc.my_odbc_set_new();
        myodbc.my_odbc_set("CALLID", strCallid);
        myodbc.my_odbc_set("TEL_TO", tel);
        myodbc.my_odbc_set("SUBJECTS", "");
        myodbc.my_odbc_set("RETRY", "3");
        myodbc.my_odbc_set("GHID", pmAgent.uid);
        myodbc.my_odbc_set("SDATE", strDate);
        myodbc.my_odbc_set("STIME", strTime);
        myodbc.my_odbc_set("UNAME", "");
        myodbc.my_odbc_set("LEVELS", "");
        myodbc.my_odbc_set("INTERVL", 10);
        myodbc.my_odbc_set("UINFO", "");
        myodbc.my_odbc_set("BODYS", body_to);
        myodbc.my_odbc_set("RFLAG", 0);

        myodbc.my_odbc_addnew("MCI_SMS_SEND", "");
        myodbc.my_odbc_disconnect();
    }

    private String m_strFilter()
    {
    	String strFilter = m_strFilter_base();
        if (pGroup != "")
        {
            strFilter = m_strFilter_base() + "AND (GHID IN(SELECT GHID FROM CTS_OPGP_MEMBER WHERE GROUPS LIKE '" + pGroup + "%')" + "AND UTYPE>0)";
        }

        //add by gaoww 20151126 增加工作流工单转发工单处理
        case_set_info myCase = new case_set_info(pCaseType);
        case_info myCaseInfo = myCase.GetCaseRecord(pCaseId);
        String strFilter_temp = "";
        if (myCase.nWF_Enable == 1)
        {
        	 List<String> alUid = fun_case.GetUid_byPriv(String.valueOf(myCaseInfo), pCaseType);
 		    for (String strItem: alUid)
            {
 		    	String strGhid = Functions.Substring(strItem, "", "-");
                if (strFilter_temp != "") strFilter_temp += ",";
                strFilter_temp += "'" + strGhid + "'";
            }
            if (strFilter_temp != "")
                strFilter = m_strFilter_base() + "AND (GHID IN(" + strFilter_temp + ")" + "AND UTYPE>0)";

        }
        return strFilter;
    }
    //基本的显示条件
    private String m_strFilter_base()
    {
    	String strFilter = "((UTYPE>0 AND UTYPE<10) AND MOBILENO IS NOT NULL AND MOBILENO !='')";
        return strFilter;
    }

}
