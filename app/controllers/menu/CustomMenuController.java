package controllers.menu;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import lombok.NonNull;
import lombok.SneakyThrows;
import me.chanjar.weixin.common.bean.menu.WxMenu;
import me.chanjar.weixin.common.bean.menu.WxMenuButton;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpMenuService;
import me.chanjar.weixin.mp.api.WxMpService;
import play.mvc.Controller;
import play.mvc.Result;

public class CustomMenuController extends Controller {

  @Inject private WxMpService service;

  /**
   * Advanced feature, need advanced interfaces permission from Wechat
   */
  @SneakyThrows(WxErrorException.class)
  public Result create() {
    WxMpMenuService menuService = service.getMenuService();
    WxMenu menu = new WxMenu();

    menu.setButtons(ImmutableList.of(
        createButton("start game", "click", "start", null),
        createButton("rankings", "click", "rankings", null),
        createButton("about", "view", "about", "www.google.com")
    ));

    menuService.menuCreate(menu);
    return ok("Successfully created");
  }

  private static WxMenuButton createButton(@NonNull String name, @NonNull String type, String key, String url) {
    WxMenuButton button = new WxMenuButton();
    button.setName(name);
    button.setType(type);

    // Optional
    button.setKey(key);
    button.setUrl(url);
    return button;
  }
}
