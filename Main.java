public class Main  {
    
    public static void main(String[] args) {
    
    boolean chooice = true;
    
    if (chooice) { 
    	String transCode = "QACCBAL";
    	String groupCIS = "400090001604411";
    	String id = "ncrp.y.4000";
    	String packageID = String.valueOf(System.currentTimeMillis());
    	String in = "<in><TotalNum>1</TotalNum><BLFlag>0</BLFlag><SynFlag>0</SynFlag><rd><iSeqno>" + packageID + "</iSeqno><AccNo>4000023029200124946</AccNo><CurrType></CurrType><ReqReserved3></ReqReserved3><AcctSeq></AcctSeq></rd></in>";
    	String xml = "<?xml version=\"1.0\" encoding=\"GBK\"?><CMS><eb><pub><TransCode>" + transCode + "</TransCode><CIS>" + groupCIS + "</CIS><BankCode>102</BankCode><ID>" + id + "</ID><TranDate>20170703</TranDate><TranTime>120359123</TranTime><fSeqno>" + packageID + "</fSeqno></pub>" + in + "</eb></CMS>";
    	System.out.println(IcbcCbsUtil.query(false, xml, transCode, groupCIS, id, packageID));
     } else {
    	String content = "<body><lists name='acctList'><list><acctNo>6224080400151</acctNo></list></lists></body>";
    	String transCode = "4402";
    	String masterID = "2000040752";
      System.out.println(SpdbCbsUtil.query(content, transCode, masterID));
     } 
    }

}
