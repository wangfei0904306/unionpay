package com.example.demo;

import com.example.demo.union.DemoBase;
import com.example.demo.union.config.SDKConfig;
import com.example.demo.union.constants.SDKConstants;
import com.example.demo.union.service.AcpService;
import com.example.demo.union.util.CertUtil;
import com.example.demo.union.util.LogUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by gangsun on 2017/4/12.
 */
@Service
public class UnionPaymentServiceImpl implements UnionPaymentService {

    @PostConstruct
    public void  init(){
        System.out.println("银联支付初始化");
        SDKConfig.getConfig().loadPropertiesFromSrc(); //从classpath加载acp_sdk.properties文件
        CertUtil.init();
    }

    //先取商户号
    private String merId = "777290058110048";

    private String redictUrl = "http://www.baidu.com";


    /**
     * 支付
     * @param request
     * @param response
     */
    @Override
    public void pay(HttpServletRequest request, HttpServletResponse response) throws IOException {

        request.setCharacterEncoding(DemoBase.encoding);
        response.setContentType("text/html; charset="+ DemoBase.encoding);

        String orderId = String.valueOf(System.currentTimeMillis()); //实际上是orderSn
        String txnAmt = null;  //订单金额
        String txnTime = null; //订单发送时间
        //交易金额，单位分，不要带小数点
        BigDecimal amount = new BigDecimal("0.01");
        amount = amount.multiply(new BigDecimal(100)).setScale(0, BigDecimal.ROUND_HALF_UP);
        txnAmt = amount.toString();
        //订单发送时间用于查询用
        Date sendPaymentDate = new Date();
        txnTime = formatTime(sendPaymentDate);

        Map<String, String> requestData = new HashMap<>();

        /***银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改***/
        requestData.put("version", DemoBase.version);   			  //版本号，全渠道默认值
        requestData.put("encoding", DemoBase.encoding); 			  //字符集编码，可以使用UTF-8,GBK两种方式
        requestData.put("signMethod", SDKConfig.getConfig().getSignMethod()); //签名方法
        requestData.put("txnType", "01");               			  //交易类型 ，01：消费
        requestData.put("txnSubType", "01");            			  //交易子类型， 01：自助消费
        requestData.put("bizType", "000201");           			  //业务类型，B2C网关支付，手机wap支付
        requestData.put("channelType", "08");           			  //渠道类型，这个字段区分B2C网关支付和手机wap支付；07：PC,平板  08：手机

        /***商户接入参数***/
        requestData.put("merId", merId);    	          			  //商户号码，请改成自己申请的正式商户号或者open上注册得来的777测试商户号
        requestData.put("accessType", "0");             			  //接入类型，0：直连商户
        requestData.put("orderId", orderId);                          //商户订单号，8-40位数字字母，不能含“-”或“_”，可以自行定制规则
        requestData.put("txnTime", txnTime);        //订单发送时间，取系统时间，格式为YYYYMMDDhhmmss，必须取当前时间，否则会报txnTime无效
        requestData.put("currencyCode", "156");         			  //交易币种（境内商户一般是156 人民币）
        requestData.put("txnAmt", txnAmt);             			      //交易金额，单位分，不要带小数点
        //requestData.put("reqReserved", "透传字段");        		      //请求方保留域，如需使用请启用即可；透传字段（可以实现商户自定义参数的追踪）本交易的后台通知,对本交易的交易状态查询交易、对账文件中均会原样返回，商户可以按需上传，长度为1-1024个字节。出现&={}[]符号时可能导致查询接口应答报文解析失败，建议尽量只传字母数字并使用|分割，或者可以最外层做一次base64编码(base64编码之后出现的等号不会导致解析失败可以不用管)。

        //前台通知地址 （需设置为外网能访问 http https均可），支付成功后的页面 点击“返回商户”按钮的时候将异步通知报文post到该地址
        //如果想要实现过几秒中自动跳转回商户页面权限，需联系银联业务申请开通自动返回商户权限
        //异步通知参数详见open.unionpay.com帮助中心 下载  产品接口规范  网关支付产品接口规范 消费交易 商户通知
        requestData.put("frontUrl", DemoBase.frontUrl);

        //后台通知地址（需设置为【外网】能访问 http https均可），支付成功后银联会自动将异步通知报文post到商户上送的该地址，失败的交易银联不会发送后台通知
        //后台通知参数详见open.unionpay.com帮助中心 下载  产品接口规范  网关支付产品接口规范 消费交易 商户通知
        //注意:1.需设置为外网能访问，否则收不到通知    2.http https均可  3.收单后台通知后需要10秒内返回http200或302状态码
        //    4.如果银联通知服务器发送通知后10秒内未收到返回状态码或者应答码非http200，那么银联会间隔一段时间再次发送。总共发送5次，每次的间隔时间为0,1,2,4分钟。
        //    5.后台通知地址如果上送了带有？的参数，例如：http://abc/web?a=b&c=d 在后台通知处理程序验证签名之前需要编写逻辑将这些字段去掉再验签，否则将会验签失败
        requestData.put("backUrl", DemoBase.backUrl);

        // 订单超时时间。
        // 超过此时间后，除网银交易外，其他交易银联系统会拒绝受理，提示超时。 跳转银行网银交易如果超时后交易成功，会自动退款，大约5个工作日金额返还到持卡人账户。
        // 此时间建议取支付时的北京时间加15分钟。
        // 超过超时时间调查询接口应答origRespCode不是A6或者00的就可以判断为失败。
        requestData.put("payTimeout", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date().getTime() + 15 * 60 * 1000));

        //////////////////////////////////////////////////
        //
        //       报文中特殊用法请查看 PCwap网关跳转支付特殊用法.txt
        //
        //////////////////////////////////////////////////

        /**请求参数设置完毕，以下对请求参数进行签名并生成html表单，将表单写入浏览器跳转打开银联页面**/
        Map<String, String> submitFromData = AcpService.sign(requestData,DemoBase.encoding);  //报文中certId,signature的值是在signData方法中获取并自动赋值的，只要证书配置正确即可。

        String requestFrontUrl = SDKConfig.getConfig().getFrontRequestUrl();  //获取请求银联的前台地址：对应属性文件acp_sdk.properties文件中的acpsdk.frontTransUrl
        String html = AcpService.createAutoFormHtml(requestFrontUrl, submitFromData,DemoBase.encoding);   //生成自动跳转的Html表单

        LogUtil.writeLog("打印请求HTML，此为请求报文，为联调排查问题的依据："+html);
        //将生成的html写到浏览器中完成自动跳转打开银联支付页面；这里调用signData之后，将html写到浏览器跳转到银联页面之前均不能对html中的表单项的名称和值进行修改，如果修改会导致验签不通过
        response.getWriter().write(html);


    }

    /**
     * 后台回调
     * @param request
     * @param response
     */
    @Override
    public void backRcvResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LogUtil.writeLog("BackRcvResponse接收后台通知开始");

        String encoding = request.getParameter(SDKConstants.param_encoding);
        // 获取银联通知服务器发送的后台通知参数
        Map<String, String> reqParam = getAllRequestParam(request);

        LogUtil.printRequestLog(reqParam);

        Map<String, String> valideData = null;
        if (null != reqParam && !reqParam.isEmpty()) {
            Iterator<Map.Entry<String, String>> it = reqParam.entrySet().iterator();
            valideData = new HashMap<String, String>(reqParam.size());
            while (it.hasNext()) {
                Map.Entry<String, String> e = it.next();
                String key = (String) e.getKey();
                String value = (String) e.getValue();
                value = new String(value.getBytes(encoding), encoding);
                valideData.put(key, value);
            }
        }

        //重要！验证签名前不要修改reqParam中的键值对的内容，否则会验签不过
        if (!AcpService.validate(valideData, encoding)) {
            LogUtil.writeLog("验证签名结果[失败].");
            //验签失败，需解决验签问题

        } else {
            LogUtil.writeLog("验证签名结果[成功].");
            //【注：为了安全验签成功才应该写商户的成功处理逻辑】交易成功，更新商户订单状态

            String orderId =valideData.get("orderId"); //获取后台通知的数据，其他字段也可用类似方式获取
            String orderSn =orderId; //orderId其实存的是Sn

            String respCode = valideData.get("respCode");
            String txnAmt = valideData.get("txnAmt");
            BigDecimal txnAmount = (new BigDecimal(txnAmt)).multiply(new BigDecimal(0.01));

            String queryId = valideData.get("queryId");
            String traceTime = valideData.get("traceTime");
            String payCardNo = valideData.get("payCardNo");
            String payCardType = valideData.get("payCardType"); //支付卡类型
            String paymentMethodMethod; //PayPaymentMethod里面的method字段
            if(StringUtils.isEmpty(payCardType)){
                paymentMethodMethod = "UNION";  //对之前代码做兼容，如果没有支付卡类型的情况走默认
            }else{
                paymentMethodMethod = "UNION-" + payCardType;
            }

            //判断respCode=00、A6后，对涉及资金类的交易，请再发起查询接口查询，确定交易成功后更新数据库。
            if("00".equals(respCode)){  // 00 交易成功

                //todo 若交易成功
            }else if("A6".equals(respCode)){  // A6 部分成功

            }

        }
        LogUtil.writeLog("BackRcvResponse接收后台通知结束");
        //返回给银联服务器http 200  状态码
        response.getWriter().print("ok");
    }

    @Override
    public void frontRcvResponse(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        LogUtil.writeLog("FrontRcvResponse前台接收报文返回开始");

        String encoding = request.getParameter(SDKConstants.param_encoding);
        LogUtil.writeLog("返回报文中encoding=[" + encoding + "]");
        String pageResult = "";
        if (DemoBase.encoding.equalsIgnoreCase(encoding)) {
            pageResult = "/utf8_result.jsp";
        } else {
            pageResult = "/gbk_result.jsp";
        }
        Map<String, String> respParam = getAllRequestParam(request);

        // 打印请求报文
        LogUtil.printRequestLog(respParam);

        Map<String, String> valideData = null;
        StringBuffer page = new StringBuffer();
        if (null != respParam && !respParam.isEmpty()) {
            Iterator<Map.Entry<String, String>> it = respParam.entrySet()
                    .iterator();
            valideData = new HashMap<String, String>(respParam.size());
            while (it.hasNext()) {
                Map.Entry<String, String> e = it.next();
                String key = (String) e.getKey();
                String value = (String) e.getValue();
                value = new String(value.getBytes(encoding), encoding);
                page.append("<tr><td width=\"30%\" align=\"right\">" + key
                        + "(" + key + ")</td><td>" + value + "</td></tr>");
                valideData.put(key, value);
            }
        }
        if (!AcpService.validate(valideData, encoding)) {
            page.append("<tr><td width=\"30%\" align=\"right\">验证签名结果</td><td>失败</td></tr>");
            LogUtil.writeLog("验证签名结果[失败].");
        } else {
            page.append("<tr><td width=\"30%\" align=\"right\">验证签名结果</td><td>成功</td></tr>");
            LogUtil.writeLog("验证签名结果[成功].");
            System.out.println(valideData.get("orderId")); //其他字段也可用类似方式获取

            String respCode = valideData.get("respCode");
            //判断respCode=00、A6后，对涉及资金类的交易，请再发起查询接口查询，确定交易成功后更新数据库。
        }
        request.setAttribute("result", page.toString());
        request.getRequestDispatcher(pageResult).forward(request, response);

        LogUtil.writeLog("FrontRcvResponse前台接收报文返回结束");

    }

    @Override
    public void query(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String orderId = String.valueOf(System.currentTimeMillis()); //实际上是orderSn
        String txnTime = null; //订单发送时间

        //订单发送时间
        Map<String, String> data = new HashMap<String, String>();

        /***银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改***/
        data.put("version", DemoBase.version);                 //版本号
        data.put("encoding", DemoBase.encoding);               //字符集编码 可以使用UTF-8,GBK两种方式
        data.put("signMethod", SDKConfig.getConfig().getSignMethod()); //签名方法
        data.put("txnType", "00");                             //交易类型 00-默认
        data.put("txnSubType", "00");                          //交易子类型  默认00
        data.put("bizType", "000201");                         //业务类型 B2C网关支付，手机wap支付

        /***商户接入参数***/
        data.put("merId", merId);                  //商户号码，请改成自己申请的商户号或者open上注册得来的777商户号测试
        data.put("accessType", "0");                           //接入类型，商户接入固定填0，不需修改

        /***要调通交易以下字段必须修改***/
        data.put("orderId", orderId);                 //****商户订单号，每次发交易测试需修改为被查询的交易的订单号
        data.put("txnTime", txnTime);                 //****订单发送时间，每次发交易测试需修改为被查询的交易的订单发送时间

        /**请求参数设置完毕，以下对请求参数进行签名并发送http post请求，接收同步应答报文------------->**/

        Map<String, String> reqData = AcpService.sign(data,DemoBase.encoding);//报文中certId,signature的值是在signData方法中获取并自动赋值的，只要证书配置正确即可。

        String url = SDKConfig.getConfig().getSingleQueryUrl();// 交易请求url从配置文件读取对应属性文件acp_sdk.properties中的 acpsdk.singleQueryUrl
        //这里调用signData之后，调用submitUrl之前不能对submitFromData中的键值对做任何修改，如果修改会导致验签不通过
        Map<String, String> rspData = AcpService.post(reqData,url,DemoBase.encoding);

        /**对应答码的处理，请根据您的业务逻辑来编写程序,以下应答码处理逻辑仅供参考------------->**/
        //应答码规范参考open.unionpay.com帮助中心 下载  产品接口规范  《平台接入接口规范-第5部分-附录》
        if(!rspData.isEmpty()){
            if(AcpService.validate(rspData, DemoBase.encoding)){
                LogUtil.writeLog("验证签名成功");
                if("00".equals(rspData.get("respCode"))){//如果查询交易成功
                    //处理被查询交易的应答码逻辑
                    String origRespCode = rspData.get("origRespCode");
                    if("00".equals(origRespCode)){
                        //交易成功，更新商户订单状态
                        //TODO
                    }else if("03".equals(origRespCode) ||
                            "04".equals(origRespCode) ||
                            "05".equals(origRespCode)){
                        //需再次发起交易状态查询交易
                        //TODO
                    }else{
                        //其他应答码为失败请排查原因
                        //TODO
                    }
                }else{//查询交易本身失败，或者未查到原交易，检查查询交易报文要素
                    //TODO
                }
            }else{
                LogUtil.writeErrorLog("验证签名失败");
                //TODO 检查验证签名失败的原因
            }
        }else{
            //未返回正确的http状态
            LogUtil.writeErrorLog("未获取到返回报文或返回http状态码非200");
        }
        String reqMessage = DemoBase.genHtmlResult(reqData);
        String rspMessage = DemoBase.genHtmlResult(rspData);
        response.getWriter().write("</br>请求报文:<br/>"+reqMessage+"<br/>" + "应答报文:</br>"+rspMessage+"");

    }

    @Override
    public void check(Long shopOrderId){

        String orderId = String.valueOf(System.currentTimeMillis()); //实际上是orderSn
        String txnTime = null; //订单发送时间

        Map<String, String> data = new HashMap<String, String>();

        /***银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改***/
        data.put("version", DemoBase.version);                 //版本号
        data.put("encoding", DemoBase.encoding);               //字符集编码 可以使用UTF-8,GBK两种方式
        data.put("signMethod", SDKConfig.getConfig().getSignMethod()); //签名方法
        data.put("txnType", "00");                             //交易类型 00-默认
        data.put("txnSubType", "00");                          //交易子类型  默认00
        data.put("bizType", "000201");                         //业务类型 B2C网关支付，手机wap支付

        /***商户接入参数***/
        data.put("merId", merId);                  //商户号码，请改成自己申请的商户号或者open上注册得来的777商户号测试
        data.put("accessType", "0");                           //接入类型，商户接入固定填0，不需修改

        /***要调通交易以下字段必须修改***/
        data.put("orderId", orderId);                 //****商户订单号，每次发交易测试需修改为被查询的交易的订单号
        data.put("txnTime", txnTime);                 //****订单发送时间，每次发交易测试需修改为被查询的交易的订单发送时间

        /**请求参数设置完毕，以下对请求参数进行签名并发送http post请求，接收同步应答报文------------->**/

        Map<String, String> reqData = AcpService.sign(data,DemoBase.encoding);//报文中certId,signature的值是在signData方法中获取并自动赋值的，只要证书配置正确即可。
        LogUtil.writeLog("查询请求数据： " + reqData.toString());

        String url = SDKConfig.getConfig().getSingleQueryUrl();// 交易请求url从配置文件读取对应属性文件acp_sdk.properties中的 acpsdk.singleQueryUrl
        //这里调用signData之后，调用submitUrl之前不能对submitFromData中的键值对做任何修改，如果修改会导致验签不通过
        Map<String, String> rspData = AcpService.post(reqData,url,DemoBase.encoding);
        LogUtil.writeLog("查询响应数据： " + rspData.toString());

        /**对应答码的处理，请根据您的业务逻辑来编写程序,以下应答码处理逻辑仅供参考------------->**/
        //应答码规范参考open.unionpay.com帮助中心 下载  产品接口规范  《平台接入接口规范-第5部分-附录》
        if(!rspData.isEmpty()){
            if(AcpService.validate(rspData, DemoBase.encoding)){
                LogUtil.writeLog("验证签名成功");
                if("00".equals(rspData.get("respCode"))){//如果查询交易成功
                    //处理被查询交易的应答码逻辑
                    String origRespCode = rspData.get("origRespCode");
                    if("00".equals(origRespCode)){
                        //交易成功，更新商户订单状态
                        String txnAmt = rspData.get("txnAmt");
                        BigDecimal txnAmount = null;
                        if(!StringUtils.isEmpty(txnAmt)){
                            txnAmount = (new BigDecimal(txnAmt)).multiply(new BigDecimal(0.01));  //分转换为元;
                        }
                        String queryId = rspData.get("queryId");
                        String traceTime = rspData.get("traceTime");
                        String payCardNo = rspData.get("payCardNo");
                        String payCardType = rspData.get("payCardType"); //支付卡类型
                        String paymentMethodMethod; //PayPaymentMethod里面的method字段
                        if(StringUtils.isEmpty(payCardType)){
                            paymentMethodMethod = "UNION";  //对之前代码做兼容，如果没有支付卡类型的情况走默认
                        }else{
                            paymentMethodMethod = "UNION-" + payCardType;
                        }

                        //更改支付状态

                        //改变工单状态
                    }else if("03".equals(origRespCode) ||
                            "04".equals(origRespCode) ||
                            "05".equals(origRespCode)){
                        //需再次发起交易状态查询交易
                        throw new RuntimeException("查询结果：订单号" + orderId + "交易失败，应答码为“" + origRespCode + "”");
                    }else{
                        //其他应答码为失败请排查原因
                        throw new RuntimeException("查询结果：订单号" + orderId + "交易失败，应答码为“" + origRespCode + "”");
                    }
                }else{//查询交易本身失败，或者未查到原交易，检查查询交易报文要素
                    throw new RuntimeException("查询失败");
                }
            }else{
                LogUtil.writeErrorLog("验证签名失败");
                throw new RuntimeException("验证签名失败");
                //TODO 检查验证签名失败的原因
            }
        }else{
            //未返回正确的http状态
            LogUtil.writeErrorLog("未获取到返回报文或返回http状态码非200");
            throw new RuntimeException("未获取到返回报文或返回http状态码非200");
        }

    }

    @Override
    public void successRedict(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(redictUrl);
    }


    //---------------------------------------------------- private -----------------------------------------------------

    /**
     * 获取请求参数中所有的信息
     *
     * @param request
     * @return
     */
    private static Map<String, String> getAllRequestParam(final HttpServletRequest request) {
        Map<String, String> res = new HashMap<String, String>();
        Enumeration<?> temp = request.getParameterNames();
        if (null != temp) {
            while (temp.hasMoreElements()) {
                String en = (String) temp.nextElement();
                String value = request.getParameter(en);
                res.put(en, value);
                //在报文上送时，如果字段的值为空，则不上送<下面的处理为在获取所有参数数据时，判断若值为空，则删除这个字段>
                //System.out.println("ServletUtil类247行  temp数据的键=="+en+"     值==="+value);
                if (null == res.get(en) || "".equals(res.get(en))) {
                    res.remove(en);
                }
            }
        }
        return res;
    }



    //收费比率 精确到分保留两位小数四舍五入
    private BigDecimal getFeeAmount(BigDecimal amount, BigDecimal feeRatio, BigDecimal feeMax) {
        BigDecimal fee = new BigDecimal(0);
        if(null == amount || null == feeRatio) return fee;
        //金额乘以费率 = 手续费
        fee = amount.multiply(feeRatio);
        //最大值为feeMax
        if(null != feeMax && feeMax.compareTo(new BigDecimal("0")) >= 0) fee = fee.max(feeMax);
        //设置精确到分并四舍五入
        fee = fee.setScale(4, BigDecimal.ROUND_HALF_UP);
        return fee;
    }

    /**
     * 时间格式化
     * @param date
     * @return
     */
    private static String formatTime(Date date){
        return new SimpleDateFormat("yyyyMMddHHmmss").format(date);
    }
    private static Date formatTime(String dateStr){
        if(null == dateStr) return null;
        if(dateStr.length() == 14){
            try {
                return new SimpleDateFormat("yyyyMMddHHmmss").parse(dateStr);
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        }else{
            try {
                return new SimpleDateFormat("MMddHHmmss").parse(dateStr);
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }

        }

    }


}
