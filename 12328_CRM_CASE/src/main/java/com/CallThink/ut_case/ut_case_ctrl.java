package com.CallThink.ut_case;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import com.ToneThink
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.CallThink.base.pmClass.e_LogInfo;
import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.pmAgent_info;
import com.CallThink.base.pmClass.pmSys;
import com.CallThink.base.support.pmInfo;
import com.CallThink.controller.BaseController;
import com.CallThink.ut_http.pmModel_api.api_business;
import com.ToneThink.DataTable.DataTable;
import com.ToneThink.DateTime.DateTime;
import com.ToneThink.ctsTools.Regex.Regex;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.fun_json;
import com.ToneThink.ctsTools.myUtility.myString;
import com.ToneThink.ctsTools.myUtility.pmList;
import com.ToneThink.ctsTools.myUtility.pmMap;
import com.ToneThink.ctsTools.myUtility.pmRet;

@Controller
public class ut_case_ctrl extends BaseController {

	// 选择文本模板
	@RequestMapping(value = "ut_case/case_template.aspx")
	public String getTemplate_list(HttpServletRequest request, Model model) {
		case_template myForm = new case_template();
		myForm.Page_Load(this, model);
		return "ut_case/case_template";
	}

	// 选择文本模板修改
	@RequestMapping(value = "ut_case/case_template_edit.aspx")
	public String getTemplate_edit(HttpServletRequest request, Model model) {
		case_template_edit myForm = new case_template_edit();
		myForm.Page_Load(this, model);
		return "ut_case/case_template_edit";
	}

	@RequestMapping(value = "ut_case/case_list.aspx")
	public String getCase_list(HttpServletRequest request, Model model) {
		case_list myForm = new case_list();
		myForm.Page_Load(this, model);
		return "ut_case/case_list";
	}
	@RequestMapping(value = "ut_case/case_list_all.aspx")
	public String getCase_list_all(HttpServletRequest request, Model model) {
		case_list myForm = new case_list();
		myForm.Page_Load(this, model);
		return "ut_case/case_list";
	}

	@RequestMapping(value = "ut_case/case_edit.aspx")
	public String getCase_edit(HttpServletRequest request, Model model) {
		case_edit myForm = new case_edit();
		myForm.Page_Load(this, model);
		return "ut_case/case_edit";
	}

	@RequestMapping(value = "ut_case/wf_trace_view.aspx")
	public String wf_trace_view(HttpServletRequest request, Model model) {
		wf_trace_view myForm = new wf_trace_view();
		myForm.Page_Load(this, model);
		return "ut_case/wf_trace_view";
	}

	@RequestMapping(value = "ut_case/wf_process_set.aspx")
	public String wf_process_set(HttpServletRequest request, Model model) {
		wf_process_set myForm = new wf_process_set();
		myForm.Page_Load(this, model);
		return "ut_case/wf_process_set";
	}

	@RequestMapping(value = "ut_case/wf_status_set.aspx")
	public String wf_status_set(HttpServletRequest request, Model model) {
		wf_status_set myForm = new wf_status_set();
		myForm.Page_Load(this, model);
		return "ut_case/wf_status_set";
	}

	@ResponseBody
	@RequestMapping(value = "ut_case/case_edit.aspx/submit", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
	public String getCase_edit_submit(HttpServletRequest request, Model model) {
		case_edit myForm = new case_edit();
		String strRet = myForm.doSubmit();
		return strRet;
	}

	@RequestMapping(value = "ut_case/popup_case_list.aspx")
	public String getPopup_case_list(HttpServletRequest request, Model model) {
		popup_case_list myForm = new popup_case_list();
		myForm.Page_Load(this, model);
		return "ut_case/popup_case_list";
	}

	@RequestMapping(value = "ut_case/popup_case_edit.aspx")
	public String getPopup_case_edit(HttpServletRequest request, Model model) {
		popup_case_edit myForm = new popup_case_edit();
		myForm.Page_Load(this, model);
		return "ut_case/popup_case_edit";
	}

	@ResponseBody
	@RequestMapping(value = "ut_case/popup_case_edit.aspx/submit", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
	public String getPopup_case_edit_submit(HttpServletRequest request, Model model) {
		popup_case_edit myForm = new popup_case_edit();
		String strRet = myForm.doSubmit();
		return strRet;
	}

	@RequestMapping(value = "ut_case/case_attach.aspx")
	public String getCase_attach(HttpServletRequest request, Model model) {
		case_attach myForm = new case_attach();
		myForm.Page_Load(this, model);
		return "ut_case/case_attach";
	}

	@ResponseBody
	@RequestMapping(value = "ut_case/case_attach.aspx/upload", method = RequestMethod.POST ,produces="application/json;charset=UTF-8")
	public String getCase_attach_upload(HttpServletRequest request, String sender, int sender_type) {
		case_attach myForm = new case_attach();
		String strRet = myForm.doUpload(request, sender, sender_type);
		return strRet;
	}

	@ResponseBody
	@RequestMapping(value = "ut_case/case_attach.aspx/download", method = RequestMethod.POST,produces="application/json;charset=UTF-8")
	public String getCase_attach_download(HttpServletRequest request, String sender, int sender_type) {
		case_attach myForm = new case_attach();
		String strRet = myForm.doUpload(request, sender, sender_type);
		return strRet;
	}

	@RequestMapping(value = "ut_case/case_hist_list.aspx")
	public String getCase_hist_list(HttpServletRequest request, Model model) {
		case_hist_list myForm = new case_hist_list();
		myForm.Page_Load(this, model);
		return "ut_case/case_hist_list";
	}

	@RequestMapping(value = "ut_case/case_hist_edit.aspx")
	public String getCase_hist_edit(HttpServletRequest request, Model model) {
		case_hist_edit myForm = new case_hist_edit();
		myForm.Page_Load(this, model);
		return "ut_case/case_hist_edit";
	}

	@RequestMapping(value = "ut_case/case_trace_log.aspx")
	public String getCase_trace_log(HttpServletRequest request, Model model) {
		case_trace_log myForm = new case_trace_log();
		myForm.Page_Load(this, model);
		return "ut_case/case_trace_log";
	}

	@RequestMapping(value = "ut_case/case_list_select.aspx")
	public String getCase_list_select(HttpServletRequest request, Model model) {
		case_list_select myForm = new case_list_select();
		myForm.Page_Load(this, model);
		return "ut_case/case_list_select";
	}

	// 转发工单-主页
	@RequestMapping(value = "ut_case/case_transto_home.aspx")
	public String case_transto_home() {
		return "ut_case/case_transto_home";
	}

	// 转发工单-左侧树
	@RequestMapping(value = "ut_case/case_transto_left_tree.aspx")
	public String case_transto_left_tree(String strNodeId, Model model) {
		return "ut_case/case_transto_left_tree";
	}

	// 转发工单-打印树形结构
	@RequestMapping(value = "ut_case/case_transto_left_tree_print.aspx")
	public void getCase_transto_left_tree_Data(Integer kType, String strNodeId, PrintWriter out) {
		String strHtml = "";
		String strHolder = "tvList";
		case_transto_left_tree myForm = new case_transto_left_tree(0);
		if (myString.IsEmpty(strNodeId))
			strHtml = myForm.getTreeView_root(strHolder);
		else
			strHtml = myForm.getTreeView_child(strNodeId);
		out.append(strHtml);
	}

	// 转发工单-座席
	@RequestMapping(value = "ut_case/case_transto_uid.aspx")
	public String case_transto_uid(HttpServletRequest request, Model model) {
		case_transto_uid myForm = new case_transto_uid();
		myForm.Page_Load(this, model);
		return "ut_case/case_transto_uid";
	}

	// 转发工单-短信
	@RequestMapping(value = "ut_case/case_transto_sms.aspx")
	public String case_transto_sms(HttpServletRequest request, Model model) {
		case_transto_sms myForm = new case_transto_sms();
		myForm.Page_Load(this, model);
		return "ut_case/case_transto_sms";
	}

	// 转发工单-Email
	@RequestMapping(value = "ut_case/case_transto_email.aspx")
	public String case_transto_email(HttpServletRequest request, Model model) {
		case_transto_email myForm = new case_transto_email();
		myForm.Page_Load(this, model);
		return "ut_case/case_transto_email";
	}

	// 选择在线的值班长
	@RequestMapping(value = "ut_case/select_online_users.aspx")
	public String select_online_users(HttpServletRequest request, Model model) {
		select_online_users myForm = new select_online_users();
		myForm.Page_Load(this, model);
		return "ut_case/select_online_users";
	}

	@RequestMapping(value = "ut_case/case_tracelog_list.aspx")
	public String case_tracelog_list(HttpServletRequest request, Model model) {
		case_tracelog_list myForm = new case_tracelog_list();
		myForm.Page_Load(this, model);
		return "ut_case/case_tracelog_list";
	}

	// 业务领域管理 主页
	@RequestMapping(value = "ut_case/case_business_home.aspx")
	public String case_business_home() {
		return "ut_case/case_business_home";
	}

	// 业务领域管理-左侧树
	@RequestMapping(value = "ut_case/case_business_left_tree.aspx")
	public void case_business_left_tree(String NodeId,String Pnode, PrintWriter out) {
		String strHtml = "";

		String strHolder = "tvList";
		case_business_left_tree myForm = new case_business_left_tree(NodeId);
		if (myString.IsEmpty(NodeId))
		strHtml = myForm.getTreeView_root(strHolder);
		else
		strHtml = myForm.getTreeView_child(NodeId);
		out.append(strHtml);
	}

	// 业务领域管理 右侧页面
	@RequestMapping(value = "ut_case/case_business_list.aspx")
	public String case_business_list(HttpServletRequest request, Model model) {
		case_business_list myForm = new case_business_list();
		myForm.Page_Load(this, model);
		return "ut_case/case_business_list";
	}


	// 选择树
	@ResponseBody
	@RequestMapping(value = "ut_case/treeview_select.ashx", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
	public String gettreeview_select(HttpServletRequest request, Model model) {
		treeview_select myForm = new treeview_select();
		return myForm.getResponse(request);
	}

	// 主页
	@RequestMapping(value = "ut_case/ut_qh/qh_agent_home.aspx")
	public String qh_agent_home() {
		return "ut_case/ut_qh/qh_agent_home";
	}

	// -左侧树
	@RequestMapping(value = "ut_case/ut_qh/qh_agent_left_tree.aspx")
	public String qh_agent_left_tree(String strNodeId, Model model) {
		return "ut_case/ut_qh/qh_agent_left_tree";
	}

	// 右侧列表页面
	@RequestMapping(value = "ut_case/ut_qh/qh_agent_list.aspx")
	public String qh_agent_list(HttpServletRequest request, Model model) {
		qh_agent_list myForm = new qh_agent_list();
		myForm.Page_Load(this, model);
		return "ut_case/ut_qh/qh_agent_list";
	}

	// -打印树形结构
	@RequestMapping(value = "ut_case/ut_qh/qh_agent_left_tree_print.aspx")
	public void qh_agent_left_tree_Data(Integer kType, String strNodeId, PrintWriter out) {
		String strHtml = "";
		String strHolder = "tvList";
		qh_agent_left_tree myForm = new qh_agent_left_tree(0);
		if (myString.IsEmpty(strNodeId))
			strHtml = myForm.getTreeView_root(strHolder);
		else
			strHtml = myForm.getTreeView_child(strNodeId);
		out.append(strHtml);
	}

	// 选择文本模板修改
	@RequestMapping(value = "ut_case/ut_qh/qh_agent_edit.aspx")
	public String qh_agent_edit(HttpServletRequest request, Model model) {
		qh_agent_edit myForm = new qh_agent_edit();
		myForm.Page_Load(this, model);
		return "ut_case/ut_qh/qh_agent_edit";
	}

	// 获取主页展示当天工单数
	@ResponseBody
	@RequestMapping(value = { "ut_case/UltraCRM.ashx" }, method = { RequestMethod.GET }, produces = {
			"application/json; charset=UTF-8" })
	public String ProcessRequest(HttpServletRequest request, HttpServletResponse response) {
		case_data case_data = new case_data();
		String processRequest = case_data.ProcessRequest(request, response);
		return processRequest;
	}
	
}
