import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


public class SpdbCbsUtil {
	
	public static final String SPDB_IP = "127.0.0.1";
	
	public static final int SPDB_SIGNATURE_SOCKET = 4437;
	
	public static final int SPDB_QUERY_SOCKET = 5777;

	/**
	 * 查询账户信息
	 * @param content 需要签名的报文
	 * @param transCode 交易码
	 * @param masterID 客户号
	 * @return 查询结果
	 */
	public static String query(String content, String transCode, String masterID) {
		//拼接请求签名报文
		Document document = Jsoup.parse(getResponse(SPDB_IP, SPDB_SIGNATURE_SOCKET, content, "GBK", true));
		//读取签名内容
		String signature = document.select("sign").text();
		//拼接查询报文
		String datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		String query = "<?xml version='1.0' encoding='GB2312'?><packet><head><transCode>" + transCode + "</transCode><signFlag>1</signFlag><masterID>" + masterID + "</masterID><packetID>" + System.currentTimeMillis() + "</packetID><timeStamp>" + datetime + "</timeStamp></head><body><signature>" + signature + "</signature></body></packet>";
		String length = (String.valueOf(query.length() + 6) + "      ").substring(0, 6);
		String data = length + query;
		Document document2 = Jsoup.parse(getResponse(SPDB_IP, SPDB_QUERY_SOCKET, data, "GBK", true));
		//读取查询结果
		String returnCode = document2.select("returnCode").eq(0).text();
		if ("AAAAAAA".equals(returnCode)) {
			Document document3 = Jsoup.parse(getResponse(SPDB_IP, SPDB_SIGNATURE_SOCKET, document2.select("signature").text(), "GBK", false));
			return document3.select("sic").html();
		} else {
			return "错误码【" + returnCode + "】：错误信息描述【" + document2.select("returnMsg").text() + "】";
		}
	}
	
	/**
	 * 连接CBS请求数据
	 * @param ip CBS的IP
	 * @param socket CBS的端口号
	 * @param content 请求报文
	 * @param encoding 编码格式
	 * @param signature 签名和查询为true，验签为false
	 * @return CBS返回的报文
	 */
	public static String getResponse(String ip, int socket, String content, String encoding, boolean signature) {
		StringBuffer sb = new StringBuffer();
		try {
			URL url = new URL("http://" + ip + ":" + socket + "/");
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			String type = signature ? "INFOSEC_SIGN/1.0" : "INFOSEC_VERIFY_SIGN/1.0";
			connection.setRequestProperty("Content-Type", type);
			connection.setRequestProperty("Content-Length", String.valueOf(content.length()));
			DataOutputStream out =new DataOutputStream(connection.getOutputStream());
			out.write(content.getBytes(encoding));
			out.flush();
			out.close();
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), encoding));
			String line;  
		    while ((line = in.readLine()) != null) {
		    	sb.append(line);
		    }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
}
