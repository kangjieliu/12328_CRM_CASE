package com.CallThink.ut_case;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.ui.Model;

import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.pmSys;
import com.CallThink.base.pmCode.UltraCRM_Page;
import com.ToneThink.DateTime.DateTime;
import com.ToneThink.FileUpload.FileUpload;
import com.ToneThink.ctsTools.WebUI.my_Field;
import com.ToneThink.ctsTools.WebUI.my_ToolStrip;
import com.ToneThink.ctsTools.WebUI.my_dataGrid;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.myFile;
import com.ToneThink.ctsTools.myUtility.myString;
import com.ToneThink.ctsTools.myUtility.pmMap;
import com.ToneThink.ctsTools.myUtility.pmRet;
import com.ToneThink.ctsTools.net.my_FTPClient;

public class case_attach  extends UltraCRM_Page
{ 
	private String pTableName = "CRM_CASE_ATTACH";
    private String pCaseId = "";
    //private string pCasename = "";
    private int pType = 0;
    my_ToolStrip myToolBar = new my_ToolStrip();
    my_dataGrid mydg = new my_dataGrid(51);    
    my_Field myFld = new my_Field(1);
        	 
    public void Page_Load(Object sender, Model model) {
    	Functions.setSession("nCountFile", 1);    	 
    	if (IsPostBack == false)//正被首次加载和访问
         {
             pmMap res = fun_main.QuerySplit(Request);
             int rc=res.nRet;
             if(rc>0)
             {
            	 HashMap htQuery = res.htRet;      
                 pType = Functions.atoi(Functions.ht_Get_strValue("casetype", htQuery)); //获取通用form类型 
                 pCaseId = Functions.ht_Get_strValue("caseid", htQuery);
             }
             Save_vs("pType", pType);
             Save_vs("pCaseId", pCaseId);
         }
         else
         {
             pType = Functions.atoi(Load_vs("pType"));
             pCaseId = Load_vs("pCaseId");
         }
    	InitToolbar();
    	Fillin_Field();     	
        Fillin_grid();
    	myToolBar.render(model);
     	myFld.render(model);
      	mydg.render(model);
    }	
  
    public String doUpload(HttpServletRequest request,String strName,int nType){
        myFld_FieldLinkClicked(request, strName, null, -1,  nType);
      	String jnRet = m_Submit_res;//myToolBar_btnItemClick(strCmd);
        return jnRet;
    }
    
    private void Fillin_grid()
    {
        int i = 0;
        mydg.SetCaption ("工单附件记录");
        mydg.SetTable( pTableName);
        mydg.SetConnStr(pmSys.conn_crm);

        mydg.fill_fld(i++, "选择", "SELECT", 5, 9);
        mydg.fill_fld(i++, "编号", "AUTOID", 0);
        mydg.fill_fld(i++, "工单编号", "CASEID", 15);
        //mydg.fill_fld(i++, "工单名称", "CASENAME", 15);
        mydg.fill_fld(i++, "工单类型", "CASETYPE", 20,1);
        mydg.set_cols_cbo_list("CASETYPE", "select CASETYPE,CASE_NAME as CASE_NAME  from CRM_CASE_TABLE", "CASETYPE,CASE_NAME", pmSys.conn_crm);
        mydg.fill_fld(i++, "附件路径", "ATTACH_FILE_PATCH", 0);
        mydg.fill_fld(i++, "附件名称", "ATTACH_FILE_NAME", 15);
        mydg.fill_fld(i++, "上传人", "GHID", 10, 1);
        mydg.set_cols_cbo_list("GHID", "select ghid,real_name from cts_opidk", "ghid,real_name", pmSys.conn_callthink);
        mydg.fill_fld(i++, "上传日期", "SDATE", 15);        
        //mydg.fill_fld(i++, "下载", "", 8, 8, "CMDNAME=DownLoad;;");
        String strUrl = fun_main.CreateHtml_img("download.gif", "");
        mydg.fill_fld(i++, "下载", "", 8, 8, "CMDNAME=DownLoad;TEXT="+strUrl+";");
        mydg.fill_header("dgvList", "AUTOID", "CASEID = '" + pCaseId + "'");
        mydg.CellLinkClicked =this;
    }    
    
    
    public void mydg_CellLinkClicked(Object sender, String text, int rows, int cols)        
    {
        if (mydg.RowCount() <= 0) return;
        if (text.equals("DownLoad"))
        { 
        	String ref_filename = mydg.get_cell(rows, "ATTACH_FILE_PATCH");
            Functions.Download_ftpfile(ref_filename, pmSys.ftp_crm_host, pmSys.ftp_crm_uid, pmSys.ftp_crm_pwd);
        }
    }
  
    //工具栏
    private void InitToolbar()
    { 
        myToolBar.fill_fld("上传附件", "ShowUpload", 0, 10);
        myToolBar.fill_fld("开始上传", "Upload", 0, 10);
        myToolBar.fill_fld("Separator", "Separator0", 0, 3);
        myToolBar.fill_fld("删除", "Delete", 0, 10);
        myToolBar.fill_toolStrip("plCommand");
        myToolBar.btnItemClick = this;//new btnClickEventHandler(myToolBar_btnItemClick);
    }

    public void myToolBar_btnItemClick(Object sender, String name, String parms, int nparms)
    { 
    	if(name.equals("Upload"))
    	{
    		btnUploadInto_ftp(); //上传到ftp目录下
    		mydg.refresh("AUTOID", "CASEID = '" + pCaseId + "'");
    	}
        if (name.equals("Delete"))
        {
            int nPos = 0;
            {
            	List<String>  alRet = mydg.GetSelectedKey("AUTOID");
	            if (alRet.size()  == 0)
	            {
	            	 Functions.MsgBox("请先选中需要删除的记录！");
	                 return;
	            }
	            else
	            {
	            	for (String strId: alRet)
	                {	                    
                        //string strId = mydg.get_cell(i, "AUTOID");
                        my_odbc pTable = new my_odbc(pmSys.conn_crm);
                        pTable.my_odbc_delete(pTableName, "AUTOID='" + strId + "'");
                        pTable.my_odbc_disconnect();
                        nPos++;
	                }
	                if (nPos <= 0)
	                {
	                    Functions.MsgBox("请先选中要删除的记录！");
	                }
	                else
	                    Functions.MsgBox("提示","删除了" + String.valueOf(nPos) + "条记录！");
	                mydg.refresh("AUTOID", "CASEID = '" + pCaseId + "'");
	            }
            }
        }
    }
    
    private void Fillin_Field()
    {  
    	myFld.funName_OnClientClick("myFld_FieldLinkClicked");
    	myFld.fill_fld("附件", "ATTACH", 100, 8);
    	myFld.fill_Panel("gbEdit");
        myFld.FieldLinkClicked =this;
    }

    public void myFld_FieldLinkClicked(Object sender, String name, String parms, int nparms, int ntype)
    {
        if ((ntype == 8)) //8  upload
        {
          	FileUpload fuUpload = new FileUpload((HttpServletRequest)sender);
            if (fuUpload.HasFile() == false) return;
            String strUrl =  Upload_imgFile(fuUpload, null);
            if(strUrl.length()>0)
            	m_Submit_res = fun_main.getResult(name, "OK", strUrl);
        }
    }
    
    //上传到站点目录下
    private String Upload_imgFile(FileUpload fuUpload,String strFld_name1)
    {
  	   String strReturn = "";
  	   if (fuUpload != null)
        {
            String strFileName = fuUpload.getFileName();
            if (strFileName.length() > 1)
            {
                boolean bOverWrite = true;                
                String pmSys_web_phypath = myFile.MapPath("/");
                String strLocal_path = myString.Format("{0}_App_Data\\crmdata\\attach\\", pmSys_web_phypath);
                
                if (myFile.DirectoryExists(strLocal_path) == false)
                {
              	  myFile.CreateDirectorys(strLocal_path);  //可以自动建立子目录
                }
                String strFullName = strLocal_path + strFileName;
                if (myFile.Exists(strFullName) == true)
                {
                    if (bOverWrite == false)
                    {
                        String strTemp_name = myFile.getFileNameWithoutExtension(strFullName);
                        String strTemp_ext = myFile.getExtension(strFullName);
                        strFullName = myString.Format("{0}_{1}{2}", strTemp_name, DateTime.Now().ToString("yyyyMMddHHmmss"), strTemp_ext);
                    }
                }
                if(fuUpload.SaveAs(strFullName)>0)
               	 strReturn = pmSys.rootURL+ strFullName.substring(strFullName.indexOf("\\_App_Data")).replace("\\", "/");
            }
        }
  	  return strReturn;
    }

    protected void btnUploadInto_ftp()
    {
        HashMap htTemp = new HashMap();

        //检查附件
        String strfilename = myFld.get_item_value("ATTACH");
        String strAttach_patch = "";//路径
        String strAttach_name = "";//上传文件名
        if (strfilename != "")
        {          
                //上载链接文件
                pmRet mRet = get_attach_file(strfilename);
                int up =(int) mRet.nRet;
                strAttach_name = (String) mRet.oRet;
                strAttach_patch = "/attach/" + strAttach_name;
                if (up == -1)
                {
                    Functions.MsgBox("上传附件失败！");
                    return;
                }
                Functions.ht_SaveEx("ATTACH_FILE_PATCH", strAttach_patch, htTemp);
          

            Functions.ht_SaveEx("ATTACH_FILE_NAME", strAttach_name, htTemp);
            Functions.ht_SaveEx("CASEID", pCaseId, htTemp);
            Functions.ht_SaveEx("CASETYPE", pType, htTemp);
            Functions.ht_SaveEx("GHID", pmAgent.uid, htTemp);
            Functions.ht_SaveEx("SDATE", DateTime.NowString("yyyy-MM-dd HH:mm:ss"), htTemp);

            my_odbc pCase = new my_odbc(pmSys.conn_crm);
            int rc = pCase.my_odbc_addnew(pTableName, htTemp);
            pCase.my_odbc_disconnect();
            if (rc > 0)
                Functions.MsgBox("提示", "上传附件成功！");
            else
                Functions.MsgBox("上传附件失败！");
            mydg.refresh("AUTOID", "CASEID = '" + pCaseId + "'");
        }
        else
        {
            Functions.MsgBox("请选择要上传的附件！");
        }
    }

    
    /// <summary>
    /// 检查是否有附件，如果有将文件上传到FTP server
    /// </summary>
    /// <param name="strOutput">附件文件名，用 \n 分隔</param>
    /// <returns>-1：无附件  1：有附件</returns>
    private pmRet<Integer, String>  get_attach_file(String strFile)
    {    	
        int nReturn = -1;
        String strAttach_name="";
        String strLocalFile, strRemoteFile;
    	pmRet<Integer,String> mRet = new pmRet<Integer, String>(nReturn,strAttach_name);
    	
          //上传文件
        strLocalFile = myFile.getDirectoryName(strFile);
        strAttach_name =  myFile.getFileName(strFile);
        
        String pmSys_web_phypath = myFile.MapPath("/");
        String strLocal_path = myString.Format("{0}_App_Data\\crmdata\\attach\\", pmSys_web_phypath);       
        String togo = strLocal_path+strAttach_name;
        

        if (strAttach_name != "")
        {
            if (myFile.Exists(togo) == true)
            {
            	 my_FTPClient myFtp = new my_FTPClient(pmSys.ftp_crm_host, pmSys.ftp_crm_uid, pmSys.ftp_crm_pwd);
                 strRemoteFile = myString.Format(".\\attach\\{0}", strAttach_name); // ftp服务器上的路径         
                 if (!myFtp.Upload(togo, strRemoteFile))
                     {
                         mRet.nRet = -1;
                         return mRet;
                     }
                 else
                	 mRet.nRet  = 1;
                 myFtp.Close();
             }
        }        
        mRet.oRet = strAttach_name;
        return mRet;
    }
}



