package com.CallThink.ut_service.controller;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.CallThink.ut_case.case_attach;
import com.CallThink.ut_service.case_service_edit;
import com.CallThink.ut_service.case_service_list;
import com.CallThink.ut_service.case_task_assign;
import com.CallThink.ut_service.case_task_edit;
import com.CallThink.ut_service.case_task_list;
import com.CallThink.ut_service.case_trace_log_list;
import com.CallThink.ut_service.wf_process_set_service;
import com.CallThink.ut_service.admin.engr_pos;
import com.CallThink.ut_service.admin.ghid_org_tree_select;
import com.CallThink.ut_service.admin.ghid_permit_edit;
import com.CallThink.ut_service.admin.ghid_permit_list;
import com.CallThink.ut_service.admin.ghid_realtime_edit;
import com.CallThink.ut_service.admin.ghid_realtime_list;
import com.CallThink.ut_service.admin.ghid_skill_left_tree;
import com.CallThink.ut_service.admin.ghid_skill_right;
import com.CallThink.ut_service.monitor.case_task_action;
import com.CallThink.ut_service.tools.map;
import com.ToneThink.ctsTools.myUtility.myString;

/**
 * 服务e调度,我的任务,控制器
 * 
 * @author G-APPLE
 *
 */
@Controller
public class ut_service_ctrl {

	@RequestMapping(value = "ut_service/case_service_list.aspx")
	public String getCase_list(HttpServletRequest request, Model model) {
		case_service_list myForm = new case_service_list();
		myForm.Page_Load(this, model);
		return "ut_service/case_service_list";
	}

	@RequestMapping(value = "ut_service/case_service_edit.aspx")
	public String getCase_edit(HttpServletRequest request, Model model) {
		case_service_edit myForm = new case_service_edit();
		myForm.Page_Load(this, model);

		return "ut_service/case_service_edit";
	}

	@RequestMapping(value = "ut_service/wf_process_set_service.aspx")
	public String getwf_process_set_service(HttpServletRequest request, Model model) {
		wf_process_set_service myForm = new wf_process_set_service();
		myForm.Page_Load(this, model);
		return "ut_service/wf_process_set_service";
	}

//	@RequestMapping(value = "ut_service/case_task_list.aspx")
//	public String Getcase_task_list(HttpServletRequest request, Model model) {
//		case_task_list myForm = new case_task_list();
//		myForm.Page_Load(this, model);
//
//		return "ut_service/case_task_list";
//	}
//
//	@RequestMapping(value = "ut_service/case_task_edit.aspx")
//	public String Getcase_task_edit(HttpServletRequest request, Model model) {
//		case_task_edit myForm = new case_task_edit();
//		myForm.Page_Load(this, model);
//
//		return "ut_service/case_task_edit";
//	}

//	@RequestMapping(value = "ut_service/case_task_assign.aspx")
//	public String Getcase_task_assign(HttpServletRequest request, Model model) {
//		case_task_assign myForm = new case_task_assign();
//		myForm.Page_Load(this, model);
//
//		myForm.AddStyle(model);
//
//		return "ut_service/case_task_assign";
//	}

//	@RequestMapping(value = "ut_service/monitor/case_task_action.aspx")
//	public String Getcase_task_action(HttpServletRequest request, Model model) {
//		case_task_action myForm = new case_task_action();
//		myForm.Page_Load(this, model);
//
//		return "ut_service/monitor/case_task_action";
//	}

	// 地图
//	@RequestMapping(value = "ut_service/tools/map.aspx")
//	public String Get_map(HttpServletRequest request, Model model) {
//		map myForm = new map();
//		myForm.Page_Load(this, model);
//
//		return "ut_service/tools/map";
//	}

	// 模板页
	@RequestMapping(value = "ut_service/admin/ghid_skill_home.aspx")
	public String Get_ghid_skill_home(HttpServletRequest request, Model model) {
		return "ut_service/admin/ghid_skill_home";
	}

	// 模板页中左侧树形结构
	@RequestMapping(value = "ut_service/admin/ghid_skill_left_tree.aspx")
	public String Get_ghid_skill_left_tree(HttpServletRequest request, Model model) {
		return "ut_service/admin/ghid_skill_left_tree";
	}

	// 打印树形结构
	@RequestMapping(value = "ut_service/admin/ghid_skill_left_tree_print.aspx")
	public void Get_ghid_skill_left_treePrint(Integer kType, String strNodeId, PrintWriter out) {
		String strHtml = "";
		String strHolder = "tvList";
		ghid_skill_left_tree myForm = new ghid_skill_left_tree(0);
		if (myString.IsEmpty(strNodeId))
			strHtml = myForm.getTreeView_root(strHolder);
		else
			strHtml = myForm.getTreeView_child(strNodeId);
		out.append(strHtml);
	}

	// 右侧list
	@RequestMapping(value = "ut_service/admin/ghid_skill_right.aspx")
	public String Get_ghid_skill_right(HttpServletRequest request, Model model) {
		ghid_skill_right myForm = new ghid_skill_right();
		myForm.Page_Load(this, model);

		return "ut_service/admin/ghid_skill_right";
	}

	// 工程师实时状态管理
	@RequestMapping(value = "ut_service/admin/ghid_realtime_list.aspx")
	public String Get_ghid_realtime_list(HttpServletRequest request, Model model) {
		ghid_realtime_list myForm = new ghid_realtime_list();
		myForm.Page_Load(this, model);

		return "ut_service/admin/ghid_realtime_list";
	}

	@RequestMapping(value = "ut_service/admin/ghid_realtime_edit.aspx")
	public String Get_ghid_realtime_edit(HttpServletRequest request, Model model) {
		ghid_realtime_edit myForm = new ghid_realtime_edit();
		myForm.Page_Load(this, model);

		return "ut_service/admin/ghid_realtime_edit";
	}

	// 手机认证管理
	@RequestMapping(value = "ut_service/admin/ghid_permit_list.aspx")
	public String Get_ghid_permit_list(HttpServletRequest request, Model model) {
		ghid_permit_list myForm = new ghid_permit_list();
		myForm.Page_Load(this, model);

		return "ut_service/admin/ghid_permit_list";
	}

	@RequestMapping(value = "ut_service/admin/ghid_permit_edit.aspx")
	public String Get_ghid_permit_edit(HttpServletRequest request, Model model) {
		ghid_permit_edit myForm = new ghid_permit_edit();
		myForm.Page_Load(this, model);

		return "ut_service/admin/ghid_permit_edit";
	}

	// 工程师位置模板页
	@RequestMapping(value = "ut_service/admin/engr_pos_home.aspx")
	public String Get_engr_pos_home(HttpServletRequest request, Model model) {
		return "ut_service/admin/engr_pos_home";
	}

	@RequestMapping(value = "ut_service/admin/engr_pos.aspx")
	public String Get_engr_pos(HttpServletRequest request, Model model) {
		return "ut_service/admin/engr_pos";
	}

	@ResponseBody
	@RequestMapping(value = "ut_service/admin/engr_pos.aspx/GetPosInfo", produces = "application/json; charset=UTF-8")
	public String Get_engr_posGetPosInfo(HttpServletRequest request, Model model, @RequestBody String strData) {
		// System.out.println(strData);
		engr_pos myForm = new engr_pos();
		myForm.Page_Load(this, model);

		return myForm.GetData(strData);
	}

	// 模板页中左侧树形结构
	@RequestMapping(value = "ut_service/admin/ghid_org_tree_select.aspx")
	public String Get_ghid_org_tree_select(HttpServletRequest request, Model model) {
		ghid_org_tree_select myForm = new ghid_org_tree_select(0, model);
		return "ut_service/admin/ghid_org_tree_select";
	}

	// 打印树形结构
	@RequestMapping(value = "ut_service/admin/ghid_org_tree_select_print.aspx")
	public void Get_ghid_org_tree_selectPrint(Integer kType, String strNodeId, PrintWriter out) {
		String strHtml = "";
		String strHolder = "tvList";
		ghid_org_tree_select myForm = new ghid_org_tree_select(0);
		if (myString.IsEmpty(strNodeId))
			strHtml = myForm.getTreeView_root(strHolder);
		else
			strHtml = myForm.getTreeView_child(strNodeId);
		out.append(strHtml);
	}

	// add by duzy
	@ResponseBody
	@RequestMapping(value = "ut_service/case_attach.aspx/upload", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
	public String getAgent_upload(HttpServletRequest request, String sender, int sender_type) {
		case_attach myForm = new case_attach();
		String strRet = myForm.doUpload(request, sender, sender_type);
		return strRet;
	}
	
	@RequestMapping(value = "ut_service/case_trace_log_list.aspx")
	public String Get_case_trace_log_list(HttpServletRequest request, Model model) {
		case_trace_log_list myForm = new case_trace_log_list();
		myForm.Page_Load(this, model);
		
		return "ut_service/case_trace_log_list";
	}
}
