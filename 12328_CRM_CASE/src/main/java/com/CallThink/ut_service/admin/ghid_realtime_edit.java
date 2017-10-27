
package com.CallThink.ut_service.admin;

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

import com.ToneThink.DateTime.DateTime;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.pmList;
import com.ToneThink.ctsTools.myUtility.pmMap;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.myString;
import com.ToneThink.ctsTools.WebUI.my_Field;
import com.ToneThink.ctsTools.WebUI.my_ToolStrip;

    public class ghid_realtime_edit extends UltraCRM_Page
    {
        my_Field myFld = new my_Field(2);
        my_ToolStrip myToolBar = new my_ToolStrip();
        private String pGhid = "";
        private String pTableName = "SM_GHID_REALTIME";
        public void Page_Load(Object sender, Model model)
        {
            if (IsPostBack == false)//正被首次加载和访问
            {
                HashMap htQuery;
                pmMap res = fun_main.QuerySplit(Request); htQuery = res.htRet;
                int rc = res.nRet;
                if (rc > 0)
                {
                    pGhid = Functions.ht_Get_strValue("ghid", htQuery);
                }
                Save_vs("pGhid", pGhid);
            }
            else
            {
                pGhid = Load_vs("pGhid");
            }
            Fillin_field();
            Fill_Case_Default();
            InitToolbar();
        
            myToolBar.render(model);
            myFld.render(model);
}
        private void Fillin_field()
        {
            myFld.SetConnStr(pmSys.conn_crm);
            myFld.SetTable(pTableName);
            myFld.SetLabelAlign("Right");
            myFld.SetMaxLabelLenth(120);
            myFld.SetMaxLabelLenth_col2(140);
            myFld.funName_OnClientClick("myFld_FieldLinkClicked");

            myFld.fill_fld("工程师", "GHID", 40, 1, false);
            myFld.set_list("GHID", "SELECT GHID,REAL_NAME FROM CTS_OPIDK", "GHID,REAL_NAME", pmSys.conn_callthink);
            myFld.fill_fld("工程师姓名", "OP_NAME", 0);
            myFld.fill_fld("行政区划", "XZQH", 40, 1);
            myFld.set_list("XZQH", "SELECT FLD_ID,FLD_NAME FROM DICT_XZQH", "FLD_ID,FLD_NAME", pmSys.conn_crm);
            myFld.fill_fld("所属机构代码", "ORG_CODE", 40, 1);
            myFld.set_list("ORG_CODE", "SELECT ORG_CODE,ORG_NAME FROM DICT_ORG_CODE", "ORG_CODE,ORG_NAME", pmSys.conn_crm);
            myFld.fill_fld("签入方式", "SIGN_TYPE", 40, 1);
            myFld.set_list("SIGN_TYPE", new String[] { "0,正常签入、签出", "1,默认视为签入、手动签出" });
            myFld.fill_fld("工作状态", "STATUS", 40, 1);
            myFld.set_list("STATUS", new String[] { "0,未签入", "1,签入", "9,失效" });
            myFld.fill_fld("X坐标", "POS_X", 40);
            myFld.fill_fld("Y坐标", "POS_Y", 40);
            myFld.fill_fld("手机电池电量", "APP_BATY", 40);
            myFld.fill_fld("手机剩余内存", "APP_RAM", 40);
            myFld.fill_fld("手机网络", "APP_WAN", 40);
            myFld.fill_fld("更新时间", "SDATE_UPDATE", 22, 5, true, false, DateTime.Now().ToString("yyyy-MM-dd HH:mm:ss"), "DateTime");
            myFld.fill_fld("已分配的任务/月", "TASK_TOTAL", 40);
            myFld.fill_fld("未完成的任务", "TASK_WORKING", 40);
            myFld.fill_fld("待料的任务", "TASK_WAIT", 40);
            myFld.fill_fld("个人偏好业务范围列表", "BUSS_LIKE", 40);
            myFld.fill_fld("主观能动", "ACTIVE", 40);
            myFld.fill_fld("备注", "MEMO", 200);
            myFld.fill_Panel("gbCase");
        }
        private void InitToolbar()
        {
            myToolBar.Clear();

            myToolBar.fill_fld(fun_main.Term("LBL_Return"), "Return");
            myToolBar.fill_fld("Separator", "Separator0", 0, 3);
            myToolBar.fill_fld_confirm("保存", "Save", "确定保存任务资料！");
            myToolBar.btnItemClick = this;// new btnClickEventHandler(myToolBar_btnItemClick);
            myToolBar.fill_toolStrip("plCommand");
        }
        public void myToolBar_btnItemClick(Object sender, String name, String parms, int nparms)
        {
            if (name.equals("Return"))
            {
                String strReturn_url = LastPageUrl_additem("history", "1");
                Functions.Redirect(strReturn_url);
            }
            else if (name.equals("Save"))
            {
                HashMap htTemp = myFld.Save();
                Functions.ht_SaveEx("OP_NAME", myFld.get_item_text("GHID"),htTemp);
                my_odbc mydb = new my_odbc(pmSys.conn_crm);
                int nRtn = mydb.my_odbc_update(pTableName, htTemp, "GHID='" + pGhid + "'");
                mydb.my_odbc_disconnect();
                if (nRtn == 1)
                {
                    Functions.MsgBox("保存成功！");
                }
                else
                {
                    Functions.MsgBox("保存失败！");
                }

            }
        }
        //填充任务资料内容
        private void Fill_Case_Default()
        {
            my_odbc mydb = new my_odbc(pmSys.conn_crm);
            String strSql = myString.Format("SELECT * FROM {0} WHERE GHID='{1}'", pTableName, pGhid);
            HashMap ht = new HashMap();
            pmMap res =mydb.my_odbc_find(strSql,true); 
            ht = res.htRet;
            int nRtn = res.nRet;
            mydb.my_odbc_disconnect();
            if (nRtn == 1)
            {
                myFld.Load(ht);
            }
            else
            {
                return;
            }
        }
    }

