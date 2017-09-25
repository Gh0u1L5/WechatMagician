# WechatMagician

_Read this README.md in other languages:_  [English](https://github.com/Gh0u1L5/WechatMagician/blob/master/README.en.md).

WechatMagician是一款炫酷的Xposed插件，致力于让用户彻底掌控微信上的聊天消息及朋友圈内容，目前它实现的功能有：
1. 防止微信好友撤回聊天消息。
2. 防止微信好友删除朋友圈动态、评论。
3. 移除转发人数上限为9人的限制。
4. 在“选择联系人”界面增加全选按钮。

目前支持最新微信 6.5.14 版， __官方交流QQ群：135955386__

## 设计理念

本项目在吸取 微X模块 经验教训的基础上希望实现以下几个小目标：
* __稳定__ —— 微X模块每逢更新必崩溃，目前能稳定使用微X模块的微信版本为2017年5月初发布的6.5.8，无法使用大量小程序API和多项新功能。
  - 本项目将每个小功能都拆分到单独的模块中，单个模块失效不会影响其他功能的使用。
  - 本项目使用自行设计的一套[API](https://github.com/Gh0u1L5/WechatMagician/blob/master/src/main/kotlin/com/gh0u1l5/wechatmagician/util/PackageUtil.kt)，通过比对特征来定位关键类、关键方法。
  - 本项目选用的[每一条特征](https://github.com/Gh0u1L5/WechatMagician/blob/master/src/main/kotlin/com/gh0u1l5/wechatmagician/xposed/WechatPackage.kt)都保证在6.5.3至6.5.14版本中稳定存在，即使失效也能在短时间内进行修复。
  - 本项目Hook的位置尽量贴近底层，通过牺牲一定的运行速度保证注入位置的稳定性。
* __简洁__ —— 微X模块有相当多的不常用功能，而这些不常用功能往往是闪退和崩溃的罪魁祸首。
  - 如果将不同的功能交由不同的插件实现，用户选择自己需要的插件安装，将大大提升开发者和用户双方的体验。
  - 本项目目前的定位是“让用户彻底掌控聊天消息及朋友圈内容”，具体来讲就是专注于防撤回、防删除、转发消息、转发朋友圈等操作。
  - 但同时本项目也是一个易拓展的 __开源微信插件模板__ ，在其中实现了对UI创建、XML解析、文件读写、数据库读写等关键操作的稳定控制。
  - 若开发者想提交与项目当前主题不符的功能（如抢红包等），可以开一个单独的Fork或Branch。
* __开源__ —— 微X模块实力证明了闭源只会耗尽个人开发者的耐心，摧毁一个项目的生命力。
  - 本项目保证永久开源，欢迎提交PR，但是请不要提交明显用于非法用途的功能。
  - 如果微信团队致信说明某功能被大量运用于非法用途或严重侵害插件使用者权益，那么该功能将会被移除。

## 效果预览
<img src="https://github.com/Gh0u1L5/WechatMagician/raw/master/image/sample-1.zh.png" width="40%" /> <img src="https://github.com/Gh0u1L5/WechatMagician/raw/master/image/sample-2.zh.png" width="40%" />

<img src="https://github.com/Gh0u1L5/WechatMagician/raw/master/image/sample-3.zh.png" width="40%" /> <img src="https://github.com/Gh0u1L5/WechatMagician/raw/master/image/sample-4.zh.png" width="40%" />

## 特别鸣谢
* 感谢 @rovo89 编写的Xposed框架
* 感谢 @rarnu 编写的防撤回插件wechat_no_revoke, 虽然 @rarnu 的代码让我直接删了（笑）。
