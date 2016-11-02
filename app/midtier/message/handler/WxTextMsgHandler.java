package midtier.message.handler;

import java.util.Map;

import com.google.common.base.Preconditions;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.WxMpXmlOutMessage;

public class WxTextMsgHandler implements WxMpMessageHandler {
  @Override
  public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context, WxMpService wxMpService, WxSessionManager sessionManager)
      throws WxErrorException {
    Preconditions.checkArgument(wxMessage.getMsgType().equals(WxConsts.XML_MSG_TEXT));

    return WxMpXmlOutMessage.TEXT()
        .content(wxMessage.getContent())
        .build();
  }
}
