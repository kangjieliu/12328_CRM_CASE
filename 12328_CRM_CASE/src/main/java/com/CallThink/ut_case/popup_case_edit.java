package com.CallThink.ut_case;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.Model;
import com.CallThink.base.pmClass.e_Level_base;
import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.myColor;
import com.CallThink.base.pmClass.pmSys;
import com.CallThink.base.pmCode.UltraCRM_Page;
import com.CallThink.base.support.pmInfo;
import com.CallThink.ut_case.pmModel_case.case_info;
import com.CallThink.ut_case.pmModel_case.case_set_info;
import com.CallThink.ut_case.pmModel_case.fun_case;
import com.CallThink.ut_customer.pmModel_cust.fun_Cust;
import com.CallThink.ut_form.pmModel_form.fun_Form;
import com.ToneThink.DataTable.DataTable;
import com.ToneThink.DateTime.DateTime;
import com.ToneThink.DateTime.TimeSpan;
import com.ToneThink.FileUpload.FileUpload;
import com.ToneThink.ctsControls.tag.TabControl;
import com.ToneThink.ctsControls.tag.TabPage;
import com.ToneThink.ctsTools.Regex.Regex;
import com.ToneThink.ctsTools.WebUI.my_Field;
import com.ToneThink.ctsTools.WebUI.my_ToolStrip;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.myString;
import com.ToneThink.ctsTools.myUtility.pmList;
import com.ToneThink.ctsTools.myUtility.pmMap;
import com.ToneThink.ctsTools.myUtility.pmRet;

public class popup_case_edit extends UltraCRM_Page {
	private String pOp = "";
	private int pType = 0;

	private String pCaller = "";
	private String pCallid = "";
	private String pIvrInfo = ""; // ivrinfo，由ivr脚本程序传过来的值

	private String pUserId = "";
	private String pCaseId = "";
	private int pStatus = 0;

	private String pFilter_Query = "";

	private String m_TableName = "CRM_CASE";
	private HashMap m_htPriv = new HashMap();
	private String pFrom = "";// add by gaoww 20141103
								// 增加页面打开来源，如果为relation时，则不添加子菜单，避免页面嵌套多层问题
	private String mZB_CASEID = "";
	case_set_info myCase;// = new cases_info(pType);
	case_info myCaseInfo = new case_info();

	my_Field myFld = new my_Field(2);// using for firm
	my_ToolStrip myToolBar = new my_ToolStrip();

	DataTable dtButton_custom = new DataTable(); // add by gaoww 20151126
													// 增加工单自定义button处理
	private String m_Submit_res; // 提交返回结果

	public void Page_Load(Object sender, Model model) {
		if (IsPostBack == false)// 正被首次加载和访问
		{
			// ?ntype=x&cmd=popup_callin&callid=" + strCallid + "&caller=" +
			// strCaller + "&info=" + strInfo";
			// 由不同(Form)调用，显示指定工单资料,格式：?cmd=xxx&&callid=xxx&caller=xxx&info=xxx";
			// cmd 的含义：
			// "popup_callin" 为来电新建工单资料，callid=呼叫标识,caller=来电号码 info=IVR信息
			// "Edit" 修改工单资料，caseid=工单ID caller,主叫号码 pType工单类型
			// "AddNew_fromCust"，为指定用户新建工单 strCaseId，用户ID,caller,主叫号码 pType工单类型

			// 由frmPopupCase_list调用，显示指定工单，有转发工单功能
			// "addnew0"，新建工单， strCaseId，无意义,caller,主叫号码 pType工单类型
			// "modify" ，修改工单,strCaseId，工单户ID,caller,主叫号码 pType工单类型

			HashMap htQuery;
			pmMap res = fun_main.QuerySplit(Request);
			int rc = res.nRet;
			if (rc > 0) {
				htQuery = res.htRet;
				pOp = Functions.ht_Get_strValue("cmd", htQuery);
				pType = Functions.atoi(Functions.ht_Get_strValue("ntype", htQuery));
				pCallid = Functions.ht_Get_strValue("callid", htQuery);
				pCaseId = Functions.ht_Get_strValue("caseid", htQuery); // add
																		// by
																		// gaoww
																		// 20101014
																		// 解决新建工单，不能带过userid的问题
				pCaller = Functions.ht_Get_strValue("caller", htQuery);
				pIvrInfo = Functions.ht_Get_strValue("info", htQuery);
				pFrom = Functions.ht_Get_strValue("from", htQuery);
			}
			Save_vs("pOp", pOp);
			Save_vs("pType", pType);
			Save_vs("pCallid", pCallid);
			Save_vs("pCaseid", pCaseId);
			Save_vs("pCaller", pCaller);
			Save_vs("pFrom", pFrom);

			// pFilter_Query = "SELECT UNAME,TEL,USERID,PERSON,UTYPE FROM
			// CRM_CUSTOMER WHERE 1>1";
			pFilter_Query = "SELECT USERID,UNAME,TEL FROM CRM_CUSTOMER WHERE 1>1";
			Save_vs("pFilter_Query", pFilter_Query);
			Save_vs("pIvrInfo", pIvrInfo);
		} else {
			pOp = Load_vs("pOp");
			pType = Functions.atoi(Load_vs("pType"));
			pCallid = Load_vs("pCallid");
			pCaller = Load_vs("pCaller");
			pCaseId = Load_vs("pCaseid");
			pFilter_Query = Load_vs("pFilter_Query");
			pIvrInfo = Load_vs("pIvrInfo");
			pFrom = Load_vs("pFrom");

			dtButton_custom = Load_vs("dtButton_custom", DataTable.class); // add
			// by
			// gaoww
			// 20151126
			// 增加自定义button功能
			mZB_CASEID=Load_vs("ZB_CASEID");
		}

		// if (pmAgent.content_width > 850) myFld = new my_Field(3);
		myCase = new case_set_info(pType);
		myFld = new my_Field(myCase.nForm_cols);
//		m_TableName = myCase.TableName;
		m_TableName ="CRM_CASE";
		myCaseInfo = myCase.GetCaseRecord(pCaseId);

		InitToolbar();
		Fillin_Field();
		if (IsPostBack == false)// 首次加载和访问
		{
			if ((pOp.equals("popup_callin")) || (pOp.equals("AddNew")))// 通过来电号码，查找在线用户资料
			{
				DataTable dtRet;
				pmRet mRet = fun_Cust.get_user_by_caller(pCaller);
				int rc = (int) mRet.nRet;
				dtRet = (DataTable) mRet.oRet;
				if (rc < 1) // 用户资料中没有符合条件的记录
				{
					Fill_Case_Default();
				} else {
					pUserId = Functions.dtCols_strValue(dtRet, "USERID");
					Fill_Case_withUserid(pUserId);
					Save_vs("pUserId", pUserId);
					if (myFld.isExist("UNAME") == true) {
						// myFld.set_readonly("UNAME");
						if (myFld.get_item_type("UNAME") == 12)
							myFld.set_item_attr("UNAME", "Fld_visible", "false");
					}
				}
			} else if (pOp.indexOf("AddNew_fromCust") >= 0) {
				// Fill_Case_withUserid(pCaseId); //delete by gaoww 20140725
				// modify by gaoww 20140725
				// 解决已有客户新增工单时客户编号和工单编号混乱导致，初始信息中caseid会变为userid的问题
				Save_vs("pUserId", pCaseId);
				pUserId = pCaseId;
				pCaseId = "";
				Fill_Case_withUserid(pUserId);
				pCaseId = myFld.get_item_value("CASEID");
				Save_vs("pCaseid", pCaseId);
			}

			else if (pOp.equals("Edit")) // 通过Pin码，查找在线用户资料
			{
				// Title = "客户在线服务 - 工单编号<" + pCaseId + ">";
				Fill_Case_withCaseid(pCaseId);
			}
			if (pOp.startsWith("AddNew") == true) {
				pCaseId = myFld.get_item_text("CASEID");
				Save_vs("pCaseid", pCaseId);
			}
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
		} else {
			if ((pOp.equals("popup_callin")) || (pOp.equals("AddNew")))// 通过来电号码，查找在线用户资料
			{
				pUserId = Load_vs("pUserId");
				if (pUserId.isEmpty())
					Fill_Case_Default();
				else
					Fill_Case_withUserid(pUserId);
			} else if (pOp.indexOf("AddNew_fromCust") >= 0) // add by gaoww
															// 20140723
															// 解决切换工单类型过程中，没有按userid进行加载，导致工单编号为空的问题
			{
				pUserId = Load_vs("pUserId");
				Fill_Case_withUserid(pUserId);
			}
		}

		myToolBar.render(model);
		myFld.render(model);

		if (pOp.indexOf("AddNew_fromCust") < 0)
			Fill_Case_ByCallin();

		if (pFrom.toLowerCase().equals("relation") == false)
			Fill_subForm(1, model);
		else
			Fill_subForm(0, model);
	}

	public String doSubmit() {
		pmMap res = fun_main.QuerySplit(Request);
		String strCmd = Functions.ht_Get_strValue("act", res.htRet);// request.getParameter("act");

		pOp = Load_vs("pOp");
		pType = Functions.atoi(Load_vs("pType"));
		pCallid = Load_vs("pCallid");
		pCaller = Load_vs("pCaller");
		pCaseId = Load_vs("pCaseid");
		pFilter_Query = Load_vs("pFilter_Query");
		pIvrInfo = Load_vs("pIvrInfo");
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

	// 根据呼入信息，填充相关字段
	private void Fill_Case_ByCallin() {
		String strTel = pCaller;
		if (strTel.length() < 1)
			if (myFld.isExist("TEL") == true)
				strTel = myFld.get_item_text("TEL");
		if (strTel.length() < 1)
			if (myFld.isExist("MOBILENO") == true)
				strTel = myFld.get_item_text("MOBILENO");
		if (strTel.length() < 1)
			if (myFld.isExist("CALLER") == true)
				strTel = myFld.get_item_text("CALLER");
		if (strTel.length() > 0)
			myToolBar.set_item_text("Tel", strTel);

		if (myFld.isExist("IVRINFO") == true)
			myFld.set_item_value("IVRINFO", pIvrInfo);
		myFld.set_item_text("CREATE_TIME", DateTime.NowString("yyyy-MM-dd HH:mm:ss"));
		// myCaseInfo = myCase.GetCaseRecord(pCaseId);
		// InitToolbar();
	}

	// #region 处理工单
	// 填充工单列表、详细信息框架
	public void Fillin_Field() {

		String strKey_kvdb = "dtCase_popup_edit_" + pType;
		DataTable dtStru = pmInfo.myKvdb.Get(strKey_kvdb);// as DataTable;
		
		if (dtStru == null) {
			dtStru = fun_Form.get_desc_data(myCase.DescName, "(FLD_VLEVELS&4)<>0 ORDER BY EDIT_INDEX");
			pmInfo.myKvdb.Setex(strKey_kvdb, dtStru, 60); // 60秒
		}
		myFld.SetConnStr(pmSys.conn_crm);
		myFld.SetTable(m_TableName);
		myFld.SetLabelAlign("Right");
		myFld.SetMaxLabelLenth(120);
		myFld.SetMaxLabelLenth_col2(100);
		myFld.funName_OnClientClick("myFld_FieldLinkClicked");

		pmRet mRet = fun_Form.Fill_Field(dtStru, myFld, 4, 0); // 显示级别：bit
																// 0在详细资料中显示
																// bit1在列表中显示，bit2在弹出中显示
		int rc = (int) mRet.nRet;
		myFld = (my_Field) mRet.oRet;
//		if (rc > 0)
			myFld.FieldLinkClicked = this;// new
											// FieldLinkClickedEventHandler(myFld_FieldLinkClicked);*/
		
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
			myFld.set_item_value("CALLER", pCaller);
			// myFld.get_item_txt("CALLER").BackColor =
			// myColor.bg_popup_caller;
		}

		List<String> alRet;

		myCase.get_edit_priv(myCaseInfo, m_htPriv);

		if (m_htPriv.containsKey("FLD_INV") == true) {
			alRet = Arrays.asList(Functions.ht_Get_strValue("FLD_INV", m_htPriv));

			// foreach (string strFld in alRet)
			// {
			// if (myFld.isExist(strFld) == true)
			// myFld.get_item_txt(strFld).Visible=false;
			// }
		}
		if (m_htPriv.containsKey("FLD_RDONLY") == true) {
			alRet = Arrays.asList(Functions.ht_Get_strValue("FLD_RDONLY", m_htPriv));
			for (String strFld : alRet) {
				if (myFld.isExist(strFld) == true)
					myFld.set_readonly(strFld);
			}
		}

	}

	public void Fill_Case_withCaseid(String strCaseId) {
		if (myCaseInfo.isExist == false) {
			Fill_Case_Default();
			return;
		} else
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
		
		if (myFld.isExist("STATUS") == true)
			pStatus = Functions.atoi(myFld.get_item_value("STATUS"));

		if (myFld.isExist("CLOSE_DAYS") == true) {
			if (myFld.get_item_text("CLOSE_DAYS") == "") {
				DateTime dt_sdate, dt_edate;
				dt_sdate = Functions.ConvertStrToDateTime(myFld.get_item_text("SDATE"), myFld.get_item_text("STIME"));
				dt_edate = Functions.ConvertStrToDateTime(myFld.get_item_text("CLOSE_DATE"));
				TimeSpan ts_days = DateTime.TimeSpan(dt_sdate, dt_edate);
				myFld.set_item_value("CLOSE_DAYS", ts_days.TotalDays() + "-" + ts_days.TotalHours());
			}
		}
	}

	// 新工单,使用已有的用户资料填充默认值
	private void Fill_Case_withUserid(String strUserId) {
		Fill_Case_Default(); // add by gaoww 20090810
								// 增加先将myfld付一个初始值，在添加已有客户资料，避免有初始值无法自动带出的问题

		DataTable dtCust;
		pmRet mRet = fun_Cust.get_user_by_userid(strUserId);
		int rc = (int) mRet.nRet;
		dtCust = (DataTable) mRet.oRet;
		if (rc < 1) {
			Fill_Case_Default();
			return;
		}

		DataTable dtRet = Functions.convert_dt_to_dt(dtCust, m_TableName, pmSys.conn_crm, null);
		// add by gaoww 20140725 解决工单默认值被dtRet中的空值清除的问题，需要判断，如果某一列内容为空则移除
		if (dtRet.getCount() > 0) {
			for (int index = dtRet.Columns().size() - 1; index >= 0; index--) {
				String fld_value = (String) dtRet.getValue(0, index);
				if (fld_value != null && !fld_value.equals(""))// modify by xutt
																// 20171013
					dtRet.Columns().remove(index);
			}
		}

		if (dtRet.getCount() > 0)
			myFld.Load(dtRet);

		myFld.set_item_value("CASETYPE", String.valueOf(pType));
		myFld.set_item_value("CASENAME", myCase.CaseName);
		myFld.set_item_value("STATUS", String.valueOf(pStatus));
		myFld.set_item_value("USERID", strUserId);
		myFld.set_item_text("SDATE", DateTime.NowString("yyyyMMdd"));
		myFld.set_item_text("STIME", DateTime.NowString("HHmmss"));
		myFld.set_item_text("EDATE", DateTime.NowString("yyyyMMdd"));
		myFld.set_item_text("CREATE_TIME", DateTime.NowString("yyyy-MM-dd HH:mm:ss"));
		// myFld.set_item_text("CASEID", myCase.GetNewCaseid(""));// delete by
		if (myFld.get_item_value("COUNTY").equals(""))
			myFld.set_item_value("COUNTY", "450301");
		// gaoww 20140725
		if (pCaseId.isEmpty()) // add by gaoww 20140725 解决第二次保存时，会新增一条相同工单的问题
		{
			//pCaseId = myCase.GetNewJJBH("", myFld.get_item_value("COUNTY")); // 20170930

			pCaseId = myCase.GetNewCaseid(""); // DateTime.Now.ToString("yyyyMMddHHmmss01");
		}
		myFld.set_item_text("CASEID", pCaseId);

		myFld.set_item_value("GHID", pmAgent.uid);
		myFld.set_item_value("CURRENTGHID", pmAgent.uid);

		myFld.set_item_text("CALLER", pCaller);
		myFld.set_item_text("CALLID", pCallid);
		
		myFld.set_item_text("ZB_CASEID",mZB_CASEID);
		myFld.set_item_value("DEADLINE", DateTime.Now().AddDays(+20).ToString("yyyy-MM-dd"));
		
	}

	private void Fill_Case_Default() {
		myFld.Load(m_TableName, "1<>1", pmSys.conn_crm);

		myFld.set_item_value("CASETYPE", String.valueOf(pType));
		String s = myCase.CaseName;
		myFld.set_item_value("CASENAME", myCase.CaseName);
		myFld.set_item_text("CREATE_TIME", DateTime.NowString("yyyy-MM-dd HH:mm:ss"));
		myFld.set_item_text("USERID", fun_Cust.GetNewUserid("", pmSys.utype_default));
		myFld.set_item_text("SDATE", DateTime.NowString("yyyyMMdd"));
		myFld.set_item_text("STIME", DateTime.NowString("HHmmss"));
		myFld.set_item_text("CLOSE_DATE", DateTime.NowString("yyyyMMdd"));
		// add by xutt
		if (myFld.get_item_value("COUNTY").equals(""))
			myFld.set_item_value("COUNTY", "450301");
		if (myFld.isExist("CASEID") == true) {
			if (pCaseId.length() == 0) {
				pCaseId = myCase.GetNewCaseid("");
				//使pCaseId = myCase.GetNewJJBH("", myFld.get_item_value("COUNTY")); // 20170930
			}

			myFld.set_item_value("CASEID", pCaseId);
			myFld.set_item_style("CASEID", "background-color:LightGreen");
		}
		
		myFld.set_item_value("GHID", pmAgent.uid);
		myFld.set_item_value("CURRENTGHID", pmAgent.uid);

		myFld.set_item_text("CALLER", pCaller);
		myFld.set_item_text("CALLID", pCallid);

		// modify by gaoww 20120607
		if ((myFld.isExist("PROV") == true) || (myFld.isExist("CITY") == true)) {
			String strTemp = fun_Cust.get_prov_by_caller(pCaller);
			myFld.set_item_value("PROV", Functions.Substring(strTemp, "PROV_NAME", ";"));// (1,
																							// "PROV_NAME",
																							// strTemp).ToString());
			myFld.set_item_value("CITY", Functions.Substring(strTemp, "CITY_NAME", ";"));// 1,
																							// "CITY_NAME",
																							// strTemp).ToString());
		}
		// 20171016
		if (myFld.isExist("ZB_CASEID") == true) {
			mZB_CASEID = myCase.GetNewZB_CASE("", myFld.get_item_value("COUNTY"));
			myFld.set_item_value("ZB_CASEID", mZB_CASEID);
			myFld.set_item_style("ZB_CASEID", "background-color:LightGreen");
			Save_vs("ZB_CASEID", mZB_CASEID);
		}
		myFld.set_item_value("DEADLINE", DateTime.Now().AddDays(+20).ToString("yyyy-MM-dd"));
		
		myToolBar.set_visible("SaveAs", true); // 备用户必须新建用户用户时才能建立
	}

	// ntype == 1 ComboBox选择下拉后
	public void myFld_FieldLinkClicked(Object sender, String name, String parms, int nparms, int ntype) {
		if (myFld.isExist("CURRENTGHID") == true)
			myFld.set_item_text("CURRENTGHID", pmAgent.uid);
		if (ntype == 0) {

		} else if (ntype == 1) // 1 ComboBox选择下拉后
		{
			
		} else if ((ntype == 11) || (ntype == 12)) // 11 打开数据字典
		{
			if (parms == null)
				return;
			if (name.equals("CITY")) {
				// if (myFld.get_item_text("PROV") != "")
				// strParm3 = "PROV_NAME='" + myFld.get_item_text("PROV") + "'";
			}
		} else if ((ntype == 16)) // 16 上传图片 //add by gaoww 20151208 增加image控件处理
		{
			// FileUpload fuTemp = myFld.get_item_fileUpload(name);
			FileUpload fuTemp = new FileUpload((HttpServletRequest) sender);
			if (fuTemp.HasFile() == false)
				return;

			String strUrl = fun_main.Upload_imgFile(fuTemp, null, true);
			myFld.set_item_value(name, strUrl);
		}
	}

	private void InitToolbar() {
		int nAccept_must = 0; // 必须先签收，其它功能才可用
		int nLevel = myCase.get_authority(myCaseInfo.nProcess, myCaseInfo.nStatus);
		/*
		 * if ((myCase.nWF_Enable == 1) && ((nLevel & 32) > 0)) { //约定第一环节 为0 if
		 * ((myCaseInfo.nProcess > 0) && (myCaseInfo.nProcess_status == 0)) { if
		 * ((myCaseInfo.Get("CURRENTGHID").equals(pmAgent.uid)) ||
		 * (myCaseInfo.Get("CURRENTGHID").isEmpty())) { myToolBar.fill_fld("签收",
		 * "wf_accept"); myToolBar.fill_fld("拒签", "wf_refuse"); nAccept_must =
		 * 1; } } }
		 */
		String strCaseid = myFld.get_item_value("CASEID");
		boolean nExist = myCaseInfo.isExist;
		if (IsPostBack == true)
			nExist = myCase.isExist_Case(strCaseid);
		myToolBar.Clear();
		if (nExist == false) // 新建工单
		{
			myToolBar.fill_fld("新建并保存", "Save", 0, 10);
			//该项只用作新建工单用
	        myToolBar.fill_fld("工单类型", "Select_utype", 25, 4,"选择工单类型");
	        myToolBar.set_list("Select_utype", "SELECT * FROM CRM_CASE_TABLE", "CASETYPE,CASE_NAME", pmSys.conn_crm);
			/*
			 * del by xutt 20171012 myToolBar.fill_fld("工单类型", "Select_utype",
			 * 25, 4, "选择工单类型"); //add by gaoww 20101021 增加可以选择工单类型的功能
			 * myToolBar.set_list("Select_utype",
			 * "SELECT * FROM CRM_CASE_TABLE", "CASETYPE,CASE_NAME",
			 * pmSys.conn_crm);
			 */ // if (myToolBar.get_item_cbo("Select_utype").Items.Count >
				// pType)
				// myToolBar.get_item_cbo("Select_utype").SelectedIndex = pType;

			// myToolBar.fill_fld("搜索客户", "Query");

			if (LastPageUrl().contains("popup_case_list.aspx") == true)
				myToolBar.fill_fld(fun_main.Term("LBL_Return"), "Return");
			else
				myToolBar.fill_fld("关闭", "Close", 0, 10);
		} else {
			// del by xutt 20171012myToolBar.fill_fld("新建", "AddNew", 0, 10);
			myToolBar.fill_fld("保存", "Save", 0, 10);
			/*
			 * myToolBar.fill_fld("选择转发类型", "Select_utype", 15, 4);
			 * myToolBar.set_list("Select_utype", "转发工单至座席");
			 * myToolBar.set_list("Select_utype", "转发工单至EMAIL");
			 * myToolBar.set_list("Select_utype", "转发工单至短信");
			 * myToolBar.fill_fld(fun_main.Term("LBL_TRANCASE"), "TranCase");
			 * //myToolBar.fill_fld(fun_main.Term("LBL_DOWNLOAD_REC"),
			 * "Download_Rec"); myToolBar.fill_fld("发送公文通知", "SendNote");//add
			 * by yanj 20121108 新公文修改,在客户资料编辑界面增加发送消息功能 if ((nLevel & 32) > 0)
			 * //有工作流权限 { myToolBar.fill_fld("Separator", "Separator2", 0, 3);
			 * myToolBar.fill_fld("工单流转", "WorkFlow",
			 * "return Set_WorkFlow('&ntype=" + pType + "&casetable=" +
			 * m_TableName + "&WF=" + myCase.nWF_Enable + "')"); }
			 */
			myToolBar.fill_fld("关闭", "Close", 0, 10);
			/*
			 * if ((pOp.equals("addnew")) || (pOp.equals("edit")))
			 * myToolBar.fill_fld("关闭", "Close", 0, 10); else { if
			 * (LastPageUrl().endsWith("desktop_im.aspx") == false) //
			 * addbygaoww20120320增加判断，如果LastPageUrl是主页地址，则不显示返回按钮
			 * myToolBar.fill_fld(fun_main.Term("LBL_Return"), "Return"); }
			 */
			// 增加自定义button显示 add by gaoww 20151126
			for (int rows = 0; rows < dtButton_custom.getCount(); rows++) {
				String strFld_id = Functions.dtCols_strValue(dtButton_custom, rows, "ACTION_KEY");
				String strFld_name = Functions.dtCols_strValue(dtButton_custom, rows, "FLD_NAME");
				myToolBar.fill_fld(strFld_name, "CASE_CUSTOM_" + strFld_id);
			}
			if (dtButton_custom.getCount() > 0)
				myToolBar.fill_fld("Separator", "Separator1", 0, 3);
		}
		myToolBar.btnItemClick = this;
		myToolBar.fill_toolStrip("plCommand");
		// myToolBar.btnItemClick += new
		// btnClickEventHandler(myToolBar_btnItemClick);
		// add by yanj 20121108 新公文修改,在客户资料编辑界面增加发送消息功能
		/*
		 * if (pmAgent.c_Levels.check_authority(e_Level_base.note_msg_admin) ==
		 * false) { if
		 * (pmAgent.c_Levels.check_authority(e_Level_base.note_msg_send) ==
		 * false) myToolBar.set_readonly("SendNote", true); }
		 */
		if ((nLevel & 1) > 0)
			myToolBar.set_readonly("AddNew", false);
		else
			myToolBar.set_readonly("AddNew", true);
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

		// if ((nLevel & 4) > 0) myToolBar.set_readonly("Delete", false);
		// else myToolBar.set_readonly("Delete", true);
		// if ((nLevel & 16) > 0) myToolBar.set_readonly("Output_word", false);
		// else myToolBar.set_readonly("Output_word", true);

		// if (IsPostBack == false)//正被首次加载和访问
		// {
		// if ((pOp.IndexOf("addnew") >= 0) || pOp.IndexOf("popup_callin") >= 0)
		// //modify by gaoww20110602 增加判断“popup_callin”，解决新来电新建工单不能生成caseid问题
		// myToolBar.set_readonly("SendNote", true); //add by gaoww 20130719
		// 新增工单时，不可发送公文通知
		// }

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
			myToolBar.set_visible("AddNew", false);
			myToolBar.set_visible("Save", false);
			myToolBar.set_visible("WorkFlow", false);
		}
	}

	public void myToolBar_btnItemClick(Object sender, String name, String parms, int nparms) {
		int rc;
		String strCaseid = pCaseId;
		if (myFld.isExist("CASEID") == true) {
			strCaseid = myFld.get_item_text("CASEID");
		} else {
			if (pCaseId.length() < 1)
				strCaseid = myCase.GetNewCaseid("");
		}

		if (name.equals("Save")) {
			// add by gaoww 20150901 服务系统定制，userid和uname可能会从前台赋值，保存时，需要从前台获取
			String fld_value = "";
			// 临时封上 -获取前台控件值
			/*
			 * if (myFld.isExist("USERID") == true) { fld_value =
			 * Request.Form["USERID"].Trim(); myFld.set_item_value("USERID",
			 * fld_value); } if (myFld.isExist("UNAME") == true) { fld_value =
			 * Request.Form["UNAME"].Trim(); myFld.set_item_value("UNAME",
			 * fld_value); }
			 */
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
			rc = Update_CaseRecord(strCaseid, htTemp);
			if (rc < 0)
				return;
			myToolBar.set_readonly("AddNew", false);
			myToolBar.set_readonly("SendNote", false); // add by gaoww 20130719
			// myToolBar.set_readonly("Delete", false);

			// add b y gaoww 20091015 增加工单控制权限
			// if (pmAgent.c_Levels.check_authority(e_Level_cust.case_transfer)
			// == false)
			// myToolBar.set_readonly("Trans_case", true);
			myToolBar.set_readonly("Cust_edit", false);
			myToolBar.set_readonly("Output_word", false);

			myCaseInfo = myCase.GetCaseRecord(strCaseid);

			// add by gaoww 20140724 解决再次保存时会产生一条新工单的问题
			Save_vs("pCaseid", strCaseid);

			pCaseId = strCaseid;
			// 更新myCaseInfo
			InitToolbar();
		} else if (name.equals("Delete")) {
			if (myCase.DelCaseRecord(strCaseid) == 1)
				Functions.Redirect(myString.Format("case_list.aspx?ntype={0}", pType));
		} else if (name.equals("TranCase")) {
			String strNewUrl, strHtml = "";
			// delete by gaoww 20150514 改为树形结构
			if (myToolBar.get_item_value("Select_utype").equals("0")) // 转发给在线座席
			{
				strNewUrl = myString.Format("~/ut_case/case_transto_uid.aspx?caseid={0}&tbl={1}&casetype={2}", pCaseId,
						m_TableName, pType);
				strHtml = myString.Format("fun_open('{0}','{1}',900,600);", strNewUrl, "转工单至坐席"); // add
																									// by
																									// zhaoj
																									// 20130902
																									// 显示为弹出框的形式
			}
			if (myToolBar.get_item_value("Select_utype").equals("2")) // 转发至Email
			{
				strNewUrl = myString.Format("~/ut_case/case_transto_email.aspx?caseid={0}&tbl={1}&Type={2}", pCaseId,
						m_TableName, pType);
				strHtml = myString.Format("fun_open('{0}','{1}',900,600);", strNewUrl, "转工单至Email");
			}
			if (myToolBar.get_item_value("Select_utype").equals("1")) // 转发至手机短信
			{
				strNewUrl = myString.Format("~/ut_case/case_transto_sms.aspx?caseid={0}&tbl={1}", pCaseId, m_TableName);
				strHtml = myString.Format("fun_open('{0}','{1}',900,600);", strNewUrl, "转工单至短信");

				// pCaseId = ViewState["lastFilter"].ToString();
				// this.Response.Redirect(String.Format("case_transto_sms.aspx?caseid={0}&tbl={1}",
				// pCaseId, m_TableName));
			}
			int nType = Functions.atoi(myToolBar.get_item_value("Select_utype"));// .SelectedIndex;
			String strTiltle = myToolBar.get_item_value("Select_utype");
			strNewUrl = myString.Format("~/ut_case/case_transto_home.aspx?caseid={0}&table={1}&casetype={2}&nType={3}",
					pCaseId, m_TableName, pType, nType);
			// strHtml = String.Format("fun_open('{0}','{1}',900,600);",
			// strNewUrl, strTiltle); //add by zhaoj 20130902 显示为弹出框的形式
			strHtml = myString.Format("fun_open('{0}','{1}',900,600);", strNewUrl, strTiltle); // add
																								// by
																								// zhaoj
																								// 20130902
																								// 显示为弹出框的形式

			Functions.js_exec(strHtml);
		} else if (name.equals("Download_Rec")) {
			String strCallID = "";
			String strSDate = DateTime.NowString("yyyyMMdd");
			if (myFld.isExist("CALLID") == true)
				strCallID = myFld.get_item_text("CALLID");
			if (myFld.isExist("SDATE") == true)
				strSDate = myFld.get_item_text("SDATE");
			if (strCallID.isEmpty()) {
				Functions.MsgBox("呼叫编号为空，不存在录音文件！");
			} else {
				String strTemp = myString.Format(
						"../ut_calllog/frmRecord_edit.aspx?cmd=CALLID&filter=callid={0};sdate={1};", strCallID,
						strSDate);
				// this.Response.Redirect(strTemp);
			}
		} else if ((name.equals("Select_utype"))) // add by gaoww 20101021
		{
			int nTemp = pType;
			String strTeble = "";
			String strTemp = Functions.Substring(parms, "(", ")");
			my_odbc pTable = new my_odbc(pmSys.conn_crm);
			rc = pTable.my_odbc_find("CRM_CASE_TABLE", "CASE_NAME='" + strTemp + "'");
			if (rc > 0) {
				strTeble = pTable.my_odbc_result("TABLE_NAME");
				nTemp = pTable.my_odbc_result("CASETYPE", 0);
			}
			pTable.my_odbc_disconnect();
			if (nTemp != pType) {
				if (Functions.isExist_Table(strTeble, pmSys.conn_crm) == true) {
					HashMap htTemp;
					m_TableName = strTeble;
					pType = nTemp;
					htTemp = myFld.Save();
					myFld.SaveEx("CASETYPE", pType, htTemp);
					myFld.SaveEx("CASENAME", strTemp, htTemp);

					// 重新装载结构
					// myFld = new my_Field(2);
					Save_vs("pType", pType);
					myCase = new case_set_info(pType);
					 myFld.Clear();
					Fillin_Field();
					myFld.set_item_style("CASEID", "background-color:LightGreen");
					myFld.set_item_style("ZB_CASEID", "background-color:LightGreen");
					myFld.Load(htTemp);
					// 临时封上-刷新子菜单内容
					// Fill_subForm(1); //add by gaoww 20140723
					// 解决切换工单类型过程中，子菜单没有根据对应的工单类型进行切换的问题
				} else {
					Functions.MsgBox("工单<" + strTemp + ">还未建立，请核查！");
				}
			}

			if (nparms == 0) {
				myToolBar.set_visible("Add_Cust", false);
				myToolBar.set_visible("Add_Cust_person", true);
			} else {
				myToolBar.set_visible("Add_Cust", true);
				myToolBar.set_visible("Add_Cust_person", false);
			}
		} else if (name.equals("Return")) // 2008.08 增加完成，返回页面后，重新刷新列表页面
		{
			Functions.Redirect(LastPageUrl()); // modify by gaoww 20120214
												// 不能写死返回的页面
		} else if (name.equals("Query")) {
			// divEdit.Visible = true;
		}
		// add by yanj 20121108 新公文修改,在工单资料编辑界面增加发送消息功能
		else if ((name.equals("SendNote"))) {
			String strNoteInfo = "CASEID=" + pCaseId + ";PTYPE=" + pType + ";";
			// this.Response.Redirect("~/ut_personality/ut_priv_note/note_msg_send.aspx?cmd=send&nType=2&nTaskType=2&taskinfo="
			// + strNoteInfo);
			String strNewUrl = "../ut_personality/ut_priv_note/note_msg_send.aspx?cmd=send&nType=3&nTaskType=2&taskinfo="
					+ strNoteInfo;
			Functions.js_exec("fun_open('" + strNewUrl + "','发送公文通知',1000,600);");
		} else if (name.startsWith("CASE_CUSTOM_") == true) // 处理自定义按钮功能
															// addbygaoww
															// 20151126
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

				// myFld.IsReload = true;
				myFld.Load(m_TableName, "CASEID='" + pCaseId + "'", pmSys.conn_crm);
				InitToolbar();
			}
		}
	}

	/*
	 * protected void btnSave_Click(object sender, EventArgs e) { for (int rows
	 * = 0; rows < dgvList.Rows.Count; rows++) { if (mydg.isSelected(rows) ==
	 * true) { pOp = "AddNew_fromCust"; string strUserid = mydg.get_cell(rows,
	 * "USERID"); //pCaseId = strUserid; Save_vs("pOp", pOp); Save_vs("pUserId",
	 * strUserid); pUserId = strUserid;
	 * 
	 * Fill_Case_Default(); //Fill_Case_withUserid(strUserid); DataTable dtCust;
	 * int rc = fun_Cust.get_user_by_userid(strUserid, out dtCust); if
	 * (dtCust.Rows.Count > 0) { for (int cols = 0; cols < dtCust.Columns.Count;
	 * cols++) { string fld_name = dtCust.Columns[cols].ToString(); string
	 * fld_value = Functions.dtCols_strValue(dtCust, fld_name); if ((fld_name !=
	 * "STATUS") && (fld_name != "SDATE") && (fld_name != "STIME") && (fld_name
	 * != "GHID")) { if (myFld.isExist(fld_name) == true)
	 * myFld.set_item_value(fld_name, fld_value); } } } Fill_subForm(1);
	 * divEdit.Visible = false; } } }
	 */

	private int Update_CaseRecord(String strCaseId, HashMap htTemp) {
		int rc;
		int nReturn = -1;
		// HashMap htTemp = myFld.Save();
		if (htTemp.size() == 0) {
			Functions.MsgBox("工单保存失败！");
			return nReturn;
		}
		Functions.ht_SaveEx("CASETYPE", String.valueOf(pType), htTemp); // ADD
																		// BY
																		// GAOWW
																		// 20101021
																		// 解决切换工单类型后，保存工单时，工单类型和选择的不一致的问题
		// ADD BY XUTT 20171012
		Functions.ht_SaveEx("UPDATE_TIME", DateTime.Now().ToString("yyyy-MM-dd HH:mm:ss"), htTemp);

		rc = myCase.judge_case_validity(htTemp, "(FLD_VLEVELS&4)<>0 ORDER BY EDIT_INDEX");
		if (rc <= 0) {
			Functions.MsgBox(myCase.FailReason);
			return nReturn;
		}

		// DataTable dtCase;
		if (myFld.isExist("LASTEXT") == true) {
			// modify by gaoww 20091029 增加判断，根据描述判断写入的是上次服务工号或分机或分组
			// myFld.set_item_value("LASTEXT", pmAgent.extNum);
			// 根据描述表内容而定，如有“分机”，则存放分机号码
			// “工号”，则存放业务员工号
			// “组”，则存放业务组号
			String strDes = myFld.get_item_des("LASTEXT");
			if (strDes.indexOf("分机") > 0)
				myFld.set_item_value("LASTEXT", pmAgent.extNum);
			else if (strDes.indexOf("工号") > 0)
				myFld.set_item_value("LASTEXT", pmAgent.uid);
			else
				myFld.set_item_value("LASTEXT", pmAgent.c_groups.Groups());
		}

		my_odbc pCase = new my_odbc(pmSys.conn_crm);
		pmList mRlist = pCase.my_odbc_find(m_TableName, "CASEID = '" + strCaseId + "'", 0);
		rc = mRlist.nRet;
		if (rc != 1) {
			pCase.my_odbc_disconnect();
			return Addnew_CaseRecord(strCaseId, htTemp);
		}
		pCase.my_odbc_disconnect();

		myCaseInfo = myCase.GetCaseRecord(pCaseId);

		htTemp = myFld.Save();
		if (htTemp.size() == 0)
			return nReturn;

		rc = myCase.UpdateCaseRecord(strCaseId, htTemp);
		if (rc <= 0)
			return nReturn;
		String txtName = myFld.get_item_text("CASENAME");
		if (txtName.isEmpty())
			txtName = strCaseId;
		Functions.MsgBox("提示", "<" + txtName + ">工单保存成功！");

		return nReturn;
	}

	private int Addnew_CaseRecord(String strCaseId, HashMap htTemp) {
		int rc;
		int nReturn = -1;

		if (myFld.isExist("LASTEXT") == true) {
			// modify by gaoww 20091029 改为根据描述填写上次服务内容
			// myFld.set_item_value("LASTEXT", pmAgent.extNum);
			// 根据描述表内容而定，如有“分机”，则存放分机号码
			// “工号”，则存放业务员工号
			// “组”，则存放业务组号
			String strDes = myFld.get_item_des("LASTEXT");
			if (strDes.indexOf("分机") > 0)
				myFld.set_item_value("LASTEXT", pmAgent.extNum);
			else if (strDes.indexOf("工号") > 0)
				myFld.set_item_value("LASTEXT", pmAgent.uid);
			else
				myFld.set_item_value("LASTEXT", pmAgent.c_groups.Groups());
		}
		if (myFld.isExist("CASETYPE") == true)
			// myFld.set_item_cbo_index("CASETYPE", pType);
			myFld.set_item_value("CASETYPE", String.valueOf(pType));

		// HashMap htTemp;
		// htTemp = myFld.Save();
		if (htTemp.size() == 0)
			return nReturn;

		myFld.SaveEx("CASETYPE", pType, htTemp);

		if (myFld.isExist("SDATE") == false)
			htTemp.put("SDATE", DateTime.NowString("yyyyMMdd"));
		if (myFld.isExist("STIME") == false)
			htTemp.put("STIME", DateTime.NowString("HHmmss"));
		if (myFld.isExist("GHID") == false)
			htTemp.put("GHID", pmAgent.uid);
		if (myFld.isExist("CURRENTGHID") == false)
			htTemp.put("CURRENTGHID", pmAgent.uid);
		if (myFld.isExist("CALLER") == false)
			htTemp.put("CALLER", pCaller);
		if (myFld.isExist("STATUS") == true) // add by gaoww 20080311
												// 解决来电弹出新工单时，不管选择什么工单状态，写到数据库中的状态值总是0的问题
			pStatus = Functions.atoi(myFld.get_item_value("STATUS"));
		myFld.SaveEx("STATUS", pStatus, htTemp);

		rc = myCase.AddCaseRecord(strCaseId, htTemp);
		if (rc <= 0)
			return nReturn;

		if (myFld.isExist("CASEID") == true)
			myFld.set_item_style("CASEID", "background-color:" + myColor.bg_popup_exist);

		String txtName = myFld.get_item_text("CASENAME");
		if (txtName.isEmpty())
			txtName = strCaseId;
		Functions.MsgBox("提示", "<" + txtName + ">工单添加成功！");
		// add by gaoww 20090203
		// 保存后重新加载工单内容，为了解决新增工单时，根据来电生成一个新的随即客户id，但是实际添加的客户资料是已有客户时，userid不是该客户id的问题
		myCaseInfo = new case_info(htTemp);
		Fill_Case_withCaseid(strCaseId);
		InitToolbar();
		return 1;
	}

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
						if (strUrl.indexOf("$(FIRMID)") > 0) // modify by gaoww
																// 20170904
																// 增加处理工单所属企业子菜单的企业id
						{
							String strFirmid = "";
							my_odbc pCust = new my_odbc(pmSys.conn_crm);
							int rc = pCust
									.my_odbc_find("SELECT FIRMID FROM CRM_CUSTOMER WHERE USERID = '" + pUserId + "'");
							if (rc > 0) {
								strFirmid = pCust.my_odbc_result("FIRMID");
								strUrl = strUrl.replace("$(FIRMID)", strFirmid);
							}
							pCust.my_odbc_disconnect();
						}
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
}
