package com.CallThink.ut_case;

import java.util.HashMap;
import java.util.List;

import org.springframework.ui.Model;

import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.pmSys;
import com.CallThink.base.pmCode.UltraCRM_Page;
import com.CallThink.cc_softcall.ATClient_webs;
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


public class case_transto_uid extends UltraCRM_Page
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
        myFld.SetMaxLabelLenth(120);
        myFld.fill_fld("工单编号", "CASEID", 30, 0);
        myFld.fill_fld("工单主题", "CASENAME", 30, 0);
        myFld.fill_fld("转发至座席", "RECV_SEATNAME", 30, 0);
        myFld.fill_fld("转发至座席号", "RECV_SEATID",30, 0); //modify by zhaoj20130905
        myFld.fill_Panel("gbEdit");
        
        myFld.set_readonly("CASEID");
        myFld.set_readonly("CASENAME");
        myFld.set_readonly("RECV_SEATNAME");
        myFld.set_readonly("RECV_SEATID");
        
        myFld.set_item_text("CASEID", pCaseId);
        my_odbc pTable = new my_odbc(pmSys.conn_crm);
        int rc = pTable.my_odbc_find(pTableName, "CASEID='" + pCaseId + "'");
        strCaseName =pTable.my_odbc_result("CASENAME");
        myFld.set_item_text("CASENAME", strCaseName);
        pTable.my_odbc_disconnect();

    }

    private void InitToolbar()
    {
        myToolBar.fill_fld("确定转发", "Update", 0, 10);
        myToolBar.fill_toolStrip("plCommand");
        myToolBar.btnItemClick = this;
    }

    public void myToolBar_btnItemClick(Object sender, String name, String parms, int nparms)
    { 
    	String strResult = "FAIL";
	    String strData = "";
        if (name.equals("Update"))
        {
            String strReal_Name = Request.getParameter("RECV_SEATNAME");
            String szDesUid = Request.getParameter("GHID");
            if(szDesUid.length()<=0)
            {
                Functions.MsgBox("请选择座席员！");
                return;
            }

            my_odbc pCase = new my_odbc(pmSys.conn_crm);
            int rc = pCase.my_odbc_find(pTableName, "caseid = '" + pCaseId + "'");
            if (rc == 1)
            {              
                pCase.my_odbc_set_new();
                pCase.my_odbc_set("currentghid", szDesUid);
                pCase.my_odbc_update(pTableName, "caseid = '" + pCaseId + "'");
                pCase.my_odbc_disconnect();     
            }
            pCase.my_odbc_disconnect();
            
           //如果坐席在线，使用ATTranMsg，弹出转发工单，如果不在线，则不作此操作
            String Sendmsg = myString.Format("TRANSMSG;FROM={0};TO={1};MSG={2};TYPE={3};", pmAgent.uid, szDesUid, pCaseId, pCaseType);
            
            ATClient_webs myWebs = new ATClient_webs();
            String strUid_Info = myWebs.ATGetUidInfo(szDesUid);
            if (strUid_Info.contains("STATUS=00") == false)
            {
                myWebs.ATSendMsg(pmAgent.uid, szDesUid, Sendmsg);
            }
            else  //add by gaoww 20160316 增加未送到消息记录
            {
                String strUrl = "/ut_case/case_edit.aspx?cmd=Edit&caseid=" + pCaseId + "&ntype=" + pCaseType;
                HashMap htTemp = new HashMap();
                htTemp.put("MTYPE", "11");               
                htTemp.put("TITLE","您有一个新工单");
                htTemp.put("CONTENT",myFld.get_item_value("CASENAME"));
                htTemp.put("URL",strUrl);
                htTemp.put("SDATE", DateTime.NowString("yyyyMMdd HHmmss"));
                htTemp.put("UNREAD","1");
                htTemp.put("GHID", szDesUid);
                my_odbc pUndeliver = new my_odbc(pmSys.conn_callthink);
                pUndeliver.my_odbc_addnew("CTS_OEM_UNDELIVERED", htTemp);
                pUndeliver.my_odbc_disconnect();
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
        String strDesName = mydg.get_cell(rows, "REAL_NAME");
        String strDesUid = mydg.get_cell(rows, "GHID");
        if (strDesUid.isEmpty()) return;
        
        int nCol = mydg.get_idx("GHID");      
        if(nCol>=0)
        {        	  
        	   String strHtml = myString.Format("<a href='#this' onclick=\"Fill_text('{0}','{1}');\">{0}</a>", strDesUid,strDesName);
        	   mydg.set_cell(rows, nCol, strHtml); 
        }
        
        nCol = mydg.get_idx("REAL_NAME");      
        if(nCol>=0)
        {        	  
        	   String strHtml = myString.Format("<a href='#this' onclick=\"Fill_text('{0}','{1}');\">{1}</a>", strDesUid,strDesName);
        	   mydg.set_cell(rows, nCol, strHtml); 
        }
    }

    private String m_strFilter()
    {
    	String strFilter = m_strFilter_base();
		if (pGroup != "")
		{
		    strFilter = m_strFilter_base() + "AND (GHID IN(SELECT GHID FROM CTS_OPGP_MEMBER WHERE GROUPS like'" + pGroup + "%')" + "AND UTYPE>0)";                    
		}
		
		//add by gaoww 20151126 增加工作流工单转发工单处理
		case_set_info myCase = new case_set_info(pCaseType);
		case_info myCaseInfo = myCase.GetCaseRecord(pCaseId);
		String strFilter_temp = "";
		if (myCase.nWF_Enable == 1)
		{
		    List<String> alUid = fun_case.GetUid_byPriv(String.valueOf(myCaseInfo), pCaseType);
		    for (String strKey: alUid)
            {
		        String strGhid = Functions.Substring(strKey, "", "-");
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
    	String strFilter = "(UTYPE>0 AND UTYPE<10)";
        return strFilter;
    }


}
