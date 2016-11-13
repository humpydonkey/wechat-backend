package midtier.message;

public class MsgConstants {

  public static final int TOP_N = 10;

  public static final String REQUEST_NAME = "姓名";
  public static final String REQUEST_GAME_RULE = "游戏规则";
  public static final String REQUEST_RANKINGS = "排行榜";

  public static final String RESPONSE_GAME_RULE = "游戏规则:"
      + "1."
      + "2."
      + "3."
      + "4."
      + "回复'姓名:张三',可以更改姓名为张三;\n"
      + "回复'排行榜',可以查看最新排行榜信息;\n";

  public static final String RESPONSE_SET_NAME_SUCCESSFUL = "姓名设置成功";

  public static final String RESPONSE_MSG_SCORED = "您的祝福语'%s'得了%s分";

  public static final String RESPONSE_ERROR_ILLEGAL_FORMAT = "您的格式不满足要求,请换一个格式重新再来";
}
