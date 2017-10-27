package com.CallThink.ut_service.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import com.CallThink.base.pmClass.e_Level_base;
import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.pmSys;
import com.CallThink.base.pmClass.pmAgent_info;
import com.CallThink.base.pmCode.UltraCRM_Page;
import com.CallThink.ut_service.pmModel_service.e_Level_service;
import com.ToneThink.DataTable.DataTable;
import com.ToneThink.DataTable.DataRow;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.pmList;
import com.ToneThink.ctsTools.myUtility.pmMap;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.myString;
import com.ToneThink.ctsTools.WebUI.my_ToolStrip;
import com.ToneThink.ctsTools.WebUI.my_dataGrid;
import com.ToneThink.ctsTools.WebUI.my_SearchField;

    public class ghid_permit_list extends UltraCRM_Page
    {
        private String pTableName = "CRM_VISITOR_PERMIT";
        private String pOp = "";
        private String pToken = "";
        private String pFilter = "";
        my_ToolStrip myToolBar = new my_ToolStrip();
        my_dataGrid mydg = new my_dataGrid();
        my_SearchField myFld_Query = new my_SearchField(3);

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
                    pToken = Functions.ht_Get_strValue("token", htQuery);
                    int nHistory = Functions.atoi(Functions.ht_Get_strValue("history", htQuery));
                    if (nHistory > 0)  //编辑页面返回
                    {
                        String strFilter_temp = Load_ss("pFilter_search_engr");
                        if (strFilter_temp.length() > 0) m_Filter_search = strFilter_temp;
                        mydg.nRestore_history = 1;
                    }
                }
                if (pOp.equals("view"))//从菜单进入显示全部
                {
                    pFilter = "1=1";
                }
                Save_vs("pFilter", pFilter);
                Save_vs("pOp", pOp);
                Save_vs("pToken", pToken);
                Save_ss("pFilter_search", m_Filter_search);        
            }
            else
            {
                pOp = Load_vs("pOp");
                pFilter = Load_vs("pFilter");
                pToken = Load_vs("pToken");
                m_Filter_search = Load_ss("pFilter_search");
            }
            InitToolbar();
            Fillin_SearchField();
            Fillin_grid();
            if (pOp.equals("popup"))//从弹屏进入 
            {
                if (mydg.RowCount() == 1)
                {
                    DataTable dtTemp = Functions.dt_GetTable("SELECT VID FROM " + pTableName + " WHERE TOKEN='" + pToken + "'", "", pmSys.conn_crm);
                    String strVid = "";
                    if (dtTemp.getCount() > 0)
                    {
                        strVid = Functions.drCols_strValue(dtTemp.Rows().get(0), "VID");
                    }
                    Functions.Redirect("ghid_permit_edit.aspx?cmd=Edit&vid=" + strVid);//modify by xutt20151023新增参数Edit
                }
                else
                {
                    pFilter = " STATUS=0 ";
                }
            }
        
            myToolBar.render(model);
            myFld_Query.render(model);
            mydg.render(model);
}
        private void InitToolbar()
        {
            myToolBar.fill_fld("增加", "Addnew"); //add by xutt 20151023 新增增加按钮
            myToolBar.fill_fld_confirm("删除", "Delete", "确实要删除选中的记录吗？");
            myToolBar.fill_fld("Separator", "Separator6", 0, 3);//add by xutt 20151023
            myToolBar.fill_fld("批量审核", "Update");
            myToolBar.fill_fld("Separator", "Separator5", 0, 3);          
            myToolBar.fill_fld("查询", "Query", 0, 10);
            myToolBar.fill_fld("显示全部", "Refresh");
            myToolBar.fill_fld("Separator", "Separator4", 0, 3);
            //myToorBar.fill_fld(fun_main.Term("LBL_TOEXCEL"), "Output");          
            myToolBar.btnItemClick = this;// MyToorBar_btnItemClick;
            myToolBar.fill_toolStrip("plCommand");
        }

        public void myToolBar_btnItemClick(Object sender, String name, String parms, int nparms)
        {
            if (name.equals("Output"))
            {
                mydg.Output_toExcelFile("任务资料列表", pmSys.CaptionExport + "_任务资料", 0);
            }
            else if (name.equals("Refresh"))
            {
                m_Filter_search = "";
                Save_ss("pFilter_search", m_Filter_search);  //存入Session 变量，用于从编辑页面返回时恢复显示
                mydg.refresh("VID", m_strFilter() + m_strOrder_by());               
            }
            else if (name.equals("Update"))
            {
                int nPos = 0;
                if (mydg.RowCount() <= 0) return;
                List<String> alRet = mydg.GetSelectedKey("VID");
                if (alRet.size() == 0)
                {
                    Functions.MsgBox("请先选中要更改的数据！");
                    return;
                }
                my_odbc pTable = new my_odbc(pmSys.conn_crm);
                for (int rows = 0; rows < mydg.RowCount(); rows++)
                {
                    if (mydg.isSelected(rows) == true)
                    {

                        String strVid = mydg.get_cell(rows, "VID");
                        int rc = pTable.my_odbc_find(pTableName, "VID=" + strVid);
                        if (rc == 1)
                        {
                            pTable.my_odbc_update(pTableName, "STATUS=1", "VID=" + strVid);
                            nPos++;
                        }
                        pTable.my_odbc_disconnect();
                    }
                }

                mydg.refresh("VID", m_strFilter() + m_strOrder_by());
                if (nPos > 0)
                    Functions.MsgBox("成功修改" + nPos + "条！");
                else
                    Functions.MsgBox("批量审核失败！");
            }
            else if (name.equals("Addnew"))//add by xutt 20151023增加认证
            {
                Functions.Redirect("ghid_permit_edit.aspx?cmd=AddNew");
            }
            else if (name.equals("Delete"))
            {
                if (mydg.RowCount() <= 0) return;
                List<String> alRet = mydg.GetSelectedKey("VID");
                if (alRet.size() == 0)
                {
                    Functions.MsgBox("请先选中需要删除的记录！");
                    return;
                }
                else
                {
                    int nFind = alRet.size();
                    String deleteSQL = "";
                    for (String strKey : alRet)
                    {
                        if (deleteSQL.length() > 0) deleteSQL += " OR ";
                        deleteSQL += myString.Format("(VID='{0}')", strKey);
                    }
                    my_odbc pCust = new my_odbc(pmSys.conn_crm);
                    int rc = pCust.my_odbc_delete(pTableName, deleteSQL);
                    pCust.my_odbc_disconnect();
                    if (rc > 0) Functions.MsgBox("已删除<" + alRet.size() + ">条记录！");
                    else
                        Functions.MsgBox("删除记录失败！");
                    mydg.refresh("VID", m_strFilter() + m_strOrder_by());
                }
            }
        }


        private void Fillin_SearchField()
        {
            myFld_Query.SetWidth(pmAgent.content_width);
            myFld_Query.SetMaxLabelLenth(80);
            myFld_Query.SetMaxLabelLenth_col2(80);
            myFld_Query.funName_OnClientClick("mySearch_FieldLinkClicked");

            myFld_Query.fill_fld("工号", "GHID", 20);
            myFld_Query.fill_fld("审核状态", "STATUS", 20, 1);
            myFld_Query.set_list("STATUS", "未审核,已审核");
            ArrayList<String> alButton_ex = new ArrayList();
            alButton_ex.add("DESP=清除;NAME=Reset;");
            myFld_Query.fill_fld_button("查询", "Search", null, false, alButton_ex, "right-200px");
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
                mydg.refresh("VID", m_strFilter() + m_strOrder_by());
            }
        }


        private void Fillin_grid()
        {
            mydg.SetTable(pTableName);
            mydg.SetSelectStr("SELECT * FROM " + pTableName + " WHERE 1<>1");
            mydg.SetPageSize(pmAgent.page_maxline - 1);

            mydg.SetConnStr(pmSys.conn_crm);
            int i = 0;
            mydg.fill_fld(i++, "VID", "VID", 0);
            mydg.fill_fld(i++, "选择", "SELECT", 5, 9);
            mydg.fill_fld(i++, "IPADDR", "IPADDR", 0);
            mydg.fill_fld(i++, "TOKEN", "TOKEN", 30, 8, "CMDNAME=Edits;");
            mydg.fill_fld(i++, "工号", "GHID", 30);
            mydg.fill_fld(i++, "姓名", "OP_NAME", 30);
            mydg.fill_fld(i++, "状态", "STATUS", 20, 1);
            mydg.set_cols_cbo_list("STATUS", new String[]{"0,未审核","1,已审核"});  

            mydg.fill_fld(i++, "备注", "MEMO", -1);
            mydg.CellLinkClicked = this;// Mydg_CellLinkClicked;
            mydg.fill_header("dgvCase", "VID", m_strFilter() + m_strOrder_by());
        }

        public void mydg_CellLinkClicked(Object sender, String text, int rows, int cols)
        {
            if (text.equals("Edits"))
            {
                String strVid = mydg.get_cell(rows, "VID");
               Functions.Redirect("ghid_permit_edit.aspx?cmd=Edit&vid=" + strVid);//modify by xutt 20151023修改传递参数编辑页面用Edit
            }
        }

        /// <summary>
        /// 查询条件
        /// </summary>
        /// <returns>查询条件字符串</returns>
        private String GetFilter_search()
        {
            //工号
            String strGhid = myFld_Query.get_item_value("GHID");
            //审核状态
            String strStatus = myFld_Query.get_item_value("STATUS");
            String strFilter_search ="";
            if (strGhid.length() > 0)
            {
                if (strFilter_search.length() > 0) strFilter_search += " AND ";
                strFilter_search += "GHID LIKE '%" + strGhid + "%'";
            }
            if (strStatus.length() > 0)
            {
                if (strFilter_search.length() > 0) strFilter_search += " AND ";
                strFilter_search += "STATUS = '" + strStatus + "'";
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
                String strFilter = "(TOKEN!='' AND TOKEN IS NOT NULL )";
                if (pFilter.length() > 0)
                {
                    if (strFilter.length() > 0) strFilter += " AND ";
                    strFilter += pFilter;
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
                String strFilter = "", strTemp = "";
                String strCode =myString.Format(pmAgent.c_Info.agent_org_code, '0');
                if (pmAgent.c_Levels.check_authority(e_Level_service.service_view_admin) == false)//如果没有服务系统资料浏览最高权限
                {
                    //服务系统资料浏览增强权限
                    if (pmAgent.c_Levels.check_authority(e_Level_service.service_view_adv) == true)//显示本建单人所属机构代码以及下级属机构代码的服务单
                        strTemp = myString.Format(" (ORG_CODE LIKE '{0}%')", strCode);
                    else//两个权限都没有的时候
                        strTemp = myString.Format(" (ORG_CODE = '{0}')", pmAgent.c_Info.agent_org_code);
                    if (strTemp.equals("")==false) //到机构工号关联表中，查出当前角色可以查看的工号
                    {
                        DataTable dtGhid;
                        my_odbc pTable = new my_odbc(pmSys.conn_crm);
                        pmList res =pTable.my_odbc_find("SELECT REL_GHID AS GHID FROM DICT_ORG_GHID_REAL WHERE " + strTemp,0); dtGhid = res.dtRet;
                        int rc = res.nRet;
                        pTable.my_odbc_disconnect();
                        strTemp = "";
                        for (int rows = 0; rows < dtGhid.getCount(); rows++)
                        {
                            String strGhid = Functions.dtCols_strValue(dtGhid, rows, "GHID");
                            if (strTemp.equals("")==false) strTemp += ",";
                            strTemp += "'" + strGhid + "'";
                        }
                    }
                    if (strTemp.equals(""))
                        strFilter = "1>1";
                    else
                        strFilter = "GHID IN (" + strTemp + ")";
                }
                else
                    strFilter = "1=1";

                return strFilter;
        }

        //排序规则
        private String m_strOrder_by()
        {
                String strOrderby = " ORDER BY VID";
                return strOrderby;
        }
    }

