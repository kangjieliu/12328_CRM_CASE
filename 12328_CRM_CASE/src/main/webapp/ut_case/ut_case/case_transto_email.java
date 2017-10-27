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
import com.ToneThink.ctsTools.WebUI.my_Field;
import com.ToneThink.ctsTools.WebUI.my_ToolStrip;
import com.ToneThink.ctsTools.WebUI.my_dataGrid;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.myString;
import com.ToneThink.ctsTools.myUtility.pmMap;


public class case_transto_email extends UltraCRM_Page
{

    private String pTableName = "CRM_CASE";
    private String pCaseId = "";
    private int pCaseType = 0;
    private String pGroup = ""; //业务组号，add by gaoww 20150511 增加按业务组显示
   
    //临时封上
    //private string email_attach_path;
    //OpenSmtp.Mail.Smtp mySmtp = new OpenSmtp.Mail.Smtp();
    
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

    public void mydg_RowDataFilled(Object sender, int rows)
    {
    	 if (rows < 0) return;
    	 String strEmail = mydg.get_cell(rows, "EMAIL");
    	 String strUid = mydg.get_cell(rows, "GHID");
         if (strEmail.isEmpty()) return;
         
         int nCol = mydg.get_idx("GHID");      
         if(nCol>=0)
         {        	  
         	   String strHtml = myString.Format("<a href='#this' onclick=\"Fill_text('{1}');\">{0}</a>", strUid,strEmail);
         	   mydg.set_cell(rows, nCol, strHtml); 
         }
         
         nCol = mydg.get_idx("EMAIL");      
         if(nCol>=0)
         {        	  
         	   String strHtml = myString.Format("<a href='#this' onclick=\"Fill_text('{1}');\">{1}</a>", strUid,strEmail);
         	   mydg.set_cell(rows, nCol, strHtml); 
         }
    }

	 public void Fillin_Field()
     {
        String strCaseName = "";
        myFld.fill_fld("工单编号", "CASEID", 30, 0);
        myFld.fill_fld("工单主题", "CASENAME", 100, 0);
        myFld.fill_fld("转移至Email", "EMAIL", 200, 10, true, false, "", "", "* 多个邮件用\";\"隔开");  //add by zhaoj 20130829
        myFld.fill_fld("Email内容", "BODY", 100, 0);

        myFld.fill_Panel("gbEdit");
        myFld.set_readonly("CASEID");
        myFld.set_readonly("CASENAME");
        //myFld.get_item("EMAIL").Width = 603;

        myFld.set_item_text("CASEID", pCaseId);
        my_odbc pTable = new my_odbc(pmSys.conn_crm);
        int rc = pTable.my_odbc_find(pTableName, "CASEID='" + pCaseId + "'");
        strCaseName = pTable.my_odbc_result("CASENAME" );
        myFld.set_item_text("CASENAME", strCaseName);
        myFld.set_item_text("BODY", "您好，此邮件为CRM的工单转发邮件，发送人：" + pmAgent.name + "，工单主题：" + strCaseName + "，请及时查看处理！");
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
	    String szDesUid = "", strEmailFile = "", strMsg_Wrong = "";
        if (name.equals("ok"))
        {
        	szDesUid =Request.getParameter("EMAIL");              
            //string strMsg = "";
            if (szDesUid.isEmpty())
            {
                Functions.MsgBox("转发的Email地址为空，转发失败！");
                return;
            }

            //delete by gaoww 20121214 bs crm暂时封上工单输出作为邮件附件功能
            /*strEmailFile = "";
            strEmailFile = Get_CaseFile(pCaseId);  //"c:\\1.doc";
            //int IFlag = 1;
            if (strEmailFile == "")
                Functions.MsgBox("发送EMAIL失败，原因：文件 \\TempData\\case.doc未生成！");*/

            //Regex rg = new Regex("^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$");
            String[] strEmails = szDesUid.split(";");
            for(String email : strEmails)
            {
                if (email.length() > 0)
                {    
                    if (Functions.isEmailAddress(email))  //判断邮件格式是否合法
                        Send_EmailFile(strEmailFile, email);
                    else
                        strMsg_Wrong += email+";";
                }
            }
            if (strMsg_Wrong.length() > 0)
            {
                Functions.MsgBox("给" + strMsg_Wrong + "发送EMAIL失败，原因：邮件格式不正确！");
                myFld.set_item_text("EMAIL", strMsg_Wrong);
                return;
            }
            strResult = "OK";
    	    strData = "转发工单成功！";
    	  	m_Submit_res = fun_main.getResult(name, strResult, strData);    	    
            Functions.js_exec("javascript:window_close();");   //modify by gaoww 20150511 改为由父窗体关闭
       
        }
    }

    private int Send_EmailFile(String strEmailFile, String strAddr)
    {

       /* try
        {
            fun_media.GetParaIni();
            OpenSmtp.Mail.EmailAddress from = new OpenSmtp.Mail.EmailAddress(pmSys_media.email_addr, pmAgent.name);

            string to_addr = strAddr.Trim();
            OpenSmtp.Mail.EmailAddress to = new OpenSmtp.Mail.EmailAddress(to_addr, "");

            OpenSmtp.Mail.MailMessage myMessage = new OpenSmtp.Mail.MailMessage(from, to);

            //myMessage.AddRecipient(to_addr,AddressType.To);  
            string strSubject;
            strSubject = myFld.get_item_text("CASENAME");   //主题
            myMessage.Subject = strSubject.Trim();
            string strBody;
            strBody = myFld.get_item_text("BODY");
            myMessage.Body = strBody;
            myMessage.Charset = "GB2312";
            myMessage.Priority = "1";
            myMessage.Notification = false;
            //delete by gaoww 20121214 bs crm暂时封上工单输出作为邮件附件功能
            //myMessage.AddAttachment(strEmailFile);//end by zhanglh 20080114


            //hanxy 2006.03
            mySmtp.AuthenticationMode = AuthenticationType.Auto;
            if (pmSys_media.cfg_smtp_auth == "1")
                mySmtp.AuthenticationMode = AuthenticationType.None;
            else if (pmSys_media.cfg_smtp_auth == "2")
                mySmtp.AuthenticationMode = AuthenticationType.Plain;
            else if (pmSys_media.cfg_smtp_auth == "3")
                mySmtp.AuthenticationMode = AuthenticationType.Login;
            else if (pmSys_media.cfg_smtp_auth == "4")
                mySmtp.AuthenticationMode = AuthenticationType.Cram_MD5;

            if (pmSys_media.cfg_smtp_Ssl == 1)
                mySmtp.EnableSsl = true;
            else if (pmSys_media.cfg_smtp_Ssl == 2)
                mySmtp.UseStartTls = true;

            if (pmSys_media.cfg_smtp_log == "0")
                mySmtp.SmtpLog = false;
            else
            {
                mySmtp.SmtpLog = true;
                mySmtp.SmtpLogPath = pmSys_media.cfg_smtp_log_path;
            }
            mySmtp.Host = pmSys_media.cfg_smtpServer_ip;
            mySmtp.Port = pmSys_media.cfg_smtpServer_port;  //"smtp:168.168.168.100:25";
            mySmtp.Username = pmSys_media.email_uid;
            mySmtp.Password = pmSys_media.email_pwd;

            mySmtp.MailMessage = myMessage;
            mySmtp.OnMailSent += new OpenSmtp.Mail.Smtp.MailSentEventHandler(OnMailSent);
            mySmtp.SendMail();

            strBody = myMessage.ToString();

            //add by gaoww 20090720 将发送的邮件保存为ibx文件，并上传到ftp服务器上，方便在发件箱中查看已发邮件html内容及附件                
            string strFile_Path = pmSys.web_phypath_crm + "\\tmpdir\\" + DateTime.Now.ToString("yyyyMMddHHmmss") + ".obx";
            string strRemoteFile = String.Format(".\\emaildata\\{0}\\{1}.obx", pmAgent.uid, DateTime.Now.ToString("yyyyMMddHHmmss"));  //ftp服务器上的路径

            myMessage.Save(strFile_Path);

            //将把保存在本地的文件上传到ftp服务器上                
            my_FTPClient theFtp = new my_FTPClient(pmSys_media.ftp_email_host, pmSys_media.ftp_email_uid, pmSys_media.ftp_email_pwd);
            theFtp.Upload(strFile_Path, strRemoteFile);
            theFtp.Close();
            //************************************************************************************************************

            my_odbc pCust = new my_odbc(pmSys.conn_callthink);
            pCust.my_odbc_set_new();

            string strCallid = String.Format("{0}{1}{2}", DateTime.Now.ToString("yyyyMMdd"), DateTime.Now.ToString("HHmmss"), "0000");
            pCust.my_odbc_set("CALLID", strCallid);
            pCust.my_odbc_set("FROM_ADDR", pmSys_media.email_addr);
            pCust.my_odbc_set("FROM_NAME", pmAgent.name);
            pCust.my_odbc_set("TO_ADDR", to_addr);
            pCust.my_odbc_set("CC_ADDR", "");
            pCust.my_odbc_set("BCC_ADDR", "");
            pCust.my_odbc_set("SUBJECTS", strSubject);
            pCust.my_odbc_set("BODYS", strBody);
            if (strEmailFile != "")
                pCust.my_odbc_set("ATTACH1", strEmailFile);
            pCust.my_odbc_set("FILE_EBX", strRemoteFile); //add by gaoww 20090720 增加ibx路径的保存，在查看已发邮件时使用 
            pCust.my_odbc_set("LEVELS", 0);//List_Priority.SelectedValue);
            pCust.my_odbc_set("SDATE", DateTime.Now.ToString("yyyyMMdd"));//,HH:mm:ss"));
            pCust.my_odbc_set("STIME", DateTime.Now.ToString("HHmmss"));
            pCust.my_odbc_set("VFLAG", 0);
            pCust.my_odbc_set("DELETED", 0);
            pCust.my_odbc_set("GHID", pmAgent.uid);
            pCust.my_odbc_addnew("MCI_EMAIL_SENT", "");
            pCust.my_odbc_disconnect();

            Functions.MessageBox(this, "邮件已按上述条件成功上载到服务器！");
        }
        catch (SmtpException se)
        {
            //MessageBox.Show(String.Format("OpenFile Err:{0}", ex.Message),"Error", MessageBoxButtons.OK, MessageBoxIcon.Exclamation); 

            Functions.MsgBox(se.Message.ToString(), "SendEmail() fail: " + se.Message);

            return 0;
        }
        catch (System.Exception ex)
        {
            Functions.MsgBox(ex.Message.ToString(), "SendEmail() fail: " + ex.Message + "; Target: " + ex.TargetSite);
            return 0;
        }*/
        return 1;

    }

    /*private void OnMailSent(object sender, int result, string reason)
    {
        //	statusBar.Text = "Mail sent.";
        Functions.MessageBox(this, "Result:" + result + ",Reason:" + reason);
        Functions.MsgBox("Result:" + result + ",Reason:" + reason);
    }*/

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
    	String strFilter = "((UTYPE>0 AND UTYPE<10) AND EMAIL IS NOT NULL AND EMAIL !='')";
            return strFilter;
     
    }

}
