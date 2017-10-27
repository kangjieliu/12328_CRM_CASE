package com.CallThink.ut_case;

import org.springframework.ui.Model;

import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.pmAgent_info;
import com.CallThink.base.pmCode.UltraCRM_Page;
/**
 * 
 * @author Liukj
 * @date 20171023
 * @Description 业务领域管理 页面
 */
public class case_business_home extends UltraCRM_Page{
	
	pmAgent_info pmAgent;
    public String pQueryString = "ktype=1";
    public void Page_Load(Object sender, Model model)
    {
        pmAgent = fun_main.GetParm();
        if (IsPostBack == false)
        {
            if (Request.getQueryString().isEmpty() == false)
                pQueryString = Request.getQueryString();
        }
    }
}
