1.使用 mvn install 将项目打为jar包
2.将 lib文件夹 和 jar包取出 (只改代码的情况下只用发布jar)
3.将微信证书置于 application-other.properties 中#微信证书配置地址 wxzsAddress=C:/txtdConfig/
4.配置支付宝微信的公私钥和appid到 application-other.properties 中 (需要和医院的HID对应 HID不能重复)