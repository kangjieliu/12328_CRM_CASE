///########################################################################################
/// Copyright (C) 2000, ToneThink.Soft  All Rights Reserved. 
/// 文件创建时间：2016-01-20
///   文件创建人：gaoww
/// 文件功能描述：工程师位置
///     调用格式：
///     维护记录：
///     
///2016-01-20 Create by gaoww
///#########################################################################################
package com.CallThink.ut_service.admin;

import java.util.HashMap;
import org.springframework.ui.Model;
import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.pmSys;
import com.CallThink.base.pmCode.UltraCRM_Page;
import com.CallThink.ut_service.tools.fun_http;
import com.ToneThink.DataTable.DataTable;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.pmList;
import com.ToneThink.ctsTools.myUtility.pmMap;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.fun_json;
import com.ToneThink.ctsTools.myUtility.myString;

@SuppressWarnings({ "unchecked", "serial" }) 
	public class engr_pos extends UltraCRM_Page
    {
        @SuppressWarnings("unused")
		private String pTableName = "SM_GHID_REALTIME";
        private String pOrg_Code = ""; //组织机构代码
        private String pOp = "";
        public void Page_Load(Object sender, Model model)
        {
            if (IsPostBack == false)//正被首次加载和访问
            {
                @SuppressWarnings("rawtypes")
				HashMap htQuery;
                pmMap res = fun_main.QuerySplit(Request); htQuery = res.htRet;
                int rc = res.nRet;
                if (rc > 0)
                {
                    pOp = Functions.ht_Get_strValue("cmd", htQuery);
                    pOrg_Code = Functions.ht_Get_strValue("key", htQuery);
                }
                Save_vs("pOp", pOp);
                Save_vs("pOrg_Code", pOrg_Code);
            }
            else
            {
                pOp = Load_vs("pOp"); 
                pOrg_Code = Load_vs("pOrg_Code");
            }

        }

        //strCmd=GetInfo&strData=100100000000
	public String GetData(String strDataMsg) {
		//strDataMsg=Functions.strURL_Decode(strDataMsg);
		String[] strArray = strDataMsg.split("&");
		String[] strArrayValue;
		String strCmd = "", strData = "";
		
		for (int i = 0; i < strArray.length; i++) {
			strArrayValue = strArray[i].split("=");
			if (strArrayValue.length < 2) continue;
			
			if ( strArrayValue[0].equals("strCmd")) {
				strCmd = strArrayValue[1];
			} else if (strArrayValue[0].equals("strData")) {
				strData = strArrayValue[1];
			}
		}

		return GetPosInfo(strCmd,strData);
	}

        public  String GetPosInfo(String strCmd, String strData)
        {
        	//strData=Functions.strURL_Decode(strData);
            String strReturn = "{}";          
          
            DataTable dtMap;
            my_odbc pTable = new my_odbc(pmSys.conn_crm);
            pmList res =pTable.my_odbc_find("SELECT GHID,OP_NAME,POS_X,POS_Y,MEMO FROM SM_GHID_REALTIME WHERE ORG_CODE='" + strData + "' AND POS_X <>'' AND POS_Y <>''",0); dtMap = res.dtRet;
            //int rc = res.nRet;
            //rc = pTable.my_odbc_find("SELECT GHID,OP_NAME,POS_X,POS_Y,MEMO FROM SM_GHID_REALTIME WHERE GHID='8615'", out dtMap);

            pTable.my_odbc_disconnect();
            if (dtMap.getCount() > 0)
            {
                //String strJason = fun_json.DataTable_toJson(dtMap);
                for (int rows = 0; rows < dtMap.getCount(); rows++)
                {
                    @SuppressWarnings("unused")
					String strX = "", strY = "", strGhid = "", strName = "";
                    strX = Functions.dtCols_strValue(dtMap, rows, "POS_X");
                    strY = Functions.dtCols_strValue(dtMap, rows, "POS_Y");
                    strGhid = Functions.dtCols_strValue(dtMap, rows, "GHID");
                    strName = Functions.dtCols_strValue(dtMap, rows, "OP_NAME");
                    engr_pos engr = new engr_pos();
                    String strLabel = myString.Format("{0}", strName);//在地图标注点显示的label内容
                    String strAddRess = engr.ATGetAddress(strX, strY);
                    if (strAddRess.equals(""))
                    {
                        //dtMap.Rows[rows].put("MEMO",strLabel);
                        dtMap.setValue(rows,"MEMO",strLabel);
                    }
                    else
                    {
                        strLabel += myString.Format("：{0}", strAddRess); 
                        //dtMap.Rows[rows].put("MEMO",strLabel);
                        dtMap.setValue(rows,"MEMO",strLabel);
                    }
                }
                strReturn = fun_json.DataTable_toJson(dtMap);
            }
            else
            {
                strReturn = "[]";
            }         
            return strReturn;
        }

        public String ATGetAddress(String strX, String strY)
        {
            String strReturn = "";

            try
            {
                String strUrl ="http://api.map.baidu.com/geocoder/v2/?ak=63d5d5de7d067945b2af4b2148850ad4&callback=renderReverse&location=" + strX + "," + strY + "&output=json&pois=1";
                strReturn=fun_http.sendGet(strUrl, "");
                strReturn = Functions.Substring(strReturn, "\"formatted_address\":\"", "\",\"business");
            }
            catch (Exception ex)
            {
                strReturn = ex.getMessage();
            }

            return strReturn;
        }
    }

