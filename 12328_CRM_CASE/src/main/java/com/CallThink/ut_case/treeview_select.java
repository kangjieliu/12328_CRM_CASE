package com.CallThink.ut_case;

import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.pmAgent_info;
import com.CallThink.base.pmClass.pmSys;
import com.CallThink.base.support.clas_redis;
import com.CallThink.base.support.pmInfo;
import com.ToneThink.DataTable.DataRow;
import com.ToneThink.DataTable.DataTable;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.Encoding;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.fun_json;
import com.ToneThink.ctsTools.myUtility.myString;
import com.ToneThink.ctsTools.myUtility.pmList;
import com.ToneThink.ctsTools.myUtility.pmMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
public class treeview_select {
	private pmAgent_info pmAgent;
	  
	  public String getResponse(HttpServletRequest request)
	  {
	    this.pmAgent = fun_main.GetParm();
	    
	    String strOutput = "";
	    pmMap res = fun_main.QuerySplit(request);
	    if (res.nRet > 0)
	    {
	      HashMap htQuery = res.htRet;
	      String strCmd = Functions.ht_Get_strValue("cmd", htQuery);
	      String strOpts = Functions.ht_Get_strValue("opts", htQuery);
	      strOutput = Prosess_data(strCmd, strOpts);
	    }
	    return strOutput;
	  }
	  
	  private String Prosess_data(String strCmd, String strOpts)
	  {
	    String strReturn = "";
	    
	    strOpts = Functions.strItem_fromBase64(strOpts, Encoding.UTF8);
	    HashMap<String, String> htOpts = fun_json.Hashtable_fromJson(strOpts);
	    String strTableName = Functions.ht_Get_strValue("table", htOpts);
	    String strField = Functions.ht_Get_strValue("field", htOpts);
	    String strFilter = Functions.ht_Get_strValue("filter", htOpts);
	    String strConn = Functions.ht_Get_strValue("conn", htOpts);
	    if (strTableName.equals("CRM_DICT_BUSINESS")) {
	    	return Response_member(0,strFilter,strCmd);
	  }
	    if (strConn.equals("callthink")) {
	      strConn = pmSys.conn_callthink;
	    } else if (strConn.equals("cdr")) {
	      strConn = pmSys.conn_cdr;
	    } else if ((strConn.equals("crm")) || (strConn.length() < 1)) {
	      strConn = pmSys.conn_crm;
	    }
	    String strSql = Functions.dt_GetSQL(strTableName, strField, strFilter);
	    
	    String strKey = "dtTreeViewSelect" + strSql.hashCode();
	    DataTable dtRet = (DataTable)pmInfo.myKvdb.Get(strKey);
	    if (dtRet == null)
	    {
	      my_odbc pTable = new my_odbc(strConn);
	      pmList res = pTable.my_odbc_find(strSql, 0);
	      dtRet = res.dtRet;
	      pTable.my_odbc_disconnect();
	      if (res.nRet == 1) {
	        pmInfo.myKvdb.Setex(strKey, dtRet, 60);
	      }
	    }
	    String strOutput = "";
	    
	    StringBuilder sbNodes_json = new StringBuilder();
	    if (dtRet.getCount() > 0)
	    {
	      for (DataRow drRet : dtRet.Rows()) {
	        sbNodes_json.append(myString.Format("{\"id\":\"{0}\",\"name\":\"{1}\",\"title\":\"{0}\",\"pId\":\"{2}\",\"icon\":\"{3}/ui_common/icons/icon/group.png\"},", new Object[] {drRet
	          .getValue(0), drRet.getValue(1), "root", pmSys.rootURL }));
	      }
	      strOutput = myString.Format("{0}", new Object[] { myString.TrimEnd(sbNodes_json.toString(), ",") });
	      strOutput = myString.Format("[{\"id\": \"root\", \"name\": \"业务领域\", \"title\": \"ALL\", \"checked\": false,\"open\":true},{0}]", new Object[] { strOutput });
	    }
	    strReturn = strOutput;
	    return strReturn;
	  }
	  
	  private String Response_member(int nOnline,String strFilter,String strCmd)
	  {
		  String strReturn = "[]";
		  DataTable dtRet = Get_GroupNode(strFilter);
		  if("BUSINESS2".equals(strCmd)){
			  if (dtRet.getCount() > 0)
			  {
				  String strRet = GetNodes_json(strFilter, dtRet, nOnline);
				  
				  strReturn = myString.Format("[{\"id\": \""+strFilter+"\", \"name\": \"业务事项\", \"title\": \"ALL\",\"checked\": false,\"open\":true},{0}]", new Object[] { strRet });
			  }
		  }else if("BUSINESS3".equals(strCmd)){
			  if (dtRet.getCount() > 0)
			  {
				  String strRet = GetNodes_json(strFilter, dtRet, nOnline);
				  
				  strReturn = myString.Format("[{\"id\": \""+strFilter+"\", \"name\": \"业务明细\", \"title\": \"ALL\",\"checked\": false,\"open\":true},{0}]", new Object[] { strRet });
			  }
		  }
	    return strReturn;
	  }
	  
	  private String GetNodes_json(String ptID, DataTable dtNode, int nMember)
	  {
	    String strReturn = "";
	    
	    ArrayList alUid_online = new ArrayList();
	    if ((nMember == 1) && (this.pmAgent.uType < 10)) {}
	    StringBuilder sbNodes_json = new StringBuilder();
	    for (DataRow drRet : dtNode.Rows())
	    {
	      String strNodeId = drRet.getValue("TYPEID").toString();
	      String strNodeName = drRet.getValue("KNAME").toString();
	      int nCnt_childNode = Functions.atoi(drRet.getValue("nCNT").toString());
	      if (nCnt_childNode < 1)
	      {
	        sbNodes_json.append(myString.Format("{\"id\":\"{0}\",\"name\":\"{1}\",\"title\":\"{0}\",\"pId\":\"{2}\",\"icon\":\"{3}/ui_common/icons/icon/group.png\"},", new Object[] { strNodeId, strNodeName, ptID, pmSys.rootURL }));
	        /*if (nMember <= 1)
	        {
	          DataTable dtRet = null;
	          my_odbc pTable = new my_odbc(pmSys.conn_crm);
	          pmList res = pTable.my_odbc_find("CRM_DICT_BUSINESS", myString.Format("TYPEID IN (SELECT TYPEID FROM CRM_DICT_BUSINESS WHERE TYPEID='{0}') ", new Object[] { strNodeId }), 0);
	          pTable.my_odbc_disconnect();
	          int rc = res.nRet;
	          dtRet = res.dtRet;
	        }*/
	      }
	      else
	      {
	        String strFilter = "(TYPEID LIKE '" + strNodeId + "__')";
	        DataTable dtRet = Get_GroupNode(strFilter);
	        String strRet = GetNodes_json(strNodeId, dtRet, nMember);
	        
	        sbNodes_json.append(myString.Format("{\"id\":\"{0}\",\"name\":\"{1}\",\"title\":\"{0}\",\"pId\":\"{2}\",\"icon\":\"{4}/ui_common/icons/icon/group.png\"},{3},", new Object[] { strNodeId, strNodeName, ptID, strRet, pmSys.rootURL }));
	      }
	      strReturn = myString.Format("{0}", new Object[] { myString.TrimEnd(sbNodes_json.toString(), ',') });
	    }
	    return strReturn;
	  }
	  
	  private DataTable Get_GroupNode(String strFilter)
	  {
	    String pTableName = "CRM_DICT_BUSINESS";
	    DataTable dtRet = null;
	    my_odbc pTable = new my_odbc(pmSys.conn_crm);
	    String strSql = "";
	    strSql = myString.Format("SELECT TYPEID,KNAME,(SELECT COUNT(*) FROM {0} WHERE TYPEID LIKE (OPGP.TYPEID + '__')) as nCNT FROM {0} OPGP WHERE PNODE ='{1}'  ORDER BY TYPEID", new Object[] { pTableName, strFilter });
	    pmList res = pTable.my_odbc_find(strSql, 0);
	    pTable.my_odbc_disconnect();
	    dtRet = res.dtRet;
	    return dtRet;
	  }
	  
}
