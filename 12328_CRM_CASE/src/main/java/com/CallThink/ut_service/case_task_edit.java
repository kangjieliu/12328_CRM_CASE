///########################################################################################
/// Copyright (C) 2000, ToneThink.Soft  All Rights Reserved. 
///#########################################################################################
/// 文件创建时间：2015-09-07
///   文件创建人：gaoww
/// 文件功能描述：任务工单编辑页面
///     调用格式：
///     
///     维护记录：
/// 2015.09.07：create by gaoww         
///#########################################################################################
package com.CallThink.ut_service;

import java.util.HashMap;
import org.springframework.ui.Model;
import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.pmSys;
import com.CallThink.base.pmCode.UltraCRM_Page;
import com.CallThink.base.support.pmInfo;
import com.CallThink.ut_form.pmModel_form.fun_Form;
import com.CallThink.ut_service.pmModel_service.e_Level_service;
import com.CallThink.ut_service.pmModel_service.fun_service;
import com.CallThink.ut_service.pmModel_service.service_info;
import com.CallThink.ut_service.pmModel_service.service_set_info;
import com.ToneThink.DataTable.DataTable;
import com.ToneThink.DateTime.DateTime;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.pmList;
import com.ToneThink.ctsTools.myUtility.pmMap;
import com.ToneThink.ctsTools.myUtility.pmRef;
import com.ToneThink.ctsTools.Regex.Regex;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.myString;
import com.ToneThink.ctsTools.WebUI.my_Field;
import com.ToneThink.ctsTools.WebUI.my_ToolStrip;

    public class case_task_edit extends UltraCRM_Page
    {
        private String pOp = "";
        private int pType = 0;
        private String pCaseId = "";
        private String pFrom = "";
        private String pTaskId = "";
        private String pTask_Status = "";//任务状态0-未签收、98-取消、99-完成为固定值
        private String m_TableName = "SM_TASK_DISP";
        private String m_TableName_Skill = "SM_BUSS_GHID_SKILL"; //工程师技能表
        private String m_TableName_Status = "DICT_TASK_STATUS";//任务状态表
        private String m_TableName_Org_Code = "DICT_ORG_CODE";//服务商组织机构
        private HashMap m_htPriv = new HashMap();

        my_Field myFld = new my_Field(3);
        my_Field myFld_Task = new my_Field(2);
        my_ToolStrip myToolBar = new my_ToolStrip();
        my_ToolStrip myToolBar_case = new my_ToolStrip(); //add by gaoww 20161114 增加查看服务单功能

        service_set_info myCase;
        private int m_Support_Task = 0; //是否支持任务

        DataTable dtTools;

        public void Page_Load(Object sender, Model model)
        {
            if (IsPostBack == false)//正被首次加载和访问
            {
                HashMap htQuery;
                pmMap res = fun_main.QuerySplit(Request); htQuery = res.htRet;
                int rc = res.nRet;
                if (rc > 0)
                {
                    pOp = Functions.ht_Get_strValue("cmd", htQuery);
                    pType = Functions.atoi(Functions.ht_Get_strValue("ntype", htQuery));
                    pCaseId = Functions.ht_Get_strValue("caseid", htQuery);
                    pFrom = Functions.ht_Get_strValue("From", htQuery);//直接工服务单列表页面跳转过来
                    pTaskId = Functions.ht_Get_strValue("taskid", htQuery);    
                }
                Save_vs("pOp", pOp);
                Save_vs("pType", pType);
                Save_vs("pCaseId", pCaseId);
                Save_vs("pFrom", pFrom);
                Save_vs("pTaskId", pTaskId);
                myCase = new service_set_info(pType);           
                service_info myCaseInfo = myCase.GetCaseRecord(pCaseId);
                m_Support_Task = myCase.is_sm_support(myCaseInfo.nProcess);
                Save_vs("m_Support_Task", m_Support_Task);
            }
            else
            {
                pOp = Load_vs("pOp");
                pType = Functions.atoi(Load_vs("pType"));
                pCaseId = Load_vs("pCaseId");
                pFrom = Load_vs("pFrom");
                pTaskId = Load_vs("pTaskId");
                pTask_Status = Load_vs("pTask_Status");
                m_Support_Task = Functions.atoi(Load_vs("m_Support_Task"));
                dtTools = Load_vs("dtTools",DataTable.class);
            }
            myCase = new service_set_info(pType);
            //ONLY FOR IPAD   
            if (Functions.isIPad() == true) //如果是IPAD 则只显示一列，如果多列会无法显示全
            {
                myFld = new my_Field(1);
                myFld_Task = new my_Field(1);
            }
            
            Fillin_field_case();
            Fill_withCaseid(pCaseId);

            Fillin_field_task();
            Fill_Case_Default();
            if (IsPostBack == false)//正被首次加载和访问
            {
                String strProcess = myFld_Task.get_item_value("PROCESS");
                String strTaskType = myFld_Task.get_item_value("TASKTYPE");
                String strSql = myString.Format("SELECT FLD_ID,FLD_NAME,NOTE,ACTION_TASK,ACTION_CASE FROM DICT_TASK_ACTION WHERE CASETYPE='{0}' AND PROCESS='{1}' AND TASKTYPE='{2}' AND FLD_TYPE&1>0 ORDER BY FLD_ORDER", pType, strProcess, strTaskType);
                my_odbc pTable = new my_odbc(pmSys.conn_crm);
                pmList res =pTable.my_odbc_find(strSql,0); dtTools = res.dtRet;
                int rc = res.nRet;
                pTable.my_odbc_disconnect();
                Save_vs("dtTools", dtTools);
            }
           
            InitToolbar();
        
            myToolBar.render(model);
            myFld.render(model);
            myFld_Task.render(model);
            myToolBar_case.render(model);
}

        private void Fillin_field_case()
        {
            myFld.SetConnStr(pmSys.conn_crm);
            myFld.SetTable(m_TableName);
            myFld.SetLabelAlign("Right");
            myFld.SetMaxLabelLenth(100);
            myFld.funName_OnClientClick("myFld_FieldLinkClicked");
            myFld.fill_fld("#服务单信息", "#1", 20);
            //myFld.fill_fld("工单类型", "CASETYPE", 25, 1, false);
            //myFld.set_list("CASETYPE", "SELECT CASE_NAME,CASETYPE FROM CRM_CASE_TABLE", "CASETYPE,CASE_NAME", pmSys.conn_crm);
            //myFld.fill_fld("工单状态", "STATUS", 25, 1, false);
            //myFld.set_list("STATUS", "SELECT STATUS_NAME AS STATUS_NAME,STATUS_ID AS STATUS FROM CRM_CASE_STATUS WHERE CASETYPE='" + pType + "'", "STATUS,STATUS_NAME", pmSys.conn_crm);
            myFld.fill_fld("客户名称", "UNAME", 25, 0, false);
            myFld.fill_fld("联系电话", "TEL", 25, 0, false);
            //myFld.fill_fld("受理时间", "SDATE", 25, 0, false);
            myFld.fill_fld("预约时间", "SDATE_NOTE", 25, 0, false);

            String fld_name = "";
            pmRef<String> pmRefValue=new pmRef<String>(fld_name);
            int nResult = get_desc_Field("SVC_CATEGORY", pmRefValue );
            fld_name=pmRefValue.oRet;
            if (nResult == 1)
            {
                myFld.fill_fld(fld_name, "SVC_CATEGORY", 25, 1, false, true);
                myFld.set_list("SVC_CATEGORY", "SELECT FLD_ID AS SVC_CATEGORY,FLD_NAME AS FLD_NAME  FROM  DICT_SVC_CATEGORY", "SVC_CATEGORY,FLD_NAME", pmSys.conn_crm);
            }

            pmRefValue=new pmRef<String>(fld_name);
            nResult = get_desc_Field("SVC_CATEGORY2", pmRefValue);
            fld_name=pmRefValue.oRet;
            if (nResult == 1)
            {
                myFld.fill_fld(fld_name, "SVC_CATEGORY2", 25, 1, false);
                myFld.set_list("SVC_CATEGORY2", "SELECT FLD_ID AS SVC_CATEGORY2,FLD_NAME AS FLD_NAME  FROM  DICT_SVC_CATEGORY2", "SVC_CATEGORY2,FLD_NAME", pmSys.conn_crm);
            }

            pmRefValue=new pmRef<String>(fld_name);
            nResult = get_desc_Field("SVC_MODE", pmRefValue);
            fld_name=pmRefValue.oRet;
            if (nResult == 1)
            {
                myFld.fill_fld(fld_name, "SVC_MODE", 25, 1, false);
                myFld.set_list("SVC_MODE", "SELECT FLD_ID AS SVC_MODE,FLD_NAME AS FLD_NAME FROM DICT_SVC_MODE", "SVC_MODE,FLD_NAME", pmSys.conn_crm);
            }

            pmRefValue=new pmRef<String>(fld_name);
            nResult = get_desc_Field("PO_CATEGORY", pmRefValue);
            fld_name=pmRefValue.oRet;
            if (nResult == 1)
            {
                myFld.fill_fld(fld_name, "PO_CATEGORY", 25, 1, false, true);
                myFld.set_list("PO_CATEGORY", "SELECT FLD_ID,FLD_NAME FROM DICT_PO_CATEGORY", "FLD_ID,FLD_NAME", pmSys.conn_crm);
            }

            pmRefValue=new pmRef<String>(fld_name);
            nResult = get_desc_Field("PO_CATEGORY2", pmRefValue);
            fld_name=pmRefValue.oRet;
            if (nResult == 1)
            {
                myFld.fill_fld(fld_name, "PO_CATEGORY2", 25, 1, false);
                myFld.set_list("PO_CATEGORY2", "SELECT FLD_ID,FLD_NAME FROM DICT_PO_CATEGORY2", "FLD_ID,FLD_NAME", pmSys.conn_crm);
            }
            
            myFld.fill_fld("地址", "ADDRESS", 92, 12, false);
            myFld.fill_Panel("gbCase");
        }

        /// <summary>
        /// 从表单描述表中动态读取服务单相关字段描述，以及是否显示
        /// </summary>        
        /// <param name="fld_name">字段名称</param>
        /// <param name="strFld_desc">字段描述</param>
        /// <returns>0-不显示，1-显示</returns>
        private int get_desc_Field(String fld_value,   pmRef<String> pmRefValue)
        {
            int nReturn = 0;
            pmRefValue.oRet = "";
            String strKey_kvdb = "dtCaseTask_edit_" + pType;
            DataTable dtStru = pmInfo.myKvdb.Get(strKey_kvdb,DataTable.class);// as DataTable;
            if (dtStru == null)
            {
                dtStru = fun_Form.get_desc_data(myCase.DescName, "((FLD_VLEVELS&1)<>0) ORDER BY EDIT_INDEX");
                pmInfo.myKvdb.Setex(strKey_kvdb, dtStru, 60); //60秒
            }
            DataTable myRow = dtStru.select("FLD_VALUE='" + fld_value + "'");
            if (myRow.Rows().Count() > 0)
            {
            	pmRefValue.oRet = Functions.dtCols_strValue(myRow,0, "FLD_NAME");
                nReturn = 1;
            }
            return nReturn;
        }

        public void Fill_withCaseid(String strCaseId)
        {
            DataTable dtRet;
            my_odbc pTable = new my_odbc(pmSys.conn_crm);
            pmList res =pTable.my_odbc_find(myCase.TableName, "CASEID = '" + strCaseId + "'",0); dtRet = res.dtRet;
            int rc = res.nRet;
            pTable.my_odbc_disconnect();
            if (rc < 0) return;
            myFld.Load(dtRet);
        }

        private void Fillin_field_task()
        {
            myFld_Task.SetConnStr(pmSys.conn_crm);
            myFld_Task.SetTable(m_TableName);
            myFld_Task.SetLabelAlign( "Right");
            myFld_Task.SetMaxLabelLenth(120);
            myFld_Task.SetMaxLabelLenth_col2(100);
            myFld_Task.funName_OnClientClick("myFld_FieldLinkClicked");
            myFld_Task.fill_fld("#任务信息", "#2", 25, 0, false);
            myFld_Task.fill_fld("工单类型", "CASETYPE", 40, 1, false);
            myFld_Task.set_list("CASETYPE", "SELECT * FROM CRM_CASE_TABLE WHERE CASETYPE>0 AND CASETYPE<20 ORDER BY CASETYPE", "CASETYPE,CASE_NAME", pmSys.conn_crm);
            
            myFld_Task.fill_fld("执行状态", "STATUS", 40, 1, false);        
            String strSql="SELECT FLD_ID,FLD_NAME FROM  " + m_TableName_Status;
            my_odbc pTable = new my_odbc(pmSys.conn_crm);
            int rc = pTable.my_odbc_find(m_TableName, "TASKID='" + pTaskId + "'");
            if (rc > 0)
            {
                String strProcess = "", strTaskType = "";
                strProcess = pTable.my_odbc_result("PROCESS");
                strTaskType = pTable.my_odbc_result("TASKTYPE");
                strSql += " WHERE PROCESS='" + strProcess + "' AND TASKTYPE='" + strTaskType + "' AND CASETYPE='"+pType +"'";
            }
            pTable.my_odbc_disconnect();
            myFld_Task.set_list("STATUS", strSql, "FLD_ID,FLD_NAME", pmSys.conn_crm);
            
           
            myFld_Task.fill_fld("任务类型", "TASKTYPE", 40, 1,false); //不可修改，如果要改，取消此任务，重新建新任务，否则影响工单状态的变更
            myFld_Task.set_list("TASKTYPE", "SELECT FLD_ID AS FLD_ID,FLD_NAME AS FLD_NAME FROM  DICT_TASK_TYPE", "FLD_ID,FLD_NAME", pmSys.conn_crm);
            myFld_Task.fill_fld("任务级别", "TASK_LEVEL", 40, 1, false); //不可修改，如果要改，取消此任务，重新建新任务，否则影响工单状态的变更
            myFld_Task.set_list("TASK_LEVEL", "主任务,从任务");           
            
            myFld_Task.fill_fld("调度员", "GHID", 40, 1,false,true);
            myFld_Task.set_list("GHID", "SELECT GHID,REAL_NAME FROM  CTS_OPIDK", "GHID,REAL_NAME", pmSys.conn_callthink);
            myFld_Task.fill_fld("调度员所属机构", "ORG_CODE", 40, 1, false);
            myFld_Task.set_list("ORG_CODE", "SELECT ORG_CODE,ORG_NAME FROM  " + m_TableName_Org_Code, "ORG_CODE,ORG_NAME", pmSys.conn_crm);
           
           
            //myFld.fill_fld("工程师编号", "GHID_ENGR", 25);
            myFld_Task.fill_fld("工程师", "GHID_ENGR", 40, 1,false);
            myFld_Task.set_list("GHID_ENGR", "SELECT GHID,REAL_NAME FROM  CTS_OPIDK", "GHID,REAL_NAME", pmSys.conn_callthink);
            myFld_Task.fill_fld("工程师联系电话", "ENGR_TEL", 40);
            myFld_Task.fill_fld("工程师所属机构", "ENGR_ORG_CODE", 40, 1,false);
            myFld_Task.set_list("ENGR_ORG_CODE", "SELECT ORG_CODE,ORG_NAME FROM  " + m_TableName_Org_Code, "ORG_CODE,ORG_NAME", pmSys.conn_crm);
            myFld_Task.fill_fld("报修时间", "SDATE", 40, 0, false);
            myFld_Task.fill_fld("派单时间", "SDATE_SEND", 40, 0, false);
            myFld_Task.fill_fld("签收时间", "SDATE_RECV", 40, 0, false);
            myFld_Task.fill_fld("出发时间", "SDATE_GO", 40, 0, false);
            myFld_Task.fill_fld("开始时间", "SDATE_WORK", 40, 0, false);
            //myFld_Task.fill_fld("上门时间", "SDATE_REACH", 40, 0, false);
            myFld_Task.fill_fld("完成时间", "EDATE", 0, 5, false, false, "yyyy-MM-dd HH:mm:ss");
            myFld_Task.fill_fld("处理结果代码", "RESULT_CODE", 40, 1);
            myFld_Task.set_list("RESULT_CODE", "SELECT FLD_ID AS RESULT_CODE,FLD_NAME AS FLD_NAME FROM DICT_HANDLE_RESULT", "RESULT_CODE,FLD_NAME", pmSys.conn_crm);
            myFld_Task.fill_fld("处理结果", "RESULT_NOTE", 100, 0);
            //myFld_Task.fill_fld("拒绝原因", "REASON_REFUSE", 100, 0);
            //myFld_Task.fill_fld("未解决原因", "REASON_BACK", 100, 0);
            //myFld_Task.fill_fld("取消原因", "REASON_CANCLE", 100, 0);
            //myFld_Task.fill_fld("中断原因", "REASON_BREAK", 100, 0);           
            myFld_Task.fill_fld("收入", "INCOME", 40, 0);
            myFld_Task.fill_fld("扫码", "BAR_CODE", 40, 0);
            myFld_Task.fill_fld("机器照片", "PHOTO", 40, 16, true, false, "", ""); //add by gaoww 20151210 增加图片上传功能
            myFld_Task.fill_fld("客户签名", "SIGNATURE", 40, 6); //add by gaoww 20151210 增加签名功能
            myFld_Task.fill_fld("任务单编号", "TASKID", 40, 0, false);
            myFld_Task.fill_fld("服务单编号", "CASEID", 40, 0, false);
            myFld_Task.fill_fld("环节", "PROCESS", 0,1);
            myFld_Task.set_list("PROCESS", "SELECT PROCESS_ID,PROCESS_NAME FROM CRM_CASE_PROCESS", "PROCESS_ID,PROCESS_NAME", pmSys.conn_crm);
            myFld_Task.FieldLinkClicked = this;// new FieldLinkClickedEventHandler(myFld_FieldLinkClicked);
                
            myFld_Task.fill_Panel("gbTask");
        }

        public void myFld_FieldLinkClicked(Object sender, String name, String parms, int nparms, int ntype)
        {
            if ((ntype == 16)) //上传图片   //add by gaoww 20151207 由ctstools控件实现image
            {
            
            	//未处理代码
             /*   FileUpload fuTemp = myFld.get_item_fileUpload(name);
                if (fuTemp.HasFile == false) return;

                String strUrl = fun_main.Upload_imgFile(fuTemp, null, true);
                myFld_Task.set_item_value(name, strUrl);*/
            }
        }

        private void InitToolbar()
        {
            //add by gaoww 20161114 增加原始服务单查看功能
            myToolBar_case.fill_fld("原始服务单", "View_case", 0, 10);
            myToolBar_case.fill_toolStrip("plCommand_case");

            myToolBar.Clear();
            if (pFrom.equals("list")==false)
            {
                myToolBar.fill_fld(fun_main.Term("LBL_Return"), "Return");
                myToolBar.fill_fld("Separator", "Separator0", 0, 3);
            }
            if (pmAgent.c_Levels.check_authority(e_Level_service.service_dds) == true)
                if (m_Support_Task == 1) //当前环节支持任务的才可新建任务
                    myToolBar.fill_fld("新建任务", "AddNew", 0, 0);
            myToolBar.fill_fld_confirm("保存", "Save", "确定保存任务资料！");
            myToolBar.fill_fld("Separator", "Separator2", 0, 3);

            String strGhid_engr = myFld_Task.get_item_value("GHID_ENGR");
            if (pOp.equals("Edit"))
            {
                if (pTask_Status.equals("0") && strGhid_engr == pmAgent.uid) //当前任务状态为0，且是自己的任务，可以签收
                {
                    myToolBar.fill_fld("签收", "Accept");
                    //myToorBar.fill_fld("拒绝", "Refuse");
                }
            }

            if (pTask_Status.equals("0")==false && pTask_Status.equals("2")==false && pTask_Status.equals("98")==false && pTask_Status.equals("99")==false && strGhid_engr == pmAgent.uid) //任务状态为执行中，并且是自己的任务
            {
                for (int rows = 0; rows < dtTools.getCount(); rows++)
                {
                    String strFld_id = Functions.dtCols_strValue(dtTools, rows, "FLD_ID");
                    String strFld_name = Functions.dtCols_strValue(dtTools, rows, "FLD_NAME");
                    myToolBar.fill_fld(strFld_name, "TASK_CUSTOM_" + strFld_id);
                }
            }
            //属于自己的任务或者有代理取消或代理完成的权限，显示下面两个button
            if (strGhid_engr == pmAgent.uid || pmAgent.c_Levels.check_authority(e_Level_service.service_task_cancel) == true)
                myToolBar.fill_fld("取消任务", "Cancel", 0, 0);
            if (strGhid_engr == pmAgent.uid || pmAgent.c_Levels.check_authority(e_Level_service.service_task_complete) == true)
                myToolBar.fill_fld("任务完成", "Complete", 0, 0);

            //myToorBar.fill_fld("重新分配", "ReAssgin", 0, 0);
            myToolBar.btnItemClick = this;// new btnClickEventHandler(myToolBar_btnItemClick);
            myToolBar.fill_toolStrip("plCommand");
        }

        //ONLY FOR IPAD   
        private String Upload_imgFile(String strFld_name)
        {          
            String strReturn = "";
            //未处理代码
         /*   HttpPostedFile fuUpload = Request.Files[strFld_name + "_upload"];
            if (fuUpload != null)
            {
                String strFileName = Path.GetFileName(fuUpload.FileName);
                if (strFileName.length() > 1)
                {
                    boolean bOverWrite = true;
                    String strCategory = "buss";
                    String strLocal_path = myString.Format("{0}\\_App_Media\\{1}\\{2}\\", pmSys.web_phypath, strCategory, DateTime.Now().ToString("yyyy"));
                    if (Directory.Exists(strLocal_path) == false)
                    {
                        Directory.CreateDirectory(strLocal_path);  //可以自动建立子目录
                    }
                    String strFullName = strLocal_path + strFileName;
                    if (File.Exists(strFullName) == true)
                    {
                        if (bOverWrite == false)
                        {
                            String strTemp_name = Path.GetFileNameWithoutExtension(strFullName);
                            String strTemp_ext = Path.GetExtension(strFullName);
                            strFullName = myString.Format("{0}_{1}{2}", strTemp_name, DateTime.Now().ToString("yyyyMMddHHmmss"), strTemp_ext);
                        }
                    }
                    fuUpload.SaveAs(strFullName);
                    strReturn = strFullName.Substring(strFullName.indexOf("\\_App_Media")).replace("\\", "/");
                    myFld.set_item_value(strFld_name, strReturn);
                    fun_main.rem("Upload_imgFile:" + strReturn, e_LogInfo.defaut);
                }
            }*/
            return strReturn;
        }

        public void myToolBar_btnItemClick(Object sender, String name, String parms, int nparms)
        {
            if (name.equals("Return"))
            {
                String strReturn_url =LastPageUrl_additem("history", "1");
                Functions.Redirect(strReturn_url);
            }
            else if (name.equals("Save"))
            {
                //ONLY FOR IPAD   
                if (Functions.isIPad() == true)
                {
                    //Upload_imgFile("SIGNATURE"); //已在签名页面保存了路径
                }

                HashMap htTemp = myFld_Task.Save();
                if (htTemp.containsKey("SIGNATURE") == true) //add by gaoww 20151210 移除签名，由签名页面保存该字段值
                    htTemp.remove("SIGNATURE");
                Functions.ht_SaveEx("ENGR_NAME", myFld_Task.get_item_text("GHID_ENGR"),htTemp);
                my_odbc mydb = new my_odbc(pmSys.conn_crm);
                int nRtn = mydb.my_odbc_update(m_TableName, htTemp, "TASKID='" + pTaskId + "'");
                mydb.my_odbc_disconnect();
                if (nRtn == 1)
                {
                    Functions.MsgBox("保存成功！");
                }
                else
                {
                    Functions.MsgBox("保存失败！");
                }

            }
            else if (name.equals("AddNew"))
            {
                Functions.Redirect(myString.Format("case_task_assign.aspx?caseid={0}&ntype={1}", pCaseId, pType));
            }
            else if (name.equals("Accept") ||name.equals("Refuse")|| name.equals("Cancel") || name.equals("Complete"))
            {
                String strTask_status = "98", strAction = "取消任务";
                if (name.equals("Complete"))
                {
                    strTask_status = "99";
                    strAction = "完成任务";
                }
                else if (name.equals("Accept"))
                {
                    strTask_status = "1";
                    strAction = "签收任务";
                }
                else if (name.equals("Refuse"))
                {
                    strTask_status = "2";
                    strAction = "拒绝任务";
                }
                pTask_Status = strTask_status;
                Save_vs("pTask_Status", pTask_Status);
                //任务状态改为98
                HashMap htTemp = new HashMap();
                Functions.ht_SaveEx("STATUS", strTask_status,htTemp);
                if (name.equals("Accept"))
                    Functions.ht_SaveEx("SDATE_RECV", DateTime.Now().ToString("yyyy-MM-dd HH:mm:ss"),htTemp);
                else if (name.equals("Cancel") || name.equals("Complete"))
                    Functions.ht_SaveEx("EDATE", DateTime.Now().ToString("yyyy-MM-dd HH:mm:ss"),htTemp);
                my_odbc mydb = new my_odbc(pmSys.conn_crm);
                int rc = mydb.my_odbc_update(m_TableName, htTemp, "TASKID='" + pTaskId + "'");
                mydb.my_odbc_disconnect();

                //记录任务轨迹
                service_info myCaseInfo = myCase.GetCaseRecord(pCaseId);
                String strInfo = myString.Format("TABLE={0};CASEID={1};TASKID={2};ORG_CODE={3};ENGR_ORG_CODE={4};STATUS={5};STATUS_TASK={6};PROCESS={7};TASKTYPE={8};SUBMIT_FROM=1;", //modify by gaoww 20151109
                    m_TableName, pCaseId, pTaskId, pmAgent.c_Info.agent_org_code, "", myCaseInfo.nStatus, strTask_status, myCaseInfo.nProcess, myFld_Task.get_item_value("TASKTYPE"));
                fun_service.addnew_trace_log(pType, strAction, "工程师[" + pmAgent.name + "]" + strAction, strInfo);

                //检查是否为主任务，是-修改工单状态
                String strMaster = myFld_Task.get_item_value("TASK_LEVEL") ; //是否为主任务 0-从，1-主
                String strTaskType = myFld_Task.get_item_value("TASKTYPE");

                if (strMaster.equals("0"))
                {
                    String strResult = fun_service.get_case_status_by_task(pType,Integer.toString(myCaseInfo.nProcess), strTaskType, strTask_status);
                    String strStatus = "", strStatus_name = "";
                    if (strResult.equals("")==false)
                    {
                        strStatus = Functions.Substring(strResult, "", "-");
                        strStatus_name = Functions.Substring(strResult, "-", "");
                        Functions.ht_SaveEx("STATUS", strStatus,htTemp);//将工单状态修改为2-已分配
                        int rc1 = myCase.UpdateCaseRecord(pCaseId, htTemp);
                        if (rc1 <= 0) return;
                     }
                }
                myFld_Task.setReload( true);
                myFld_Task.Load(m_TableName, "TASKID='" + pTaskId + "'", pmSys.conn_crm);
                Functions.MsgBox(strAction + "成功");
                InitToolbar();
            }
            else if (name.startsWith("TASK_CUSTOM_") == true) //处理自定义按钮功能
            {
                String strFld_id = Functions.Substring(name, "TASK_CUSTOM_", "");
                DataTable myRow_Tool = dtTools.select("FLD_ID=" + strFld_id);
                if (myRow_Tool.Rows().Count() > 0)
                {
                    String strFld_name = Functions.dtCols_strValue(myRow_Tool,0, "FLD_NAME");
                    String strNote = Functions.dtCols_strValue(myRow_Tool,0, "NOTE"); //进展情况
                    String strAction_task = Functions.dtCols_strValue(myRow_Tool,0, "ACTION_TASK"); //修改任务
                    String strAction_case = Functions.dtCols_strValue(myRow_Tool,0, "ACTION_CASE"); //修改工单
                    if (strAction_task.equals("")==false)//修改任务
                    {
                        my_odbc mydb = new my_odbc(pmSys.conn_crm);
                        //如果正则表达式找到匹配项，则为 true；否则，为 false。
                        if (Regex.IsMatch(strAction_task, "^[\\d]{1,3}$") == true)  //数字 modify by fengw 20151026 [\\d]{1,3} -> ^[\\d]{1,3}$
                        {
                            mydb.my_odbc_set_new();
                            mydb.my_odbc_set("STATUS", strAction_task);
                            mydb.my_odbc_update("SM_TASK_DISP", "TASKID='" + pTaskId + "'");
                        }
                        else if (Regex.IsMatch(strAction_task, "[\\W_]+=([\\d]{1,3})|[DATETIME]|[DATE][TIME]") == true)
                        {
                            if (strAction_task.indexOf("[DATETIME]") > 0) strAction_task = strAction_task.replace("[DATETIME]", "'" + DateTime.Now().ToString("yyyy-MM-dd HH:mm:ss") + "'");
                            if (strAction_task.indexOf("[DATE]") > 0) strAction_task = strAction_task.replace("[DATE]", "'" + DateTime.Now().ToString("yyyy-MM-dd") + "'");
                            if (strAction_task.indexOf("[TIME]") > 0) strAction_task = strAction_task.replace("[TIME]", "'" + DateTime.Now().ToString("HH:mm:ss") + "'");
                            mydb.my_odbc_update("SM_TASK_DISP", strAction_task, "TASKID='" + pTaskId + "'");
                        }
                        else if (Regex.IsMatch(strAction_task, "UPDATE ") == true)
                            mydb.my_odbc_do(strAction_task + " WHERE TASKID='" + pTaskId + "'");
                        mydb.my_odbc_disconnect();
                    }

                    if (myFld_Task.get_item_value("TASK_LEVEL") == "0") //add by gaoww 20161123 主任务才修改服务单状态
                    {
                        if (strAction_case.equals("")==false) //修改工单状态
                        {
                            my_odbc mydb = new my_odbc(pmSys.conn_crm);
                            if (Regex.IsMatch(strAction_case, "^[\\d]{1,3}$") == true)  //数字 modify by fengw 20151026 [\\d]{1,3} -> ^[\\d]{1,3}$
                            {
                                mydb.my_odbc_set_new();
                                mydb.my_odbc_set("STATUS", strAction_case);//modify by fengw 20151022 strAction_task->strAction_case
                                mydb.my_odbc_update(myCase.TableName, "CASEID='" + pCaseId + "'");
                            }
                            else if (Regex.IsMatch(strAction_case, "[\\W_]+=([\\d]{1,3})|[DATETIME]|[DATE][TIME]") == true)
                            {
                                if (strAction_case.indexOf("[DATETIME]") > 0) strAction_case = strAction_case.replace("[DATETIME]", "'" + DateTime.Now().ToString("yyyy-MM-dd HH:mm:ss") + "'");
                                if (strAction_case.indexOf("[DATE]") > 0) strAction_case = strAction_case.replace("[DATE]", "'" + DateTime.Now().ToString("yyyy-MM-dd") + "'");
                                if (strAction_case.indexOf("[TIME]") > 0) strAction_case = strAction_case.replace("[TIME]", "'" + DateTime.Now().ToString("HH:mm:ss") + "'");
                                mydb.my_odbc_update(myCase.TableName, strAction_case, "CASEID='" + pCaseId + "'");
                            }
                            else if (Regex.IsMatch(strAction_case, "UPDATE ") == true)
                            {
                                mydb.my_odbc_do(strAction_case + " WHERE CASEID='" + pCaseId + "'");//modify by fengw 20151022 strAction_task->strAction_case
                            }
                            mydb.my_odbc_disconnect();
                        }
                    }

                    myFld_Task.Load(m_TableName, "TASKID='" + pTaskId + "'", pmSys.conn_crm);
                    service_info myCaseInfo = myCase.GetCaseRecord(pCaseId);

                    if (strNote.equals("")==false) //记录轨迹
                    {
                        String fld_value = "";
                        if (strNote.indexOf("[NAME]") > 0)   //action名称
                            strNote = strNote.replace("[NAME]", "'" + strFld_name + "'");
                        if (strNote.indexOf("[UNAME]") > 0)  //座席姓名
                            strNote = strNote.replace("[UNAME]", "'" + pmAgent.name + "'");
                        if (strNote.indexOf("[TASK]") > 0)   //任务名称
                        {
                            fld_value = myFld_Task.get_item_text("TASKTYPE");
                            strNote = strNote.replace("[TASK]", "'" + fld_value + "'");
                        }
                        if (strNote.indexOf("[PROCESS]") > 0)//环节名称
                        {
                            fld_value = myFld_Task.get_item_text("PROCESS");
                            strNote = strNote.replace("[PROCESS]", "'" + fld_value + "'");
                        }
                        if (strNote.indexOf("[CASE]") > 0)   //工单类型名称
                        {
                            fld_value = myFld_Task.get_item_text("CASETYPE");
                            strNote = strNote.replace("[CASE]", "'" + fld_value + "'");
                        }
                        if (strNote.indexOf("[TEL]") >= 0)
                        {
                            String strSql = myString.Format("SELECT MOBILENO FROM CTS_OPIDK WHERE GHID='{0}'",pmAgent.uid);
                            DataTable dtTemp = Functions.dt_GetTable(strSql, "", pmSys.conn_callthink);
                            strNote = strNote.replace("[TEL]", Functions.dtCols_strValue(dtTemp, "MOBILENO"));
                        }
                        String strTask_Status = myFld_Task.get_item_value("STATUS");
                        //modify by gaoww 20151109
                        String strInfo = myString.Format("TABLE={0};CASEID={1};TASKID={2};ORG_CODE={3};STATUS={4};STATUS_TASK={5};SUBMIT_FROM=1;", myCase.TableName, pCaseId, pTaskId, pmAgent.c_Info.agent_org_code, myCaseInfo.nStatus, strTask_Status);
                        fun_service.addnew_trace_log(pType, strFld_name, strNote, strInfo);
                    }
                }
            }
        }

        //填充任务资料内容
        private void Fill_Case_Default()
        {
             my_odbc mydb = new my_odbc(pmSys.conn_crm);
             if (pOp.equals("Edit"))
             {
                 String strSql = myString.Format("SELECT * FROM {0} WHERE TASKID='{1}'", m_TableName, pTaskId);
                 //直接从服务单列表页面跳转过来
                 if (pFrom.equals("list"))
                 {
                     strSql = myString.Format("SELECT * FROM {0} WHERE CASEID='{1}'", m_TableName, pCaseId);
                 }
                 else
                 {
                     strSql = myString.Format("SELECT * FROM {0} WHERE TASKID='{1}'", m_TableName, pTaskId);
                 }
                 HashMap ht = new HashMap();
                 pmMap res =mydb.my_odbc_find(strSql,true); ht = res.htRet;
                 int nRtn = res.nRet;
                 mydb.my_odbc_disconnect();
                 if (nRtn == 1)
                 {
                     myFld_Task.Load(ht);
                     pTask_Status = myFld_Task.get_item_value("STATUS");
                     Save_vs("pTask_Status", pTask_Status);
                 }
                 else
                 {
                     return;
                 }
             }
            //新建任务显示默认值
             if (pOp.equals("AddNew"))
             {
                
             }
        }
    }

