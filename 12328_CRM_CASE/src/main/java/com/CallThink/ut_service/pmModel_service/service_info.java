package com.CallThink.ut_service.pmModel_service;

import java.util.HashMap;

import com.ToneThink.ctsTools.myUtility.Functions;

//#region 一个的工单详细信息-结构
public class service_info
{
    //public bool? HasValue;     //标识是否为有效值 使用可空类型Nullable<T>，默认值为null，等效于 Nullable<bool> HasValue = null
    public String strCaseId;             //工单号
    //private string m_strName;          //操作员姓名
    public int nType;                    //工单类型
    public int nStatus;                  //工单状态
    public int nProcess;                 //业务环节
    public int nProcess_status;          //业务环节-状态
    public HashMap htCase;             //工单数据_Key：字段  Value：字段值
    private int m_nLevel;                //该角色使用工单的权限  0-无权使用 1-只读  2-读写
    //private int m_nAccept_auto;        //是否自动签收，1-是 0-否。
    public boolean isExist;                 //工单是否存在  false-不存在  true-存在 

    /// <summary>
    /// 构造函数，读取该工号所在组
    /// </summary>
    /// <param name="strUid">业务员工号</param>
    public service_info()
    {
        //HasValue = true;
        strCaseId = "";
        nType = 0;
        nStatus = 0;
        nProcess = 0;
        nProcess_status = 0;
        htCase = new HashMap();
        m_nLevel = -1;
        //m_nAccept_auto = -1;
        isExist = false;
    }
    public service_info(HashMap htInfo)
    {
        htCase = htInfo;
        strCaseId = Functions.ht_Get_strValue("CASEID", htCase);
        nStatus = Functions.atoi(Functions.ht_Get_strValue("STATUS", htCase));
        nType = Functions.atoi(Functions.ht_Get_strValue("CASETYPE", htCase));

        nProcess = Functions.atoi(Functions.ht_Get_strValue("PROCESS", htCase));
        nProcess_status = Functions.atoi(Functions.ht_Get_strValue("PROCESS_STATUS", htCase));
        m_nLevel = -1;
        //m_nAccept_auto = -1;
        isExist = true;
    }
   
    public String Get(String strFld_name)
    {
        return Functions.ht_Get_strValue(strFld_name, htCase);
    }

    public int Set(String strFld_name, Object objFld_value)
    {
        return Functions.ht_SaveEx(strFld_name, objFld_value,htCase);
    }

}
// #endregion
