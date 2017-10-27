package com.CallThink.ut_service.pmModel_service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.crypto.Data;

import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.pmAgent_info;
import com.CallThink.base.pmClass.pmSys;
import com.CallThink.base.support.pmInfo;
import com.ToneThink.DataTable.DataColumn;
import com.ToneThink.DataTable.DataRow;
import com.ToneThink.DataTable.DataSet;
import com.ToneThink.DataTable.DataTable;
import com.ToneThink.DateTime.DateTime;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.myString;
import com.ToneThink.ctsTools.myUtility.pmList;

//#region 工单相关函数
public  class fun_service
{
    /// <summary>
    ///  根据查询条件,读取所有类型工单表的内容
    /// </summary>
    /// <param name="strFilter">查询条件</param>
    /// <param name="strFields">新表包含的字段列表，如："CASEID,UNAME"</param>
    /// <param name="strConn">连接串</param>
    /// <returns>DataTable，存放符合条件的工单记录</returns>
    public static DataTable get_case_by_filter(String strFilter, String strField, String strConn)
    {
        //DataSet dsRet = new DataSet();
    	List<DataTable> lstDs=new ArrayList<>();
        DataTable dtTemp;
        String strTable, strName;
        int nType;

        my_odbc pCase = new my_odbc(strConn);
        int rc = pCase.my_odbc_find("CRM_CASE_TABLE", "USERID_LNK=1");
        while (rc > 0)
        {
            nType = pCase.my_odbc_result("CASETYPE",0);
            strTable = pCase.my_odbc_result("TABLE_NAME");
            strName = pCase.my_odbc_result("CASE_NAME");
            if (Functions.isExist_Table(strTable, strConn) == false)
            {
                rc = pCase.my_odbc_nextrows(1);
                continue;
            }
            dtTemp = Functions.dt_GetTable(strTable, strFilter, strConn);
            if (dtTemp.getCount() > 0)
            {
                if (dtTemp.Columns().contains("TABLE_NAME") == true) dtTemp.Columns().remove("TABLE_NAME");
                DataColumn newCol = new DataColumn();
                newCol.setColumnName( "TABLE_NAME");
                newCol.setDataType(0);//.DataType = System.Type.GetType("System.String");
                //newCol.DefaultValue = strTable;
                dtTemp.Columns().add(newCol);
                if (dtTemp.Columns().contains("CASETYPE") == true) dtTemp.Columns().remove("CASETYPE");
                DataColumn newCol1 = new DataColumn();
                newCol1.setColumnName( "CASETYPE");
                //newCol1.DataType = System.Type.GetType("System.Int16");
                //newCol1.DefaultValue = nType;
                dtTemp.Columns().add(newCol1);
                //if (dsRet.getDataTables().contains(dtTemp.getTableName()) == false)
                 //   dsRet.addDataTable(dtTemp.copyTable());
                lstDs.add(dtTemp);
            }
            rc = pCase.my_odbc_nextrows(1);
        }
        pCase.my_odbc_disconnect();
        //string [,] strField = new string[,] {{"工单编号","CASEID","varchar"}};
       
        //return Functions.dt_JoinTable(dsRet, strField);
        return Functions.dt_JoinTable(lstDs, strField);
    }
    /// <summary>
    /// 操作轨迹
    /// </summary>
    /// <param name="casetype">工单类型</param>
    /// <param name="subject">动作-主题</param>
    /// <param name="desp">动作-描述</param>
    /// <param name="info">附加信息，格式：TABLE=xxx;CASEID=xx;TASKID=xx;ORG_CODE=xx;ENGR_ORG_CODE=XX;STATUS_TASK=XX;STATUS=XX;SUBMIT_FROM=XX;</param>
    public static void addnew_trace_log(int casetype, String subject, String desp, String info)
    {
        pmAgent_info pmAgent = fun_main.GetParm();
        DateTime dtTemp = DateTime.Now();
        my_odbc mydb = new my_odbc(pmSys.conn_crm);
        HashMap htTrace = new HashMap();
        Functions.ht_SaveEx("CASETYPE", casetype,htTrace);
        Functions.ht_SaveEx("ACTION_NAME", subject,htTrace);
        Functions.ht_SaveEx("NOTE", desp,htTrace);
        Functions.ht_SaveEx("SDATE", dtTemp.ToString("yyyy-MM-dd HH:mm:ss"),htTrace);
        if (info.length() > 0)
        {
            info += ";";
            //string fld_value = Functions.GetItemValue(1, "TABLE", info); //delete by gaoww 20151109
            //Functions.ht_SaveEx("TABLE_NAME", fld_value, ref htTrace);

            String fld_value = Functions.Substring(info, "CASEID",1);
            Functions.ht_SaveEx("CASEID", fld_value,htTrace);

            fld_value = Functions.Substring(info, "TASKID",1);
            Functions.ht_SaveEx("TASKID", fld_value,htTrace);

            fld_value = Functions.Substring(info, "ORG_CODE",1);
            Functions.ht_SaveEx("ORG_CODE", fld_value,htTrace);

            //fld_value = Functions.GetItemValue(1, "ENGR_ORG_CODE", info);
            //Functions.ht_SaveEx("ENGR_ORG_CODE", fld_value, ref htTrace);

            fld_value = Functions.Substring(info, "STATUS_TASK",1);
            Functions.ht_SaveEx("STATUS_TASK", fld_value,htTrace);

            fld_value = Functions.Substring(info, "STATUS",1);
            Functions.ht_SaveEx("STATUS", fld_value,htTrace);

            fld_value = Functions.Substring(info, "PROCESS",1);
            if (fld_value.equals("")==false) //add by by gaoww 20161123 增加判断，如果为空则不写入到哈希表，mysql数据库时会报错
                Functions.ht_SaveEx("PROCESS", fld_value,htTrace);

            fld_value = Functions.Substring(info, "TASKTYPE",1);
            if (fld_value.equals("")==false) //add by by gaoww 20161123 增加判断，如果为空则不写入到哈希表，mysql数据库时会报错
                Functions.ht_SaveEx("TASKTYPE", fld_value,htTrace);

            fld_value = Functions.Substring(info, "SUBMIT_FROM",1); //add by gaoww 20151109
            Functions.ht_SaveEx("SUBMIT_FROM", fld_value,htTrace);
            
        }
        Functions.ht_SaveEx("GHID", pmAgent.uid,htTrace);
        Functions.ht_SaveEx("OP_NAME", pmAgent.name,htTrace);
        int rc = mydb.my_odbc_addnew("SM_TASK_TRACE_LOG", htTrace);

        mydb.my_odbc_disconnect();
    }

    /// <summary>
    /// 根据任务状态计算出工单要变更的状态值
    /// </summary>
    /// <param name="casetype">工单类型</param>
    /// <param name="strTaskid">任务编号</param>
    /// <param name="strTaskStatus">任务当前状态</strTaskStatus>
    /// <returns>空，表示不更新工单当前状态，（状态值+状态名）-根据返回结果更新工单状态</returns>
    //public static string get_case_status_by_task(int casetype, string strCaseid, string strTaskid, string strTaskStatus)
    public static String get_case_status_by_task(int casetype, String strProcess, String strTaskType, String strTaskStatus)
    {
        String strReturn = "",strTableName="";
        String strStatus_dest="";//工单初始状态,目标状态值
        //if (strCaseid == "") return strReturn;
        //service_set_info myCase = new service_set_info(casetype);
        String strKey = "get_case_status_name_" + casetype;
        HashMap htStatus = pmInfo.myKvdb.Get(strKey,HashMap.class);
        if (htStatus == null)
        {
            DataTable dtRet;
            my_odbc pTemp = new my_odbc(pmSys.conn_crm);
            String strSql = "SELECT STATUS_ID,STATUS_NAME FROM CRM_CASE_STATUS WHERE CASETYPE='" + casetype + "'";
            pmList res =pTemp.my_odbc_find(strSql,0); dtRet = res.dtRet;
            pTemp.my_odbc_disconnect();
            htStatus = new HashMap();
            for (DataRow drRow : dtRet.Rows())
            {
                htStatus.put(Functions.drCols_strValue(drRow, "STATUS_ID"), Functions.drCols_strValue(drRow, "STATUS_NAME"));
            }
            pmInfo.myKvdb.Setex(strKey, htStatus, 600);//缓存10分钟            
        }
        my_odbc pTable = new my_odbc(pmSys.conn_crm);
        String strFilter =myString.Format("CASETYPE='{0}' AND FLD_ID='{1}' AND TASKTYPE='{2}' AND PROCESS='{3}'",casetype ,strTaskStatus ,strTaskType ,strProcess );
        int rc = pTable.my_odbc_find("DICT_TASK_STATUS", strFilter);
        if (rc > 0)
        {
            strStatus_dest = pTable.my_odbc_result("ACTION_AFTER");                
        }
        pTable.my_odbc_disconnect();

        if (strStatus_dest.equals("")==false)
        {
            String strName = Functions.ht_Get_strValue(strStatus_dest, htStatus);
            if (strName.contains("-"))
                strName = Functions.Substring(strName, "-", "");
            strReturn = strStatus_dest + "-" + strName;
        }
        return strReturn;
    }
       // #region delete by gaoww 20151016 任务状态值写死的方法
      


    /// <summary>
    /// 获取当前环节可以受理的座席工号
    /// </summary>
    /// <param name="strProcess">环节ID</param>
    /// <returns></returns>
    public static ArrayList GetUid_byPriv(String strProcess,int nType)
    {
        ArrayList alReturn = new ArrayList();
        String strRoles = "", fld_value = "";
        String strSql = "SELECT ROLES FROM CRM_CASE_ROLES_LEVELS WHERE (CASETYPE='" + nType + "') AND (PROCESS='" + strProcess + "' OR PROCESS=-1) AND (LV_WF=1)";

        my_odbc pTable = new my_odbc(pmSys.conn_crm);
        int rc = pTable.my_odbc_find(strSql);
        while (rc > 0)
        {
            fld_value = pTable.my_odbc_result("ROLES");
            if (fld_value.equals("-1"))
            {
                strRoles = "All";
                break;
            }
            if (strRoles.equals(""))
                strRoles = "'" + fld_value + "'";
            else
                strRoles += ",'" + fld_value + "'";
            rc = pTable.my_odbc_nextrows(1);
        }
        pTable.my_odbc_disconnect();
        if (strRoles.length() < 1) return alReturn;
        else if (strRoles.equals("All"))
            strSql = "SELECT GHID,REAL_NAME FROM CTS_OPIDK WHERE (UTYPE>0 AND UTYPE<10)";//modify by gaoww 20141222 只显示UTYPE=1~9的座席
        else
            strSql = "SELECT GHID,REAL_NAME FROM CTS_OPIDK WHERE (UTYPE>0 AND UTYPE<10) AND ROLES IN (" + strRoles + ")";//modify by gaoww 20141222 只显示UTYPE=1~9的座席

        pTable = new my_odbc(pmSys.conn_callthink);
        rc = pTable.my_odbc_find(strSql);
        while (rc > 0)
        {
            fld_value = pTable.my_odbc_result("REAL_NAME");
            strRoles = pTable.my_odbc_result("GHID");
            alReturn.add(strRoles + "-" + fld_value);
            rc = pTable.my_odbc_nextrows(1);
        }
        pTable.my_odbc_disconnect();
        return alReturn;
    }
}
//#endregion