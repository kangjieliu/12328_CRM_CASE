package com.CallThink.ut_case;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.CallThink.base.pmClass.pmSys;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.myString;
import com.ToneThink.ctsTools.myUtility.pmMap;

public class business_callback {

	public Map queryId(HttpServletRequest request, String kname) {
		my_odbc pTable = new my_odbc(pmSys.conn_crm);
//        int rc = pTable.my_odbc_find("CRM_DICT_BUSINESS", "KNAME="+kname);
        String strSql = myString.Format("SELECT TYPEID FROM CRM_DICT_BUSINESS WHERE GHID='{1}'", kname);
        pmMap res = pTable.my_odbc_find(strSql, true);
        HashMap htRet = res.htRet;
		return htRet;
	}

}
