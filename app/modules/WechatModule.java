package modules;

import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import midtier.message.handler.WxTextMsgHandler;
import midtier.message.handler.WxVoiceMsgHandler;

import com.google.inject.AbstractModule;

public class WechatModule extends AbstractModule {

  @Override
  protected void configure() {
    WxMpInMemoryConfigStorage config = new WxMpInMemoryConfigStorage();
    config.setAppId("wxa9a0942a7abcbd4d");
    config.setSecret("f18031cc1477e757d7badf126fcc2881"); // app corpSecret
    config.setToken("happybirthday");
    config.setAesKey("UbHfGVroWBYypehZiyfQEJvB8zHVId52Z8xNBYIO1Gw"); // EncodingAESKey

    WxMpService wxService = new WxMpServiceImpl();
    wxService.setWxMpConfigStorage(config);

    bind(WxMpService.class).toInstance(wxService);

    WxMpMessageRouter wxMpMessageRouter = new WxMpMessageRouter(wxService);
    configureMessageRouter(wxMpMessageRouter);
    bind(WxMpMessageRouter.class).toInstance(wxMpMessageRouter);
  }

  // encrypt and put them in application.conf
  //  wxMpConfig.appId="wxa9a0942a7abcbd4d"
  //  wxMpConfig.secret="f18031cc1477e757d7badf126fcc2881"
  //  wxMpConfig.token="happybirthday"
  //  wxMpConfig.aesKey="UbHfGVroWBYypehZiyfQEJvB8zHVId52Z8xNBYIO1Gw"

  private void configureMessageRouter(WxMpMessageRouter router) {
    router.rule().async(false).msgType(WxConsts.XML_MSG_VOICE).handler(new WxVoiceMsgHandler()).end();
    router.rule().async(false).msgType(WxConsts.XML_MSG_TEXT).handler(new WxTextMsgHandler()).end();
  }
}
