package com.CallThink.ut_case;

import java.util.HashMap;

import javax.servlet.http.HttpSession;

import org.springframework.ui.Model;

import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmCode.UltraCRM_Page;
import com.ToneThink.ctsTools.WebUI.my_Field;
import com.ToneThink.ctsTools.WebUI.my_SearchField;
import com.ToneThink.ctsTools.WebUI.my_ToolStrip;
import com.ToneThink.ctsTools.WebUI.my_dataGrid;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.pmMap;
/**
 * 
 * @author Liukj
 * @date 20170914
 * @Description 行政划区管理页面
 */
public class qh_agent_home extends UltraCRM_Page {

	my_SearchField mySearch = new my_SearchField(3);
	private String pMenu_id = "";
	my_dataGrid mydg = new my_dataGrid(51);
	my_ToolStrip myToolBar = new my_ToolStrip();
	my_Field myFld_Query_custom = new my_Field(3);
	HttpSession session = Request.getSession();

	public void Page_Load(Object sender, Model model) {
		pmAgent = fun_main.GetParm();
		if (IsPostBack == false)// 正被首次加载和访问
		{
			int nHistory = 0;
			pmMap res = fun_main.QuerySplit(Request);
			int rc = res.nRet;
			if (rc > 0) {
				HashMap htQuery = res.htRet;
				nHistory = Functions.atoi(Functions.ht_Get_strValue("history", htQuery));
				pMenu_id = Functions.ht_Get_strValue("menu_id", htQuery);
			}
			Save_vs("pMenu_id", pMenu_id);
		} else {
			pMenu_id = Load_vs("pMenu_id");
		}

		myFld_Query_custom.render(model);
		myToolBar.render(model);
		mySearch.render(model);
		mydg.render(model);
	}
}
