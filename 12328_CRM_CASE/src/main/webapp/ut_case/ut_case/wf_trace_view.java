///########################################################################################
/// Copyright (C) 2000, ToneThink.Soft  All Rights Reserved. 
///#########################################################################################
/// 文件创建时间：2013-03-20
///   文件创建人：peng
/// 文件功能描述：工单工作流设置
///     调用格式：
///     维护记录：
/// 2013.03.20 peng 优化代码          
///#########################################################################################
package com.CallThink.ut_case;

import java.util.HashMap;
import java.util.LinkedHashMap;
import org.springframework.ui.Model;
import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.pmSys;
import com.CallThink.base.pmCode.UltraCRM_Page;
import com.CallThink.ut_case.pmModel_case.case_info;
import com.CallThink.ut_case.pmModel_case.case_set_info;
import com.ToneThink.DataTable.DataTable;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.pmList;
import com.ToneThink.ctsTools.myUtility.pmMap;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.myString;
import com.ToneThink.ctsTools.WebUI.HtmlTable;
import com.ToneThink.ctsTools.WebUI.HtmlTableCell;
import com.ToneThink.ctsTools.WebUI.HtmlTableRow;
import com.ToneThink.ctsTools.WebUI.my_Field;
import com.ToneThink.ctsTools.WebUI.my_ToolStrip;
import com.ToneThink.ctsTools.WebUI.my_ToolStrip;
import com.ToneThink.ctsTools.WebUI.my_dataGrid;
import com.ToneThink.ctsTools.WebUI.my_SearchField;

    public class wf_trace_view extends UltraCRM_Page
    {
        private String pTableName = "CRM_CASE";
        private String pCaseId = "";
        private int pType = 0;
        private String m_lblRem = "";
        
        private HashMap m_htPriv = new HashMap();
        LinkedHashMap<Integer, String> m_dyProcess = new LinkedHashMap<Integer, String>();
    	   
        case_set_info myCase;
        case_info myCaseInfo = new case_info();

        my_Field myFld = new my_Field(2);
        my_dataGrid mydg = new my_dataGrid(50);
        public void Page_Load(Object sender, Model model)
        {
            if (IsPostBack == false)//正被首次加载和访问
            {
                HashMap htQuery;
                pmMap res = fun_main.QuerySplit(Request); htQuery = res.htRet;
                int rc = res.nRet;
                if (rc > 0)
                {
                    pTableName = Functions.ht_Get_strValue("casetable", htQuery);
                    pCaseId = Functions.ht_Get_strValue("caseid", htQuery);
                    pType = Functions.atoi(Functions.ht_Get_strValue("ntype", htQuery));
                }
                Save_vs("pTableName", pTableName);
                Save_vs("pCaseId", pCaseId);
                Save_vs("pType", pType);
            }
            else
            {
                pTableName = Load_vs("pTableName");
                pCaseId = Load_vs("pCaseId");
                pType = Functions.atoi(Load_vs("pType"));
            }
            myCase = new case_set_info(pType);
            myCaseInfo = myCase.GetCaseRecord(pCaseId);

            Fillin_field();
            String strProcess = Fillin_plProcess();
            Fillin_grid();
            
            myFld.render(model);
            mydg.render(model);
            model.addAttribute("plProcess",strProcess);
            model.addAttribute("lblRem",m_lblRem);
        }

        //动态填充工单信息
        private void Fillin_field()
        {
            myFld.SetLabelAlign("Right");
            myFld.SetMaxLabelLenth(100);
            myFld.funName_OnClientClick("myFld_FieldLinkClicked");

            myFld.fill_fld("工单编号", "CASEID", 25, 0, false);
            myFld.fill_fld("工单状态", "STATUS", 25, 0, false);
            myFld.fill_fld("工单名称", "CASENAME", 106, 0, false);
            myFld.fill_fld("环节", "PROCESS", 25, 0, false);
            myFld.fill_fld("环节状态", "PROCESS_STATUS", 25, 0, false);
            myFld.fill_fld("创建人", "GHID", 25, 0, false);
            myFld.fill_fld("所属人", "CURRENTGHID", 25, 0, false);
            myFld.fill_Panel("plInfo_case");

            myFld.set_item_value("CASEID", myCaseInfo.strCaseId);
            myFld.set_item_value("CASENAME", myCaseInfo.Get("CASENAME"));
            myFld.set_item_value("STATUS", String.valueOf(myCaseInfo.nStatus));
            myFld.set_item_value("PROCESS", String.valueOf(myCaseInfo.nProcess));
            myFld.set_item_value("PROCESS_STATUS", String.valueOf(myCaseInfo.nProcess_status));
            myFld.set_item_value("GHID", myCaseInfo.Get("GHID"));
            String strTemp;
            my_odbc pTable = new my_odbc(pmSys.conn_crm);
            int rc = pTable.my_odbc_find("CRM_CASE_STATUS", "CASETYPE=" + pType + " AND STATUS_ID='" + myCaseInfo.nStatus + "'");
            if (rc == 1)
            {
                strTemp = pTable.my_odbc_result("STATUS_NAME");
                myFld.set_item_value("STATUS", strTemp);
            }
            rc = pTable.my_odbc_find("CRM_CASE_PROCESS", "CASETYPE=" + pType + " AND PROCESS_ID='" + myCaseInfo.nProcess + "'");
            if (rc == 1)
            {
                strTemp = pTable.my_odbc_result("PROCESS_NAME");
                myFld.set_item_value("PROCESS", strTemp);
            }
            pTable.my_odbc_disconnect();

            myFld.set_item_value("GHID", GetUid_name(myCaseInfo.Get("GHID")));
            myFld.set_item_value("CURRENTGHID", GetUid_name(myCaseInfo.Get("CURRENTGHID")));
            rc = myCaseInfo.nProcess_status;
            if (rc == 0) strTemp = "0-未签收";
            else if (rc == 1) strTemp = "1-已签收";
            else if (rc == 2) strTemp = "2-拒签收";
            else if (rc == 3) strTemp = "3-已提交";
            else if (rc == 4) strTemp = "4-被退回";
            else strTemp = String.valueOf(myCaseInfo.nProcess_status);
            myFld.set_item_value("PROCESS_STATUS", strTemp);
        }

        //动态加载用户控件-状态
        private String Fillin_plProcess()
        {
            DataTable dtRet;
            my_odbc pProcess = new my_odbc(pmSys.conn_crm);
            pmList res =pProcess.my_odbc_find("CRM_CASE_PROCESS", "CASETYPE='" + pType + "' ORDER BY WF_INDEX ASC",0); dtRet = res.dtRet;
            int rc = res.nRet;
            pProcess.my_odbc_disconnect();

            String html_out, strName;
            int nId, nId_now = 0;
            int numCells = dtRet.getCount();
            HtmlTable hTable = new HtmlTable();
            HtmlTableRow tRow = new HtmlTableRow();
            //第一行加载环节名称
            tRow.VAlign = "center";
            //modify by gaoww 20130527 应该用序号比较当前环节和已完成环节的button样式
            DataTable myRow = dtRet.select("PROCESS_ID='" + myCaseInfo.nProcess + "'");
            if (myRow.getCount() > 0)
            	  nId_now = Functions.dtCols_nValue(myRow, 0, "WF_INDEX");

            for (int i = 0; i < numCells; i++)
            {
                nId = Functions.dtCols_nValue(dtRet, i, "WF_INDEX");  //modify by gaoww 20130527 应该使用工作流序号，而不是环节值
                strName = Functions.dtCols_strValue(dtRet, i, "PROCESS_NAME");
                m_dyProcess.put(nId, strName);

                HtmlTableCell tCol = new HtmlTableCell();
                String strClass = "Process_btnStyle_dis";
                //modify by gaoww 20130527 应该用序号比较
                if (nId_now < nId)
                    strClass = "Process_btnStyle_dis";
                else if (nId_now == nId)
                    strClass = "Process_btnStyle_select";
                else if (nId_now > nId)
                    strClass = "Process_btnStyle_done";

                html_out = myString.Format("<input type='button' id='btnProcess_{0}' name='btnProcess_{0}' value=' {1} ' class='{2}'  />", nId, strName, strClass);
                tCol.setControl(html_out);
                tRow.Cells().add(tCol);
                tCol = new HtmlTableCell();
                //tCol.Width = "50px";
                if ((i + 1) < numCells) //不是最后环节
                {
                    html_out = myString.Format("<img src='./images/{0}'  width='60' height='12' />", (myCaseInfo.nProcess > nId) ? "arrow_green.png" : "arrow_gray.png");
                    tCol.setControl(html_out);
                }
                tRow.Cells().add(tCol);
            }
            hTable.add(tRow);
            //第二行加载环节信息
            tRow = new HtmlTableRow();
            tRow.Align = "left";
            for (int i = 0; i < numCells; i++)
            {
                HtmlTableCell tCol = new HtmlTableCell();
                tCol.ColSpan = 2;

                int nProcess_id = Functions.dtCols_nValue(dtRet, i, "PROCESS_ID"); ;
                nId = Functions.dtCols_nValue(dtRet, i, "WF_INDEX");  //modify by gaoww 20130527 应该使用工作流序号，而不是环节值
              
                //modify by gaoww 20130527 应该用序号比较
                if (nId_now > nId)
                {
                   // Hashtable htRet = GetTrace_log(nId);
                    HashMap htRet = GetTrace_log(nProcess_id);
                    strName = GetUid_name(Functions.ht_Get_strValue("GHID", htRet));
                    //&#12288;可以看作一个空白的汉字
                    if (i == 0)
                        html_out = myString.Format("<br>&#12288;提交:{0}<br>提交人:{1}", Functions.ht_Get_strValue("SDATE_SEND", htRet), strName);
                    else
                        html_out = myString.Format("&#12288;接收:{0}<br>&#12288;提交:{1}<br>提交人:{2}{3}", Functions.ht_Get_strValue("SDATE_RECV", htRet), Functions.ht_Get_strValue("SDATE_SEND", htRet), strName, (Functions.ht_Get_strValue("GHID", htRet).equals("1")) ? "<br>超时时间" : "");
                    tCol.setControl(html_out);
                }
                tRow.Cells().add(tCol);
            }
            hTable.add(tRow);
            hTable.Border = 0;
            //plProcess.Controls.put(hTable);
            return hTable.toString();
        }

        //读取踪迹记录
        private HashMap GetTrace_log(int nProcessId)
        {
            HashMap htRet = new HashMap();
            String strSql = myString.Format("SELECT SDATE_RECV,SDATE_SEND,GHID,TIMEOUT,TIMEOUTLEN FROM CRM_CASE_TRACE WHERE CASEID='{0}' AND CASETYPE={1} AND PROCESS={2}", pCaseId, pType, nProcessId);
            my_odbc pTable = new my_odbc(pmSys.conn_crm);
            pmMap res =pTable.my_odbc_find(strSql,true); htRet = res.htRet;
            int rc = res.nRet;
            pTable.my_odbc_disconnect();
            return htRet;
        }

        //根据工号，读姓名 
        private String GetUid_name(String strUid)
        {
            String strReturn = strUid;
            my_odbc pTable = new my_odbc(pmSys.conn_callthink);
            int rc = pTable.my_odbc_find("CTS_OPIDK", "GHID='" + strUid + "'");            
            if (rc == 1)
            {
                String strTemp;
                strTemp = pTable.my_odbc_result("REAL_NAME");
                strReturn = strUid + "-" + strTemp;
            } 
            pTable.my_odbc_disconnect();
            return strReturn;
        }

        private void Fillin_grid()
        {
            int i = 0;
            mydg.SetTable("CRM_CASE_TRACE");
            mydg.SetConnStr(pmSys.conn_crm);
            mydg.SetPageSize(25);

            if (pCaseId.equals(""))
                mydg.fill_fld(i++, "选择", "SELECT", 5, 9);
            mydg.fill_fld(i++, "AUTOID", "AUTOID", 0);
            mydg.fill_fld(i++, "工单编号", "CASEID", 16);
            mydg.fill_fld(i++, "工单名称", "CASENAME", -1);
            mydg.fill_fld(i++, "工单类型", "CASETYPE", 0, 1);
            mydg.set_cols_cbo_list("CASETYPE", "SELECT * FROM CRM_CASE_TABLE", "CASETYPE,CASE_NAME", pmSys.conn_crm);
            mydg.fill_fld(i++, "工作流实例ID", "INSTANCEID", 0);
            mydg.fill_fld(i++, "工单状态", "STATUS", 0);

            mydg.fill_fld(i++, "转入日期", "SDATE_RECV", 16);
            mydg.fill_fld(i++, "发送人", "GHID_PREV", 8);

            mydg.fill_fld(i++, "前一环节", "PS_PREV", 10, 1);
            mydg.set_cols_cbo_list("PS_PREV", "SELECT * FROM CRM_CASE_PROCESS WHERE CASETYPE='" + pType + "'", "PROCESS_ID,PROCESS_NAME", pmSys.conn_crm);  //modify by gaoww 20130403
            mydg.fill_fld(i++, "当前环节", "PROCESS", 10, 1);
            mydg.set_cols_cbo_list("PROCESS", "SELECT * FROM CRM_CASE_PROCESS WHERE CASETYPE='" + pType + "'", "PROCESS_ID,PROCESS_NAME", pmSys.conn_crm);  //modify by gaoww 20130403
            mydg.fill_fld(i++, "环节状态", "PS_STATUS_RECV", 12, 1);

            mydg.set_cols_cbo_list("PS_STATUS_RECV", "0-未签收,1-已签收,2-拒签收,3-已提交,4-被退回");
            //mydg.fill_fld(i++, "转出日期", "SDATE_SEND", 16);
            mydg.fill_fld(i++, "业务员", "GHID", 8);
            mydg.fill_fld(i++, "解决时限", "DATE_EXP", 0);
            mydg.fill_fld(i++, "是否超时", "TIMEOUT", 8, 1);
            mydg.fill_fld(i++, "超时时长", "TIMEOUTLEN", 8);
            mydg.set_cols_cbo_list("TIMEOUT", "否,是");

            String strFilter = m_strFilter_base() + m_strOrder_by();
            mydg.fill_header("dgvList", "AUTOID", strFilter);
        }

        //基本的显示条件
        private String m_strFilter_base()
        {
            String strFilter = "";
            if (pCaseId.equals(""))
            {
                strFilter = "1>1";
            }
            else
            {
                if (strFilter.length() > 0) strFilter = " AND ";
                strFilter = "(CASEID='" + pCaseId + "')";
            }
            return strFilter;
        }

        //排序规则
        private String m_strOrder_by()
        {
                String strOrderby = "";// " ORDER BY AUTOID DESC";
                return strOrderby;
        }

        protected void rem(String strMsg)
        {
            //Functions.MsgBox(strMsg);
            m_lblRem = strMsg;
        }
    }

