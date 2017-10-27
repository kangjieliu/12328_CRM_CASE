
package com.CallThink.ut_service.admin;

import java.util.HashMap;

import javax.print.DocFlavor.STRING;

import org.springframework.ui.Model;
import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.myColor;
import com.CallThink.base.pmClass.pmSys;
import com.CallThink.base.pmCode.UltraCRM_Page;

import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.pmMap;
import com.ToneThink.ctsTools.Regex.Regex;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.myString;
import com.ToneThink.ctsTools.myUtility.my_Encode;
import com.ToneThink.ctsTools.WebUI.Fld_attr;
import com.ToneThink.ctsTools.WebUI.my_Field;
import com.ToneThink.ctsTools.WebUI.my_ToolStrip;

    public class ghid_permit_edit extends UltraCRM_Page
    {
        my_Field myFld = new my_Field(1);
        my_ToolStrip myToolBar = new my_ToolStrip();
        private String pVid = "";
        private String pTableName = "CRM_VISITOR_PERMIT";
        private String pOp= "";//add by xutt 20151023

        public void Page_Load(Object sender, Model model)
        {
            if (IsPostBack == false)//正被首次加载和访问
            {
                HashMap htQuery;
                pmMap res = fun_main.QuerySplit(Request); htQuery = res.htRet;
                int rc = res.nRet;
                if (rc > 0)
                {
                    pVid = Functions.ht_Get_strValue("vid", htQuery);
                    pOp = Functions.ht_Get_strValue("cmd", htQuery);

                }
                Save_vs("pVid", pVid);
                Save_vs("pOp", pOp);
            }
            else
            {
                pVid = Load_vs("pVid");
                pOp = Load_vs("pOp");
            }

            InitToolbar();
            Fillin_field();
            //modify by xutt 20151023修改如果是新增页面给新增赋值
            if (pOp.equals("AddNew"))
            {
                FillCase_Default();
                myToolBar.set_visible("Delete", false);
            }
            else
            {
                Fill_Case_Default(pVid);//modify by xutt 20151023增加参数传值
            }
          
		myToolBar.render(model);
		myFld.render(model);
	}

       
        private void Fillin_field()
        {
            myFld.SetConnStr(pmSys.conn_crm);
            myFld.SetTable(pTableName);
            myFld.SetLabelAlign("Right");
            myFld.SetMaxLabelLenth(120);
            myFld.SetMaxLabelLenth_col2(100);
            myFld.funName_OnClientClick("myFld_FieldLinkClicked");
            myFld.fill_fld("编号", "VID", 40, 0, false);
            myFld.fill_fld("IPADDR", "IPADDR", 40, 0);
            myFld.fill_fld("TOKEN", "TOKEN", 40, 0);
            myFld.fill_fld("工号", "GHID", 40, 0);
            myFld.fill_fld("姓名", "OP_NAME", 40, 0); 
            myFld.fill_fld("状态", "STATUS", 40, 1, false);
            myFld.set_list("STATUS", "未审核,已审核");
            myFld.fill_fld("备注", "MEMO", 40,0);
            myFld.fill_Panel("gbCase");

            //modify by xutt 修改时设为只读
            if (pOp.equals("Edit"))
            {
                if (myFld.isExist("IPADDR") == true)
                    myFld.set_readonly("IPADDR");
                if (myFld.isExist("STATUS") == true)
                    myFld.set_readonly("STATUS");
                if (myFld.isExist("GHID") == true)
                    myFld.set_readonly("GHID");
                if (myFld.isExist("MEMO") == true)
                    myFld.set_readonly("MEMO");
            }
         
        }
        private void InitToolbar()
        {
            myToolBar.Clear();

            myToolBar.fill_fld(fun_main.Term("LBL_Return"), "Return");
            myToolBar.fill_fld("Separator", "Separator0", 0, 3);
            //add by xutt 20151023 新增 添加和删除按钮
            myToolBar.fill_fld_confirm("删除", "Delete", " 确实要删除所选工单资料吗？");
            myToolBar.fill_fld_confirm("保存", "Save", "确定保存任务资料！");
            myToolBar.fill_fld("审核通过", "ACCEDE");
            myToolBar.fill_fld("审核未通过", "REFUSE");

            myToolBar.btnItemClick = this;// new btnClickEventHandler(myToolBar_btnItemClick);
            myToolBar.fill_toolStrip("plCommand");
        }
        public void myToolBar_btnItemClick(Object sender, String name, String parms, int nparms)
        {
            //返回
            if (name.equals("Return"))
            {
                String strReturn_url = LastPageUrl_additem("history", "1");
                Functions.Redirect(strReturn_url);
            }
           //审核通过
            else if (name.equals("ACCEDE"))
            {
                HashMap htTemp = new HashMap();
                Functions.ht_SaveEx("STATUS", 1,htTemp);
                my_odbc mydb = new my_odbc(pmSys.conn_crm);
                int nRtn = mydb.my_odbc_update(pTableName, htTemp, "VID='" + pVid+"'");
                if (nRtn == 1)
                {
                    myFld.Load(htTemp);
                    Functions.MsgBox("操作成功！");
                }
                else
                {
                    Functions.MsgBox("操作失败！");
                }
            }
            //审核未通过
            else if (name.equals("REFUSE"))
            {
                HashMap htTemp = new HashMap();
                Functions.ht_SaveEx("STATUS", 0,htTemp);
                my_odbc mydb = new my_odbc(pmSys.conn_crm);
                int nRtn = mydb.my_odbc_update(pTableName, htTemp, "VID='" + pVid+"'");
                if (nRtn == 1)
                {
                    myFld.Load(htTemp);
                    Functions.MsgBox("操作成功！");
                }
                else
                {
                    Functions.MsgBox("操作失败！");
                }
            }
            //添加修改 add by xutt 20151023
            else if (name.equals("Save"))
            {
                HashMap htTemp = myFld.Save();
                String strToken = Functions.ht_Get_strValue("TOKEN", htTemp);
                //对当前值进行MD5加密
                if ((Regex.IsMatch(strToken, "[^*]{3}[*]+") == true) || (strToken.equals("")))
                    htTemp.remove("TOKEN");
                else
                {
                    strToken =new my_Encode().md5_encode_16(strToken);
                    Functions.ht_SaveEx("TOKEN", strToken,htTemp);
                }
                my_odbc pTable = new my_odbc(pmSys.conn_crm);
                int rc = 0;
                //
                if (pOp.equals("AddNew"))
                {
                    if (Functions.ht_Get_strValue("IPADDR", htTemp).trim() == "" && strToken.equals("") && Functions.ht_Get_strValue("GHID", htTemp).trim() == "")
                    {
                        Functions.MsgBox("IP地址、验证码、工号不可同时为空值!");
                        return;
                    }
                    
                   String strVid=myFld.get_item_value("VID");
                    rc=pTable.my_odbc_find(myString.Format("SELECT * FROM {0} WHERE VID='{1}'", pTableName,strVid));
                    pTable.my_odbc_disconnect();
					if (rc == 1) {
						rc = pTable.my_odbc_update(pTableName, htTemp, "VID='" + strVid + "'");
						pTable.my_odbc_disconnect();
					} else {
						rc = pTable.my_odbc_addnew(pTableName, htTemp);
						pTable.my_odbc_disconnect();
					}
                    if (rc > 0)
                        Functions.MsgBox("工程师App认证增加成功!");
                    else
                        Functions.MsgBox("工程师App认证增加失败!");
                }
                else if (pOp.equals("Edit"))
                {
                    if (Functions.ht_Get_strValue("IPADDR", htTemp).trim() == "" && Functions.ht_Get_strValue("GHID", htTemp).trim() == "")
                    {
                        Functions.MsgBox("IP地址、验证码、工号不可同时为空值!");
                        return;
                    }

                    rc = pTable.my_odbc_update(pTableName, htTemp, "VID='" + pVid + "'");
                    pTable.my_odbc_disconnect();
                    if (rc > 0)
                        Functions.MsgBox("工程师App认证修改成功!");
                    else
                        Functions.MsgBox("工程师App认证修改失败!");
                }

            }
            //删除 add by xutt 20151023
            else if (name.equals("Delete"))
            {
                my_odbc mydb = new my_odbc(pmSys.conn_crm);
                int nRtn = mydb.my_odbc_delete(pTableName, "VID='" + pVid+"'");
                if (nRtn == 1)
                {
                    Functions.MsgBox("删除成功！");
                    Functions.Redirect("ghid_permit_list.aspx?cmd=view");
                }
                else
                {
                    Functions.MsgBox("删除失败！");
                }
            }
        
        }

        //增加 add by xutt 20151023
        private void FillCase_Default()
        {
            myFld.Load(pTableName, "1<>1", pmSys.conn_crm);

            int nVid;
            my_odbc pTable = new my_odbc(pmSys.conn_crm);
            int rc = pTable.my_odbc_find(myString.Format("SELECT MAX(VID) AS VID FROM {0}", pTableName));
            nVid = pTable.my_odbc_result("VID",0);
            pTable.my_odbc_disconnect();
            myFld.set_item_text("VID",Integer.toString(nVid + 1));
            //状态
            myFld.set_item_value("STATUS","0");

        }
        //填充任务资料内容
        private void Fill_Case_Default(String strCaseid)
        {
            my_odbc mydb = new my_odbc(pmSys.conn_crm);
            String strSql = myString.Format("SELECT * FROM {0} WHERE VID='{1}'", pTableName, strCaseid);
            HashMap ht = new HashMap();
            pmMap res =mydb.my_odbc_find(strSql,true);
            ht = res.htRet;
            int nRtn = res.nRet;
            mydb.my_odbc_disconnect();
            if (nRtn == 1)
            {
                myFld.Load(ht);
                //add by gaoww 20150929 验证规则不起作用，先恢复按颜色显示是否已有内容的方式
                if (Functions.ht_Get_strValue("TOKEN", ht) != "")
                {
                    //myFld.get_item_txt("TOKEN").BackColor = myColor.bg_popup_caller;
                    //myFld.get_item_txt("TOKEN").ToolTip = "";
                    myFld.set_item_text("TOKEN", "");
                    myFld.set_item_attr("TOKEN",Fld_attr.Fld_BackColor, "background-color:"+myColor.bg_popup_caller+";");
                    myFld.set_item_attr("TOKEN",Fld_attr.Fld_toolTip,"");
                }
                //myFld.get_item_txt("TOKEN").Tag = Functions.ht_Get_strValue("TOKEN", ht);
            }
            else 
            {
                FillCase_Default();//如果不存在 新增一条记录 modify by xutt 20151023
            }

        }
    }

