package com.CallThink.ut_case;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.CallThink.base.pmClass.e_LogInfo;
import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.pmAgent_info;
import com.CallThink.base.pmClass.pmSys;
import com.CallThink.base.support.pmInfo;
import com.ToneThink.DateTime.DateTime;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.fun_json;
import com.ToneThink.ctsTools.myUtility.myString;
import com.ToneThink.ctsTools.myUtility.pmMap;
import com.ToneThink.ctsTools.myUtility.pmRet;

/**
 * 
 * @author Liukj
 * @date 20171016
 * @Description 获取主页展示当天工单数
 */
public  class case_data {
	private String m_TableName = "CRM_CASE";
	private pmAgent_info pmAgent_info;
	public  String ProcessRequest(HttpServletRequest request, HttpServletResponse response){
		String str1 = "jsoncallback";
		String str2 = "unknown";
		String str3 = "FAIL";
		String str4 = "none";
		String str5 = myString.Format("{\"cmd\":\"{0}\",\"ret\":\"{1}\",\"data\":\"{2}\"}",
				new Object[] { str2, str3, str4 });
		try {
			pmMap localpmMap = fun_main.QuerySplit(request);
			int i = localpmMap.nRet;
			if (i > 0) {
				HashMap localHashMap = localpmMap.htRet;
				str2 = Functions.ht_Get_strValue("cmd", localHashMap);
				str1 = Functions.ht_Get_strValue("jsoncallback", localHashMap);
				if (str2 == "agent_login") {
					pmAgent_info = new pmAgent_info();
				} else if (str2 == "check_agent_permit") {
					 pmAgent_info = new pmAgent_info();
				} else {
					pmAgent_info = fun_main.GetParm();
				}
				pmRet localpmRet = Prosess_data(str2, localHashMap);
				str3 = (String) localpmRet.nRet;
				str4 = (String) localpmRet.oRet;
			}
			str5 = fun_main.getResult(str2, str3, str4);
		} catch (Exception localException1) {
			fun_main.rem("ut_case/UltraCRM.ashx ProcessRequest Fail:" + localException1.getMessage(),
					e_LogInfo.warning);
		}
		if (str1.length() > 0) {
			str5 = str1 + "(" + str5 + ");";
		}
		return str5;
	}
	public pmRet Prosess_data(String strCmd, HashMap<String,Object> htParm) {
		String str1 = "FAIL";
		String str2 = "";
		
		String str3 = Functions.ht_Get_strValue("uid", htParm);
		String str4 = Functions.ht_Get_strValue("uid", htParm);
		String str5 = Functions.ht_Get_strValue("uid", htParm);
		String str6 = "homepage_new_data";

 		Map<String,Object> localObject = (Map) pmInfo.myKvdb.Get(str6);
		if (localObject != null) {
			localObject.put("C1", Integer.valueOf(result(0, str3)));
			str2 = fun_json.Object_toJson(localObject);
			str1 = "OK";
			return new pmRet(str1, str2);
		}
		int[] arrayOfInt = { 0, 0, 0, 0 };
		arrayOfInt[0] = result(0, str3);
		arrayOfInt[1] = result(1, str4);
		arrayOfInt[2] = result(2, str5);
		arrayOfInt[3] = result(3, str6);

		localObject = new HashMap<String,Object>();
		localObject.put("C1", Integer.valueOf(arrayOfInt[0]));
		localObject.put("C2", Integer.valueOf(arrayOfInt[1]));
		localObject.put("C3", Integer.valueOf(arrayOfInt[2]));
		localObject.put("C4", Integer.valueOf(arrayOfInt[3]));

		pmInfo.myKvdb.Setex(str6, localObject, 600);
		str2 = fun_json.Object_toJson(localObject);
		str1 = "OK";
		pmRet localpmRet = new pmRet(str1, str2);
		return localpmRet;
	}

	private static int result(int paramInt, String paramString) {
		int i = 0;
		int j = 0;
		String str1 = "";
		my_odbc localmy_odbc = new my_odbc(pmSys.conn_crm);
		localmy_odbc.IsShowBox = 2;
		if (paramInt == 0) {
			str1 = "SELECT COUNT(*) AS CNT FROM CRM_CASE WHERE CASETYPE ='"+paramInt+"'AND SDATE='" + DateTime.NowString("yyyyMMdd") + "'";
			j = localmy_odbc.my_odbc_find(str1);
			if (j > 0) {
				i = localmy_odbc.my_odbc_result("CNT", 0);
			}
		}
		if (paramInt == 1) {
			str1 = "SELECT COUNT(*) AS CNT FROM CRM_CASE WHERE CASETYPE ='"+paramInt+"'AND SDATE='" + DateTime.NowString("yyyyMMdd") + "'";
			j = localmy_odbc.my_odbc_find(str1);
			if (j > 0) {
				i = localmy_odbc.my_odbc_result("CNT", 0);
			}
		}
		if (paramInt == 2) {
			str1 = "SELECT COUNT(*) AS CNT FROM CRM_CASE WHERE CASETYPE ='"+paramInt+"'AND SDATE='" + DateTime.NowString("yyyyMMdd") + "'";
			j = localmy_odbc.my_odbc_find(str1);
			if (j > 0) {
				i = localmy_odbc.my_odbc_result("CNT", 0);
			}
		}
		if (paramInt == 3) {
			str1 = "SELECT COUNT(*) AS CNT FROM CRM_KNOWLEDGE_LIB_BS WHERE SDATE LIKE '"
					+ DateTime.NowString("yyyy-MM-dd") + "%'";
			j = localmy_odbc.my_odbc_find(str1);
			if (j > 0) {
				i = localmy_odbc.my_odbc_result("CNT", 0);
			}
		}
		localmy_odbc.my_odbc_disconnect();
		return i;
	}
}
