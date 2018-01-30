# WechatMagician

_Read this README.md in other languages:_  [English](https://github.com/Gh0u1L5/WechatMagician/blob/master/README.en.md).

WechatMagician是一款骚兮兮的Xposed插件，致力于让用户彻底掌控微信上的聊天消息及朋友圈内容，目前支持到微信 6.6.1 版，实现的功能有：
1. 防止微信好友撤回聊天消息（撤回提示可自定义）。
2. 防止微信好友删除朋友圈动态、评论。
3. 发送消息时可选图片的上限从9张上调至用户设定的数量（默认1000张）。
4. 转发聊天消息时可选好友人数从9人上调至无限。
5. 在“选择联系人”界面增加全选按钮。
6. 一键转发他人朋友圈（支持纯文字、图文、视频、链接等）。
7. 单条朋友圈一键截图。
8. 设定朋友圈关键字黑名单，免除广告与被秀恩爱的烦恼。
9. 自动确认电脑端登录请求（开启该功能即视为用户同意自行承担可能的安全风险）。

__注：使用朋友圈转发/截图功能时，请长按好友头像下方的空白处。__

## QQ群 / 微信群

官方QQ群：
* 一群：135955386（已满）
* 二群：157550472

官方微信群：添加微信账号 "XposedHelper" 发送关键词 “微信巫师”

## 设计理念

本项目在吸取其他微信插件经验教训的基础上希望实现以下几个小目标：
* __稳定__ —— 绝大部分微信插件每逢更新必崩溃，许多老旧插件已经无法在新版微信上运行。
  - 本项目将每个小功能都拆分到单独的模块中，单个模块失效不会影响其他功能的使用。
  - 本项目使用自行设计的一套[API](https://github.com/Gh0u1L5/WechatMagician/blob/master/src/main/kotlin/com/gh0u1l5/wechatmagician/util/PackageUtil.kt)，通过比对特征来定位关键类、关键方法。
  - 本项目选用的[每一条特征](https://github.com/Gh0u1L5/WechatMagician/blob/master/src/main/kotlin/com/gh0u1l5/wechatmagician/backend/WechatPackage.kt)都保证在6.5.3至6.6.1版本中稳定存在，即使失效也能在短时间内进行修复。
  - 本项目Hook的位置尽量贴近底层，通过牺牲一定的运行速度保证注入位置的稳定性。
* __简洁__ —— 大型插件如微X模块有相当多的不常用功能，而这些不常用功能往往是闪退和崩溃的罪魁祸首。
  - 如果将不同的功能交由不同的插件实现，用户选择自己需要的插件安装，将大大提升开发者和用户双方的体验。
  - 本项目目前的定位是“让用户彻底掌控聊天消息及朋友圈内容”，具体来讲就是专注于防撤回、防删除、转发消息、转发朋友圈等操作。
  - 但同时本项目也是一个易拓展的 __开源微信插件模板__ ，在其中实现了对UI创建、XML解析、文件读写、文件加解密、数据库读写等关键操作的稳定控制。
  - 若开发者想提交与项目当前主题不符的功能（如抢红包等），可以开一个单独的Fork或Branch。
* __开源__ —— 闭源只会耗尽个人开发者的耐心，摧毁一个项目的生命力。
  - 本项目保证永久开源，欢迎提交PR，但是请不要提交明显用于非法用途的功能。
  - 如果微信团队致信说明某功能被大量运用于非法用途或严重侵害插件使用者权益，那么该功能将会被移除。

## 效果预览
<img src="https://github.com/Gh0u1L5/WechatMagician/raw/master/image/sample-1.zh.png" width="40%" /> <img src="https://github.com/Gh0u1L5/WechatMagician/raw/master/image/sample-2.zh.png" width="40%" />

<img src="https://github.com/Gh0u1L5/WechatMagician/raw/master/image/sample-3.zh.png" width="40%" /> <img src="https://github.com/Gh0u1L5/WechatMagician/raw/master/image/sample-4.zh.png" width="40%" />

<img src="https://github.com/Gh0u1L5/WechatMagician/raw/master/image/sample-5.zh.png" width="40%" /> <img src="https://github.com/Gh0u1L5/WechatMagician/raw/master/image/sample-6.zh.png" width="40%" />

<img src="https://github.com/Gh0u1L5/WechatMagician/raw/master/image/sample-7.zh.png" width="40%" /> <img src="https://github.com/Gh0u1L5/WechatMagician/raw/master/image/sample-8.zh.png" width="40%" />

<img src="https://github.com/Gh0u1L5/WechatMagician/raw/master/image/interface-1.zh.png" width="40%" /> <img src="https://github.com/Gh0u1L5/WechatMagician/raw/master/image/interface-2.zh.png" width="40%" />

<img src="https://github.com/Gh0u1L5/WechatMagician/raw/master/image/interface-3.zh.png" width="40%" /> <img src="https://github.com/Gh0u1L5/WechatMagician/raw/master/image/interface-4.zh.png" width="40%" />

## 特别鸣谢
* 感谢 @rovo89 编写的Xposed框架
* 感谢 @rarnu 编写的防撤回插件wechat_no_revoke
