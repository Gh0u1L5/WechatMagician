# WechatMagician

_Read this README.md in other languages:_  [English](https://github.com/Gh0u1L5/WechatMagician/blob/master/README.en.md).

WechatMagician是一款骚兮兮的Xposed插件，底层使用 __[WechatSpellbook](https://github.com/Gh0u1L5/WechatSpellbook)__ 框架，致力于让用户彻底掌控微信上的聊天消息及朋友圈内容，支持微信 6.5.3 及以上版本。

## 快速上手

第一次clone项目的时候，记得加上```--recursive ```参数，因为WechatMagican依赖 __[WechatSpellbook](https://github.com/Gh0u1L5/WechatSpellbook)__ 框架。
``` bash
git clone --recursive https://github.com/Gh0u1L5/WechatMagician.git
```

如果已经clone过了，就执行以下命令直接更新一次组件。
``` bash
git submodule update --init --recursive
```

同理，如果是自己下载ZIP包解压的情况，就需要再去下载WechatSpellbook的代码，解压到spellbook文件夹中。

## 实现功能

#### 聊天相关
1. 防止微信好友撤回聊天消息（撤回提示可自定义）。
2. 转发消息时可选择任意数量好友。
3. 发送消息时可发送9张以上的图片（上限可调整，默认1000张）。
4. 隐藏不常用的群聊至群聊助手。（需在设置界面手动开启）
5. 设定好友为密友，隐藏聊天痕迹。（需在设置界面手动开启）
6. 一键标记所有聊天对话为已读。

#### 朋友圈相关
1. 防止微信好友删除朋友圈动态、评论。
2. 屏蔽微信在朋友圈投放的广告。
3. 一键转发他人朋友圈。
4. 单条朋友圈一键截图。
5. 按关键字屏蔽朋友圈，免除广告与秀恩爱的烦恼。（需在设置界面手动开启）

#### 其他杂项
1. 自动确认电脑端登录请求（开启该功能即视为用户同意自行承担可能的安全风险）。

## QQ群 / 微信群
官方QQ群：
* 一群：135955386（已满）
* 二群：157550472

官方微信群：
1. 添加微信账号 "XposedHelper" 发送关键词 “微信巫师”
2. 添加微信账号 "CSYJZF"

## 设计理念

本项目在吸取其他微信插件经验教训的基础上希望实现以下几个小目标：
* __稳定__ —— 绝大部分微信插件每逢更新必崩溃，许多老旧插件已经无法在新版微信上运行。
  - 本项目将每个小功能都拆分到单独的模块中，单个模块失效不会影响其他功能的使用。
  - 本项目使用自行设计的一套[API](https://github.com/Gh0u1L5/WechatSpellbook/blob/master/src/main/kotlin/com/gh0u1l5/wechatmagician/spellbook/util/ReflectionUtil.kt)，通过比对特征来定位关键类、关键方法。
  - 本项目选用的[每一条特征](https://github.com/Gh0u1L5/WechatSpellbook/tree/master/src/main/kotlin/com/gh0u1l5/wechatmagician/spellbook/mirror/)都保证自 6.5.3 版本开始稳定存在，即使失效也能在短时间内进行修复。
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
