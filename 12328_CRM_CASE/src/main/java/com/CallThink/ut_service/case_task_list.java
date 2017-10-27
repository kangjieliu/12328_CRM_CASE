///########################################################################################
/// Copyright (C) 2000, ToneThink.Soft  All Rights Reserved. 
///#########################################################################################
/// 文件创建时间：2015-09-07
///   文件创建人：gaoww
/// 文件功能描述：任务工单列表页面
///     调用格式：
///     
///     维护记录：
/// 2015.09.07：create by gaoww 
/// 2015.09.16：modify by lics 多任务就显示任务单列表单任务就直接进入任务单编辑页面
///#########################################################################################
package com.CallThink.ut_service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import com.CallThink.base.pmClass.e_Level_base;
import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.pmSys;
import com.CallThink.base.pmClass.pmAgent_info;
import com.CallThink.base.pmCode.UltraCRM_Page;
import com.CallThink.base.support.pmInfo;
import com.CallThink.ut_service.pmModel_service.e_Level_service;
import com.CallThink.ut_service.pmModel_service.service_info;
import com.CallThink.ut_service.pmModel_service.service_set_info;
import com.ToneThink.DataTable.DataTable;
import com.ToneThink.DataTable.DataRow;
import com.ToneThink.DateTime.DateTime;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.pmList;
import com.ToneThink.ctsTools.myUtility.pmMap;
import com.ToneThink.ctsTools.Regex.Regex;
import com.ToneThink.ctsTools.Regex.Regex.RegexOptions;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.myString;
import com.ToneThink.ctsTools.WebUI.my_ToolStrip;
import com.ToneThink.ctsTools.WebUI.my_dataGrid;
import com.ToneThink.ctsTools.WebUI.my_SearchField;

    public class case_task_list extends UltraCRM_Page
    {
        private String pTableName = "SM_TASK_DISP";
        private String m_TableName_Skill = "SM_BUSS_GHID_SKILL"; //工程师技能表
        private String m_TableName_Org_Code = "DICT_ORG_CODE";//服务商组织机构
        private int pType = 21;
        private String pOp = "list";
        private String pFilter = "";
        private String pCaseid = "";
        private String pMark = "";//从导航菜单直接进入任务管理页面
        private String m_Filter_search = "";
        private int m_Support_Task = 0; //是否支持任务

        my_SearchField myFld_Query = new my_SearchField(3);
        my_dataGrid mydg = new my_dataGrid(51);
        my_ToolStrip myToolBar = new my_ToolStrip();

        public void Page_Load(Object sender, Model model)
        {
            int nHistory = 0;
            String strTitle = "任务单列表";
            if (IsPostBack == false)//正被首次加载和访问
            {
                HashMap htQuery;
                pmMap res = fun_main.QuerySplit(Request); htQuery = res.htRet;
                int rc = res.nRet;
                if (rc > 0)
                {
                    pOp = Functions.ht_Get_strValue("cmd", htQuery);
                    pType = Functions.atoi(Functions.ht_Get_strValue("ntype", htQuery));
                    pFilter = Functions.ht_Get_strValue("filter", htQuery);
                    pCaseid = Functions.ht_Get_strValue("caseid", htQuery);
                    nHistory = Functions.atoi(Functions.ht_Get_strValue("history", htQuery));
                    pMark = Functions.ht_Get_strValue("Mark", htQuery);
                    if (nHistory > 0)  //编辑页面返回
                    {
                        String strFilter_temp = Load_ss("pFilter_search" + pType);
                        if (strFilter_temp.length() > 0) m_Filter_search = strFilter_temp;
                        mydg.nRestore_history = 1;
                    }                    
                }
                Save_vs("pType", pType);
                Save_vs("pOp", pOp);
                Save_vs("pFilter", pFilter);
                Save_vs("pCaseid", pCaseid);
                Save_ss("pFilter_search" + pType, m_Filter_search);
                Save_vs("pMark", pMark);

                service_set_info myService = new service_set_info(pType);
                service_info myCaseInfo = myService.GetCaseRecord(pCaseid);
                m_Support_Task = myService.is_sm_support(myCaseInfo.nProcess);
                Save_vs("m_Support_Task", m_Support_Task);
            }
            else
            {
                pOp = Load_vs("pOp");
                pType = Functions.atoi(Load_vs("pType"));
                pFilter = Load_vs("pFilter");
                pCaseid = Load_vs("pCaseid");
                m_Filter_search = Load_ss("pFilter_search" + pType);
                pMark = Load_vs("pMark");
                m_Support_Task = Functions.atoi(Load_vs("m_Support_Task"));
            }
            //m_strFilter_base = strFilter;

            InitToolbar();
            Fillin_SearchField();            
            Fillin_grid();
            //增加判断，如果只有一个任务单则直接进入编辑页面，否则在列表中刷新 
            if (pOp.equals("Edit"))
            {
                //if ((dgvCase.DataSource as DataTable).getCount()==1)
               if (mydg.RowCount()==1)
                {
                    String strTaskid = mydg.get_cell(0, "TASKID");
                    String strNewUrl = "case_task_edit.aspx?" + Request.getQueryString() + "&From=list&taskid=" + strTaskid;
                   
                    //modify by gaoww 20151120 增加判断，如果任务还未指派到工程师，则打开任务分派页面
                    String strOrg_Code_eng =  mydg.get_cell(0, "ENGR_ORG_CODE"); //工程师所属机构
                    String strGhid_engr = mydg.get_cell(0, "GHID_ENGR");  //工程师
                    if (strGhid_engr.equals(""))//没有分派工程师的，并且当前任务所属机构是自己部门或下属部门的，进入任务分派页面
                    {
                        //if (strOrg_Code_eng.startsWith(pmAgent.c_Info.agent_org_code.TrimEnd('0')) == true)
                       if (strOrg_Code_eng.startsWith(myString.TrimEnd(pmAgent.c_Info.agent_org_code,'0')) == true)
                        {
                            strNewUrl = myString.Format("case_task_assign.aspx?cmd=Edit&caseid={0}&taskid={1}&ntype={2}", pCaseid, strTaskid, pType);
                        }
                    }
                    Functions.Redirect(strNewUrl);
                }
                //else if ((dgvCase.DataSource as DataTable).getCount() == 0)
                else if (mydg.RowCount() == 0)
                {
                    if (pmAgent.c_Levels.check_authority(e_Level_service.service_dds) == true) //如果有调度权限则调到任务分派页面
                    {
                        if (m_Support_Task == 1)  //服务管理定制，如果当前环节是可以任务分配的，才可以进入新建任务界面
                            Functions.Redirect("case_task_assign.aspx?cmd=popup&caseid=" + pCaseid + "&ntype=" + pType);
                    }
                }
            }
        
         myToolBar.render(model);
         myFld_Query.render(model);
         mydg.render(model);
}

        private void Fillin_grid()
        {
            mydg.ID(Functions.atos(pType)+pMark);
            mydg.SetTable(pTableName);
            mydg.SetSelectStr("SELECT * FROM " + pTableName + " WHERE 1>1");
            mydg.SetPagerMode(2);
            mydg.SetPageSize(pmAgent.page_maxline - 1);

            mydg.SetCaption("任务单列表");
            mydg.SetConnStr(pmSys.conn_crm);
            int i = 0;
            mydg.fill_fld(i++, "选择", "SELECT", 5, 9);
            mydg.fill_fld(i++, "任务单编号", "TASKID", 0);
            mydg.fill_fld(i++, "任务类型", "TASKTYPE", 0);
            mydg.fill_fld(i++, "服务单名称", "CASEID", 20,1);
            mydg.set_cols_cbo_list("CASEID", "SELECT CASEID,CASENAME FROM CRM_CASE1", "CASEID,CASENAME", pmSys.conn_crm);
            mydg.fill_fld(i++, "客户名称", "UNAME", 15);
            mydg.fill_fld(i++, "工单类型", "CASETYPE", 0, 1);
            mydg.set_cols_cbo_list("CASETYPE", "SELECT * FROM CRM_CASE_TABLE WHERE CASETYPE>0 AND CASETYPE<20 ORDER BY CASETYPE", "CASETYPE,CASE_NAME", pmSys.conn_crm);
            mydg.fill_fld(i++, "任务级别", "TASK_LEVEL", 8, 1);
            mydg.set_cols_cbo_list("TASK_LEVEL", "主任务,从任务");
            mydg.fill_fld(i++, "环节", "PROCESS", 6, 1);
            mydg.set_cols_cbo_list("PROCESS", "SELECT PROCESS_ID AS PROCESS,PROCESS_NAME AS PROCESS_NAME FROM CRM_CASE_PROCESS", "PROCESS,PROCESS_NAME", pmSys.conn_crm);
            mydg.fill_fld(i++, "任务状态", "STATUS", 8, 1);
            mydg.set_cols_cbo_list("STATUS", "SELECT FLD_ID,FLD_NAME FROM DICT_TASK_STATUS", "FLD_ID,FLD_NAME", pmSys.conn_crm);           
            mydg.fill_fld(i++, "调度员", "GHID", 8, 1);
            mydg.set_cols_cbo_list("GHID", "SELECT GHID,REAL_NAME FROM CTS_OPIDK", "GHID,REAL_NAME", pmSys.conn_callthink);
            //mydg.fill_fld(i++, "报修时间", "SDATE", 12, 8, "CMDNAME=Edits;NULLAS=[null];");
            mydg.fill_fld(i++, "维修工程师", "GHID_ENGR", 10, 1);
            mydg.set_cols_cbo_list("GHID_ENGR", "SELECT GHID,OP_NAME FROM  " + m_TableName_Skill, "GHID,OP_NAME", pmSys.conn_crm);
            mydg.fill_fld(i++, "维修工程师姓名", "ENGR_NAME", 0);

            //mydg.fill_fld(i++, "工程师联系电话", "ENGR_TEL", 16);
            mydg.fill_fld(i++, "工程师所属机构代码", "ENGR_ORG_CODE", 16, 1);
            mydg.set_cols_cbo_list("ENGR_ORG_CODE", "SELECT ORG_CODE,ORG_NAME FROM  " + m_TableName_Org_Code, "ORG_CODE,ORG_NAME", pmSys.conn_crm);
            //mydg.set_cols_cbo_list("ENGR_ORG_CODE",);
            mydg.fill_fld(i++, "报修时间", "SDATE", 8,5,"Date");
            mydg.fill_fld(i++, "派单时间", "SDATE_SEND", 18);
            mydg.fill_fld(i++, "查看", "", 4, 8);

            mydg.RowDataFilled = this;
            mydg.fill_header("dgvCase", "TASKID", m_strFilter() + m_strOrder_by());
        }


        public void mydg_RowDataFilled(Object sender, int rows)
        {
            if (rows < 0) return; //表头行，不处理
            String strTaskid = mydg.get_cell(rows, "TASKID");
            String strName = mydg.get_cell(rows, "ENGR_NAME");
            String strCaseid = mydg.get_cell(rows, "CASEID");
            String strCaseType = mydg.get_cell(rows, "CASETYPE");
            if (strTaskid.equals("")) return;
            int cols = mydg.get_idx("SDATE_SEND");
            if (cols >= 0)
            {
                String strNewUrl = "case_task_edit.aspx?cmd=Edit&caseid=" + strCaseid + "&taskid=" + strTaskid + "&ntype=" + strCaseType;
                if (pmAgent.c_Levels.check_authority(e_Level_service.service_dds) == true) //有调度权限的座席
                {
                    //add by gaoww 20151120
                    String strOrg_Code_eng = mydg.get_cell(rows, "ENGR_ORG_CODE"); //工程师所属机构
                    String strGhid_engr = mydg.get_cell(rows, "GHID_ENGR");  //工程师
                    if (strGhid_engr.equals(""))//没有分派工程师的，并且当前任务所属机构是自己部门或下属部门的，进入任务分派页面
                    {
                        if (strOrg_Code_eng.startsWith(myString.TrimEnd(pmAgent.c_Info.agent_org_code,'0')) == true)
                        {
                            strNewUrl = myString.Format("case_task_assign.aspx?cmd=Edit&caseid={0}&taskid={1}&ntype={2}", strCaseid,strTaskid, pType);
                        }
                    }
                }
                String strHtml = myString.Format("<a href='{0}'>{1}</a>", strNewUrl, fun_main.CreateHtml_img("Tasks.gif", ""));
                mydg.set_cell(rows,cols + 1,strHtml);
            }

            cols = mydg.get_idx("STATUS"); //任务状态根据工单类型、任务类型和环节相关，所以需要逐条分析
            if (cols >= 0)
            {
                String strStatus = mydg.get_cell(rows, "STATUS");
                String strProcess = mydg.get_cell(rows, "PROCESS");
                String strTaskType = mydg.get_cell(rows, "TASKTYPE");
                String strKey_kvdb = "dtTask_status_"+strProcess +"_"+strCaseType +"_"+strTaskType;          
                DataTable dtTable = pmInfo.myKvdb.Get(strKey_kvdb,DataTable.class);
                if (dtTable == null)
                {
                    my_odbc pTable = new my_odbc(pmSys.conn_crm);
                    String strSql ="SELECT FLD_ID,FLD_NAME FROM DICT_TASK_STATUS WHERE CASETYPE ='" + strCaseType + "' AND PROCESS = '" + strProcess + "' AND TASKTYPE='" + strTaskType + "'";
                    pmList pm= pTable.my_odbc_find(strSql ,0 );
                    int rc =pm.nRet;
                    dtTable=pm.dtRet;
                    pTable.my_odbc_disconnect();
                    pmInfo.myKvdb.Setex(strKey_kvdb, dtTable, 60); //60秒
                }
                if(dtTable.getCount() >0)
                {
                 /*DataRow[] myRow = dtTable.select("FLD_ID='" + strStatus + "'");
                    if (myRow.length() > 0)
                    {
                        String strStatus_Name = Functions.drCols_strValue(myRow[0], "FLD_NAME");
                        mydg.set_cell(rows,cols,strStatus_Name);
                    }*/
                	for (int i = 0; i < dtTable.Rows().Count(); i++) {
						if (Functions.dtCols_strValue(dtTable, i,"FLD_ID").equals(strStatus)) {
							   String strStatus_Name = Functions.dtCols_strValue(dtTable, i, "FLD_NAME");
		                        mydg.set_cell(rows,cols,strStatus_Name);
		                        break;
						}
					}
                }
            }
        }
      
        private void InitToolbar()
        {
            LinkedHashMap<String, String> m_dyProcess = new LinkedHashMap<String, String>();
            m_dyProcess.put("All", "全部");
            DataTable dtTemp;
            dtTemp = Functions.dt_GetTable("SELECT FLD_ID,FLD_NAME FROM DICT_TASK_STATUS WHERE CASETYPE='"+pType +"'", "", pmSys.conn_crm);
            String strKey, strValue;
            for (DataRow myRow : dtTemp.Rows())
            {
                strKey = Functions.drCols_strValue(myRow, "FLD_ID");
                strValue = Functions.drCols_strValue(myRow, "FLD_NAME");
                if (Regex.IsMatch(strValue, "[\\d]+-") == false)  //STATUS_NAME 存成 1-已完成 ->  已完成
                {
                    strValue = strKey + "-" + strValue;
                }
                m_dyProcess.put(strKey, strValue);
            }

            int nSelectIndex = 0, nTemp = 0;
            dtTemp = Functions.dt_GetTable("CRM_CASE_TABLE", "CASETYPE>0 AND CASETYPE<20", pmSys.conn_crm);
            LinkedHashMap<String, String> m_dyCaseType = new LinkedHashMap<String, String>();
            for (DataRow myRow : dtTemp.Rows())
            {
                strKey = Functions.drCols_strValue(myRow, "CASETYPE");
                strValue = strKey + "(" + Functions.drCols_strValue(myRow, "CASE_NAME") + ")";
                m_dyCaseType.put(strKey, strValue);
                if ( Integer.toString(pType).equals(strKey) )
                {
                    nSelectIndex = nTemp;
                }
                nTemp++;
            }

            if (pCaseid.equals(""))
            {
                myToolBar.fill_fld("选择工单类型", "Select_CaseType", 25, 4, "请选择服务分类");
                //myToorBar.set_list("Select_CaseType", "SELECT * FROM CRM_CASE_TABLE WHERE CASETYPE>0 AND CASETYPE<20 ORDER BY CASETYPE", "CASETYPE,CASE_NAME", pmSys.conn_crm);
                myToolBar.set_list("Select_CaseType", m_dyCaseType); //modify by gaoww 20161114
                myToolBar.fill_fld("Separator", "Separator0", 0, 3);
            }
            if (pMark.equals("pending")==false && pMark.equals("handling")==false && pMark.equals("finish")==false)
            {
                myToolBar.fill_fld("请选择", "SelectStatus", 20, 4, "请选择");
                myToolBar.set_list("SelectStatus", m_dyProcess);
            }
            myToolBar.fill_fld("Separator", "Separator1", 0, 3);
            myToolBar.fill_fld("刷新", "Query_all");
            myToolBar.fill_fld("Separator", "Separator2", 0, 3);
            if (pOp.equals("Edit"))
            {
                if (m_Support_Task == 1) //当前环节支持任务，才可以新建任务
                {
                    myToolBar.fill_fld("新建任务", "AddNew");
                    myToolBar.fill_fld("Separator", "Separator3", 0, 3);
                }
            }

            if (pMark.equals("menu") && pmAgent.c_Levels.check_authority(e_Level_service.service_dds) == true)//当任务列表是从导航菜单点击过来时设置增加为只读
            {
                myToolBar.set_readonly("AddNew", true);
            }

            myToolBar.fill_fld("查询", "Query", 0, 10);
            myToolBar.fill_fld("Separator", "Separator5", 0, 3);
            myToolBar.fill_fld(fun_main.Term("LBL_TOEXCEL"), "Output");

            myToolBar.btnItemClick = this;// new btnClickEventHandler(myToolBar_btnItemClick);
            myToolBar.fill_toolStrip("plCommand");

            if (myToolBar.isExist("Select_CaseType"))
            {
                //myToolBar.get_item_cbo("Select_CaseType").SelectedIndex = nSelectIndex;
            	myToolBar.set_item_value("Select_CaseType",Integer.toString(nSelectIndex) );
            }
        }
        public void myToolBar_btnItemClick(Object sender, String name, String parms, int nparms)
        {
            //刷新
            if (name.equals("Query_all"))
            {
                m_Filter_search = "";
                Save_ss("pFilter_search" + pType, m_Filter_search);
                mydg.refresh("TASKID", m_strFilter() + m_strOrder_by());
            }
            else if (name.equals("AddNew"))
            {
                Functions.Redirect(myString.Format("case_task_assign.aspx?caseid={0}&ntype={1}", pCaseid, pType));
            }
            //删除
            else if (name.equals("Delete"))
            {
                int nPos = 0;
                if (mydg.RowCount() <= 0) return;
                List<String> alRet = mydg.GetSelectedKey("TASKID");
                if (alRet.size() == 0)
                {
                    Functions.MsgBox("请先选中要任务！");
                    return;
                }
                my_odbc pTable = new my_odbc(pmSys.conn_crm);
                for (int rows = 0; rows < mydg.RowCount(); rows++)
                {
                    if (mydg.isSelected(rows) == true)
                    {
                        String strTaskId = mydg.get_cell(rows, "TASKID");
                        int rc = pTable.my_odbc_find(pTableName, "TASKID=" + strTaskId);
                        if (rc == 1)
                        {
                            pTable.my_odbc_delete(pTableName, "TASKID=" + strTaskId);
                            nPos++;
                        }
                        pTable.my_odbc_disconnect();
                    }
                }
                mydg.refresh("TASKID", m_strFilter() + m_strOrder_by());
                if (nPos > 0)
                    Functions.MsgBox("成功删除<" + nPos + ">条记录！");
                else
                    Functions.MsgBox("删除记录失败，请选中要删除的记录！");
            }
            //导出
            else if (name.equals("Output"))
            {
                my_odbc pTemp = new my_odbc(pmSys.conn_crm);
                String strSql = "SELECT COUNT(*) AS CNT FROM " + pTableName;
                if (m_strFilter_base().equals("")==false)
                    strSql += " WHERE " + m_strFilter();
                int rc = pTemp.my_odbc_find(strSql);
                int nCount = 0;
                if (rc > 0)
                    nCount = pTemp.my_odbc_result("CNT",0);
                pTemp.my_odbc_disconnect();
                if (nCount > 10000)
                {
                    Functions.MsgBox("输出的任务记录超过1万条，请查询后再输出！");
                    return;
                }
                mydg.Output_toExcelFile("任务资料列表", pmSys.CaptionExport + "_任务资料", 0);
            }
               //用casetype筛选任务
            else if (name.equals("Select_CaseType"))
            {
                mydg.refresh("TASKID", m_strFilter() + m_strOrder_by());
            }           
            else if (name.equals("SelectStatus"))
            {               
                mydg.refresh("TASKID", m_strFilter() + m_strOrder_by());
            }
        }
        private void Fillin_SearchField()
        {
            myFld_Query.SetWidth(pmAgent.content_width);
            myFld_Query.SetMaxLabelLenth(80);
            myFld_Query.SetMaxLabelLenth_col2(80);
            myFld_Query.funName_OnClientClick("mySearch_FieldLinkClicked");

            myFld_Query.fill_fld("开始日期", "SDATE", 21, 5, true, true, DateTime.Now().AddYears(-3).ToString("yyyy-MM-dd"), "yyyy-MM-dd");
            myFld_Query.set_tooltip("SDATE", "报修开始时间");
            myFld_Query.fill_fld("结束日期", "EDATE", 21, 5, true, false, "", "yyyy-MM-dd");
            myFld_Query.set_tooltip("EDATE", "报修结束时间");
            myFld_Query.fill_fld("工程师姓名", "ENGR_NAME", 20);
            ArrayList<String> alButton_ex = new ArrayList();
            alButton_ex.add("DESP=清除;NAME=Reset;");
            myFld_Query.fill_fld_button("查询", "Search", null, false, alButton_ex, "right");
            myFld_Query.fill_Panel("plSeach");
            myFld_Query.FieldLinkClicked = this;// new SearchFieldLinkClickedEventHandler(myFld_Query_FieldLinkClicked);
        }

        public void myFld_Query_FieldLinkClicked(Object sender, String name, String parms, int nparms, int ntype)
        {
            if (name.equals("Search"))
            {
                //验证查询条件
                boolean isBool = Validate();
                if (!isBool) return;
                //生成查询条件
                String strFilter = GetFilter_search();
                m_Filter_search = strFilter;
                Save_ss("pFilter_search" + pType, m_Filter_search);
                mydg.refresh("TASKID", m_strFilter() + m_strOrder_by());
            }
        }

        /// <summary>
        /// 验证查询条件
        /// </summary>
        /// <returns>返回是否通过验证</returns>
        private boolean Validate()
        {
            //报修开始时间
            String strSdate = myFld_Query.get_item_value("SDATE").replace("-", "");
            //报修结束时间
            String strEdate = myFld_Query.get_item_value("EDATE").replace("-", "");
            if (Functions.atoi(strSdate) > Functions.atoi(strEdate))
            {
                Functions.MsgBox("开始时间必须小于结束时间！");
                return false;
            }
            else
            {
                return true;
            }
        }
        /// <summary>
        /// 查询条件
        /// </summary>
        /// <returns>查询条件字符串</returns>
        private String GetFilter_search()
        {
            //报修开始时间
            String strSdate = myFld_Query.get_item_value("SDATE").replace("-", "");
            //报修结束时间
            String strEdate = myFld_Query.get_item_value("EDATE").replace("-", "");
            //工程师姓名
            String strName = myFld_Query.get_item_value("ENGR_NAME");
            String strFilter_search ="";
            if (strSdate.length() > 0)
            {
                if (strFilter_search.length() > 0) strFilter_search += " AND ";
                strFilter_search += "SDATE > '" + strSdate + "'";
            }
            if (strEdate.length() > 0)
            {
                if (strFilter_search.length() > 0) strFilter_search += " AND ";
                strFilter_search += "SDATE < '" + strEdate + "'";
            }
            if (strName.length() > 0)
            {
                if (strFilter_search.length() > 0) strFilter_search += " AND ";
                strFilter_search += "ENGR_NAME LIKE '%" + strName + "%'";
            }
            if (strFilter_search.length()<=0)
            {
                strFilter_search = "1<>1";
            }
            return strFilter_search;
        }
        

        private String m_strFilter()
        {
                String strFilter = m_strFilter_base();
                if (m_strFilter_priv().length() > 0)
                {
                    if (strFilter.length() > 0) strFilter += " AND ";
                    strFilter += m_strFilter_priv();
                }
                return strFilter;
        }

        //基本的显示条件
        private String m_strFilter_base()
        {
                String strFilter = "";
                if (pMark.equals("menu"))//如果是导航菜单进入的任务管理
                {
                    String strTaskStatus = myToolBar.get_item_text("SelectStatus");
                    if(strTaskStatus==null||strTaskStatus.length()==0) strTaskStatus="全部";//add by duzy 20170512
                    if (strTaskStatus.equals("全部")==false)
                        strTaskStatus = Functions.Substring(strTaskStatus, "", "-");
                    String strTemp = myToolBar.get_item_value("Select_CaseType");
                    String strCasetype =strTemp;// Functions.Substring(strTemp, "", "(");
         
                    if (strTaskStatus.equals("全部")==false)
                        strFilter += "STATUS='" + strTaskStatus + "' AND CASETYPE='" + strCasetype + "'";
                    else if (strCasetype.equals("")||strCasetype.equals("0")) {//默认显示全部
                    	// strFilter += "CASETYPE='1'";
					}
                    else {
                    	 strFilter += "CASETYPE='" + strCasetype + "'";
					}
                }
                else if (pCaseid.equals("")==false)
                {
                    strFilter = " CASEID='" + pCaseid + "'";
                }

                if (pFilter.length() > 0)
                {
                    if (strFilter.length() > 0) strFilter += " AND ";
                    strFilter += pFilter;
                }
                if (m_Filter_search.length() > 0)
                {
                    if (strFilter.length() > 0) strFilter += " AND ";
                    strFilter += m_Filter_search;
                }

                return strFilter;
        }


        //给予角色的特权 privileged
        private String m_strFilter_priv()
        {
                String strFilter = "",strCode = "";
                strCode =myString.TrimEnd(pmAgent.c_Info.agent_org_code, '0');//pmAgent.c_Info.agent_org_code.TrimEnd('0'); 
                if (pMark.equals("menu"))
                {                    
                    if (pmAgent.c_Levels.check_authority(e_Level_service.service_view_admin) == false)//如果没有服务系统资料浏览最高权限
                    {
                        //服务系统资料浏览增强权限
                        if (pmAgent.c_Levels.check_authority(e_Level_service.service_view_adv) == true)//显示本建单人所属机构代码以及下级属机构代码的服务单
                        {
                            strFilter = myString.Format(" (ORG_CODE LIKE '{0}%' OR ENGR_ORG_CODE LIKE '{0}%' )", strCode);
                        }
                        else//两个权限都没有的时候,可以看分配给自己的任务
                        {
                            strFilter = myString.Format(" (GHID_ENGR = '{0}' )", pmAgent.uid);
                        }
                    }
                }
                else if (pMark.equals("pending") || pMark.equals("handling") || pMark.equals("finish") || pMark.equals("unfinish")) //modify by gaoww 20160603 增加unfinish，从主页我的任务进入
                {
                    strFilter = "GHID_ENGR='" + pmAgent.uid + "'";
                    if (pMark.equals("pending"))     //待处理
                        strFilter += " AND STATUS='0'";
                    else if (pMark.equals("finish")) //已完成或已取消
                        strFilter += " AND (STATUS='98' OR STATUS='99')";
                    else                        //处理中
                        strFilter += " AND (STATUS>'0' AND STATUS<'98')";
                }
                return strFilter;
           
        }

        //排序规则
        private String m_strOrder_by()
        {
                String strOrderby = " ORDER BY TASKID DESC";
                return strOrderby;
        }
    }

