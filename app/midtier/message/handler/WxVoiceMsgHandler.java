package midtier.message.handler;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.WxMpXmlOutMessage;

import com.google.common.base.Preconditions;

@Slf4j
public class WxVoiceMsgHandler implements WxMpMessageHandler {

  @Override
  public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context, WxMpService wxMpService, WxSessionManager sessionManager)
      throws WxErrorException {
    Preconditions.checkArgument(wxMessage.getMsgType().equals(WxConsts.XML_MSG_VOICE));

//    WxMpMaterialService materialService = wxMpService.getMaterialService();
//    File file = materialService.mediaDownload(wxMessage.getMediaId());
//    log.info("Download to: {}", file.getAbsolutePath());

    return WxMpXmlOutMessage.TEXT()
        .toUser(wxMessage.getFromUser())
        .fromUser(wxMessage.getToUser())
        .content(wxMessage.getContent())
//        .mediaId(wxMessage.getMediaId())
        .build();
  }
}
