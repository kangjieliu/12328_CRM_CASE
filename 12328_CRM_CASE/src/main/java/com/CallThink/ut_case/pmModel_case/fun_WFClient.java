///########################################################################################
/// Copyright (C) 2000, ToneThink.Soft  All Rights Reserved. 
/// 文件创建时间：2000-06-05
///   文件创建人：peng
/// 文件功能描述：与UltraCRM相关的主要函数
/// 
///     维护记录：
/// 2007-07-08 将该文件拆分，只处理Customer Case
/// 2007-08-17 与标准版相同
///            修改Fill_Field(),Fill_Grid() 函数 
///#########################################################################################
package com.CallThink.ut_case.pmModel_case;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.ByteArrayBuffer;

import java.util.List;
import com.ToneThink.DataTable.DataTable;
import com.CallThink.base.pmClass.pmSys;
import com.ToneThink.DataTable.DataRow;
import com.ToneThink.DateTime.DateTime;
import com.ToneThink.ctsTools.dbHelper.my_odbc;
import com.ToneThink.ctsTools.myUtility.pmList;
import com.ToneThink.ctsTools.myUtility.pmMap;
import com.ToneThink.ctsTools.Regex.Regex;
import com.ToneThink.ctsTools.Regex.Regex.RegexOptions;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.myException;
import com.ToneThink.ctsTools.myUtility.myString;

    /// <summary>
    /// fun_CRM 的摘要说明。
    /// </summary>
    public class fun_WFClient
    {
        public static int Submit_toWFS(String strBusinessId, String strBookMarkName, String strInput)   //add by gaoww 20100224 增加传入指定工单流转坐席工号的参数
        {
            int nReturn = -1;
            /*
            WorkflowServiceClient myClient;
            if (pmSys.WorkFlow_url.equals(""))  //add by gaoww 20130716 增加判断，如果不填此参数，表示此用户没有购买引擎程序，只做工作流状态或环节的数据库更新，不做提交工作流操作，避免出现连接不上引擎程序提示超时
                return nReturn;
            try
            {
                //pmSys.WorkFlow_url = "http://168.168.168.8:5050/WF/";
                //pmSys.WorkFlow_url = "http://168.168.168.203:5050/WF/";
                BasicHttpBinding web_http_bind = new BasicHttpBinding();
                web_http_bind.Name = "BasicHttpBinding_IWorkflowService";
                web_http_bind.HostNameComparisonMode = HostNameComparisonMode.StrongWildcard;
                web_http_bind.Security.Mode = BasicHttpSecurityMode.None;
                web_http_bind.SendTimeout = new TimeSpan(0, 0, 5);          //默认 1Min
                web_http_bind.ReceiveTimeout = new TimeSpan(0, 0, 10);      // 默认 10Min

                myClient = new WorkflowServiceClient(web_http_bind, new EndpointAddress(pmSys.WorkFlow_url));

                //GHID|GHID_RECV|CASETYPE|TABLENAME|CASEID|CASENAME
                //string strInput = String.Format("{0}|{1}|{2}|{3}", strUid, strUid_rec,n strCaseId, strWFNodeInfov); //增加指定下一环节处理坐席功能
                myClient.ProcessChanged(strBusinessId, strBookMarkName, strInput);
                
                nReturn = 1;
            }
            catch (Exception ex)
            {
                Functions.MsgBox("提交任务到工作流失败，原因：" + myException.Message(ex));
            }*/
            
            if (pmSys.WorkFlow_url.equals(""))  //add by gaoww 20130716 增加判断，如果不填此参数，表示此用户没有购买引擎程序，只做工作流状态或环节的数据库更新，不做提交工作流操作，避免出现连接不上引擎程序提示超时
                return nReturn;
            try
            {
                pmSys.WorkFlow_url = "http://168.168.165.8:5050/WF";
                //pmSys.WorkFlow_url = "http://168.168.168.203:5050/WF/";

                //GHID|GHID_RECV|CASETYPE|TABLENAME|CASEID|CASENAME
                //string strInput = String.Format("{0}|{1}|{2}|{3}", strUid, strUid_rec,n strCaseId, strWFNodeInfov); //增加指定下一环节处理坐席功能
                //myClient.ProcessChanged(strBusinessId, strBookMarkName, strInput);
                
                String strCTI_url = myString.Format("{0}/ProcessChanged/{1}/{2}/{3}", pmSys.WorkFlow_url,strBusinessId,strBookMarkName,strInput);
                String strRet = httpClient(strCTI_url);
                nReturn = 1;
            }
            catch (Exception ex)
            {
                Functions.MsgBox("提交任务到工作流失败，原因：" + myException.Message(ex));
            }
            return nReturn;
        }
        
    	private static String httpClient(String strCTI_url) throws IOException {
    		//HttpServletRequest request,HttpServletResponse response
    		
             String strReturn = "FAIL";
             //String strCTI_url = "http://168.168.168.120";
             //strCTI_url = "http://localhost:8080/WF";
             try
             {
            	//HttpGet() strUrl 地址不能包含 |, \t,空格 等字符
             	String strUrl = strCTI_url.replace("|", "%7c").replace(" ", "+").replace("\t", "+");// + "/cc_softcall/ATClient.ashx?" + request.getQueryString();
                RequestConfig globalConfig = RequestConfig.custom()
                 		.setConnectTimeout(5000)                              //5Secs 
                 		.setConnectionRequestTimeout(10000)           //10Secs 
                 		.setCookieSpec(CookieSpecs.STANDARD)
                 		.build();
         
                 CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(globalConfig).build();
                 //创建一个GET请求
                 HttpGet httpGet = new HttpGet(strUrl);
      
                 httpGet.addHeader("Connection","keep-alive");  
                 httpGet.addHeader("Accept-Encoding", "gzip, deflate");  
                 httpGet.addHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.152 Safari/537.36");
                 //httpGet.addHeader("Cookie","_gat=1; nsfw-click-load=off; gif-click-load=on; _ga=GA1.2.1861846600.1423061484");
                 try {
                     //发送请求，并执行
                     CloseableHttpResponse resp = httpClient.execute(httpGet);
                     
                     //得到响应状态码  
                     int statuCode = resp.getStatusLine().getStatusCode();  
                     //根据状态码进行逻辑处理  
                     switch (statuCode){  
                       case 200:  
                         //获得响应实体  
                         InputStream in = resp.getEntity().getContent();
                         ByteArrayBuffer buffer = new ByteArrayBuffer(4096);  
                         byte[] tmp = new byte[4096];  
                         int count;  
                         while((count=in.read(tmp)) != -1){  
                             buffer.append(tmp, 0, count);  
                         }  
                         strReturn = new String(buffer.toByteArray());
                         System.out.println(strReturn+";"+strCTI_url);
                         break;  
                     case 400:  
                         System.out.println("下载400错误代码，请求出现语法错误" + strUrl);  
                         break;  
                     case 403:  
                         System.out.println("下载403错误代码，资源不可用" + strUrl);                
                         break;  
                     case 404:  
                         System.out.println("下载404错误代码，无法找到指定资源地址" + strUrl);  
                         break;  
                     case 503:  
                         System.out.println("下载503错误代码，服务不可用" + strUrl);  
                         break;  
                     case 504:  
                         System.out.println("下载504错误代码，网关超时" + strUrl);  
                         break;  
                     }  
                   } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
             catch (Exception ex)
             {
             	System.out.println("httpClient fail:" + myException.Message(ex));
            	 
             }
            return strReturn;
    	}
    	
    	//请求Get
/*
    	//public class JavaNetURLRESTFulClient {

    	   //    private static final String targetURL = "http://localhost:8080/JerseyJSONExample/rest/jsonServices/print/Jamie";

    	       public static void main(String[] args) {

    	                try {

    	                     URL restServiceURL = new URL(targetURL);

    	                     HttpURLConnection httpConnection = (HttpURLConnection) restServiceURL.openConnection();
    	                     httpConnection.setRequestMethod("GET");
    	                     httpConnection.setRequestProperty("Accept", "application/json");

    	                     if (httpConnection.getResponseCode() != 200) {
    	                            throw new RuntimeException("HTTP GET Request Failed with Error code : "
    	                                          + httpConnection.getResponseCode());
    	                     }

    	                     BufferedReader responseBuffer = new BufferedReader(new InputStreamReader(
    	                            (httpConnection.getInputStream())));

    	                     String output;
    	                     System.out.println("Output from Server:  \n");

    	                     while ((output = responseBuffer.readLine()) != null) {
    	                            System.out.println(output);
    	                     }

    	                     httpConnection.disconnect();

    	                } catch (MalformedURLException e) {

    	                     e.printStackTrace();

    	                } catch (IOException e) {

    	                     e.printStackTrace();

    	                }

    	              }
    	//}

    	//运行后输出结果是：

    	//Output from Server:      {"id":1,"firstName":"Jamie","age":22,"lastName":"Diaz"}

    	//POST提交：

    	//public class JavaNetURLRESTFulClient {

    	 //      private static final String targetURL = "http://localhost:8080/JerseyJSONExample/rest/jsonServices/send";

    	       public static void main(String[] args) {

    	              try {

    	                     URL targetUrl = new URL(targetURL);

    	                     HttpURLConnection httpConnection = (HttpURLConnection) targetUrl.openConnection();
    	                     httpConnection.setDoOutput(true);
    	                     httpConnection.setRequestMethod("POST");
    	                     httpConnection.setRequestProperty("Content-Type", "application/json");

    	                     String input = "{\"id\":1,\"firstName\":\"Liam\",\"age\":22,\"lastName\":\"Marco\"}";

    	                     OutputStream outputStream = httpConnection.getOutputStream();
    	                     outputStream.write(input.getBytes());
    	                     outputStream.flush();

    	                     if (httpConnection.getResponseCode() != 200) {
    	                            throw new RuntimeException("Failed : HTTP error code : "
    	                                   + httpConnection.getResponseCode());
    	                     }

    	                     BufferedReader responseBuffer = new BufferedReader(new InputStreamReader(
    	                                   (httpConnection.getInputStream())));

    	                     String output;
    	                     System.out.println("Output from Server:\n");
    	                     while ((output = responseBuffer.readLine()) != null) {
    	                            System.out.println(output);
    	                     }

    	                     httpConnection.disconnect();

    	                } catch (MalformedURLException e) {

    	                     e.printStackTrace();

    	                } catch (IOException e) {

    	                     e.printStackTrace();

    	               }

   	              }
   	              */     
    	
    	/*
    	利用setRequestProperty设置请求头参数，请求头参数以键值对形式传入，如果需要编码，则传入编码好的字符串即可。

    	conn.setRequestProperty("Authorization", "Basic YWRtaW46YWRtaW4=");
    	conn.setRequestProperty("Content-Type", "application/json");
    	conn.setRequestProperty("departmentName","dGVzdA==");
    	添加body参数

    	conn.setDoOutput(true);// 是否输入参数
    	         StringBuffer params = new StringBuffer();
    	         // 表单参数与get形式一样
    	         params.append("f292139625cd4d59fcff42360ce11fc");
    	         byte[] bypes = params.toString().getBytes();
    	         conn.getOutputStream().write(bypes);// 输入参数
        	解决输出乱码

    	package Restful;
    	import java.io.BufferedReader;
    	import java.io.IOException;
    	import java.io.InputStream;
    	import java.io.InputStreamReader;
    	import java.net.HttpURLConnection;
    	import java.net.URL;
    	import java.net.URLEncoder;
    	public class Restful {
    	    public static void main(String[] args) throws IOException {
    	        URL url = new URL("http://localhost:8080/NbOrg/api/getPersonInfo/6d1e8b89eefb37a0b3f222533f82ca59");
    	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    	        // 提交模式
    	        conn.setRequestMethod("GET");// POST GET PUT DELETE
    	        conn.setRequestProperty("Authorization", "Basic YWRtaW46YWRtaW4=");//YWRtaW46YWRtaW4=");
    	        // 设置访问提交模式，表单提交
    	        conn.setRequestProperty("Content-Type", "application/json");
    	        conn.setConnectTimeout(15000);// 连接超时 单位毫秒
    	        conn.setReadTimeout(15000);// 读取超时 单位毫秒             
    	         //读取请求返回值
//    	       conn.setDoOutput(true);// 是否输入参数
//    	      
//    	       StringBuffer params = new StringBuffer();
//    	       // 表单参数与get形式一样
//    	       params.append("f292139625cd4d59fcff42360ce11fc");
//    	       byte[] bypes = params.toString().getBytes();
//    	       conn.getOutputStream().write(bypes);// 输入参数
    	         byte bytes[]=new byte[1024];
    	         InputStream inStream=conn.getInputStream();
    	         inStream.read(bytes, 0, inStream.available());
    	         System.out.println(new String(bytes, "utf-8"));
    	    }
    	}
    	第二种获取返回值的代码

    	BufferedReader br = new BufferedReader(new InputStreamReader(
    	                    myURLConnection.getInputStream()));
    	            String line;
    	            StringBuilder response = new StringBuilder();
    	            while ((line = br.readLine()) != null) {
    	                response.append(line);
    	                response.append('\r');
    	                System.out.println(line);
    	            }
    	            br.close();
    		不过容易出现乱码，注意设置编码格式。
    		*/
    }

