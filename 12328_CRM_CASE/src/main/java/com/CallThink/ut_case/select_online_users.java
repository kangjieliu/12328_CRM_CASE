///########################################################################################
/// Copyright (C) 2000, ToneThink.Soft  All Rights Reserved. 
///#########################################################################################
/// 文件创建时间：2015-01-16
///   文件创建人：xutt
/// 文件功能描述：弹出在线坐席人员
///#########################################################################################
package com.CallThink.ut_case;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;

import com.CallThink.base.pmClass.clas_Level;
import com.CallThink.base.pmClass.e_Level_base;
import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.pmSys;
import com.CallThink.base.pmClass.pmAgent_info;
import com.CallThink.base.pmCode.UltraCRM_Page;
import com.CallThink.base.support.pmInfo;
import com.CallThink.ut_case.pmModel_case.clas_levels_case;
import com.CallThink.ut_case.pmModel_case.fun_case;
import com.CallThink.ut_tele_calls.softcall.cls_softcall;
import com.ToneThink.DataTable.DataTable;
import com.ToneThink.DateTime.DateTime;
import com.ToneThink.DataTable.DataRow;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.pmList;
import com.ToneThink.ctsTools.myUtility.pmMap;
import com.sun.prism.paint.Color;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.myString;
import com.ToneThink.ctsTools.WebUI.my_ToolStrip;
import com.ToneThink.ctsTools.WebUI.my_dataGrid;
import com.ToneThink.ctsTools.WebUI.my_SearchField;

    public class select_online_users  extends UltraCRM_Page
    {
        private String pOp = "";     //用来判断是哪种操作类型选择坐席员 Syng_Deal-同步处置,Request-请求接管,Upgrade-警情升级
        private String pGhid = "";   //要显示的工号,All-显示全部在线座席
        private String pCaseID = ""; //编号
        private String pTableName = "CRM_CASE"; //
        private int pType; //类型

        pmAgent_info pmAgent;
        private my_dataGrid mydg = new my_dataGrid();
        private my_ToolStrip myToolBar = new my_ToolStrip();
        private String pId_dlg = ""; //页面标志
    
        public void Page_Load(Object sender, Model model)
        {
            pmAgent = fun_main.GetParm();         

            //add by gaoww 20130329 rdbutton改为服务器端控件，否则页面刷新后，无法记住选定项
            if (IsPostBack == false)//正被首次加载和访问
            {
               
                HashMap htQuery;
                pmMap res = fun_main.QuerySplit(Request); htQuery = res.htRet;
                int rc = res.nRet;
                if (rc > 0)
                {
                    pOp = Functions.ht_Get_strValue("cmd", htQuery);
                    pId_dlg = Functions.ht_Get_strValue("nIdlg", htQuery);
                    pCaseID = Functions.ht_Get_strValue("caseid", htQuery);
                    pType =Functions.atoi( Functions.ht_Get_strValue("type", htQuery));
            
                  
                }
                Save_vs("pOp", pOp);
                Save_vs("pGhid", pGhid);
                Save_vs("pId_dlg", pId_dlg);
                Save_vs("pCaseID", pCaseID);
                Save_vs("pType", pType);
            }
            else
            {
                pOp = Load_vs("pOp");
                pGhid = Load_vs("pGhid");
                pId_dlg = Load_vs("pId_dlg");
                pCaseID = Load_vs("pCaseID");
                pType = Functions.atoi(Load_vs("pType"));
              
            }
            InitToolbar();
            Fillin_dataGrid();
            myToolBar.render(model);
            mydg.render(model);
}
        /// <summary>
        /// 选定按钮事件
        /// </summary>
        private void InitToolbar()
        {
            myToolBar.fill_fld("选定", "Select");
        	myToolBar.fill_fld(fun_main.Term("LBL_CANCEL"), "Cancel", "return fun_close('" + pCaseID + "')");
            myToolBar.fill_toolStrip("plCommand");
            myToolBar.btnItemClick = this;// new btnClickEventHandler(myToolBar_btnItemClick);
        }

       public void myToolBar_btnItemClick(Object sender, String name, String parms, int nparms)
        {
            if (name.equals("Select"))
            {
                String strGhid = "", strExt = "";
                List<String> alRet = mydg.GetSelectedKey("GHID");
                if (alRet.size() == 0)
                {
                    Functions.MsgBox("请先选中要加载的记录！");
                    return;
                }
             
                for (String strId: alRet)
                {                    
                    String strUidInfo = pmInfo.myATClient.ATGetUidInfo(strId);
                    String strCallStatus = Functions.Substring(strUidInfo, "CALLS",1);
                    String strStatus = Functions.Substring(strUidInfo, "STATUS",1);
                    String strGhid_Name = Functions.Substring(strUidInfo, "NAME",1);
                    
                    if (strStatus.equals("00"))
                    {
                        Functions.MsgBox("选中的座席已登出，请重新选择！");
                        return;
                    }
                    if (strCallStatus.equals("01")==false)
                    {
                        Functions.MsgBox("选中的座席正在通话中，请重新选择！");
                        return;

                    }
                    strExt = Functions.Substring(strUidInfo, "EXT",1);
                    strGhid = Functions.Substring(strUidInfo, "UID",1);
                    String strTrace_Info = myString.Format("TABLE={0};CASEID={1};", pTableName, pCaseID);                
                   //  if (pOp.equals("Return_Deal")) //退回
                   // 	fun_case.addnew_trace_log(pType, "退回", "本次警情退回[" + strGhid_Name + "(" + strGhid + ")]", strTrace_Info,pmAgent.uid,pmAgent.name);
                    if (pOp.equals("Transfer")) //转办
                    	fun_case.addnew_trace_log(pType, "转办", "本次警情转办给[" + strGhid_Name + "(" + strGhid + ")]", strTrace_Info,pmAgent.uid,pmAgent.name);
                  
                    String strRawUrl = myString.Format("[{Idlg:'{0}',kid:'{1}',ext_dest:'{2}',key:'{3}',uid_scr:'{4}',ext_scr:'{5}'}]", pCaseID, strGhid, strExt, pOp, pmAgent.uid, pmAgent.extNum);
                    Functions.js_exec("onBtnUpdate(" + strRawUrl + ")");//add by xutt 20150116
                 //   Functions.js_exec("onBtnUpdate(66)");//add by xutt 20150116
                }
               
              
            }
        }
     
        private void Fillin_dataGrid()
        {
            int i = 0;
            mydg.SetTable("CTS_OPIDK");
            mydg.ID(pId_dlg);

            mydg.SetConnStr(pmSys.conn_callthink);
            mydg.SetPagerMode(0);
            mydg.SetPageSize(50);
            mydg.fill_fld(i++, "选择", "SELECT", 4, 9, "radio");
            mydg.fill_fld(i++, "工号", "GHID", 10);
            mydg.fill_fld(i++, "姓名", "REAL_NAME", 15);
            mydg.fill_fld(i++, "分机", "EXT", 10);
            mydg.fill_fld(i++, "手机", "MOBILENO", 0);
            mydg.fill_fld(i++, "角色", "ROLES", 12, 1);
            mydg.set_cols_cbo_list("ROLES", "SELECT ROLES,RNAME FROM CTS_OPIDK_ROLES", "ROLES,RNAME", pmSys.conn_callthink);
          //  mydg.fill_fld(i++, "状态", "ADDR", 12);
            cls_softcall myCls = new cls_softcall();
            DataTable dtRet = new DataTable();
           String strLogin = "";
           String   strSysInfo = "Get_Agent_Idle_byLevel;LEVEL=" + clas_levels_case.case_submit + ";TYPE=1;";
            try
            {
                if (strSysInfo.equals("")==false)
                {
                    //获取空闲坐席
                    String strUid_on_list = pmInfo.myATClient.ATGetSystemInfo(strSysInfo);
                    String[] Snum = strUid_on_list.split("[|]");
                    if (strUid_on_list.equals("")==false && Snum.length > 0)
                    {
                        for (String struid : Snum)
                        {
                            if (struid.equals(""))
                                continue;
                            else if (struid == pmAgent.uid)//add by xutt不能自己给自己发消息
                                continue;
                            if (strLogin.equals("")==false) strLogin += ",";
                            strLogin += "'" + struid + "'";
                        }
                    }
                }
               
            }
            catch (Exception ex)
            {
                
               
            }
           
       
           // mydg.set_rows_color("GHID", "", Color.GREEN.toString());
         
                dtRet = myCls.GetUid_dt(pmAgent.nRoles, myCls.GetUid_filter(strLogin, 1));
            mydg.RowDataFilled = this;// new RowDataFilledEventHandler(mydg_RowDataFilled);
            mydg.fill_header("dgvList", dtRet);

        }

        public void mydg_RowDataFilled(Object sender, int rows)
        {
        }

    }

