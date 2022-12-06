package com.example.tradingbotfuturesapp.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("tradingbot")
public class TradingBotFuturesAppController 
{
    static String publicKey = "pCWsMXEuG1EOaowoVgqYcoIaWDazkUw6mbdnoNJvi9DX3Q7g7C7dNKTFVS8vfWwK";
    static String privateKey = "xB4iF9VWsKRxRRYFbpidPlrxVpmyXii2UWBx0VhlH2yOpKHXg5fGmFog8DkESSER";
    
    @RequestMapping(value = "/testConnectivity", method = RequestMethod.GET, produces = "text/html")
    public String testConnectivity() throws IOException, InterruptedException
    {
        String command = "curl -v -X GET https://fapi.binance.com/fapi/v1/ping";
        return commandExecution(command);
    }
    
    private long checkServerTime() throws IOException, InterruptedException
    {
        String command = "curl -v -X GET https://fapi.binance.com/fapi/v1/time";
        String serverTime = commandExecution(command);
        return new JSONObject(serverTime).getLong("serverTime");
    }
    
    @RequestMapping(value = "/exchangeInformation", method = RequestMethod.GET, produces = "text/html")
    public String exchangeInformation() throws IOException, InterruptedException
    {
        String command = "curl -v -X GET https://fapi.binance.com/fapi/v1/exchangeInfo";
        return commandExecution(command);
    }
    
    @RequestMapping(value = "/orderBook", method = RequestMethod.GET, produces = "text/html")
    public String orderBook(@RequestParam("symbol") String symbol,@RequestParam("limitOPT") String limitOPT) throws IOException, InterruptedException
    {
        String command = "curl -v -X GET https://fapi.binance.com/fapi/v1/depth?symbol="+symbol+"&limit="+limitOPT;
        return commandExecution(command);
    }
    
    @RequestMapping(value = "/recentTradesList", method = RequestMethod.GET, produces = "text/html")
    public String recentTradesList(@RequestParam("symbol") String symbol,@RequestParam("limitOPT") String limitOPT) throws IOException, InterruptedException
    {
        String command = "curl -v -X GET https://fapi.binance.com/fapi/v1/trades?symbol="+symbol+"&limit="+limitOPT;
        return commandExecution(command);
    }
    
    @RequestMapping(value = "/oldTradeLookup", method = RequestMethod.GET, produces = "text/html")
    public String oldTradeLookup(String publicKey, @RequestParam("symbol") String symbol, @RequestParam("limitOPT") String limitOPT, @RequestParam("fromIdOPT") String fromIdOPT) throws IOException, InterruptedException
    {
        String command = "curl -v -H \"X-MBX-APIKEY: "+publicKey+"\" -X GET https://fapi.binance.com/fapi/v1/historicalTrades?symbol="+symbol+"&limit="+limitOPT+"&fromId="+fromIdOPT;
        return commandExecution(command);
    }
    
    @RequestMapping(value = "/compressedAggregatedTradeList", method = RequestMethod.GET, produces = "text/html")
    public String compressedAggregatedTradeList(@RequestParam("symbol") String symbol, long fromId, long startTime, long endTime, @RequestParam("limitOPT") String limitOPT) throws IOException, InterruptedException
    {
        String command = "curl -v -X GET https://fapi.binance.com/fapi/v1/aggTrades?symbol="+symbol+"&limit="+limitOPT;
        return commandExecution(command);
    }
    
    @RequestMapping(value = "/klineCandlestickData", method = RequestMethod.GET, produces = "text/html")
    public String klineCandlestickData(@RequestParam("symbol") String symbol, @RequestParam("interval") String interval) throws IOException, InterruptedException
    {
        String command = "curl -v -X GET https://fapi.binance.com/fapi/v1/klines?symbol="+symbol+"&interval="+interval+"&limit=2";
        return commandExecution(command);
    }
    
    @RequestMapping(value = "/currentAvgPrice", method = RequestMethod.GET, produces = "text/html")
    public String currentAvgPrice(@RequestParam("symbol") String symbol) throws IOException, InterruptedException
    {
        String command = "curl -v -X GET https://fapi.binance.com/fapi/v1/avgPrice?symbol="+symbol;
        return commandExecution(command);
    }
    
    @RequestMapping(value = "/tickerPriceChangeStatistics", method = RequestMethod.GET, produces = "text/html")
    public String tickerPriceChangeStatistics(@RequestParam("symbol") String symbol) throws IOException, InterruptedException
    {
        String command = "curl -v -X GET https://fapi.binance.com/fapi/v1/ticker/24hr";
        return commandExecution(command);
    }
    
    @RequestMapping(value = "/symbolPriceTicker", method = RequestMethod.GET, produces = "text/html")
    public String symbolPriceTicker(@RequestParam("symbol") String symbol) throws IOException, InterruptedException
    {
        String command = "curl -v -X GET https://fapi.binance.com/fapi/v1/ticker/price?symbol="+symbol;
        return commandExecution(command);
    }
    
    @RequestMapping(value = "/symbolOrderBookTicker", method = RequestMethod.GET, produces = "text/html")
    public String symbolOrderBookTicker(@RequestParam("symbol") String symbol) throws IOException, InterruptedException
    {
        String command = "curl -v -X GET https://fapi.binance.com/fapi/v1/ticker/bookTicker?symbol="+symbol;
        return commandExecution(command);
    }
    
    @RequestMapping(value = "/initialLeverage", method = RequestMethod.GET, produces = "text/html")
    public String initialLeverage(@RequestParam("symbol") String symbol) throws NoSuchAlgorithmException, InvalidKeyException, IOException, InterruptedException
    {
        long recvWindow = 5000;
        long timeStamp = this.checkServerTime();
        String queryString = "symbol="+symbol+"&leverage=1&recvWindow="+recvWindow+"&timestamp="+timeStamp;
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(this.privateKey.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secretKeySpec);
        String signature = new String(Hex.encodeHex(sha256_HMAC.doFinal(queryString.getBytes())));
        String command = "curl -v -H \"X-MBX-APIKEY: "+this.publicKey+"\" -X POST \"https://fapi.binance.com/fapi/v1/leverage?"+queryString+"&signature="+signature+"\" ";
        return commandExecution(command);
    }
        
    /*http://localhost:8091/tradingbot/newMarketOrder?symbol=BTCUSDT&side=SELL&positionSide=LONG&type=MARKET&quantity=0.001*/
    @RequestMapping(value = "/newMarketOrder", method = RequestMethod.GET, produces = "text/html")
    public String newMarketOrder(@RequestParam("symbol") String symbol, @RequestParam("side") String side, @RequestParam("positionSide") String positionSide, @RequestParam("type") String type,
            @RequestParam("quantity") String quantity, 
            String quoteOrderQtyOPT, String newClientOrderIdOPT, String icebergQtyOPT, String newOrderRespTypeOPT) throws NoSuchAlgorithmException, InvalidKeyException, IOException, InterruptedException
    {
        long recvWindow = 5000;
        long timeStamp = this.checkServerTime();
        String queryString = "symbol="+symbol+"&side="+side+"&positionSide="+positionSide+"&type="+type+"&quantity="+quantity+"&recvWindow="+recvWindow+"&timestamp="+timeStamp;
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(this.privateKey.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secretKeySpec);
        String signature = new String(Hex.encodeHex(sha256_HMAC.doFinal(queryString.getBytes())));
        String command = "curl -v -H \"X-MBX-APIKEY: "+this.publicKey+"\" -X POST \"https://fapi.binance.com/fapi/v1/order?"+queryString+"&signature="+signature;
        return commandExecution(command);
    }
   
    @RequestMapping(value = "/marginType", method = RequestMethod.GET, produces = "text/html")
    public String marginType(@RequestParam("symbol") String symbol) throws NoSuchAlgorithmException, InvalidKeyException, IOException, InterruptedException
    { 
        long recvWindow = 6000;
        long timeStamp = this.checkServerTime();
        String queryString = "symbol="+symbol+"&marginType=ISOLATED&recvWindow="+recvWindow+"&timestamp="+timeStamp;
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(this.privateKey.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secretKeySpec);
        String signature = new String(Hex.encodeHex(sha256_HMAC.doFinal(queryString.getBytes())));
        String command = "curl -v -H \"X-MBX-APIKEY: "+publicKey+"\" -X POST \"https://fapi.binance.com/fapi/v1/marginType?"+queryString+"&signature="+signature+"\" ";
        return commandExecution(command);
    }
    
    @RequestMapping(value = "/modifyIsolatedPositionMargin", method = RequestMethod.GET, produces = "text/html")
    public String modifyIsolatedPositionMargin(@RequestParam("symbol") String symbol) throws NoSuchAlgorithmException, InvalidKeyException, IOException, InterruptedException
    { 
        long recvWindow = 6000;
        long timeStamp = this.checkServerTime();
        String queryString = "symbol="+symbol+"&amount=0.001&type=2&recvWindow="+recvWindow+"&timestamp="+timeStamp;
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(this.privateKey.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secretKeySpec);
        String signature = new String(Hex.encodeHex(sha256_HMAC.doFinal(queryString.getBytes())));
        String command = "curl -v -H \"X-MBX-APIKEY: "+publicKey+"\" -X POST \"https://fapi.binance.com/fapi/v1/positionMargin?"+queryString+"&signature="+signature+"\" ";
        return commandExecution(command);
    }
    
   @RequestMapping(value = "/testNewOrder", method = RequestMethod.GET, produces = "text/html")
    public String testNewOrder(@RequestParam("symbol") String symbol, @RequestParam("side") String side, @RequestParam("type") String type, 
            @RequestParam("timeInForce") String timeInForce, @RequestParam("quantity") String quantity, String quoteOrderQtyOPT, 
            @RequestParam("price") String price, @RequestParam("stopPrice") String stopPrice, String newClientOrderIdOPT, 
            String icebergQtyOPT, String newOrderRespTypeOPT) throws NoSuchAlgorithmException, InvalidKeyException, IOException, InterruptedException
    { 
        long recvWindow = 6000;
        long timeStamp = this.checkServerTime();
        String queryString = "";
        if(type.equals("TAKE_PROFIT")){queryString = "symbol="+symbol+"&side="+side+"&type="+type+"&quantity="+quantity+"&price="+price+"&stopPrice="+stopPrice+"&timeInForce=GTC&recvWindow="+recvWindow+"&timestamp="+timeStamp;}
        if(type.equals("STOP")){queryString = "symbol="+symbol+"&side="+side+"&type="+type+"&quantity="+quantity+"&price="+price+"&stopPrice="+stopPrice+"&recvWindow="+recvWindow+"&timestamp="+timeStamp;}
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(this.privateKey.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secretKeySpec);
        String signature = new String(Hex.encodeHex(sha256_HMAC.doFinal(queryString.getBytes())));
        String command = "curl -v -H \"X-MBX-APIKEY: "+publicKey+"\" -X POST \"https://api.binance.com/api/v3/order/test?"+queryString+"&signature="+signature+"\" ";
        return commandExecution(command);
    }
    
    @RequestMapping(value = "/queryOrder", method = RequestMethod.GET, produces = "text/html")
     public String queryOrder(@RequestParam("symbol") String symbol, String orderIdOPT, 
             String origClientOrderIdOPT) throws NoSuchAlgorithmException, InvalidKeyException, IOException, InterruptedException
    {
        // Signature method
        long recvWindow = 6000;
        long timeStamp = this.checkServerTime();
        String queryString = "symbol="+symbol+"&recvWindow="+recvWindow+"&timestamp="+timeStamp;
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(this.privateKey.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secretKeySpec);
        String signature = new String(Hex.encodeHex(sha256_HMAC.doFinal(queryString.getBytes())));
        String command = "curl -v -H \"X-MBX-APIKEY: "+publicKey+"\" -X GET \"https://fapi.binance.com/fapi/v1/order?"+queryString+"&signature="+signature+"\" ";
        return commandExecution(command);
    }
    
     @RequestMapping(value = "/cancelOrder", method = RequestMethod.GET, produces = "text/html")
     public String cancelOrder(@RequestParam("symbol") String symbol, 
             @RequestParam("orderId") String orderId, String origClientOrderIdOPT, 
             String newClientOrderIdOPT) throws NoSuchAlgorithmException, InvalidKeyException, IOException, InterruptedException
     {
        // Signature method
        long recvWindow = 6000;
        long timeStamp = this.checkServerTime();
        String queryString = "symbol="+symbol+"&orderId="+orderId+"&recvWindow="+recvWindow+"&timestamp="+timeStamp;
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(this.privateKey.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secretKeySpec);
        String signature = new String(Hex.encodeHex(sha256_HMAC.doFinal(queryString.getBytes())));
        String command = "curl -v -H \"X-MBX-APIKEY: "+publicKey+"\" -X DELETE \"https://fapi.binance.com/fapi/v1/order?"+queryString+"&signature="+signature+"\" ";
        return commandExecution(command);
     }
     
     @RequestMapping(value = "/cancelAllOpenOrders", method = RequestMethod.GET, produces = "text/html")
     public String cancelallOpenOrders(@RequestParam("symbol") String symbol, 
             String orderIdOPT, String origClientOrderIdOPT, String newClientOrderIdOPT) throws NoSuchAlgorithmException, InvalidKeyException, IOException, InterruptedException
     {
        // Signature method
        long recvWindow = 6000;
        long timeStamp = this.checkServerTime();
        String queryString = "symbol="+symbol+"&recvWindow="+recvWindow+"&timestamp="+timeStamp;
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(this.privateKey.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secretKeySpec);
        String signature = new String(Hex.encodeHex(sha256_HMAC.doFinal(queryString.getBytes())));
        String command = "curl -v -H \"X-MBX-APIKEY: "+publicKey+"\" -X DELETE \"https://fapi.binance.com/fapi/v1/allOpenOrders?"+queryString+"&signature="+signature+"\" ";
        return commandExecution(command);
     }
     
     @RequestMapping(value = "/countdownCancelAllOpenOrders", method = RequestMethod.GET, produces = "text/html")
          public String countdownCancelAllOpenOrders(@RequestParam("symbol") String symbol, @RequestParam("countdownTime") String countdownTime, 
             String orderIdOPT, String origClientOrderIdOPT, String newClientOrderIdOPT) throws NoSuchAlgorithmException, InvalidKeyException, IOException, InterruptedException
     {
        // Signature meth
        long recvWindow = 6000;
        long timeStamp = this.checkServerTime();
        String queryString = "symbol="+symbol+"&countdownTime="+countdownTime+"&recvWindow="+recvWindow+"&timestamp="+timeStamp;
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(this.privateKey.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secretKeySpec);
        String signature = new String(Hex.encodeHex(sha256_HMAC.doFinal(queryString.getBytes())));
        String command = "curl -v -H \"X-MBX-APIKEY: "+publicKey+"\" -X POST \"https://fapi.binance.com/fapi/v1/countdownCancelAll?"+queryString+"&signature="+signature+"\" ";
        return commandExecution(command);
     }
     
     @RequestMapping(value = "/currentOpenOrders", method = RequestMethod.GET, produces = "text/html")
     public String currentOpenOrders(@RequestParam("symbol") String symbol) throws NoSuchAlgorithmException, InvalidKeyException, IOException, InterruptedException
     {  
         // Signature method
        long recvWindow = 6000;
        long timeStamp = this.checkServerTime();
        String queryString = "symbol="+symbol+"&recvWindow="+recvWindow+"&timeStamp="+timeStamp;
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(privateKey.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secretKeySpec);
        String signature = new String(Hex.encodeHex(sha256_HMAC.doFinal(queryString.getBytes())));
        String command = "curl -v -H \"X-MBX-APIKEY: "+publicKey+"\" -X GET https://fapi.binance.com/fapi/v1/openOrders?"+queryString+"&signature="+signature+"\" ";
        return commandExecution(command);
     }
     
     @RequestMapping(value = "/allOrders", method = RequestMethod.GET, produces = "text/html")
     public String allOrders(@RequestParam("symbol") String symbol) throws NoSuchAlgorithmException, InvalidKeyException, IOException, InterruptedException
     {  
         // Signature method
        long recvWindow = 6000;
        long timeStamp = this.checkServerTime();
        String queryString = "symbol="+symbol+"&limit=3&recvWindow="+recvWindow+"&timestamp="+timeStamp;
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(privateKey.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secretKeySpec);
        String signature = new String(Hex.encodeHex(sha256_HMAC.doFinal(queryString.getBytes())));
        String command = "curl -v -H \"X-MBX-APIKEY: "+publicKey+"\" -X GET https://fapi.binance.com/fapi/v1/allOrders?"+queryString+"&signature="+signature+"\" ";
        return commandExecution(command);
     }
    
     @RequestMapping(value = "/newOCOTrade", method = RequestMethod.GET, produces = "text/html")
      public String newOCOTrade(@RequestParam("symbol") String symbol, String listClientOrderIdOPT, 
                @RequestParam("side") String side, @RequestParam("quantity") String quantity, int limitClientOrderIdOPT, 
                @RequestParam("price") String price, String limitIcebergQtyOPT, String stopClientOrderIdOPT, 
                @RequestParam("stopPrice") String stopPrice, int stopLimitPriceOPT, int stopIcebergQtyOPT, 
                String stopLimitTimeInForceQTY, String newOrderRespTypeOPT) throws InvalidKeyException, NoSuchAlgorithmException, IOException, InterruptedException
    {
        // Signature method
        long recvWindow = 6000;
        long timeStamp = this.checkServerTime();
        String queryString = "symbol="+symbol+"&side="+side+"&quantity="+quantity+"&price="+price+"&stopPrice="+stopPrice+"&recvWindow="+recvWindow+"&timestamp="+timeStamp;
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(this.privateKey.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secretKeySpec);
        String signature = new String(Hex.encodeHex(sha256_HMAC.doFinal(queryString.getBytes())));
        String command = "curl -v -H \"X-MBX-APIKEY: "+this.publicKey+"\" -X POST \"https://fapi.binance.com/fapi/v1/order/oco?"+queryString+"&signature="+signature+"\" ";
        return commandExecution(command);
    }
     
      @RequestMapping(value = "/queryOCO", method = RequestMethod.GET, produces = "text/html")
    public String queryOCO(String orderListIdOPT, @RequestParam("origClientOrderId") String origClientOrderId) throws InvalidKeyException, NoSuchAlgorithmException, IOException, InterruptedException
    {
        long recvWindow = 6000;
        long timeStamp = this.checkServerTime();
        String queryString = "&origClientOrderId="+origClientOrderId+"&recvWindow="+recvWindow+"&timestamp="+timeStamp;
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(this.privateKey.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secretKeySpec);
        String signature = new String(Hex.encodeHex(sha256_HMAC.doFinal(queryString.getBytes())));
        String command = "curl -v -H \"X-MBX-APIKEY: "+this.publicKey+"\" -X GET https://fapi.binance.com/fapi/v1/orderList?"+queryString+"&signature="+signature+"\" ";
        return commandExecution(command);
    }
    
    @RequestMapping(value = "/queryAllOCO", method = RequestMethod.GET, produces = "text/html")
    public String queryAllOCO(@RequestParam("fromIdOPT") long fromIdOPT, 
            long startTime, long endTime, int limit) throws NoSuchAlgorithmException, InvalidKeyException, IOException, InterruptedException
    {
        long recvWindow = 6000;
        long timeStamp = this.checkServerTime();   
        String queryString = "fromId="+fromIdOPT+"&recvWindow="+recvWindow+"&timestamp="+timeStamp;
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(this.privateKey.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secretKeySpec);
        String signature = new String(Hex.encodeHex(sha256_HMAC.doFinal(queryString.getBytes())));
        String command = "curl -v -H \"X-MBX-APIKEY: "+this.publicKey+"\" -X GET https://fapi.binance.com/fapi/v1/allOrderList?"+queryString+"&signature="+signature+"\" ";
        return commandExecution(command);
    }
    
    @RequestMapping(value = "/queryOpenOCO", method = RequestMethod.GET, produces = "text/html")
    public String queryOpenOCO() throws NoSuchAlgorithmException, InvalidKeyException, IOException, InterruptedException
    {
        long recvWindow = 6000;
        long timeStamp = this.checkServerTime();
        String queryString = "&recvWindow="+recvWindow+"&timestamp="+timeStamp;
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(this.privateKey.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secretKeySpec);
        String signature = new String(Hex.encodeHex(sha256_HMAC.doFinal(queryString.getBytes())));
        String command = "curl -v -H \"X-MBX-APIKEY: "+this.publicKey+"\" -X GET https://fapi.binance.com/fapi/v1/openOrderList?"+queryString+"&signature="+signature+"\" ";
        return commandExecution(command);
    }
    
    @RequestMapping(value = "/accountInformation", method = RequestMethod.GET, produces = "text/html")
    public String accountInformation() throws NoSuchAlgorithmException, InvalidKeyException, IOException, InterruptedException
    {
        long recvWindow = 6000;
        long timeStamp = this.checkServerTime();
        String queryString = "&recvWindow="+recvWindow+"&timestamp="+timeStamp;
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(this.privateKey.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secretKeySpec);
        String signature = new String(Hex.encodeHex(sha256_HMAC.doFinal(queryString.getBytes())));
        String command = "curl -v -H \"X-MBX-APIKEY: "+this.publicKey+"\" -X GET https://fapi.binance.com/fapi/v1/account?"+queryString+"&signature="+signature+"\" ";
        return commandExecution(command);   
    }
    
    @RequestMapping(value = "/accountTradeList", method = RequestMethod.GET, produces = "text/html")
    public String accountTradeList(@RequestParam("symbol") String symbol, long startTime, long endTime,
        long fromId, int limit) throws NoSuchAlgorithmException, InvalidKeyException, IOException, InterruptedException
    {
        long recvWindow = 6000;
        long timeStamp = this.checkServerTime();
        String queryString = "symbol="+symbol+"&recvWindow="+recvWindow+"&timestamp="+timeStamp;
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(privateKey.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secretKeySpec);
        String signature = new String(Hex.encodeHex(sha256_HMAC.doFinal(queryString.getBytes())));
        String command = "curl -v -H \"X-MBX-APIKEY: "+publicKey+"\" -X GET https://fapi.binance.com/fapi/v1/myTrades?"+queryString+"&signature="+signature+"\" ";
        return commandExecution(command);    
    }

    // User data stream endpoints
    @RequestMapping(value = "/userDataStream", method = RequestMethod.GET, produces = "text/html")
    public String startUserDataStream() throws IOException, InterruptedException
    {
        String command = "curl -v -H \"X-MBX-APIKEY: "+publicKey+"\" -X POST https://fapi.binance.com/fapi/v1/listenKey";
        String dataStream = commandExecution(command);
        return new JSONObject(dataStream).getString("listenKey");  
    }
    
    @RequestMapping(value = "/keepUserDataStream", method = RequestMethod.GET, produces = "text/html")
    public String keepUserDataStream(String listenKey) throws IOException, InterruptedException
    { 
        String command = "curl -v -H \"X-MBX-APIKEY: "+publicKey+"\" -X PUT https://fapi.binance.com/fapi/v1/listenKey="+listenKey;
        return commandExecution(command);    
    }
    
    @RequestMapping(value = "/deleteUserDataStream", method = RequestMethod.GET, produces = "text/html")
    public String deleteUserDataStream(String publicKey, String listenKey) throws IOException, InterruptedException
    { 
        String command = "curl -v -H \"X-MBX-APIKEY: "+publicKey+"\" -X DELETE https://fapi.binance.com/fapi/v1/listenKey="+listenKey;
        return commandExecution(command);    
    }
   
    public String commandExecution(String command) throws IOException
    {
        String result = "";
        Process process = Runtime.getRuntime().exec(command);
        InputStream procIS = process.getInputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(procIS));
        String send = "";
        while ((send = reader.readLine()) != null) {
            System.out.println(send);
            result+=send;
        }
            
        reader.close();
            
        process.destroy();
        if (process.exitValue() != 0) {
            System.out.println("Abnormal process termination destroy");
        }
    
        return result;
    }
}
