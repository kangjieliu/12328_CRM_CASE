package com.CallThink.ut_case;

import java.util.HashMap;

import org.springframework.ui.Model;

import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.pmSys;
import com.CallThink.base.pmCode.UltraCRM_Page;
import com.CallThink.base.support.pmInfo;
import com.CallThink.ut_case.pmModel_case.case_info;
import com.CallThink.ut_case.pmModel_case.case_set_info;
import com.CallThink.ut_customer.pmModel_cust.fun_Cust;
import com.CallThink.ut_form.pmModel_form.fun_Form;
import com.ToneThink.DataTable.DataTable;
import com.ToneThink.ctsTools.Regex.Match;
import com.ToneThink.ctsTools.Regex.Regex;
import com.ToneThink.ctsTools.Regex.Regex.RegexOptions;
import com.ToneThink.ctsTools.WebUI.my_ToolStrip;
import com.ToneThink.ctsTools.WebUI.my_dataGrid;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.myString;
import com.ToneThink.ctsTools.myUtility.pmMap;
import com.ToneThink.ctsTools.myUtility.pmRet;

public class popup_case_list  extends UltraCRM_Page
{
	private String m_TableName = "CRM_CASE";
    private String pOp = "";
    private int pType = 0;
    private String pCaller = "";
    private String pCallid = "";

    //private string pUserId = "";
    //private int pStatus = 0;
    private String pIvrInfo = ""; //ivrinfo，由ivr脚本程序传过来的值

    case_set_info myCase;// = new cases_info(pType);
    case_info myCaseInfo = new case_info();

    my_dataGrid mydg = new my_dataGrid(51);
    my_ToolStrip myToolBar = new my_ToolStrip();
        	 
    public void Page_Load(Object sender, Model model) {
    	pmAgent = fun_main.GetParm();
    	Functions.setSession("nCountFile", 1);
    	int rc=0;
    	if (IsPostBack == false)//正被首次加载和访问
        {
            //?ntype=x&cmd=popup_callin&callid=" + strCallid + "&caller=" + strCaller + "&info=" + strInfo";
            //由不同(Form)调用，显示指定工单资料,格式：?cmd=xxx&&callid=xxx&caller=xxx&info=xxx";
            //cmd 的含义： 
            // "popup_callin"  查找在线用户所属工单，来电，callid=呼叫标识,caller=来电号码 info=IVR信息

    		pmMap res = fun_main.QuerySplit(Request);
            rc=res.nRet;
            if(rc>0)
            {
                HashMap htQuery = res.htRet;      
                pOp = Functions.ht_Get_strValue("cmd", htQuery);
                pType = Functions.atoi(Functions.ht_Get_strValue("ntype", htQuery));
                pCallid = Functions.ht_Get_strValue("callid", htQuery);
                //pUserId = Functions.ht_Get_strValue("userid", htQuery); //add by gaoww 20101014 解决新建工单，不能带过userid的问题
                pCaller = Functions.ht_Get_strValue("caller", htQuery);
                //strInfo = Functions.ht_Get_strValue("info", htQuery);
                pIvrInfo = Functions.ht_Get_strValue("info", htQuery);
            }
            Save_vs("pOp", pOp);
            Save_vs("pType", pType);
            Save_vs("pCallid", pCallid);
            //Save_vs("pUserid", pUserId);
            Save_vs("pCaller", pCaller);
            Save_vs("pIvrInfo", pIvrInfo);
        }
        else
        {
            pOp = Load_vs("pOp");
            pType = Functions.atoi(Load_vs("pType"));
            pCallid = Load_vs("pCallid");
            pCaller = Load_vs("pCaller");
            //pUserId = Load_vs("pUserid");
            pIvrInfo = Load_vs("pIvrInfo");
        }
        myCase = new case_set_info(pType);
        m_TableName = myCase.TableName;
        //strUname = myCase.CaseName;

        InitToolbar();
    	myToolBar.render(model);    	

        if (myCase.nUserId_lnk == 0)  //不与用户资料关联的工单，直接弹工单编辑页面
        {            
        	Functions.Redirect("popup_case_edit.aspx?" + Request.getQueryString());
        }
        String strUserid = "";
        String strUname = "";
        if (pOp.equals("popup_callin"))//查找在线用户资料，来电
        {
        	Functions.setCookie("redirect_url",null,10);  
            Match mtRet =Regex.Match(pIvrInfo, "USERID:\\d+[|]?",RegexOptions.IgnoreCase);
            if (mtRet.Success == true) //查找在线用户资料，Pin码
            {
                strUserid = Functions.Substring(mtRet.Value, "USERID:", "");//.TrimEnd('|'); 
            }
            else
            {
                DataTable dtRet;
                pmRet mRet1 = fun_Cust.get_user_by_caller(pCaller);
                rc = (int) mRet1.nRet ;
                dtRet = (DataTable) mRet1.oRet;
                
                if (rc < 1) //用户资料中没有符合条件的记录
                {                	
                    Functions.Redirect("popup_case_edit.aspx?" + Request.getQueryString());                	
                	//String strTemp=pmSys.rootURL+"/ut_case/popup_case_edit.aspx?" + Request.getQueryString();
                	//Functions.setCookie("redirect_url",strTemp,10);  
                	return;
                }
                strUserid = Functions.dtCols_strValue(dtRet, "USERID");
                strUname = Functions.dtCols_strValue(dtRet, "UNAME");
            }

           
            String Title = "客户在线服务 - 用户<" + strUname + ">,主叫号码<" + pCaller + ">"; // pMsg=userid
            my_odbc pTable = new my_odbc(pmSys.conn_crm);
            rc = pTable.my_odbc_find(m_TableName, "USERID='" + strUserid + "'");
            pTable.my_odbc_disconnect();
            if (rc < 1)  //有用户纪录，无工单纪录
            {
                Functions.Redirect(pmSys.rootURL+"/ut_case/popup_case_edit.aspx?" + Request.getQueryString()); //add by gaoww 20110602 检查如果只有客户资料但是没工单记录，也直接弹工单编辑页面
            	//String strTemp=pmSys.rootURL+"/ut_case/popup_case_edit.aspx?" + Request.getQueryString();
            	//Functions.setCookie("redirect_url",strTemp,10);  
            	return;
            }
        }    
        
        String strFilter ="";
      	strFilter ="USERID='" + strUserid + "' AND CASETYPE='"+pType+"' ORDER BY SDATE DESC,STIME DESC";
      	Fillin_grid(strFilter);
    	mydg.render(model);
       
    }	
  
    private void InitToolbar()
    { 
        myToolBar.fill_fld("新建工单", "AddNew",0,10);
        //该项只用作新建工单用
        myToolBar.fill_fld("选择工单类型", "Select_CaseType", 25, 4);
        myToolBar.set_list("Select_CaseType", "SELECT * FROM CRM_CASE_TABLE ORDER BY CASETYPE", "CASETYPE,CASE_NAME", pmSys.conn_crm);
     
        //if (myToolBar.get_item_cbo("Select_CaseType").Items.Count > pType)
        //   myToolBar.get_item_cbo("Select_CaseType").SelectedIndex = pType;
        myToolBar.set_item_value("Select_CaseType",String.valueOf(pType));
       
        
        myToolBar.fill_toolStrip("plCommand");
        myToolBar.btnItemClick = this;// new btnClickEventHandler(myToolBar_btnItemClick);
    }

    public void myToolBar_btnItemClick(Object sender, String name, String parms, int nparms)
    {    	
        if (name.equals("AddNew"))
        {
            //if (dgvCase.Rows.Count <= 0) return;
            int rows = 0;// dgvCase.SelectedIndex;//.CurrentCell.RowIndex;
            String strUserid = mydg.get_cell(rows, "USERID");

            //String strType = Functions.Substring(myToolBar.get_item_cbo("Select_CaseType").Text, null, "(");
            String strType = myToolBar.get_item_value("Select_CaseType");
            Functions.Redirect(myString.Format("popup_case_edit.aspx?cmd=AddNew&caseid={0}&ntype={1}&caller={2}&info={3}&callid={4}", strUserid, strType, pCaller, pIvrInfo, pCallid)); //modify by gaoww 20110602 解决如果是新来电，新建工单无法生成caseid问题
        }
        else if (name.equals("Select_CaseType"))
        {
        	String strTemp = Functions.Substring(parms, null, "(");
            nparms = Functions.atoi(strTemp);
            
            //add by gaoww 20150909 增加状态切换功能
            String strQueryString = Request.getQueryString();
            if (strQueryString.contains("ntype=" + pType))
               strQueryString= strQueryString.replace("ntype=" + pType, "ntype=" + nparms);
            Functions.Redirect("popup_case_list.aspx?" + strQueryString);     
        }
        else if (name.equals("Cust_edit"))
        {
            //if (dgvCase.Rows.Count <= 0) return;
            int rows = 0;// dgvCase.SelectedIndex;//.CurrentCell.RowIndex;
            String strUserid = mydg.get_cell(rows, "USERID");
            Functions.Redirect(myString.Format("~/ut_customer/popup_cust.aspx?cmd=Pin&userid={0}&caller={1}", strUserid, pCaller));
        }
        else if (name.equals("Call_list"))
        {
            //if (dgvCase.Rows.Count <= 0) return;
            int rows = 0;// dgvCase.SelectedIndex;//.CurrentCell.RowIndex;
            String strUserid = mydg.get_cell(rows, "USERID");
            Functions.Redirect(myString.Format("~/ut_customer/customer_call_list.aspx?userid={0}&caller={1}", strUserid, pCaller));//modify by gaoww 20121015 解决点详细话单时，提示页面不存在
        }
        else if (name.equals("Query"))
        {
            //pmSys.frmstrParams = "";
            //frmSearch dlg = new frmSearch(1, pType);
            //dlg.ShowDialog();
            //if (pmSys.frmstrParams != "")
            //{
            //    mydg.refresh("SELECT * FROM " + pTableName + " WHERE " + pmSys.frmstrParams);
            //}
        }
        /*
        else if (name == "Close")
        {
            Close();
        }
         */
    }

    // 处理工单
    //填充工单列表框架
    private void Fillin_grid(String strFilter)
    {  
    	String strKey_kvdb = "dtCase_popup_list_" + pType;
        DataTable dtStru = pmInfo.myKvdb.Get(strKey_kvdb);// as DataTable;
        if (dtStru == null)
        {
            dtStru = fun_Form.get_desc_data(myCase.DescName, "((FLD_VLEVELS&2)<>0) ORDER BY LIST_INDEX");
            pmInfo.myKvdb.Setex(strKey_kvdb, dtStru, 60); //60秒
        }
        if (dtStru.getCount() > 0)
        {
            mydg.ID(String.valueOf(pType));
            mydg.SetTable(m_TableName);
            mydg.SetSelectStr("SELECT * FROM " + m_TableName + " WHERE 1>1");
            mydg.SetConnStr(pmSys.conn_crm);

            mydg.SetPageSize(27);
            //myFld.SetCaption = "工单资料详细信息";
            mydg.SetCaption("工单资料列表");

            pmRet mRet = fun_Form.Fill_Grid(dtStru, mydg, 2, 0);  //显示级别：bit 0在详细资料中显示 bit1在列表中显示，bit2在弹出中显示
            int rc = (int) mRet.nRet;
            mydg = (my_dataGrid) mRet.oRet;
            /*if (rc > 0)
            {
                mydg.CellLinkClicked += new CellLinkClickedEventHandler(dgvCase_CellLinkClicked);
                mydg.RowDataFilled += new RowDataFilledEventHandler(mydg_RowDataFilled);                    
            }*/

            mydg.RowDataFilled = this;
            mRet = myCase.set_list_color(mydg);
            mydg = (my_dataGrid) mRet.oRet;
            mydg.fill_header("dgvList", "CASEID", strFilter);
        }
    }

    //未用
    public void mydg_RowDataFilled(Object sender, int rows)
    {
        if (rows < 0) return; //表头行，不处理
        
        //根据表单设计list_type=4，动态设置链接字段
        String strKey_kvdb = "dtCase_list_" + pType;
        DataTable dtStru = pmInfo.myKvdb.Get(strKey_kvdb);// as DataTable;
        if (dtStru == null)
        {
            dtStru = fun_Form.get_desc_data(myCase.DescName, "((FLD_VLEVELS&2)<>0) ORDER BY LIST_INDEX");
            pmInfo.myKvdb.Setex(strKey_kvdb, dtStru, 60); //60秒
        }        
        if(dtStru.getCount()>0)
        {        	
        	int nType = Functions.atoi(mydg.get_cell(rows, "CASETYPE"));
            int nStatus = Functions.atoi(mydg.get_cell(rows, "STATUS"));
            String strCaseid = mydg.get_cell(rows, "CASEID");
            String strNewUrl = myString.Format("popup_case_edit.aspx?cmd=Edit&caseid={0}&ntype={1}&status={2}", strCaseid, nType, nStatus);            
            DataTable dtLink = Functions.dt_GetTable_select(dtStru, "LIST_TYPE=4");
            if(dtLink.getCount()>0)
            {
            	 for(int index=0;index<dtLink.getCount();index++)
            	 {
            	        String fld_value = Functions.dtCols_strValue(dtLink, index, "FLD_VALUE");
            	        String strValue = mydg.get_cell(rows, fld_value);
            	        if (myString.IsEmpty(strValue)) continue;
            	        int cols = mydg.get_idx(fld_value);
            	        if(cols>=0)
            	        {            	        	 
            	        	String strHtml = myString.Format("<a href='#this' onclick=\"open_view('{0}','{1}','{2}');\">{2}</a>", strNewUrl, mydg.get_cell(rows, "CASENAME"), strValue);
            	            mydg.set_cell(rows, cols, strHtml);
            	        }
            	 }             
             }
             else 
             {            	 
                 if (myString.IsEmpty(strCaseid)==false)
                 {
	                 int cols = mydg.get_idx("CASEID");
	                 if (cols >= 0)
	                 {    
	                	 String strHtml = myString.Format("<a href='#this' onclick=\"open_view('{0}','{1}','{2}');\">{2}</a>", strNewUrl, mydg.get_cell(rows, "CASENAME"), strCaseid);
	                     mydg.set_cell(rows, cols, strHtml);
	                 }
                 }
             }
        }        
        
       //add by gaoww 20160226 增加按权限号码隐藏功能
        if (pmAgent.c_Levels.check_authority("phone_number_hidden") == true)
        {
            //DataTable dtData = (DataTable)mydg.DataSource;
            String fld_value;
            int nCol = 0;
            
            nCol = mydg.get_idx("TEL");
            if(nCol>=0)
            {
	            fld_value = mydg.get_cell(rows, "TEL");
	            fld_value = fun_main.phone_number_hidden(fld_value);
	            mydg.set_cell(rows, nCol, fld_value);
            }
            
            nCol = mydg.get_idx("MOBILENO");
            if(nCol>=0)
            {                
                fld_value = mydg.get_cell(rows, "MOBILENO");
                fld_value = fun_main.phone_number_hidden(fld_value);
                mydg.set_cell(rows, nCol, fld_value);
            }
            nCol = mydg.get_idx("CALLER");
            if(nCol>=0)
            {                
                fld_value = mydg.get_cell(rows, "CALLER");
                fld_value = fun_main.phone_number_hidden(fld_value);
                mydg.set_cell(rows, nCol, fld_value);
            }
        }  
    }

    /*private void dgvCase_CellLinkClicked(object sender, string text, int rows, int cols)
    {
        if (dgvCase.Rows.Count <= 0) return;
        string strCaseid = mydg.get_cell(rows, "CASEID");
        if (strCaseid == "") return;
        int nType = Functions.atoi(mydg.get_cell(rows, "CASETYPE"));
        int nStatus = Functions.atoi(mydg.get_cell(rows, "STATUS"));
        //2008.08
        this.Response.Redirect(String.Format("popup_case_edit.aspx?cmd=Edit&caseid={0}&ntype={1}&status={2}", strCaseid, nType, nStatus));
        //string strNewUrl = String.Format("case_edit.aspx?cmd=edit&caseid={0}&ntype={1}&status={2}", strCaseid, nType.ToString(), nStatus.ToString());
        //Functions.js_open(strNewUrl);     
    }*/
}



