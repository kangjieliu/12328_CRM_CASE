///########################################################################################
/// Copyright (C) 2000, ToneThink.Soft  All Rights Reserved. 
///#########################################################################################
/// 文件创建时间：2015-09-22
///   文件创建人：lics
/// 文件功能描述：工程师APP实时状态列表页面
///     调用格式：
///     
///     维护记录：
/// 2015.09.22：create by lics 
///#########################################################################################
package com.CallThink.ut_service.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import org.springframework.ui.Model;
import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.pmSys;
import com.CallThink.base.pmCode.UltraCRM_Page;
import com.CallThink.ut_service.pmModel_service.e_Level_service;
import com.ToneThink.DataTable.DataTable;
import com.ToneThink.DateTime.DateTime;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.pmMap;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.myString;
import com.ToneThink.ctsTools.WebUI.my_ToolStrip;
import com.ToneThink.ctsTools.WebUI.my_dataGrid;
import com.ToneThink.ctsTools.WebUI.my_SearchField;

    public class ghid_realtime_list extends UltraCRM_Page
    {
        private String pTableName = "SM_GHID_REALTIME";//工程师APP实时状态表
        private String pTableNameOpidk = "CTS_OPIDK";
        private int pType = 21;
        private String pOp = "list";
        private String pFilter = "";
        private String pCaseid = "";

        my_SearchField myFld_Query = new my_SearchField(2);
        my_dataGrid mydg = new my_dataGrid(51);
        my_ToolStrip myToolBar = new my_ToolStrip();
        private String m_Filter_search = ""; //本页面人工选择的查询条件，会话需要保存在Session中
        
        public void Page_Load(Object sender, Model model)
        {
            if (IsPostBack == false)//正被首次加载和访问
            {
                HashMap htQuery;
                pmMap res = fun_main.QuerySplit(Request); htQuery = res.htRet;
                int rc = res.nRet;
                if (rc > 0)
                {
                    pOp = Functions.ht_Get_strValue("cmd", htQuery);
                    pType = Functions.atoi(Functions.ht_Get_strValue("ntype", htQuery));
                    pFilter = Functions.ht_Get_strValue("filter", htQuery);
                    pCaseid = Functions.ht_Get_strValue("caseid", htQuery);
                    int nHistory = Functions.atoi(Functions.ht_Get_strValue("history", htQuery));
                    if (nHistory > 0)  //编辑页面返回
                    {
                        String strFilter_temp = Load_ss("pFilter_search_engr");
                        if (strFilter_temp.length() > 0) m_Filter_search = strFilter_temp;
                        mydg.nRestore_history = 1;
                    }
                }
                Save_vs("pType", pType);
                Save_vs("pOp", pOp);
                Save_vs("pFilter", pFilter);
                Save_vs("pCaseid", pCaseid);
                Save_ss("pFilter_search", m_Filter_search);       
            }
            else
            {
                pOp = Load_vs("pOp");
                pType = Functions.atoi(Load_vs("pType"));
                pFilter = Load_vs("pFilter");
                pCaseid = Load_vs("pCaseid");
                m_Filter_search = Load_ss("pFilter_search");
            }

		InitToolbar();
		Fillin_SearchField();
		Fillin_grid();
		myToolBar.render(model);
		myFld_Query.render(model);
		mydg.render(model);
	}

        private void Fillin_grid()
        {
            mydg.ID(Functions.atos(pType));
            mydg.SetTable(pTableName);
            mydg.SetSelectStr("SELECT * FROM " + pTableName + " WHERE 1>1");
            mydg.SetPageSize(pmAgent.page_maxline - 1);

            mydg.SetCaption("工程师APP实时状态列表");
            mydg.SetConnStr(pmSys.conn_crm);
            int i = 0;
            mydg.fill_fld(i++, "选择", "SELECT", 5, 9);
            mydg.fill_fld(i++, "工程师工号", "GHID", 10);
            //mydg.set_cols_cbo_list("GHID", "SELECT GHID,REAL_NAME FROM CTS_OPIDK", "GHID,REAL_NAME", pmSys.conn_callthink);
            mydg.fill_fld(i++, "工程师姓名", "OP_NAME", 10);
            mydg.fill_fld(i++, "行政区划", "XZQH", 0, 1);
            mydg.set_cols_cbo_list("XZQH", "SELECT FLD_ID,FLD_NAME FROM DICT_XZQH", "FLD_ID,FLD_NAME", pmSys.conn_crm);
            mydg.fill_fld(i++, "所属机构", "ORG_CODE", 18, 1);
            mydg.set_cols_cbo_list("ORG_CODE", "SELECT ORG_CODE,ORG_NAME FROM DICT_ORG_CODE", "ORG_CODE,ORG_NAME", pmSys.conn_crm);
            mydg.fill_fld(i++, "签入方式", "SIGN_TYPE", 0, 1);
            mydg.set_cols_cbo_list("SIGN_TYPE", new String[] { "0,正常签入、签出", "1,默认视为签入、手动签出" });
            mydg.fill_fld(i++, "值班状态", "STATUS", 8, 1);
            mydg.set_cols_cbo_list("STATUS", new String[] { "0,未签入", "1,签入", "9,失效" });
            mydg.fill_fld(i++, "审核状态", "",7, 8);
            mydg.fill_fld(i++, "X坐标", "POS_X", 0);
            mydg.fill_fld(i++, "Y坐标", "POS_Y", 0);
            mydg.fill_fld(i++, "手机电池电量", "APP_BATY", 0);
            mydg.fill_fld(i++, "手机剩余内存", "APP_RAM", 0);
            mydg.fill_fld(i++, "手机网络", "APP_WAN", 0);
            mydg.fill_fld(i++, "更新时间", "SDATE_UPDATE", 0, 5, "yyyy-MM-dd HH:mm:ss");
            mydg.fill_fld(i++, "已分配的任务/月", "TASK_TOTAL", 14, 1);
            mydg.fill_fld(i++, "未完成的任务", "TASK_WORKING", 12);
            mydg.fill_fld(i++, "待料的任务", "TASK_WAIT", 0);
            mydg.fill_fld(i++, "个人偏好业务范围列表", "BUSS_LIKE", 0);
            mydg.fill_fld(i++, "备注", " MEMO", 0);
            mydg.fill_fld(i++, "主观能动", "ACTIVE", 10);
            mydg.fill_fld(i++, "位置", "", 7, 8);
            mydg.fill_fld(i++, "查看", "", 7, 8);
            mydg.fill_fld(i++, "设置技能", "", 7, 8);
            mydg.fill_fld(i++, "选择机构", "", 7, 8);
            mydg.set_rows_color("STATUS", "STATUS=1","	#90EE90");// System.Drawing.Color.LightGreen);//根据工作状态行显示不同的颜色
            mydg.set_rows_color("STATUS", "STATUS=0", "#D3D3D3");//System.Drawing.Color.LightGray);
            mydg.set_rows_color("STATUS", "STATUS=9","#FF6347");// System.Drawing.Color.Tomato );

            mydg.RowDataFilled = this;// new RowDataFilledEventHandler(mydg_RowDataFilled);
            mydg.fill_header("dgvCase", "GHID", m_strFilter() + m_strOrder_by());
        }

        public void mydg_RowDataFilled(Object sender, int rows)
        {
            if (rows < 0) return; //表头行，不处理
            String strGhid = mydg.get_cell(rows, "GHID");
            if (strGhid.equals("")) return;
            int cols = mydg.get_idx("ACTIVE");
            if (cols >= 0)
            {
                String strNewUrl = "", strHtml = "";
                String strPosx = mydg.get_cell(rows, "POS_X");
                String strPosy = mydg.get_cell(rows, "POS_Y");
                //位置
                if (strPosx.equals("")==false && strPosy.equals("")==false)
                {
                    strNewUrl =pmSys.rootURL+ myString.Format("/ut_service/tools/map.aspx?cmd=posxy&pos_x={0}&pos_y={1}", strPosx, strPosy);
                    strHtml = myString.Format("<a href=\"javascript:Add_MainTab('{0}','{1}', 800, 600);\">{2}</a>", "工程师位置", strNewUrl, fun_main.CreateHtml_img("map.png", ""));
                    mydg.set_cell(rows,cols + 1,strHtml);
                }

                //详细
                strNewUrl = "ghid_realtime_edit.aspx?cmd=Edit&ghid=" + strGhid;
                strHtml = myString.Format("<a href='{0}'>{1}</a>", strNewUrl, fun_main.CreateHtml_img("Tasks.gif", ""));
                mydg.set_cell(rows,cols + 2,strHtml);

                //技能
                String strOrg_code = mydg.get_cell(rows, "ORG_CODE");
                strNewUrl =pmSys.rootURL+ "/ut_service/admin/ghid_skill_right.aspx?cmd=Edit&from=ghid&ghid=" + strGhid + "&key=" + strOrg_code;
                strHtml = myString.Format("<a href=\"javascript:Add_MainTab('{0}','{1}', 800, 600);\">{2}</a>", "设置技能-" + strGhid, strNewUrl, fun_main.CreateHtml_img("Report_add.gif", ""));
                  
                mydg.set_cell(rows,cols + 3,strHtml);

                //选择机构
                if (strOrg_code.equals(""))
                {
                    String strName = mydg.get_cell(rows, "OP_NAME");
                    strNewUrl =pmSys.rootURL+ "/ut_service/admin/ghid_org_tree_select.aspx?cmd=update&ghid=" + strGhid + "&name=" + strName;
                    strHtml = myString.Format("<a href=\"javascript:Add_MainTab('{0}','{1}',400,600);\">{2}</a>", "选择机构-" + strGhid, strNewUrl, fun_main.CreateHtml_img("Report_add.gif", ""));
                    mydg.set_cell(rows,cols + 4,strHtml);
                }
            }

            cols = mydg.get_idx("STATUS");
            if (cols >= 0) //审核状态
            {
                my_odbc pTable = new my_odbc(pmSys.conn_crm);
                int rc = pTable.my_odbc_find("CRM_VISITOR_PERMIT","GHID='"+strGhid +"' AND STATUS='1'");
                pTable.my_odbc_disconnect();
                if(rc>0)
                    mydg.set_cell(rows,cols + 1,"已审核");
                else
                    mydg.set_cell(rows,cols + 1,"未审核");
            }
        }

        private void InitToolbar()
        {
            myToolBar.fill_fld("刷新", "Query_all");
            //myToorBar.fill_fld("Separator", "Separator1", 0, 3);
            //myToorBar.fill_fld("增加", "AddNew");
            myToolBar.fill_fld("Separator", "Separator2", 0, 3);
            myToolBar.fill_fld_confirm("删除", "Delete", " 确实要删除所选资料吗？");
            myToolBar.fill_fld("Separator", "Separator3", 0, 3);
            myToolBar.fill_fld("查询", "Query", 0, 10);
            myToolBar.fill_fld("Separator", "Separator0", 0, 3);
            myToolBar.fill_fld("选择导入座席类型", "Select_Type", 25, 4, "选择导入座席类型");
            LinkedHashMap<String, String> linkedHashMap=new LinkedHashMap<>();
            linkedHashMap.put("0", "服务人员");
            linkedHashMap.put("1", "呼叫中心人员");
            linkedHashMap.put("2", "全部");
            //myToolBar.set_list("Select_Type", "服务人员,呼叫中心人员,全部");
            myToolBar.set_list("Select_Type", linkedHashMap);
            
            myToolBar.fill_fld_confirm("从座席资料导入", "Load", "确定从座席资料导入?");
            myToolBar.fill_fld("Separator", "Separator4", 0, 3);
            myToolBar.fill_fld(fun_main.Term("LBL_TOEXCEL"), "Output");
            myToolBar.fill_fld("Separator", "Separator5", 0, 3);
            myToolBar.btnItemClick = this;// new btnClickEventHandler(myToolBar_btnItemClick);
            myToolBar.fill_toolStrip("plCommand");
        }

        public void myToolBar_btnItemClick(Object sender, String name, String parms, int nparms)
        {
            //刷新
            if (name.equals("Query_all"))
            {
                m_Filter_search = "";
                Save_ss("pFilter_search", m_Filter_search);  //存入Session 变量，用于从编辑页面返回时恢复显示
                mydg.refresh("GHID", m_strFilter() + m_strOrder_by());
            }
            else if (name.equals("Load"))
            {
                String strSql = myString.Format("SELECT * FROM {0} ", pTableNameOpidk);
                String strFilter = "UTYPE=10";
               
                int nSelectType =Functions.atoi(myToolBar.get_item_value("Select_Type")); //myToolBar.get_item_cbo("Select_Type").SelectedIndex;
                if (nSelectType == 1)
                {
                    strFilter = "UTYPE>0 AND UTYPE<10";
                }
                else if(nSelectType ==2)
                    strFilter = "UTYPE>0 AND UTYPE<=10";
                strSql += " WHERE " + strFilter;
                DataTable dtTemp = Functions.dt_GetTable(strSql, "", pmSys.conn_callthink);
                if (dtTemp.getCount() <= 0)
                {
                    Functions.MsgBox("没有符合条件的坐席！");
                    return;
                }
                int nCountSucc = 0;
                int nCountFal = 0;
                for (int i = 0; i < dtTemp.getCount(); i++)
                {
                    String strGhid = Functions.drCols_strValue(dtTemp.Rows().get(i), "GHID");
                    String strSqlTemp = myString.Format("SELECT GHID FROM {0} WHERE GHID='{1}'", pTableName, strGhid);
                    DataTable dtCode = Functions.dt_GetTable("SELECT ORG_CODE  FROM DICT_ORG_GHID_REAL WHERE REL_GHID='" + strGhid + "'", "", pmSys.conn_crm);
                    String strCode = "";//所属机构代码
                    if (dtCode.getCount() > 0)
                        strCode = Functions.drCols_strValue(dtCode.Rows().get(0), "ORG_CODE");

                    DataTable dt = Functions.dt_GetTable(strSqlTemp, "", pmSys.conn_crm);
                    if (dt.getCount() <= 0)//如果工程师APP实时状态表不存在这个工程师则导入到工程师APP实时状态表
                    {
                        my_odbc mydb = new my_odbc(pmSys.conn_crm);
                        HashMap htTemp = new HashMap();
                        Functions.ht_SaveEx("GHID", strGhid,htTemp);//工号
                        Functions.ht_SaveEx("OP_NAME", Functions.drCols_strValue(dtTemp.Rows().get(i), "REAL_NAME"),htTemp);//工程师姓名
                       
                        Functions.ht_SaveEx("ORG_CODE", strCode,htTemp);//所属机构代码
                        Functions.ht_SaveEx("SDATE_UPDATE", DateTime.Now().ToString("yyyy-MM-dd HH:mm:ss"),htTemp);//更新时间
                        Functions.ht_SaveEx("SIGN_TYPE",0 ,htTemp);//签入方式
                        Functions.ht_SaveEx("STATUS",0 ,htTemp);//工作状态
                        int nRtn = mydb.my_odbc_addnew(pTableName, htTemp);
                        mydb.my_odbc_disconnect();
                        if (nRtn > 0)
                        {
                            nCountSucc++;
                        }
                        else
                        {
                            nCountFal++;
                        }
                    }
                    else //如果有， 则更新机构和姓名
                    {
                        my_odbc mydb = new my_odbc(pmSys.conn_crm);
                        HashMap htTemp = new HashMap();
                        Functions.ht_SaveEx("ORG_CODE", strCode,htTemp);//所属机构代码                       
                        Functions.ht_SaveEx("OP_NAME", Functions.drCols_strValue(dtTemp.Rows().get(i), "REAL_NAME"),htTemp);//工程师姓名
                        Functions.ht_SaveEx("SDATE_UPDATE", DateTime.Now().ToString("yyyy-MM-dd HH:mm:ss"),htTemp);//更新时间
                        int nRtn = mydb.my_odbc_update(pTableName, htTemp, "GHID='" + strGhid + "'");
                    }
                 }

                mydg.refresh("GHID", m_strFilter() + m_strOrder_by());
                Functions.MsgBox("导入成功" + nCountSucc + "条，失败" + nCountFal + "条！");
            }      
            else if (name.equals("Delete"))
            {
                int nPos = 0;
                if (mydg.RowCount() <= 0) return;
                List<String> alRet = mydg.GetSelectedKey("GHID");
                if (alRet.size() == 0)
                {
                    Functions.MsgBox("请先选中要删除的数据！");
                    return;
                }
                my_odbc pTable = new my_odbc(pmSys.conn_crm);
                for (int rows = 0; rows < mydg.RowCount(); rows++)
                {
                    if (mydg.isSelected(rows) == true)
                    {

                        String strGhid = mydg.get_cell(rows, "GHID");

                        int rc = pTable.my_odbc_find(pTableName, "GHID=" + strGhid);
                        if (rc == 1)
                        {
                            pTable.my_odbc_delete(pTableName, "GHID=" + strGhid);
                            nPos++;
                        }
                        pTable.my_odbc_disconnect();
                    }
                }
                mydg.refresh("GHID", m_strFilter() + m_strOrder_by());
                if (nPos > 0)
                    Functions.MsgBox("成功删除<" + nPos + ">条记录！");
                else
                    Functions.MsgBox("删除记录失败，请选中要删除的记录！");
            }
            else if (name.equals("Output"))
            {
                mydg.Output_toExcelFile("任务资料列表", pmSys.CaptionExport + "_任务资料", 0);
            }
        }


        private void Fillin_SearchField()
        {
            myFld_Query.SetWidth(pmAgent.content_width);
            myFld_Query.SetMaxLabelLenth(80);
            myFld_Query.SetMaxLabelLenth_col2(80);
            myFld_Query.funName_OnClientClick("mySearch_FieldLinkClicked");

            myFld_Query.fill_fld("工程师工号", "GHID", 24);
            myFld_Query.fill_fld("工程师姓名", "OP_NAME", 24);
            myFld_Query.fill_fld("工作状态", "STATUS", 24, 1);
            myFld_Query.set_list("STATUS", new String[] { "0,未签入", "1,签入", "9,失效" });
            ArrayList<String> alButton_ex = new ArrayList<>();
            alButton_ex.add("DESP=清除;NAME=Reset;");
            myFld_Query.fill_fld_button("查询", "Search", null, false, alButton_ex, "right-338px");
            myFld_Query.fill_Panel("plSeach");
            myFld_Query.FieldLinkClicked = this;// new SearchFieldLinkClickedEventHandler(myFld_Query_FieldLinkClicked);
        }

        @Override
		public void mySearch_FieldLinkClicked(Object sender, String name, String parms, int nparms, int ntype) {
            if (name.equals("Search"))
            {
                //生成查询条件
                String strFilter_search = GetFilter_search();
                m_Filter_search = strFilter_search;
                Save_ss("pFilter_search", m_Filter_search);
                mydg.refresh("GHID", m_strFilter() + m_strOrder_by());               
            }
        }

         /// <summary>
        /// 查询条件
        /// </summary>
        /// <returns>查询条件字符串</returns>
        private String GetFilter_search()
        {
            //工程师编号
            String strGhid = myFld_Query.get_item_value("GHID");
            //工程师姓名
            String strName = myFld_Query.get_item_value("OP_NAME");
            //工作状态
            String strEStatus = myFld_Query.get_item_value("STATUS");
            String strFilter_search ="";
            if (strGhid.length() > 0)
            {
                if (strFilter_search.length() > 0) strFilter_search += " AND ";
                strFilter_search += "GHID LIKE '%" + strGhid + "%'";
            }
            if (strName.length() > 0)
            {
                if (strFilter_search.length() > 0) strFilter_search += " AND ";
                strFilter_search += "OP_NAME LIKE '%" + strName + "%'";
            }
            if (strEStatus.length() > 0)
            {
                if (strFilter_search.length() > 0) strFilter_search += " AND ";
                strFilter_search += "STATUS='" + strEStatus + "'";
            }
            if (strFilter_search.equals(""))
            {
                strFilter_search = "1<>1";
            }
            return strFilter_search;
        }

        private String m_strFilter()
        {
                String strFilter = m_strFilter_base();
                if (m_strFilter_priv().length() > 0)
                {
                    if (strFilter.length() > 0) strFilter += " AND ";
                    strFilter += m_strFilter_priv();
                }
                return strFilter;
        }

        //基本的显示条件
        private String m_strFilter_base()
        {
                String strFilter = "";
                if (pFilter.length() > 0)
                {
                    strFilter = pFilter;
                }

                if (m_Filter_search.length() > 0)
                {
                    if (strFilter.length() > 0) strFilter += " AND ";
                    strFilter += m_Filter_search;
                }
                return strFilter;
        }

        //给予角色的特权 privileged
        private String m_strFilter_priv()
        {
                String strFilter = "1=1";
                String strCode =myString.TrimEnd(pmAgent.c_Info.agent_org_code, '0');
                if (pmAgent.c_Levels.check_authority(e_Level_service.service_view_admin) == false)//如果没有服务系统资料浏览最高权限
                {
                    //服务系统资料浏览增强权限
                    if (pmAgent.c_Levels.check_authority(e_Level_service.service_view_adv) == true)//显示本建单人所属机构代码以及下级属机构代码的服务单
                        strFilter = myString.Format(" (ORG_CODE LIKE '{0}%')", strCode);
                    else//两个权限都没有的时候
                        strFilter = myString.Format(" (ORG_CODE = '{0}')", pmAgent.c_Info.agent_org_code);
                }               

                return strFilter;
        }

        //排序规则
        private String m_strOrder_by()
        {
                String strOrderby = " ORDER BY GHID";
                return strOrderby;
        }
    }

