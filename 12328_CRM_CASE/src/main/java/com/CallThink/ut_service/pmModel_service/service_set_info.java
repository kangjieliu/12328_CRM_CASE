///########################################################################################
/// Copyright (C) 2000, ToneThink.Soft  All Rights Reserved. 
/// 文件创建时间：2015-09-23
///   文件创建人：gaoww
/// 文件功能描述：与服务系统相关的主要函数
/// 
///     维护记录：
///#########################################################################################
package com.CallThink.ut_service.pmModel_service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.List;
import com.ToneThink.DataTable.DataTable;
import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.pmAgent_info;
import com.CallThink.base.pmClass.pmSys;
import com.CallThink.base.support.pmInfo;
import com.CallThink.ut_form.pmModel_form.fun_Form;
import com.CallThink.ut_service.fun_random;
import com.ToneThink.DataTable.DataColumn;
import com.ToneThink.DataTable.DataRow;
import com.ToneThink.DataTable.DataSet;
import com.ToneThink.DateTime.DateTime;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.pmList;
import com.ToneThink.ctsTools.myUtility.pmMap;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.fun_json;
import com.ToneThink.ctsTools.myUtility.myString;
import com.ToneThink.ctsTools.WebUI.my_ToolStrip;
import com.ToneThink.ctsTools.WebUI.my_dataGrid;
import com.ToneThink.ctsTools.Regex.Regex;
import com.ToneThink.ctsTools.WebUI.my_SearchField;

//#region 处理一种类型的工单
    /// <summary>
    /// 处理一种类型的工单
    /// </summary>
    public class service_set_info
    {
        public int nType;                 //工单类型
        public String TableName;          //工单表名
        public String TableKey;          //工单表的关键字，特殊情况下关键字不一定是CASEID； //add by gaoww 20130924
        public String CaseName;           //工单名称
        public int nWF_Enable;            //是否使用工作流  0-不使用  1-使用
        public String DescName;           //使用的表单描述表名称
        //public DataTable dtCase_type;   //工单类型表，对应  CRM_CASE_TABLE              ,条件： CASETYPE = " + nType_case
        //public DataTable dtPriv_fld;    //工单表单编辑权限限制表  对应 CRM_CASE_FLD_PRIV,条件： (ROLES={0} AND CASETYPE={1} AND STATUS={2})"
        public int nSubMenu;              //关联的子卡片页
        public int nUserId_lnk;           //是否关联客户资料 0-不关联 1-关联
        //public DataTable dtLevel_role;      //工单表单编辑权限限制表  对应 CRM_CASE_FLD_PRIV,条件： (ROLES={0} AND CASETYPE={1} AND STATUS={2})"
        public String FailReason = "";     //失败原因
        public int nForm_cols;             //编辑页面每行显示几列（0-自适应，1-4可设）

        pmAgent_info pmAgent;
        public service_set_info() //用于调用函数
        {
            pmAgent = fun_main.GetParm();
            nType = 0;
            //dtCase_type = new DataTable();
            TableName = "";
            TableKey = "CASEID"; 
            CaseName = "";
            nWF_Enable = 0;
            DescName = "";
        }
        public service_set_info(int nType_case)
        {
            pmAgent = fun_main.GetParm();
            nType = nType_case;
            String strSql = myString.Format("SELECT * FROM CRM_CASE_TABLE WHERE CASETYPE={0}", nType_case);
            DataTable dtCase_type = Functions.dt_GetTable(strSql, "", pmSys.conn_crm);
            if (dtCase_type.getCount() > 0)
            {
                TableName = Functions.dtCols_strValue(dtCase_type, "TABLE_NAME");
                TableKey = Functions.dtCols_strValue(dtCase_type, "TABLE_KEY");
                CaseName = Functions.dtCols_strValue(dtCase_type, "CASE_NAME");
                nWF_Enable = Functions.dtCols_nValue(dtCase_type, "WF_ENABLE");
                DescName = Functions.dtCols_strValue(dtCase_type, "DESC_NAME");
                nSubMenu = Functions.dtCols_nValue(dtCase_type, "SUBMENU");
                nUserId_lnk = Functions.dtCols_nValue(dtCase_type, "USERID_LNK");
                nForm_cols = Functions.dtCols_nValue(dtCase_type, "FORM_COL");
            }
        }

        //#region 工单-增查删改
        /// <summary>
        /// 获取一个空闲的CaseID
        /// </summary>
        /// <returns></returns>
        public String GetNewCaseid(String strCaseId)
        {
            int rc;
            String strBase = strCaseId;
            //生成四位随机数
            Random rand = new Random();
            String strRnd =fun_random.getPadLeft();

            if (strCaseId.equals(""))
            {
                //strBase = DateTime.Now.ToString("yyyyMMddHHmmss");
            	//nType.ToString("00") 用单独逻辑判断
            	String strType=Integer.toString(nType);
            	if(strType.length()==1) strType="0"+strType;
                strBase = DateTime.Now().ToString("yyyyMMddHHmmss") +strType ; //modify by gaoww 20151021 增加工单类型作为工单编号一部分，避免重复
                strCaseId = strBase + strRnd;
            }
            my_odbc pCust = new my_odbc(pmSys.conn_crm);
            for (int i = 0; i < 100; i++)
            {
                rc = pCust.my_odbc_find(TableName, TableKey + "='" + strCaseId + "'");
                if (rc == 1)
                {
                    if (i == 99)
                    {
                        strCaseId = "";
                        break;
                    }
                    else
                    {
                        strRnd =fun_random.getPadLeft();// rand.next(9999).ToString().PadLeft(4, '0');
                        strCaseId = strBase + strRnd;
                        continue;
                    }
                }
                else break;
            }
            pCust.my_odbc_disconnect();
            return strCaseId;
        }

       
        
        //根据工号,读取工单详细记录
        public service_info GetCaseRecord(String strCaseId)
        {
            service_info myCaseInfo = new service_info();
            if (myString.IsNullOrEmpty(strCaseId) == true) return myCaseInfo;

       
            myCaseInfo.strCaseId = strCaseId;
            HashMap htCase;
            my_odbc pCust = new my_odbc(pmSys.conn_crm);
            String strSelect = myString.Format("SELECT * FROM {0} WHERE {1}='{2}'", TableName, TableKey, strCaseId);
            pmMap res =pCust.my_odbc_find(strSelect,true); htCase = res.htRet;// TableName, "CASEID = '" + CaseId + "'");
            int rc = res.nRet;
            pCust.my_odbc_disconnect();
            if (rc == 1)
            {
                myCaseInfo = new service_info(htCase);
            }
            return myCaseInfo;
        }


        /// <summary>
        /// 增加工单资料
        /// </summary>
        /// <param name="caseid"></param>
        /// <param name="htCase"></param>
        /// <param name="strTableName"></param>
        /// <returns>-1:不合法  0:写失败 1:写成功 </returns>
        public int AddCaseRecord(String strCaseId, HashMap htCase)
        {
            int nReturn = SaveCaseRecord(strCaseId, htCase);
            return nReturn;
        }

        public int UpdateCaseRecord(String strCaseId, HashMap htCase)
        {
            int nReturn = SaveCaseRecord(strCaseId, htCase);
            return nReturn;
        }

        public int SaveCaseRecord(String strCaseId, HashMap htCase)
        {
            int nReturn = -1;

            boolean bCaseExist = true;

            if (htCase.size() < 1) return nReturn;

            String strTemp;

            if (htCase.containsKey("STIME") == true)
            {
                strTemp = Functions.ht_Get_strValue("STIME", htCase).replace(":", "");
                Functions.ht_SaveEx("STIME", strTemp,htCase);
            }
            if (htCase.containsKey("CLOSE_DATE") == true)
            {
                strTemp = Functions.ht_Get_strValue("CLOSE_DATE", htCase).replace("-", "");
                Functions.ht_SaveEx("CLOSE_DATE", strTemp,htCase);
            }
            Functions.ht_SaveEx("SDATE_UPDATE", DateTime.Now().ToString("yyyy-MM-dd HH:mm:ss"),htCase);//add by gaoww 20151202 增加更新日期修改            

            my_odbc pCase = new my_odbc(pmSys.conn_crm);
            pCase.IsShowBox = 0;
            int nRet = pCase.my_odbc_find(TableName, TableKey + "='" + strCaseId + "'");
            //pCase.my_odbc_disconnect();
            if (nRet == 1)
            {
                //add by gaoww 20090327,增加记录工单历史记录功能。                
                Add_Case_Hist(1, TableName, strCaseId, htCase);

                int nStatus = Functions.atoi(Functions.ht_Get_strValue("STATUS", htCase));
                if (nStatus == 100)//e_Status_case.nClose)
                {
                    Functions.ht_SaveEx("CLOSE_DATE", DateTime.Now().ToString("yyyyMMdd HHmmss"),htCase); //闭单日期
                    Functions.ht_SaveEx("CLOSE_GHID", pmAgent.uid,htCase); //闭单业务员
                }

                //add by gaoww 20151103 增加ghid_rel各个环节有关联的工号处理
                if (pmAgent.uid != Functions.ht_Get_strValue("GHID", htCase)) //如果不是工单创建人，则检查是否在关联工号里
                {
                    String strGhid_rel_scr = Functions.ht_Get_strValue("GHID_REL", htCase);
                    List<String> alGhid_rel = Arrays.asList(strGhid_rel_scr.split("[,]"));
                    if (alGhid_rel.contains(pmAgent.uid) == false)
                    {
                        if (strGhid_rel_scr.equals("")==false) strGhid_rel_scr += ",";
                        strGhid_rel_scr += pmAgent.uid;
                        Functions.ht_SaveEx("GHID_REL", strGhid_rel_scr,htCase);
                    }
                }

                int  rc =pCase.my_odbc_update(TableName, htCase, TableKey + "='" + strCaseId + "'");
                pCase.my_odbc_disconnect();

                String strCname = Functions.ht_Get_strValue("CASENAME", htCase);
               //未处理代码
                // fun_CRM.addnew_op_hist(3, "修改工单资料", strCname, "TABLE=" + TableName + ";KEY=" + strCaseId);
                return 1;
            }
            else
            {
                //add by gaoww 20090327,增加记录工单历史记录功能。                
                Add_Case_Hist(0, TableName, strCaseId, htCase);
                String strCname = Functions.ht_Get_strValue("CASENAME", htCase);

                if (htCase.containsKey(TableKey) == false) htCase.put(TableKey, strCaseId);

                //pCase = new my_odbc(pmSys.conn_crm);
                pCase.my_odbc_addnew(TableName, htCase);
                pCase.my_odbc_disconnect();
                //未处理代码
                //fun_CRM.addnew_op_hist(3, "新增工单资料", strCname, "TABLE=" + TableName + ";KEY=" + strCaseId);
                nReturn = 1;
            }
           
            return nReturn;
        }


        //1-OK  0-工单不存在 -1-失败  -2-没有权限 
        public int DelCaseRecord(String strCaseId)//, String uname)
        {
            int rc;
            String fld_value, fld_value1, fld_value2;
            String userid = "", username = "", casename = "";
            //strCause = "OK";

            my_odbc pCase = new my_odbc(pmSys.conn_crm);
            rc = pCase.my_odbc_find(TableName, TableKey + "='" + strCaseId + "'");
            if (rc != 1)
            {
                Functions.MsgBox("该工单不存在，请重新输入！");
                pCase.my_odbc_disconnect();
                return 0;
            }
            else
            {
                fld_value = pCase.my_odbc_result("GHID");
                fld_value1 = pCase.my_odbc_result("CURRENTGHID");
                fld_value2 = pCase.my_odbc_result("CLOSE_GHID");
                userid = pCase.my_odbc_result("USERID");
                username = pCase.my_odbc_result("UNAME");
                casename = pCase.my_odbc_result("CASENAME");
                
            }
            //if (Functions.MsgBox_OK("确实要删除<" + uname + ">的工单资料吗？") == true) //delete by gaoww 20111008 bs程序这里提示没有用，所以封上
            //{
            pCase.my_odbc_delete(TableName, "CASEID = '" + strCaseId + "'");
            pCase.my_odbc_disconnect();
            //未处理代码
            //fun_CRM.addnew_op_hist(3, "删除工单资料", casename, "TABLE=" + TableName + ";KEY=" + strCaseId);  //增加记录操作员删除工单功能
            HashMap htCase = new HashMap();
            htCase.put("USERID", userid);
            htCase.put("CALLID", "");
            htCase.put("UNAME", username);
            htCase.put(TableKey, strCaseId);

            Add_Case_Hist(2, TableName, strCaseId, htCase); //增加记录删除工单历史记录功能
            return 1;
            //}
            //pCase.my_odbc_disconnect();
            //addnew_op_hist(3, "删除工单资料", uname, "TABLE=" + strTableName + ";KEY=" + caseid);
            //return 0;
        }


        /// <summary>
        /// 判断添加的工单内容是否符合完整性要求
        /// </summary>
        /// <param name="htTemp"></param>
        /// <returns>返回值 1－完整；0－不完整</returns>
        public boolean judge_case_validity(HashMap htTemp)
        {
            boolean nReturn = false;
            String strTemp = Functions.ht_Get_strValue("TEL", htTemp);
            if (strTemp.equals(""))
            {
                strTemp = Functions.ht_Get_strValue("CALLER", htTemp);
            }
            if (strTemp.equals(""))
            {
                Functions.MsgBox("联系电话不能为空！");
                return nReturn;
            }
            if (Functions.IsTeleNum(strTemp) == false)
            {
                Functions.MsgBox("联系电话格式不正确，包含非法字符！");
                return nReturn;
            }
            if ((strTemp.startsWith("13") == true) || (strTemp.startsWith("15") == true) || (strTemp.startsWith("14") == true) || (strTemp.startsWith("18") == true))
            {
                if (strTemp.length() != 11)
                {
                   // Functions.MsgBox("手机号码必须是11位！");
                    Functions.MsgBox("联系电话中填写的手机号码必须是11位！");
                    return nReturn;
                }
            }
           
            strTemp = Functions.ht_Get_strValue("UNAME", htTemp);           
            nReturn = true;
            return nReturn;
        }

        
        
      /// <summary>
        /// 判断添加的工单内容是否符合完整性要求
        /// </summary>
        /// <param name="htTemp"></param>
        /// <param name="strFilter">表单查询条件</param>
        /// <returns>返回值 1－完整；0－不完整;string:不完整的原因</returns>
        public Integer   judge_case_validity(HashMap  htTemp,String strFilter)
        {
        	int nReturn = 0;
        	FailReason ="";
        	
            String strTemp = Functions.ht_Get_strValue("TEL", htTemp);
            if (strTemp.isEmpty())
            {
                strTemp = Functions.ht_Get_strValue("CALLER", htTemp);
            }
            if (strTemp.isEmpty())
            {
                //Functions.MsgBox("电话号码不能为空！");
            	FailReason ="电话号码不能为空！";
                return nReturn;
            }
            if (Functions.IsTeleNum(strTemp) == false)
            {
                //Functions.MsgBox("电话号码格式不正确，包含非法字符！");
            	FailReason ="电话号码格式不正确，包含非法字符！";
                return nReturn;
            }
            if ((strTemp.startsWith("13") == true) || (strTemp.startsWith("15") == true) || (strTemp.startsWith("14") == true) || (strTemp.startsWith("18") == true))
            {
                if (strTemp.length() != 11)
                {
                    //Functions.MsgBox("手机号码[" + strTemp + "]必须是11位！");
                	FailReason="手机号码[" + strTemp + "]必须是11位！";
                    return nReturn;
                }
            }


           // 判断添加的用户资料/工单中不允许为空的字段是否为空，为空则进行提示      
            String strSql = "EDIT_REGEX !='' AND "+strFilter;
            DataTable dtDesc = fun_Form.get_desc_data(DescName, strFilter);// "((FLD_VLEVELS&1)<>0) ORDER BY EDIT_INDEX");
            //foreach (string strKey in htTemp.Keys)
            for(int idx=0;idx<dtDesc.getCount();idx++)
            {
          	  String fld_value = Functions.dtCols_strValue(dtDesc, idx, "FLD_VALUE");
          	  String fld_value1 = Functions.dtCols_strValue(dtDesc, idx, "EDIT_REGEX");
          	  String fld_regex = Functions.Substring(fld_value1, "", ";");
          	  String fld_prompt = Functions.Substring(fld_value1, ";", "");
          	  if(fld_regex.isEmpty()) continue;
              if(htTemp.containsKey(fld_value)==false) continue;
               
              strTemp = Functions.ht_Get_strValue(fld_value, htTemp);
              if (Regex.IsMatch(strTemp, fld_regex) == false)
              {
            	  FailReason = fld_prompt;
                  break;
              }                  
          }
           
           if(FailReason.isEmpty())
          	 nReturn=1;

            return nReturn;
        }

        public boolean get_authority(service_info myCaseInfo, String strMask)
        {
            return get_authority(myCaseInfo.nProcess, myCaseInfo.nStatus, strMask);
        }
        //}
        //检查工单权限
        //strMask =base：基本权限  add：增加  del：删除 edit：修改   view：查看  output：输出  wf：工作流
        //返回：true-有该权限  false-无该权限 
        public boolean get_authority(int nProcess, int nStatus, String strMask)
        {
            boolean bReturn = false;

            int nLevel = 0, nMask = 0;
            nLevel = get_authority(nProcess, nStatus);

            if (strMask.equals("add")) nMask = 1;
            else if (strMask.equals("update")) nMask = 2;
            else if (strMask.equals("del")) nMask = 4;
            else if (strMask.equals("view")) nMask = 8;
            else if (strMask.equals("output")) nMask = 16;
            else if (strMask.equals("wf")) nMask = 32;
            if ((nLevel & nMask) > 0) bReturn = true;
            return bReturn;
        }

        // Bit0-add：增加  Bit1-edit：修改  Bit2-del：删除  Bit3-view：查看  Bit4-output：输出  Bit5-wf：工作流
        public int get_authority(int nProcess, int nStatus)
        {
            int nLevel = 0xFF;
            //权限表读入内存数据库
            String strKey = "dt_myCase_level_" + nType;
            DataTable dtRet= pmInfo.mySession.Get(strKey);//DataTable.class);// as DataTable;
            if (dtRet == null)
            {
                dtRet = new DataTable();
                //string strFilter = String.Format("(ROLES={0} AND CASETYPE={1})", pmAgent.nRoles, nType);
                String strFilter = myString.Format("((ROLES={0} OR ROLES=-1) AND (CASETYPE={1}))", pmAgent.nRoles, nType); //modify by gaoww 20130421 应该把当前角色和-1角色的权限都读出来
                my_odbc pTable = new my_odbc(pmSys.conn_crm);
                pmList res =pTable.my_odbc_find("CRM_CASE_ROLES_LEVELS", strFilter,0); dtRet = res.dtRet;
                int rc = res.nRet;
                if (rc != 1)
                {
                    strFilter = myString.Format("((ROLES={0} OR ROLES=-1) AND (CASETYPE={1} OR CASETYPE=-1))", pmAgent.nRoles, nType);
                     res =pTable.my_odbc_find("CRM_CASE_ROLES_LEVELS", strFilter,0); dtRet = res.dtRet;
                }
                pTable.my_odbc_disconnect();
                pmInfo.mySession.Setex(strKey, dtRet, 60);               
            }

            //有相符记录
            String strField = "STATUS";
            if (nWF_Enable == 1)
            {
                strField = "PROCESS";
                nStatus = nProcess;
            }
            if (dtRet.getCount() > 0)
            {
                nLevel = 0;
               
                String strFilter = myString.Format("ROLES={0} AND ({1}={2})", pmAgent.nRoles, strField, nStatus);
               DataTable drRet = dtRet.select(strFilter);
                if (drRet.Rows().Count()< 1)
                {
                    strFilter = myString.Format("ROLES={0} AND ({1}=-1)", pmAgent.nRoles, strField);
                    drRet = dtRet.select(strFilter);
                    if (drRet.Rows().Count() < 1)
                    {
                        strFilter = myString.Format("ROLES=-1 AND ({0}={1})", strField, nStatus);
                        drRet = dtRet.select(strFilter);
                        if (drRet.Rows().Count()< 1)
                        {
                            strFilter = myString.Format("ROLES=-1 AND ({0}=-1)", strField);
                            drRet = dtRet.select(strFilter);
                        }
                    }
                }
                if (drRet.Rows().Count() < 1) return nLevel;

                int nTemp = Functions.dtCols_nValue(drRet,0, "LV_ADDNEW");// Functions.drCols_nValue(drRet[0], "LV_ADDNEW");
                if (nTemp == 1) nLevel |= 1;
                nTemp =Functions.dtCols_nValue(drRet,0, "LV_UPDATE"); //Functions.drCols_nValue(drRet[0], "LV_UPDATE");
                if (nTemp == 1) nLevel |= 2;
                nTemp =Functions.dtCols_nValue(drRet,0, "LV_DELETE");// Functions.drCols_nValue(drRet[0], "LV_DELETE");
                if (nTemp == 1) nLevel |= 4;
                nTemp =Functions.dtCols_nValue(drRet,0, "LV_VIEW");// Functions.drCols_nValue(drRet[0], "LV_VIEW");
                if (nTemp == 1) nLevel |= 8;
                nTemp =Functions.dtCols_nValue(drRet,0, "LV_OUTPUT");// Functions.drCols_nValue(drRet[0], "LV_OUTPUT");
                if (nTemp == 1) nLevel |= 16;
                nTemp = Functions.dtCols_nValue(drRet,0, "LV_WF");// Functions.drCols_nValue(drRet[0], "LV_WF");
                if (nTemp == 1) nLevel |= 32;
            }

         
            return nLevel;
        }

      
        /// <summary>
        /// 根据座席员角色权限，读取工单列表条件
        /// </summary>
        /// <param name="nProcess">环节(适用有工作流)，-1：查所有环节</param>
        /// <param name="nStatus">环节(适用无工作流)，-1：查所有状态 </param>
        /// <returns>条件</returns>
        public String get_list_filter(int nProcess, int nStatus)
        {
            String strReturn = "";
            //权限表读入内存数据库
            //未处理代码-sessionid不存在
            //pmInfo.mySession.SessionId = pmAgent.uid;
            String strKey = "dt_myCase_level_F" + nType;
            DataTable dtRet = pmInfo.mySession.Get(strKey);								//正确   pmInfo.mySession.Get(strKey,null);//这种方式获取缓存有问题		
            if (dtRet == null) 
            {
                dtRet = new DataTable();
                //string strFilter = String.Format("(ROLES={0} AND CASETYPE={1})", pmAgent.nRoles, nType);
                String strFilter = myString.Format("((ROLES={0} OR ROLES=-1) AND (CASETYPE={1}))", pmAgent.nRoles, nType); //modify by gaoww 20130421 应该把当前角色和-1角色的权限都读出来
                my_odbc pTable = new my_odbc(pmSys.conn_crm);
                pmList res =pTable.my_odbc_find("CRM_CASE_ROLES_LEVELS", strFilter,0); dtRet = res.dtRet;
                int rc = res.nRet;
                if (rc != 1)
                {
                    strFilter = myString.Format("((ROLES={0} OR ROLES=-1) AND (CASETYPE={1} OR CASETYPE=-1))", pmAgent.nRoles, nType);
                    res =pTable.my_odbc_find("CRM_CASE_ROLES_LEVELS", strFilter,0); dtRet = res.dtRet;
                }
                pTable.my_odbc_disconnect();
                pmInfo.mySession.Setex(strKey, dtRet, 60);     
            }
            
            int nFind = 0;
            String strField = "STATUS";
            if (nWF_Enable == 1)
            {
                strField = "PROCESS";
                nStatus = nProcess;
            }
            //有相符的条件，查找匹配最多，默认使用 ROLES=-1 CASETYPE=-1 PROCESS/STATUS =-1 的记录
            if (dtRet.getCount() > 0)
            {
               // String strFilter = myString.Format("ROLES={0} AND {1}={2}", pmAgent.nRoles, strField, nStatus);
              // DataTable drRet = dtRet.select(strFilter);
               String strFilter ="";
               DataTable drRet = dtRet.select( myString.Format("ROLES={0}", pmAgent.nRoles))
            		   								.select( myString.Format("{0}={1}",strField, nStatus));
           
                if (drRet.Rows().Count() < 1)
                {
                    //strFilter = myString.Format("ROLES={0} AND ({1}=-1)", pmAgent.nRoles, strField);
                    //drRet = dtRet.select(strFilter);
                	drRet = dtRet.select( myString.Format("ROLES={0}", pmAgent.nRoles))
										 .select( myString.Format("{0}=-1",strField));
                    if (drRet.Rows().Count()< 1)
                    {
                        //strFilter = myString.Format("ROLES=-1 AND ({0}={1})", strField, nStatus);
                        //drRet = dtRet.select(strFilter);
                    	drRet = dtRet.select("ROLES=-1").select(myString.Format("{0}={1}", strField, nStatus));
                        if (drRet.Rows().Count() < 1)
                        {
                            //strFilter = myString.Format("ROLES=-1 AND ({0}=-1)", strField);
                            //drRet = dtRet.select(strFilter);
                        	drRet = dtRet.select("ROLES=-1").select(myString.Format("{0}=-1", strField));
                        }
                    }
                }
                int nView, nTemp;
                for (int nIdx = 0; nIdx < drRet.Rows().Count(); nIdx++)
                {
                    nView = Functions.dtCols_nValue(drRet,nIdx, "LV_VIEW");
                    if (nView != 1) continue;
                    nTemp = Functions.dtCols_nValue(drRet,nIdx, strField);  //PROCESS/STATUS
                    if (nTemp == -1)
                    {
                        strReturn = strField + ">=0";
                        nFind = 2;
                        break;
                    }
                    if (strReturn.length() < 1)
                        strReturn = myString.Format("({0}={1})", strField, nStatus);
                    else
                        strReturn += myString.Format(" OR ({0}={1})", strField, nStatus);
                    nFind = 1;
                }
                //add by gaoww 20130421 解决选择显示全部，但是没设环节或状态为-1的权限，但是单独的环节或状态浏览所有权限都有的情况，显示不出所有记录的问题
                if (nFind == 0 && nStatus == -1)
                {
                    DataTable dtTemp;
                    if (nWF_Enable == 1)
                        dtTemp = Functions.dt_GetTable("SELECT PROCESS_ID AS PROCESS_ID FROM CRM_CASE_PROCESS WHERE CASETYPE='" + nType + "'", "", pmSys.conn_crm);
                    else
                        dtTemp = Functions.dt_GetTable("SELECT STATUS_ID AS PROCESS_ID FROM CRM_CASE_STATUS WHERE CASETYPE='" + nType + "'", "", pmSys.conn_crm);
                    int nLevel = 1;
                    for (DataRow myRow : dtTemp.Rows())
                    {
                        strKey = Functions.drCols_strValue(myRow, "PROCESS_ID");
                        strFilter = myString.Format("{0}={1}", strField, strKey);
                        drRet = dtRet.select(strFilter);
                        if (drRet.Rows().Count() > 0)
                        {
                            nView = Functions.dtCols_nValue(drRet,0, "LV_VIEW");
                            if (nView != 1)
                            {
                                nLevel = -1;  //只要有一个环节或状态没权限就不显示全部
                                break;
                            }
                        }
                        else
                        {
                            nLevel = -1;  //只要有一个环节或状态没权限就不显示全部
                            break;
                        }

                    }
                    if (nLevel == 1)
                    {
                        strReturn = strField + ">=0";
                        nFind = 1;
                    }
                }
            }
            //显示所有时，没有查看权限，可以查看与自己有关的
            String strOwner = myString.Format("(GHID='{0}' OR CURRENTGHID='{0}' OR CLOSE_GHID='{0}')", pmAgent.uid);
            if ((nProcess == -1) || (nStatus == -1))//显示所有
            {
                if (nFind == 0)
                {
                    strReturn = strOwner;
                }
                else if (nFind == 1)
                {
                    strReturn = myString.Format("(({0}) OR ({1}))", strReturn, strOwner);
                }
            }
            else//(nProcess/nStatus>0表示某一环节下的记录)
            {
                if (nFind == 0)
                {
                    strReturn = myString.Format("({0}={1}) AND ({2})", strField, nStatus, strOwner);
                }
                else if (nFind == 2)
                {
                    strReturn = myString.Format("({0}={1})", strField, nStatus);
                }
            }
            if (strReturn.length() < 1) strReturn = "1>1";
            return strReturn;
        }

        //读取该角色-编辑状态哪些字段不可见、只读、必填，哪些命令不可见、只读
        public HashMap get_edit_priv(service_info myCaseInfo,  HashMap htRet)
        {
           // boolean bReturn = false;

            DataTable dtRet;
            String strFilter = myString.Format("(ROLES={0} AND CASETYPE={1} AND STATUS={2})", pmAgent.nRoles, nType, myCaseInfo.nStatus);
            if (nWF_Enable == 1)
                strFilter = myString.Format("(ROLES={0} AND CASETYPE={1} AND PROCESS={2} AND PROCESS_STATUS={3} AND STATUS={4})", pmAgent.nRoles, nType, myCaseInfo.nProcess, myCaseInfo.nProcess_status, myCaseInfo.nStatus);

            my_odbc mydb = new my_odbc(pmSys.conn_crm);
            pmList res =mydb.my_odbc_find("CRM_CASE_FLD_PRIV", strFilter,0); dtRet = res.dtRet;
            int rc = res.nRet;
            if (rc != 1)//add by gaoww 20100226 如果没有符合条件的记录，则将status的值改为-1，检查是否有符合的记录，
            {
                strFilter = myString.Format("(ROLES={0} AND CASETYPE={1} AND STATUS={2})", pmAgent.nRoles, nType, "-1");
                if (nWF_Enable == 1)
                    strFilter = myString.Format("(ROLES={0} AND CASETYPE={1} AND PROCESS={2} AND PROCESS_STATUS={3} AND STATUS={4})", pmAgent.nRoles, nType, myCaseInfo.nProcess, myCaseInfo.nProcess_status, "-1");
                 res =mydb.my_odbc_find("CRM_CASE_FLD_PRIV", strFilter,0); dtRet = res.dtRet;
                rc = res.nRet;
                if (rc != 1)//add by gaoww 20100126 如果没有符合条件的记录，则将process_status的值改为-1,STATUS 为当前工单值，检查是否有符合的记录，
                {
                    if (nWF_Enable == 1)
                        strFilter = myString.Format("(ROLES={0} AND CASETYPE={1} AND PROCESS={2} AND PROCESS_STATUS={3} AND STATUS={4})", pmAgent.nRoles, nType, myCaseInfo.nProcess, "-1", myCaseInfo.nStatus);
                     res =mydb.my_odbc_find("CRM_CASE_FLD_PRIV", strFilter,0); dtRet = res.dtRet;
                    rc = res.nRet;
                    if (rc != 1)   //将status和process_status都改为-1检查是否有符合的记录
                    {
                        if (nWF_Enable == 1)
                            strFilter = myString.Format("(ROLES={0} AND CASETYPE={1} AND PROCESS={2} AND PROCESS_STATUS={3} AND STATUS={4})", pmAgent.nRoles, nType, myCaseInfo.nProcess, "-1", "-1");
                          res =mydb.my_odbc_find("CRM_CASE_FLD_PRIV", strFilter,0); dtRet = res.dtRet;
                        rc = res.nRet;
                    }
                }
            }
            mydb.my_odbc_disconnect();

            htRet = new HashMap();
            if (dtRet.getCount() > 0)
            {
                String strResult = "";
                strResult = Functions.dtCols_strValue(dtRet, "FLD_INVISIBLE");
                if (strResult.length() > 0)
                    htRet.put("FLD_INV", Arrays.asList(strResult.split("[,]")));
                strResult = Functions.dtCols_strValue(dtRet, "FLD_READONLY");
                if (strResult.length() > 0)
                    htRet.put("FLD_RDONLY", Arrays.asList(strResult.split("[,]")));
                strResult = Functions.dtCols_strValue(dtRet, "FLD_MUST");
                if (strResult.length() > 0)
                    htRet.put("FLD_MUST", Arrays.asList(strResult.split("[,]")));
                strResult = Functions.dtCols_strValue(dtRet, "CMD_INVISIBLE");
                if (strResult.length() > 0)
                    htRet.put("CMD_INV", Arrays.asList(strResult.split("[,]")));
                strResult = Functions.dtCols_strValue(dtRet, "CMD_READONLY");
                if (strResult.length() > 0)
                    htRet.put("CMD_RDONLY", Arrays.asList(strResult.split("[,]")));
            }
           /* if (htRet.size() > 0)
            {
                bReturn = true;
            }*/
            return htRet;
        }
                  

        /// <summary>
        /// 根据工单编号检查工单是否存在
        /// </summary>
        /// <param name="strCaseid"></param>
        /// <returns></returns>
        public boolean isExist_Case(String strCaseid)
        {
            boolean nResult = false;
            my_odbc pTable = new my_odbc(pmSys.conn_crm);
            int rc = pTable.my_odbc_find("SELECT CASEID FROM " + TableName + " WHERE CASEID='" + strCaseid + "'");
            if (rc > 0)
            {
                nResult = true;
            }
            pTable.my_odbc_disconnect();
            return nResult;
        }

        /// <summary>
        /// 获取工单的process和process_status值
        /// </summary>
        /// <param name="strCaseid"></param>
        /// <param name="htCase"></param>
        /// <returns></returns>
        public void Get_ProcessInfo(String strCaseid,  HashMap htCase)
        {
            String strProcess, strProcess_status;
            my_odbc pTable = new my_odbc(pmSys.conn_crm);
            int rc = pTable.my_odbc_find(TableName, "CASEID='" + strCaseid + "'");
            if (rc > 0)
            {
                strProcess = pTable.my_odbc_result("PROCESS");
                strProcess_status = pTable.my_odbc_result("PROCESS_STATUS");
                Functions.ht_SaveEx("PROCESS", strProcess,htCase);
                Functions.ht_SaveEx("PROCESS_STATUS", strProcess_status,htCase);
            }
            pTable.my_odbc_disconnect();
        }

        public int set_list_color( my_dataGrid mydg)
        {
            int nReturn = -1;
            String strKey = "dt_myCase_color_" + nType;
            DataTable dtRet = pmInfo.myKvdb.Get(strKey,null);//DataTable.class);// as DataTable;
            if (dtRet == null)
            {
                dtRet = new DataTable();
                my_odbc pTable = new my_odbc(pmSys.conn_crm);
                String strSql = myString.Format("SELECT STATUS_ID,COLOR_NAME FROM CRM_CASE_STATUS WHERE CASETYPE ='{0}'", nType);
                pmList res =pTable.my_odbc_find(strSql,0); dtRet = res.dtRet;
                pTable.my_odbc_disconnect();
                pmInfo.myKvdb.Setex(strKey, dtRet, 60);
            }

            if (dtRet.getCount() > 0)
            {
                String strFilter, strColorName;
                for (int i = 0; i < dtRet.getCount(); i++)
                {
                    strFilter = "$=" + Functions.dtCols_strValue(dtRet, i, "STATUS_ID");
                    strColorName = Functions.dtCols_strValue(dtRet, i, "COLOR_NAME");
                    mydg.set_rows_color("STATUS", strFilter, pmSys.c_Color.byName(strColorName),"STATUS");
                }
                nReturn = 1;
            }
            return nReturn;
        }

        /// <summary>
        /// 新增和修改工单时，增加历史记录
        /// </summary>
        /// <param name="nCmd">工单历史记录类型，0-新增工单，1-修改工单，2-删除工单</param>
        /// <param name="strCaseTable">工单表名</param>
        /// <param name="strCaseId">工单编号</param>
        /// <param name="htCase">要保存的工单内容</param>
        public void Add_Case_Hist(int nCmd, String strCaseTable, String strCaseId, HashMap htCase)
        {
            int nHist = 0;
            int nCaseType = 0;
            String strCaseTable_Hist = "CRM_CASE_HIST";
            String strDescName = "";

            //add by gaoww 20090327,增加记录工单历史记录功能。
            HashMap htCase_Hist = new HashMap(); //原有的工单记录
            HashMap htTemp = new HashMap();
            DataTable dtDesc;

            my_odbc pCase = new my_odbc(pmSys.conn_crm);
            //判断是否需要增加工单历史记录
            int rc = pCase.my_odbc_find("CRM_CASE_TABLE", "TABLE_NAME = '" + strCaseTable + "'");
            if (rc > 0)
            {
                nHist = pCase.my_odbc_result("LOG_HIST",0);
                nCaseType = pCase.my_odbc_result("CASETYPE",0);
                strDescName = pCase.my_odbc_result("DESC_NAME");
                dtDesc = fun_Form.get_desc_data(strDescName, "");
            }
            else
            {
                pCase.my_odbc_disconnect();
                return;
            }

            if (nHist == 0)
            {
                pCase.my_odbc_disconnect();
                return;
            }
            if (nCaseType != 0)
                strCaseTable_Hist += Functions.atos(nCaseType);

            htTemp.put("SDATE", DateTime.Now().ToString());
            htTemp.put("GHID", pmAgent.uid);
            if (htCase.containsKey("CASEID") == true)
                htTemp.put("CASEID", htCase.get("CASEID"));
            if (htCase.containsKey("USERID") == true)
                htTemp.put("USERID", htCase.get("USERID"));
            if (htCase.containsKey("CALLID") == true)
                htTemp.put("CALLID", htCase.get("CALLID"));
            if (htCase.containsKey("UNAME") == true)
                htTemp.put("UNAME", htCase.get("UNAME"));

            if (nCmd == 0)  //新增工单的工单的历史记录增加
            {
                Functions.ht_SaveEx("DESP", "新增工单",htTemp);

                pCase.my_odbc_addnew(strCaseTable_Hist, htTemp);
            }
            else if (nCmd == 1)  //修改工单的工单的历史记录增加
            {
                String strDesp = "";
                pmMap res =pCase.my_odbc_find("SELECT * FROM " + TableName + " WHERE CASEID='" + strCaseId + "'",true); htCase_Hist = res.htRet;
                for (Object entry : htCase.keySet())
                {
                    String key = entry.toString();
                    String key_name = "";
                    if (dtDesc != null)
                    {
                        //根据字段值，取出该字段的名称
                        DataTable myRow = dtDesc.select("FLD_VALUE='" + key + "'");
                        if (myRow.Rows().Count() > 0)
                            key_name =Functions.dtCols_strValue(myRow,0, "FLD_NAME");// myRow[0]["FLD_NAME"].ToString();  //modify by gaoww 20100118 
                    }

                    if (htCase_Hist.containsKey(entry) == true)
                    {
                        String strFilter_Hist = Functions.ht_Get_strValue(Functions.atos(entry), htCase_Hist);
                        String strFilter_New =Functions.ht_Get_strValue(Functions.atos(entry), htCase);
                        //判断如果是日期，需要将内容转换为统一的日期格式进行比较，避免新数据中单日或单月的数字前面加0，而数据库中去掉0，虽然实际一样，但是比较出来不一样的问题 
                        if (Functions.IsDateTime(strFilter_Hist) == true && Functions.IsDateTime(strFilter_New) == true)
                        {
                            strFilter_Hist = Functions.ConvertStrToDateTime(strFilter_Hist).ToString("yyyy-MM-dd HH:mm:ss");
                            strFilter_New = Functions.ConvertStrToDateTime(strFilter_New).ToString("yyyy-MM-dd HH:mm:ss");
                        }
                        //判断如果是日期，需要将内容转换为统一的日期格式进行比较，避免新数据中单日或单月的数字前面加0，而数据库中去掉0，虽然实际一样，但是比较出来不一样的问题 
                        if (Functions.isDate(strFilter_Hist) == true && Functions.isDate(strFilter_New) == true)
                        {
                            strFilter_Hist = Functions.ConvertStrToDateTime(strFilter_Hist).ToString("yyyy-MM-dd");
                            strFilter_New = Functions.ConvertStrToDateTime(strFilter_New).ToString("yyyy-MM-dd");

                        }
                        if (strFilter_Hist != strFilter_New)
                        {

                            if (strDesp.equals(""))
                                strDesp = "“" + key_name + "”由：" + strFilter_Hist + "，改为：" + strFilter_New + "；";
                            else
                                strDesp += "     “" + key_name + "”由：" + strFilter_Hist + "，改为：" + strFilter_New;
                        }
                    }

                }
                if (strDesp.equals(""))
                    strDesp = " 座席保存工单，但没做任何修改";
                else
                    strDesp = " 修改工单：" + strDesp;
                Functions.ht_SaveEx("DESP", strDesp,htTemp);
                pCase.my_odbc_addnew(strCaseTable_Hist, htTemp);
            }
            else if (nCmd == 2)
            {
                Functions.ht_SaveEx("DESP", "删除工单",htTemp);
                pCase.my_odbc_addnew(strCaseTable_Hist, htTemp);
            }
            pCase.my_odbc_disconnect();
        }


        //add by gaoww 20151021 服务管理定制，判断当前环节是否支持任务
        public int is_sm_support(int nProcess)
        {
            int nSupport = 0;
            my_odbc pTable = new my_odbc(pmSys.conn_crm);
            int rc = pTable.my_odbc_find("CRM_CASE_PROCESS", "CASETYPE='" + nType + "' AND PROCESS_ID='" + nProcess + "'");
            if (rc > 0)
            {                
                nSupport = pTable.my_odbc_result("SM_SUPPORT",0);
            }
            pTable.my_odbc_disconnect();
            return nSupport;
        }
    }
    //#endregion

  
   

