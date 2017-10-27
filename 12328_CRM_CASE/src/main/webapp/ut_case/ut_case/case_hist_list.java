package com.CallThink.ut_case;

import java.util.HashMap;
import java.util.List;

import org.springframework.ui.Model;

import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.pmSys;
import com.CallThink.base.pmCode.UltraCRM_Page;
import com.ToneThink.DateTime.DateTime;
import com.ToneThink.ctsTools.WebUI.my_ToolStrip;
import com.ToneThink.ctsTools.WebUI.my_dataGrid;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.myString;
import com.ToneThink.ctsTools.myUtility.pmMap;

public class case_hist_list  extends UltraCRM_Page
{ 
	 private String pCaseId = "";
     private String pOp = "addnew";
     private int pType = 0;
     private String pTableName = "CRM_CASE_HIST";

     private my_dataGrid mydg = new my_dataGrid(30);
     my_ToolStrip myToolBar = new my_ToolStrip();

        	 
    public void Page_Load(Object sender, Model model) {

        if (IsPostBack == false)//正被首次加载和访问
        {
        	pmMap res = fun_main.QuerySplit(Request);
            int rc=res.nRet;
            if(rc>0)
            {
            	HashMap htQuery = res.htRet;      
                /*注:为保证查询后返回该页面的工单类型不变，必须在crm_menu_bs中，将工单页面的参数设为“ntype=”，否则在进入页面时会使用查询的filter字段记录工单类型，查询后记录工单类型的字段会被查询条件覆盖，导致总返回到工单类型为0的列表汇总*/
                pOp = Functions.ht_Get_strValue("cmd", htQuery);
                pType = Functions.atoi(Functions.ht_Get_strValue("casetype", htQuery)); //获取通用form类型 
                pCaseId = Functions.ht_Get_strValue("caseid", htQuery);
            }
            Save_vs("pType", pType);
            Save_vs("pCaseid", pCaseId);
            Save_vs("cmd", pOp);
        }
        else
        {
            pType = Functions.atoi(Load_vs("pType"));
            pCaseId = Load_vs("pCaseid");
            pOp = Load_vs("cmd");
        }
        if (pType != 0)
            pTableName +=String.valueOf(pType);
                    
  	    InitToolbar();
      
       String strFilter = "CASEID = '" + pCaseId + "'";    
        if (pOp.equals("admin"))
        	strFilter="1=1";
     	Fillin_grid(strFilter);

        myToolBar.render(model);
     	mydg.render(model);  	
    }	
  
    private void Fillin_grid(String strFilter)
    {
        int i = 0;
        //dgCust.Width = panelCust.Width;// - 10;
        mydg.SetCaption("工单历史记录");
        mydg.SetTable( pTableName);
        //mydg.SetSelectStr = "SELECT * FROM " + pTableName + " WHERE CASEID = '" + pCaseId + "'";
        mydg.SetConnStr(pmSys.conn_crm);

        if (pOp.toLowerCase().equals("admin")) //modify by gaoww 20111008，解决admin大小写不匹配时，无选择框
            mydg.fill_fld(i++, "选择", "SELECT", 5, 9);
        mydg.fill_fld(i++, "编号", "AUTOID", 0);
        mydg.fill_fld(i++, "呼叫编号", "CALLID", 10);
        mydg.fill_fld(i++, "用户名称", "UNAME", 22);
        mydg.fill_fld(i++, "客户编号", "USERID", 0);
        mydg.fill_fld(i++, "工单编号", "CASEID", 22, 8, "CMDNAME=Link;NULLAS=[null]");
        mydg.fill_fld(i++, "操作日期", "SDATE", 22);
        mydg.fill_fld(i++, "座席工号", "GHID", 10, 1);
        mydg.set_cols_cbo_list("GHID", "SELECT GHID,REAL_NAME FROM CTS_OPIDK", "GHID,REAL_NAME", pmSys.conn_callthink);
        mydg.fill_fld(i++, "详细信息", "DESP", -1);
        mydg.fill_fld(i++, "播放", "", 8, 8, "CMDNAME=DownLoad;");
        if (pOp.toLowerCase().equals("admin")) 
            mydg.fill_fld(i++, "查看工单", "", 8, 8, "CMDNAME=CASE;");
        
        mydg.RowDataFilled = this;
        mydg.fill_header("dgvList", "AUTOID",strFilter );
       // mydg.CellLinkClicked += new CellLinkClickedEventHandler(dataGrid_CellLinkClicked);
    }
 
    public void mydg_RowDataFilled(Object sender, int rows)
    {
        if (rows < 0) return; //表头行，不处理              
        
        String strId = mydg.get_cell(rows, "AUTOID");        
        int  nCol = mydg.get_idx("CASEID");
        String strCaseid = mydg.get_cell(rows, "CASEID");
    	 if(nCol>=0)
        {
        	String strUrl = myString.Format("case_hist_edit.aspx?cmd={0}&casetype={1}&autoid={2}", pOp, pType, strId);
        	String strHtml = myString.Format("<a href='#this' onclick=\"open_view('{0}','{1}','{2}');\">{2}</a>", strUrl, "记录-"+strId,strCaseid);
        	 mydg.set_cell(rows, nCol, strHtml);
        }
        
        nCol = mydg.get_idx("DESP");
        if(nCol>=0)
        {
        	String strCallid = mydg.get_cell(rows, "CALLID");
            String strSdate = mydg.get_cell(rows, "SDATE");
        	if (strCallid.isEmpty()==false)
        	{
                if (Functions.IsDateTime(strSdate) == true)
                    strSdate = Functions.ConvertStrToDateTime(strSdate).ToString("yyyyMMdd");
                else
                    strSdate = DateTime.NowString("yyyyMMdd");
               String strUrl = myString.Format(pmSys.rootURL+"/ut_calllog/frmRecord_edit.aspx?cmd=CALLID&filter=sdate={0};callid={1};", strSdate, strCallid);  //modify by gaoww 20160108 改为相对路径
               String strHtml = myString.Format("<a href='#this' onclick=\"open_view('{0}');\">{1}</a>", strUrl,fun_main.CreateHtml_img("media_vms.gif", ""));
               mydg.set_cell(rows, ++nCol, strHtml);
        	}
        	
        	if (pOp.toLowerCase().equals("admin"))
            {
        		String strUrl =myString.Format("case_edit.aspx?cmd=Edit&casetype={1}&caseid={2}", pOp, pType, strCaseid);           
        		String strHtml = myString.Format("<a href='#this' onclick=\"open_view('{0}');\">{1}</a>", strUrl, fun_main.CreateHtml_img("Contacts.gif", ""));
                mydg.set_cell(rows, ++nCol, strHtml);
            }
        }
    }

    private void InitToolbar()
    { 
        if (pOp.toLowerCase().equals("admin"))
        {
            //myToolBar.fill_fld("查看工单内容", "Modify"); //delete by gaoww 20161227 改在RowDataFilled中实现
            myToolBar.fill_fld("删除", "Delete",0,10);// "确实删除选中的记录吗？");
            myToolBar.fill_fld("工单类型", "Select_Case", 20, 4, "选择工单类型");
            myToolBar.set_list("Select_Case", "SELECT * FROM CRM_CASE_TABLE  ORDER BY CASETYPE", "CASETYPE,CASE_NAME", pmSys.conn_crm);
            
           //if (myToolBar.get_item_cbo("Select_Case").Items.Count > pType)
                myToolBar.set_item_value("Select_Case",String.valueOf(pType));
        }
       
        myToolBar.fill_fld("刷新", "Refresh");            

        myToolBar.fill_toolStrip("plCommand");
        myToolBar.btnItemClick = this;//new btnClickEventHandler(myToolBar_btnItemClick);
    }

    public void myToolBar_btnItemClick(Object sender, String name, String parms, int nparms)
    { 
        if (name.equals("Refresh"))
        {
            if (pOp.toLowerCase() .equals("admin")) //modify by gaoww 20111008，解决admin大小写不匹配时，刷新为空
                mydg.refresh("AUTOID", "order by sdate desc ");
            else
                mydg.refresh("AUTOID","CASEID='" + pCaseId + "'");           
        }
        else if (name.equals("Select_Case"))
        {
        	 String strSelect =myToolBar.get_item_value("Select_Case");
        	 if (strSelect.isEmpty()==false)
        	 {
                pType =Functions.atoi(Functions.Substring(strSelect, "", "("));
                Save_vs("pType", pType);
                if (pType != 0)
                {
                    pTableName = "CRM_CASE_HIST" + String.valueOf(pType);
                }
                else
                    pTableName = "CRM_CASE_HIST";
                if (Functions.isExist_Table(pTableName, pmSys.conn_crm) == true)
                {
                    mydg.SetTable(pTableName);
                    mydg.refresh("autoid", "order by sdate desc ");
                    //fnGetUname();
                }
            }
            else
                return;

        }           
        else if (name.equals("Delete"))
        {
            int nPos = 0;
           
            List<String>  alRet = mydg.GetSelectedKey("AUTOID");
            if (alRet.size()  == 0)
            {
            	 Functions.MsgBox("请先选中要删除的记录！");
                 return;
            }
            else
            {
                int nFind = 0;
                for (String strId: alRet)
                {                    
                    my_odbc pTable = new my_odbc(pmSys.conn_crm);
                    pTable.my_odbc_delete(pTableName, "AUTOID='" + strId + "'");
                    pTable.my_odbc_disconnect();
                    nPos++;
                }
            }
            
           Functions.MsgBox("提示", "删除了" +String.valueOf(nPos) + "条记录！");        
           mydg.refresh("AUTOID", "order by sdate desc ");
       
        }
        else if (name.equals("Modify"))  //delete by gaoww 20161227 改在 RowDataFilled中实现
        {
        	 /*String strCaseTable = "", strCaseid = "";
            int nStatus = 0, nPos = -1;

           for (int rows = 0; rows < dgvList.Rows.Count; rows++)
            {
                if (mydg.isSelected(rows) == false) continue;
                strCaseid = mydg.get_cell(rows, "caseid");
                //检查工单是否存在
                my_odbc pTable = new my_odbc(pmSys.conn_crm);
                int rc = pTable.my_odbc_find("CRM_CASE_TABLE", "CASETYPE='" + pType + "'");
                if (rc <= 0)
                {
                    pTable.my_odbc_disconnect();
                    break;
                }
                pTable.my_odbc_result("TABLE_NAME", out strCaseTable);
                rc = pTable.my_odbc_find(strCaseTable, "caseid='" + strCaseid + "'");
                if (rc <= 0)
                {
                    pTable.my_odbc_disconnect();
                    Functions.MsgBox("该工单记录不存在！");
                    break;
                }
                pTable.my_odbc_result("STATUS", out nStatus);
                pTable.my_odbc_disconnect();

               
                    {
                        my_odbc pTemp = new my_odbc(pmSys.conn_crm);

                        rc = pTemp.my_odbc_find(strCaseTable, "CASEID='" + strCaseid + "'");
                        if (rc > 0)
                        {
                            string strGhid = "", strGhid_close = "", strGhid_current = "";
                            pTemp.my_odbc_result("GHID", out strGhid);
                            pTemp.my_odbc_result("CLOSE_GHID", out strGhid_close);
                            pTemp.my_odbc_result("CURRENTGHID", out strGhid_current);
                            if (pmAgent.uid != strGhid && pmAgent.uid != strGhid_close && pmAgent.uid != strGhid_current)
                            {
                                Functions.MsgBox("对不起，您没有修改此工单的权限！");
                                pTemp.my_odbc_disconnect();
                                break;
                            }
                        }
                        pTemp.my_odbc_disconnect();
                    }
                }
                nPos =1;
                break;
            }
            if (nPos == 1)
            {
                this.Response.Redirect(String.Format("case_edit.aspx?cmd=Edit&casetype={1}&caseid={2}", pOp, pType, strCaseid));           
            }*/
        }
    }
}



