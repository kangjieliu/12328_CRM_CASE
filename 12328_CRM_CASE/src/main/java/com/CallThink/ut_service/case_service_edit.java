///########################################################################################
/// Copyright (C) 2000, ToneThink.Soft  All Rights Reserved. 
///#########################################################################################
/// 文件创建时间：2015-09-07
///   文件创建人：gaoww
/// 文件功能描述：服务工单编辑页面
///     调用格式：
///     
///     维护记录：
/// 2015.09.07：create by gaoww         
///#########################################################################################
package com.CallThink.ut_service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import com.CallThink.base.pmClass.e_Level_base;
import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.myColor;
import com.CallThink.base.pmClass.pmSys;
import com.CallThink.base.pmClass.pmAgent_info;
import com.CallThink.base.pmCode.UltraCRM_Page;
import com.CallThink.base.support.pmInfo;
import com.CallThink.ut_case.pmModel_case.WFCase_info;
import com.CallThink.ut_case.pmModel_case.case_info;
import com.CallThink.ut_case.pmModel_case.case_set_info;
import com.CallThink.ut_customer.pmModel_cust.customer_set_info;
import com.CallThink.ut_customer.pmModel_cust.fun_Cust;
import com.CallThink.ut_form.pmModel_form.fun_Form;
import com.CallThink.ut_service.pmModel_service.e_Level_service;
import com.CallThink.ut_service.pmModel_service.fun_service;
import com.CallThink.ut_service.pmModel_service.service_info;
import com.CallThink.ut_service.pmModel_service.service_set_info;
import com.ToneThink.DataTable.DataTable;
import com.ToneThink.DataTable.DataRow;
import com.ToneThink.DateTime.DateTime;
import com.ToneThink.FileUpload.FileUpload;
import com.ToneThink.ctsControls.tag.TabControl;
import com.ToneThink.ctsControls.tag.TabPage;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.pmList;
import com.ToneThink.ctsTools.myUtility.pmMap;
import com.ToneThink.ctsTools.myUtility.pmRef;
import com.ToneThink.ctsTools.myUtility.pmRet;
import com.ToneThink.ctsTools.Regex.Regex;
import com.ToneThink.ctsTools.Regex.Regex.RegexOptions;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.myFile;
import com.ToneThink.ctsTools.myUtility.myString;
import com.ToneThink.ctsTools.WebUI.my_Field;
import com.ToneThink.ctsTools.WebUI.my_ToolStrip;

    public class case_service_edit extends UltraCRM_Page
    {
        private String pOp = "";
        private int pType = 0;
        private String pCaseId = "";
        private String pCaseId_Rel = ""; //关联工单的工单编号 add by gaoww 20151202
        private int pStatus = 0;
        //private string pId_dlg = "";
      
        private String m_TableName = "CRM_CASE";
        private HashMap m_htPriv = new HashMap();

        private String pFrom = "";//add by gaoww 20141103 增加页面打开来源，如果为relation时，则不添加子菜单，避免页面嵌套多层问题

        service_set_info myCase;// = new cases_info(pType);
        service_info myCaseInfo = new service_info();

        my_Field myFld = new my_Field(2);// using for firm  //备注:工单显示3列会使排版很拥挤，不好看，所以还用2列，gaoww
        my_ToolStrip myToolBar = new my_ToolStrip();

        DataTable dtTools; 

        public void Page_Load(Object sender, Model model)
        {
            String strTitle = "工单资料管理";
            if (IsPostBack == false)//正被首次加载和访问
            {
                //由不同(Form)调用，显示指定工单资料,格式：?cmd=xxx&&ntype=x&status=x&info=caseid";
                //cmd 的含义： 
                // "AddNew"        新建工单资料，pType工单类型   
                // "AddNew_fromCust"，为指定用户新建工单 caseid=用户ID,ntype=工单类型
                // "Edit"          修改工单资料，caseid=工单ID ntype=工单类型  status=工单状态   

                //no used
                // "Relation"      作为子卡片页显示，caseid=工单ID  ntype=工单类型 【工单所属联系人】

                HashMap htQuery;
                pmMap res = fun_main.QuerySplit(Request); htQuery = res.htRet;
                int rc = res.nRet;
                if (rc > 0)
                {
                    pOp = Functions.ht_Get_strValue("cmd", htQuery);
                    pType = Functions.atoi(Functions.ht_Get_strValue("ntype", htQuery));
                    pStatus = Functions.atoi(Functions.ht_Get_strValue("status", htQuery));
                    pCaseId = Functions.ht_Get_strValue("caseid", htQuery);
                    pCaseId_Rel = Functions.ht_Get_strValue("caseid_rel", htQuery);
                    pFrom = Functions.ht_Get_strValue("from", htQuery);
                    //pId_dlg = Functions.ht_Get_strValue("nIdlg", htQuery);
                }
                Save_vs("pOp", pOp);
                Save_vs("pType", pType);
                Save_vs("pStatus", pStatus);
                Save_vs("pCaseId", pCaseId);
                Save_vs("pCaseId_Rel", pCaseId_Rel);
                Save_vs("pFrom", pFrom);
                //Save_vs("pId_dlg", pId_dlg);
            }
            else
            {
                pOp = Load_vs("pOp");
                pType = Functions.atoi(Load_vs("pType"));
                pStatus = Functions.atoi(Load_vs("pStatus"));
                pCaseId = Load_vs("pCaseId");
                pCaseId_Rel = Load_vs("pCaseId_Rel");
                pFrom = Load_vs("pFrom");
                dtTools = Load_vs("dtTools",DataTable.class); //add by gaoww 20151109 增加自定义button功能
                
            }
            //if (pmAgent.content_width > 850) myFld = new my_Field(3);
            myCase = new service_set_info(pType);
            myFld = new my_Field(myCase.nForm_cols);
            m_TableName = myCase.TableName;
            myCaseInfo = myCase.GetCaseRecord(pCaseId);
            strTitle = myString.Format("{0}-资料管理", myCase.CaseName);

            Fillin_field();
            if (IsPostBack == false)//首次加载和访问
            {
                if (pOp.indexOf("AddNew_fromCust") >= 0)
                {
                    Fill_Case_withUserid(pCaseId);
                }
                else if (pOp.indexOf("AddNew") >= 0)
                {
                    Fill_Case_Default();
                 }
                else if (pOp.indexOf("Edit") >= 0)
                {
                    Fill_Case_withCaseid(pCaseId);
                }
                if (pOp.startsWith("AddNew") == true)
                {
                    pCaseId = myFld.get_item_text("CASEID");
                    Save_vs("pCaseId", pCaseId);
                }

                //add by gaoww 20151109 增加自定义button功能
                String strSql = myString.Format("SELECT ACTION_KEY,FLD_ID,FLD_NAME,NOTE,ACTION_CASE FROM CRM_CASE_ACTION WHERE CASETYPE='{0}' ORDER BY FLD_ORDER", pType);
                my_odbc pTable = new my_odbc(pmSys.conn_crm);
                pmList res =pTable.my_odbc_find(strSql,0); 
                dtTools = res.dtRet;
                int rc = res.nRet;
                pTable.my_odbc_disconnect();
                Save_vs("dtTools", dtTools);
            }
            InitToolbar();
            myToolBar.render(model);
            myFld.render(model);
            
            if (pFrom.toLowerCase().equals("relation")==false)
          	  Fill_subForm(1,model);
            else 
          	  Fill_subForm(0,model);      
        }
        
        //#region 处理工单
        //填充工单列表、详细信息框架
        private void Fillin_field()
        {
            String strKey_kvdb = "dtCase_edit_" + pType;
            DataTable dtStru = pmInfo.myKvdb.Get(strKey_kvdb);// as DataTable;
            if (dtStru == null)
            {
                dtStru = fun_Form.get_desc_data(myCase.DescName, "((FLD_VLEVELS&1)<>0) ORDER BY EDIT_INDEX");
                pmInfo.myKvdb.Setex(strKey_kvdb, dtStru, 60); //60秒
            }
            if (dtStru.getCount() > 0)
            {
                //dtStru.PrimaryKey = new DataColumn[] { dtStru.Columns["FLD_VALUE"] };  //add by gaoww 20100203 增加主键，如果不增加，在后面判断是否不显示字段在dtStru时，会异常
            	List<String> alRet;         
            	 m_htPriv=myCase.get_edit_priv(myCaseInfo, m_htPriv);
                if (m_htPriv.containsKey("FLD_INV") == true)
                {
                	//alRet = Arrays.asList(Functions.ht_Get_strValue("FLD_INV", m_htPriv));
                	alRet=(List<String>)m_htPriv.get("FLD_INV");
                    String strFilter = "";
                    //去除 dtStru 中alRet（FLD_VALUE）包含的内容FLD_VALUE 
                    for (String strFld : alRet)
                    {
                    	if (dtStru.Rows().contains(strFld) == false) continue;
                        /*if (strFilter.length() < 1) //delete by gaoww 20160510 
                            strFilter = String.Format("(FLD_VALUE<>'{0}')", strFld);
                        else
                            strFilter += String.Format(" AND (FLD_VALUE<>'{0}')", strFld);*/
                        if (strFilter.length() > 0) strFilter += ",";//modify by gaoww 20160510 由于datatable.select中不能有<>符号，如果有会报错，导致登出crm，所以改为not in语句
                        strFilter += "'" + strFld + "'";
                    }
                    if (strFilter.length() > 0)
                    {
                        strFilter = myString.Format("FLD_VALUE NOT IN ({0})", strFilter);//add by gaoww 20160510 改为not in语句
                        dtStru = Functions.dt_GetTable_select(dtStru, strFilter);
                    }
                }

                //myFld.SetCaption = "工单资料详细信息";
                myFld.SetConnStr(pmSys.conn_crm);
                myFld.SetTable(m_TableName);
                myFld.SetLabelAlign("Right");
                myFld.SetMaxLabelLenth(120);
                myFld.SetMaxLabelLenth_col2(100);
                myFld.funName_OnClientClick("myFld_FieldLinkClicked");

                pmRet mRet  = fun_Form.Fill_Field(dtStru, myFld, 1, 0);  //显示级别：bit 0在详细资料中显示 bit1在列表中显示，bit2在弹出中显示
                int rc = (int) mRet.nRet;
                myFld=(my_Field) mRet.oRet;
                if (rc > 0)
                    myFld.FieldLinkClicked = this;// new FieldLinkClickedEventHandler(myFld_FieldLinkClicked);
              
                myFld.fill_Panel("gbCase");
             

                if (myFld.isExist("CASEID") == true) myFld.set_readonly("CASEID");
                if (myFld.isExist("USERID") == true) myFld.set_readonly("USERID");
                if (myFld.isExist("SDATE") == true) myFld.set_readonly("SDATE");
                if (myFld.isExist("STIME") == true) myFld.set_readonly("STIME");
                if (myFld.isExist("BDATE") == true) myFld.set_readonly("BDATE");
                if (myFld.isExist("CURRENTGHID") == true) myFld.set_readonly("CURRENTGHID");

                if (myFld.isExist("CALLER") == true)
                {
                    myFld.set_item_style("CALLER", "background-color:"+myColor.bg_popup_caller); 
                }

                if (m_htPriv.containsKey("FLD_RDONLY") == true)
                {
                	// alRet = Arrays.asList(Functions.ht_Get_strValue("FLD_RDONLY", m_htPriv));  
                	alRet=(List<String>)m_htPriv.get("FLD_RDONLY");
                     for(String strFld : alRet) {
                         if (myFld.isExist(strFld) == true) myFld.set_readonly(strFld);
                     }
                }
            }
        }

        public void Fill_Case_withCaseid(String strCaseId)
        {
            //int rc = myFld.Load(m_TableName, "CASEID = '" + strCaseId + "'", pmSys.conn_crm);
            //if (rc < 0)
            if (myCaseInfo.isExist == false)
            {
                Fill_Case_Default();
                return;
            }
            myFld.Load(myCaseInfo.htCase);
            myFld.set_item_value("CURRENTGHID", pmAgent.uid);

            if (myFld.isExist("STATUS") == true)
                pStatus = Functions.atoi(myFld.get_item_value("STATUS"));

            if (myFld.isExist("CLOSE_DAYS") == true)
            {
                if (myFld.get_item_text("CLOSE_DAYS") == "")
                {
                    DateTime dt_sdate, dt_edate;
                    dt_sdate = Functions.ConvertStrToDateTime(myFld.get_item_text("SDATE"), myFld.get_item_text("STIME"));
                    dt_edate = Functions.ConvertStrToDateTime(myFld.get_item_text("CLOSE_DATE"));
                    //TimeSpan ts_days = dt_edate - dt_sdate;
                    //myFld.set_item_text("CLOSE_DAYS", ts_days.Days + "-" + ts_days.Hours);
                }
            }
        }

        //新工单,使用已有的用户资料填充默认值
        private void Fill_Case_withUserid(String strUserId)
        {
            DataTable dtCust;
            pmRet mRet= fun_Cust.get_user_by_userid(strUserId);
            int rc = (int) mRet.nRet;
            dtCust=(DataTable) mRet.oRet;
            if (rc < 0)
            {
                Fill_Case_Default();
                return;
            }
            DataTable dtRet = Functions.convert_dt_to_dt(dtCust, m_TableName, pmSys.conn_crm, null);
            if (dtRet.getCount() > 0) myFld.Load(dtRet);

            myFld.set_item_value("CASETYPE", Functions.atos(pType));
            myFld.set_item_value("USERID", strUserId);
            myFld.set_item_text("SDATE", DateTime.Now().ToString("yyyyMMdd"));
            myFld.set_item_text("STIME", DateTime.Now().ToString("HHmmss"));
            myFld.set_item_text("EDATE", DateTime.Now().ToString("yyyyMMdd"));
            myFld.set_item_text("CASEID", myCase.GetNewCaseid(""));// fun_CRM.gfnGetCaseid("", pmSys.casetype_default); //DateTime.Now().ToString("yyyyMMddHHmmss01");
            myFld.set_item_value("GHID", pmAgent.uid);
            myFld.set_item_value("CURRENTGHID", pmAgent.uid);
        }

        //新工单,显示默认值
        private void Fill_Case_Default()
        {
            myFld.Load(m_TableName, "1<>1", pmSys.conn_crm);

            myFld.set_item_value("CASETYPE", Functions.atos(pType));

            myFld.set_item_text("USERID", fun_Cust.GetNewUserid("", pmSys.utype_default));
            myFld.set_item_text("SDATE", DateTime.Now().ToString("yyyyMMdd"));
            myFld.set_item_text("STIME", DateTime.Now().ToString("HHmmss"));
            myFld.set_item_text("CLOSE_DATE", DateTime.Now().ToString("yyyyMMdd"));
            if (myFld.isExist("CASEID") == true)
            {
                if (pCaseId.equals(""))
                {
                    pCaseId = myCase.GetNewCaseid(""); //DateTime.Now().ToString("yyyyMMddHHmmss01");
                }
                myFld.set_item_text("CASEID", pCaseId);
                //myFld.get_item_txt("CASEID").BackColor = System.Drawing.Color.LightGreen;
                myFld.set_item_style("CALLER", "background-color:LightGreen");                 
            }
            myFld.set_item_value("GHID", pmAgent.uid);
            myFld.set_item_value("CURRENTGHID", pmAgent.uid);
            if (myFld.isExist("ORG_CODE") == true)
                myFld.set_item_text("ORG_CODE", pmAgent.c_Info.agent_org_code); //add by gaoww 20151029 增加建单人所属机构保存
      

            if ((myFld.isExist("PROV") == true) || (myFld.isExist("CITY") == true))
            {
                String strTemp = fun_Cust.get_prov_by_caller(myFld.get_item_text("TEL"));
                //modify by gaoww 20151029 增加判断，如果返回结果不为空，则重新赋值
                String fld_value = Functions.Substring(strTemp, "PROV_NAME",1);
                if (fld_value.equals("")==false)
                    myFld.set_item_value("PROV", fld_value);
                fld_value = Functions.Substring(strTemp, "CITY_NAME",1);
                if (fld_value.equals("")==false)
                    myFld.set_item_value("CITY", fld_value);                
            }
        }

        //ntype == 1 ComboBox选择下拉后  
        public void myFld_FieldLinkClicked(Object sender, String name, String parms, int nparms, int ntype)
        {
            if (myFld.isExist("CURRENTGHID") == true)
                myFld.set_item_text("CURRENTGHID", pmAgent.uid);
            if (ntype == 0)
            {
                
            }
            else if (ntype == 1) //1  ComboBox选择下拉后  
            {
            }
            else if ((ntype == 11) || (ntype == 12)) //11  打开数据字典
            {
                if (parms == null) return;
                if (name.equals("CITY"))
                {
                    // if (myFld.get_item_text("PROV") != "")
                    //     strParm3 = "PROV_NAME='" + myFld.get_item_text("PROV") + "'";
                }
            
            }
            else if ((ntype == 16)) //上传图片   //add by gaoww 20151207 由ctstools控件实现image
            {
            	//临时封上
                /*FileUpload fuTemp =  myFld.get_item_fileUpload(name);
                if (fuTemp.HasFile() == false) return;

                String strUrl = fun_main.Upload_imgFile(fuTemp, null, true);
                myFld.set_item_value(name, strUrl);*/
            }
        }

        //
        private void InitToolbar()
        {
            myToolBar.Clear(); //modify by gaoww 20140523
            //myToorBar.IsFixed = true;
            int nAccept_must = 0; //必须先签收，其它功能才可用 
            int nLevel = myCase.get_authority(myCaseInfo.nProcess, myCaseInfo.nStatus);
            //add by gaoww 20111008 如果返回的地址为空，则不显示“返回”按钮，避免报错
            //modify by gaoww 20120606 如果是从右下角弹出框点开的则不显示返回，否则会回到主页
            //modify by gaoww 20121130 增加pId_dlg的判断，实现弹出窗口不显示返回按钮
            if (((LastPageUrl().isEmpty()==false) && (LastPageUrl().contains("web_desk/desktop_im.aspx") == false)&&(pFrom.equals("relation")==false)))// && pId_dlg == "") //modify by gaoww 20161116 增加判断，子菜单时，不显示返回按钮，因为使用的是弹出框方式
                myToolBar.fill_fld(fun_main.Term("LBL_Return"), "Return",0,10);

            if ((myCase.nWF_Enable == 1) && ((nLevel & 32) > 0))
            {
                //约定第一环节 为0
                if ((myCaseInfo.nProcess > 0) && (myCaseInfo.nProcess_status == 0))
                {
                    if ((myCaseInfo.Get("CURRENTGHID").equals(pmAgent.uid)) || (myCaseInfo.Get("CURRENTGHID").equals("")))
                    {
                        myToolBar.fill_fld("签收", "wf_accept");
                        myToolBar.fill_fld("拒签", "wf_refuse");
                        nAccept_must = 1;
                    }
                }
            }
            //工单是否存在  false-不存在  true-存在
            boolean nExist = myCaseInfo.isExist;
            if (IsPostBack == true) nExist = myCase.isExist_Case(pCaseId);

            if (nExist == false)  //新建工单
            {
                myToolBar.fill_fld("新建并保存", "Save", 0, 10);
            }
            else
            {
                //myToorBar.fill_fld(fun_main.Term("LBL_ADDNEW"), "AddNew");
                myToolBar.fill_fld("复制", "Copy");
                myToolBar.fill_fld(fun_main.Term("LBL_SAVE"), "Save", 0, 10);
                myToolBar.fill_fld_confirm(fun_main.Term("LBL_DELETE"), "Delete", " 确实要删除工单资料吗？");
                myToolBar.fill_fld("Separator", "Separator0", 0, 3);
                if (myCase.is_sm_support(myCaseInfo.nProcess) == 1)
                {
                    if (pmAgent.c_Levels.check_authority(e_Level_service.service_dds) == true)
                    {
                        myToolBar.fill_fld("任务分派", "Assign", 0, 10);
                        myToolBar.fill_fld("Separator", "Separator1", 0, 3);
                    }
                }
                //增加自定义button显示
                for (int rows = 0; rows < dtTools.getCount(); rows++)
                {
                    String strFld_id = Functions.dtCols_strValue(dtTools, rows, "ACTION_KEY");
                    String strFld_name = Functions.dtCols_strValue(dtTools, rows, "FLD_NAME");
                    myToolBar.fill_fld(strFld_name, "CASE_CUSTOM_" + strFld_id);
                }
                if (dtTools.getCount() > 0)
                    myToolBar.fill_fld("Separator", "Separator5", 0, 3);
                myToolBar.fill_fld("选择转发类型", "Select_utype", 20, 4);
                myToolBar.set_list("Select_utype", "转发工单至座席");
                myToolBar.set_list("Select_utype", "转发工单至短信");
                myToolBar.set_list("Select_utype", "转发工单至EMAIL");
                myToolBar.set_list("Select_utype", "转发工单至微信"); //add by gaoww 20151229
                myToolBar.fill_fld(fun_main.Term("LBL_TRANCASE"), "TranCase");
                //myToorBar.fill_fld(fun_main.Term("LBL_DOWNLOAD_REC"), "Download_Rec"); //delete by gaoww 20101015 bs先暂时封上此功能
                myToolBar.fill_fld("输出工单(Word)", "Output_word");
                // myToorBar.fill_fld("输出并打印工单(Word)", "Output_word_Print");
                myToolBar.fill_fld("Separator", "Separator2", 0, 3);
                if ((nLevel & 32) > 0) //有工作流权限
                {
                    //bool bRet = myCase.get_authority(myCaseInfo, "wf");
                    //if (bRet == true)
                    if ((myCaseInfo.Get("CURRENTGHID").equals(pmAgent.uid)) || ((myCaseInfo.nProcess_status == 4) && (myCaseInfo.Get("CURRENTGHID").equals(""))) || ((myCaseInfo.nProcess == 0) && (myCaseInfo.nProcess_status == 0) && (myCaseInfo.Get("CURRENTGHID").equals(""))))
                        myToolBar.fill_fld("工单流转", "WorkFlow", "return Set_WorkFlow('&ntype=" + pType + "&casetable=" + m_TableName + "&WF=" + myCase.nWF_Enable + "')");
                }
               
                if (myCase.nWF_Enable == 1) //modify by gaoww 20130423 没有该环节工作流权限也应该可以查看进展
                    myToolBar.fill_fld("查看进展", "WorkFlow_view", "return View_WorkFlow('&ntype=" + pType + "&casetable=" + m_TableName + "&WF=" + myCase.nWF_Enable + "')");

                myToolBar.fill_fld("Separator", "Separator3", 0, 3);
                myToolBar.fill_fld("发送公文通知", "SendNote");//add by yanj 20121108 新公文修改,在客户资料编辑界面增加发送消息功能
                myToolBar.fill_fld("设定任务提醒", "SetNotify");//add by fengw 20130722 设定任务提醒

                //if ((pOp == "addnew") || (pOp == "edit"))
                //    myToorBar.fill_fld(fun_main.Term("LBL_Return"), "Return");
                //else
                //    myToorBar.fill_fld(fun_main.Term("LBL_Return"), "Return", "return fun_return(0);");
            }
            myToolBar.fill_toolStrip("plCommand");
            myToolBar.btnItemClick = this;// new btnClickEventHandler(myToolBar_btnItemClick);

            if ((nLevel & 1) > 0) myToolBar.set_readonly("AddNew", false);
            else myToolBar.set_readonly("AddNew", true);
            if ((nLevel & 2) > 0) myToolBar.set_readonly("Save", false);
            else
            {
                if (nExist == false) //add by gaoww 20140813 此时为新增工单，应按新增权限控制
                {
                    if ((nLevel & 1) > 0) myToolBar.set_readonly("Save", false);
                    else myToolBar.set_readonly("Save", true);
                }
                else
                    myToolBar.set_readonly("Save", true);
            }
            if ((nLevel & 4) > 0) myToolBar.set_readonly("Delete", false);
            else myToolBar.set_readonly("Delete", true);
            if ((nLevel & 16) > 0) myToolBar.set_readonly("Output_word", false);
            else myToolBar.set_readonly("Output_word", true);
            //add by zhaoj 20130906 增加工单时，设工单流转、发送公文通知、设定任务提醒为只读
            myToolBar.set_readonly("Select_utype", false);
            myToolBar.set_readonly("TranCase", false);
            if ((nLevel & 32) > 0) myToolBar.set_readonly("WorkFlow", false);
            else myToolBar.set_readonly("WorkFlow", false);
            myToolBar.set_readonly("SendNote", false);
            myToolBar.set_readonly("SetNotify", false);

            //modify by gaoww 20100712 根据权限控制命令显示
            List<String> alRet;           
            if (m_htPriv.containsKey("CMD_INV") == true)
            {
           	 //alRet = Arrays.asList(Functions.ht_Get_strValue("CMD_INV", m_htPriv));          
            	alRet=(List<String>)m_htPriv.get("CMD_INV");
             for(String strCmd : alRet) {     
                 //add by gaoww 20151126 增加自定义button显示控制
                 DataTable myRow_tool = dtTools.select("ACTION_KEY ='" + strCmd + "'");
                 if (myRow_tool.getCount()  > 0)
                     myToolBar.set_visible("CASE_CUSTOM_" + strCmd, false);
                 else
                     myToolBar.set_visible(strCmd, false);
             }
            }

            if (m_htPriv.containsKey("CMD_RDONLY") == true)
            {
            	//alRet = Arrays.asList(Functions.ht_Get_strValue("CMD_RDONLY", m_htPriv));         
            	alRet=(List<String>)m_htPriv.get("CMD_RDONLY");
                for(String strCmd : alRet) {       
                    //add by gaoww 20151126 增加自定义button只读控制
               	 DataTable myRow_tool = dtTools.select("ACTION_KEY ='" + strCmd + "'");
                    if (myRow_tool.getCount() > 0)
                        myToolBar.set_readonly("CASE_CUSTOM_" + strCmd, true);
                    else
                        myToolBar.set_readonly(strCmd, true);
                }
            }

            //add by gaoww 20140804 如果当前所属人工号是当前座席时，保存按钮不受权限控制，改为可用
            String strGhid_creat = Functions.ht_Get_strValue("CURRENTGHID", myCaseInfo.htCase);
            if (strGhid_creat == pmAgent.uid)
            {
                myToolBar.set_readonly("Save", false);
            }
       
            if (nAccept_must == 1)
            {
                myToolBar.set_visible("AddNew", false);
                myToolBar.set_visible("Save", false);
                myToolBar.set_visible("Delete", false);
                myToolBar.set_visible("Select_utype", false);
                myToolBar.set_visible("Output_word", false);
                myToolBar.set_visible("TranCase", false);
                //myToorBar.set_visible("SendNote", false);
                myToolBar.set_visible("WorkFlow", false);
                //myToorBar.set_visible("WorkFlow_view", false);
            }
        }

        public void myToolBar_btnItemClick(Object sender, String name, String parms, int nparms)
        {
         	 String strResult = "FAIL";
      	     String strData = "";
      	     
            int rc;
            String strCaseid = pCaseId;
            if (myFld.isExist("CASEID") == true)
            {
                strCaseid = myFld.get_item_text("CASEID");
            }
            else
            {
                if (pCaseId.length() < 1) strCaseid = myCase.GetNewCaseid("");
            }
            if (name.equals("Copy"))
            {
                myFld.setReload(true);
            	myFld.set_item_text("SDATE", DateTime.Now().ToString("yyyyMMdd"));
                myFld.set_item_text("STIME", DateTime.Now().ToString("HHmmss"));
                myFld.set_item_text("CLOSE_DATE", DateTime.Now().ToString("yyyyMMdd"));
                if (myFld.isExist("CASEID") == true)
                {
                    pCaseId = myCase.GetNewCaseid(""); //DateTime.Now().ToString("yyyyMMddHHmmss01");
                    myFld.set_item_text("CASEID", pCaseId);
                    Save_vs("pCaseId", pCaseId);  //以下两行用于刷新toolbar
                    
                    //add by gaoww 20151111 解决复制后mycaseinfo没有重读，导致button显示不对的问题
                    myCaseInfo = myCase.GetCaseRecord(pCaseId); 
                    m_htPriv=myCase.get_edit_priv(myCaseInfo, m_htPriv);
                    InitToolbar();
                }
                myFld.set_item_text("GHID", pmAgent.uid);
                myFld.set_item_text("CURRENTGHID", pmAgent.uid);
                if (myFld.isExist("CASENAME") == true)
                {
                    myFld.set_item_text("CASENAME", myFld.get_item_text("CASENAME") + " Copy");                
                    myFld.set_item_style("CASENAME", "background-color:LightGreen"); 
                }
                
                myFld.set_item_value("STATUS", "0");
                myFld.set_item_value("PROCESS", "0");
                myFld.set_item_value("PROCESS_STATUS", "0");
            }
            else if (name.equals("Save"))
            {
            	 rc=1;
              	 HashMap htTemp = myFld.Save();            	 
            	 rc= myCase.judge_case_validity(htTemp, "((FLD_VLEVELS&1)<>0) ORDER BY EDIT_INDEX");
            	 if(rc<=0)
            	 {
            		 Functions.MsgBox(myCase.FailReason);
                  	 return;
            	 }
            	 
              	 Boolean bJudge= myCase.judge_case_validity(htTemp);
            	 if(bJudge==false)
            	 {
            		 rc=-1;
            		 Functions.MsgBox(myCase.FailReason);
                  	 return;
            	 }

                String fld_value = "";              
                Update_CaseRecord(strCaseid, pType);
                myCaseInfo = myCase.GetCaseRecord(strCaseid);
                myToolBar.Clear();
                InitToolbar();
                
			if (rc <= 0) {
				strData = "保存工单失败!";
				m_Submit_res = fun_main.getResult(name, strResult, strData);
				return;
			} else
				strData = "保存工单成功!";
            }
            else if (name.equals("Assign"))
            {
                int nStatus = Functions.ht_Get_nValue("STATUS", myCaseInfo.htCase);
                String strNewUrl = myString.Format("/ut_service/case_task_list.aspx?cmd=Edit&caseid={0}&ntype={1}", strCaseid, pType );
                if (nStatus == 0) //未分派任务的进入分派页面，分派过的进入列表页面
                    strNewUrl = myString.Format("/ut_service/case_task_assign.aspx?caseid={0}&ntype={1}", strCaseid, pType);
                //Response.Redirect(strNewUrl);
                Functions.js_exec("Add_MainTab('任务分派','" + strNewUrl + "')");
            }
            else if (name.equals("Delete"))
            {
                int nFind = 0;
                //strUname = myFld.get_item_text("UNAME");
                rc = myCase.DelCaseRecord(strCaseid);
                if (rc == 0) return;

                String strReturn_url = LastPageUrl_additem("history", "1");
                Functions.Redirect(strReturn_url);
            }
            else if (name.equals("TranCase"))
            {
                String strNewUrl, strHtml = "";
                String strType = myToolBar.get_item_value("Select_utype");
                if(strType.equals("转发工单至座席")) strType="0";
                else if(strType.equals("转发工单至短信")) strType="1";
                else if(strType.equals("转发工单至EMAIL")) strType="2";
                else if(strType.equals("转发工单至微信")) strType="3";
                
                String strTiltle = myToolBar.get_item_value("Select_utype");
                strNewUrl = myString.Format("~/ut_case/case_transto_home.aspx?caseid={0}&table={1}&casetype={2}&nType={3}", pCaseId, m_TableName, pType, strType);
                //strHtml = String.Format("fun_open('{0}','{1}',900,600);", strNewUrl, strTiltle); //add by zhaoj 20130902   显示为弹出框的形式
                if (myCase.nWF_Enable == 1) //add by gaoww 20151112 如果是使用工作流的工单，转发至座席时显示当前环节可受理工作流的座席
                {
                    String strUrl_para = myString.Format("?caseid={0}&casetype={1}&table={2}", pCaseId, pType, m_TableName);
                    strNewUrl = "~/ut_case/case_transto_uid.aspx" + strUrl_para;
                    if (strType == "1")
                        strNewUrl = "~/ut_case/case_transto_sms.aspx" + strUrl_para;
                    else if (strType == "2")
                        strNewUrl = "~/ut_case/case_transto_email.aspx" + strUrl_para;
                    else if (strType == "3") //add by gaoww 20151229 增加发送微信通知
                        strNewUrl = "~/ut_case/case_transto_wx.aspx" + strUrl_para;
                }
                strHtml = myString.Format("fun_open('{0}','{1}',900,600);", strNewUrl, strTiltle); //add by zhaoj 20130902   显示为弹出框的形式

                Functions.js_exec(strHtml);
            }
            else if (name.equals("Download_Rec"))
            {
                String strCallID = "";
                String strSDate = DateTime.Now().ToString("yyyyMMdd");
                if (myFld.isExist("CALLID") == true)
                    strCallID = myFld.get_item_text("CALLID");
                if (myFld.isExist("SDATE") == true)
                    strSDate = myFld.get_item_text("SDATE");
                if (strCallID.equals(""))
                {
                    Functions.MsgBox("呼叫编号为空，不存在录音文件！");
                }
                else
                {
                    String strTemp = myString.Format("../ut_calllog/frmRecord_edit.aspx?cmd=CALLID&filter=callid={0};sdate={1};", strCallID, strSDate);
                    Functions.Redirect(strTemp);
                }
            }
            else if (name.equals("Output_word"))
            {
                fnOutputWord(0);  //modify by gaoww 20100528 增加输出和输出并打印的区分
            }
            else if (name.equals("Output_word_Print")) //add by gaoww 20100527 增加输出并打印功能
            {
                fnOutputWord(1);
            }
            else if ((name.equals("Return")))  //2008.08 增加完成，返回页面后，重新刷新列表页面
            {
                // this.Response.Redirect(String.Format("case_list.aspx?cmd=list&ntype={0}", pType));
                String strReturn_url = LastPageUrl_additem("history", "1");
                Functions.Redirect(strReturn_url);
            }
            //add by yanj 20121108 新公文修改,在工单资料编辑界面增加发送消息功能
            else if ((name.equals("SendNote")))
            {
                String strNoteInfo = "CASEID=" + pCaseId + ";PTYPE=" + pType + ";";
                //this.Response.Redirect("~/ut_personality/ut_priv_note/note_msg_send.aspx?cmd=send&nType=2&nTaskType=2&taskinfo=" + strNoteInfo);
                String strNewUrl = pmSys.rootURL+ "/ut_personality/ut_priv_note/note_msg_send.aspx?cmd=send&ntype=3&nTaskType=2&taskinfo=" + strNoteInfo;
                Functions.js_exec("fun_open('" + strNewUrl + "','发送公文通知',1000,600);");
            }
            //add by fengw 20130722 设定任务提醒
            else if (name.equals("SetNotify"))
            {
                String strNotifyInfo = "CASE;CASEID=" + pCaseId + ";TYPE=" + pType + ";";
                String strNewUrl =pmSys.rootURL+"/ut_personality/Task_notice_edit.aspx?cmd=notify&taskinfo=" + strNotifyInfo;
                Functions.js_exec("win_openAlert('" + strNewUrl + "','设定任务提醒');");
            }
            else if ((name.equals("wf_accept")) || (name.equals("wf_refuse")))
            {
                //服务系统定制
                case_set_info myCase_temp = new case_set_info(pType);
                case_info myCaseInfo_temp = myCase_temp.GetCaseRecord(pCaseId);
                WFCase_info myWFCase = new WFCase_info(myCase_temp, myCaseInfo_temp);
                if (name.equals("wf_accept"))//签收
                {
                    if (myCase.isExist_Case(pCaseId) == false)
                    {
                        rem("工单不存在，请先保存再签收！");
                        return;
                    }
                    String strUid_prev;
                    pmRef<String> out = new pmRef<String>("");            
                    int nPS_prev = myWFCase.GetPSInfo_prev(pmAgent.uid, out);
                    strUid_prev= out.oRet;
                    int nResult = myWFCase.Submit_process(1, strUid_prev, pmAgent.uid, myCaseInfo.nProcess);
                    if (nResult == 1)
                    {
                        myCaseInfo = myCase.GetCaseRecord(pCaseId);
                        InitToolbar();
                        //服务管理定制，签收成功后，如果当前环节为可分配任务，则跳转到分配任务页面
                        if (pmAgent.c_Levels.check_authority(e_Level_service.service_dds) == true)
                        {
                            int nSupport = myCase.is_sm_support(myCaseInfo_temp.nProcess);
                            if (nSupport == 1)
                                Functions.Redirect("~/ut_service/case_task_assign.aspx?cmd=popup&caseid=" + pCaseId + "&ntype=" + pType);
                        }
                        rem("签收操作成功！");
                    }
                    else
                        rem("签收操作失败！");
                }
                else if (name.equals("wf_refuse"))//拒签 
                {
                    if (myWFCase.nRefuse_Count() >= pmSys.WorkFlow_Refuse_Counts)
                    {
                        rem("此工单的拒签已超过最大次数！");
                        return;
                    }
                    pmRef<String> strGhid=null;
                    int nPS_prev = myWFCase.GetPSInfo_prev("", strGhid);
                    if (nPS_prev < 0)
                    {
                        rem("拒签工单失败！");
                        return;
                    }
                    int nResult = myWFCase.Submit_process(2, pmAgent.uid, strGhid.oRet, nPS_prev);
                    if (nResult == 1)
                    {
                        //myCaseInfo = myCase.GetCaseRecord(pCaseId);
                        //myToorBar.Clear();
                        //InitToolbar();
                        //rem("拒签工单成功！");
                        String strReturn_url = LastPageUrl_additem("history", "1");
                        Functions.Redirect(strReturn_url);
                    }
                    else
                        rem("拒签工单失败！");
                }                
             }
            else if (name.startsWith("CASE_CUSTOM_") == true) //处理自定义按钮功能
            {
                String strAction_key = Functions.Substring(name, "CASE_CUSTOM_", "");
                DataTable myRow_Tool = dtTools.select("ACTION_KEY=" + strAction_key);
                if (myRow_Tool.getCount() > 0)
                {
                    String strFld_name = Functions.dtCols_strValue(myRow_Tool, "FLD_NAME");
                    String strNote = Functions.dtCols_strValue(myRow_Tool, "NOTE"); //进展情况
                    String strAction_case = Functions.dtCols_strValue(myRow_Tool, "ACTION_CASE"); //修改工单   
                    String strWF_cmd = ""; //工作流命令
                    if (strAction_case.equals("")==false) //修改工单状态
                    {
                        strAction_case = strAction_case.toUpperCase();
                        HashMap htCase = new HashMap();
                        if (Regex.IsMatch(strAction_case, "^[\\d]{1,3}$") == true)  //数字 modify by fengw 20151026 [\\d]{1,3} -> ^[\\d]{1,3}$
                        {
                            Functions.ht_SaveEx("STATUS", strAction_case,htCase);
                        }
                        else if (Regex.IsMatch(strAction_case, "WF") == true)
                        {
                            strWF_cmd = Functions.Substring(strAction_case, "WF-", "");
                        }
                        else
                        {
                            if (Regex.IsMatch(strAction_case, "[\\W_]+=([\\d]{1,3})|[DATETIME]|[DATE][TIME]") == true)
                            {
                                if (strAction_case.indexOf("[DATETIME]") > 0) strAction_case = strAction_case.replace("[DATETIME]", "'" + DateTime.Now().ToString("yyyy-MM-dd HH:mm:ss") + "'");
                                if (strAction_case.indexOf("[DATE]") > 0) strAction_case = strAction_case.replace("[DATE]", "'" + DateTime.Now().ToString("yyyy-MM-dd") + "'");
                                if (strAction_case.indexOf("[TIME]") > 0) strAction_case = strAction_case.replace("[TIME]", "'" + DateTime.Now().ToString("HH:mm:ss") + "'");
                            }
                            if (Regex.IsMatch(strAction_case, "UPDATE ") == true)
                            {
                                strAction_case = Functions.Substring(strAction_case, "SET", "");
                            }
                            String[] strFild_update = strAction_case.split("[,]");
                            for (int index = 0; index < strFild_update.length; index++)
                            {
                                String strFld = Functions.Substring(strFild_update[index], "", "=").trim();
                                String strValue = Functions.Substring(strFild_update[index], "=", "").trim();
                                Functions.ht_SaveEx(strFld, strValue,htCase);
                            }
                        }
                        if (htCase.size() > 0)
                        {
                            my_odbc pCase = new my_odbc(pmSys.conn_crm);
                            pCase.my_odbc_update(m_TableName, htCase, "CASEID='" + pCaseId + "'");
                            pCase.my_odbc_disconnect();
                        }
                    }

                    service_info myCaseInfo = myCase.GetCaseRecord(pCaseId);
                    m_htPriv= myCase.get_edit_priv(myCaseInfo, m_htPriv);
                    if (strWF_cmd.equals("")==false) //增加工作流功能处理
                    {
                        WF_Submit_process(strWF_cmd.toLowerCase()); 
                    }
                    myFld.setReload(true);
                    myFld.Load(m_TableName, "CASEID='" + pCaseId + "'", pmSys.conn_crm);    
                    InitToolbar();

                    String strSql = "";
                    DataTable dtTemp;
                    if (strNote.equals("")==false) //记录轨迹
                    {
                        if (strNote.indexOf("[NAME]") > 0)   //button名称
                            strNote = strNote.replace("[NAME]", "'" + strFld_name + "'");
                        if (strNote.indexOf("[UNAME]") > 0)  //座席姓名
                            strNote = strNote.replace("[UNAME]", "'" + pmAgent.name + "'");
                        if (strNote.indexOf("[PROCESS]") > 0)//环节名称
                        {
                            strSql = myString.Format("SELECT PROCESS_NAME FROM CRM_CASE_PROCESS WHERE PROCESS_ID='{0}'", myCaseInfo.nProcess);
                            dtTemp = Functions.dt_GetTable(strSql, "", pmSys.conn_crm);
                            strNote = strNote.replace("[PROCESS]", Functions.dtCols_strValue(dtTemp, "PROCESS_NAME"));
                        }
                        if (strNote.indexOf("[CASE]") > 0)   //工单类型名称
                        {
                            strSql = myString.Format("SELECT CASE_NAME FROM CRM_CASE_TABLE WHERE CASETYPE='{0}'", pType);
                            dtTemp = Functions.dt_GetTable(strSql, "", pmSys.conn_crm);
                            strNote = strNote.replace("[CASE]", Functions.dtCols_strValue(dtTemp, "CASE_NAME"));
                        }
                        String strInfo = myString.Format("TABLE={0};CASEID={1};TASKID={2};ORG_CODE={3};STATUS={4};STATUS_TASK={5};SUBMIT_FROM=0;", myCase.TableName, pCaseId, "", pmAgent.c_Info.agent_org_code, myCaseInfo.nStatus, "");
                        fun_service.addnew_trace_log(pType, strFld_name, strNote, strInfo);
                    }
                }
            }
        }

       
        private void Update_CaseRecord(String strCaseId, int nType)
        {
            int rc;
            HashMap htTemp = myFld.Save();
            if (htTemp.size() == 0)
            {
                Functions.MsgBox("工单保存失败！");
                return;
            }
           // if (myCase.judge_case_validity(htTemp) == false) return;

            my_odbc pCase = new my_odbc(pmSys.conn_crm);
            rc =pCase.my_odbc_find(m_TableName, "CASEID = '" + strCaseId + "'");//,0); dsCase = res.dtRet;
            if (rc != 1)
            {
                pCase.my_odbc_disconnect();
                Addnew_CaseRecord(strCaseId);
                return;
            }
            pCase.my_odbc_disconnect();

            //delete by gaoww 20140813 已经在保存按钮的是否显示和是否可用。做过判断
            //add by gaoww 20081121增加判断，检查是否有修改工单的权限，如果有还需要判断工单的所属人、当前所属人或闭单人是当前坐席时可以修改工单，否则提示不可修改工单。
            /*if (myCase.get_authority(myCaseInfo.nProcess, myCaseInfo.nStatus, "update") == false)
            {
                Functions.MsgBox("对不起，您没有修改此工单的权限！");
                return;
            }*/

            //rc = myFld.save.Save(out htTemp);
            //if (rc != 1) return;
            if (htTemp.containsKey("PROCESS") == true)   //ADD BY GAOWW 20100504 增加判断，如果修改的时候有process和process_status字段，则自动移除，避免工作流已经修改了工单的process和process_status字段后，由于座席工单编辑页面 没有关闭，在进行保存时，会更改这两个字段值，导致后面的签收或提交操作验证这两个字段不正确的问题
                htTemp.remove("PROCESS");
            if (htTemp.containsKey("PROCESS_STATUS") == true)//ADD BY GAOWW 20100504 增加判断，如果修改的时候有process和process_status字段，则自动移除，避免工作流已经修改了工单的process和process_status字段后，由于座席工单编辑页面 没有关闭，在进行保存时，会更改这两个字段值，导致后面的签收或提交操作验证这两个字段不正确的问题
                htTemp.remove("PROCESS_STATUS");
            if (pCaseId_Rel.equals("")==false) //add by gaoww 20151202 增加关联工单编号保存
                Functions.ht_SaveEx("CASEID_REL", pCaseId_Rel,htTemp);
          
            rc = myCase.UpdateCaseRecord(strCaseId, htTemp);
            if (rc <= 0) return;
            Functions.MsgBox("提示", "工单保存成功！");
        }

        private int Addnew_CaseRecord(String strCaseId)
        {
            int rc = 0 ;

            HashMap htTemp;
            //rc = myFld.Save(out htTemp);
            //if (rc != 1) return;
            htTemp = myFld.Save();
            if (htTemp.isEmpty())
            { 
           	     return rc;
            }
            if (myFld.isExist("CASETYPE") == false)
                htTemp.put("CASETYPE", pType);
            if (myFld.isExist("SDATE") == false)
                htTemp.put("SDATE", DateTime.Now().ToString("yyyyMMdd"));
            if (myFld.isExist("STIME") == false)
                htTemp.put("STIME", DateTime.Now().ToString("HHmmss"));
            //if (myFld.isExist("TEL") == true)   //解决tel字段不存在，保存工单时提示电话号码不能为空
            //    Functions.ht_SaveEx("TEL", myFld.get_item_txt("TEL").Text, ref htTemp);

            //	else if(fld_name=="EDATE")
            //		myFld.get_item_txt("EDATE").Text = DateTime.Now.ToString("yyyyMMdd");
            //	else if(fld_name=="CASEID")
            //		myFld.get_item_txt("CASEID").Text = DateTime.Now.ToString("yyyyMMddHHmmss01");
            if (myFld.isExist("GHID") == false)
                htTemp.put("GHID", pmAgent.uid);
            if (myFld.isExist("CURRENTGHID") == false)
                htTemp.put("CURRENTGHID", pmAgent.uid);
            if (pCaseId_Rel.equals("")==false) //add by gaoww 20151202 增加关联工单编号保存
                Functions.ht_SaveEx("CASEID_REL", pCaseId_Rel,htTemp);
            
            //2007.11.21 修改，新建工单的工单状态，为坐席人员选中的工单状态，原来新建工单保存时的工单状态毕为为“0-未处理”
            //myFld.SaveEx("STATUS", pStatus, ref htTemp);
            rc = myCase.AddCaseRecord(strCaseId, htTemp);
            if (rc <= 0) return rc;
            // if (pParentForm != null) pParentForm.nRefresh_grid = 1;

            if (myFld.isExist("CASEID") == true)
              	 myFld.set_item_style("CASEID","background-color;"+myColor.bg_popup_exist);
            
          
            String txtName = myFld.get_item_text("CASENAME");
            Functions.MsgBox("提示", "<" + txtName + ">工单添加成功！");
           

            if (m_htPriv.containsKey("CMD_RDONLY") == true)
            {
                List<String> alRet; 
                //alRet = Arrays.asList(Functions.ht_Get_strValue("CMD_RDONLY", m_htPriv));
                alRet=(List<String>)m_htPriv.get("CMD_RDONLY");
                 for (String strCmd : alRet)
                {
                    myToolBar.set_readonly(strCmd, true);
                }
            }
            return 1;
        }

        /// <summary>
        /// 输出word文档
        /// </summary>
        /// <param name="nType">0-只输出word，1-输出并打印</param>
        private void fnOutputWord(int nType)
        {
            //判断传入的casrId是否存在，若不存在则给出提示；
            my_odbc myTable = new my_odbc(pmSys.conn_crm);
            String strSql = myString.Format("SELECT * FROM {0} WHERE CASEID='{1}'", m_TableName, pCaseId);
            int rc = myTable.my_odbc_find(strSql);
            myTable.my_odbc_disconnect();
            if (rc <= 0)
            {
                Functions.MsgBox("该工单不存在！请先保存");
            }
            else
            {
                String strFileName_output = m_TableName + ".doc";
                HashMap htTemp = myFld.Save();
                HashMap htRet = get_case_output(myCase, htTemp, myFld);
                if (htRet.isEmpty() ==false)
                {
                    //不同的工单，使用不同的word模版
                    //string strCaseName = pmSys.web_phypath + "\\_APP_DATA\\template\\" + pTableName + ".dot";
                   String strCaseName = pmSys.web_phypath + "\\_APP_DATA\\template\\" + m_TableName + ".htm";
                  
                   if (myFile.Exists(strCaseName) == false)
                        strCaseName = pmSys.web_phypath + "\\_APP_DATA\\template\\crm_case.htm";
                   Functions.OutputWordFile_withTemplate(strCaseName, strFileName_output, htRet);
                }
            }
        }

        /// <summary>
        /// 获取 当前工单 输出数据
        /// </summary>
        /// <param name="myCase"></param>
        /// <param name="htSave"></param>
        /// <returns></returns>
        private HashMap get_case_output(service_set_info myCase, HashMap htSave, my_Field myFld)
        {
       	 HashMap htTemp = new HashMap();

            DataTable dtDesc;
            DataTable dtCase, dtCust;

            String fld_value, fld_name;
            int fld_vlevel, fld_edit_type;

            String strTableName = myCase.TableName;
            String strCaseId = Functions.ht_Get_strValue("CASEID", htSave);
            String strUserId = Functions.ht_Get_strValue("USERID", htSave);

            //Functions.dt_GetTable(strTableName, "1>1");
            my_odbc pCust = new my_odbc(pmSys.conn_crm);
            pmList mRet = pCust.my_odbc_find("SELECT * FROM " + strTableName + " WHERE CASEID = '" + strCaseId + "'",0);
            int rc= mRet.nRet;
            dtCase  = mRet.dtRet;
            pCust.my_odbc_disconnect();
            if (rc == 1)
            {
                dtDesc = fun_Form.get_desc_data(myCase.DescName, "((FLD_VLEVELS&1)<>0) ORDER BY EDIT_INDEX");
                if (dtDesc != null)
                {
                    for (int i = 0; i < dtDesc.getCount(); i++)
                    {
                        fld_name = Functions.dtCols_strValue(dtDesc, i, "FLD_NAME");
                        fld_value = Functions.dtCols_strValue(dtDesc, i, "FLD_VALUE");
                        fld_vlevel = Functions.dtCols_nValue(dtDesc, i, "FLD_VLEVELS");

                        fld_edit_type = Functions.dtCols_nValue(dtDesc, i, "EDIT_TYPE");
                        String fld_type = Functions.dtCols_strValue(dtDesc, i, "FLD_TYPE");
                        String fld_cbo_list = "";
                        String strValue = "";
                        if (fld_edit_type == 1) //ComboBox 
                        {
                            strValue = myFld.get_item_text(fld_value);
                        }
                        if (fld_edit_type == 3) //check button
                        {
                            int nValue = -1;
                            fld_cbo_list = Functions.dtCols_strValue(dtDesc, i, "FLD_CBO_LIST");
                            String[] strCbo_list = fld_cbo_list.split(",");
                            if (htSave.containsKey(fld_value) == true)
                                nValue = Functions.atoi(Functions.ht_Get_strValue(fld_value,htSave));
                            for (int index = 0; index < strCbo_list.length; index++)
                            {
   							//if(nValue|i)
                                if ((nValue & Functions.atoi(Math.pow(2, index))) == 0) //未选中
                                {
                                    if (strValue.length()==0)
                                        strValue = "□" + strCbo_list[index];
                                    else
                                        strValue += "  □" + strCbo_list[index];
                                }
                                else //选中
                                {
                                    if (strValue.length()==0)
                                        strValue = "√" + strCbo_list[index];
                                    else
                                        strValue += "  √" + strCbo_list[index];
                                }
                            }
                        }
                        else if (fld_edit_type == 2) //radio button
                        {
                            int nValue = -1;
                            fld_cbo_list = Functions.dtCols_strValue(dtDesc, i, "FLD_CBO_LIST");
                            String[] strCbo_list = fld_cbo_list.split(",");
                            if (htSave.containsKey(fld_value) == true)
                                nValue = Functions.atoi(Functions.ht_Get_strValue(fld_value,htSave));
                            for (int index = 0; index < strCbo_list.length; index++)
                            {
                                //if(nValue|i)
                                if ((nValue & Functions.atoi(Math.pow(2, index))) == 0) //未选中
                                {
                                    if (strValue .length()==0)
                                        strValue = "○" + strCbo_list[index];
                                    else
                                        strValue += "  ○" + strCbo_list[index];
                                }
                                else //选中
                                {
                                    if (strValue.length()==0)
                                        strValue = "⊙" + strCbo_list[index];
                                    else
                                        strValue += "  ⊙" + strCbo_list[index];
                                }
                            }
                        }
                        

                        if (htSave.containsKey(fld_value) == true)
                            if (fld_edit_type != 1 && fld_edit_type != 2 && fld_edit_type != 3)
                                htTemp.put("CASE_" + fld_value, Functions.ht_Get_strValue(fld_value, htSave));
                            else
                                htTemp.put("CASE_" + fld_value, strValue);
                        else if ((dtCase != null) && (dtCase.getCount() > 0) && (dtCase.Columns().contains(fld_value) == true))  //2007.09.17 hanxy 判断如果字段在描述表中存在，而在数据库表中不存在，则不添加哈希表
                            htTemp.put("CASE_" + fld_value, Functions.dtCols_strValue(dtCase, fld_value));
                    }
                }
            }
            //DataTable dtCust;
            pmRet mRet_cust  = fun_Cust.get_user_by_userid(strUserId);
            rc = (int) mRet_cust.nRet;
            dtCust  = (DataTable) mRet_cust.oRet;
            if (rc >= 0)
            {
                customer_set_info myCust = new customer_set_info(rc);
                dtDesc = fun_Form.get_desc_data(myCust.DescName, "((FLD_VLEVELS&1)<>0) ORDER BY EDIT_INDEX");
                //dsCust.Tables[0].PrimaryKey = new DataColumn[] {dsCust "USERID"};
                //dtCust = dsCust.Tables[0].Clone;
                //dtCust = dsCust.Tables[0];  //modify by gaoww 20100421 使用clone只是结构，无法将值赋给dtcust，导致后面取值时报错
                if (dtDesc != null)
                {
                    for (int i = 0; i < dtDesc.getCount(); i++)
                    {
                        fld_name = Functions.dtCols_strValue(dtDesc, i, "FLD_NAME");
                        fld_value = Functions.dtCols_strValue(dtDesc, i, "FLD_VALUE");
                        fld_vlevel = Functions.dtCols_nValue(dtDesc, i, "FLD_VLEVELS");
                        if (dtCust.Columns().contains(fld_value) == true)  //2007.09.17 hanxy 判断如果字段在描述表中存在，而在数据库表中不存在，则不添加哈希表
                            htTemp.put("CUST_" + fld_value, Functions.dtCols_strValue(dtCust, fld_value));
                    }
                }
            }
            return htTemp;
        }

   
        //根据工作流bm_id做对应操作
        private void WF_Submit_process(String strCmd)
        {
            boolean bRet = myCase.get_authority(myCaseInfo, "wf");
            if (bRet == false)
            {
                rem("对不起，您无权处理此工单！");
                return;
            }
            int nResult = 0;
            case_set_info myCase_temp = new case_set_info(pType);
            case_info myCaseInfo_temp = myCase_temp.GetCaseRecord(pCaseId);
            WFCase_info myWFCase = new WFCase_info(myCase_temp, myCaseInfo_temp);

            //取出第一环节、最后一环节 ID
            int m_nFirst_Id = 0, m_nLast_Id = 0;
            DataTable dtRet;
            my_odbc pProcess = new my_odbc(pmSys.conn_crm);
            pmList res =pProcess.my_odbc_find("CRM_CASE_PROCESS", "CASETYPE='" + pType + "' ORDER BY WF_INDEX ASC",0); dtRet = res.dtRet;
            int rc = res.nRet;
            pProcess.my_odbc_disconnect();
            int nNumCol = dtRet.getCount();
            if (nNumCol > 0)
            {
                m_nFirst_Id = Functions.dtCols_nValue(dtRet, 0, "PROCESS_ID");
                m_nLast_Id = Functions.dtCols_nValue(dtRet, nNumCol - 1, "PROCESS_ID");
            }
            if (strCmd.equals("accept"))
            {
                if (myCase.isExist_Case(pCaseId) == false)
                    return;
                pmRef<String> strUid_prev= new pmRef<String>("");   
                int nPS_prev = myWFCase.GetPSInfo_prev(pmAgent.uid,  strUid_prev);
                nResult = myWFCase.Submit_process(1, strUid_prev.oRet, pmAgent.uid, myCaseInfo.nProcess);
                if (nResult == 1)
                {
                    Functions.MsgBox("提示", "签收操作成功！");
                }
                else
                    rem("签收操作失败！");
            }
            else if (strCmd.equals("refuse"))//拒签 
            {
                if (myWFCase.nRefuse_Count() >= pmSys.WorkFlow_Refuse_Counts)
                {
                    Functions.MsgBox("提示", "此工单的拒签已超过最大次数！");
                    return;
                }
                pmRef<String> strGhid=null;
                int nPS_prev = myWFCase.GetPSInfo_prev("", strGhid);
                if (nPS_prev < 0)
                {
                    Functions.MsgBox("提示", "拒签工单失败！");
                    return;
                }
                nResult = myWFCase.Submit_process(2, pmAgent.uid, strGhid.oRet, nPS_prev);
                if (nResult == 1)
                {
                    Functions.MsgBox("提示", "拒签工单成功！");
                }
                else
                    rem("拒签工单失败！");
            }

            else if (strCmd.equals("submit"))//提交 
            {
                service_set_info myService = new service_set_info(pType);
                int nSupport = myService.is_sm_support(myCaseInfo.nProcess);
                if (nSupport == 1)  //服务管理定制，如果当前环节是可以任务分配的，需要判断工单状态是否已经完成，才能提交
                {
                    if (Check_task_finished() == false) //modify by gaoww 20151102 ,由于工单状态贯穿所有环节，所以改为判断任务执行状态
                    {
                        Functions.MsgBox("当前环节工单还未结束，不能提交！");
                        return;
                    }
                }
                int nAccept_auto = 0;
                pmRef<Integer> out = new pmRef<Integer>(0);
                int nProcess_next = myWFCase.get_process_index(1, out); nAccept_auto = out.oRet;
                List<String> alRet;                
                m_htPriv= myCase.get_edit_priv(myCaseInfo, m_htPriv);
                //检查是否有不为空的字段
                if (m_htPriv.containsKey("FLD_MUST") == true)
                {
                    alRet = (List<String>)m_htPriv.get("FLD_MUST");
                    String strFilter = "";
                    //去除 dtStru 中alRet（FLD_VALUE）包含的内容FLD_VALUE 
                    String strFilter_fld = "";
                    for (String strFld : alRet)
                    {
                        if (strFilter.length() < 1)
                            strFilter = myString.Format("(LEN({0})<1 OR {0} IS NULL)", strFld);  //modify by gaoww 20130403
                        else
                            strFilter += myString.Format(" OR (LEN({0})<1 OR {0} IS NULL)", strFld); //modify by gaoww 20130403

                        if (strFilter_fld.equals(""))
                            strFilter_fld = "FLD_VALUE='" + strFld + "'";
                        else
                            strFilter_fld += " OR FLD_VALUE='" + strFld + "'";
                    }
                    if (strFilter.length() > 0)
                    {
                        HashMap htCase = new HashMap();
                        my_odbc pTable = new my_odbc(pmSys.conn_crm);
                        String strSql = "SELECT * FROM " + myCase.TableName + " WHERE CASEID ='" + pCaseId + "' AND (" + strFilter + ")";
                        pmMap map =pTable.my_odbc_find(strSql,true); htCase = map.htRet;
                        int rc1 = map.nRet;
                        pTable.my_odbc_disconnect();
                        if (rc1 == 1)
                        {
                            String strFilter_null = "";
                            for (String strFld : alRet)
                            {
                                if (Functions.ht_Get_strValue(strFld, htCase) == "")
                                {
                                    if (strFilter_null.equals(""))
                                        strFilter_null = "FLD_VALUE='" + strFld + "'";
                                    else
                                        strFilter_null += " OR FLD_VALUE='" + strFld + "'";
                                }
                            }
                            String strFilter_name = Get_Filter_name(strFilter_null);  //modify by gaoww 20150828

                            Functions.MsgBox("<" + strFilter_name + ">未填写正确，不能提交，请先正确填写！");
                            return;
                        }
                    }
                }

                //string strGhid = Functions.Substring(cboSubmitGhid.Text, "", "-");
                int nTemp = 0;
                String strTemp = String.valueOf(myWFCase.get_process_index(1, out)); nTemp = out.oRet; //下一环节不应该直接编号+1，应该用工作流序号来获取             
                List<String> alUid =fun_service.GetUid_byPriv(strTemp,pType);
                String strGhid_recv=""; //add by gaoww 20151112 接收座席工号，获取下一环节受理人工号，如果有人在线默认为第一个座席，如果没人在线或多人在线则为空
                int nOnline_cnt = 0;
                if (pmInfo.myATClient != null)
                {
                    for (String strItem : alUid)
                    {
                        String strGhid = Functions.Substring(strItem, "", "-");
                        String strUidInfo = pmInfo.myATClient.ATGetUidInfo(strGhid);
                        String strUid_Status = Functions.Substring(strUidInfo, "STATUS",1);//获取工号状态
                        if (strUid_Status.equals("00")==false)
                        {
                            nOnline_cnt++;
                            strGhid_recv = strGhid;
                        }
                    }
                }
                if (nOnline_cnt != 1) //如果有多个人在线，或无人在线，则接收人为空
                    strGhid_recv = "";
                nResult = myWFCase.Submit_process(3, pmAgent.uid, strGhid_recv, 0);
                if (nResult == 1)
                {
                    Functions.MsgBox("提示", "提交工单成功！");
                    //服务管理定制，提交成功后，如果当前座席有调度权限，则判断是否进入调度页面
                    //如果当前环节不是最后一环节，则判断下一环节是否可以分配任务,如果有则通知工单编辑页面跳转到调度页面
                    HashMap htCase = new HashMap();
                    if (myCaseInfo.nProcess != m_nLast_Id)
                    {
                        nSupport = myService.is_sm_support(nProcess_next);
                        if (nSupport == 1)
                        {
                            //将工单状态强制改为0
                            Functions.ht_SaveEx("TASK_LEVEL", "0",htCase); //modify by gaoww 20151102 ，“是否分派”改为初始值0
                            myCase.UpdateCaseRecord(pCaseId, htCase);
                        }
                    }
                }
                else if (nResult == -2)
                    rem("提交工单失败，未找到合适的流程节点！");
                else
                    rem("提交工单失败！");
            }
            else if (strCmd.equals("backward"))// 退回
            {
                if (myWFCase.nBack_Count() >= pmSys.WorkFlow_Refuse_Counts)
                {
                    rem("此工单的退回已超过最大次数！");
                    return;
                }
                if (myCaseInfo.nProcess == m_nFirst_Id)
                {
                    rem("此环节已是第一环节，无法执行退回操作！");
                    return;
                }
                String strGhid = "";
                int nProcess_new = -1;  //编辑页面快捷操作，只能退回到前一环节   
                String strSql = myString.Format("SELECT PS_PREV,GHID_PREV FROM CRM_CASE_TRACE WHERE CASEID='{0}' AND CASETYPE={1} AND PROCESS={2} AND PS_STATUS_RECV=1  ORDER BY AUTOID DESC", pCaseId, pType, myCaseInfo.nProcess);
                my_odbc pTable = new my_odbc(pmSys.conn_crm);
                rc = pTable.my_odbc_find(strSql);
                if (rc == 1)
                {
                    nProcess_new = pTable.my_odbc_result("PS_PREV",0);
                    strGhid = pTable.my_odbc_result("GHID_PREV");
                }
                pTable.my_odbc_disconnect();
                if (nProcess_new == -1)
                {
                    rem("退回工单失败！");
                }

                nResult = myWFCase.Submit_process(4, pmAgent.uid, strGhid, nProcess_new);
                if (nResult == 1)
                {
                    Functions.MsgBox("提示", "退回工单成功！");
                }
                else if (nResult == -2)
                    rem("退回工单失败，未找到合适的流程节点！");
                else
                    rem("退回工单失败！");

            }
        }

        private String Get_Filter_name(String strFilter_fld)
        {
            String strReturn = "";
            if (strFilter_fld.equals("")) return strReturn;
            DataTable dtDesc = fun_Form.get_desc_data(myCase.DescName, strFilter_fld);
            if (dtDesc == null) return strReturn;
            for (int rows = 0; rows < dtDesc.getCount(); rows++)
            {
                String fld_value = Functions.dtCols_strValue(dtDesc, rows, "FLD_NAME");
                if (strReturn.equals(""))
                    strReturn = fld_value;
                else
                    strReturn += "," + fld_value;
            }
            return strReturn;
        }

        /// <summary>
        /// 检查环节下的任务是否结束 add by gaoww 20151102 
        /// </summary>
        /// <returns></returns>
        private boolean Check_task_finished()
        {
            boolean bResult = false;
            DataTable dtTable = new DataTable();
            String strSql = myString.Format("SELECT STATUS FROM SM_TASK_DISP WHERE CASEID='{0}' AND PROCESS='{1}' AND TASK_LEVEL='0'", pCaseId, myCaseInfo.nProcess); //只查当前环节的主任务，从任务不用管
            my_odbc pTable = new my_odbc(pmSys.conn_crm);
            pmList res =pTable.my_odbc_find(strSql,0); dtTable = res.dtRet;
            int rc = res.nRet;
            pTable.my_odbc_disconnect();
            if (rc >= 0) //modify by gaoww 20151112
            {
                if (dtTable.getCount() == 0) //没有任务可以直接提交
                    bResult = true;
                else
                {
               	 DataTable myRow = dtTable.select("STATUS<98"); //是否有主任务不是取消或完成
                    if (myRow.getCount() == 0)
                        bResult = true;
                }
            }
            return bResult;
        }

        private TabControl tabBody;
        private void Fill_subForm(int nSubMenu,Model model)
        {
            if (nSubMenu == 0)
            {
            	 Functions.setCookie("hide_more", "1", 20);            
                 return;
            }
            nSubMenu = myCase.nSubMenu;
            if (nSubMenu == 0) //不显示子菜单
            {
            	 Functions.setCookie("hide_more", "1", 20);
                return;
            }

            tabBody = new TabControl("plRelation");
            tabBody.setHeight(String.valueOf(pmAgent.content_height)+"px");
            Fill_Relation(nSubMenu);
            tabBody.render(model);
        }

        private void Fill_Relation(int nVisible)
        {
            try
            {
                //delete by gaoww 20160108
                //add by gaoww 20141029 增加判断，如果pmSys.bsCRM_homepage为空了，则重新取一下，否则子菜单会找不到页面
                /*if (pmSys.bsCRM_homepage.equals("") || pmSys.bsCRM_homepage == null)
                {
                    string strRootUrl = "http://" + HttpContext.Current.Request.ServerVariables["HTTP_HOST"] + Session["rootURL"].ToString();
                    if (strRootUrl.EndsWith("/") == true) strRootUrl = strRootUrl.TrimEnd('/');
                    pmSys.bsCRM_homepage = strRootUrl;
                }*/

                String strCaller = "", strUserid = "", strCallid = "", strSdate = "";
                if (myFld.isExist("CALLER") == true) strCaller = myFld.get_item_text("CALLER");
                if (myFld.isExist("USERID") == true) strUserid = myFld.get_item_text("USERID");
                if (myFld.isExist("CALLID") == true) strCallid = myFld.get_item_text("CALLID");
                if (myFld.isExist("SDATE") == true) strSdate = myFld.get_item_text("SDATE"); //add by gaoww 20140606
                if (strUserid.equals(""))
                {
                    String strTableName;
                    my_odbc pCase = new my_odbc(pmSys.conn_crm);
                    int rc = pCase.my_odbc_find("SELECT TABLE_NAME FROM CRM_CASE_TABLE WHERE CASETYPE = " + pType);
                    if (rc > 0)
                    {
                        strTableName = pCase.my_odbc_result("TABLE_NAME");
                        rc = pCase.my_odbc_find("SELECT USERID FROM " + strTableName + " WHERE CASEID = '" + pCaseId + "'");
                        if (rc > 0) strUserid = pCase.my_odbc_result("USERID");
                    }
                    pCase.my_odbc_disconnect();
                }

                int idx;
                int myCount;
                String[] mySubMenu;// = new String[]  {"联系人资料","关联电话表","财务信息","业务记录","电话记录"};
                String[] mySubForm = null;
                DataTable dt = new DataTable();
                //if (mySubForm == null) //第一次调用
                {
                    //SqlConnection myConn = new SqlConnection(pmSys.conn_crm);
                    //string strTemp = String.Format("SELECT * FROM CRM_CASE_SUBMENU WHERE ((VISIBLE&{0})<>0 AND (LEVELS&{1})<>0) ORDER BY DisplayOrder", nVisible, pmAgent.c_Levels.nMenu);//modify by gaoww 20080917 解决submenu中不能正确显示子卡片页的问题。原来合成的sql语句中level变量后面应该是1而不是0
                    //SqlDataAdapter myDa = new SqlDataAdapter(strTemp, myConn);
                    //myDa.Fill(dt);

                    String strTemp = myString.Format("SELECT Text,WebForm_url FROM CRM_CASE_SUBMENU WHERE ((VISIBLE&{0})<>0 AND (LEVELS&{1})<>0) ORDER BY DisplayOrder", nVisible, pmAgent.c_Levels.getMenu());
                    dt = Functions.dt_GetTable(strTemp, "", pmSys.conn_crm);

                    myCount = dt.getCount();
                    if (myCount > 0)
                    {
                        //string strDllName, strFormName,strParam;
                        String strUrl;

                        mySubMenu = new String[myCount];
                        mySubForm = new String[myCount];
                        for (idx = 0; idx < myCount; idx++)
                        {
                            mySubMenu[idx] = Functions.dtCols_strValue(dt, idx, "Text");
                            strUrl = Functions.dtCols_strValue(dt, idx, "WebForm_url");
                            if (strUrl.indexOf("~") >= 0)
                                strUrl = strUrl.replace("~", pmSys.bsCRM_homepage());
                            if (strUrl.indexOf("?") > 0)
                            {
                                //与V310兼容
                                strUrl = strUrl.replace("$P1", strUserid);
                                strUrl = strUrl.replace("$P2", strCaller);
                                strUrl = strUrl.replace("$P3", pCaseId);
                                //V320格式
                                strUrl = strUrl.replace("$(USERID)", strUserid);
                                strUrl = strUrl.replace("$(CALLER)", strCaller);
                                strUrl = strUrl.replace("$(CALLID)", strCallid);//add by zhaoj 20130902 
                                strUrl = strUrl.replace("$(CASEID)", pCaseId);
                                //strUrl = strUrl.Replace("$(IVRINFO)", pmAgent.Ivrinfo);
                                strUrl = strUrl.replace("$(CASETYPE)", Functions.atos(pType));
                                strUrl = strUrl.replace("$(SDATE)", strSdate); //add by gaoww 20140606
                            }
                            mySubForm[idx] = strUrl;//Functions.newForm(this, strDllName, strFormName, strParam);
                        }
                    }
                    else //动态菜单不存在，使用默认值
                    {
                        mySubMenu = new String[] { "用户资料", "业务记录", "电话记录" };
                        mySubForm = new String[] { pmSys.bsCRM_homepage()+"/ut_customer/customer_list.aspx",//?pUserid, pCaller, "Relation"),
                                         pmSys.bsCRM_homepage()+"/ut_case/case_list.aspx",//new frmCustomer_telno(pUserid, pCaller, 1),
                                         //"~/ut_tools/common_from_list.aspx",//new frmCommon_form_list(0,"USERID='" + pUserid + "'"),
                                         //"~/ut_tools/common_from_list.aspx",//new frmCase_list_select("USERID='" + pUserid + "'"),
                                        pmSys.bsCRM_homepage()+ "/ut_customer/customer_call_list.aspx"//new frmPopupTel_list(pUserid, pCaller)
                                       };
                        myCount = mySubMenu.length;
                    }
                }

               // ctsTools.Controls.TabPage[] myPage = new ctsTools.Controls.TabPage[myCount];
                for (idx = 0; idx < mySubMenu.length; idx++)
                {
               	 TabPage myPage = new  TabPage();           
               	 myPage.Id =String.valueOf(idx);
               	 myPage.Title = mySubMenu[idx];
                    myPage.NavigateUrl = mySubForm[idx];
                    myPage.NavigateType = idx;
                    if(idx==0)
                   	 myPage.Selected=true;
                    tabBody.setId(String.valueOf(pType));
                    tabBody.TabPages().put(idx,myPage);
                }
            }
            catch (Exception ex)
            {
            }
        }

        protected void rem(String strMsg)
        {
            Functions.MsgBox(strMsg);
            //lblRem.Text = strMsg;
        }

        /*
        [System.Web.Services.WebMethod]//modify by gaoww 20130124 不在使用ctsTools.ajax方法
        public static string GetParam(string strCmd, string strData)
        {
            string strReturn = "FAIL";
            Hashtable htRet = HttpContext.Current.Session["vs_case_edit"] as Hashtable;
            if (htRet != null)
            {
                // strReturn = fun_json.Hashtable_toJson(htRet);
                string nType = htRet["pType"].ToString();
                string strTableName = htRet["pTableName"].ToString();
                strReturn = String.Format("&CASETYPE={0}&CASETABLE={1}", nType, strTableName);
            }
            return strReturn;
        }
       */

    }

