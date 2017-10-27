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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.pmAgent_info;
import com.CallThink.base.pmClass.pmSys;
import com.CallThink.ut_form.pmModel_form.fun_Form;
import com.ToneThink.DataTable.DataRow;
import com.ToneThink.DataTable.DataTable;
import com.ToneThink.DateTime.DateTime;
import com.ToneThink.ctsTools.Regex.Regex;
import com.ToneThink.ctsTools.WebUI.my_dataGrid;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.fun_json;
import com.ToneThink.ctsTools.myUtility.myString;
import com.ToneThink.ctsTools.myUtility.pmList;
import com.ToneThink.ctsTools.myUtility.pmMap;
import com.ToneThink.ctsTools.myUtility.pmRet;

    /// <summary>
    /// case_set_info 的摘要说明。
    /// </summary>
public class case_set_info
{ 
    public int nType;                       //工单类型
    public String TableName;          //工单表名
    public String TableKey;              //工单表的关键字，特殊情况下关键字不一定是CASEID； //add by gaoww 20130924
    public String CaseName;           //工单名称
    public int nWF_Enable;              //是否使用工作流  0-不使用  1-使用
    public String DescName;           //使用的表单描述表名称
    //public DataTable dtCase_type;   //工单类型表，对应  CRM_CASE_TABLE              ,条件： CASETYPE = " + nType_case
    //public DataTable dtPriv_fld;    //工单表单编辑权限限制表  对应 CRM_CASE_FLD_PRIV,条件： (ROLES={0} AND CASETYPE={1} AND STATUS={2})"
    public int nSubMenu;              //关联的子卡片页
    public int nUserId_lnk;           //是否关联客户资料 0-不关联 1-关联
    //public DataTable dtLevel_role;      //工单表单编辑权限限制表  对应 CRM_CASE_FLD_PRIV,条件： (ROLES={0} AND CASETYPE={1} AND STATUS={2})"
    public String FailReason = "";     //失败原因
    public int nForm_cols;             //编辑页面每行显示几列（0-自适应，1-4可设）

    pmAgent_info pmAgent;
    public case_set_info() //用于调用函数
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

    public case_set_info(int nType_case)
    {
        pmAgent = fun_main.GetParm();
        nType = nType_case;
        //String strSql = myString.Format("SELECT * FROM CRM_CASE_TABLE WHERE CASETYPE={0}", nType_case);
        DataTable dtCase_type = Functions.dt_GetTable("CRM_CASE_TABLE", " CASETYPE='"+nType_case+"'", pmSys.conn_crm);       
        		
        // if (rc > 0)
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
        String strRnd = String.valueOf(rand.nextInt(9999));//.PadLeft(4, '0');
        strRnd= padLeft(strRnd,4,"0");

        if (strCaseId .isEmpty())
        {
            //strBase = DateTime.Now.ToString("yyyyMMddHHmmss");
            strBase = DateTime.NowString("yyyyMMddHHmmss") +String.valueOf (nType);//modify by gaoww 20151119 增加工单类型作为工单编号一部分，避免重复
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
                	 strRnd = String.valueOf(rand.nextInt(9999));//.PadLeft(4, '0');
                     strRnd= padLeft(strRnd,4,"0");
                    //userid = userid.Substring(14) + strRnd;//modify  by gaoww 20090414 userid的值取得长度和位置不对
                    strCaseId = "JT"+strBase + strRnd;
                    continue;
                }
            }
            else break;
        }
        pCust.my_odbc_disconnect();
        return strCaseId;
    }

  ///获取数据
  		public int GetCaseCount()
  		{
  			my_odbc myTABLE=new my_odbc(pmSys.conn_crm);
  			int nCount=0;
  			int rc=myTABLE.my_odbc_find("SELECT  COUNT(*) AS CN FROM "+TableName+" WHERE SDATE='"+DateTime.Now().ToString("yyyyMMdd")+"'");
  			if (rc>0) {
  				nCount = Functions.atoi(myTABLE.my_odbc_result("CN"));
  			}
  			myTABLE.my_odbc_disconnect();
  			return nCount; 
  		}
    // 2014-08-12 专为 3T1 修改
    /// <summary>
    /// 生成一个接警单编号JJDBH add by gaoww 20130916
    /// </summary>
    /// <returns></returns>
    public  String GetNewJJBH(String strCaseId,String strXZQH)
    {
        int rc;
        int nCount=GetCaseCount();
        String strBase = strCaseId;
        if (strCaseId == "")
        {
            strBase = DateTime.Now().ToString("yyyyMMdd");
            if (nCount >= 500000)
      		  nCount = 0;
      
            NumberFormat  f=new DecimalFormat("00000000");
            strCaseId = "JT"+ strXZQH+ strBase +f.format(++nCount);
        }
        my_odbc pCust = new my_odbc(pmSys.conn_crm);
        for (int i = 0; i < 100; i++)
        {
            rc = pCust.my_odbc_find(TableName, TableKey + "='" + strCaseId + "'");
            pCust.my_odbc_disconnect();
          
            if (rc == 1)
            {
                if (i == 99)
                {
                    strCaseId = "";
                    break;
                }
                else
                {
                	  if (nCount >= 500000)
                		  nCount = 0;
                    NumberFormat  f=new DecimalFormat("00000000");
                    strCaseId = "JT"+ strXZQH+ strBase +f.format(nCount++);
                    continue;
                }
                
            }
            else break;
        }
         
        pCust.my_odbc_disconnect();
        return strCaseId;
    }
    
    // 2017-10-16 12328交通运输局
    /// <summary>
    /// 生成一个转办单号
    /// </summary>
    /// <returns></returns>
    public  String GetNewZB_CASE(String strCaseId,String strXZQH)
    {
        int rc;
        int nCount=GetCaseCount();
        String strBase = strCaseId;
        if (strCaseId == "")
        {
            strBase = DateTime.Now().ToString("yyyyMMdd");
            if (nCount >= 9000)
      		  nCount = 0;
      
            NumberFormat  f=new DecimalFormat("0000");
            strCaseId = "JT"+ strXZQH+ strBase +f.format(++nCount);
        }
        my_odbc pCust = new my_odbc(pmSys.conn_crm);
        for (int i = 0; i < 100; i++)
        {
            rc = pCust.my_odbc_find(TableName, TableKey + "='" + strCaseId + "'");
            pCust.my_odbc_disconnect();
          
            if (rc == 1)
            {
                if (i == 99)
                {
                    strCaseId = "";
                    break;
                }
                else
                {
                	  if (nCount >= 9000)
                		  nCount = 0;
                    NumberFormat  f=new DecimalFormat("0000");
                    strCaseId = "JT"+ strXZQH+ strBase +f.format(nCount++);
                    continue;
                }
                
            }
            else break;
        }
         
        pCust.my_odbc_disconnect();
        return strCaseId;
    }
    
    //根据工号,读取工单详细记录
    public case_info GetCaseRecord(String strCaseId)
    {
        case_info myCaseInfo = new case_info();
        if (myString.IsEmpty(strCaseId) == true) return myCaseInfo;

        myCaseInfo.strCaseId = strCaseId;
        HashMap htCase;
        my_odbc pCust = new my_odbc(pmSys.conn_crm);
        String strSelect = myString.Format("SELECT * FROM {0} WHERE {1}='{2}'", TableName, TableKey, strCaseId);
        pmMap mRet = pCust.my_odbc_find(strSelect,true);// TableName, "CASEID = '" + CaseId + "'");
        pCust.my_odbc_disconnect();
        int rc = mRet.nRet;       
        htCase=mRet.htRet;
        if (rc == 1)
        {
            //myCaseInfo.nStatus = Functions.atoi(Functions.ht_Get_strValue("STATUS", htCase));
            //myCaseInfo.nProcess = Functions.atoi(Functions.ht_Get_strValue("PROCESS", htCase));
            //myCaseInfo.nProcess_status = Functions.atoi(Functions.ht_Get_strValue("PROCESS_STATUS", htCase));
            //myCaseInfo.htCase = htCase;
            myCaseInfo = new case_info(htCase);
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

        if (htCase.isEmpty()) return nReturn;

        //检查工单完整性
        //if (judge_case_validity(htCase) == false) return nReturn;

        String strTemp;
        //统一时间格式，防止超界
        if (htCase.containsKey("SDATE") == true)
        {
            strTemp = Functions.ht_Get_strValue("SDATE", htCase).replace("-", "");
            Functions.ht_SaveEx("SDATE", strTemp, htCase);
        }
        if (htCase.containsKey("STIME") == true)
        {
            strTemp = Functions.ht_Get_strValue("STIME", htCase).replace(":", "");
            Functions.ht_SaveEx("STIME", strTemp, htCase);
        }
        if (htCase.containsKey("CLOSE_DATE") == true)
        {
            strTemp = Functions.ht_Get_strValue("CLOSE_DATE", htCase).replace("-", "");
            Functions.ht_SaveEx("CLOSE_DATE", strTemp, htCase);
        }

        my_odbc pCase = new my_odbc(pmSys.conn_crm);
        int nRet = pCase.my_odbc_find(TableName, TableKey + "='" + strCaseId + "'");
        //pCase.my_odbc_disconnect();
        if (nRet == 1)
        {
            //Functions.MsgBox("该工单已存在，请重新输入！");
            //return 0;

            //add by gaoww 20090327,增加记录工单历史记录功能。                
            Add_Case_Hist(1, TableName, strCaseId, htCase);

            int nStatus = Functions.atoi(Functions.ht_Get_strValue("STATUS", htCase));
            if (nStatus == 100)//e_Status_case.nClose)
            {
                Functions.ht_SaveEx("CLOSE_DATE", DateTime.NowString("yyyyMMdd HHmmss"),  htCase); //闭单日期
                Functions.ht_SaveEx("CLOSE_GHID", pmAgent.uid,  htCase); //闭单业务员
            }

            pCase.my_odbc_update(TableName, htCase, TableKey + "='" + strCaseId + "'");
            pCase.my_odbc_disconnect();
            
            //}
            //pCase.my_odbc_disconnect();
            String strCname = Functions.ht_Get_strValue("CASENAME", htCase);
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
            int a = pCase.my_odbc_addnew(TableName, htCase);
            pCase.my_odbc_disconnect();
            //fun_CRM.addnew_op_hist(3, "新增工单资料", strCname, "TABLE=" + TableName + ";KEY=" + strCaseId);
            nReturn = 1;
        }      
        
        return nReturn;
    }


    //1-OK  0-工单不存在 -1-失败  -2-没有权限 
    public int DelCaseRecord(String strCaseId)//, string uname)
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
        	fld_value= pCase.my_odbc_result("GHID" );
        	fld_value1= pCase.my_odbc_result("CURRENTGHID"  );
        	fld_value2= pCase.my_odbc_result("CLOSE_GHID"  );
        	userid=pCase.my_odbc_result("USERID" );
        	username=pCase.my_odbc_result("UNAME");
        	casename=pCase.my_odbc_result("CASENAME");
            //if(pmAgent.levels !=99)
            //delete by gaoww 20130530 所有工单权限都改用CRM_CASE_STATUS_PRIV表控制
            /*if (pmAgent.c_Levels.check_authority(e_Level_cust.case_admin) == false)
            {
                bool nFind = ((pmAgent.uid == fld_value) || (pmAgent.uid == fld_value1) || (pmAgent.uid == fld_value2));
                if (nFind == false)
                {
                    Functions.MsgBox("对不起，您没有权限删除此工单！");
                    pCase.my_odbc_disconnect();
                    return -2;
                }
            }*/
        }
        //if (Functions.MsgBox_OK("确实要删除<" + uname + ">的工单资料吗？") == true) //delete by gaoww 20111008 bs程序这里提示没有用，所以封上
        //{
        pCase.my_odbc_delete(TableName, "CASEID = '" + strCaseId + "'");
        pCase.my_odbc_disconnect();
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

    

    public boolean get_authority(case_info myCaseInfo, String strMask)
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
        DataTable dtRet = null ;//= pmInfo.mySession.Get<DataTable>(strKey);// as DataTable;
        if (dtRet == null)
        {
            dtRet = new DataTable();
            //string strFilter = String.Format("(ROLES={0} AND CASETYPE={1})", pmAgent.nRoles, nType);
            String strFilter = myString.Format("((ROLES={0} OR ROLES=-1) AND (CASETYPE={1}))", pmAgent.nRoles, nType); //modify by gaoww 20130421 应该把当前角色和-1角色的权限都读出来
            my_odbc pTable = new my_odbc(pmSys.conn_crm);
           pmList mRet = pTable.my_odbc_find("CRM_CASE_ROLES_LEVELS", strFilter, 0);
           int rc = mRet.nRet;
           dtRet=mRet.dtRet;
            if (rc != 1)
            {
                strFilter = myString.Format("((ROLES={0} OR ROLES=-1) AND (CASETYPE={1} OR CASETYPE=-1))", pmAgent.nRoles, nType);
                pTable.my_odbc_find("CRM_CASE_ROLES_LEVELS", strFilter, 0);
            }
            pTable.my_odbc_disconnect();
            //pmInfo.mySession.Setex(strKey, dtRet, 60);               
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
            /*string strFilter = String.Format("(STATUS={0})", nStatus);
            if (nWF_Enable == 1) strFilter = String.Format("(PROCESS={0})", nProcess);
            DataRow[] drRet = dtRet.Select(strFilter);
            if (drRet.Length < 1)
            {
                strFilter = String.Format("(STATUS=-1)");
                if (nWF_Enable == 1) strFilter = String.Format("(PROCESS=-1)");
                drRet = dtRet.Select(strFilter);
            }
            if (drRet.Length < 1) return nLevel;*/

            String strFilter = myString.Format("ROLES={0} AND ({1}={2})", pmAgent.nRoles, strField, nStatus);
            //DataTable drRet = dtRet.select(strFilter);
            DataTable drRet = Functions.dt_GetTable_select(dtRet, strFilter);
            //System.out.println(fun_json.DataTable_toJson(dtRet));
            //System.out.println(fun_json.DataTable_toJson(drRet));
            if (drRet.getCount() < 1)
            {
                strFilter = myString.Format("ROLES={0} AND ({1}=-1)", pmAgent.nRoles, strField);
                drRet = dtRet.select(strFilter);
                if (drRet.getCount() < 1)
                {
                    strFilter = myString.Format("ROLES=-1 AND ({0}={1})", strField, nStatus);
                    drRet = dtRet.select(strFilter);
                    if (drRet.getCount() < 1)
                    {
                        strFilter = myString.Format("ROLES=-1 AND ({0}=-1)", strField);
                        drRet = dtRet.select(strFilter);
                    }
                }
            }
            if (drRet.getCount() < 1) return nLevel;

            int nTemp = Functions.dtCols_nValue(drRet,0, "LV_ADDNEW");
            if (nTemp == 1) nLevel |= 1;
            nTemp = Functions.dtCols_nValue(drRet,0,"LV_UPDATE");
            if (nTemp == 1) nLevel |= 2;
            nTemp = Functions.dtCols_nValue(drRet,0,"LV_DELETE");
            if (nTemp == 1) nLevel |= 4;
            nTemp = Functions.dtCols_nValue(drRet,0, "LV_VIEW");
            if (nTemp == 1) nLevel |= 8;
            nTemp = Functions.dtCols_nValue(drRet,0,"LV_OUTPUT");
            if (nTemp == 1) nLevel |= 16;
            nTemp = Functions.dtCols_nValue(drRet,0, "LV_WF");
            if (nTemp == 1) nLevel |= 32;
        }

        //if (dtLevel_role == null)
        //{
        //    string strFilter = String.Format("(ROLES={0} AND CASETYPE={1} AND STATUS={2})", pmAgent.nRoles, nType, nProcess_or_status);
        //    if (nWF_Enable == 1)
        //        strFilter = String.Format("(ROLES={0} AND CASETYPE={1} AND PROCESS={2})", pmAgent.nRoles, nType, nProcess_or_status);
        //    my_odbc pTable = new my_odbc(pmSys.conn_crm);
        //    int rc = pTable.my_odbc_find("CRM_CASE_ROLES_LEVELS", strFilter, out dtLevel_role);
        //    if (rc != 1)
        //    {
        //        strFilter = String.Format("((ROLES={0} OR ROLES=-1) AND (CASETYPE={1} OR CASETYPE=-1) AND ({2}={3} OR {2}=-1))", pmAgent.nRoles, nType, (nWF_Enable == 1) ? "PROCESS" : "STATUS", nProcess_or_status);
        //        pTable.my_odbc_find("CRM_CASE_ROLES_LEVELS", strFilter, out dtLevel_role);
        //    }
        //    pTable.my_odbc_disconnect();
        //}
        return nLevel;
    }

    /// <summary>
    /// 根据座席员权限，读取工单列表
    /// </summary>
    /// <param name="nView_all">nView_all=1-所有可查看的记录 0-待处理的</param>
    /// <returns></returns>
    /*
    public string priv_get_list_filter(int nView_all)
    {
        string strReturn = "";
        string strOwner = "";

        if (nView_all == 1)
        {
            //检查角色权限，角色权限优先
            //if (pmAgent.c_Levels.check_authority(new string[] { e_Level_cust.case_view, e_Level_cust.case_admin }) == true)
            //{
            //    strReturn = "(1=1)";
            //    return strReturn;
            //}
            //没有权限，可以查看与自己有关的
            strOwner = String.Format("(GHID='{0}' OR CURRENTGHID='{0}' OR CLOSE_GHID='{0}')", pmAgent.uid);
        }

        if (nWF_Enable == 1) //支持工作流的工单
        {
            int nProcess, nProcess_status;
            string strFilter = String.Format("(ROLES={0} AND CASETYPE={1} AND ((LEVELS&3)<>0))", pmAgent.nRoles, nType);
            if (nView_all == 0)
                strFilter = String.Format("(ROLES={0} AND CASETYPE={1}) AND ((LEVELS&2)<>0)", pmAgent.nRoles, nType);
            my_odbc mydb = new my_odbc(pmSys.conn_crm);
            int rc = mydb.my_odbc_find("CRM_CASE_LIST_PRIV", strFilter);
            while (rc == 1)
            {
                mydb.my_odbc_result("PROCESS", out nProcess);
                mydb.my_odbc_result("PROCESS_STATUS", out nProcess_status);
                if (strReturn.Length < 1)
                {
                    if (nProcess_status == -1)  //如果process_status为-1,则显示所有环节的工单
                        strReturn = String.Format("(PROCESS={0})", nProcess);
                    else
                        strReturn = String.Format("(PROCESS={0} AND PROCESS_STATUS={1})", nProcess, nProcess_status);
                }
                else
                {
                    if (nProcess_status == -1)  //如果process_status为-1,则显示所有环节的工单
                        strReturn += String.Format(" OR (PROCESS={0})", nProcess);
                    else
                        strReturn += String.Format(" OR (PROCESS={0} AND PROCESS_STATUS={1})", nProcess, nProcess_status);
                }
                rc = mydb.my_odbc_nextrows(1);
            }
            mydb.my_odbc_disconnect();
            //if (strReturn.Length > 1)
            //{
            //    if (strOwner.Length > 1)
            //        strReturn = String.Format("(({0}) OR ({1}))", strReturn, strOwner);
            //}
            //else
            //{
            //    if (strOwner.Length > 1)
            //        strReturn = strOwner;
            //    else strReturn = "1>1";
            //}
        }
        else //不支持工作流的工单
        {
            int nStatus;
            string strFilter = String.Format("(ROLES={0} AND CASETYPE={1})", pmAgent.nRoles, nType);
            if (nView_all == 0)
                strFilter = String.Format("(ROLES={0} AND CASETYPE={1}) AND ((LEVELS&2)<>0)", pmAgent.nRoles, nType);
            my_odbc mydb = new my_odbc(pmSys.conn_crm);
            int rc = mydb.my_odbc_find("CRM_CASE_LIST_PRIV", strFilter);
            while (rc == 1)
            {
                mydb.my_odbc_result("STATUS", out nStatus);
                if (strReturn.Length < 1)
                    strReturn = String.Format("(PROCESS={0})", nStatus);
                else
                    strReturn += String.Format(" OR (PROCESS={0})", nStatus);
                rc = mydb.my_odbc_nextrows(1);
            }
            mydb.my_odbc_disconnect();

        }
        if (strReturn.Length > 1)
        {
            if (strOwner.Length > 1)
                strReturn = String.Format("(({0}) OR ({1}))", strReturn, strOwner);
        }
        else
        {
            if (strOwner.Length > 1)
                strReturn = strOwner;
            else strReturn = "1>1";
        }
        return strReturn;
    }
    */
    /// <summary>
    /// 根据座席员角色权限，读取工单列表条件
    /// </summary>
    /// <param name="nProcess">环节(适用有工作流)，-1：查所有环节</param>
    /// <param name="nStatus">环节(适用无工作流)，-1：查所有状态 </param>
    /// <returns>条件</returns>
    public String get_list_filter(int nProcess, int nStatus)
    {
        /*
        if (nWF_Enable == 1) //支持工作流的工单
        {
            int nProcess, nProcess_status;
            string strFilter = String.Format("(ROLES={0} AND CASETYPE={1} AND LV_VIEW=1)", pmAgent.nRoles, nType);
            my_odbc mydb = new my_odbc(pmSys.conn_crm);
            int rc = mydb.my_odbc_find("CRM_CASE_ROLES_LEVELS", strFilter);
            while (rc == 1)
            {
                mydb.my_odbc_result("PROCESS", out nProcess);
                //mydb.my_odbc_result("PROCESS_STATUS", out nProcess_status);
                if (strReturn.Length < 1)
                    strReturn = String.Format("(PROCESS={0})", nProcess);
                else
                    strReturn += String.Format(" OR (PROCESS={0})", nProcess);
                rc = mydb.my_odbc_nextrows(1);
            }
            mydb.my_odbc_disconnect();
        }
        else //不支持工作流的工单
        {
            int nStatus;
            string strFilter = String.Format("(ROLES={0} AND CASETYPE={1} AND LV_VIEW=1)", pmAgent.nRoles, nType);
            my_odbc mydb = new my_odbc(pmSys.conn_crm);
            int rc = mydb.my_odbc_find("CRM_CASE_ROLES_LEVELS", strFilter);
            while (rc == 1)
            {
                mydb.my_odbc_result("STATUS", out nStatus);
                if (strReturn.Length < 1)
                    strReturn = String.Format("(STATUS={0})", nStatus);
                else
                    strReturn += String.Format(" OR (STATUS={0})", nStatus);
                rc = mydb.my_odbc_nextrows(1);
            }
            mydb.my_odbc_disconnect();

        }
        */
        String strReturn = "";
        //权限表读入内存数据库
        //pmInfo.mySession.SessionId = pmAgent.uid;
        //string strKey = "dt_myCase_level_" + nType;
        DataTable dtRet = null;// = pmInfo.mySession.Get<DataTable>(strKey);// as DataTable;
        if (dtRet == null) 
        {
            dtRet = new DataTable();
            //string strFilter = String.Format("(ROLES={0} AND CASETYPE={1})", pmAgent.nRoles, nType);
            String strFilter = myString.Format("((ROLES={0} OR ROLES=-1) AND (CASETYPE={1}))", pmAgent.nRoles, nType); //modify by gaoww 20130421 应该把当前角色和-1角色的权限都读出来
            my_odbc pTable = new my_odbc(pmSys.conn_crm);
            pmList mRet = pTable.my_odbc_find("CRM_CASE_ROLES_LEVELS", strFilter, 0);
            int rc = mRet.nRet;
            dtRet=mRet.dtRet;
            if (rc != 1)
            {
                strFilter = myString.Format("((ROLES={0} OR ROLES=-1) AND (CASETYPE={1} OR CASETYPE=-1))", pmAgent.nRoles, nType);
                mRet = pTable.my_odbc_find("CRM_CASE_ROLES_LEVELS", strFilter, 0);
                rc = mRet.nRet;
                dtRet=mRet.dtRet;
            }
            pTable.my_odbc_disconnect();
            //pmInfo.mySession.Setex(strKey, dtRet, 60);                
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
            /*string strFilter = String.Format("({0}={1})", strField, nStatus);
            DataRow[] drRet = dtRet.Select(strFilter);
            if (drRet.Length < 1)
            {
                strFilter = String.Format("({0}=-1)", strField);
                drRet = dtRet.Select(strFilter);
            }*/
            String strFilter = myString.Format("ROLES={0} AND ({1}={2})", pmAgent.nRoles, strField, nStatus);
            
            DataTable drRet = dtRet.select(strFilter);
            if (drRet.getCount() < 1)
            {
                strFilter = myString.Format("ROLES={0} AND ({1}=-1)", pmAgent.nRoles, strField);
                drRet = dtRet.select(strFilter);
                if (drRet.getCount() < 1)
                {
                    strFilter = myString.Format("ROLES=-1 AND ({0}={1})", strField, nStatus);
                    drRet = dtRet.select(strFilter);
                    if (drRet.getCount() < 1)
                    {
                        strFilter = myString.Format("ROLES=-1 AND ({0}=-1)", strField);
                        drRet = dtRet.select(strFilter);
                    }
                }
            }
            int nView, nTemp;
            for (int nIdx = 0; nIdx < drRet.getCount(); nIdx++)
            {
                nView = Functions.dtCols_nValue(drRet,nIdx, "LV_VIEW");
                if (nView != 1) continue;
                nTemp = Functions.dtCols_nValue(drRet,nIdx,strField);  //PROCESS/STATUS
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
                 for (DataRow myRow:dtTemp.Rows())
                 {
                     String strKey = Functions.drCols_strValue(myRow, "PROCESS_ID");
                     strFilter = myString.Format("({0}={1})", strField, strKey);
                     drRet = dtRet.select(strFilter);
                     if (drRet.getCount() > 0)
                     {
                         nView = Functions.dtCols_nValue(drRet, "LV_VIEW");
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
    public boolean get_edit_priv(case_info myCaseInfo,  HashMap htRet)
    {
    	boolean bReturn = false;

        DataTable dtRet;
        String strFilter = myString.Format("(ROLES={0} AND CASETYPE={1} AND STATUS={2})", pmAgent.nRoles, nType, myCaseInfo.nStatus);
        if (nWF_Enable == 1)
            strFilter = myString.Format("(ROLES={0} AND CASETYPE={1} AND PROCESS={2} AND PROCESS_STATUS={3} AND STATUS={4})", pmAgent.nRoles, nType, myCaseInfo.nProcess, myCaseInfo.nProcess_status, myCaseInfo.nStatus);

        my_odbc mydb = new my_odbc(pmSys.conn_crm);
        pmList mRet =  mydb.my_odbc_find("CRM_CASE_FLD_PRIV", strFilter, 0);
        int rc=mRet.nRet;
        dtRet =mRet.dtRet;
        if (rc != 1)//add by gaoww 20100226 如果没有符合条件的记录，则将status的值改为-1，检查是否有符合的记录，
        {
            strFilter = myString.Format("(ROLES={0} AND CASETYPE={1} AND STATUS={2})", pmAgent.nRoles, nType, "-1");
            if (nWF_Enable == 1)
                strFilter = myString.Format("(ROLES={0} AND CASETYPE={1} AND PROCESS={2} AND PROCESS_STATUS={3} AND STATUS={4})", pmAgent.nRoles, nType, myCaseInfo.nProcess, myCaseInfo.nProcess_status, "-1");
            mRet  = mydb.my_odbc_find("CRM_CASE_FLD_PRIV", strFilter,0);
            rc=mRet.nRet;
            dtRet =mRet.dtRet;
            if (rc != 1)//add by gaoww 20100126 如果没有符合条件的记录，则将process_status的值改为-1,STATUS 为当前工单值，检查是否有符合的记录，
            {
                if (nWF_Enable == 1)
                    strFilter = myString.Format("(ROLES={0} AND CASETYPE={1} AND PROCESS={2} AND PROCESS_STATUS={3} AND STATUS={4})", pmAgent.nRoles, nType, myCaseInfo.nProcess, "-1", myCaseInfo.nStatus);
                mRet = mydb.my_odbc_find("CRM_CASE_FLD_PRIV", strFilter, 0);
                rc=mRet.nRet;
                dtRet =mRet.dtRet;
                if (rc != 1)   //将status和process_status都改为-1检查是否有符合的记录
                {
                    if (nWF_Enable == 1)
                        strFilter = myString.Format("(ROLES={0} AND CASETYPE={1} AND PROCESS={2} AND PROCESS_STATUS={3} AND STATUS={4})", pmAgent.nRoles, nType, myCaseInfo.nProcess, "-1", "-1");
                    mRet = mydb.my_odbc_find("CRM_CASE_FLD_PRIV", strFilter, 0);
                    rc=mRet.nRet;
                    dtRet =mRet.dtRet;
                }
            }
        }
        mydb.my_odbc_disconnect();

        htRet = new HashMap();
        if (dtRet.getCount()> 0)
        {
            String strResult = "";
            strResult = Functions.dtCols_strValue(dtRet, "FLD_INVISIBLE");
            if (strResult.length() > 0)
            {
            	List<String> listA = Arrays.asList(strResult.split(","));
            	List<String> listB = new ArrayList<String>(listA);
            	htRet.put("FLD_INV", listB);
            }
            strResult = Functions.dtCols_strValue(dtRet, "FLD_READONLY");
            if (strResult.length() > 0)
            {
            	List<String> listA = Arrays.asList(strResult.split(","));
             	List<String> listB = new ArrayList<String>(listA);
                htRet.put("FLD_RDONLY", listB);
            }
            strResult = Functions.dtCols_strValue(dtRet, "FLD_MUST");
            if (strResult.length() > 0)
                {
            	List<String> listA = Arrays.asList(strResult.split(","));
            	List<String> listB = new ArrayList<String>(listA);
            	htRet.put("FLD_MUST",listB);
                }
            strResult = Functions.dtCols_strValue(dtRet, "CMD_INVISIBLE");
            if (strResult.length() > 0)
            {
            	List<String> listA = Arrays.asList(strResult.split(","));
            	List<String> listB = new ArrayList<String>(listA);
            	htRet.put("CMD_INV",listB);
            }
            strResult = Functions.dtCols_strValue(dtRet, "CMD_READONLY");
            if (strResult.length() > 0)
            {
            	List<String> listA = Arrays.asList(strResult.split(","));
            	List<String> listB = new ArrayList<String>(listA);
            	htRet.put("CMD_RDONLY",listB);
            }
        }
        if (htRet.isEmpty() ==false)
        {
            bReturn = true;
        }
        return bReturn;
    }

    /*
    /// <summary>
    /// 获取该类工单的处理权限
    /// </summary>
    /// <param name="strCaseid">工单编号</param>
    public int Get_CaseLevels(string strCaseid)
    {
        case_info myInfo = GetCaseRecord(strCaseid);
        return Get_CaseLevels(nType, myInfo.nStatus, myInfo.nProcess, myInfo.nProcess_status);
    }
    */
    /*
    public int priv_get_level(case_info myInfo)
    {
        //case_info myInfo = GetCaseRecord(strCaseid);
        return priv_get_level(nType, myInfo.nStatus, myInfo.nProcess, myInfo.nProcess_status);
    }

    /// <summary>
    /// 获取该类工单的处理权限
    /// </summary>
    /// <param name="nType_case">工单类型</param>
    /// <param name="nStatus">状态</param>
    /// <param name="nProcess">环节</param>
    /// <param name="nProcess_status">环节状态</param>
    /// <returns>0-无权使用 1-只读  2-读写</returns>
    public int priv_get_level(int nType_case, int nStatus, int nProcess, int nProcess_status)
    {
        int nReturn = 0;
        int nLevels = -1;

        string strFilter = String.Format("(ROLES={0} AND CASETYPE={1} AND STATUS={2})", pmAgent.nRoles, nType_case, nStatus);
        if (nWF_Enable == 1)
            strFilter = String.Format("(ROLES={0} AND CASETYPE={1} AND PROCESS={2} AND PROCESS_STATUS={3})", pmAgent.nRoles, nType_case, nProcess, nProcess_status);
        my_odbc pTable = new my_odbc(pmSys.conn_crm);
        int rc = pTable.my_odbc_find("CRM_CASE_LIST_PRIV", strFilter);
        if (rc > 0)
        {
            pTable.my_odbc_result("LEVELS", out nLevels);
        }
        else
        {
            strFilter = String.Format("(ROLES={0} AND CASETYPE={1} AND STATUS={2})", pmAgent.nRoles, nType, "-1");
            if (nWF_Enable == 1)
                strFilter = String.Format("(ROLES={0} AND CASETYPE={1} AND PROCESS={2} AND PROCESS_STATUS={3})", pmAgent.nRoles, nType_case, nProcess, "-1");
            rc = pTable.my_odbc_find("CRM_CASE_LIST_PRIV", strFilter);
            if (rc > 0)
            {
                pTable.my_odbc_result("LEVELS", out nLevels);
            }
        }
        pTable.my_odbc_disconnect();
        if ((nLevels & 2) != 0) nReturn = 2;
        else if ((nLevels & 1) != 0) nReturn = 1;
        return nReturn;
    }


    //检查业务员对该工单的权限，nType=0-增加 1-删除 2-修改
    public bool priv_crud_record(string strCaseId, int nType)
    {
        bool bReturn = true;
        if (pmAgent.c_Levels.check_authority(e_Level_cust.case_admin) == false)
        {
            string strPriv = e_Level_cust.case_del;
            if (nType == 2) strPriv = e_Level_cust.case_modi;
            if (pmAgent.c_Levels.check_authority(strPriv) == false)
            {
                //  Functions.MsgBox("对不起，您没有删除此工单的权限！");
                bReturn = false;
            }
            else
            {
                my_odbc pTemp = new my_odbc(pmSys.conn_crm);
                int rc = pTemp.my_odbc_find(TableName, "CASEID='" + strCaseId + "'");
                if (rc > 0)
                {
                    string strGhid = "", strGhid_close = "", strGhid_current = "";
                    pTemp.my_odbc_result("GHID", out strGhid);
                    pTemp.my_odbc_result("CLOSE_GHID", out strGhid_close);
                    pTemp.my_odbc_result("CURRENTGHID", out strGhid_current);
                    if (pmAgent.uid != strGhid && pmAgent.uid != strGhid_close && pmAgent.uid != strGhid_current)
                    {
                        //Functions.MsgBox("对不起，您没有删除此工单的权限！");
                        //pTemp.my_odbc_disconnect();
                        bReturn = false;
                    }
                }
                pTemp.my_odbc_disconnect();
            }
        }
        return bReturn;
    }
    */

    /// <summary>
    /// 根据工单编号检查工单是否存在
    /// </summary>
    /// <param name="strCaseid"></param>
    /// <returns></returns>
    public boolean isExist_Case(String strCaseid)
    {
    	boolean nResult = false;
        my_odbc pTable = new my_odbc(pmSys.conn_crm);
        int rc  = pTable.my_odbc_find("SELECT CASEID FROM " + TableName + " WHERE CASEID='" + strCaseid + "'");
        pmList mRet = pTable.my_odbc_find("SELECT * FROM " + TableName + " WHERE CASEID='" + strCaseid + "'",0);
        rc = mRet.nRet;
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
    public pmRet<Integer,HashMap>  Get_ProcessInfo(String strCaseid,  HashMap htCase)
    {
        String strProcess, strProcess_status;
        int nReturn=0;
        pmRet<Integer,HashMap> mRet = new pmRet<Integer, HashMap>(nReturn,htCase);
    	
        my_odbc pTable = new my_odbc(pmSys.conn_crm);
        int rc = pTable.my_odbc_find(TableName, "CASEID='" + strCaseid + "'");
        if (rc > 0)
        {
        	strProcess=pTable.my_odbc_result("PROCESS" );
        	strProcess_status=pTable.my_odbc_result("PROCESS_STATUS" );
            Functions.ht_SaveEx("PROCESS", strProcess,  htCase);
            Functions.ht_SaveEx("PROCESS_STATUS", strProcess_status,  htCase);
        }
        pTable.my_odbc_disconnect();
        return mRet;
    }

    public  pmRet<Integer, my_dataGrid>  set_list_color( my_dataGrid mydg)
    {
        int nReturn = -1;
    	pmRet<Integer, my_dataGrid> mRet = new pmRet<Integer, my_dataGrid>(nReturn,mydg);
    	
    	String strKey = "dt_myCase_color_" + nType;
        DataTable dtRet = null;// = pmInfo.myKvdb.Get<DataTable>(strKey);// as DataTable;
        if (dtRet == null)
        {
            dtRet = new DataTable();
            my_odbc pTable = new my_odbc(pmSys.conn_crm);
            String strSql = myString.Format("SELECT STATUS_ID,COLOR_NAME FROM CRM_CASE_STATUS WHERE CASETYPE ='{0}'", nType);
            pmList mRet1 =  pTable.my_odbc_find(strSql, 0);
            dtRet = mRet1.dtRet;
            pTable.my_odbc_disconnect();
            //pmInfo.myKvdb.Setex(strKey, dtRet, 60);
        }

        if (dtRet.getCount()> 0)
        {
            String strFilter, strColorName;
            for (int i = 0; i < dtRet.getCount(); i++)
            {
                strFilter = "$=" + Functions.dtCols_strValue(dtRet, i, "STATUS_ID");
                strColorName = Functions.dtCols_strValue(dtRet, i, "COLOR_NAME");
               // mydg.set_rows_color("STATUS", strFilter, pmSys.c_Color.byName(strColorName), "STATUS");
            }
            nReturn = 1;
        }
        return mRet;
    }

    /// <summary>
    /// 新增和修改工单时，增加历史记录
    /// </summary>
    /// <param name="nCmd">工单历史记录类型，0-新增工单，1-修改工单，2-删除工单</param>
    /// <param name="strCaseTable">工单表名</param>
    /// <param name="strCaseId">工单编号</param>
    /// <param name="htCase">要保存的工单内容</param>
    public void Add_Case_Hist(int nCmd, String strCaseTable, String strCaseId, HashMap  htCase)
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
        	 nHist=pCase.my_odbc_result("LOG_HIST",0);
        	 nCaseType=pCase.my_odbc_result("CASETYPE",0 );
        	 strDescName=pCase.my_odbc_result("DESC_NAME" );
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
            strCaseTable_Hist +=String.valueOf(nCaseType);
       
        htTemp.put("SDATE", DateTime.NowString("yyyyMMdd"));
        htTemp.put("GHID", pmAgent.uid);
        if (htCase.containsKey("CASEID") == true)
            htTemp.put("CASEID", Functions.ht_Get_strValue("CASEID",htCase));
        if (htCase.containsKey("USERID") == true)
            htTemp.put("USERID", Functions.ht_Get_strValue("USERID",htCase));
        if (htCase.containsKey("CALLID") == true)
            htTemp.put("CALLID", Functions.ht_Get_strValue("CALLID",htCase));
        if (htCase.containsKey("UNAME") == true)
            htTemp.put("UNAME", Functions.ht_Get_strValue("UNAME",htCase));

        if (nCmd == 0)  //新增工单的工单的历史记录增加
        {
            Functions.ht_SaveEx("DESP", "新增工单", htTemp);

            pCase.my_odbc_addnew(strCaseTable_Hist, htTemp);
        }
        else if (nCmd == 1)  //修改工单的工单的历史记录增加
        {
            String strDesp = "";
            pmMap mRet =   pCase.my_odbc_find("SELECT * FROM " + TableName + " WHERE CASEID='" + strCaseId + "'", true);
            htCase=mRet.htRet;           
           
            java.util.Iterator item = htCase.entrySet().iterator();
            //foreach (DictionaryEntry entry in htCase.keySet())           
            while((item.hasNext())) {
            	String value  = item.next().toString();
            	String key =Functions.Substring(value, "", "=");
                String key_name = "";
                //fn_GetFilter_Name(key, nCaseType, ref key_name);
                if (dtDesc != null)
                {
                    //根据字段值，取出该字段的名称
                    DataTable myRow = dtDesc.select("FLD_VALUE='" + key + "'");
                    if (myRow.getCount() > 0)
                        key_name =Functions.dtCols_strValue(myRow, "FLD_NAME") ;  //modify by gaoww 20100118 
                }

                if (htCase_Hist.containsKey(key) == true)
                {
                    String strFilter_Hist =Functions.ht_Get_strValue(key,htCase_Hist).trim();
                    String strFilter_New = Functions.ht_Get_strValue(key,htCase).trim();
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

                        if (strDesp.isEmpty())
                            strDesp = "“" + key_name + "”由：" + strFilter_Hist + "，改为：" + strFilter_New + "；";
                        else
                            strDesp += "     “" + key_name + "”由：" + strFilter_Hist + "，改为：" + strFilter_New;
                    }
                }

            }
            if (strDesp.isEmpty())
                strDesp = " 座席保存工单，但没做任何修改";
            else
                strDesp = " 修改工单：" + strDesp;
            Functions.ht_SaveEx("DESP", strDesp,  htTemp);
            pCase.my_odbc_addnew(strCaseTable_Hist, htTemp);
        }
        else if (nCmd == 2)
        {
            Functions.ht_SaveEx("DESP", "删除工单",  htTemp);
            pCase.my_odbc_addnew(strCaseTable_Hist, htTemp);
        }
        pCase.my_odbc_disconnect();
    }
    /*
    public static DataTable Refresh_CaseTable()
    {
        DataTable dtCase_table;
        string strTable;
        my_odbc pCase = new my_odbc(pmSys.conn_crm);
        int rc = pCase.my_odbc_find("CRM_CASE_TABLE", "", out dtCase_table);
        if (rc > 0)
        {
            foreach (DataRow myRow in dtCase_table.Rows)
            {
                strTable = myRow["TABLE_NAME"].ToString();
                if (Functions.isExist_Table(strTable, pmSys.conn_crm) == false)
                {
                    myRow.Delete();
                }
            }
            dtCase_table.AcceptChanges();
        }
        return dtCase_table;
    }
    public static DataTable Get_CaseType(int nType)
    {
        DataTable dtReturn = new DataTable();
        DataTable dtCase_table = Refresh_CaseTable();
        dtReturn = dtCase_table.Clone();
        DataRow[] drRet = dtCase_table.Select("CASETYPE=" + nType);
        if (drRet.Length > 0)
            dtReturn.ImportRow(drRet[0]);
        return dtReturn;
    }
    */
    
    public String padLeft(String oriStr,int len,String alexin){
  	  int nlen = oriStr.length();    	
  	  String newStr="";
  	  if(nlen < len){
  	       for(int i=0;i<len-nlen;i++){
  		   newStr = newStr+alexin;
  	       }
  	  }
  	  newStr = oriStr + newStr;
  	  return newStr;
  }
}
