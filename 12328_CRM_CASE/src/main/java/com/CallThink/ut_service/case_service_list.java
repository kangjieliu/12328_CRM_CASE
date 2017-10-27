///########################################################################################
/// Copyright (C) 2000, ToneThink.Soft  All Rights Reserved. 
///#########################################################################################
/// 文件创建时间：2015-09-07
///   文件创建人：gaoww
/// 文件功能描述：服务工单列表页面
///     调用格式：
///     服务单浏览：cmd=view&ntype=1
///     异常服务单：cmd=abnormal&ntype=1
///     待调度服务单：cmd=assign&ntype=1
///     二次调度：cmd=assign_sec&ntype=1
///     待签收：cmd=unaccept&ntype=1
///     被退回：cmd=backward&ntype=1
///     维护记录：
/// 2015.09.07：create by gaoww         
///#########################################################################################
package com.CallThink.ut_service;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.ui.Model;

import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.pmSys;
import com.CallThink.base.pmCode.UltraCRM_Page;
import com.CallThink.base.support.pmInfo;
import com.CallThink.ut_form.pmModel_form.fun_Form;
import com.CallThink.ut_service.pmModel_service.e_Level_service;
import com.CallThink.ut_service.pmModel_service.service_set_info;
import com.ToneThink.DataTable.DataRow;
import com.ToneThink.DataTable.DataTable;
import com.ToneThink.DateTime.DateTime;
import com.ToneThink.ctsTools.Regex.Regex;
import com.ToneThink.ctsTools.WebUI.Fld_attr;
import com.ToneThink.ctsTools.WebUI.my_Field;
import com.ToneThink.ctsTools.WebUI.my_SearchField;
import com.ToneThink.ctsTools.WebUI.my_ToolStrip;
import com.ToneThink.ctsTools.WebUI.my_dataGrid;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.myString;
import com.ToneThink.ctsTools.myUtility.pmMap;
import com.ToneThink.ctsTools.myUtility.pmRet;
import com.alibaba.druid.sql.ast.expr.SQLSequenceExpr.Function;
    public class case_service_list extends UltraCRM_Page
    {
        //aspx页面定义 hidn_nSearch 控件，用于保存最后一次查询方式：0-快速查询，1-高级查询

        private String pTableName = "CRM_CASE";
        private int pType = 0;
        private String pOp = "list";
        private String pFilter = "";
        private String m_Process_task = ""; //服务管理定制，可分配任务环节

        private String m_Filter_search = ""; //本页面人工选择的查询条件，会话需要保存在Session中
        //页面刷新时，该条件清空 
        private String pCaseId_Rel = ""; //关联工单的工单编号 add by gaoww 20151202
        private String pMenu_id = "";    //菜单编号 add by gaoww 20151214 避免casetype相同但filter不同的菜单同时打开，列表显示条件的session互相干扰

        service_set_info myCase;
        my_SearchField myFld_Query = new my_SearchField(2);
        my_dataGrid mydg = new my_dataGrid(51);
        my_ToolStrip myToolBar = new my_ToolStrip();
        
        //高级查询
        private String[][] pFld;
        my_ToolStrip myToolBar_custom = new my_ToolStrip();
        my_Field myFld_Query_custom = new my_Field(3);
        
        public void Page_Load(Object sender, Model model)
        {
            int nHistory = 0;
            String strTitle = "工单资料管理";
            if (IsPostBack == false)//正被首次加载和访问
            {
            	pmAgent = fun_main.GetParm();
                HashMap htQuery;
                pmMap res = fun_main.QuerySplit(Request); htQuery = res.htRet;
                int rc = res.nRet;
                if (rc > 0)
                {
                    pOp = Functions.ht_Get_strValue("cmd", htQuery);
                    pType = Functions.atoi(Functions.ht_Get_strValue("ntype", htQuery));//add by zhanglh 20080505 for 工单类型  
                    pFilter = Functions.ht_Get_strValue("filter", htQuery);
                    nHistory = Functions.atoi(Functions.ht_Get_strValue("history", htQuery));
                    pCaseId_Rel = Functions.ht_Get_strValue("caseid_rel", htQuery);
                    pMenu_id = Functions.ht_Get_strValue("menu_id", htQuery);
                    if (nHistory > 0)  //编辑页面返回
                    {
                        String strFilter_temp = Load_ss("pFilter_search" + pType + pMenu_id);
                        if (strFilter_temp.length() > 0) m_Filter_search = strFilter_temp;
                        mydg.nRestore_history = 1;
                    }
                }
                //string strUrl = "(status=1 and process=1)";
                //strUrl = Functions.strURL_Encode(strUrl);
                
                Save_vs("pType", pType);
                Save_vs("pOp", pOp);
                Save_vs("pFilter", pFilter);
                Save_ss("pFilter_search" + pType + pMenu_id, m_Filter_search);  //add by gaoww 20140604 存入Session 变量，解决高级查询后，如果直接双击卡片页，再翻页，就会将之前查询结果显示出来
                Save_vs("pCaseId_Rel", pCaseId_Rel);
                Save_vs("pMenu_id", pMenu_id);
               
                //Save_vs("pFilter_custom", pFilter_custom);
                //Save_vs("Query_type", m_Query_type);
            }
            else
            {
                pOp = Load_vs("pOp");
                pType = Functions.atoi(Load_vs("pType"));
                pFilter = Load_vs("pFilter");
                m_Process_task = Load_vs("m_Process_task");
                pCaseId_Rel = Load_vs("pCaseId_Rel");
                pMenu_id = Load_vs("pMenu_id");
                m_Filter_search = Load_ss("pFilter_search" + pType + pMenu_id); //add by gaoww 20140528
               
                //pFilter_custom = Load_vs("pFilter_custom");
                //m_Query_type = Functions.atoi(Load_vs("Query_type"));
            }

            myCase = new service_set_info(pType);
            strTitle = myString.Format("{0}-资料管理", myCase.CaseName);
            pTableName = myCase.TableName;

            SetPageTitle(strTitle);
            //m_strFilter_base = strFilter;

            if (IsPostBack == false)//服务管理定制，取出当前工单支持任务分配环节id，支持多个环节
            {
                my_odbc pTable = new my_odbc(pmSys.conn_crm);
                String strSql="SELECT PROCESS_ID FROM CRM_CASE_PROCESS WHERE CASETYPE='"+pType +"' AND SM_SUPPORT=1";
                int rc1 = pTable.my_odbc_find(strSql);
                while (rc1 > 0)
                {
                    String fld_value = "";
                    fld_value = pTable.my_odbc_result("PROCESS_ID");
                   rc1= pTable.my_odbc_nextrows(1);
                    if (m_Process_task.length() > 0) m_Process_task += ",";
                    m_Process_task += "'" + fld_value + "'";
                }
                pTable.my_odbc_disconnect();
                Save_vs("m_Process_task", m_Process_task);
            }

            InitToolbar();
            Fillin_SearchField();
            Fillin_grid();
            
           //高级查询
            InitToolbar_custom("plCommand_custom");    
            Build_frmSearchFld(myCase.DescName);        
          	myToolBar_custom.render(model);
          	myFld_Query_custom.render(model);
          	
            myToolBar.render(model);
            myFld_Query.render(model);
            mydg.render(model);
        }

        private void Fillin_grid()
        {
            String strKey_kvdb = "dtCase_list_" + pType;
            DataTable dtStru = pmInfo.myKvdb.Get(strKey_kvdb);// as DataTable;
            if (dtStru == null)
            {
                dtStru = fun_Form.get_desc_data(myCase.DescName, "((FLD_VLEVELS&2)<>0) ORDER BY LIST_INDEX");
                pmInfo.myKvdb.Setex(strKey_kvdb, dtStru, 60); //60秒
            }
            if (dtStru.getCount() > 0)
            {
                mydg.ID(Functions.atos(pType) + Functions.atos(pMenu_id));
                mydg.SetTable(pTableName);
                mydg.SetSelectStr("SELECT * FROM " + pTableName + " WHERE 1>1");
                mydg.SetPageSize(pmAgent.page_maxline-1);

                mydg.SetCaption("工单列表");
                mydg.SetConnStr(pmSys.conn_crm);

                mydg.fill_fld(0, "选择", "SELECT", 5, 9);  //add 2007.10.11

                pmRet mRet = fun_Form.Fill_Grid(dtStru, mydg, 2, 0);  //显示级别：bit 0在详细资料中显示 bit1在列表中显示，bit2在弹出中显示
                int rc = (int) mRet.nRet;
                mydg=(my_dataGrid) mRet.oRet;
                //if (rc>0)
                    //mydg.CellLinkClicked += new CellLinkClickedEventHandler(dataGrid_CellLinkClicked);
                int i = mydg.ColCount();
                //mydg.fill_fld(i++, "环节", "PROCESS", 0); //借用此字段，定位下面“任务信息”字段的位置
                mydg.fill_fld(i++, "任务信息", "", 7, 8);

                myCase.set_list_color(mydg);
                String strFilter = m_strFilter() + m_strOrder_by();
           
                mydg.RowDataFilled = this;// new RowDataFilledEventHandler(mydg_RowDataFilled);
                //mydg.CellLinkClicked=this;
                mydg.fill_header("dgvList", "CASEID", strFilter);
            }
        }

       
		public void mydg_CellLinkClicked(Object sender, String text, int rows, int cols) {
			   if (mydg.TotalRow() <= 0) return;
			   String strCaseid = mydg.get_cell(rows, "CASEID");
	            if (strCaseid == "") return;
	            int nType = Functions.atoi(mydg.get_cell(rows, "CASETYPE"));
	            int nStatus = Functions.atoi(mydg.get_cell(rows, "STATUS"));
	            
	            String strNewUrl = myString.Format("case_service_edit.aspx?cmd=Edit&caseid={0}&ntype={1}&status={2}", strCaseid, nType, nStatus);	      
	            Functions.Redirect(strNewUrl);
	           // this.Response.Redirect(strNewUrl);
		}

		@Override
		public void mydg_RowDataFilled(Object sender, int rows) {
            if (rows < 0) return; //表头行，不处理
            String strCaseid = mydg.get_cell(rows, "CASEID");
            if (strCaseid.equals("")) return;
            int nType = Functions.atoi(mydg.get_cell(rows, "CASETYPE"));
            int nStatus = Functions.atoi(mydg.get_cell(rows, "STATUS"));
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
                String strNewUrl = myString.Format("case_service_edit.aspx?cmd=Edit&caseid={0}&ntype={1}&status={2}", strCaseid, nType, nStatus);            
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
           
            //int nCnt = mydg.ColCount() ;
           String strFld_Desp_last = mydg.get_item_name(mydg.ColCount() - 2);
            int cols = mydg.get_idx(strFld_Desp_last);
            if (cols >= 0)
            {
                String strNewUrl = myString.Format("/ut_service/case_task_list.aspx?cmd=Edit&caseid={0}&ntype={1}", strCaseid, nType);
                String strHtml = myString.Format("<a href=\"javascript:Add_MainTab('任务单-{0}','{1}');\">{2}</a>", strCaseid, strNewUrl,fun_main.CreateHtml_img("Tasks.gif", ""));
                mydg.set_cell(rows,cols+1,strHtml);
            }
        }

        private HashMap<String,String> m_Process = new HashMap();  //保存快速查询结构    
        private void InitToolbar()
        {
        	//myToolBar.Clear();
        	LinkedHashMap<String, String> m_dyProcess = new LinkedHashMap<String, String>();
       	    m_dyProcess.put("All", "全部");
            m_Process.put("All", "全部");
            DataTable dtTemp;
            
            if (myCase.nWF_Enable == 1)
                dtTemp = Functions.dt_GetTable("SELECT PROCESS_ID AS PROCESS_ID,PROCESS_NAME AS PROCESS_NAME FROM CRM_CASE_PROCESS WHERE CASETYPE='" + pType + "'  ORDER BY WF_INDEX ASC", "", pmSys.conn_crm);
            else
                dtTemp = Functions.dt_GetTable("SELECT STATUS_ID AS PROCESS_ID,STATUS_NAME AS PROCESS_NAME FROM CRM_CASE_STATUS WHERE CASETYPE='" + pType + "'", "", pmSys.conn_crm);
            String strKey, strValue;
            for (DataRow myRow : dtTemp.Rows())
            {
                strKey = Functions.drCols_strValue(myRow, "PROCESS_ID");
                strValue = Functions.drCols_strValue(myRow, "PROCESS_NAME");
                if (Regex.IsMatch(strValue, "[\\d]+-") == false)  //STATUS_NAME 存成 1-已完成 ->  已完成
                {
                    strValue = strKey + "-" + strValue;
                }
                m_dyProcess.put(strKey, strValue);
                m_Process.put(strKey, strValue);
            }
            if (pType > 0 && pType < 20) //add by gaoww 20150909 服务管理定制,暂定1-20为服务单，服务单列表增加工单类型切换
            {
                myToolBar.fill_fld("选择工单类型", "Select_CaseType", 25, 4, "请选择服务分类");
                myToolBar.set_list("Select_CaseType", "SELECT * FROM CRM_CASE_TABLE WHERE CASETYPE>0 AND CASETYPE<20 ORDER BY CASETYPE", "CASETYPE,CASE_NAME", pmSys.conn_crm);
                myToolBar.set_item_attr("Select_CaseType", Fld_attr.Fld_index,Integer.toString(pType-1) );
         
                //临时封上
                /*int nCnt = myToolBar.get_item_cbo("Select_CaseType").Items.size();
                for (int index = 0; index < nCnt; index++)
                {
                    String strText = myToolBar.get_item_cbo("Select_CaseType").Items[index].Text;
                    if (strText.startsWith(pType + "(") == true)
                    {
                        myToolBar.get_item_cbo("Select_CaseType").SelectedIndex = index;
                        break;
                    }
                }*/
            }

            if (pOp.equals("assign")==false&&pOp.equals("assign_sec")==false)
            {
                myToolBar.fill_fld("请选择", "SelectProcess", 20, 4, "请选择");
                myToolBar.set_list("SelectProcess", m_dyProcess);
            }
            //myToorBar.get_item_cbo("SelectProcess").AutoPostBack = false;
            myToolBar.fill_fld("刷新", "Query_all");

            myToolBar.fill_fld("Separator", "Separator0", 0, 3);
            myToolBar.fill_fld("增加", "AddNew");
            myToolBar.fill_fld_confirm("删除", "Delete", " 确实要删除所选工单资料吗？");

            myToolBar.fill_fld("Separator", "Separator1", 0, 3);
            //myToorBar.fill_fld(fun_main.Term("LBL_Search"), "Query", Build_frmSearchFld(myCase.CaseName));
            myToolBar.fill_fld("查询", "Query", 0, 10); //modify by gaoww 20111230
            //myToorBar.fill_fld("高级查询", "Query_Custom", Build_frmSearchFld(myCase.CaseName)); //modify by gaoww 20111230
            myToolBar.fill_fld("Separator", "Separator2", 0, 3);
            myToolBar.fill_fld(fun_main.Term("LBL_TOEXCEL"), "Output");
            //myToorBar.fill_fld("相关客户信息", "ReleCustomer");    //2013-03-01 peng 意义不明确,应加在每条记录链接

            myToolBar.fill_toolStrip("plCommand");
            myToolBar.btnItemClick = this;// new btnClickEventHandler(myToolBar_btnItemClick);

            /*
            //不是Cust管理员
            if (pmAgent.c_Levels.check_authority(e_Level_cust.case_admin) == false)
            {
                //if (pmAgent.c_Levels.check_authority(e_Level_cust..case_add) == false)
                //    myToorBar.set_readonly("AddNew", true);
                if (pmAgent.c_Levels.check_authority(e_Level_cust.case_del) == false)
                {
                    myToorBar.set_readonly("Delete", true);
                }
                if (pmAgent.c_Levels.check_authority(e_Level_cust.case_modi) == false)
                    myToorBar.set_readonly("Modify", true);
                if (pmAgent.c_Levels.check_authority(e_Level_cust.case_output) == false)
                {
                    myToorBar.set_readonly("Output", true);
                }
            }
           */

            myToolBar.set_readonly("AddNew", true);
            myToolBar.set_readonly("Delete", true);
            //myToorBar.set_readonly("Modify", true);
            myToolBar.set_readonly("Output", true);

            //约定第一环节 为0
            int nLevel = myCase.get_authority(0, 0);
            if ((nLevel & 1) > 0) myToolBar.set_readonly("AddNew", false);

            //遍历所有环节权限，如果都有则删除或输出权限则选择全部时也可用
            int nLevel_del = 1, nLevel_output = 1;
            m_dyProcess.remove("All");
            for (String strKeySet: m_dyProcess.keySet())
            {
                int nProcess = Functions.atoi(Functions.Substring(m_dyProcess.get(strKeySet), "", "-"));
                int nResult = myCase.get_authority(nProcess, nProcess);
                if ((nResult & 4) <= 0)
                    nLevel_del = -1;
                if ((nResult & 16) <= 0)
                    nLevel_output = -1;
            }
            if (nLevel_del > 0)
                myToolBar.set_readonly("Delete", false);
            if (nLevel_output > 0)
                myToolBar.set_readonly("Output", false);
        }

        public void myToolBar_btnItemClick(Object sender, String name, String parms, int nparms)
        {
            String strUserid;
            if (name.equals("AddNew"))
            {
                Functions.Redirect(myString.Format("case_service_edit.aspx?cmd=AddNew&ntype={0}&caseid_rel={1}", pType, pCaseId_Rel)); //modify by gaoww 20151202 增加caseid_rel
            }
            else if (name.equals("Delete"))
            {
                List<String> alRet = mydg.GetSelectedKey("CASEID");
                if (alRet.size() == 0)
                {
                    Functions.MsgBox("请先选中需要删除的工单资料记录！");
                    return;
                }
                else
                {
                    int nFind = 0;
                    for (String strKey : alRet)
                    {
                        if (myCase.DelCaseRecord(strKey) == 1)
                        {
                            nFind++;
                        }
                    }
                    if (nFind >= 0) 
                    	Functions.MsgBox("提示", "已删除" + nFind + "条工单资料的记录！");
                    mydg.refresh("CASEID", m_strFilter() + m_strOrder_by());
                }
            }
            else if (name.equals("Output"))
            {
                //add by gaoww 20140804 在输出时增加判断，超过1w条不允许输出（王超建议），输出过多记录会导致crm卡死
                my_odbc pTemp = new my_odbc(pmSys.conn_crm);
                String strSql = "SELECT COUNT(*) AS CNT FROM " + myCase.TableName;
                if (m_strFilter().length()>0)
                    strSql += " WHERE " + m_strFilter();
                int rc = pTemp.my_odbc_find(strSql);
                int nCount = 0;
                if (rc > 0)
                    nCount = pTemp.my_odbc_result("CNT",0);
                pTemp.my_odbc_disconnect();
                if (nCount > 10000)
                {
                    Functions.MsgBox("输出的工单记录超过1万条，请查询后再输出！");
                    return;
                }

                mydg.Output_toExcelFile("工单资料列表", pmSys.CaptionExport + "_工单资料", 0);
            }            
            else if (name.equals("Query_all"))
            {
                m_Filter_search = "";
                Save_ss("pFilter_search" + pType + pMenu_id, m_Filter_search);  //存入Session 变量，用于从编辑页面返回时恢复显示

                mydg.setLastVisitedPage(1);
                Functions.Redirect(myString.Format("case_service_list.aspx?ntype={0}&filter={1}&menu_id={2}&cmd={3}", pType, pFilter,pMenu_id,pOp )); //modify by gaoww 20150819 解决从主页我的任务点击“更多”按钮进入后，再点刷新显示出所有人的工单问题

            }
            else if ((name.equals("Select_CaseType")))
            {
                String strTemp = Functions.Substring(parms, null, "(");
                nparms = Functions.atoi(strTemp);
                String strQueryString = Request.getQueryString();
                if (strQueryString.contains("ntype=" + pType))
                    strQueryString = strQueryString.replace("ntype=" + pType, "ntype=" + nparms);
                Functions.Redirect("case_service_list.aspx?" + strQueryString);
            }
            else if (name.equals("SelectProcess"))
            {
                myToolBar.set_readonly("AddNew", true);
                myToolBar.set_readonly("Delete", true);
                myToolBar.set_readonly("Output", true);

                int nLevel = 0;
                String strSelect = myToolBar.get_item_value(name);
                if(m_Process.containsKey(strSelect)) strSelect=m_Process.get(strSelect);
                if (strSelect.isEmpty())
                {
                    nLevel = myCase.get_authority(0, 0);
                    if ((nLevel & 1) > 0) myToolBar.set_readonly("AddNew", false);
                    //遍历所有环节权限，如果都有则删除或输出权限则选择全部时也可用
                    int nLevel_del = 1, nLevel_output = 1;
                    for(String strValue:m_Process.keySet())
                    {                        
                        if (strValue.equals("All")) continue;
                        int nProcess = Functions.atoi(Functions.Substring(strValue, "", "-"));
                        int nResult = myCase.get_authority(nProcess, nProcess);
                        if ((nResult & 4) <= 0)
                            nLevel_del = -1;
                        if ((nResult & 16) <= 0)
                            nLevel_output = -1;
                    }
                    if (nLevel_del > 0)
                        myToolBar.set_readonly("Delete", false);
                    if (nLevel_output > 0)
                        myToolBar.set_readonly("Output", false);
                }
                else
                {
                    int nProcess = Functions.atoi(Functions.Substring(strSelect, "", "-"));
                    nLevel = myCase.get_authority(nProcess, nProcess);
                    if ((nLevel & 1) > 0) myToolBar.set_readonly("AddNew", false);
                    if ((nLevel & 4) > 0) myToolBar.set_readonly("Delete", false);
                    if ((nLevel & 16) > 0) myToolBar.set_readonly("Output", false);
                }
                String strFilter = m_strFilter() + m_strOrder_by();
                mydg.refresh("CASEID", strFilter);
            }
            //高级查询
            else if ((name.equals("Query_Save")))
            {
                String strRet;
                if (myFld_Query_custom.get_item_text("SQL_FILTER").length()<1)
                {
                    strRet =fun_Form.CreateSql(pFld,myFld_Query_custom);
                }
                else
                {
                    strRet = myFld_Query_custom.get_item_text("SQL_FILTER");
                }
                //修改进入“搜索”，点击“高级查询”，再“确定”，查询结果有问题。2008.08
                if (strRet.equals("-1")) return;              
                if (strRet.isEmpty())
                {
                    Functions.MsgBox("请选择查询条件!");
                    return;
                }              
                
                m_Filter_search = strRet;
                Save_ss("pFilter_search" + pType+pMenu_id, m_Filter_search);  //存入Session 变量，用于从编辑页面返回时恢复显示             
       
                String strFilter = m_strFilter();
                mydg.refresh("CASEID", strFilter + m_strOrder_by());
            }
            else if ((name.equals("Query_CreateSql")))
            {
   	       	  myFld_Query_custom.setReload(true);
   	       	  myFld_Query_custom.set_item_text("SQL_FILTER", fun_Form.CreateSql(pFld,myFld_Query_custom));
   	          myFld_Query_custom.set_item_attr("SQL_FILTER", "Fld_visible", "true");
            }
            else if ((name.equals("Query_ResetSql")))
            {
   	       	  myFld_Query_custom.setReload(true);
   	       	  myFld_Query_custom.set_item_text("SQL_FILTER", "");
   	       	  myFld_Query_custom.set_item_attr("SQL_FILTER", "Fld_visible", "false");
            }
        }

        private HashMap<String,String> m_htQuickSearch_fld = new HashMap();  //保存快速查询结构
        private void Fillin_SearchField()
        {
            String strPrompt = "支持工单名称、客户名称";
            m_htQuickSearch_fld.put("CASENAME", "工单名称");
            m_htQuickSearch_fld.put("UNAME", "客户名称");
            DataTable dtStru = fun_Form.get_desc_data(myCase.DescName, "((FLD_VLEVELS&16)<>0) ORDER BY EDIT_INDEX");
            if (dtStru != null)
            {
                String strValue, strName;
                for (int i = 0; i < dtStru.getCount(); i++)
                {
                    strValue = Functions.dtCols_strValue(dtStru, i, "FLD_VALUE");
                    strName = Functions.dtCols_strValue(dtStru, i, "FLD_NAME");
                    if (strValue.equals("CASENAME")) continue;
                    if (strValue.equals("UNAME")) continue;
                    if (strName.startsWith("\\"))
                        strName = Functions.Substring(strName, "\\", "");
                    if (strName.startsWith("*"))
                        strName = Functions.Substring(strName, "*", "");
                    Functions.ht_SaveEx(strValue, strName,m_htQuickSearch_fld);
                    strPrompt += "、" + strName;
                }
            }
            strPrompt += "的模糊查询";

            myFld_Query.SetWidth(pmAgent.content_width-200);
            myFld_Query.SetMaxLabelLenth( 80);
            myFld_Query.SetMaxLabelLenth_col2(60);
            myFld_Query.funName_OnClientClick("mySearch_FieldLinkClicked");


            myFld_Query.fill_fld("开始日期", "SDATE", 21, 5, true, true, DateTime.Now().AddYears(-3).ToString("yyyy-MM-dd"), "yyyy-MM-dd");
            myFld_Query.fill_fld("结束日期", "EDATE", 21, 5, true, false, "", "yyyy-MM-dd");

            myFld_Query.fill_fld("快速查询", "UNAME", 50, 0, true, true);
            myFld_Query.set_MaxLenth("UNAME", 100);
            myFld_Query.set_tooltip("UNAME", strPrompt);//"支持工单名称、客户名称、拼音码、地址、电话的模糊查询");

            ArrayList<String> alButton_ex = new ArrayList();
            alButton_ex.add("DESP=高级查询;NAME=CustomSearch;");
            //alButton_ex.Add("DESP=清除;NAME=Reset;");
            //myFld_Query.fill_fld_button("查询", "Search", null, false, alButton_ex, "right-65px");
            myFld_Query.fill_fld_button("查询", "QuickSearch", null, false, alButton_ex, "left");
          
            myFld_Query.fill_Panel("plSeach");
            myFld_Query.FieldLinkClicked = this;// new SearchFieldLinkClickedEventHandler(mySearch_FieldLinkClicked);

            //ctlSearch_custom.Init_CustomSeach("Case", pType);
        }

        public void mySearch_FieldLinkClicked(Object sender, String name, String parms, int nparms, int ntype)
        {
            if (name.equals("QuickSearch"))
            {
                //pFilter = Functions.atos(Request.QueryString["filter"]);
                String strFilter_search = GetFilter_search();
                if (strFilter_search.length() < 1)
                {
                    Functions.MsgBox("请输入查询条件！");
                    return;
                }
                m_Filter_search = strFilter_search;
                Save_ss("pFilter_search" + pType + pMenu_id, m_Filter_search);  //存入Session 变量，用于从编辑页面返回时恢复显示

                String strFilter = m_strFilter();
                mydg.refresh("CASEID", strFilter + m_strOrder_by());
                //mydg.Goto_NewPageIndex(1);
            }
            else if (name.equals("CustomSearch"))
            {
                //JS 处理
            }
        }

        private String GetFilter_search()
        {
            String strSdate = myFld_Query.get_item_value("SDATE").replace("-", "");
            String strEdate = myFld_Query.get_item_value("EDATE").replace("-", "");
            String strInput = myFld_Query.get_item_value("UNAME");
            String strFilter_search = "";
            String strFilter_date = "";
           
            if (strSdate.length() > 0)
            {
                strFilter_date = " SDATE>= '" + strSdate + "'";
            }
            if (strEdate.length() > 0)
            {
                if (strFilter_date.length() > 0) strFilter_date += " AND ";
                strFilter_date += " SDATE<= '" + strEdate + "'";
            }
            if (strInput.length() > 0)
            {                
            	for(String strKey:m_htQuickSearch_fld.keySet())           
                {
                    if (strFilter_search.length() > 0) strFilter_search += " OR ";
                    strFilter_search += myString.Format("({0} LIKE '%{1}%')", strKey, strInput);
                }
            }

            String strFilter = strFilter_date;
            if (strFilter_search.length() > 0) strFilter = myString.Format("({0}) AND ({1})", strFilter, strFilter_search);
            return strFilter;
        }

        private String m_strFilter()
        {
            String strFilter = m_strFilter_base();
            if (m_strFilter_priv().length() > 0)
            {
                if (strFilter.length() > 0) strFilter += " AND ";
                strFilter += m_strFilter_priv();
            }

            //add by gaoww 20160513 高级查询增加可以输入order by排序功能，所以需要将查询条件放在所有查询语句最后
            if (m_Filter_search.length() > 0)
            {
                if (strFilter.length() > 0) strFilter += " AND ";
                strFilter += m_Filter_search;
            }
            //strFilter += m_strOrder_by;
            return strFilter;
          
        }
        //基本的显示条件
        private String m_strFilter_base()
        {
            String strFilter = "";
            if (pFilter.length() > 0)
            {
                strFilter = pFilter;
            }
            if (pCaseId_Rel.equals("")==false) //add by gaoww 20151202
            {
                if (strFilter.length() > 0) strFilter += " AND ";
                strFilter += " CASEID_REL='" + pCaseId_Rel + "'";
            }

            if (strFilter.length() > 0) strFilter += " AND ";
            strFilter += " CASETYPE='" + pType + "'";
            if (pOp.equals("abnormal"))//异常服务单
            {
            }
            else if (pOp.equals("assign")) //待调度的服务单
            {
                strFilter += " AND (TASK_LEVEL='0' AND PROCESS IN(" + m_Process_task + "))";
            }
            else if (pOp.equals("assign_sec")) //任务进行中的服务单
            {
                strFilter += " AND (TASK_LEVEL<>'0' AND PROCESS IN(" + m_Process_task + "))";
            }
            else if (pOp.equals("unaccept")) //待签收 //add by gaoww 20151112
            {
              strFilter += " AND (PROCESS_STATUS='0')";
            }
            else if (pOp.equals("backward")) //被退回 //add by gaoww 20151112
            {
                strFilter += " AND (PROCESS_STATUS='4')";
            }
            return strFilter;
        }

        //给予角色的特权 privileged
        private String m_strFilter_priv()
        {
            String strFilter = "";
            String strSelect = myToolBar.get_item_value("SelectProcess");
            if(m_Process.containsKey(strSelect)) 
            	strSelect=m_Process.get(strSelect);
         else strSelect="全部";
            
            int nProcess = -1;
            if (strSelect.equals("全部")==false)
            {
                nProcess = Functions.atoi(Functions.Substring(strSelect, "", "-"));
            }
           if (myCase.nWF_Enable == 1)
                strFilter = myCase.get_list_filter(nProcess, 0);
            else
                strFilter = myCase.get_list_filter(0, nProcess);

            if (pFilter.length() > 0)
            {
                if (strFilter.length() > 0)
                    strFilter = myString.Format("({0}) AND {1}", strFilter, pFilter);
                else
                    strFilter = pFilter;
            }

            //modify by gaoww 20151112
            String strCode = pmAgent.c_Info.agent_org_code;
            if (pmAgent.c_Info.agent_org_level == 1)
                strCode = Functions.Substring(pmAgent.c_Info.agent_org_code, 0, 6);
            else  if (pmAgent.c_Info.agent_org_level == 2)
                strCode = Functions.Substring(pmAgent.c_Info.agent_org_code, 0, 8);
            String strFilter_view = "";
            if ((pOp.equals("assign")) || (pOp.equals("assign_sec"))) //modify by gaoww 20151112 如果是待分配或执行中的服务单，只显示当前服务机构时本单位或下属单位的
            {
                strFilter_view = myString.Format(" (ORG_CODE LIKE '{0}%' OR ORG_CODE_TASK LIKE '{0}%' OR CASEID IN(SELECT CASEID FROM SM_TASK_DISP WHERE ORG_CODE LIKE '{0}%' OR ENGR_ORG_CODE LIKE  '{0}%'))", strCode);                       
            }
            else if ((pOp.equals("unaccept")))  //add by gaoww 20151113 增加待签收显示当前工号是自己，或者当前座席可处理环节并且currentghid=''的
            {
                strFilter_view = " (CURRENTGHID='" + pmAgent.uid + "') ";
                if (myCase.nWF_Enable == 1) //检查哪个环节在未签收状态下有使用工作流的权限
                {
                    DataTable dtTemp = Functions.dt_GetTable("SELECT PROCESS_ID AS PROCESS_ID FROM CRM_CASE_PROCESS WHERE CASETYPE='" + pType + "'  ORDER BY WF_INDEX ASC", "", pmSys.conn_crm);
                    String strFilter_process = "";
                    for (int rows = 0; rows < dtTemp.getCount(); rows++)
                    {
                        int fld_int = Functions.dtCols_nValue(dtTemp, rows, "PROCESS_ID");
                        int nLevel = myCase.get_authority(fld_int, -1);
                        if ((nLevel & 32) > 0)
                        {
                            if (strFilter_process.equals("")==false) strFilter_process += ",";
                            strFilter_process += "'" + fld_int + "'";
                        }
                    }
                    if (strFilter_process.equals("")==false)
                        strFilter_view = "(" + strFilter_view + " OR (PROCESS IN(" + strFilter_process + ") AND CURRENTGHID=''))";
                }
            }
            else if ((pOp.equals("backward")))  //add by gaoww 20151112 增加被退回菜单处理
            {
                strFilter_view = "CURRENTGHID='" + pmAgent.uid + "'";
            }
            else
            {
                if (pmAgent.c_Levels.check_authority(e_Level_service.service_view_admin) == false)//如果没有服务系统资料浏览最高权限
                {
                    //服务系统资料浏览增强权限
                    if (pmAgent.c_Levels.check_authority(e_Level_service.service_view_adv) == true)//显示本建单人所属机构代码以及下级属机构代码的服务单
                    {
                        strFilter_view = myString.Format(" (ORG_CODE LIKE '{0}%' OR ORG_CODE_TASK LIKE '{0}%' OR CASEID IN(SELECT CASEID FROM SM_TASK_DISP WHERE ORG_CODE LIKE '{0}%' OR ENGR_ORG_CODE LIKE  '{0}%'))", strCode);
                    }
                    else//两个权限都没有的时候
                    {
                        strFilter_view = myString.Format(" (ORG_CODE = '{0}' OR ORG_CODE_TASK = '{0}' OR CASEID IN(SELECT CASEID FROM SM_TASK_DISP WHERE ORG_CODE = '{0}' OR ENGR_ORG_CODE = '{0}'))", pmAgent.c_Info.agent_org_code);
                    }
                }
                else //add by gaoww 20151112
                    strFilter_view = "1=1";
            }

            if (strFilter.length() > 0)
                strFilter += " AND (" + strFilter_view + ")";
            else
                strFilter = strFilter_view;
            return strFilter;
        }

        //排序规则
        private String m_strOrder_by()
        {
                String strOrderby = " ORDER BY CASEID DESC";
                if (m_Filter_search.toLowerCase().contains("order by") == true) //add by gaoww 20160513 如果高级查询返回条件中有order by排序，则不使用默认排序规则
                    strOrderby = "";
                return strOrderby;
        }
        
        //高级查询
        private void InitToolbar_custom(String plCommand)
        { 
        	 myToolBar_custom.funName_OnClientClick("btnToolBarClick");
             myToolBar_custom.fill_fld("开始查询", "Query_Save",0,10);
        	 myToolBar_custom.fill_fld("Separator", "Separator2", 0, 3);
        	 myToolBar_custom.fill_fld("自定义查询", "Query_CreateSql",0,10);
        	 myToolBar_custom.fill_fld("取消自定义查询", "Query_ResetSql",0,10);
        	 myToolBar_custom.fill_fld("Separator", "Separator4", 0, 3);
             //myToolBar.fill_fld("关闭", "Close", 0, 10);
        	 myToolBar_custom.fill_fld("切换到快速查询", "ReturnQuickSearch", 0, 10);

        	 myToolBar_custom.fill_toolStrip(plCommand);
        	 myToolBar_custom.btnItemClick = this;// new btnClickEventHandler(myToolBar_btnItemClick);
      
        }
        
        //根据表单设计-弹出，显示查询项
       private void Build_frmSearchFld(String strDescName)
        {
            //String strTitle = "查询联系人资料...";
            //String[,] pFld;
    	    DataTable dtStru= pmInfo.myKvdb.Get("dtCase_list_custom" + pType);
            if (dtStru == null)
            {
                dtStru = fun_Form.get_desc_data(strDescName, "((FLD_VLEVELS&8)<>0) ORDER BY EDIT_INDEX");
                pmInfo.myKvdb.Setex("dtCase_list_custom" + pType, dtStru, 60); //60秒
            }
            if (dtStru.getCount() > 0)
            {    
                pFld = new String[dtStru.getCount()][7];
                for (int i = 0; i < dtStru.getCount(); i++)
                {
                    pFld[i][0] = Functions.dtCols_strValue(dtStru, i, "FLD_NAME");
                    pFld[i][1] = Functions.dtCols_strValue(dtStru, i, "FLD_VALUE");
                    pFld[i][2] = Functions.dtCols_strValue(dtStru, i, "FLD_TYPE");
                    int ntemp = Functions.dtCols_nValue(dtStru, i, "EDIT_TYPE");
                    pFld[i][3] =String.valueOf(ntemp & 0xff);
                    pFld[i][4] = Functions.dtCols_strValue(dtStru, i, "FLD_CBO_LIST");
                    if (pFld[i][0].startsWith("\\") == true)
                    {
                        pFld[i][0] = pFld[i][0].replace("\\", "");
                    }
                    pFld[i][5] = Functions.dtCols_strValue(dtStru, i, "EDIT_RELATION");   //add by gaoww 20090212 增加列表关联的内容获取
                    pFld[i][6] = Functions.dtCols_strValue(dtStru, i, "FLD_FORMAT");      //add by gaoww 20130520 增加格式传入，combox的类型可以根据编辑页面一样可编辑或只能list选择
                }
            }
            Fillin_SearchField_custom("plEdit");         
        }
        
      	private void Fillin_SearchField_custom(String plSeach)
        { 		
            if ((pFld == null) || (pFld.length == 0)) //没有记录，显示高级查询
                return;

            myFld_Query_custom.SetMaxLabelLenth(120);
            myFld_Query_custom.SetMaxLabelLenth_col2(10);
            myFld_Query_custom.SetLabelAlign("right");

            pmRet mRet = fun_Form.Fill_Field_Search(pFld, myFld_Query_custom);
            
            int rc = (int) mRet.nRet;
            myFld_Query_custom=(my_Field) mRet.oRet;          
            myFld_Query_custom.fill_fld("hidn_nQuery", "hidn_nQuery",0);  //控制是否显示查询div，0-不显示，1-显示
            myFld_Query_custom.fill_fld("hidn_nSearch", "hidn_nSearch",0); //控制显示的查询方式：0-快速查询，1-高级查询
            myFld_Query_custom.fill_Panel(plSeach);
            
            myFld_Query_custom.set_item_value("hidn_nSearch", "0");
            myFld_Query_custom.set_item_value("hidn_nQuery", "0");
          
            String strRet = myFld_Query_custom.get_item_text("SQL_FILTER");
            if (strRet.length() < 1) //2014-05-20 peng 默认情况，不显示SQL语句项 
            {	
            	myFld_Query_custom.set_item_attr("SQL_FILTER", "Fld_visible", "false");
            }
        }
    }

