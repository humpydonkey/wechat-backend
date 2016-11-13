package midtier.message;

public class MsgConstants {

  public static final int TOP_N = 10;

  public static final String REQUEST_NAME = "姓名";
  public static final String REQUEST_GAME_RULE = "游戏规则";
  public static final String REQUEST_RANKINGS = "排行榜";

  public static final String RESPONSE_GAME_END = "时间到,游戏结束!";

  public static final String RESPONSE_GAME_RULE = "游戏规则:\n"
      + "生日祝福语游戏\n"
      + "1.通过打字或语音给本微信号发送祝福语,系统会根据您的祝福语的吉祥程度打分,得分最高的前十人有红包奖励\n"
      + "2.每个人可以发送多条祝福语,既可以语音,也可以打字\n"
      + "3.完全相同的祝福语只会显示一次\n"
      + "4.游戏会在周日下午一点准时自动结束\n\n"
      + "回复'姓名 张三',可以更改姓名为张三\n"
      + "回复'排行榜',可以查看最新排行榜信息\n"
      + "回复'游戏规则',可以查看游戏规则\n";

  public static final String RESPONSE_SET_NAME_SUCCESSFUL = "姓名设置成功";

  public static final String RESPONSE_MSG_SCORED = "您的祝福语'%s'得了%s分";

  public static final String RESPONSE_ERROR_ILLEGAL_FORMAT = "您的格式不满足要求,请换一个格式重新再来";
  public static final String RESPONSE_ERROR_MSG_TOO_LONG = "不好意思,您的发的祝福语字数太多了,最多可以发100个字";
}
