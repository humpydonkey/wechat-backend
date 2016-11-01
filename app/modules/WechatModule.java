package modules;

import com.google.inject.name.Names;
import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

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
    wxMpMessageRouter.rule().async(false).end();
    bind(WxMpMessageRouter.class).toInstance(wxMpMessageRouter);
  }

  // encrypt and put them in application.conf
  //  wxMpConfig.appId="wxa9a0942a7abcbd4d"
  //  wxMpConfig.secret="f18031cc1477e757d7badf126fcc2881"
  //  wxMpConfig.token="happybirthday"
  //  wxMpConfig.aesKey="UbHfGVroWBYypehZiyfQEJvB8zHVId52Z8xNBYIO1Gw"
}
