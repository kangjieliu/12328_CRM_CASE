package com.CallThink.ut_case;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.logging.slf4j.Log4jLogger;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.omg.CORBA.INTERNAL;
import org.springframework.ui.Model;

import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.myColor;
import com.CallThink.base.pmClass.pmSys;
import com.CallThink.base.pmCode.UltraCRM_Page;
import com.CallThink.base.support.pmInfo;
import com.CallThink.ut_case.pmModel_case.WFCase_info;
import com.CallThink.ut_case.pmModel_case.case_info;
import com.CallThink.ut_case.pmModel_case.case_set_info;
import com.CallThink.ut_case.pmModel_case.fun_case;
import com.CallThink.ut_customer.pmModel_cust.customer_set_info;
import com.CallThink.ut_customer.pmModel_cust.fun_Cust;
import com.CallThink.ut_form.pmModel_form.fun_Form;
import com.ToneThink.DataTable.DataRow;
import com.ToneThink.DataTable.DataTable;
import com.ToneThink.DateTime.DateTime;
import com.ToneThink.ctsControls.tag.TabControl;
import com.ToneThink.ctsControls.tag.TabPage;
import com.ToneThink.ctsTools.Regex.Regex;
import com.ToneThink.ctsTools.WebUI.my_Field;
import com.ToneThink.ctsTools.WebUI.my_ToolStrip;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.myFile;
import com.ToneThink.ctsTools.myUtility.myString;
import com.ToneThink.ctsTools.myUtility.pmList;
import com.ToneThink.ctsTools.myUtility.pmMap;
import com.ToneThink.ctsTools.myUtility.pmRef;
import com.ToneThink.ctsTools.myUtility.pmRet;
import com.alibaba.druid.sql.ast.statement.SQLIfStatement.Else;

public class case_edit extends UltraCRM_Page {
	private String pOp = "";
	private int pType = 0;
	private String pCaseId = "";
	private String pCaseId_Rel = ""; // 关联工单的工单编号 add by gaoww 20151119
	private int pStatus = 0;
	// private string pId_dlg = "";

	private String m_TableName = "CRM_CASE";
	private HashMap m_htPriv = new HashMap();
	private String pFrom = "";// add by gaoww 20141103
								// 增加页面打开来源，如果为relation时，则不添加子菜单，避免页面嵌套多层问题
	case_set_info myCase;// = new cases_info(pType);
	case_info myCaseInfo = new case_info();

	my_Field myFld = new my_Field(2);// using for firm
										// //备注:工单显示3列会使排版很拥挤，不好看，所以还用2列，gaoww
	my_ToolStrip myToolBar = new my_ToolStrip();

	DataTable dtButton_custom = new DataTable(); // add by gaoww 20151126
													// 增加工单自定义button处理

	private String m_Submit_res; // 提交返回结果

	public void Page_Load(Object sender, Model model) {
		pmAgent = fun_main.GetParm();
		if (IsPostBack == false)// 正被首次加载和访问
		{
			pmMap res = fun_main.QuerySplit(Request);
			int rc = res.nRet;
			if (rc > 0) {
				HashMap htQuery = res.htRet;
				pOp = Functions.ht_Get_strValue("cmd", htQuery);
				pType = Functions.atoi(Functions.ht_Get_strValue("ntype", htQuery));
				pStatus = Functions.atoi(Functions.ht_Get_strValue("status", htQuery));
				pCaseId = Functions.ht_Get_strValue("caseid", htQuery);
				pCaseId_Rel = Functions.ht_Get_strValue("caseid_rel", htQuery);
				pFrom = Functions.ht_Get_strValue("from", htQuery);
			}
			Save_vs("pOp", pOp);
			Save_vs("pType", pType);
			Save_vs("pStatus", pStatus);
			Save_vs("pCaseId", pCaseId);
			Save_vs("pCaseId_Rel", pCaseId_Rel);
			Save_vs("pFrom", pFrom);
		} else {
			pOp = Load_vs("pOp");
			pType = Functions.atoi(Load_vs("pType"));
			pStatus = Functions.atoi(Load_vs("pStatus"));
			pCaseId = Load_vs("pCaseId");
			pCaseId_Rel = Load_vs("pCaseId_Rel");
			pFrom = Load_vs("pFrom");
			dtButton_custom = Load_vs("dtButton_custom", DataTable.class); // add
																			// by
																			// gaoww
																			// 20151126
																			// 增加自定义button功能
		}

		myCase = new case_set_info(pType);
		myFld = new my_Field(myCase.nForm_cols);
		//m_TableName = myCase.TableName;
		m_TableName = "CRM_CASE";
		myCaseInfo = myCase.GetCaseRecord(pCaseId);

		Fillin_Field();
		if (IsPostBack == false)// 首次加载和访问
		{
			if (pOp.indexOf("AddNew_fromCust") >= 0) {
				Fill_Case_withUserid(pCaseId);
			} else if (pOp.indexOf("AddNew") >= 0) {
				Fill_Case_Default();
			} else if (pOp.indexOf("Edit") >= 0) {
				Fill_Case_withCaseid(pCaseId);
			}
			if (pOp.startsWith("AddNew") == true) {
				// pCaseId = myFld.get_item_text("CASEID");
				Save_vs("pCaseId", pCaseId);
			}
			pCaseId = Load_vs("pCaseId");
			// add by gaoww 20151126 增加自定义button功能
			if (Functions.isExist_Table("CRM_CASE_ACTION", pmSys.conn_crm) == true) {
				String strSql = myString.Format(
						"SELECT ACTION_KEY,FLD_ID,FLD_NAME,NOTE,ACTION_CASE FROM CRM_CASE_ACTION WHERE CASETYPE='{0}' ORDER BY FLD_ORDER",
						pType);
				my_odbc pTable = new my_odbc(pmSys.conn_crm);
				pmList mRet = pTable.my_odbc_find(strSql, 0);
				pTable.my_odbc_disconnect();
				dtButton_custom = mRet.dtRet;
				Save_vs("dtButton_custom", dtButton_custom);
			}
		} else
			myFld.Save();

		InitToolbar();
		myToolBar.render(model);
		myFld.render(model);

		if (pFrom.toLowerCase().equals("relation") == false)
			Fill_subForm(1, model);
		else
			Fill_subForm(0, model);
	}

	public String doSubmit() {
		pmMap res = fun_main.QuerySplit(Request);
		String strCmd = Functions.ht_Get_strValue("act", res.htRet);// request.getParameter("act");

		// 获取参数
		pOp = Load_vs("pOp");
		pType = Functions.atoi(Load_vs("pType"));
		pStatus = Functions.atoi(Load_vs("pStatus"));
		pCaseId = Load_vs("pCaseId");
		pCaseId_Rel = Load_vs("pCaseId_Rel");
		pFrom = Load_vs("pFrom");
		dtButton_custom = Load_vs("dtButton_custom", DataTable.class);

		myCase = new case_set_info(pType);
		Fillin_Field();
		myFld.Save();
		pCaseId = myFld.get_item_value("CASEID");
		myCaseInfo = myCase.GetCaseRecord(pCaseId);

		myToolBar_btnItemClick(null, strCmd, "", 0);
		String jnRet = m_Submit_res;// myToolBar_btnItemClick(strCmd);
		return jnRet;
	}

	public void Fillin_Field() {
		String strKey_kvdb = "dtCase_edit_" + pType;
		DataTable dtStru = pmInfo.myKvdb.Get(strKey_kvdb);// as DataTable;
		// if (dtStru == null)
		{
			dtStru = fun_Form.get_desc_data(myCase.DescName, "((FLD_VLEVELS&1)<>0) ORDER BY EDIT_INDEX");
			pmInfo.myKvdb.Setex(strKey_kvdb, dtStru, 60); // 60秒
		}
		if (dtStru.getCount() > 0) {
			// dtStru.PrimaryKey = new DataColumn[] {
			// dtStru.Columns["FLD_VALUE"] }; //add by gaoww 20100203
			// 增加主键，如果不增加，在后面判断是否不显示字段在dtStru时，会异常
			List<String> alRet;
			myCase.get_edit_priv(myCaseInfo, m_htPriv);
			if (m_htPriv.containsKey("FLD_INV") == true) {
				alRet = Arrays.asList(Functions.ht_Get_strValue("FLD_INV", m_htPriv));
				String strFilter = "";
				// 去除 dtStru 中alRet（FLD_VALUE）包含的内容FLD_VALUE
				for (String strFld : alRet) {
					if (dtStru.Rows().contains(strFld) == false)
						continue;
					/*
					 * if (strFilter.Length < 1) //delete by gaoww 20160510
					 * strFilter = String.Format("(FLD_VALUE<>'{0}')", strFld);
					 * else strFilter +=
					 * String.Format(" AND (FLD_VALUE<>'{0}')", strFld);
					 */
					if (strFilter.length() > 0)
						strFilter += ",";// modify by gaoww 20160510
											// 由于datatable.select中不能有<>符号，如果有会报错，导致登出crm，所以改为not
											// in语句
					strFilter += "'" + strFld + "'";
				}

				if (strFilter.length() > 0) {
					strFilter = myString.Format("FLD_VALUE NOT IN ({0})", strFilter);// addby
																						// gaoww
																						// 20160510改为not
																						// in语句
					dtStru = dtStru.select(strFilter);// Functions.dt_GetTable_select(dtStru,strFilter);
				}
			}

			// myFld.SetCaption = "工单资料详细信息";
			myFld.SetConnStr(pmSys.conn_crm);
			myFld.SetTable(m_TableName);
			myFld.SetLabelAlign("Right");
			myFld.SetMaxLabelLenth(120);
			myFld.SetMaxLabelLenth_col2(100);
			myFld.funName_OnClientClick("myFld_FieldLinkClicked");
			pmRet mRet = fun_Form.Fill_Field(dtStru, myFld, 1, 0); // 显示级别：bit
																	// 0在详细资料中显示
																	// bit1在列表中显示，bit2在弹出中显示

			int rc = (int) mRet.nRet;
			myFld = (my_Field) mRet.oRet;
			// if (rc > 0)
			// myFld.FieldLinkClicked += new
			// FieldLinkClickedEventHandler(myFld_FieldLinkClicked);
			myFld.fill_Panel("gbEdit");

			if (myFld.isExist("CASEID") == true)
				myFld.set_readonly("CASEID");
			if (myFld.isExist("USERID") == true)
				myFld.set_readonly("USERID");
			if (myFld.isExist("SDATE") == true)
				myFld.set_readonly("SDATE");
			if (myFld.isExist("STIME") == true)
				myFld.set_readonly("STIME");
			if (myFld.isExist("BDATE") == true)
				myFld.set_readonly("BDATE");
			if (myFld.isExist("CURRENTGHID") == true)
				myFld.set_readonly("CURRENTGHID");

			if (myFld.isExist("CALLER") == true) {
				// myFld.get_item_txt("CALLER").BackColor =
				// myColor.bg_popup_caller;
			}

			if (m_htPriv.containsKey("FLD_RDONLY") == true) {
				alRet = Arrays.asList(Functions.ht_Get_strValue("FLD_RDONLY", m_htPriv));
				for (String strFld : alRet) {
					if (myFld.isExist(strFld) == true)
						myFld.set_readonly(strFld);
				}
			}
		}
	}

	public void Fill_Case_withCaseid(String strCaseId) {
		// int rc = myFld.Load(m_TableName, "CASEID = '" + strCaseId + "'",
		// pmSys.conn_crm);
		// if (rc < 0)
		if (myCaseInfo.isExist == false) {
			Fill_Case_Default();
			return;
		}
		myFld.Load(myCaseInfo.htCase);
		myFld.set_item_value("CURRENTGHID", pmAgent.uid);
		String BUSINESS2 = myFld.get_item_value("BUSINESS2");
		if(StringUtils.isNotBlank(BUSINESS2)){  //add by Liukj 20171026
			my_odbc pOpidk = new my_odbc(pmSys.conn_crm);
			pOpidk.my_odbc_find("CRM_DICT_BUSINESS", "TYPEID='"+BUSINESS2+"'");
			String strKNAME = pOpidk.my_odbc_result("KNAME");
			if(StringUtils.isNotBlank(strKNAME)){
				BUSINESS2 = BUSINESS2 + "("+strKNAME+")";
				myFld.set_item_value("BUSINESS2", BUSINESS2);
			}
			String BUSINESS3 = myFld.get_item_value("BUSINESS3");
			if(StringUtils.isNotBlank(BUSINESS3)){
				pOpidk.my_odbc_find("CRM_DICT_BUSINESS", "TYPEID='"+BUSINESS3+"'");
				strKNAME = pOpidk.my_odbc_result("KNAME");
				if(StringUtils.isNotBlank(strKNAME)){
					BUSINESS3 = BUSINESS3 + "("+strKNAME+")";
					myFld.set_item_value("BUSINESS3", BUSINESS3);
				}
			}
			pOpidk.my_odbc_disconnect();
		}
		
	//	myFld.set_item_value("DEADLINE", DateTime.Now().AddDays(+20).ToString("yyyy-MM-dd"));
		if (myFld.isExist("STATUS") == true)
			pStatus = Functions.atoi(myFld.get_item_value("STATUS"));

		if (myFld.isExist("CLOSE_DAYS") == true) {
			if (myFld.get_item_text("CLOSE_DAYS").length() < 1) {
				DateTime dt_sdate, dt_edate;
				dt_sdate = Functions.ConvertStrToDateTime(myFld.get_item_text("SDATE"), myFld.get_item_text("STIME"));
				dt_edate = Functions.ConvertStrToDateTime(myFld.get_item_text("CLOSE_DATE"));
			}
			
		}
	}

	// 新工单,使用已有的用户资料填充默认值
	private void Fill_Case_withUserid(String strUserId) {
		DataTable dtCust;
		pmRet mRet = fun_Cust.get_user_by_userid(strUserId);
		int rc = (int) mRet.nRet;
		dtCust = (DataTable) mRet.oRet;
		if (rc < 0) {
			Fill_Case_Default();
			return;
		}

		DataTable dtRet = Functions.convert_dt_to_dt(dtCust, m_TableName, pmSys.conn_crm, null);
		if (dtRet.getCount() > 0)
			myFld.Load(dtRet);

		myFld.set_item_value("CASETYPE", String.valueOf(pType));
		myFld.set_item_value("USERID", strUserId);
		myFld.set_item_text("SDATE", DateTime.NowString("yyyyMMdd"));
		myFld.set_item_text("STIME", DateTime.NowString("HHmmss"));
		myFld.set_item_text("EDATE", DateTime.NowString("yyyyMMdd"));
		myFld.set_item_text("CASEID", myCase.GetNewCaseid(""));// fun_CRM.gfnGetCaseid("",pmSys.casetype_default);DateTime.Now.ToString("yyyyMMddHHmmss01");

		myFld.set_item_value("GHID", pmAgent.uid);
		myFld.set_item_value("CURRENTGHID", pmAgent.uid);
		myFld.set_item_value("DEADLINE", DateTime.Now().AddDays(+20).ToString("yyyy-MM-dd"));
		// DEADLINE
	}

	// 新工单,显示默认值
	private void Fill_Case_Default() {
		myFld.Load(m_TableName, "1<>1", pmSys.conn_crm);

		myFld.set_item_value("CASETYPE", String.valueOf(pType));
		myFld.set_item_value("CASENAME",myCase.CaseName);
		myFld.set_item_text("USERID", fun_Cust.GetNewUserid("", pmSys.utype_default));
		myFld.set_item_text("CREATE_TIME", DateTime.NowString("yyyy-MM-dd HH:mm:ss"));
		myFld.set_item_text("SDATE", DateTime.NowString("yyyyMMdd"));
		myFld.set_item_text("STIME", DateTime.NowString("HHmmss"));
		myFld.set_item_text("CLOSE_DATE", DateTime.NowString("yyyyMMdd"));
		// add by xutt
		if (myFld.get_item_value("COUNTY").equals(""))
			myFld.set_item_value("COUNTY", "450301");
		if (myFld.isExist("CASEID") == true) {
			if (pCaseId.length() == 0) {
				pCaseId = myCase.GetNewCaseid(""); 
			}
			 
			myFld.set_item_value("CASEID", pCaseId);
			myFld.set_item_style("CASEID", "background-color:LightGreen");
		}
		myFld.set_item_value("GHID", pmAgent.uid);
		myFld.set_item_value("CURRENTGHID", pmAgent.uid);

		if ((myFld.isExist("PROV") == true) || (myFld.isExist("CITY") == true)) {
			String strTemp = fun_Cust.get_prov_by_caller(myFld.get_item_text("TEL"));
			myFld.set_item_value("PROV", Functions.Substring(strTemp, "PROV_NAME", ";"));// (1,
																							// "PROV_NAME",
																							// strTemp).ToString());
			myFld.set_item_value("CITY", Functions.Substring(strTemp, "CITY_NAME", ";"));// 1,
																							// "CITY_NAME",
																							// strTemp).ToString());
		}
		//20171016
		if(myFld.isExist("ZB_CASEID")==true)
		{
			String strZB_CASE=myCase.GetNewZB_CASE("", myFld.get_item_value("COUNTY"));
			myFld.set_item_value("ZB_CASEID", strZB_CASE);
			myFld.set_item_style("ZB_CASEID", "background-color:LightGreen");
		}
		myFld.set_item_value("DEADLINE", DateTime.Now().AddDays(+20).ToString("yyyy-MM-dd"));
		
	}

	private void InitToolbar() {
		myToolBar.Clear(); // modify by gaoww 20140523

		int nAccept_must = 0; // 必须先签收，其它功能才可用
		int nLevel = myCase.get_authority(myCaseInfo.nProcess, myCaseInfo.nStatus);

		// 增加判断，子菜单时，不显示返回按钮，因为使用的是弹出框方式
		if (((LastPageUrl().isEmpty() == false) && (LastPageUrl().contains("web_desk/desktop_im.aspx") == false)
				&& (pFrom.equals("relation") == false)))// //modify by
														// gaoww20161116增加判断，子菜单时，不显示返回按钮，因为使用的是弹出框方式
			myToolBar.fill_fld(fun_main.Term("LBL_Return"), "Return", 0, 10);
		 
		// 工单是否存在 false-不存在 true-存在
		boolean nExist = myCaseInfo.isExist;
		// if (IsPostBack == true)
		nExist = myCase.isExist_Case(pCaseId);

		if (nExist == false) // 新建工单
		{
			myToolBar.fill_fld("新建并保存", "Save", 0, 10);
		} else {
			// del by xutt 20170922 myToolBar.fill_fld("复制", "Copy", 0, 10);
			myToolBar.fill_fld(fun_main.Term("LBL_SAVE"), "Save", 0, 10);
			//myToolBar.fill_fld(fun_main.Term("LBL_DELETE"), "Delete", 0, 10);
			myToolBar.fill_fld("Separator", "Separator0", 0, 3);

			// 增加自定义button显示 add by gaoww 20151126
			for (int rows = 0; rows < dtButton_custom.getCount(); rows++) {
				String strFld_id = Functions.dtCols_strValue(dtButton_custom, rows, "ACTION_KEY");
				String strFld_name = Functions.dtCols_strValue(dtButton_custom, rows, "FLD_NAME");
				myToolBar.fill_fld(strFld_name, "CASE_CUSTOM_" + strFld_id);
			}
			if (dtButton_custom.getCount() > 0)
				myToolBar.fill_fld("Separator", "Separator1", 0, 3);
			/*
			 * del by xutt 20170922 myToolBar.fill_fld("选择转发类型", "Select_utype",
			 * 25, 4); myToolBar.set_list("Select_utype", "转发工单至座席");
			 * myToolBar.set_list("Select_utype", "转发工单至短信");
			 * myToolBar.set_list("Select_utype", "转发工单至EMAIL");
			 * myToolBar.fill_fld("转发工单", "TranCase", 0, 10);
			 * //myToolBar.fill_fld(fun_main.Term("LBL_DOWNLOAD_REC"),
			 * "Download_Rec"); //delete by gaoww 20101015 bs先暂时封上此功能
			 * myToolBar.fill_fld("输出工单(Word)", "Output_word", 0, 10); //
			 * myToolBar.fill_fld("输出并打印工单(Word)", "Output_word_Print");
			 * myToolBar.fill_fld("Separator", "Separator2", 0, 3); if ((nLevel
			 * & 32) > 0) //有工作流权限 { //bool bRet =
			 * myCase.get_authority(myCaseInfo, "wf"); //if (bRet == true) if
			 * ((myCaseInfo.Get("CURRENTGHID").equals(pmAgent.uid)) ||
			 * ((myCaseInfo.nProcess_status == 4) &&
			 * (myCaseInfo.Get("CURRENTGHID") .length()==0)) ||
			 * ((myCaseInfo.nProcess == 0) && (myCaseInfo.nProcess_status == 0)
			 * && (myCaseInfo.Get("CURRENTGHID") .length()==0)))
			 * myToolBar.fill_fld("工单流转", "WorkFlow",
			 * "return Set_WorkFlow('&ntype=" + pType + "&casetable=" +
			 * m_TableName + "&WF=" + myCase.nWF_Enable + "')"); } if
			 * (myCase.nWF_Enable == 1) //modify by gaoww 20130423
			 * 没有该环节工作流权限也应该可以查看进展 myToolBar.fill_fld("查看进展", "WorkFlow_view",
			 * "return View_WorkFlow('&ntype=" + pType + "&casetable=" +
			 * m_TableName + "&WF=" + myCase.nWF_Enable + "')");
			 * 
			 * myToolBar.fill_fld("Separator", "Separator3", 0, 3);
			 * myToolBar.fill_fld("发送公文通知", "SendNote", 0, 10);//add by yanj
			 * 20121108 新公文修改,在客户资料编辑界面增加发送消息功能
			 * 
			 * myToolBar.fill_fld("设定任务提醒", "SetNotify", 0, 10);//add by fengw
			 * 20130722 设定任务提醒
			 */
		}
		myToolBar.fill_toolStrip("plCommand");
		myToolBar.btnItemClick = this;// myToolBar.btnItemClick += new
										// btnClickEventHandler(myToolBar_btnItemClick);

		/*
		 * if ((nLevel & 1) > 0) myToolBar.set_readonly("AddNew", false); else
		 * myToolBar.set_readonly("AddNew", true);
		 */
		if ((nLevel & 2) > 0)
			myToolBar.set_readonly("Save", false);
		else {
			if (nExist == false) // add by gaoww 20140813 此时为新增工单，应按新增权限控制
			{
				if ((nLevel & 1) > 0)
					myToolBar.set_readonly("Save", false);
				else
					myToolBar.set_readonly("Save", true);
			} else
				myToolBar.set_readonly("Save", true);
		}
		if ((nLevel & 4) > 0)
			myToolBar.set_readonly("Delete", false);
		else
			myToolBar.set_readonly("Delete", true);
		if ((nLevel & 16) > 0)
			myToolBar.set_readonly("Output_word", false);
		else
			myToolBar.set_readonly("Output_word", true);
		// add by zhaoj 20130906 增加工单时，设工单流转、发送公文通知、设定任务提醒为只读
		myToolBar.set_readonly("Select_utype", false);
		myToolBar.set_readonly("TranCase", false);
		// 临时封上 工作流功能
		/*
		 * if ((nLevel & 32) > 0) myToolBar.set_readonly("WorkFlow", false);
		 * else myToolBar.set_readonly("WorkFlow", false);
		 */
		myToolBar.set_readonly("SendNote", false);
		myToolBar.set_readonly("SetNotify", false);

		// modify by gaoww 20100712 根据权限控制命令显示
		// ArrayList alRet;
		List<String> alRet;
		if (m_htPriv.containsKey("CMD_INV") == true) {
			 
			alRet = Arrays.asList(Functions.ht_Get_strValue("CMD_INV", m_htPriv));
			for (String strCmd : alRet) {
				// add by gaoww 20151126 增加自定义button显示控制
				DataTable myRow_tool = dtButton_custom.select("ACTION_KEY ='" + strCmd + "'");
				if (myRow_tool.getCount() > 0)
					myToolBar.set_visible("CASE_CUSTOM_" + strCmd, false);
				else
					myToolBar.set_visible(strCmd, false);
			}
		}

		if (m_htPriv.containsKey("CMD_RDONLY") == true) {
			 
			alRet = Arrays.asList(Functions.ht_Get_strValue("CMD_RDONLY", m_htPriv));
			for (String strCmd : alRet) {
				// add by gaoww 20151126 增加自定义button只读控制
				DataTable myRow_tool = dtButton_custom.select("ACTION_KEY ='" + strCmd + "'");
				if (myRow_tool.getCount() > 0)
					myToolBar.set_readonly("CASE_CUSTOM_" + strCmd, true);
				else
					myToolBar.set_readonly(strCmd, true);
			}
		}

		// add by gaoww 20140804 如果当前所属人工号是当前座席时，保存按钮不受权限控制，改为可用
		String strGhid_creat = Functions.ht_Get_strValue("CURRENTGHID", myCaseInfo.htCase);
		if (strGhid_creat.equals(pmAgent.uid)) {
			myToolBar.set_readonly("Save", false);
		}
		if (nAccept_must == 1) {
			// myToolBar.set_visible("AddNew", false);
			myToolBar.set_visible("Save", false);
			myToolBar.set_visible("Delete", false);
			myToolBar.set_visible("Select_utype", false);
			myToolBar.set_visible("Output_word", false);
			myToolBar.set_visible("TranCase", false);
			// myToolBar.set_visible("SendNote", false);
			myToolBar.set_visible("WorkFlow", false);
			// myToolBar.set_visible("WorkFlow_view", false);
		}
		// myToolBar.fill_toolStrip(strId);
		// myToolBar.btnItemClick += new
		// btnClickEventHandler(myToolBar_btnItemClick);
	}

	public void myToolBar_btnItemClick(Object sender, String name, String parms, int nparms) {
		String strResult = "FAIL";
		String strData = "";

		int rc;
		String strCaseid = pCaseId;
		if (myFld.isExist("CASEID") == true) {
			strCaseid = myFld.get_item_text("CASEID");
		} else {
			if (pCaseId.length() < 1)
				 strCaseid = myCase.GetNewCaseid("");
			
				//strCaseid = myCase.GetNewJJBH("", myFld.get_item_value("COUNTY")); // DateTime.Now.ToString("yyyyMMddHHmmss01");450301

		}
		if (name.equals("Save")) {
			String BUSINESS2 = myFld.get_item_value("BUSINESS2");
			String BUSINESS3 = "";
			if(StringUtils.isNotBlank(BUSINESS2)){            //add by Liukj 20171026
				BUSINESS2 = StringUtils.substringBefore(BUSINESS2,"(");
				BUSINESS3 = myFld.get_item_value("BUSINESS3");
				if(StringUtils.isNotBlank(BUSINESS3)){
					BUSINESS3 = StringUtils.substringBefore(BUSINESS3,"(");
				}
			}
			HashMap htTemp = myFld.Save();
			htTemp.put("BUSINESS2", BUSINESS2);
			htTemp.put("BUSINESS3", BUSINESS3);
			rc = myCase.judge_case_validity(htTemp, "((FLD_VLEVELS&1)<>0) ORDER BY EDIT_INDEX");
			if (rc <= 0) {
				Functions.MsgBox(myCase.FailReason);
				return;
			}

			rc = Update_CaseRecord(strCaseid, htTemp);
			myCaseInfo = myCase.GetCaseRecord(strCaseid);
			myToolBar.Clear();
			InitToolbar();
			if (rc <= 0) {
				strData = "保存工单失败!";
				m_Submit_res = fun_main.getResult(name, strResult, strData);
				return;
			} else
				strData = "保存工单成功!";
		} else if (name.equals("Delete")) {
			int nFind = 0;
			// strUname = myFld.get_item_text("UNAME");
			rc = myCase.DelCaseRecord(strCaseid);
			if (rc == 0) {
				strData = "删除失败!";
				m_Submit_res = fun_main.getResult(name, strResult, strData);
				return;
			}
			String strReturn_url = LastPageUrl_additem("history", "1");
			Functions.Redirect(strReturn_url);
		} else if (name.equals("TranCase")) {
			String strNewUrl, strHtml = "";
			int nType = 0;
			String strTiltle = "";
			strNewUrl = myString.Format("~/ut_case/case_transto_home.aspx?caseid={0}&table={1}&casetype={2}&nType={3}",
					pCaseId, m_TableName, pType, nType);
			if (myCase.nWF_Enable == 1) // add by gaoww 20151126//
										// 如果是使用工作流的工单，转发至座席时显示当前环节可受理工作流的座席
			{
				if (nType == 0)
					strNewUrl = myString.Format("~/ut_case/case_transto_uid.aspx?caseid={0}&table={1}&casetype={2}",
							pCaseId, m_TableName, pType);
				else if (nType == 1)
					strNewUrl = myString.Format("~/ut_case/case_transto_sms.aspx?caseid={0}&table={1}&casetype={2}",
							pCaseId, m_TableName, pType);
				else if (nType == 2)
					strNewUrl = myString.Format("~/ut_case/case_transto_email.aspx?caseid={0}&table={1}&casetype={2}",
							pCaseId, m_TableName, pType);
			}

			strHtml = myString.Format("fun_open('{0}','{1}',900,600);", strNewUrl, strTiltle); // add
																								// by
																								// zhaoj20130902显示为弹出框的形式
			Functions.js_exec(strHtml);
		} else if (name.equals("Download_Rec")) {
			String strCallID = "";
			String strSDate = DateTime.NowString("yyyyMMdd");
			if (myFld.isExist("CALLID") == true)
				strCallID = myFld.get_item_text("CALLID");
			if (myFld.isExist("SDATE") == true)
				strSDate = myFld.get_item_text("SDATE");
			if (strCallID.length() == 0) {
				Functions.MsgBox("呼叫编号为空，不存在录音文件！");
			} else {
				String strTemp = myString.Format(
						"../ut_calllog/frmRecord_edit.aspx?cmd=CALLID&filter=callid={0};sdate={1};", strCallID,
						strSDate);
				Functions.Redirect(strTemp);
			}
		} else if (name.equals("Output_word")) {
			fnOutputWord(0); // modify by gaoww 20100528 增加输出和输出并打印的区分
		} else if (name.equals("Output_word_Print")) // add by gaoww
														// 20100527增加输出并打印功能
		{
			fnOutputWord(1);
		} else if (name.equals("Return")) // 2008.08 增加完成，返回页面后，重新刷新列表页面
		{
			String strReturn_url = LastPageUrl_additem("history", "1");
			Functions.Redirect(strReturn_url);
		}
		// add by yanj 20121108 新公文修改,在工单资料编辑界面增加发送消息功能
		else if (name.equals("SendNote")) {
			String strNoteInfo = "CASEID=" + pCaseId + ";PTYPE=" + pType + ";";
			// this.Response.Redirect("~/ut_personality/ut_priv_note/note_msg_send.aspx?cmd=send&nType=2&nTaskType=2&taskinfo="
			// + strNoteInfo);
			String strNewUrl = "~/ut_personality/ut_priv_note/note_msg_send.aspx?cmd=send&ntype=3&nTaskType=2&taskinfo="
					+ strNoteInfo;
			Functions.js_exec("fun_open('" + strNewUrl + "','发送公文通知',1000,600);");
		}
		// add by fengw 20130722 设定任务提醒
		else if (name.equals("SetNotify")) {
			String strNotifyInfo = "CASE;CASEID=" + pCaseId + ";TYPE=" + pType + ";";
			String strNewUrl = "~/ut_personality/Task_notice_edit.aspx?cmd=notify&taskinfo=" + strNotifyInfo;
			// Functions.js_exec("win_open('" + strNewUrl + "','设定任务提醒');");
			Functions.js_exec("fun_open('" + strNewUrl + "','设定任务提醒',1000,400);");
		} else if (name.startsWith("CASE_CUSTOM_") == true) // 处理自定义按钮功能 addbygaoww 20151126
		{
			String strAction_key = Functions.Substring(name, "CASE_CUSTOM_", "");
			DataTable myRow_button = dtButton_custom.select("ACTION_KEY=" + strAction_key);
			if (myRow_button.getCount() > 0) {
				String strFld_name = Functions.dtCols_strValue(myRow_button, "FLD_NAME");
				String strAction_case = Functions.dtCols_strValue(myRow_button, "ACTION_CASE"); // 修改工单
				String strWF_cmd = ""; // 工作流命令
				HashMap htCase = myFld.Save();
				if (strAction_case.length() > 0) // 修改工单状态
				{
					strAction_case = strAction_case.toUpperCase();

					if (Regex.IsMatch(strAction_case, "^[\\d]{1,3}$") == true) // 数字modifyby
																				// fengw
																				// 20151026[\d]{1,3}
																				// ->
																				// //
																				// ^[\d]{1,3}$
					{
						Functions.ht_SaveEx("STATUS", strAction_case, htCase);
					} else if (Regex.IsMatch(strAction_case, "WF") == true) {
						strWF_cmd = Functions.Substring(strAction_case, "WF-", "");
					} else {
						if (Regex.IsMatch(strAction_case, "[\\W_]+=([\\d]{1,3})|[DATETIME]|[DATE][TIME]") == true) {
							if (strAction_case.indexOf("[DATETIME]") > 0)
								strAction_case = strAction_case.replace("[DATETIME]",
										"'" + DateTime.NowString("yyyy-MM-dd HH:mm:ss") + "'");
							if (strAction_case.indexOf("[DATE]") > 0)
								strAction_case = strAction_case.replace("[DATE]",
										"'" + DateTime.NowString("yyyy-MM-dd") + "'");
							if (strAction_case.indexOf("[TIME]") > 0)
								strAction_case = strAction_case.replace("[TIME]",
										"'" + DateTime.NowString("HH:mm:ss") + "'");
						}
						if (Regex.IsMatch(strAction_case, "UPDATE ") == true) {
							strAction_case = Functions.Substring(strAction_case, "SET", "");
						}
						String[] strFild_update = strAction_case.split(",");
						for (int index = 0; index < strFild_update.length; index++) {
							String strFld = Functions.Substring(strFild_update[index], "", "=").trim();
							String strValue = Functions.Substring(strFild_update[index], "=", "").trim();
							Functions.ht_SaveEx(strFld, strValue, htCase);
						}
					}
					if (htCase.isEmpty() == false) {
						my_odbc pCase = new my_odbc(pmSys.conn_crm);
						pCase.my_odbc_update(m_TableName, htCase, "CASEID='" + pCaseId + "'");
						pCase.my_odbc_disconnect();
					}
				}

				// add by xutt 20170922 查看知识库 12328项目定制
				if (strFld_name.equals("查看知识库")) {
					my_odbc pCase = new my_odbc(pmSys.conn_crm);
					String strURL = "";
					rc = pCase.my_odbc_find("SELECT KNOWLEDGE_URL FROM CRM_SEL_KNOWLEDGE");
					if (rc > 0) {
						strURL = pCase.my_odbc_result("KNOWLEDGE_URL");
					}
					pCase.my_odbc_disconnect();
					String strNewUrl = myString.Format("{0}", strURL);
					Functions.js_exec("Add_MainTab('知识库','" + strNewUrl + "')");
				} else if (strFld_name.equals("转办")) {// 转办
					String strNewUrl = "ut_case/select_online_users.aspx?cmd=Transfer&ntype=" + pType + "&caseid="
							+ myFld.get_item_value("CASEID");
					Functions.js_exec("fun_open('" + strNewUrl + "','选择在线班长',1000,600);");
				}				 
				else {
					// add by xutt 20170926其他按钮增加轨迹操作
					String strTrace_Info = myString.Format("TABLE={0};CASEID;", m_TableName,
							myFld.get_item_value("CASEID"));
					String strDesp = pmAgent.uid;
					String strSubject = "";
					if (strFld_name.equals("继续受理")) {
						strDesp = pmAgent.uid + "继续受理";
						strSubject = "继续受理";
					} else if (strFld_name.equals("直接办结")) {
						strDesp = pmAgent.uid + "直接办结";
						strSubject = "直接办结";
						Functions.ht_SaveEx("CLOSE_TIME", DateTime.Now().ToString("yyyyMMdd HHmmss"), htCase);
						Functions.ht_SaveEx("STATUS", strAction_case, htCase);
						Update_CaseRecord(pCaseId, htCase);
					} else if (strFld_name.equals("不予受理")) {
						strDesp = pmAgent.uid + "不予受理";
						strSubject = "不予受理";
					} else if (strFld_name.equals("退回")) {// add by xutt
																	// 20170928
						strDesp = pmAgent.uid + "退回";
						strSubject = "退回";
					}

					fun_case.addnew_trace_log(pType, strSubject, strDesp, strTrace_Info, pmAgent.uid, pmAgent.name);
				}
				case_info myCaseInfo = myCase.GetCaseRecord(pCaseId);
				myCase.get_edit_priv(myCaseInfo, m_htPriv);
				if (strWF_cmd.length() > 0) // 增加工作流功能处理
				{
					WF_Submit_process(strWF_cmd.toLowerCase());
				}
				// myFld.IsReload = true;
				myFld.Load(m_TableName, "CASEID='" + pCaseId + "'", pmSys.conn_crm);
				InitToolbar();
			}
		}
		m_Submit_res = fun_main.getResult(name, strResult, strData);
	}

	// 保存工单，返回结果，1-成功，0-失败 20170926改为哈希表传值
	private int Update_CaseRecord(String strCaseId, HashMap htTemp) {
		int rc;
		// HashMap htTemp = myFld.Save();
		if (htTemp.isEmpty()) {
			Functions.MsgBox("工单保存失败！");
			return 0;
		}
		// if (myCase.judge_case_validity(htTemp) == false) return -1;

		my_odbc pCase = new my_odbc(pmSys.conn_crm);
		rc = pCase.my_odbc_find(m_TableName, "CASEID = '" + strCaseId + "'");// ,out
		//																		// dsCase);
        //ADD BY XUTT 20171012
		Functions.ht_SaveEx("UPDATE_TIME", DateTime.Now().ToString("yyyy-MM-dd HH:mm:ss"), htTemp);
		if (rc != 1) {
			pCase.my_odbc_disconnect();
			rc = Addnew_CaseRecord(strCaseId, htTemp);
			return rc;
		}
		pCase.my_odbc_disconnect();

		// rc = myFld.Save(out htTemp);
		// if (rc != 1) return;
		if (htTemp.containsKey("PROCESS") == true) // ADD BY GAOWW 20100504
													// 增加判断，如果修改的时候有process和process_status字段，则自动移除，避免工作流已经修改了工单的process和process_status字段后，由于座席工单编辑页面
													// 没有关闭，在进行保存时，会更改这两个字段值，导致后面的签收或提交操作验证这两个字段不正确的问题
			htTemp.remove("PROCESS");
		if (htTemp.containsKey("PROCESS_STATUS") == true)// ADD BY GAOWW
															// 20100504
															// 增加判断，如果修改的时候有process和process_status字段，则自动移除，避免工作流已经修改了工单的process和process_status字段后，由于座席工单编辑页面
															// 没有关闭，在进行保存时，会更改这两个字段值，导致后面的签收或提交操作验证这两个字段不正确的问题
			htTemp.remove("PROCESS_STATUS");
		if (pCaseId_Rel != "") // add by gaoww 20151119 增加关联工单编号保存
			Functions.ht_SaveEx("CASEID_REL", pCaseId_Rel, htTemp);
		rc = myCase.UpdateCaseRecord(strCaseId, htTemp);
		if (rc <= 0)
			return rc;
		Functions.MsgBox("提示", "工单保存成功！");
		return 1;
	}

	// 保存工单，返回结果，1-成功，0-失败 20170926改为哈希表传值
	private int Addnew_CaseRecord(String strCaseId, HashMap htTemp) {
		int rc = 0;

		// HashMap htTemp = myFld.Save();
		if (htTemp.isEmpty()) {
			return rc;
		}
		if (myFld.isExist("CASETYPE") == false)
			htTemp.put("CASETYPE", pType);
		if (myFld.isExist("SDATE") == false)
			htTemp.put("SDATE", DateTime.NowString("yyyyMMdd"));
		if (myFld.isExist("STIME") == false)
			htTemp.put("STIME", DateTime.NowString("HHmmss"));
		if (myFld.isExist("GHID") == false)
			htTemp.put("GHID", pmAgent.uid);
		if (myFld.isExist("CURRENTGHID") == false)
			htTemp.put("CURRENTGHID", pmAgent.uid);

		// UPDATE_TIME CURRENTGHID

		if (pCaseId_Rel != "") // add by gaoww 20151119 增加关联工单编号保存
			Functions.ht_SaveEx("CASEID_REL", pCaseId_Rel, htTemp);
		// 2007.11.21 修改，新建工单的工单状态，为坐席人员选中的工单状态，原来新建工单保存时的工单状态毕为为“0-未处理”
		// myFld.SaveEx("STATUS", pStatus, ref htTemp);

		rc = myCase.AddCaseRecord(strCaseId, htTemp);
		if (rc <= 0)
			return rc;
		// if (pParentForm != null) pParentForm.nRefresh_grid = 1;

		if (myFld.isExist("CASEID") == true)
			myFld.set_item_style("CASEID", "background-color;" + myColor.bg_popup_exist);

		String txtName = myFld.get_item_text("CASENAME");
		Functions.MsgBox("提示", "<" + txtName + ">工单添加成功！");
		// fun_case.addnew_trace_log(pType, "添加", "本次警情退回[" + strGhid_Name + "("
		// + strGhid + ")]", strTrace_Info,pmAgent.uid,pmAgent.name);
		// String strTrace_Info = String.format("TABLE={0};CASEID={1};",
		// m_TableName, myFld.get_item_value("CASEID"));
		// fun_case.addnew_trace_log(pType, "添加", "添加工单",
		// strTrace_Info,pmAgent.uid,pmAgent.name);
		//
		if (m_htPriv.containsKey("CMD_RDONLY") == true) {
			List<String> alRet;
			alRet = Arrays.asList(Functions.ht_Get_strValue("CMD_RDONLY", m_htPriv));
			String strFilter = "";
			// 去除 dtStru 中alRet（FLD_VALUE）包含的内容FLD_VALUE
			for (String strCmd : alRet) {
				myToolBar.set_readonly(strCmd, true);
			}
		}
		return 1;
	}

	/// <summary>
	/// 输出word文档
	/// </summary>
	/// <param name="nType">0-只输出word，1-输出并打印</param>
	private void fnOutputWord(int nType) {
		// 判断传入的casrId是否存在，若不存在则给出提示；
		my_odbc myTable = new my_odbc(pmSys.conn_crm);
		String strSql = myString.Format("SELECT * FROM {0} WHERE CASEID='{1}'", m_TableName, pCaseId);
		int rc = myTable.my_odbc_find(strSql);
		myTable.my_odbc_disconnect();
		if (rc <= 0) {
			Functions.MsgBox("该工单不存在！请先保存");
		} else {
			String strFileName_output = m_TableName + ".doc";
			HashMap htTemp = myFld.Save();
			HashMap htRet = get_case_output(myCase, htTemp, myFld);
			if (htRet.isEmpty() == false) {
				// 不同的工单，使用不同的word模版
				// string strCaseName = pmSys.web_phypath +
				// "\\_APP_DATA\\template\\" + pTableName + ".dot";
				String strCaseName = pmSys.web_phypath + "\\_APP_DATA\\template\\" + m_TableName + ".htm";

				if (myFile.Exists(strCaseName) == false)
					strCaseName = pmSys.web_phypath + "\\_APP_DATA\\template\\crm_case.htm";
				Functions.OutputWordFile_withTemplate(strCaseName, strFileName_output, htRet);
			}
		}
	}

	/// <summary>
	/// 获取 当前工单 输出数据
	/// </summary>
	/// <param name="myCase"></param>
	/// <param name="htSave"></param>
	/// <returns></returns>
	private HashMap get_case_output(case_set_info myCase, HashMap htSave, my_Field myFld) {
		HashMap htTemp = new HashMap();

		DataTable dtDesc;
		DataTable dtCase, dtCust;

		String fld_value, fld_name;
		int fld_vlevel, fld_edit_type;

		//String strTableName = myCase.TableName;
		String strTableName = "CRM_CASE";
		String strCaseId = Functions.ht_Get_strValue("CASEID", htSave);
		String strUserId = Functions.ht_Get_strValue("USERID", htSave);

		// Functions.dt_GetTable(strTableName, "1>1");
		my_odbc pCust = new my_odbc(pmSys.conn_crm);
		pmList mRet = pCust.my_odbc_find("SELECT * FROM " + strTableName + " WHERE CASEID = '" + strCaseId + "'", 0);
		int rc = mRet.nRet;
		dtCase = mRet.dtRet;
		pCust.my_odbc_disconnect();
		if (rc == 1) {
			dtDesc = fun_Form.get_desc_data(myCase.DescName, "((FLD_VLEVELS&1)<>0) ORDER BY EDIT_INDEX");
			if (dtDesc != null) {
				for (int i = 0; i < dtDesc.getCount(); i++) {
					fld_name = Functions.dtCols_strValue(dtDesc, i, "FLD_NAME");
					fld_value = Functions.dtCols_strValue(dtDesc, i, "FLD_VALUE");
					fld_vlevel = Functions.dtCols_nValue(dtDesc, i, "FLD_VLEVELS");

					fld_edit_type = Functions.dtCols_nValue(dtDesc, i, "EDIT_TYPE");
					String fld_type = Functions.dtCols_strValue(dtDesc, i, "FLD_TYPE");
					String fld_cbo_list = "";
					String strValue = "";
					if (fld_edit_type == 1) // ComboBox
					{
						strValue = myFld.get_item_text(fld_value);
					}
					if (fld_edit_type == 3) // check button
					{
						int nValue = -1;
						fld_cbo_list = Functions.dtCols_strValue(dtDesc, i, "FLD_CBO_LIST");
						String[] strCbo_list = fld_cbo_list.split(",");
						if (htSave.containsKey(fld_value) == true)
							nValue = Functions.atoi(Functions.ht_Get_strValue(fld_value, htSave));
						for (int index = 0; index < strCbo_list.length; index++) {
							// if(nValue|i)
							if ((nValue & Functions.atoi(Math.pow(2, index))) == 0) // 未选中
							{
								if (strValue.length() == 0)
									strValue = "□" + strCbo_list[index];
								else
									strValue += "  □" + strCbo_list[index];
							} else // 选中
							{
								if (strValue.length() == 0)
									strValue = "√" + strCbo_list[index];
								else
									strValue += "  √" + strCbo_list[index];
							}
						}
					} else if (fld_edit_type == 2) // radio button
					{
						int nValue = -1;
						fld_cbo_list = Functions.dtCols_strValue(dtDesc, i, "FLD_CBO_LIST");
						String[] strCbo_list = fld_cbo_list.split(",");
						if (htSave.containsKey(fld_value) == true)
							nValue = Functions.atoi(Functions.ht_Get_strValue(fld_value, htSave));
						for (int index = 0; index < strCbo_list.length; index++) {
							// if(nValue|i)
							if ((nValue & Functions.atoi(Math.pow(2, index))) == 0) // 未选中
							{
								if (strValue.length() == 0)
									strValue = "○" + strCbo_list[index];
								else
									strValue += "  ○" + strCbo_list[index];
							} else // 选中
							{
								if (strValue.length() == 0)
									strValue = "⊙" + strCbo_list[index];
								else
									strValue += "  ⊙" + strCbo_list[index];
							}
						}
					}

					if (htSave.containsKey(fld_value) == true)
						if (fld_edit_type != 1 && fld_edit_type != 2 && fld_edit_type != 3)
							htTemp.put("CASE_" + fld_value, Functions.ht_Get_strValue(fld_value, htSave));
						else
							htTemp.put("CASE_" + fld_value, strValue);
					else if ((dtCase != null) && (dtCase.getCount() > 0)
							&& (dtCase.Columns().contains(fld_value) == true)) // 2007.09.17
																				// hanxy
																				// 判断如果字段在描述表中存在，而在数据库表中不存在，则不添加哈希表
						htTemp.put("CASE_" + fld_value, Functions.dtCols_strValue(dtCase, fld_value));
				}
			}
		}
		// DataTable dtCust;
		pmRet mRet_cust = fun_Cust.get_user_by_userid(strUserId);
		rc = (int) mRet_cust.nRet;
		dtCust = (DataTable) mRet_cust.oRet;
		if (rc >= 0) {
			customer_set_info myCust = new customer_set_info(rc);
			dtDesc = fun_Form.get_desc_data(myCust.DescName, "((FLD_VLEVELS&1)<>0) ORDER BY EDIT_INDEX");
			// dsCust.Tables[0].PrimaryKey = new DataColumn[] {dsCust "USERID"};
			// dtCust = dsCust.Tables[0].Clone;
			// dtCust = dsCust.Tables[0]; //modify by gaoww 20100421
			// 使用clone只是结构，无法将值赋给dtcust，导致后面取值时报错
			if (dtDesc != null) {
				for (int i = 0; i < dtDesc.getCount(); i++) {
					fld_name = Functions.dtCols_strValue(dtDesc, i, "FLD_NAME");
					fld_value = Functions.dtCols_strValue(dtDesc, i, "FLD_VALUE");
					fld_vlevel = Functions.dtCols_nValue(dtDesc, i, "FLD_VLEVELS");
					if (dtCust.Columns().contains(fld_value) == true) // 2007.09.17
																		// hanxy
																		// 判断如果字段在描述表中存在，而在数据库表中不存在，则不添加哈希表
						htTemp.put("CUST_" + fld_value, Functions.dtCols_strValue(dtCust, fld_value));
				}
			}
		}
		return htTemp;
	}

	// [region] 工作流相关操作
	// 根据工作流bm_id做对应操作
	private void WF_Submit_process(String strCmd) {
		boolean bRet = myCase.get_authority(myCaseInfo, "wf");
		if (bRet == false) {
			rem("对不起，您无权处理此工单！");
			return;
		}
		int nResult = 0;
		WFCase_info myWFCase = new WFCase_info(myCase, myCaseInfo);

		// 取出第一环节、最后一环节 ID
		int m_nFirst_Id = 0, m_nLast_Id = 0;
		DataTable dtRet;
		my_odbc pProcess = new my_odbc(pmSys.conn_crm);
		pmList res = pProcess.my_odbc_find("CRM_CASE_PROCESS", "CASETYPE='" + pType + "' ORDER BY WF_INDEX ASC", 0);
		dtRet = res.dtRet;
		int rc = res.nRet;
		pProcess.my_odbc_disconnect();
		int nNumCol = dtRet.getCount();
		if (nNumCol > 0) {
			m_nFirst_Id = Functions.dtCols_nValue(dtRet, 0, "PROCESS_ID");
			m_nLast_Id = Functions.dtCols_nValue(dtRet, nNumCol - 1, "PROCESS_ID");
		}
		if (strCmd.equals("submit"))// 提交
		{
			int nAccept_auto = 0;
			pmRef<Integer> out = new pmRef<Integer>(0);
			int nProcess_next = myWFCase.get_process_index(1, out);
			nAccept_auto = out.oRet;
			List<String> alRet;
			myCase.get_edit_priv(myCaseInfo, m_htPriv);
			// 检查是否有不为空的字段
			if (m_htPriv.containsKey("FLD_MUST") == true) {
				alRet = (List<String>) m_htPriv.get("FLD_MUST");
				String strFilter = "";
				// 去除 dtStru 中alRet（FLD_VALUE）包含的内容FLD_VALUE
				String strFilter_fld = "";
				for (String strFld : alRet) {
					if (strFilter.length() < 1)
						strFilter = myString.Format("(LEN({0})<1 OR {0} IS NULL)", strFld); // modify
																							// by
																							// gaoww
																							// 20130403
					else
						strFilter += myString.Format(" OR (LEN({0})<1 OR {0} IS NULL)", strFld); // modify
																									// by
																									// gaoww
																									// 20130403

					if (strFilter_fld.equals(""))
						strFilter_fld = "FLD_VALUE='" + strFld + "'";
					else
						strFilter_fld += " OR FLD_VALUE='" + strFld + "'";
				}
				if (strFilter.length() > 0) {
					HashMap htCase = new HashMap();
					my_odbc pTable = new my_odbc(pmSys.conn_crm);
					String strSql = "SELECT * FROM " + myCase.TableName + " WHERE CASEID ='" + pCaseId + "' AND ("
							+ strFilter + ")";
					pmMap map = pTable.my_odbc_find(strSql, true);
					htCase = map.htRet;
					int rc1 = map.nRet;
					pTable.my_odbc_disconnect();
					if (rc1 == 1) {
						String strFilter_null = "";
						for (String strFld : alRet) {
							if (Functions.ht_Get_strValue(strFld, htCase) == "") {
								if (strFilter_null.equals(""))
									strFilter_null = "FLD_VALUE='" + strFld + "'";
								else
									strFilter_null += " OR FLD_VALUE='" + strFld + "'";
							}
						}
						String strFilter_name = Get_Filter_name(strFilter_null); // modify
																					// by
																					// gaoww
																					// 20150828

						Functions.MsgBox("<" + strFilter_name + ">未填写正确，不能提交，请先正确填写！");
						return;
					}
				}
			}

			int nTemp = 0;
			String strTemp = String.valueOf(myWFCase.get_process_index(1, out));
			nTemp = out.oRet; // 下一环节不应该直接编号+1，应该用工作流序号来获取
			List<String> alUid = fun_case.GetUid_byPriv(strTemp, pType);
			String strGhid_recv = ""; // 接收座席工号，获取下一环节受理人工号，如果有人在线默认为第一个座席，如果没人在线或多人在线则为空
			int nOnline_cnt = 0;
			if (pmInfo.myATClient != null) {
				for (String strItem : alUid) {
					String strGhid = Functions.Substring(strItem, "", "-");
					String strUidInfo = pmInfo.myATClient.ATGetUidInfo(strGhid);
					String strUid_Status = Functions.Substring(strUidInfo, "STATUS", 1);// 获取工号状态
					if (strUid_Status.equals("00") == false) {
						nOnline_cnt++;
						strGhid_recv = strGhid;
					}
				}
			}
			if (nOnline_cnt != 1) // 如果有多个人在线，或无人在线，则接收人为空
				strGhid_recv = "";
			nResult = myWFCase.Submit_process(3, pmAgent.uid, strGhid_recv, 0);
			if (nResult == 1) {
				Functions.MsgBox("提示", "提交工单成功！");
			} else if (nResult == -2)
				rem("提交工单失败，未找到合适的流程节点！");
			else
				rem("提交工单失败！");
		} else if (strCmd.equals("backward"))// 退回
		{
			if (myWFCase.nBack_Count() >= pmSys.WorkFlow_Refuse_Counts) {
				rem("此工单的退回已超过最大次数！");
				return;
			}
			if (myCaseInfo.nProcess == m_nFirst_Id) {
				rem("此环节已是第一环节，无法执行退回操作！");
				return;
			}
			String strGhid = "";
			int nProcess_new = -1; // 编辑页面快捷操作，只能退回到前一环节
			String strSql = myString.Format(
					"SELECT PS_PREV,GHID_PREV FROM CRM_CASE_TRACE WHERE CASEID='{0}' AND CASETYPE={1} AND PROCESS={2} AND PS_STATUS_RECV=1  ORDER BY AUTOID DESC",
					pCaseId, pType, myCaseInfo.nProcess);
			my_odbc pTable = new my_odbc(pmSys.conn_crm);
			rc = pTable.my_odbc_find(strSql);
			if (rc == 1) {
				nProcess_new = pTable.my_odbc_result("PS_PREV", 0);
				strGhid = pTable.my_odbc_result("GHID_PREV");
			}
			pTable.my_odbc_disconnect();
			if (nProcess_new == -1) {
				rem("退回工单失败！");
			}
			nResult = myWFCase.Submit_process(4, pmAgent.uid, strGhid, nProcess_new);
			if (nResult == 1) {
				Functions.MsgBox("提示", "退回工单成功！");
			} else if (nResult == -2)
				rem("退回工单失败，未找到合适的流程节点！");
			else
				rem("退回工单失败！");

		}
	}

	private String Get_Filter_name(String strFilter_fld) {
		String strReturn = "";
		if (strFilter_fld.equals(""))
			return strReturn;
		DataTable dtDesc = fun_Form.get_desc_data(myCase.DescName, strFilter_fld);
		if (dtDesc == null)
			return strReturn;
		for (int rows = 0; rows < dtDesc.getCount(); rows++) {
			String fld_value = Functions.dtCols_strValue(dtDesc, rows, "FLD_NAME");
			if (strReturn.equals(""))
				strReturn = fld_value;
			else
				strReturn += "," + fld_value;
		}
		return strReturn;
	}

	/// <summary>
	/// 检查环节下的任务是否结束 add by gaoww 20151102
	/// </summary>
	/// <returns></returns>
	private boolean Check_task_finished() {
		boolean bResult = false;
		DataTable dtTable = new DataTable();
		String strSql = myString.Format(
				"SELECT STATUS FROM SM_TASK_DISP WHERE CASEID='{0}' AND PROCESS='{1}' AND TASK_LEVEL='0'", pCaseId,
				myCaseInfo.nProcess); // 只查当前环节的主任务，从任务不用管
		my_odbc pTable = new my_odbc(pmSys.conn_crm);
		pmList res = pTable.my_odbc_find(strSql, 0);
		dtTable = res.dtRet;
		int rc = res.nRet;
		pTable.my_odbc_disconnect();
		if (rc >= 0) // modify by gaoww 20151112
		{
			if (dtTable.getCount() == 0) // 没有任务可以直接提交
				bResult = true;
			else {
				DataTable myRow = dtTable.select("STATUS<98"); // 是否有主任务不是取消或完成
				if (myRow.getCount() == 0)
					bResult = true;
			}
		}
		return bResult;
	}
	// [end]

	private TabControl tabBody;

	private void Fill_subForm(int nSubMenu, Model model) {
		if (nSubMenu == 0) {
			Functions.setCookie("hide_more", "1", 20);
			return;
		}
		nSubMenu = myCase.nSubMenu;
		if (nSubMenu == 0) // 不显示子菜单
		{
			Functions.setCookie("hide_more", "1", 20);
			return;
		}

		tabBody = new TabControl("plRelation");
		tabBody.setHeight(String.valueOf(pmAgent.content_height) + "px");
		Fill_Relation(nSubMenu);
		// this.plRelation.Controls.Add(tabBody);
		tabBody.render(model);
	}

	private void Fill_Relation(int nVisible) {
		try {
			String strCaller = "", strUserid = "", strCallid = "", strSdate = "";
			if (myFld.isExist("CALLER") == true)
				strCaller = myFld.get_item_text("CALLER");
			if (myFld.isExist("USERID") == true)
				strUserid = myFld.get_item_text("USERID");
			if (myFld.isExist("CALLID") == true)
				strCallid = myFld.get_item_text("CALLID");
			if (myFld.isExist("SDATE") == true)
				strSdate = myFld.get_item_text("SDATE"); // add by gaoww
															// 20140606
			if (strUserid.isEmpty()) {
				String strTableName;
				my_odbc pCase = new my_odbc(pmSys.conn_crm);
				int rc = pCase.my_odbc_find("SELECT TABLE_NAME FROM CRM_CASE_TABLE WHERE CASETYPE = " + pType);
				if (rc > 0) {
					strTableName = pCase.my_odbc_result("TABLE_NAME");
					rc = pCase.my_odbc_find("SELECT USERID FROM " + strTableName + " WHERE CASEID = '" + pCaseId + "'");
					if (rc > 0)
						strUserid = pCase.my_odbc_result("USERID");
				}
				pCase.my_odbc_disconnect();
			}

			int idx;
			int myCount;
			String[] mySubMenu;// = new string[]
								// {"联系人资料","关联电话表","财务信息","业务记录","电话记录"};
			String[] mySubForm = null;
			DataTable dt = new DataTable();

			String strTemp = myString.Format(
					"SELECT Text,WebForm_url FROM CRM_CASE_SUBMENU WHERE ((VISIBLE&{0})<>0 AND (LEVELS&{1})<>0) ORDER BY DisplayOrder",
					nVisible, pmAgent.c_Levels.getMenu());
			dt = Functions.dt_GetTable(strTemp, "", pmSys.conn_crm);

			myCount = dt.getCount();
			if (myCount > 0) {
				String strUrl;
				mySubMenu = new String[myCount];
				mySubForm = new String[myCount];
				for (idx = 0; idx < myCount; idx++) {
					mySubMenu[idx] = Functions.dtCols_strValue(dt, idx, "Text");
					strUrl = Functions.dtCols_strValue(dt, idx, "WebForm_url");
					if (strUrl.indexOf("~") >= 0)
						strUrl = strUrl.replace("~", pmSys.bsCRM_homepage());
					if (strUrl.indexOf("?") > 0) {
						// 与V310兼容
						strUrl = strUrl.replace("$P1", strUserid);
						strUrl = strUrl.replace("$P2", strCaller);
						strUrl = strUrl.replace("$P3", pCaseId);
						// V320格式
						strUrl = strUrl.replace("$(USERID)", strUserid);
						strUrl = strUrl.replace("$(CALLER)", strCaller);
						strUrl = strUrl.replace("$(CALLID)", strCallid);// add
																		// by
																		// zhaoj
																		// 20130902
						strUrl = strUrl.replace("$(CASEID)", pCaseId);
						// strUrl = strUrl.Replace("$(IVRINFO)",
						// pmAgent.Ivrinfo);
						strUrl = strUrl.replace("$(CASETYPE)", String.valueOf(pType));
						strUrl = strUrl.replace("$(SDATE)", strSdate); // add by
																		// gaoww
																		// 20140606
					}
					mySubForm[idx] = strUrl;// Functions.newForm(this,
											// strDllName, strFormName,
											// strParam);
				}
			} else // 动态菜单不存在，使用默认值
			{
				mySubMenu = new String[] { "用户资料", "业务记录", "电话记录" };
				mySubForm = new String[] { pmSys.bsCRM_homepage() + "/ut_customer/customer_list.aspx", // ?pUserid,
																										// pCaller,
																										// "Relation"),
						pmSys.bsCRM_homepage() + "/ut_case/case_list.aspx", // new
																			// frmCustomer_telno(pUserid,
																			// pCaller,
																			// 1),
						// "~/ut_tools/common_from_list.aspx",//new
						// frmCommon_form_list(0,"USERID='" + pUserid + "'"),
						// "~/ut_tools/common_from_list.aspx",//new
						// frmCase_list_select("USERID='" + pUserid + "'"),
						pmSys.bsCRM_homepage() + "/ut_customer/customer_call_list.aspx"// new
																						// frmPopupTel_list(pUserid,
																						// pCaller)
				};
				myCount = mySubMenu.length;
			}

			// ctsTools.Controls.TabPage[] myPage = new
			// ctsTools.Controls.TabPage[myCount];
			for (idx = 0; idx < mySubMenu.length; idx++) {
				TabPage myPage = new TabPage();
				myPage.Id = String.valueOf(idx);
				myPage.Title = mySubMenu[idx];
				myPage.NavigateUrl = mySubForm[idx];
				myPage.NavigateType = idx;
				if (idx == 0)
					myPage.Selected = true;
				tabBody.setId(String.valueOf(pType));
				tabBody.TabPages().put(idx, myPage);
			}
		} catch (Exception ex) {
		}
	}

	protected void rem(String strMsg) {
		Functions.MsgBox(strMsg);
		// lblRem.Text = strMsg;
	}

}
