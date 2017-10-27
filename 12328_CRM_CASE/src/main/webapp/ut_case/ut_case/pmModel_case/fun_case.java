///########################################################################################
/// Copyright (C) 2000, ToneThink.Soft  All Rights Reserved. 
/// 文件创建时间：2000-06-05
///   文件创建人：peng
/// 文件功能描述：与UltraCRM相关的主要函数
/// 
///     维护记录：
/// 2007-07-08 将该文件拆分
/// 2007-09-11 Fill_Field() 可以处理列表为：1-未处理,2-已完成,10-处理中 形式的内容
/// 2010-05-14 Fill_Field() 列表内容为：未处理,已完成,处理中
///                         检查字段类型，如果为varchar，显示为 未处理-未处理,已完成-已完成,处理中-处理中
///                                       其它         ，显示为 0-未处理,1-已完成,2-处理中
///######################################################################################### 
package com.CallThink.ut_case.pmModel_case;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.CallThink.base.pmClass.pmSys;
import com.ToneThink.DataTable.DataColumn;
import com.ToneThink.DataTable.DataSet;
import com.ToneThink.DataTable.DataTable;
import com.ToneThink.DateTime.DateTime;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.Functions;


    /// <summary>
    /// fun_case 的摘要说明。
    /// </summary>
   public  class fun_case
   { 
	   /// <summary>
       ///  根据查询条件,读取所有类型工单表的内容
       /// </summary>
       /// <param name="strFilter">查询条件</param>
       /// <param name="strFields">新表包含的字段列表，如："CASEID,UNAME"</param>
       /// <param name="strConn">连接串</param>
       /// <returns>DataTable，存放符合条件的工单记录</returns>
       public static DataTable get_case_by_filter(String strFilter,String strField, String strConn)
       {
           //DataSet dsRet = new DataSet();
    	   List<DataTable> dsRet =  new ArrayList<DataTable>();
           DataTable dtTemp= null;
           String strTable, strName;
           int nType;

           my_odbc pCase = new my_odbc(strConn);
           int rc = pCase.my_odbc_find("CRM_CASE_TABLE", "USERID_LNK=1");
           while (rc > 0)
           {
        	   nType = pCase.my_odbc_result("CASETYPE", 0);
        	   strTable = pCase.my_odbc_result("TABLE_NAME");
        	   strName = pCase.my_odbc_result("CASE_NAME" );
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
                   newCol.setColumnName("TABLE_NAME");
                   //newCol.setDataType(Type.getType("System.String"));
                   //newCol.DefaultValue = strTable;
                   dtTemp.Columns().add(newCol);
                   if (dtTemp.Columns().contains("CASETYPE") == true) dtTemp.Columns().remove("CASETYPE");
                   DataColumn newCol1 = new DataColumn();
                   newCol1.setColumnName("CASETYPE");
                   //newCol1.setDataType(Type.getType("System.Int16"));
                   //newCol1.DefaultValue = nType;
                   dtTemp.Columns().add(newCol1);
                   /*if (dsRet.contains(dtTemp.getTableName()) == false)
                       dsRet.add(dtTemp.copyTable());*/
                   dsRet.add(dtTemp);
               }
               rc = pCase.my_odbc_nextrows(1);
           }
           pCase.my_odbc_disconnect();
           //string [,] strField = new string[,] {{"工单编号","CASEID","varchar"}};
           return Functions.dt_JoinTable(dsRet, strField);
       }


       /// <summary>
       /// 获取当前环节可以受理的座席工号
       /// </summary>
       /// <param name="strProcess">环节ID</param>
       /// <returns></returns>
       public static List<String> GetUid_byPriv(String strProcess, int nType)
       {
    	   List<String> alReturn = new ArrayList<String>();
    	   String strRoles = "", fld_value = "";
    	   String strSql = "SELECT ROLES FROM CRM_CASE_ROLES_LEVELS WHERE (CASETYPE='" + nType + "') AND (PROCESS='" + strProcess + "' OR PROCESS=-1) AND (LV_WF=1)";

           my_odbc pTable = new my_odbc(pmSys.conn_crm);
           int rc = pTable.my_odbc_find(strSql);
           while (rc > 0)
           {
        	   fld_value = pTable.my_odbc_result("ROLES" );
               if (fld_value.equals("-1"))
               {
                   strRoles = "All";
                   break;
               }
               if (strRoles.isEmpty())
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

       /// <summary>
       /// 操作轨迹20170925
       /// </summary>
       /// <param name="casetype">工单类型</param>
       /// <param name="subject">动作-主题</param>
       /// <param name="desp">动作-描述</param>
       /// <param name="info">附加信息，格式：TABLE=xxx;caseid=xx;</param>
       public static void addnew_trace_log(int casetype, String subject, String desp, String info,String strUid,String strUname)
       {
           
           DateTime dtTemp = DateTime.Now();
           HashMap htTrace = new HashMap<>();
           Functions.ht_SaveEx("CASETYPE", casetype,  htTrace);
           Functions.ht_SaveEx("SUBJECT", subject,  htTrace);
           Functions.ht_SaveEx("DESP", desp, htTrace);
           Functions.ht_SaveEx("SDATE", dtTemp.ToString("yyyy-MM-dd HH:mm:ss"), htTrace);
           if (info.length() > 0)
           {
               info += ";";
               String fld_value = Functions.Substring(info, "TABLE",1);
               Functions.ht_SaveEx("TABLE_NAME", fld_value,   htTrace);

               fld_value =Functions.Substring(info, "CASEID", 1);
               Functions.ht_SaveEx("CASEID", fld_value,  htTrace);
           }
           Functions.ht_SaveEx("GHID", strUid,   htTrace);
           Functions.ht_SaveEx("OP_NAME", strUname,   htTrace);
           my_odbc mydb = new my_odbc(pmSys.conn_crm);
          int rc= mydb.my_odbc_addnew("EVN_TRACE_LOG", htTrace);
           mydb.my_odbc_disconnect();
       }



   }
