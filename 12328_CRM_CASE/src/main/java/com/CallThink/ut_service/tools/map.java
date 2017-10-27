
package com.CallThink.ut_service.tools;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import com.CallThink.base.pmClass.e_Level_base;
import com.CallThink.base.pmClass.fun_main;
import com.CallThink.base.pmClass.pmSys;
import com.CallThink.base.pmClass.pmAgent_info;
import com.CallThink.base.pmCode.UltraCRM_Page;
import com.ToneThink.ctsTools.WebUI.my_ToolStrip;
import com.ToneThink.ctsTools.myUtility.Functions;
import com.ToneThink.ctsTools.myUtility.pmMap;

    public class map extends UltraCRM_Page
    {
        private String pPosX = "";
        private String pPosY = "";
        private String pOp = "";
        private String pAddress = "";
        private String pDrag = "1";//是否可以拖拽标注
        my_ToolStrip myToolBar = new my_ToolStrip();
        public void Page_Load(Object sender, Model model)
        {
            HashMap htQuery;
            pmMap res = fun_main.QuerySplit(Request); htQuery = res.htRet;
            int rc = res.nRet;
            if (rc > 0)
            {
                pOp = Functions.ht_Get_strValue("cmd", htQuery);
                pPosX = Functions.ht_Get_strValue("pos_x", htQuery);
                pPosY = Functions.ht_Get_strValue("pos_y", htQuery);
                pAddress = Functions.ht_Get_strValue("address", htQuery);
                pDrag = Functions.ht_Get_strValue("drag", htQuery);
            }
            String strX = "";
            String strY = "";
            String strAddRess = "";
            if (pOp.equals("posxy"))
            {
                strX = pPosX;
                strY = pPosY;
                strAddRess = ATGetAddress(strX, strY, "");
               
            }
            else if (pOp.equals("addr"))
            {
                strX = Functions.Substring( ATGetAddress("", "",pAddress), "posx",1);
                strY = Functions.Substring(ATGetAddress("", "",pAddress), "posy", 1);
                strAddRess = pAddress;
                Functions.setCookie("x_point", strX, 1);
                Functions.setCookie("y_point", strY, 1);
                InitToolbar();
            }
            String strInfo = "X=" + strX + ";Y=" + strY + ";LABLE=地址：" + strAddRess + ";";
           //提交到前台的脚本不生效,改成model方式提交到前台触发
            //Functions.js_exec("addMarker_byResult('" + strInfo + "')");

            model.addAttribute("strResult", strInfo);
            myToolBar.render(model);
        }
        
        private void InitToolbar()
        {
            myToolBar.funName_OnClientClick("btnToolBarClick");
            if(pDrag.equals("1"))
                myToolBar.fill_fld("更新地址", "Address", 0, 10); 
            myToolBar.fill_fld("关闭", "Close", 0, 10);
            myToolBar.fill_toolStrip("plCommand");
        }

        /// <summary>
        /// 获取位置信息
        /// </summary>
        /// <param name="x">X值</param>
        /// <param name="y">Y值</param>
        /// <param name="address">位置信息描述</param>
        /// <returns>当传入的是xy时返回位置描述，当传入的是位置描述是返回xy坐标</returns>
        public String ATGetAddress(String x, String y,String address)
        {
            String strReturn = "";

            try
            {
                String strUrl = "";
                if (pOp.equals("posxy"))//当传入的是xy坐标时
                {
                    strUrl = "http://api.map.baidu.com/geocoder/v2/?ak=63d5d5de7d067945b2af4b2148850ad4&callback=renderReverse&location=" + y + "," + x + "&output=json&pois=1";
                }
                else if (pOp.equals("addr"))//当传入的是位置信息时
                {
                    strUrl = "http://api.map.baidu.com/geocoder/v2/?address=" + address + "&output=json&ak=63d5d5de7d067945b2af4b2148850ad4&callback=showLocation";
                }
                
               /* HttpWebRequest request = (HttpWebRequest)WebRequest.Create(strUrl);
                request.Method = "GET";
                request.Timeout = 5000;
                request.KeepAlive = true;

                HttpWebResponse response = (HttpWebResponse)request.GetResponse();
                StreamReader sRead = new StreamReader(response.GetResponseStream(), Encoding.UTF8);
                strReturn = sRead.ReadToEnd();
                sRead.Close();
                response.Close();*/
                
                strReturn=fun_http.sendGet(strUrl,"" );
                
                if (pOp.equals("posxy"))//当传入的是xy坐标时
                {
                    strReturn = Functions.Substring(strReturn, "\"formatted_address\":\"", "\",\"business");
                }
                else if (pOp.equals("addr"))//当传入的是位置信息时
                {
                    String strX = Functions.Substring(strReturn, "\"lng\":", ",\"lat\"");
                    String strY = Functions.Substring(strReturn, "\"lat\":", "},");
                    strReturn= "posx=" + strX + ";posy=" + strY + ";";
                }
            }
            catch (Exception ex)
            {
                strReturn = ex.getMessage();
            }

            return strReturn;
        }
    }

