package com.CallThink.ut_case;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.CallThink.base.pmClass.pmSys;
import com.CallThink.base.support.pmInfo;
import com.ToneThink.DateTime.DateTime;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.fun_json;
import com.ToneThink.ctsTools.myUtility.pmRet;

/**
 * 
 * @author Liukj
 * @date 20171016
 * @Description 获取主页展示当天工单数
 */
public  class case_data {
	
	public static pmRet Prosess_data(String strCmd, HashMap htParm) {
		String str1 = "FAIL";
		String str2 = "";

		String str3 = Functions.ht_Get_strValue("uid", htParm);
		String str4 = Functions.ht_Get_strValue("uid", htParm);
		String str5 = Functions.ht_Get_strValue("uid", htParm);
		String str6 = "homepage_new_data";

		Object localObject = (Map) pmInfo.myKvdb.Get(str6);
		if (localObject != null) {
			((Map) localObject).put("C1", Integer.valueOf(result(0, str3)));
			str2 = fun_json.Object_toJson(localObject);
			str1 = "OK";
			return new pmRet(str1, str2);
		}
		int[] arrayOfInt = { 0, 0, 0, 0 };
		arrayOfInt[0] = result(0, str3);
		arrayOfInt[1] = result(1, str4);
		arrayOfInt[2] = result(2, str5);
		arrayOfInt[3] = result(3, str6);

		localObject = new HashMap();
		((Map) localObject).put("C1", Integer.valueOf(arrayOfInt[0]));
		((Map) localObject).put("C2", Integer.valueOf(arrayOfInt[1]));
		((Map) localObject).put("C3", Integer.valueOf(arrayOfInt[2]));
		((Map) localObject).put("C4", Integer.valueOf(arrayOfInt[3]));

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
		Map localMap;
		int k;
		Iterator localIterator;
		Map.Entry localEntry;
		if (paramInt == 0) {
			str1 = "SELECT COUNT(*) AS CNT FROM CRM_CASE WHERE SDATE='" + DateTime.NowString("yyyyMMdd") + "'";
			j = localmy_odbc.my_odbc_find(str1);
			if (j > 0) {
				i = localmy_odbc.my_odbc_result("CNT", 0);
			}
		}
		if (paramInt == 1) {
			str1 = "SELECT COUNT(*) AS CNT FROM CRM_CASE1 WHERE SDATE='" + DateTime.NowString("yyyyMMdd") + "'";
			j = localmy_odbc.my_odbc_find(str1);
			if (j > 0) {
				i = localmy_odbc.my_odbc_result("CNT", 0);
			}
		}
		if (paramInt == 2) {
			str1 = "SELECT COUNT(*) AS CNT FROM CRM_CASE2 WHERE SDATE='" + DateTime.NowString("yyyyMMdd") + "'";
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
