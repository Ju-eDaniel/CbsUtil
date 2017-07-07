import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.sun.org.apache.xml.internal.security.utils.Base64;

public class IcbcCbsUtil  {
    
  public static final String ICBC_IP = "127.0.0.1";
	
	public static final int ICBC_SIGNATURE_SOCKET = 449;
	
	public static final int ICBC_QUERY_SOCKET = 448;

	/**
	 * 查询流程处理
	 * @param signature 需要签名则为true
	 * @param xml 请求的xml报文
	 * @param transCode 交易码
	 * @param groupCIS 集团CIS
	 * @param id 用户ID
	 * @param packageID 包序列ID
	 * @return 处理过查询结果
	 */
	public static String query(boolean signature, String xml, String transCode, String groupCIS, String id, String packageID) {
		String sign = null;
		if (signature) {
			//拼接请求签名报文
			Document document = Jsoup.parse(getResponse(true, xml, ""));
			//读取签名内容
			sign = document.select("sign").text();
		}
		String sendTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		StringBuffer sb = new StringBuffer();
		sb.append("Version=0.0.1.0");
		sb.append("&TransCode=" + transCode);
		sb.append("&BankCode=102");
		sb.append("&GroupCIS=" + groupCIS);
		sb.append("&ID=" + id);
		sb.append("&PackageID=" + packageID);
		sb.append("&SendTime=" + sendTime);
		sb.append("&reqData=" + (signature ? sign : xml));
		String result = getResponse(false, sb.toString(), "/servlet/ICBCCMPAPIReqServlet?userID=" + id + "&PackageID=" + packageID + "&SendTime=" + sendTime);
		try {
			if (result.startsWith("reqData=")) {
				String decodeResult = new String(Base64.decode(result.substring(8)), "GBK");
				Document document = Jsoup.parse(decodeResult);
				String retCode = document.select("RetCode").text();
				if ("0".equals(retCode)) {
					return document.select("out").html();
				} else {
					return "错误代码：" + retCode + "；错误说明：" + document.select("RetMsg").text();
				}
			} else {
				return "错误代码：" + new String(Base64.decode(result.substring(10)), "GBK");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "数据解码错误";
		}
	}
    
	/**
	 * 执行查询请求
	 * @param signature 签名操作则为true
	 * @param content 请求传输的内容
	 * @param extension URL扩展
	 * @return 查询结果
	 */
    public static String getResponse(boolean signature, String content, String extension) {
        try {
        	StringBuffer sb = new StringBuffer();
        	byte data[] = content.getBytes("GBK");
        	URL url = new URL("http://" + ICBC_IP + ":" + (signature ? ICBC_SIGNATURE_SOCKET : ICBC_QUERY_SOCKET) + "/" + extension);
        	HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        	connection.setRequestMethod("POST");
        	connection.setDoInput(true);
        	connection.setDoOutput(true);
        	connection.setUseCaches(false);
        	connection.setRequestProperty("Charset", "GBK");
        	connection.setRequestProperty("Content-Type", signature ? "INFOSEC_SIGN/1.0" : "application/x-www-form-urlencoded");
        	connection.setRequestProperty("Content-Length", String.valueOf(data.length));
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());
			out.write(data);
			out.flush();
			out.close();
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "GBK"));
			String line;  
		    while ((line = in.readLine()) != null) {
		    	sb.append(line);
		    }
		    return sb.toString();
        } catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    }

}
