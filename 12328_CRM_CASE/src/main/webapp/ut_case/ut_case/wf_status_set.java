///########################################################################################
/// Copyright (C) 2000, ToneThink.Soft  All Rights Reserved. 
///#########################################################################################
/// 文件创建时间：2013-03-20
///   文件创建人：peng
/// 文件功能描述：工单状态设置
///     调用格式：
///     维护记录：
/// 2013.03.20 peng 用于无工作流工单修改状态          
///#########################################################################################
package com.CallThink.ut_case;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import org.springframework.ui.Model;
import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.pmSys;
import com.CallThink.base.pmCode.UltraCRM_Page;
import com.CallThink.ut_case.pmModel_case.WFCase_info;
import com.CallThink.ut_case.pmModel_case.case_info;
import com.CallThink.ut_case.pmModel_case.case_set_info;
import com.CallThink.ut_form.pmModel_form.fun_Form;
import com.ToneThink.DataTable.DataTable;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.pmList;
import com.ToneThink.ctsTools.myUtility.pmMap;
import com.ToneThink.ctsTools.Regex.Regex;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.myString;
import com.ToneThink.ctsTools.WebUI.Fld_attr;
import com.ToneThink.ctsTools.WebUI.HtmlTable;
import com.ToneThink.ctsTools.WebUI.HtmlTableCell;
import com.ToneThink.ctsTools.WebUI.HtmlTableRow;
import com.ToneThink.ctsTools.WebUI.myPageUtil;
import com.ToneThink.ctsTools.WebUI.my_Field;

    public class wf_status_set extends UltraCRM_Page
    {
        private String pTableName = "CRM_CASE";
        private String pCaseId = "";
        private int pType = 0;
        private String m_lblRem = "";
        
        private HashMap m_htPriv = new HashMap();
        //private Dictionary<int, string> m_dyProcess = new Dictionary<int, string>();
        LinkedHashMap<Integer, String> m_dyProcess = new LinkedHashMap<Integer, String>();
   	    private int m_nFirst_Id = 0, m_nLast_Id = 0;   //第一状态、最后一状态 ID

        case_set_info myCase;
        case_info myCaseInfo;
        WFCase_info myWFCase;

        my_Field myFld = new my_Field(2);
        my_Field myFld_handle = new my_Field(1);
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
            myWFCase = new WFCase_info(myCase, myCaseInfo);

            Fillin_field();
            String strProcess = Fillin_plProcess();
            Fillin_Button();

           myFld.render(model);
           model.addAttribute("plProcess",strProcess);
           myFld_handle.render_templet(model);
           model.addAttribute("lblRem",m_lblRem);
        }

        //动态填充工单信息
        private void Fillin_field()
        {
            //myFld.SetCaption = "工单资料详细信息";
            myFld.SetLabelAlign("Right");
            myFld.SetMaxLabelLenth(100);
            myFld.funName_OnClientClick("myFld_FieldLinkClicked");

            myFld.fill_fld("工单编号", "CASEID", 25, 0, false);
            myFld.fill_fld("工单状态", "STATUS", 25, 0, false);
            myFld.fill_fld("工单名称", "CASENAME", 106, 0, false);
            myFld.fill_fld("创建人", "GHID", 25, 0, false);
            myFld.fill_fld("所属人", "CURRENTGHID", 25, 0, false);
            
            myFld.fill_Panel("plInfo_case");
            Fillin_data();
        }

        private void Fillin_data()
        {
            myFld.set_item_value("CASEID", myCaseInfo.strCaseId);
            myFld.set_item_value("CASENAME", myCaseInfo.Get("CASENAME"));
            myFld.set_item_value("STATUS",String.valueOf(myCaseInfo.nStatus));
            myFld.set_item_value("GHID", myCaseInfo.Get("GHID"));
            String strTemp;
            my_odbc pTable = new my_odbc(pmSys.conn_crm);
            int rc = pTable.my_odbc_find("CRM_CASE_STATUS", "CASETYPE=" + pType + " AND STATUS_ID='" + myCaseInfo.nStatus + "'");
            if (rc == 1)
            {
                strTemp = pTable.my_odbc_result("STATUS_NAME");
                myFld.set_item_value("STATUS", strTemp);
            }
            pTable.my_odbc_disconnect();
            myFld.set_item_value("GHID", GetUid_name(myCaseInfo.Get("GHID")));
            myFld.set_item_value("CURRENTGHID", GetUid_name(myCaseInfo.Get("CURRENTGHID")));
        }

        //动态加载用户控件-状态
        private String Fillin_plProcess()
        {
            DataTable dtRet;
            my_odbc pProcess = new my_odbc(pmSys.conn_crm);
            pmList res =pProcess.my_odbc_find("CRM_CASE_STATUS", "CASETYPE=" + pType,0); dtRet = res.dtRet;
            int rc = res.nRet;
            pProcess.my_odbc_disconnect();
            int nNumCol = dtRet.getCount();

            if (nNumCol > 0) //取出第一状态、最后一状态 ID
            {
                m_nFirst_Id = Functions.dtCols_nValue(dtRet, 0, "STATUS_ID");
                m_nLast_Id = Functions.dtCols_nValue(dtRet, nNumCol - 1, "STATUS_ID");
            }

            String html_out, strName;
            int nId;
            HtmlTable hTable = new HtmlTable();
            HtmlTableRow tRow = new HtmlTableRow();
            //第一行加载环节名称
            tRow.VAlign = "center";
            for (int i = 0; i < nNumCol; i++)
            {
                nId = Functions.dtCols_nValue(dtRet, i, "STATUS_ID");
                strName = Functions.dtCols_strValue(dtRet, i, "STATUS_NAME");
                if (Regex.IsMatch(strName, "[\\d]+-") == true)  //STATUS_NAME 存成 1-已完成 ->  已完成
                {
                    strName = Functions.Substring(strName, "-", "");
                }
                m_dyProcess.put(nId, strName);

                HtmlTableCell tCol = new HtmlTableCell();
                String strClass = "Process_btnStyle_dis";
                if (myCaseInfo.nStatus < nId)
                    strClass = "Process_btnStyle_dis";
                else if (myCaseInfo.nStatus == nId)
                    strClass = "Process_btnStyle_select";
                else if (myCaseInfo.nStatus > nId)
                    strClass = "Process_btnStyle_done";

                html_out = myString.Format("<input type='button' id='btnProcess_{0}' name='btnProcess_{0}' value=' {1} ' class='{2}'  />", nId, strName, strClass);
                tCol.setControl(html_out);
                tRow.Cells().add(tCol);
                tCol = new HtmlTableCell();
                //tCol.Width = "50px";
                if (nId != m_nLast_Id) //不是最后环节
                {
                    html_out = myString.Format("<img src='./images/{0}'  width='60' height='12' />", (myCaseInfo.nStatus > nId) ? "arrow_green.png" : "arrow_gray.png");
                    tCol.setControl(html_out);
                }
                tRow.Cells().add(tCol);
            }
            hTable.add(tRow);
            
            //第二行加载环节信息
            hTable.Border = 0;
            //plProcess.Controls.put(hTable);
            return hTable.toString();
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

        //初始化控件
        private void Fillin_Button()
        {
            int nSubmit = 0, nBack = 0, nSkip = 0, nComplete = 0;   //bit0=1 不可见  bit1=1 只读
            List<String> alRet;   
            myCase.get_edit_priv(myCaseInfo, m_htPriv);
            //检查是否有不可见的命令
            if (m_htPriv.containsKey("CMD_INV") == true)
            {
                alRet = Arrays.asList(Functions.ht_Get_strValue("CMD_INV", m_htPriv));               
                for (String strCmd : alRet)
                {
                    if (strCmd.equals("case_submit")) nSubmit = 1;
                    else if (strCmd.equals("case_goto")) nSkip = 1;
                    else if (strCmd.equals("case_back")) nBack = 1;
                    else if (strCmd.equals("case_complete")) nComplete = 1;//add by gaoww 20140228 增加完成权限
                }
            }
            //检查是否有不可用的命令
            if (m_htPriv.containsKey("CMD_RDONLY") == true)
            {
                alRet = Arrays.asList(Functions.ht_Get_strValue("CMD_RDONLY", m_htPriv));               
                for (String strCmd : alRet)
                {
                    if (strCmd.equals("case_submit")) nSubmit |= 2;
                    else if (strCmd.equals("case_goto")) nSkip |= 2;
                    else if (strCmd.equals("case_back")) nBack |= 2;
                    else if (strCmd.equals("case_complete")) nComplete |= 2;//add by gaoww 20140228 增加完成权限
                }
            }        
    
            //bit0=1 不可见
            if ((nSubmit & 1) != 0) myPageUtil.setAttr("plSubmit", "visible", "false"); //plSubmit.Visible = false;
            if ((nBack & 1) != 0) myPageUtil.setAttr("plBackward", "visible", "false"); //plBackward.Visible = false;
            if ((nSkip & 1) != 0) myPageUtil.setAttr("plSkip", "visible", "false"); //plSkip.Visible = false;
            if ((nComplete & 1) != 0) myPageUtil.setAttr("btnComplete", "visible", "false"); //btnComplete.Visible = false;//add by gaoww 20140228 增加完成权限


            //peng 因为Button disable 后，:hover 仍然起作用，故默认状态设为 wfcmd_btnStyle_dis，可用在改为：wfcmd_btnStyle
            if ((nSubmit & 2) == 2) myPageUtil.setAttr("plSubmit", "enable", "false"); //plSubmit.Enabled = false;
            else myPageUtil.setAttr("btnSubmit", "class", "wfcmd_btnStyle");//btnSubmit.CssClass = "wfcmd_btnStyle";
            if ((nBack & 2) == 2) myPageUtil.setAttr("plBackward", "enable", "false"); //plBackward.Enabled = false;
            else myPageUtil.setAttr("btnBackward", "class", "wfcmd_btnStyle");//btnBackward.CssClass = "wfcmd_btnStyle";
            if ((nSkip & 2) == 2) myPageUtil.setAttr("plSkip", "enable", "false"); //plSkip.Enabled = false;
            else myPageUtil.setAttr("btnSkip", "class", "wfcmd_btnStyle");//btnSkip.CssClass = "wfcmd_btnStyle";
            if (myCaseInfo.nStatus == m_nFirst_Id)  //第一个状态，不显示退回 add by gaoww 20130402
            {   
                nBack = 3;
            }
   
            myFld_handle.fill_fld("下一环节-接收人", "cboSubmitGhid", 25,1);
            myFld_handle.fill_fld("接收环节", "cboProcess_back", 25,1);
            myFld_handle.set_AutoPostBack("cboProcess_back", true);
            myFld_handle.fill_fld("接收人", "cboBackGhid", 25,1);
            myFld_handle.fill_fld("接收环节", "cboProcess", 25,1);
            myFld_handle.set_AutoPostBack("cboProcess", true);
            myFld_handle.fill_fld("接收人", "cboSkipGhid", 25,1);
            myFld_handle.fill_fld("超时日期", "txtExpDate", 25);
            myFld_handle.fill_fld("接收人", "cboUpdateGhid", 25,1);
            
            //自定义控件
            myFld_handle.fill_fld("", "btnSubmit", 20,7);
            myFld_handle.fill_fld("", "btnBackward", 20,7);
            myFld_handle.fill_fld("", "btnSkip", 20,7);
            myFld_handle.fill_fld("", "btnUpdate", 20,7);
            myFld_handle.fill_fld("", "btnComplete", 20,7);
 
            //初始化提交座席工号下拉框
            LinkedHashMap<String, String> ht_cbobox = new LinkedHashMap<String, String>();

            //add by gaoww 20100224 增加提交判断，如果已经是最后一个环节，则不需要在选择下一环节接收人
            if (nSubmit == 0)
            {
                String strTemp =String.valueOf(myCaseInfo.nStatus + 1);  //下一环节的编号
                alRet = GetUid_byPriv(strTemp);
                //cboSubmitGhid.Items.put("不指定座席"); 
                ht_cbobox.put("不指定座席", "不指定座席");
                for (String strItem : alRet)
                {
                	//cboSubmitGhid.Items.put(strItem);
                    ht_cbobox.put(strItem, strItem);
                 }
                 myFld_handle.set_list("cboSubmitGhid", ht_cbobox);
                 myFld_handle.set_item_attr("cboSubmitGhid",Fld_attr.Fld_index,"0");
             }
            //初始化退回座席工号下拉框
            if (nBack == 0)
            {            	
                //cboProcess_back.Items.Clear();
               	ht_cbobox = new LinkedHashMap<String, String>();
                //foreach (KeyValuePair<int, string> deRet in m_dyProcess)
                for (Integer KeySet: m_dyProcess.keySet())
                {
                    if (KeySet >= myCaseInfo.nStatus) continue;
                    //cboProcess_back.Items.put(deRet.Key + "-" + deRet.Value);
                    String strValue = m_dyProcess.get(KeySet);
                    ht_cbobox.put(KeySet + "-" + strValue,KeySet + "-" + strValue);
                }
                myFld_handle.set_list("cboProcess_back", ht_cbobox);
                
                String strTemp = Functions.Substring(myFld_handle.get_item_value("cboProcess_back"), "", "-");
                if (strTemp.length() > 0)
                {
                	ht_cbobox = new LinkedHashMap<String, String>();
                	//cboBackGhid.Items.put("提交人");
                  	ht_cbobox.put("提交人", "提交人");
                    alRet = GetUid_byPriv(strTemp);
                    for (String strItem : alRet)
                    {
                        //cboBackGhid.Items.put(strItem);
                     	ht_cbobox.put(strItem, strItem);
                    }
                    myFld_handle.set_list("cboBackGhid", ht_cbobox);
                    myFld_handle.set_item_attr("cboBackGhid",Fld_attr.Fld_index,"0");
               }
            }
           
            //初始化跳转信息下拉框
            if (nSkip == 0)
            {
            	ht_cbobox = new LinkedHashMap<String, String>();
            	for (Integer KeySet: m_dyProcess.keySet())
                {
                	String strValue = m_dyProcess.get(KeySet);
                    //cboProcess.Items.put(KeySet+ "-" + strValue);
                    ht_cbobox.put(KeySet + "-" + strValue,KeySet + "-" + strValue);
                }
                myFld_handle.set_list("cboProcess", ht_cbobox);

                String strTemp = Functions.Substring(myFld_handle.get_item_value("cboProcess"), "", "-");
                if (strTemp.length() > 0)
                {
                 	ht_cbobox= new LinkedHashMap<String, String>();
                    //cboSkipGhid.Items.put("不指定座席");
                   	ht_cbobox.put("不指定座席", "不指定座席");
                    alRet = GetUid_byPriv(strTemp);
                    for (String strItem : alRet)
                    {
                        //cboSkipGhid.Items.put(strItem);
                      	ht_cbobox.put(strItem, strItem);
                    }
                    myFld_handle.set_list("cboSkipGhid", ht_cbobox);
                    myFld_handle.set_item_attr("cboSkipGhid",Fld_attr.Fld_index,"0");
                }
            }
            if (nSubmit == 0) //允许提交，更新、完成 才起作用
            {
            	myPageUtil.setAttr("btnUpdate", "class", "wfcmd_btnStyle");
                myPageUtil.setAttr("btnComplete", "class", "wfcmd_btnStyle");
                             
              	ht_cbobox.clear();
                //cboUpdateGhid.Items.put("不指定座席");
               	ht_cbobox.put("不指定座席", "不指定座席");
                alRet = GetUid_byPriv(String.valueOf(myCaseInfo.nProcess));
                for (String strItem : alRet)
                {
                    //cboUpdateGhid.Items.put(strItem);
                 	ht_cbobox.put(strItem, strItem);
                }
                myFld_handle.set_list("cboUpdateGhid", ht_cbobox);
                myFld_handle.set_item_attr("cboUpdateGhid",Fld_attr.Fld_index,"0");
            }
            else
            {
                //btnComplete.Enabled = false;
                //btnUpdate.Enabled = false;
            	myPageUtil.setAttr("btnComplete", "enable", "false");
            	myPageUtil.setAttr("btnUpdate", "enable", "false");
            }
            myFld_handle.FieldLinkClicked = this;  
            myFld_handle.fill_Panel("");    
        }

        //读取 该状态有读写权限的工号，格式 8600-张三
        private List<String> GetUid_byPriv(String strStatus)
        {
            List<String> alReturn = new ArrayList<String>();
            String strRoles = "", fld_value = "";
            //string strSql = "SELECT ROLES FROM CRM_CASE_LIST_PRIV WHERE CASETYPE='" + pType + "' AND (STATUS='" + strStatus + "' OR STATUS=-1) AND LEVELS=2";
            String strSql = "SELECT ROLES FROM CRM_CASE_ROLES_LEVELS WHERE (CASETYPE='" + pType + "') AND (STATUS='" + strStatus + "' OR STATUS=-1) AND (LV_WF=1)";

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
                strSql = "SELECT GHID,REAL_NAME FROM CTS_OPIDK";
            else
                strSql = "SELECT GHID,REAL_NAME FROM CTS_OPIDK WHERE ROLES IN (" + strRoles + ")";

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

        public void myFld_FieldLinkClicked(Object sender, String name, String parms, int nparms, int ntype)
        {
           //提交
        	if(name.equals("btnSubmit"))
  	        {
               myToolBar_btnItemClick(sender, "Submit","",0);
   	        }
        //退回
        	else	if(name.equals("btnBackward"))
            {
                myToolBar_btnItemClick(sender, "Backward","",0);
            }
        //跳转
        	else if(name.equals("btnSkip"))
    	    {
                myToolBar_btnItemClick(sender, "Skip","",0);
            }
      		else	if(name.equals("btnUpdate"))
            {
               myToolBar_btnItemClick(sender, "Update","",0);
            }
        	else	if(name.equals("btnComplete"))
            {
               myToolBar_btnItemClick(sender, "Complete","",0);
            }
        }
/*        
        protected void cboProcess_SelectedIndexChanged(Object sender, EventArgs e)
        {
            if (cboProcess.Text.equals("")) return;
            cboSkipGhid.Items.Clear();
            cboSkipGhid.Items.put("不指定座席");
            String strTemp = Functions.Substring(cboProcess.Text, "", "-");
            List<String> alRet = GetUid_byPriv(strTemp);
            for (String strItem : alRet)
            {
                cboSkipGhid.Items.put(strItem);
            }
        }

        protected void cboProcess_back_SelectedIndexChanged(Object sender, EventArgs e)
        {
            if (cboProcess_back.Text.equals("")) return;
            cboBackGhid.Items.Clear();
            String strTemp = Functions.Substring(cboProcess_back.Text, "", "-");
            List<String> alRet = GetUid_byPriv(strTemp);
            for (String strItem : alRet)
            {
                cboBackGhid.Items.put(strItem);
            }
        }
*/
        public void myToolBar_btnItemClick(Object sender, String name, String parms, int nparms)//, String parms, int nparms)
        {
            boolean bRet = myCase.get_authority(myCaseInfo, "wf");
            if (bRet == false)
            {
                rem("对不起，您无权处理此工单！");
                return;
            }
            int nResult = 0;
            if (name.equals("Submit"))//提交 
            {
                if (myCaseInfo.nStatus != m_nLast_Id)  //最后一个环节，不需要在选择下一环节，提交即闭单
                {
                    if (myFld_handle.get_item_value("cboSubmitGhid").equals(""))
                    {
                        rem("您没有选择下一环节接收人，请先选择！");
                        return;
                    }
                }
                List<String>  alRet;
                myCase.get_edit_priv(myCaseInfo, m_htPriv);
                //检查是否有不可见的命令
                if (m_htPriv.containsKey("FLD_MUST") == true)
                {
                    alRet = Arrays.asList(Functions.ht_Get_strValue("FLD_MUST", m_htPriv));               
                    String strFilter = "";
                    String strFilter_fld = "";
                    //去除 dtStru 中alRet（FLD_VALUE）包含的内容FLD_VALUE 
                    for (String strFld : alRet)
                    {
                        if (strFilter.length() < 1)
                            strFilter = myString.Format("(LEN({0})<1 OR {0} IS NULL)", strFld);  //modify by gaoww 20130403
                        else
                            strFilter += myString.Format(" OR (LEN({0})<1 OR {0} IS NULL)", strFld); //modify by gaoww 20130403

                        if (strFilter_fld.equals(""))
                            strFilter_fld = "FLD_VALUE='" + strFld + "'";
                        else
                            strFilter_fld += " OR FLD_VALUE='" + strFld + "'";
                    }
                    if (strFilter.length() > 0)
                    {
                        my_odbc pTable = new my_odbc(pmSys.conn_crm);
                        int rc = pTable.my_odbc_find("SELECT * FROM " + pTableName, "CASEID ='" + pCaseId + "' AND (" + strFilter + ")");
                        pTable.my_odbc_disconnect();
                        if (rc == 1)
                        {
                            String strFilter_name = Get_Filter_name(strFilter_fld);
                            rem("<" + strFilter_name + ">未填写正确，不能提交，请先正确填写！");                           
                            return;
                        }
                    }
                }

                String strGhid = Functions.Substring(myFld_handle.get_item_value("cboSubmitGhid"), "", "-");
                nResult = myWFCase.Submit_status(3, pmAgent.uid, strGhid, 0);
                if (nResult == 1)
                {
                    rem("提交工单成功！");
                    Functions.js_exec("javascript:fun_close();");
                }
                else if (nResult == -2)
                    rem("提交工单失败，未找到合适的流程节点！");
                else
                    rem("提交工单失败！");
            }
            else if (name.equals("Backward"))// 退回
            {
                if (myCaseInfo.nStatus == m_nFirst_Id)
                {
                    rem("此环节已是第一环节，无法执行退回操作！");
                    return;
                }
                if (myFld_handle.get_item_value("cboBackGhid").equals(""))
                {
                    rem("请先选择退回的座席工号！");
                    return;
                }
                String strGhid = Functions.Substring(myFld_handle.get_item_value("cboBackGhid"), "", "-");
                int nStatus_new = Functions.atoi(Functions.Substring(myFld_handle.get_item_value("cboProcess_back"), "", "-"));
                nResult = myWFCase.Submit_status(4, pmAgent.uid, strGhid, nStatus_new);
                if (nResult == 1)
                {
                    rem("退回工单成功！");
                    Functions.js_exec("javascript:fun_close();");
                }
                else if (nResult == -2)
                    rem("退回工单失败，未找到合适的流程节点！");
                else
                    rem("退回工单失败！");

            }
            else if (name.equals("Skip"))//跳转
            {
                if (myCase.isExist_Case(pCaseId) == false)
                {
                    rem("工单不存在，请先保存再跳转收！");
                    return;
                }
                String strGhid = Functions.Substring(myFld_handle.get_item_value("cboSkipGhid"), "", "-");
                int nStatus_new = Functions.atoi(Functions.Substring(myFld_handle.get_item_value("cboProcess"), "", "-"));
                nResult = myWFCase.Submit_status(5, pmAgent.uid, strGhid, nStatus_new);

                if (nResult == 1)
                {
                    rem("工单跳转成功！");
                    Functions.js_exec("javascript:fun_close();");
                }
                else if (nResult == -2)
                    rem("工单跳转失败，未找到合适的流程节点！");
                else
                    rem("工单跳转失败！");
            }
            else if (name.equals("Update"))//变更
            {
                if (myCase.isExist_Case(pCaseId) == false)
                {
                    rem("工单不存在，请先保存！");
                    return;
                }
                String strGhid = Functions.Substring(myFld_handle.get_item_value("cboUpdateGhid"), "", "-") + "-" + myFld_handle.get_item_value("txtExpDate");
                int nStatus_new = 0;
                nResult = myWFCase.Submit_status(6, pmAgent.uid, strGhid, nStatus_new);

                if (nResult == 1)
                {
                    rem("工单跳转成功！");
                    Functions.js_exec("javascript:fun_close();");
                }
                else if (nResult == -2)
                    rem("工单跳转失败，未找到合适的流程节点！");
                else
                    rem("工单跳转失败！");
            }
            else if (name.equals("Complete"))//完成
            {
                if (myCase.isExist_Case(pCaseId) == false)
                {
                    rem("工单不存在，请先保存！");
                    return;
                }
                String strGhid = pmAgent.uid;
                int nStatus_new = m_nLast_Id;
                nResult = myWFCase.Submit_status(9, pmAgent.uid, strGhid, nStatus_new);

                if (nResult == 1)
                {
                    rem("工单归档成功！");
                    Functions.js_exec("javascript:fun_close();");
                }
                else if (nResult == -2)
                    rem("工单归档失败，未找到合适的流程节点！");
                else
                    rem("工单归档失败！");
            }
        }
 
        protected void rem(String strMsg)
        {
            //lblRem.Text = strMsg;
            m_lblRem = strMsg;
        	//myFld_handle.set_item_value("lblRem",strMsg);
        }

        private String Get_Filter_name(String strFilter_fld)
        {
            String strReturn = "";
            if (strFilter_fld.equals("")) return strReturn;
            DataTable dtDesc = fun_Form.get_desc_data(myCase.DescName, strFilter_fld);
            if (dtDesc == null) return strReturn;
            for (int rows = 0; rows < dtDesc.getCount(); rows++)
            {
                String fld_value = Functions.dtCols_strValue(dtDesc, rows, "FLD_NAME");
                if (strReturn.equals(""))
                    strReturn = fld_value;
                else
                    strReturn += "," + fld_value;
            }
            return strReturn;
        }
    }

