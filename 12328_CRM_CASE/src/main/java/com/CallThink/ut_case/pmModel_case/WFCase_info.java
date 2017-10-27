///########################################################################################
/// Copyright (C) 2000, ToneThink.Soft  All Rights Reserved. 
/// 文件创建时间：2000-06-05
///   文件创建人：peng
/// 文件功能描述：与UltraCRM相关的主要函数
/// 
///     维护记录：
/// 2007-07-08 将该文件拆分，只处理Customer Case
/// 2007-08-17 与标准版相同
///            修改Fill_Field(),Fill_Grid() 函数 
///#########################################################################################
package com.CallThink.ut_case.pmModel_case;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import com.ToneThink.DataTable.DataTable;
import com.CallThink.base.pmClass.pmSys;
import com.ToneThink.DateTime.DateTime;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.pmList;
import com.ToneThink.ctsTools.myUtility.pmRef;
import com.ToneThink.ctsTools.Regex.Regex;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.myString;

    //#region 工单-工作流相关函数
    public class WFCase_info
    {
        private int m_nType;                 //工单类型
        private String m_TableName;          //指定类型-工单表名

        private int m_nStatus;                  //工单状态
        private int m_nProcess;                 //业务环节
        private int m_nProcess_status;          //业务环节-状态
        private int m_WF_Index;                 //工作流序号 add by gaoww 20130527
        private String m_strCaseId;             //工单号
        private String m_strCaseName;           //工单名称

        //private int m_nAccept_auto;             //1-自动签收

        //private List<int> m_dyProcess;
        private int m_nFirst_Id = 0, m_nLast_Id = 0;   //第一环节、最后一环节 ID


        public WFCase_info(case_set_info myCase, case_info myCaseInfo)
        {
            Fill_info( myCase,  myCaseInfo);
        }

        private void Fill_info(case_set_info myCase, case_info myCaseInfo)
        {
            m_TableName = myCase.TableName;
            m_nType = myCase.nType;

            m_nProcess = myCaseInfo.nProcess;
            m_nProcess_status = myCaseInfo.nProcess_status;  //0-未签收 1-已签收  2-拒签收 3-已提交 4-被退回
            //m_nAccept_auto = myCaseInfo.nAccept_auto;
            m_strCaseId = myCaseInfo.strCaseId;
            m_strCaseName = myCaseInfo.Get("CASENAME");
            m_nStatus = myCaseInfo.nStatus;
            m_WF_Index = 0;
            //m_dyProcess = new List<int>();
            //get_process_info(m_nType);

            if (myCase.nWF_Enable == 1)
            {
                DataTable dtRet;
                my_odbc pProcess = new my_odbc(pmSys.conn_crm);
                pmList res =pProcess.my_odbc_find("CRM_CASE_PROCESS", "CASETYPE='" + m_nType + "' ORDER BY WF_INDEX ASC",0); dtRet = res.dtRet;
                int rc = res.nRet;
                pProcess.my_odbc_disconnect();
                int nNumCol = dtRet.getCount();

                if (nNumCol > 0) //取出第一环节、最后一环节 ID
                {
                    m_nFirst_Id = Functions.dtCols_nValue(dtRet, 0, "PROCESS_ID");
                    m_nLast_Id = Functions.dtCols_nValue(dtRet, nNumCol - 1, "PROCESS_ID");

                    //add by gaoww 20130527 增加取工作流序号
                    DataTable myRow = dtRet.select("PROCESS_ID='" + m_nProcess + "'");
                    if (myRow.getCount() > 0)
                        m_WF_Index = Functions.dtCols_nValue(myRow, "WF_INDEX");
                }
            }
        }

        //nCommand：接收命令：1-签收、2-拒签；   发出命令：3-提交、4-退回（已执行过的节点）、5-跳转(任意节点)、
        //nProcess_new = 0，不起作用 
        public int Submit_process(int nCommand, String strUid, String strUid_recv, int nProcess_new)
        {
            int nReturn = -1;
            int nProcess = m_nProcess;  //当前环节
            String strDate_exp = "";
            int nAccept_auto = 0;// 提交时检查 下一环节是否自动签收

            //检查当前状态
            //add by gaoww 20151111增加工作流命令和工单内容关联
            String strBookId = "";
            if (nCommand < 6) strBookId = String.format("%02d%02d%02d", m_nType, nProcess, nCommand);
            if (nCommand == 1)
            {
                if (nProcess == m_nFirst_Id) return nReturn; //第一环节不能签收,（直接提交）
                nProcess_new = m_nProcess;
                //case_update(nProcess_new, 1, -1, strUid);
                case_update(nProcess_new, 1, -1, strUid_recv,strBookId); //modify by gaoww 20130407
           
            }
            else if (nCommand == 2)
            {
                if (nProcess == m_nFirst_Id) return nReturn; //第一环节不能拒签
                //nProcess_new = get_process_index(0);
                case_update(nProcess_new, 2, -1, strUid_recv, strBookId);
            }
            else if (nCommand == 3)
            {
                if (nProcess == m_nLast_Id)  //最后一环节：（已提交+工单状态为闭单）
                {
                    nProcess_new = m_nProcess;
                    case_update(nProcess_new, 3, 100, strUid, strBookId);
                }
                else
                {
                	pmRef<Integer> mRef = new pmRef(0);
                    nProcess_new = get_process_index(1, mRef);nAccept_auto=mRef.oRet;
                    case_update(nProcess_new, 0, -1, strUid_recv, strBookId);
                }
            }
            else if (nCommand == 4)
            {
                case_update(nProcess_new, 4, -1, strUid_recv, strBookId);
            }
            else if (nCommand == 5)
            {
                case_update(nProcess_new, 0, -1, strUid_recv, strBookId);
            }
            else if (nCommand == 6)
            {
                strDate_exp = Functions.Substring(strUid_recv, "-", "");
                strUid_recv = Functions.Substring(strUid_recv, "", "-");
                //case_update(nProcess_new, 0, -1, strUid_recv);
                case_update(nProcess_new, m_nProcess_status, -1, strUid_recv, "");//modify by gaoww 20130418 变更时，环节和环节状态不变
            }
            else if (nCommand == 9)
            {
                case_update(nProcess_new, 3, 100, strUid, "");
            }

            case_trace_log(nCommand, strUid, strUid_recv, nProcess, nProcess_new);
            nReturn = 1;

            //提交工作流
            String strBusinessId = m_strCaseId + "_" + m_nType;
            String strBookMarkName = get_wf_bookmark(m_nType, nProcess, nCommand, nProcess_new);
            //GHID|GHID_RECV|CASETYPE|TABLENAME|CASEID|CASENAME
            String strInput = myString.Format("{0}|{1}|{2}|{3}|{4}|{5}|{6}", strUid, strUid_recv, m_nType, m_TableName, m_strCaseId, m_strCaseName, strDate_exp);

            fun_WFClient.Submit_toWFS(strBusinessId, strBookMarkName, strInput);

            if ((nCommand == 3) && (nAccept_auto == 1)) //1-自动签收
            {
                //System.Threading.Thread.Sleep(1000);
                Submit_process(1, strUid_recv, strUid_recv, nProcess_new);
            }
            return nReturn;
        }

        //更新工单状态 strUid-CURRENTGHID 当前服务业务员 nStatus=100,CLOSE_GHID 闭单业务员
        private int case_update(int nProcess_new, int nProcess_status_new, int nStatus_new, String strUid, String strBM_Id)
        {
            int nReturn = -1, rc = -1;
            String strAction_case = "";                   
            if (nProcess_new < 0) return nReturn;
            String strFilter = myString.Format("(CASEID='{0}')", m_strCaseId);
            HashMap htCase = new HashMap();
            my_odbc pCase = new my_odbc(pmSys.conn_crm);
            if (strBM_Id.equals("")==false) //add by gaoww 20151111 增加工作流和工单状态变化关联
            {
                if (Functions.isExist_TableCol("CRM_CASE_WFBOOKMARK", "ACTION_CASE", pmSys.conn_crm) == true) //为兼容旧程序，先判断字段是否存在
                {
                    rc = pCase.my_odbc_find("SELECT ACTION_CASE FROM CRM_CASE_WFBOOKMARK WHERE BM_ID='"+strBM_Id+"'");
                    if (rc > 0)
                        strAction_case = pCase.my_odbc_result("ACTION_CASE");
                }
            }
            rc = pCase.my_odbc_find(m_TableName, strFilter);
            if (rc == 1)
            {
                Functions.ht_SaveEx("PROCESS", nProcess_new,htCase);
                //ACCEPT_AUTO smallint,   -- 是否自动签收，1-是 0-否。
                Functions.ht_SaveEx("PROCESS_STATUS", nProcess_status_new,htCase);
                if (nStatus_new >= 0)
                {
                    Functions.ht_SaveEx("STATUS", nStatus_new,htCase);
                    if (nStatus_new == 100)
                        Functions.ht_SaveEx("CLOSE_GHID", strUid,htCase);
                }
                Functions.ht_SaveEx("CURRENTGHID", strUid,htCase);

                //add by gaoww 20151111 增加对工单相关处理，action_case中的内容如果和上面已有字段的内容相同，以action_case中修改的内容为准
                if (strAction_case.equals("")==false)
                {
                    strAction_case = strAction_case.toUpperCase();
                    if (Regex.IsMatch(strAction_case, "^[\\d]{1,3}$") == true)  //数字 modify by fengw 20151026 [\\d]{1,3} -> ^[\\d]{1,3}$
                    {
                        Functions.ht_SaveEx("STATUS", strAction_case,htCase );                       
                    }
                    else 
                    {
                        if (Regex.IsMatch(strAction_case, "[\\W_]+=([\\d]{1,3})|[DATETIME]|[DATE][TIME]") == true)
                        {
                            if (strAction_case.indexOf("[DATETIME]") > 0) strAction_case = strAction_case.replace("[DATETIME]",  DateTime.Now().ToString("yyyy-MM-dd HH:mm:ss") );
                            if (strAction_case.indexOf("[DATE]") > 0) strAction_case = strAction_case.replace("[DATE]", DateTime.Now().ToString("yyyyMMdd"));
                            if (strAction_case.indexOf("[TIME]") > 0) strAction_case = strAction_case.replace("[TIME]",  DateTime.Now().ToString("HHmmss"));
                        }
                        if (Regex.IsMatch(strAction_case, "UPDATE ") == true)
                        {
                            strAction_case = Functions.Substring(strAction_case, "SET", "");
                        }
                        String[] strFild_update = strAction_case.split("[,]");
                        for (int index = 0; index < strFild_update.length; index++)
                        {
                            String strFld = Functions.Substring(strFild_update[index], "", "=").trim();
                            String strValue = Functions.Substring(strFild_update[index], "=", "").trim();
                            Functions.ht_SaveEx(strFld, strValue,htCase);
                        }
                    }
                }

                pCase.my_odbc_update(m_TableName, htCase, strFilter);
                //m_nProcess_status = nProcess_status_new;
                nReturn = 1;
                Refresh();
            }
            pCase.my_odbc_disconnect();
            return nReturn;
        }



        //更新工单资料
        private void Refresh()
        {
            case_set_info myCase = new case_set_info(m_nType);
            case_info myCaseInfo = myCase.GetCaseRecord(m_strCaseId);
            Fill_info(myCase, myCaseInfo);
        }

        //        if (m_nAccept_auto < 0)
        //        {
        //            my_odbc pTable = new my_odbc(pmSys.conn_crm);
        //            int rc = pTable.my_odbc_find("CRM_CASE_PROCESS", "CASETYPE='" + nType + "' AND  PROCESS_ID='" + nProcess + "'");
        //            if (rc > 0)
        //            {
        //                pTable.my_odbc_result("ACCEPT_AUTO", out m_nAccept_auto);
        //            }
        //            pTable.my_odbc_disconnect();
        //        }
        //        return m_nAccept_auto;


        /// <summary>
        /// 取环节值
        /// </summary>
        /// <param name="nCmd">1-下一个  其它-上一个 </param>
        /// <returns></returns>
        public int get_process_index(int nCmd, pmRef<Integer> nAccept_auto)
        {
            int nReturn = -1;
            nAccept_auto.oRet = 0;
            DataTable dtRet;
            my_odbc pTable = new my_odbc(pmSys.conn_crm);
            //string strFilter = String.Format("CASETYPE='{0}' AND PROCESS_ID{1}'{2}' ORDER BY WF_INDEX ASC", m_nType, (nCmd == 1) ? ">" : "<", m_nProcess);
            String strFilter = myString.Format("CASETYPE='{0}' AND WF_INDEX{1}'{2}' ORDER BY WF_INDEX ASC", m_nType, (nCmd == 1) ? ">" : "<", m_WF_Index ); //modify by gaoww 20130527 应该用工作流序号比较，查找下一个环节值
            pmList res =pTable.my_odbc_find("CRM_CASE_PROCESS", strFilter,0); dtRet = res.dtRet;
            int rc = res.nRet;
            pTable.my_odbc_disconnect();
            int nNumCol = dtRet.getCount();
            if (nNumCol > 0) //取出下一状态 ID
            {
                nReturn = Functions.dtCols_nValue(dtRet, 0, "PROCESS_ID");
                nAccept_auto.oRet = Functions.dtCols_nValue(dtRet, 0, "ACCEPT_AUTO");                
            }
            return nReturn;
        }


        /*
        private int get_process_info(int nType)
        {
            DataTable dtRet;
            my_odbc pProcess = new my_odbc(pmSys.conn_crm);
            int rc = pProcess.my_odbc_find("CRM_CASE_PROCESS", "CASETYPE='" + nType + "' ORDER BY WF_INDEX ASC", out dtRet);
            pProcess.my_odbc_disconnect();
            int nNumCol = dtRet.Rows.Count;

            if (nNumCol > 0) //取出第一环节、最后一环节 ID
            {
                m_nFirst_Id = Functions.dtCols_nValue(dtRet, 0, "PROCESS_ID");
                m_nLast_Id = Functions.dtCols_nValue(dtRet, nNumCol - 1, "PROCESS_ID");
            }

            int nId;
            //string strName;
            for (int i = 0; i < nNumCol; i++)
            {
                nId = Functions.dtCols_nValue(dtRet, i, "PROCESS_ID"); ;
                //strName = Functions.dtCols_strValue(dtRet, i, "PROCESS_NAME");
                m_dyProcess.Add(nId);
            }
            return 1;
        }

        /// <summary>
        /// 取环节值
        /// </summary>
        /// <param name="nCmd">1-下一个  其它-上一个 </param>
        /// <returns></returns>
        private int get_process_index(int nCmd)
        {
            if (m_dyProcess == null)
            {
                m_dyProcess = new List<int>();
                get_process_info(m_nType);
            }
            int nReturn = -1;
            int nCount = m_dyProcess.Count;
            int nIdx;
            for (nIdx = 0; nIdx < nCount; nIdx++)
            {
                if (m_dyProcess[nIdx] ==m_nProcess) break;
            }
            if (nCmd == 1)
            {
                if ((nIdx + 1) < nCount) nReturn = m_dyProcess[nIdx + 1];
            }
            else
            {
                if ((nIdx - 1) >= 0) nReturn = m_dyProcess[nIdx - 1];
            }
            return nReturn;
        }
        */

        /// <summary>
        /// 取出该工单当前环节已被拒签的次数
        /// </summary>
        /// <param name="strCaseid"></param>
        /// <returns></returns>
        public int nRefuse_Count()
        {
                int nCount = 0;
                my_odbc pTable = new my_odbc(pmSys.conn_crm);
                int rc = pTable.my_odbc_find("SELECT COUNT(*) AS COUNT FROM CRM_CASE_TRACE WHERE CASEID='" + m_strCaseId + "' AND PROCESS='" + m_nProcess + "' AND PS_STATUS_RECV=2");
                if (rc > 0)
                {
                    nCount = pTable.my_odbc_result("COUNT",0);
                }
                pTable.my_odbc_disconnect();
                return nCount;
        }

        public int nBack_Count()
        {
                int nCount = 0;
                my_odbc pTable = new my_odbc(pmSys.conn_crm);
                int rc = pTable.my_odbc_find("SELECT COUNT(*) AS COUNT FROM CRM_CASE_TRACE WHERE CASEID='" + m_strCaseId + "' AND PROCESS='" + m_nProcess + "' AND PS_STATUS_RECV=4");
                if (rc > 0)
                {
                    nCount = pTable.my_odbc_result("COUNT",0);
                }
                pTable.my_odbc_disconnect();
                return nCount;
        }

        //上一环节发送人
        //public string strUid_prev
        //{
        //    get
        //    {
        //        string strReturn = "";
        //        my_odbc pTable = new my_odbc(pmSys.conn_crm);
        //        int rc = pTable.my_odbc_find("SELECT GHID_PREV FROM CRM_CASE_TRACE WHERE CASEID='" + m_strCaseId + "' AND PROCESS='" + m_nProcess + "' ORDER BY AUTOID DESC");
        //        if (rc > 0)
        //        {
        //            pTable.my_odbc_result("GHID_PREV", out strReturn);
        //        }
        //        pTable.my_odbc_disconnect();
        //        return strReturn;
        //    }
        //}

        //根据踪迹记录，读取上一环节记录
        public int GetPSInfo_prev(String strUid, pmRef<String> strUid_prev)
        {
            int nPS_prev = -1;

            strUid_prev.oRet = strUid;
            String strSql = myString.Format("SELECT PS_PREV,GHID_PREV FROM CRM_CASE_TRACE WHERE CASEID='{0}' AND CASETYPE={1} AND PROCESS={2} AND PS_STATUS_RECV=0  ORDER BY AUTOID DESC", m_strCaseId, m_nType, m_nProcess);
            my_odbc pTable = new my_odbc(pmSys.conn_crm);
            int rc = pTable.my_odbc_find(strSql);
            if (rc == 1)
            {
                nPS_prev = pTable.my_odbc_result("PS_PREV",0);
                strUid_prev.oRet = pTable.my_odbc_result("GHID_PREV");
            }
            pTable.my_odbc_disconnect();
            return nPS_prev;
        }

        // 读取工作流 BookMarkName
        private String get_wf_bookmark(int nType, int nProcess, int nCommand, int nProcess_new)
        {
            List<String> alCmd = Arrays.asList(new String[] { "accept", "refuse", "submit", "back", "skip", "update" });

            String strBookMarkName = "";
            /*
            string strFilter = String.Format("(CASETYPE={0} AND PROCESS={1} AND SUBMIT_CMD={2})", nType, nProcess, nCommand);
            my_odbc pCase = new my_odbc(pmSys.conn_crm);
            int rc = pCase.my_odbc_find("CRM_CASE_WF_BOOKMARK", strFilter);
            if (rc > 0)
            {
                pCase.my_odbc_result("BMNAME", out strBookMarkName);
            }
            else strBookMarkName = "submit_1";
            pCase.my_odbc_disconnect();
            */
            //strBookMarkName = String.Format("{0}_{1}_{2}", alCmd[nCommand - 1], nType, nProcess);
            //strBookMarkName = String.Format("{0}", alCmd[nCommand - 1]);
            if (nCommand < 6) strBookMarkName = String.format("%02d%02d%02d-%02d", nType, nProcess, nCommand, nProcess_new);
            else if (nCommand == 6) strBookMarkName = "Update";
            else if (nCommand == 9) strBookMarkName = "Complete";
            return strBookMarkName;
        }

        //记录工单轨迹
        private int case_trace_log(int nCommand, String strUid, String strUid_recv, int nProcess_prev, int nProcess_new)
        {
            int nReturn = -1;
            String strInstance = "";

            String strNow = DateTime.Now().ToString("yyyyMMdd HHmmss");
            if (nProcess_prev == nProcess_new)  //环节不变
            {
                if ((nCommand == 1))  //签收
                {
                    String strFilter = myString.Format("(CASETYPE='{0}' AND CASEID='{1}' AND PROCESS='{2}')", m_nType, m_strCaseId, nProcess_prev);
                    my_odbc pCase = new my_odbc(pmSys.conn_crm);
                    int rc = pCase.my_odbc_find("CRM_CASE_TRACE", strFilter);
                    if (rc == 1)
                    {
                        pCase.my_odbc_set_new();
                        pCase.my_odbc_set("TIMEOUT", 0);
                        pCase.my_odbc_set("SDATE_ACCEPT", strNow);
                        pCase.my_odbc_set("SUBMIT_CMD", nCommand);
                        pCase.my_odbc_set("PS_STATUS_RECV", m_nProcess_status);

                        pCase.my_odbc_update("CRM_CASE_TRACE", strFilter);
                    }
                    else
                    {
                        pCase.my_odbc_set_new();
                        pCase.my_odbc_set("CASEID", m_strCaseId);
                        pCase.my_odbc_set("CASENAME", m_strCaseName);
                        pCase.my_odbc_set("CASETYPE", m_nType);
                        pCase.my_odbc_set("STATUS", m_nStatus);
                        pCase.my_odbc_set("PROCESS", m_nProcess);
                        pCase.my_odbc_set("INSTANCEID", strInstance);
                        pCase.my_odbc_set("TIMEOUT", 0);

                        pCase.my_odbc_set("SDATE_RECV", strNow);
                        //pCase.my_odbc_set("GHID_SEND", strUid);
                        pCase.my_odbc_set("PS_STATUS_RECV", m_nProcess_status);
                        pCase.my_odbc_set("GHID", strUid);
                        //--环节不变，环节状态变
                        pCase.my_odbc_set("SDATE_ACCEPT", strNow);
                        pCase.my_odbc_set("SUBMIT_CMD", nCommand);
                        pCase.my_odbc_addnew("CRM_CASE_TRACE");
                    }
                    pCase.my_odbc_disconnect();
                }
            }
            else  //不同环节,记录接收、发送两个状态记录
            {
                String strFilter = myString.Format("SELECT * FROM WF_INSTANCE_BUSINESS WHERE (BUSINESSID='{0}')", m_strCaseId);
                my_odbc pCase = new my_odbc(pmSys.conn_callthink);
                int rc = pCase.my_odbc_find(strFilter);
                if (rc == 1)
                {
                    strInstance = pCase.my_odbc_result("INSTANCEID");
                }
                pCase.my_odbc_disconnect();

                //DATE_EXP    varchar(15),         --解决时限
                //MEMO       varchar(120),         --批注信息
                //TIMEOUT    smallint,              --是否超时（0-否，1-是）
                //TIMEOUTLEN   int                --超时时长
                if ((nCommand == 2) || (nCommand == 3) || (nCommand == 4) || (nCommand == 5))
                {
                    pCase = new my_odbc(pmSys.conn_crm);
                    //--发送时，检查如已接收过，则修改记录；未接收过，新增记录
                    strFilter = myString.Format("(CASETYPE='{0}' AND CASEID='{1}' AND PROCESS='{2}' AND SDATE_SEND=' ')", m_nType, m_strCaseId, nProcess_prev);
                    rc = pCase.my_odbc_find("CRM_CASE_TRACE", strFilter);
                    if (rc == 1)
                    {
                        pCase.my_odbc_set_new();
                        pCase.my_odbc_set("SDATE_SEND", strNow);
                        pCase.my_odbc_set("SUBMIT_CMD", nCommand);
                        pCase.my_odbc_set("GHID", strUid);
                        pCase.my_odbc_update("CRM_CASE_TRACE", strFilter);
                    }
                    else
                    {
                        pCase.my_odbc_set_new();
                        pCase.my_odbc_set("CASEID", m_strCaseId);
                        pCase.my_odbc_set("CASENAME", m_strCaseName);
                        pCase.my_odbc_set("CASETYPE", m_nType);
                        pCase.my_odbc_set("STATUS", m_nStatus);
                        pCase.my_odbc_set("PROCESS", nProcess_prev);
                        pCase.my_odbc_set("INSTANCEID", strInstance);
                        pCase.my_odbc_set("TIMEOUT", 0);

                        pCase.my_odbc_set("SDATE_SEND", strNow);
                        pCase.my_odbc_set("SUBMIT_CMD", nCommand);
                        pCase.my_odbc_set("GHID", strUid);
                        pCase.my_odbc_addnew("CRM_CASE_TRACE");
                    }

                    //--接收时记录，创建一条新记录
                    pCase.my_odbc_set_new();
                    pCase.my_odbc_set("CASEID", m_strCaseId);
                    pCase.my_odbc_set("CASENAME", m_strCaseName);
                    pCase.my_odbc_set("CASETYPE", m_nType);
                    pCase.my_odbc_set("STATUS", m_nStatus);
                    pCase.my_odbc_set("PROCESS", nProcess_new);
                    pCase.my_odbc_set("INSTANCEID", strInstance);
                    pCase.my_odbc_set("TIMEOUT", 0);

                    pCase.my_odbc_set("SDATE_RECV", strNow);
                    pCase.my_odbc_set("PS_STATUS_RECV", m_nProcess_status);
                    pCase.my_odbc_set("PS_PREV", nProcess_prev);
                    pCase.my_odbc_set("GHID_PREV", strUid);
                    pCase.my_odbc_set("SDATE_SEND", " ");          //未写发 标志

                    pCase.my_odbc_set("GHID", strUid_recv);
                    pCase.my_odbc_addnew("CRM_CASE_TRACE");
                    pCase.my_odbc_disconnect();
                }
            }
            return nReturn;
        }

        //#region 工单-简易工作流相关函数

        //nCommand：接收命令：发出命令：3-提交、4-退回（已执行过的节点）、5-跳转(任意节点)、
        //nStatus_new = 0，不起作用 
        public int Submit_status(int nCommand, String strUid, String strUid_recv, int nStatus_new)
        {
            int nReturn = -1;
            int nStatus = m_nStatus;
            String strDate_exp = "";
            //检查当前状态
            if (nCommand == 3)
            {
                nStatus_new = get_status_index(1);
                if (nStatus_new > 0)
                {
                    case_status_update(nStatus_new, strUid_recv);
                }
                else //最后一状态：（工单状态为闭单）
                {
                    case_status_update(100, strUid);
                }
            }
            else if ((nCommand == 4) || (nCommand == 5))
            {
                case_status_update(nStatus_new, strUid_recv);
            }
            else if (nCommand == 6)
            {
                strDate_exp = Functions.Substring(strUid_recv, "-", "");
                strUid_recv = Functions.Substring(strUid_recv, "", "-");
                case_status_update(-1, strUid_recv);
            }
            else if (nCommand == 9)
            {
                case_status_update(100, strUid);
            }

            //case_trace_log(nCommand, strUid, strUid_recv, nProcess_prev, nStatus_new);
            nReturn = 1;

            //提交工作流
            String strBusinessId = m_strCaseId + "_" + m_nType;
            String strBookMarkName = get_wf_bookmark(m_nType, nStatus, nCommand, nStatus_new);
            //GHID|GHID_RECV|CASETYPE|TABLENAME|CASEID|CASENAME
            String strInput = myString.Format("{0}|{1}|{2}|{3}|{4}|{5}|{6}", strUid, strUid_recv, m_nType, m_TableName, m_strCaseId, m_strCaseName, strDate_exp);

            fun_WFClient.Submit_toWFS(strBusinessId, strBookMarkName, strInput);
            return nReturn;
        }

        private int case_status_update(int nStatus_new, String strUid)
        {
            int nReturn = -1;
            if (nStatus_new < 0) return nReturn;
            String strFilter = myString.Format("(CASEID='{0}')", m_strCaseId);
            my_odbc pCase = new my_odbc(pmSys.conn_crm);
            int rc = pCase.my_odbc_find(m_TableName, strFilter);
            if (rc == 1)
            {
                pCase.my_odbc_set_new();
                if (nStatus_new >= 0)
                {
                    pCase.my_odbc_set("STATUS", nStatus_new);
                    if (nStatus_new == 100)
                    {
                        pCase.my_odbc_set("CLOSE_GHID", strUid);
                        pCase.my_odbc_set("CLOSE_DATE", DateTime.Now().ToString("yyyyMMdd HHmmss"));
                    }
                }
                if (strUid.length() > 0)
                {
                    pCase.my_odbc_set("CURRENTGHID", strUid);
                }
                pCase.my_odbc_update(m_TableName, strFilter);
                nReturn = 1;
            }
            pCase.my_odbc_disconnect();
            return nReturn;
        }

        private int get_status_index(int nCmd)
        {
            int nReturn = -1;
            DataTable dtRet;
            my_odbc pTable = new my_odbc(pmSys.conn_crm);
            String strFilter = myString.Format("CASETYPE='{0}' AND STATUS_ID{1}'{2}' ORDER BY STATUS_ID ASC", m_nType, (nCmd == 1) ? ">" : "<", m_nStatus);

            pmList res =pTable.my_odbc_find("CRM_CASE_STATUS", strFilter,0); dtRet = res.dtRet;
            int rc = res.nRet;
            pTable.my_odbc_disconnect();
            int nNumCol = dtRet.getCount();
            if (nNumCol > 0) //取出下一状态 ID
            {
                nReturn = Functions.dtCols_nValue(dtRet, 0, "STATUS_ID");
            }
            return nReturn;
        }
     // #endregion

    }
   // #endregion
