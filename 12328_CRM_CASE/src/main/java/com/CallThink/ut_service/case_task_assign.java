///########################################################################################
/// Copyright (C) 2000, ToneThink.Soft  All Rights Reserved. 
///#########################################################################################
/// 文件创建时间：2015-09-07
///   文件创建人：gaoww
/// 文件功能描述：任务工单新建页面
///     调用格式：
///     
///     维护记录：
/// 2015.09.07：create by gaoww         
///#########################################################################################
package com.CallThink.ut_service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import org.springframework.ui.Model;
import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.pmSys;
import com.CallThink.base.pmClass.pmAgent_info;
import com.CallThink.base.pmCode.UltraCRM_Page;
import com.CallThink.base.support.pmInfo;
import com.CallThink.cc_softcall.ATClient_webs;
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
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.fun_json;
import com.ToneThink.ctsTools.myUtility.myString;
import com.ToneThink.ctsTools.WebUI.Fld_attr;
import com.ToneThink.ctsTools.WebUI.my_Field;
import com.ToneThink.ctsTools.WebUI.my_ToolStrip;
import com.ToneThink.ctsTools.WebUI.my_dataGrid;

    public class case_task_assign extends UltraCRM_Page
    {
        private String pOp = "";
        private String pCaseId = ""; //服务单工单编号
        private String pTaskId = ""; //任务单编号
        private String pFilter = "";

        private String m_TableName = "CRM_CASE"; //服务单工单表名
        private String m_TableName_Task = "SM_TASK_DISP"; //任务单表名
        private String m_TableName_Skill = "SM_BUSS_GHID_SKILL"; //工程师技能表
        private String m_TableName_RealTime = "SM_GHID_REALTIME";//工程师APP实时状态表
        DataTable pDataTable = new DataTable();
        private int pType = 1; //服务单类型

        service_set_info myCase;
        service_info myCaseInfo = new service_info();

        pmAgent_info pmAgent;

        //服务单信息
        my_ToolStrip myToolBar = new my_ToolStrip();
        my_Field myFld = new my_Field(3);

        //任务分配
        my_ToolStrip myToolBar_Task = new my_ToolStrip();
        //my_SearchField myFld_Task = new my_SearchField(3);
        my_Field myFld_Task = new my_Field(3);
        my_dataGrid mydg = new my_dataGrid(51);

        DataTable dtPlan; //预案算法表
        private int pAssign_Type = 0; //任务分派类型 0-服务机构，1-工程师（用来判断rdo_Type选择是否变化，如果使用rdo_Type的select事件，会在回调后执行Fillin_field_Task函数后，仍然变回服务机构选中）
        int rdo_Type_SelectedIndex=-1;
        String m_divGridStyle="";
        String m_divgbTask="";
        public void Page_Load(Object sender, Model model)
        {
            pmAgent = fun_main.GetParm();
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
                    pTaskId = Functions.ht_Get_strValue("taskid", htQuery);
                }
                Save_vs("pOp", pOp);
                Save_vs("pType", pType);
                Save_vs("pCaseId", pCaseId);
                Save_vs("pTaskId", pTaskId);
                Save_vs("pFilter", pFilter);
            }
            else
            {
                pOp = Load_vs("pOp");
                pType = Functions.atoi(Load_vs("pType"));
                pCaseId = Load_vs("pCaseId");
                pTaskId = Load_vs("pTaskId");
                pFilter = Load_vs("pFilter");
                pDataTable = Load_vs("pDataTable",DataTable.class);
                dtPlan = Load_vs("dtPlan",DataTable.class); //add by gaoww 20151109 增加自定义button功能
                pAssign_Type = Functions.atoi(Load_vs("pAssign_Type"));
            }
            myCase = new service_set_info(pType);
            m_TableName = myCase.TableName;
            myCaseInfo = myCase.GetCaseRecord(pCaseId);
            
            //add by gaoww 20151110 增加预案表信息读取
            if (IsPostBack == false)//首次加载和访问
            {
                String strSql = "SELECT * FROM SM_DISPATCH_PLAN WHERE ISVALID='1' ORDER BY WT_ORDER DESC";
                my_odbc pTable = new my_odbc(pmSys.conn_crm);
                pmList res =pTable.my_odbc_find(strSql,0); dtPlan = res.dtRet;
                int rc = res.nRet;
                pTable.my_odbc_disconnect();
                Save_vs("dtPlan", dtPlan);
                //modify by gaoww 20151120
                if (pmAgent.c_Info.agent_org_level == 3) //如果是第三级网点，则只能按工程师分配
                {
                    //rdo_Type.Enabled = false; //delete by gaoww 20151120 解决如果设为不可用，点了选取后，SelectedValue会变为0
                    //rdo_Type.SelectedValue = "1";
                	rdo_Type_SelectedIndex=1;
                }
                else
                {
                    //rdo_Type.SelectedValue = "0";
                	rdo_Type_SelectedIndex=0;
                }
            }
            
            Fillin_field_case();
            Fillin_field_Task();
            Fill_withCaseid(pCaseId);
        	//rdo_Type_SelectedIndex=1;//--------------------------------duzy---------------test20170509
            if (!IsPostBack)
            {
                //pDataTable = GetEng();//默认加载符合品类预案的工程师信息
               // if (rdo_Type.SelectedValue.equals("0")) //add by gaoww 20151120 如果默认进来是工程师，则自动刷新列表
            	if(rdo_Type_SelectedIndex==0)
                {
                    String[][] strFields = new String[][]  {{"BUSS_ID","System.String",""},
                                               {"GHID","System.String",""},
                                               {"OP_NAME","System.String",""},
                                               {"ORG_CODE","System.String",""},
                                               {"SKILL_LEVEL","System.String",""},
                                               {"SKILL_LEVEL1","System.String",""},
                                               {"BTYPE","System.String",""},
                                               {"POS_X","System.String",""},
                                               {"POS_Y","System.String",""},
                                               {"MAP","System.String",""},};

                    pDataTable = Functions.dt_CreateTable_stru(m_TableName_Skill, strFields);
                }
                else
                {
                    pDataTable = GetEng();
                    if (pDataTable.getCount() > 0)
                    {
                        String strGhid = Functions.dtCols_strValue(pDataTable, 0, "GHID");
                        String strName = Functions.dtCols_strValue(pDataTable, 0, "OP_NAME");
                        myFld_Task.set_item_value("GHID", strName + "(" + strGhid + ")");
                    }
                }
                Save_vs("pDataTable", pDataTable);
            }
            else
            {
            	pDataTable=Load_vs("pDataTable",DataTable.class);
            	rdo_Type_SelectedIndex=Functions.atoi( myFld_Task.get_item_value("TYPE"));
            }
            
        InitToolbar();//放在后面的原因是toolbar中要rdo_Type_SelectedIndex进行判断,否则会有问题
        Init_Data();
        Fillin_grid();
        myToolBar.render(model);
        myFld.render(model);
        myToolBar_Task.render(model);
        myFld_Task.render(model);
        mydg.render(model);
}

        private void Init_Data()
        {
            if (rdo_Type_SelectedIndex == 0)
            {
                //divGrid.Visible = false;
                m_divGridStyle="display:none;";
            }
            else
            {
                //divGrid.Visible = true;
            	 m_divGridStyle="display:block;";
            }
        }

        private void Fillin_field_case()
        {
            myFld.SetConnStr(pmSys.conn_crm);
            myFld.SetTable(m_TableName);
            myFld.SetLabelAlign("Right");
            myFld.SetMaxLabelLenth(100);
            myFld.funName_OnClientClick("myFld_FieldLinkClicked");
            myFld.fill_fld("客户名称", "UNAME", 25, 0, false);
            myFld.fill_fld("联系电话", "TEL", 25, 0, false);
            myFld.fill_fld("受理时间", "SDATE", 25, 5, false,false,"","Date");
            myFld.fill_fld("工单类型", "CASETYPE", 25, 1, false);
            myFld.set_list("CASETYPE", "SELECT CASE_NAME,CASETYPE FROM CRM_CASE_TABLE", "CASETYPE,CASE_NAME", pmSys.conn_crm);
            myFld.fill_fld("工单状态", "STATUS", 25, 1, false);
            myFld.set_list("STATUS", "SELECT STATUS_NAME AS STATUS_NAME,STATUS_ID AS STATUS FROM CRM_CASE_STATUS WHERE CASETYPE='" + pType + "'", "STATUS,STATUS_NAME", pmSys.conn_crm);
            String fld_name = "";
            pmRef<String> pmRefValue=new pmRef<String>(fld_name);
            int nResult = get_desc_Field("SVC_CATEGORY",pmRefValue);
            fld_name= pmRefValue.oRet;
            if (nResult == 1)
            {
                myFld.fill_fld(fld_name, "SVC_CATEGORY", 25, 1, false, true);
                myFld.set_list("SVC_CATEGORY", "SELECT FLD_ID AS SVC_CATEGORY,FLD_NAME AS FLD_NAME  FROM  DICT_SVC_CATEGORY", "SVC_CATEGORY,FLD_NAME", pmSys.conn_crm);
            }

            pmRefValue=new pmRef<String>(fld_name);
            nResult = get_desc_Field("SVC_CATEGORY2", pmRefValue);
            fld_name= pmRefValue.oRet;
            if (nResult == 1)
            {
                myFld.fill_fld(fld_name, "SVC_CATEGORY2", 25, 1, false);
                myFld.set_list("SVC_CATEGORY2", "SELECT FLD_ID AS SVC_CATEGORY2,FLD_NAME AS FLD_NAME  FROM  DICT_SVC_CATEGORY2", "SVC_CATEGORY2,FLD_NAME", pmSys.conn_crm);
            }

            pmRefValue=new pmRef<String>(fld_name);
            nResult = get_desc_Field("SVC_MODE", pmRefValue);
            fld_name= pmRefValue.oRet;
            if (nResult == 1)
            {
                myFld.fill_fld(fld_name, "SVC_MODE", 25, 1, false);
                myFld.set_list("SVC_MODE", "SELECT FLD_ID AS SVC_MODE,FLD_NAME AS FLD_NAME FROM DICT_SVC_MODE", "SVC_MODE,FLD_NAME", pmSys.conn_crm);
            }
            
            pmRefValue=new pmRef<String>(fld_name);
            nResult = get_desc_Field("PO_CATEGORY", pmRefValue);
            fld_name= pmRefValue.oRet;
            if (nResult == 1)
            {
                myFld.fill_fld(fld_name, "PO_CATEGORY", 25, 1,false,true);
                myFld.set_list("PO_CATEGORY", "SELECT FLD_ID,FLD_NAME FROM DICT_PO_CATEGORY", "FLD_ID,FLD_NAME", pmSys.conn_crm);
            }

            pmRefValue=new pmRef<String>(fld_name);
            nResult = get_desc_Field("PO_CATEGORY2",pmRefValue);
            fld_name= pmRefValue.oRet;
            if (nResult == 1)
            {
                myFld.fill_fld(fld_name, "PO_CATEGORY2", 25, 1, false);
                myFld.set_list("PO_CATEGORY2", "SELECT FLD_ID,FLD_NAME FROM DICT_PO_CATEGORY2", "FLD_ID,FLD_NAME", pmSys.conn_crm);
            }

            myFld.fill_fld("地址", "ADDRESS", 92, 12,true,true);
            myFld.fill_fld("工单编号", "CASEID", 0);

            myFld.fill_Panel("gbCase");
        }

        private void Fillin_field_Task()
        {
            myFld_Task.SetMaxLabelLenth(100);
            myFld_Task.funName_OnClientClick("myFld_FieldLinkClicked");
            //myFld_Task.fill_fld("分配目标", "TYPE", 10, 2);
            //myFld_Task.set_list("TYPE", "服务机构,工程师(1)");
            myFld_Task.fill_fld("分配目标", "TYPE", 10, 2);
            myFld_Task.set_list("TYPE", new String[] {"0,服务机构","1,工程师"});
            if (IsPostBack==false) {
				myFld_Task.set_item_value("TYPE", "0");
			}
            //myFld_Task.set_list("TYPE", "服务机构,工程师(1)");
            myFld_Task.fill_fld("分配人员", "GHID", 25);
            myFld_Task.fill_fld("环节", "PROCESS", 0);

            myFld_Task.fill_fld("任务级别", "TASK_LEVEL", 2, 2, true, true, "0");
            myFld_Task.set_list("TASK_LEVEL", new String[] { "0,主任务(1)", "1,从任务" });
            myFld_Task.fill_fld("任务类型", "TASKTYPE", 25, 1, true, false, "1");
            myFld_Task.set_list("TASKTYPE", "SELECT FLD_ID AS FLD_ID,FLD_NAME AS FLD_NAME FROM  DICT_TASK_TYPE WHERE CASETYPE ='"+pType +"' AND PROCESS='"+myCaseInfo.nProcess +"'", "FLD_ID,FLD_NAME", pmSys.conn_crm);
            myFld_Task.fill_fld("预约时间", "SDATE_NOTE", 25, 5, true, false, "", "yyyy-MM-dd HH:mm");
            myFld_Task.fill_fld("", "btnRdo_Type", 20,7);//此按键是用于模拟radio回发服务器的按键
            
            String fld_name = "";
            pmRef<String> pmRefValue=new pmRef<String>(fld_name);
            int nResult = get_desc_Field("ORG_CODE1", pmRefValue);
            fld_name= pmRefValue.oRet;
            if (nResult == 0)
                fld_name = "服务商总部";
            myFld_Task.fill_fld(fld_name, "ORG_CODE1", 25, 1, true, true);
            myFld_Task.set_list("ORG_CODE1", "SELECT ORG_CODE AS ORG_CODE1,ORG_NAME AS ORG_NAME FROM DICT_ORG_CODE WHERE ORG_LEVEL=1", "ORG_CODE1,ORG_NAME", pmSys.conn_crm);
            //myFld_Task.set_item_relation("ORG_CODE1", "ORG_CODE2", "SELECT ORG_CODE AS  ORG_CODE2,ORG_NAME  AS ORG_NAME   FROM  DICT_ORG_CODE WHERE (ORG_CODE like left('[ORG_CODE1].Value',6)+ '______' ) AND ORG_LEVEL=2", pmSys.conn_crm, 1);
            myFld_Task.set_item_relation("ORG_CODE1", "ORG_CODE2", "SELECT ORG_CODE AS  ORG_CODE2,ORG_NAME  AS ORG_NAME   FROM  DICT_ORG_CODE WHERE REL_CODE ='[ORG_CODE1].Value' AND ORG_LEVEL=2", pmSys.conn_crm, 1); //modify by gaoww 20161009 解决mysql数据库不支持left语法的问题

            pmRefValue=new pmRef<String>(fld_name);
            nResult = get_desc_Field("ORG_CODE2", pmRefValue);
            fld_name= pmRefValue.oRet;
            if (nResult == 0)
                fld_name = "服务商分公司";
            myFld_Task.fill_fld(fld_name, "ORG_CODE2", 25, 1);
            myFld_Task.set_item_relation("ORG_CODE2", "ORG_CODE3", "SELECT ORG_CODE AS ORG_CODE3,ORG_NAME  AS ORG_NAME   FROM  DICT_ORG_CODE  WHERE REL_CODE='[ORG_CODE2].Value'  AND ORG_LEVEL=3", pmSys.conn_crm, 1);

            pmRefValue=new pmRef<String>(fld_name);
            nResult = get_desc_Field("ORG_CODE3", pmRefValue);
            fld_name= pmRefValue.oRet;
            if (nResult == 0)
                fld_name = "服务商网点";
            myFld_Task.fill_fld(fld_name, "ORG_CODE3", 25, 1);

            myFld_Task.fill_fld("任务单编号", "TASKID", 0);
            int nTemp = rdo_Type_SelectedIndex;//add by gaoww 20151118 解决选中后又回到默认选中的情况
            myFld_Task.fill_Panel("gbTask");
            rdo_Type_SelectedIndex = nTemp;//add by gaoww 20151118

            myFld_Task.FieldLinkClicked = this;// new FieldLinkClickedEventHandler(myFld_Task_FieldLinkClicked);
            if (rdo_Type_SelectedIndex == 0)
            {
                myFld_Task.set_readonly("GHID");
                //myFld_Task.set_readonly("TASK_LEVEL"); //delete by gaoww 20151120 服务机构和工程师，都可以选主从任务
                myFld_Task.set_readonly("TASKTYPE");
                // myFld_Task.set_readonly("SDATE_NOTE");    
            }
            else
            {
                myFld_Task.set_readonly("GHID", false);
                //myFld_Task.set_readonly("TASK_LEVEL", false);//delete by gaoww 20151120 服务机构和工程师，都可以选
                myFld_Task.set_readonly("TASKTYPE", false);
                //myFld_Task.set_readonly("SDATE_NOTE", false);
            }
        }

        /// <summary>
        /// 从表单描述表中动态读取服务单相关字段描述，以及是否显示
        /// </summary>        
        /// <param name="fld_name">字段名称</param>
        /// <param name="strFld_desc">字段描述</param>
        /// <returns>0-不显示，1-显示</returns>
        private int get_desc_Field(String fld_value,pmRef<String> pmRefValue)
        {
            int nReturn = 0;
            pmRefValue.oRet = "";
            String strKey_kvdb = "dtCaseTask_edit_" + pType;
            DataTable dtStru = pmInfo.myKvdb.Get(strKey_kvdb,null);// as DataTable;
            if (dtStru == null)
            {
                dtStru = fun_Form.get_desc_data(myCase.DescName, "((FLD_VLEVELS&1)<>0) ORDER BY EDIT_INDEX");
                pmInfo.myKvdb.Setex(strKey_kvdb, dtStru, 60); //60秒
            }
 
            DataTable myRow = dtStru.select("FLD_VALUE='" + fld_value + "'");
            if (myRow.Rows().Count() > 0)
            {
            	 pmRefValue.oRet = Functions.drCols_strValue(myRow.Rows().get(0),"FLD_NAME");
                nReturn = 1;
            }
            return nReturn;
        }

        @Override
		public void myFld_FieldLinkClicked(Object sender, String name, String parms, int nparms, int ntype) {
        	if (name.equals("btnRdo_Type")) {
        		rdo_Type_SelectedIndexChanged();
			}
		}


        public void Fill_withCaseid(String strCaseId)
        {
            DataTable dtRet, dtRet_task = null;
            my_odbc pTable = new my_odbc(pmSys.conn_crm);
            pmList res =pTable.my_odbc_find(m_TableName, "CASEID = '" + strCaseId + "'",0); dtRet = res.dtRet;
            int rc = res.nRet;
            if (pOp.equals("Edit")) //add by gaoww 20151120
            {
                 res =pTable.my_odbc_find(m_TableName_Task, "TASKID = '" + pTaskId + "'",0); dtRet_task = res.dtRet;
            }
            pTable.my_odbc_disconnect();
            if (rc <= 0) return;
            myFld.Load(dtRet);

             //将工单中的默认信息添加到任务信息中
            String strOrg_Code = Functions.dtCols_strValue(dtRet, "ORG_CODE_TASK");
            if (pOp.equals("Edit")) //如果是edit，则从任务表中读取维修工程师机构代码
            {
                strOrg_Code = Functions.dtCols_strValue(dtRet_task, "ENGR_ORG_CODE");
            }
            if (strOrg_Code.equals("")||strOrg_Code.length() !=12) //如果服务单中没有服务商机构代码，则使用当前座席默认的机构代码
                strOrg_Code = pmAgent.c_Info.agent_org_code;
            String strOrg_Code1= Functions.Substring(strOrg_Code,0,6)+"000000";
            String strOrg_Code2 = "", strOrg_Code3 = "";
            if (myString.TrimEnd(strOrg_Code, '0').length() ==12)
            {
                strOrg_Code3 = strOrg_Code;   
                strOrg_Code2 =Functions.Substring(strOrg_Code,0,8)+"0000";
            }
            else if (myString.TrimEnd(strOrg_Code,'0').length() == 8)
            {
                strOrg_Code2 = Functions.Substring(strOrg_Code, 0, 8) + "0000";
            }
            if (strOrg_Code1.equals("")==false)
            {
                myFld_Task.set_item_value("ORG_CODE1", strOrg_Code1);
                if (pmAgent.c_Info.agent_org_level >= 1)
                {
                    myFld_Task.set_readonly("ORG_CODE1", true);
                }
            }
            if (strOrg_Code2.equals("")==false)
            {
                myFld_Task.set_item_value("ORG_CODE2", strOrg_Code2);
                if (pmAgent.c_Info.agent_org_level >= 2)
                {
                    myFld_Task.set_readonly("ORG_CODE2", true);
                }
            }
            if (strOrg_Code3.equals("")==false)
            {
                myFld_Task.set_item_value("ORG_CODE3", strOrg_Code3);
                if (pmAgent.c_Info.agent_org_level ==3)
                {
                    myFld_Task.set_readonly("ORG_CODE3", true);
                }
            }

            if (pOp.equals("Edit")==false)
            {
                String fld_value = Functions.dtCols_strValue(dtRet, "SDATE_NOTE");
                if (fld_value.equals("")==false)
                    myFld_Task.set_item_value("SDATE_NOTE", fld_value);

                fld_value = Functions.dtCols_strValue(dtRet, "PROCESS");
                if (fld_value.equals("")==false)
                    myFld_Task.set_item_value("PROCESS", fld_value);
            }
            else
                myFld_Task.Load(dtRet_task);
        }



        private void Fillin_grid()
        {
            //mydg.SetTable = "SM_BUSS_GHID_SKILL";
            mydg.SetPageSize(pmAgent.page_maxline - 5);
            mydg.SetPagerMode(2);
            int i = 0;
            mydg.fill_fld(i++, "选择", "SELECT", 5, 9, "radio");
            mydg.fill_fld(i++, "品类、细类代码", "BUSS_ID", 0);
            mydg.fill_fld(i++, "工程师编号", "GHID", 10);
            //mydg.fill_fld(i++, "工程师姓名", "OP_NAME", 15, 8, "CMDNAME=Link;NULLAS=[null]");
            mydg.fill_fld(i++, "工程师姓名", "OP_NAME", 15, 0);
            mydg.fill_fld(i++, "所属服务机构", "ORG_CODE", -1, 1);
            //mydg.set_cols_cbo_list("ORG_CODE", "SELECT ORG_CODE,ORG_NAME  FROM  DICT_ORG_CODE", "ORG_CODE,ORG_NAME", pmSys.conn_crm); //没设表名所以不能用set_cols_cbo_list
            mydg.fill_fld(i++, "主观技能水平", "SKILL_LEVEL", 15);
            mydg.fill_fld(i++, "客观技能水平", "SKILL_LEVEL1", 15);
            mydg.fill_fld(i++, "类型", "BTYPE", -1, 1);
            //mydg.set_cols_cbo_list("BTYPE", "产品类型,服务类型");
            mydg.fill_fld(i++, "X坐标", "POS_X", 0);
            mydg.fill_fld(i++, "Y坐标", "POS_Y", 0);
            //mydg.fill_fld(i++, "地图", "MAP", 8, 8, "CMDNAME=Link;TEXT=<img border=0 src='" + Session["themeURL"] + "images/icon/map.png'>");
            mydg.fill_fld(i++, "位置", "MAP", 8, 8);
            mydg.CellLinkClicked = this;// new CellLinkClickedEventHandler(dataGrid_CellLinkClicked);
            mydg.RowDataFilled = this;// new RowDataFilledEventHandler(mydg_RowDataFilled);
            mydg.fill_header("dgvCase", pDataTable);
        }

        public void mydg_RowDataFilled(Object sender, int rows)
        {
            if (rows < 0) return;
            String strGhid = mydg.get_cell(rows, "GHID");
            String strName = mydg.get_cell(rows, "OP_NAME");
            String strOrgCode = mydg.get_cell(rows, "ORG_CODE");
            int cols = mydg.get_idx("OP_NAME");
            if (cols > 0) //填充分配机构/人员  add  by lics 20150914
            {
                String strHtml = myString.Format("<label>{0}</label><label style='display:none'>#{0}({1})</label>", strName, strGhid);
                mydg.set_cell(rows,cols,strHtml);
            }
            cols = mydg.get_idx("ORG_CODE");
            if (cols > 0)//机构代码替换成机构名称
            {
                DataTable dtTemp = Functions.dt_GetTable("SELECT ORG_NAME  FROM  DICT_ORG_CODE WHERE ORG_CODE='" + strOrgCode + "'", "", pmSys.conn_crm);
                if (dtTemp.getCount() <= 0) return;
                String strOrgName = Functions.drCols_strValue(dtTemp.Rows().get(0) ,"ORG_NAME");
                String strHtml = myString.Format("<label>{0}</label>", strOrgName);
                mydg.set_cell(rows,cols,strHtml);
            }
            cols = mydg.get_idx("MAP");
            if (cols > 0)
            {
                String strPosx = mydg.get_cell(rows, "POS_X");
                String strPosy = mydg.get_cell(rows, "POS_Y");
                String strNewUrl = myString.Format("/ut_service/tools/map.aspx?cmd=posxy&pos_x={0}&pos_y={1}", strPosx, strPosy);
                String strHtml = myString.Format("<a href=\"javascript:Add_MainTab('{0}','{1}');\">{2}</a>", "工程师位置", strNewUrl, fun_main.CreateHtml_img("map.png", ""));
                mydg.set_cell(rows,cols,strHtml);
            }
            cols = mydg.get_idx("BTYPE");
            if (cols > 0)//BTYPE=0 对应 0-产品类型  BTYPE>0对应0-服务类型 add by lics 20150921
            {
                int nBtype = Functions.atoi(mydg.get_cell(rows, "BTYPE"));
                String strCell = "";
                if (nBtype == 0)
                {
                    strCell = "产品类型";
                }
                else if (nBtype > 0)
                {
                    strCell = "服务类型";
                }
                mydg.set_cell(rows,cols,strCell);
            }
        }
        public void mydg_CellLinkClicked(Object sender, String text, int rows, int cols)
        {
            if (mydg.RowCount() <= 0) return;

        }


        private void InitToolbar()
        {
            if (((LastPageUrl().equals("") ==false) && (LastPageUrl().contains("web_desk/desktop_im.aspx") == false))&&(LastPageUrl().contains("case_task_assign.aspx") == false))// && pId_dlg.equals(""))
                myToolBar.fill_fld(fun_main.Term("LBL_Return"), "Return");
            myToolBar.fill_fld("原始服务单", "View_case", 0, 10);
            myToolBar.fill_fld("回拨", "Callback", 0, 10);
            myToolBar.fill_toolStrip("plCommand");
            myToolBar.btnItemClick = this;// new btnClickEventHandler(myToolBar_btnItemClick);
            if (pmAgent.c_Levels.check_authority(e_Level_service.service_dds) == true)
                myToolBar_Task.fill_fld("新建任务", "AddNew", 0, 10);
            if (!IsPostBack)
            {
                myToolBar_Task.set_readonly("AddNew", true);
            }

           // if (rdo_Type_SelectedIndex == 1)
            {
                myToolBar_Task.fill_fld("Separator", "Separator0", 0, 3);
                myToolBar_Task.fill_fld("选择预案", "Select_plan", 25, 14, "请选择预案");
                //myToorBar_Task.set_list("Select_plan", "品类,服务类型");
                LinkedHashMap<String, String> tmpTable = new LinkedHashMap<String, String>(); //modify by gaoww 20151110 改为使用从预案表中动态读取预案
            //String strSelectedValue="";
                for (int rows = 0; rows < dtPlan.getCount(); rows++)
                {
                    String strPlan_Id = Functions.dtCols_strValue(dtPlan, rows, "PLANID");
                    String strPlan_Name = Functions.dtCols_strValue(dtPlan, rows, "PLAN_NAME");
                    tmpTable.put(strPlan_Id, strPlan_Name);
                   // if(rows==0) strSelectedValue=strPlan_Id;
                }
                myToolBar_Task.set_list("Select_plan", tmpTable);
              //目前下拉框类型14,设定选中值无效
                //myToolBar_Task.set_item_attr("Select_plan", Fld_attr.Fld_index, "1");
                myToolBar_Task.fill_fld("选取", "Choose", 0, 10);
            }
            myToolBar_Task.fill_fld("Separator", "Separator1", 0, 3);
            myToolBar_Task.fill_fld("提交任务", "Complete", 0, 0);
            myToolBar_Task.fill_toolStrip("plCommand_task");
            myToolBar_Task.btnItemClick = this;// new btnClickEventHandler(myToolBar_btnItemClick);

        }

        public void myToolBar_btnItemClick(Object sender, String name, String parms, int nparms)
        {
            if (name.equals("AddNew")) //原始服务单
            {
                Functions.Redirect(pmSys.rootURL+"/ut_service/case_task_assign.aspx?" + Request.getQueryString());
            }
            else if (name.equals("Choose"))//选取预案
            {
                String strOrg_Code1 = myFld_Task.get_item_value("ORG_CODE1");
                if (strOrg_Code1.equals(""))
                {
                    Functions.MsgBox("请选择要分配的服务机构");
                    return;
                }
                //string strIndex = myToorBar_Task.get_item_cbo("Select_plan").SelectedIndex.ToString();
                DataTable dt = GetEng();
                pDataTable = dt;
                Save_vs("pDataTable", pDataTable);
                mydg.refresh(dt);
                if (pDataTable.getCount() > 0)
                {
                    String strGhid = Functions.dtCols_strValue(pDataTable, 0, "GHID");
                    String strName = Functions.dtCols_strValue(pDataTable, 0, "OP_NAME");
                    myFld_Task.set_item_value("GHID", strName + "(" + strGhid + ")");
                }
            }
            else if ((name.equals("Return")))  //2008.08 增加完成，返回页面后，重新刷新列表页面
            {
                String strReturn_url = LastPageUrl_additem("history", "1");
                Functions.Redirect(strReturn_url);
            }
            else if (name.equals("Complete"))
            {
                String strOrg_Code1 = myFld_Task.get_item_value("ORG_CODE1");
                String strOrg_Code2 = myFld_Task.get_item_value("ORG_CODE2");
                String strOrg_Code3 = myFld_Task.get_item_value("ORG_CODE3");
                String strMaster = myFld_Task.get_item_value("TASK_LEVEL"); ; //是否为主任务 0-主，1-从
                String strTaskType = myFld_Task.get_item_value("TASKTYPE");   //任务类型
                String strTask_Org_level = "1";                                    //分配机构级别 1-总部 2-分部 3-网点 10-工程师 
                String strOrg_Name_eng = myFld_Task.get_item_text("ORG_CODE1");//选中分配的最后一级机构名称
                String strOrg_Code_eng = strOrg_Code1;                         //选中分配的最后一级机构代码
                if (strOrg_Code1.equals(""))
                {
                    Functions.MsgBox("请选择要分配的服务机构");
                    return;
                }    
                if (strTaskType.equals(""))
                {
                    Functions.MsgBox("请选择任务类型！");
                    return;
                }
                if (strMaster.equals("0")) //检查是否已经有主任务
                {
                    String strSql = myString.Format("SELECT STATUS FROM SM_TASK_DISP WHERE CASEID='{0}' AND PROCESS='{1}' AND TASK_LEVEL='0' AND STATUS<'98'", pCaseId, myCaseInfo.nProcess); //只查当前环节的主任务，从任务不用管
                    if (pTaskId.equals("")==false) //查主任务时不查本任务
                        strSql += " AND TASKID<>'" + pTaskId + "'";
                    my_odbc pTable = new my_odbc(pmSys.conn_crm);
                    int rc = pTable.my_odbc_find(strSql);
                    pTable.my_odbc_disconnect();
                    if (rc > 0)
                    {
                        Functions.MsgBox("当前服务单已有主任务，不能重复增加，请重新选择任务类型！");
                        return;
                    }
                }
                //验证是否填写分配机构/人员
                String strGhid = myFld_Task.get_item_value("GHID").trim();
                String strGhid_eng = "";//工程师工号
                String strName_eng = "";//工程师姓名                    
                if (rdo_Type_SelectedIndex == 1) //分配任务到工程师
                {
                    strTask_Org_level = "10";
                    if (strGhid.trim().length() == 0)
                    {
                        Functions.MsgBox("请填写分配机构/人员！");
                        return;
                    }
                    HashMap htInfo=new HashMap();
                    pmRef<HashMap> pmRefValue=new pmRef<HashMap>(htInfo);
                    if (Get_Engr_Info(strGhid, pmRefValue) == true)
                    {
                    	htInfo=pmRefValue.oRet;
                    	
                        strGhid_eng = Functions.ht_Get_strValue("GHID_ENG", htInfo);
                        strName_eng = Functions.ht_Get_strValue("NAME_ENG", htInfo);
                        strOrg_Name_eng = Functions.ht_Get_strValue("ORG_NAME_ENG", htInfo);
                        strOrg_Code_eng = Functions.ht_Get_strValue("ORG_CODE_ENG", htInfo);                        
                    }
                    else
                    {
                        Functions.MsgBox("目标分配人员不存在，请重新选择！");
                        return;
                    }
                }
                else
                {
                    if (strOrg_Code3.equals("")==false)
                    {
                        strOrg_Name_eng = myFld_Task.get_item_text("ORG_CODE3");
                        strOrg_Code_eng = strOrg_Code3;
                        strTask_Org_level = "3";
                    }
                    else if (strOrg_Code2.equals("")==false)
                    {
                        strOrg_Name_eng = myFld_Task.get_item_text("ORG_CODE2");
                        strOrg_Code_eng = strOrg_Code2;
                        strTask_Org_level = "2";
                    }
                }
                //先创建任务单
                String strTaskid = "";
                pmRef<String> pmRefValue=new pmRef<String>(strTaskid);
                boolean isBool = Save_Task(strGhid_eng, strName_eng,strOrg_Code_eng,strOrg_Name_eng, rdo_Type_SelectedIndex,pmRefValue);
                strTaskid =pmRefValue.oRet;
                //如果生成任务单成功设置提交按钮为只读
                if (isBool)
                {
                    myToolBar.set_readonly("Choose", true);
                    myToolBar_Task.set_readonly("Complete", true);
                    //gbTask.Enabled = false;
                    m_divgbTask="disabled=\"disabled\"";
                }
                else return;
               
                HashMap htTemp = new HashMap();
                if (strMaster.equals("0")) //主任务单,修改服务单
                {
                    //myFld.IsReload = true;
                    //保存用户可能修改的内容
                    Functions.ht_SaveEx("SDATE_NOTE", myFld.get_item_value("SDATE_NOTE"),htTemp);
                    Functions.ht_SaveEx("SDATE_SEND", DateTime.Now().ToString("yyyy-MM-dd HH:mm:ss"),htTemp); //add by gaoww 20151102
                    Functions.ht_SaveEx("ADDRESS", myFld.get_item_value("ADDRESS"),htTemp);
                    String strResult = fun_service.get_case_status_by_task(pType,Integer.toString(myCaseInfo.nProcess) , strTaskType, "0");
                    String strStatus = "", strStatus_name = "";
                    if (strResult.equals("")==false)
                    {
                        strStatus = Functions.Substring(strResult, "", "-");
                        strStatus_name = Functions.Substring(strResult, "-", "");
                        Functions.ht_SaveEx("STATUS", strStatus,htTemp);//将工单状态修改为2-已分配                            
                    }
                    Functions.ht_SaveEx("ORG_CODE_TASK", strOrg_Code_eng,htTemp);
                    Functions.ht_SaveEx("TASK_LEVEL", strTask_Org_level,htTemp);
                }               

                //add by gaoww 20160113 增加向最后一级机构调度人员发送通知消息
                if (rdo_Type_SelectedIndex == 0) //分配任务到服务机构，选择发送目标
                {
                    //查出机构下属工号
                    DataTable dtGhid = new DataTable();
                    my_odbc pTable_org = new my_odbc(pmSys.conn_crm);
                    pmList res =pTable_org.my_odbc_find("DICT_ORG_GHID_REAL", "ORG_CODE='" + strOrg_Code_eng + "'",0); dtGhid = res.dtRet;
                    pTable_org.my_odbc_disconnect();
                    String strGhid_org = "", strGhid_dds = "";
                    for (int rows = 0; rows < dtGhid.getCount(); rows++)
                    {
                        if (strGhid_org.equals("")==false) strGhid_org += ",";
                        strGhid_org += "'" + Functions.dtCols_strValue(dtGhid, rows, "REL_GHID") + "'";
                    }
                    if (dtGhid.getCount() > 0)
                    {
                        //检查工号的角色哪个有调度权限
                        dtGhid = new DataTable();
                        String strSql = "SELECT GHID FROM CTS_OPIDK WHERE GHID IN(" + strGhid_org + ") AND ROLES IN(select ROLES from CTS_OPIDK_ROLES_LEVELS_MEM WHERE LEVEL_NAME='service_dds')";
                        my_odbc pTable_ghid = new my_odbc(pmSys.conn_callthink);
                        res =pTable_ghid.my_odbc_find(strSql,0); dtGhid = res.dtRet;
                        pTable_ghid.my_odbc_disconnect();
                        for (int rows = 0; rows < dtGhid.getCount(); rows++)
                        {
                            if (strGhid_dds.equals("")==false) strGhid_dds += ",";
                            strGhid_dds += "" + Functions.dtCols_strValue(dtGhid, rows, "GHID") + "";
                        }
                        if (strGhid_dds.equals("")==false)
                        {
                            //检查调度人员是否在线
                            //获取当前在线的调度人员
                            String strSysInfo = "Get_Agent_Online_byLevel;LEVEL=" + e_Level_service.service_dds + ";TYPE=1;";
                            List<String> alUid_online_list =Arrays.asList(pmInfo.myATClient.ATGetSystemInfo(strSysInfo).split("[|]"));
                            List<String> alUid_dest = new ArrayList<>();
                            //和当前机构人员比较
                            String[] strGhid_dds_list = strGhid_dds.split("[,]");
                            for (int cnt = 0; cnt < strGhid_dds_list.length; cnt++)
                            {
                                if (alUid_online_list.contains(strGhid_dds_list[cnt]) == true)
                                    alUid_dest.add(strGhid_dds_list[cnt]);
                            }
                            //向在线人员发送通知消息
                            String strGhid_dest = "";
                            for (int cnt = 0; cnt < alUid_dest.size(); cnt++)
                            {
                                strGhid_dest = alUid_dest.get(cnt);                                
                                break; //只给第一个人发送调度消息，暂不群发
                            }
                            if (strGhid_dest.equals(""))
                                strGhid_dest = Functions.dtCols_strValue(dtGhid, 0, "GHID");
                            strGhid_eng = strGhid_dest;
                        }
                    }
                }
                
                Send_Notice(strGhid_eng, strTaskid); //add by gaoww 20151231 增加发送通知功能

                //将被分配任务的工程师工号，保存到ghid_rel中
                if (strGhid_eng.equals("")==false)
                {
                    String strGhid_rel_scr = Functions.ht_Get_strValue("GHID_REL", myCaseInfo.htCase);
                    List<String> alGhid_rel = Arrays.asList(strGhid_rel_scr.split("[,]"));
                    if (alGhid_rel.contains(strGhid_eng) == false)
                    {
                        if (strGhid_rel_scr.equals("")==false) strGhid_rel_scr += ",";
                        strGhid_rel_scr += strGhid_eng;
                        Functions.ht_SaveEx("GHID_REL", strGhid_rel_scr,htTemp);
                    }

                    //add by gaoww 20160224 增加将分配工程师写回工单功能
                    if (rdo_Type_SelectedIndex == 1)
                    {
                        String strGhid_Task = strName_eng + "(" + strGhid_eng + ")";
                        strGhid_rel_scr = Functions.ht_Get_strValue("GHID_TASK", myCaseInfo.htCase);
                        alGhid_rel = Arrays.asList(strGhid_rel_scr.split("[,]"));
                        if (alGhid_rel.contains(strGhid_Task) == false)
                        {
                            if (strGhid_rel_scr.equals("")==false) strGhid_rel_scr += ",";
                            strGhid_rel_scr += strGhid_Task;
                            Functions.ht_SaveEx("GHID_TASK", strGhid_rel_scr,htTemp);
                        }
                    }
                }
                if (htTemp.size() != 0)
                {
                    int rc1 = myCase.UpdateCaseRecord(pCaseId, htTemp);
                    if (rc1 <= 0) return;
                }
            }
        }


        /// <summary>
        /// 保存任务单
        /// </summary>
        /// <param name="strEngrGhid">工程师工号</param>
        /// <param name="strEngrName">工程师姓名</param>
        ///  <param name="strEngrName">工程师所属机构</param>
        /// <param name="Task_Dest">分配目标：0-服务机构，1-工程师</param>
        /// <returns>是否成功</returns>
        private boolean Save_Task(String strEngrGhid, String strEngrName, String strOrg_Code_eng, String strOrg_Name_eng, int Task_Dest, pmRef<String> pmRefValue)
        {
            try
            {                
                HashMap htTask = myFld_Task.Save();
                //生成任务编号
               String strTaskId = "";
                if (pOp.equals("Edit"))
                    strTaskId = pTaskId;
                else
                {
                    //生成四位随机数
                    String strRnd = fun_random.getPadLeft();
                    String strBase = DateTime.Now().ToString("yyyyMMddHHmmss");
                    strTaskId = strBase + strRnd;
                    pTaskId = strTaskId;
                    pmRefValue.oRet=strTaskId;
                    Save_vs("pTaskId", pTaskId);
                }                
                Functions.ht_SaveEx("TASKID", strTaskId,htTask);
                Functions.ht_SaveEx("CASEID", pCaseId,htTask);
                Functions.ht_SaveEx("GHID", pmAgent.uid,htTask);
                Functions.ht_SaveEx("ORG_CODE", pmAgent.c_Info.agent_org_code,htTask);
                String strDate =Functions.ht_Get_strValue("SDATE", myCaseInfo.htCase);//Functions.atos(myCaseInfo.htCase["SDATE"]);//报修日期
                String strTime =Functions.ht_Get_strValue("STIME", myCaseInfo.htCase);// Functions.atos(myCaseInfo.htCase["STIME"]);//报修时间
                Functions.ht_SaveEx("SDATE", Functions.ConvertStrToDateTime(strDate, strTime).ToString("yyyy-MM-dd HH:mm:ss"),htTask );
                Functions.ht_SaveEx("SDATE_SEND", DateTime.Now().ToString("yyyy-MM-dd HH:mm:ss"),htTask);             
                Functions.ht_SaveEx("GHID_ENGR", strEngrGhid,htTask);
                Functions.ht_SaveEx("ENGR_NAME", strEngrName,htTask);
                Functions.ht_SaveEx("ENGR_ORG_CODE", strOrg_Code_eng,htTask);
                Functions.ht_SaveEx("CASETYPE", myCaseInfo.nType,htTask);
                Functions.ht_SaveEx("STATUS", "0",htTask);//任务执行状态(0-未签收)

                //add by gaoww 20151202 将工单部分字段写入任务表
                Functions.ht_SaveEx("CASENAME",Functions.ht_Get_strValue("CASENAME", myCaseInfo.htCase) ,htTask);
                Functions.ht_SaveEx("UNAME", Functions.ht_Get_strValue("UNAME", myCaseInfo.htCase),htTask);
                Functions.ht_SaveEx("TEL", Functions.ht_Get_strValue("TEL", myCaseInfo.htCase),htTask);
                Functions.ht_SaveEx("MOBILENO", Functions.ht_Get_strValue("MOBILENO", myCaseInfo.htCase),htTask);
                Functions.ht_SaveEx("ADDRESS", Functions.ht_Get_strValue("ADDRESS", myCaseInfo.htCase),htTask);
                Functions.ht_SaveEx("SVC_CATEGORY", Functions.ht_Get_strValue("SVC_CATEGORY", myCaseInfo.htCase),htTask);

                //将任务记录写入任务表
                String strFilter = "CASEID='" + pCaseId + "' AND TASKID='" + strTaskId + "'";
                my_odbc mydb = new my_odbc(pmSys.conn_crm);
                int rc = mydb.my_odbc_find(m_TableName_Task, strFilter);
                if (rc > 0)
                {
                    mydb.my_odbc_update(m_TableName_Task, htTask, strFilter);
                    mydb.my_odbc_disconnect();
                }
                else
                {
                    rc = mydb.my_odbc_addnew(m_TableName_Task, htTask);
                    mydb.my_odbc_disconnect();
                }
                if (rc > 0)
                {
                    String strStatus_case = Functions.ht_Get_strValue("STATUS", myCaseInfo.htCase);
                    String strInfo = myString.Format("CASEID={0};TASKID={1};ORG_CODE={2};STATUS={3};STATUS_TASK={4};PROCESS={5};TASKTYPE={6};SUBMIT_FROM=1;",
                        pCaseId, pTaskId, pmAgent.c_Info.agent_org_code,  strStatus_case, "0", myCaseInfo.nProcess, myFld_Task.get_item_value("TASKTYPE"));
                    if (Task_Dest == 0) //服务机构
                        fun_service.addnew_trace_log(pType, "分配任务", "调度员[" + pmAgent.name + "]将服务单分配到[" + strOrg_Name_eng + ")]", strInfo);
                    else //工程师
                    {
                        fun_service.addnew_trace_log(pType, "分配任务", "调度员[" + pmAgent.name + "]将服务单分配到工程师[" + strEngrName + "(" + strEngrGhid + ")]", strInfo);                        
                    }
                    Functions.MsgBox("提示","任务提交成功！");
                    return true;
                }
                else
                {
                    Functions.MsgBox("提示", "任务提交失败！");
                    return false;
                }
            }
            catch (Exception ex)
            {
                fun_main.rem("提交任务失败，原因：" +ex.getMessage(), 1);
                return false;
            }
        }


        private void Send_Notice(String strEngrGhid, String strTaskId)
        {
            //add by gaoww 20151231 发送微信通知    
            if (pmSys.Task_Send_Wxc_Assing == 1)
            {
                String strSn = "";
                my_odbc pTable = new my_odbc(pmSys.conn_crm);
                int rc = pTable.my_odbc_find("CRM_CUSTOMER_MEDIA", "UTYPE='6' AND MMCID='2' AND ACCID='" + strEngrGhid + "'");
                if (rc > 0)
                {
                    strSn = pTable.my_odbc_result("SN");
                    pTable.my_odbc_disconnect();

                    String strUrl = myString.Format("<a href=\"{0}/mt_app_wx/homepage.html?cmd=wxchat_passport&newurl=app.myTask_edit&parm={{TaskId:{1}}}\">登陆UltraCRM</a>", pmSys.bsCRM_homepage_ex, strTaskId);
                    String strMsg = myString.Format("你有新的任务，点击【{0}】，可查看详细信息", strUrl);
                    if (rdo_Type_SelectedIndex == 0)
                        strMsg = myString.Format("你有新的任务等待调度，点击【{0}】，可查看详细信息", strUrl);
                    String Sendmsg = myString.Format("WXC_SEND;FROM={0};TO={1};MSG={2};TYPE=6;NOTIFY=1;", pmAgent.uid, strSn, strMsg);
                    int nResult = 0;
                    if (pmInfo.myATClient != null)
                        nResult = pmInfo.myATClient.ATSendOEMCommand(pmAgent.uid, strSn, "AGENTTOMCI", Sendmsg);
                }
                else
                    pTable.my_odbc_disconnect();
            }
            //add by gaoww 20151231 发送oem消息通知，如果不在线写undeliver表
            String strUid_Status = "00";  //座席是否在线，00-离线，其他-在线
            ATClient_webs myWebs = new ATClient_webs();
            String strUid_info = myWebs.ATGetUidInfo(strEngrGhid);
            strUid_Status = Functions.Substring(strUid_info, "STATUS",1);
            String strTitle = "你有一个新的任务";
            if (strUid_Status.equals("00")==false)
            {
                String strMsg = "你有一个新的任务【" + Functions.ht_Get_strValue("CASENAME", myCaseInfo.htCase) + "】，请查收！";
                String strSend = myString.Format("TASK_ASSGIN;FROM={0};SUBJECT={1};TASKID={2};CONTENT={3};CASEID={4};TYPE={5};", pmAgent.uid, strTitle, strTaskId, strMsg, pCaseId, pType);
                if (rdo_Type_SelectedIndex == 0)
                    strSend = myString.Format("popup_service_dds;FROM={0};TO={1};MSG={2};TYPE={3};", pmAgent.uid, strEngrGhid, pCaseId, pType);
                myWebs.ATSendMsg(pmAgent.uid, strEngrGhid, strSend);
            }
            else //座席离线，写入通知库
            {
                if (rdo_Type_SelectedIndex == 1) //分配给工程师的任务有离线提醒
                {
                    String strTableName = "CTS_OEM_UNDELIVERED";
                    String strContent = Functions.ht_Get_strValue("CASENAME", myCaseInfo.htCase);
                    //string strUrl = String.Format(@"app.myTask_edit&parm={{TaskId:""{0}""}}", strTaskId);
                    String strUrl = myString.Format("/ut_service/case_task_edit.aspx?cmd=Edit&ntype={0}&caseid={1}&taskid={2}", pType, pCaseId, strTaskId); //modify by gaoww 20160314 修改任务离线消息url地址
                    
                    HashMap htMsg = new HashMap();
                    Functions.ht_SaveEx("TITLE", strTitle,htMsg);
                    Functions.ht_SaveEx("GHID", strEngrGhid,htMsg);
                    Functions.ht_SaveEx("CONTENT", strContent,htMsg);
                    Functions.ht_SaveEx("URL", strUrl,htMsg);
                    Functions.ht_SaveEx("SDATE", DateTime.Now().ToString("yyyyMMdd HHmmss"),htMsg);
                    Functions.ht_SaveEx("UNREAD", 1,htMsg);
                    my_odbc pTable = new my_odbc(pmSys.conn_callthink);
                    //strFilter = String.Format("(GHID='{0}' AND TITLE='{1}' AND CONTENT='{2}' AND URL='{3}')", strEngrGhid, strTitle, strContent, strUrl);
                    String strFilter = myString.Format("(GHID='{0}' AND TITLE='{1}' AND CONTENT='{2}' AND URL='{3}' )", strEngrGhid, strTitle, strContent, strUrl);
                    int nRet = pTable.my_odbc_find(strTableName, strFilter);
                    if (nRet == 1)
                        pTable.my_odbc_update(strTableName, htMsg, strFilter);
                    else
                        pTable.my_odbc_addnew(strTableName, htMsg);
                    pTable.my_odbc_disconnect();
                }
            }
        }
        //add by gaoww 20151120
        /// <summary>
        /// 根据填写的内容，获取工程师姓名、工号、所属机构代码、机构名称，机构级别
        /// </summary>
        /// <param name="strGhid"></param>
        /// <param name="htInfo"></param>
        private boolean Get_Engr_Info(String strGhid, pmRef<HashMap> pmRefValue)
        {
            boolean bReturn = false;
            HashMap htInfo = new HashMap();
            //判断是填写的还是选中的赋值的
            if (strGhid.contains("(") && strGhid.contains(")"))//如果是选中赋值拆分字符串取出工号和姓名
            {
                Functions.ht_SaveEx("GHID_ENG", Functions.Substring(strGhid, "(", ")"),htInfo);
                Functions.ht_SaveEx("NAME_ENG", Functions.Substring(strGhid, "", "("),htInfo);
            }
            else//手动填写工号或者工程师姓名
            {
                if (Functions.isNumber(strGhid))//如果用户输入的是工号则根据工号查询出工程师姓名
                {
                    DataTable dtTemp = Functions.dt_GetTable("SELECT REAL_NAME FROM CTS_OPIDK WHERE GHID='" + strGhid + "'", "", pmSys.conn_callthink);
                    if (dtTemp.getCount() > 0)
                    {
                        Functions.ht_SaveEx("GHID_ENG", strGhid,htInfo);
                        Functions.ht_SaveEx("NAME_ENG", Functions.drCols_strValue(dtTemp.Rows().get(0), "REAL_NAME"),htInfo);
                    }
                    else
                    {
                        Functions.MsgBox("无此工程师！");
                        return bReturn;
                    }
                }
                else//根据姓名查询出工号
                {
                    DataTable dtTemp = Functions.dt_GetTable("SELECT GHID FROM CTS_OPIDK WHERE REAL_NAME='" + strGhid + "'", "", pmSys.conn_callthink);
                    if (dtTemp.getCount() > 0)
                    {
                        Functions.ht_SaveEx("GHID_ENG", Functions.drCols_strValue(dtTemp.Rows().get(0), "GHID"),htInfo);
                        Functions.ht_SaveEx("NAME_ENG", strGhid,htInfo);
                    }
                    else
                    {
                        Functions.MsgBox("无此工程师！");
                        return bReturn;
                    }
                }
            }
            //查出所属机构代码和机构名称
            String strOrg_Code_eng = "",strGhid_eng="";
            strGhid_eng=Functions.ht_Get_strValue("GHID_ENG",htInfo );
            String strSql = myString.Format("SELECT ORG_CODE,ORG_NAME FROM DICT_ORG_GHID_REAL WHERE REL_GHID='{0}'", strGhid_eng);
            DataTable dt = Functions.dt_GetTable(strSql, "", pmSys.conn_crm);
            if (dt.getCount() > 0)
            {
                Functions.ht_SaveEx("ORG_CODE_ENG", Functions.drCols_strValue(dt.Rows().get(0), "ORG_CODE"),htInfo);
                Functions.ht_SaveEx("ORG_NAME_ENG", Functions.drCols_strValue(dt.Rows().get(0), "ORG_NAME"),htInfo);               
            }
            
            pmRefValue.oRet=htInfo;            
            return true;
        }

        /// <summary>
        /// 筛选符合条件的工程师
        /// </summary>
        /// <param name="strPlan">预案</param>
        /// <returns>返回符合条件的工程师数据表</returns>
        private DataTable GetEng()
        {
            DataTable dtTemp;
            String strFilter = "";
            String strCategory = Functions.ht_Get_strValue("SVC_CATEGORY", myCaseInfo.htCase);//服务分类
            String strBand = Functions.ht_Get_strValue("PO_BRAND", myCaseInfo.htCase);//维修品牌
            String strPoCategory = Functions.ht_Get_strValue("PO_CATEGORY", myCaseInfo.htCase);//品类
            String strPoCategory2 = Functions.ht_Get_strValue("PO_CATEGORY2", myCaseInfo.htCase);//细类

            //modify by gaoww 20151110 改为从预案表中读取
            String strPlan_id = myToolBar_Task.get_item_value("Select_plan");
            String strBtype = "0";
            DataTable myRow_plan = dtPlan.select("PLANID='" + strPlan_id + "'");
            if (myRow_plan.Rows().Count() > 0)
            {
                strBtype = Functions.drCols_strValue(myRow_plan.Rows().get(0), "BTYPE");
            }
            if (strBtype.equals("0"))//预案选择品类
            {
                strFilter = "((SBGS.BUSS_ID='" + strPoCategory + "') AND SBGS.BTYPE='0')";
            }
            else if (strBtype.equals("1"))//预案选择服务类型
            {
                strFilter = "((SBGS.BUSS_ID='" + strCategory + "' ) AND SBGS.BTYPE='1')";
            }
            //string strSql = "SELECT SBGS.*,SGR.POS_X,SGR.POS_Y,SGR.TASK_WORKING,SGR.STATUS FROM " + m_TableName_Skill + " AS SBGS," + m_TableName_RealTime + " AS SGR WHERE SGR.STATUS<>0 AND SGR.GHID=SBGS.GHID AND " + strFilter;// +" ORDER BY SBGS.SKILL_LEVEL DESC,SBGS.SKILL_LEVEL1 DESC";
            String strSql = "SELECT SBGS.*,SGR.POS_X,SGR.POS_Y,SGR.TASK_WORKING,SGR.STATUS FROM " + m_TableName_Skill + " AS SBGS INNER JOIN " + m_TableName_RealTime + " AS SGR ON SBGS.GHID=SGR.GHID WHERE SGR.STATUS<>0  AND " + strFilter;// +" ORDER BY SBGS.SKILL_LEVEL DESC,SBGS.SKILL_LEVEL1 DESC";

            String strOrg_Code1 = myFld_Task.get_item_value("ORG_CODE1");
            String strOrg_Code2 = myFld_Task.get_item_value("ORG_CODE2");
            String strOrg_Code3 = myFld_Task.get_item_value("ORG_CODE3");
            String strOrg_Code_eng = strOrg_Code1; //选中分配的最后一级机构代码
            if (strOrg_Code3.equals("")==false)
            {
                strOrg_Code_eng = strOrg_Code3;
            }
            else if (strOrg_Code2.equals("")==false)
            {
                strOrg_Code_eng = strOrg_Code2; //modify by gaoww 20151029
            }
            if (strOrg_Code_eng.equals("")==false)
                strSql += " AND SBGS.ORG_CODE='" + strOrg_Code_eng + "'";

            strSql += " ORDER BY SBGS.SKILL_LEVEL DESC,SBGS.SKILL_LEVEL1 DESC";

            dtTemp = Functions.dt_GetTable(strSql, "", pmSys.conn_crm);
            dtTemp.Columns().addColumn("MAP", 0);//添加位置显示列

            return dtTemp;
        }

        protected void rdo_Type_SelectedIndexChanged()
        {
            if (rdo_Type_SelectedIndex==1)
            {
                pDataTable = GetEng();
                Save_vs("pDataTable", pDataTable);
                mydg.refresh(pDataTable);
                if (pDataTable.getCount() > 0)
                {
                    if (myFld_Task.get_item_value("GHID") == "")
                    {
                        String strGhid = Functions.dtCols_strValue(pDataTable, 0, "GHID");
                        String strName = Functions.dtCols_strValue(pDataTable, 0, "OP_NAME");
                        myFld_Task.set_item_value("GHID", strName + "(" + strGhid + ")");
                    }
                }
            }
        }      
        
        /**
         * 增加前端控制的style的属性
         * @param model
         */
        public void AddStyle(Model model) {
        	model.addAttribute("divgbTask", m_divgbTask);
        	model.addAttribute("divGridStyle", m_divGridStyle);
		}
        
    }

