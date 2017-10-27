///#########################################################################################
/// 文件创建时间：2015-06-12
///   文件创建人：xutt
/// 文件功能描述：轨迹列表页面
///     调用格式：
///     维护记录：
/// 2015.06.12 xutt
///#########################################################################################
package com.CallThink.ut_service;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import com.CallThink.base.pmClass.e_Level_base;
import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.pmSys;
import com.CallThink.base.pmClass.pmAgent_info;
import com.CallThink.base.pmCode.UltraCRM_Page;

import com.ToneThink.DataTable.DataTable;
import com.ToneThink.DataTable.DataRow;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.pmList;
import com.ToneThink.ctsTools.myUtility.pmMap;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.myString;
import com.ToneThink.ctsTools.WebUI.my_ToolStrip;
import com.ToneThink.ctsTools.WebUI.my_dataGrid;
import com.ToneThink.ctsTools.WebUI.my_SearchField;

    public class case_trace_log_list extends UltraCRM_Page
    {

        private String pTableName = "SM_TASK_TRACE_LOG";
        private String pOp = "";
        pmAgent_info pmAgent;
        private String pCaseid = ""; //服务单编号
        my_SearchField myFld_Query = new my_SearchField(3);
        my_dataGrid mydg = new my_dataGrid(51);
        my_ToolStrip myToolBar = new my_ToolStrip();
        private String pType = "0"; //工单类型
        private String m_Filter_search = ""; //本页面人工选择的查询条件，会话需要保存在Session中

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
                    pCaseid = Functions.ht_Get_strValue("caseid", htQuery);
                    pType = Functions.ht_Get_strValue("casetype", htQuery);
                }
                Save_vs("pOp", pOp);
                Save_vs("pCaseid", pCaseid);
                Save_vs("pType", pType); 
            }
            else
            {
                pOp = Load_vs("pOp");
                pCaseid = Load_vs("pCaseid");
                pType = Load_vs("pType");
            }

            Fillin_grid();
            InitToolbar();
            myToolBar.render(model);
            mydg.render(model);
        }
        private void InitToolbar()
        {
            myToolBar.fill_fld("刷新", "Query_all");
            myToolBar.fill_toolStrip("plCommand");
            myToolBar.btnItemClick = this;// new btnClickEventHandler(myToolBar_btnItemClick);
        }

        public void myToolBar_btnItemClick(Object sender, String name, String parms, int nparms)
        {
            if (name.equals("Query_all"))
            {
                String strFilter = m_strFilter_base() + m_strOrder_by();
                mydg.refresh("AUTOID", strFilter);
            }
        }

        private void Fillin_grid()
        {
            //填充表格区
            int i = 0;
            mydg.SetTable(pTableName);
            mydg.SetSelectStr("SELECT * FROM " + pTableName + " WHERE 1>1 ");//modify by gaoww 20110601. 改为分页显示加快页面加载速度
            mydg.SetConnStr(pmSys.conn_crm);
            mydg.SetPageSize(pmAgent.page_maxline - 4);
            mydg.SetPagerMode(2);

            mydg.fill_fld(i++, "AUTOID", "AUTOID", 0);
            if (pCaseid.length() > 0)
            {
                mydg.fill_fld(i++, "服务单编号", "CASEID", 0);
            }
            else
            {
                mydg.fill_fld(i++, "服务单编号", "CASEID", 18);
            }
            mydg.fill_fld(i++, "任务单编号", "TASKID", 0);
            mydg.fill_fld(i++, "操作时间", "SDATE", 30);
            mydg.fill_fld(i++, "环节", "PROCESS", 10,1);
            mydg.set_cols_cbo_list("PROCESS", "SELECT PROCESS_ID AS PROCESS,PROCESS_NAME AS PROCESS_NAME FROM  CRM_CASE_PROCESS WHERE CASETYPE='" + pType + "'", "PROCESS,PROCESS_NAME", pmSys.conn_crm);
            mydg.fill_fld(i++, "工单状态", "STATUS", 15, 1);
            mydg.set_cols_cbo_list("STATUS", "SELECT STATUS_ID AS STATUS,STATUS_NAME FROM  CRM_CASE_STATUS WHERE CASETYPE='" + pType + "'", "STATUS,STATUS_NAME", pmSys.conn_crm);
            mydg.fill_fld(i++, "进展说明", "NOTE", -1);
            mydg.fill_fld(i++, "工单类型", "CASETYPE", 0);
            mydg.fill_fld(i++, "操作员", "GHID", 15, 1);
            mydg.set_cols_cbo_list("GHID", "SELECT GHID,REAL_NAME FROM  CTS_OPIDK", "GHID,REAL_NAME", pmSys.conn_callthink);
            mydg.fill_fld(i++, "操作员所属机构", "ORG_CODE", 30, 1);
            mydg.set_cols_cbo_list("ORG_CODE", "SELECT ORG_CODE,ORG_NAME FROM  DICT_ORG_CODE", "ORG_CODE,ORG_NAME", pmSys.conn_crm);
            mydg.fill_fld(i++, "任务类型", "TASKTYPE", 0);
            //delete by gaoww 20151109
            //mydg.fill_fld(i++, "工程师所属机构", "ENGR_ORG_CODE", 0, 1);
            //mydg.set_cols_cbo_list("ENGR_ORG_CODE", "SELECT ORG_CODE AS ENGR_ORG_CODE,ORG_NAME FROM  DICT_ORG_CODE", "ENGR_ORG_CODE,ORG_NAME", pmSys.conn_crm);
           
            String strFilter = m_strFilter_base() + m_strOrder_by();

            mydg.RowDataFilled = this;
            mydg.fill_header("dgvList", "AUTOID", strFilter);
        }

        public void mydg_RowDataFilled(Object sender, int rows)
        {
            if (rows < 0) return; //表头行，不处理
        
        }              
     
       
        //基本的显示条件
        private String m_strFilter_base()
        {
                String strFilter_Temp = myString.Format(" CASEID='{0}'",pCaseid);
                return strFilter_Temp;
        }

        //排序规则
        private String m_strOrder_by()
        {
                String strOrderby = " ORDER BY SDATE DESC";  
                return strOrderby;
        }
    }

