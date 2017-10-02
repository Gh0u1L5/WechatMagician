# WechatMagician

WechatMagician is a cool Xposed module designed for Chinese social media application Wechat, to help the users get the ultimate control of their messages and moments. This module implemented following features:
1. Prevent friends from recalling messages.
2. Prevent friends from deleting moments or comments.
3. Loose the limit on number of photos to send.
4. Bypass the limit on number of recipients when retransmitting messages.
5. Add a button "Select All" in "Select Contacts" interface.
6. Retweet other's moments (only works for text moments or image moments).
7. Take screenshot of a single moment.

__P.S. If you want to retweet or take screenshot, please long press the blank area under others' avatar.__

Currently this module supports WeChat 6.5.3 ~ 6.5.16.

## Design
After learning from the failure of the famous WeXposed module, this project wants to do a better job in the following aspects:
* __Stability__: WeXposed crashes for almost every Wechat update. Currently the compatible version for WeXposed is Wechat 6.5.8, which has some attractive features missing.
  - This project wraps each feature into a small "unit"; a single unit can crash safely without ruining the whole module.
  - This project has [a set of APIs](https://github.com/Gh0u1L5/WechatMagician/blob/master/src/main/kotlin/com/gh0u1l5/wechatmagician/util/PackageUtil.kt) to analyze and match the signatures of critical classes / methods.
  - This project picks only [the signatures](https://github.com/Gh0u1L5/WechatMagician/blob/master/src/main/kotlin/com/gh0u1l5/wechatmagician/xposed/WechatPackage.kt) that exist from Wechat 6.5.3 to 6.5.14. Even if a signature is broken in the coming Wechat updates, it can be fixed easily.
  - This project hooks the methods close to system components / platform tools. This sacrifices some speed but ensures some stable break points.
* __Simplicity__: WeXposed has many functions that are hardly used by most of the users, but most of the crashes are caused by those functions.
  - If those functions are implemented by different modules, and the users can just install the modules as they need, then our lives would be much more easier.
  - This project aims to "help the users get the ultimate control of their messages and moments"; more specifically, it wants to help the users to prevent recalling / deleting, retransmit messages, retweet moments, etc.
  - However, in the meantime, this project is also __an open-source template for Wechat modules__. With current framework you can easily hook several critical operations in Wechat including UI updates, XML parsing, file I/O and database operations.
  - If you have developed something that is unrelated to current purpose of this project, you may still get a standalone branch for your contribution.
* __Open Source__: WeXposed taught us a great lesson that closed-source will only ruin a non-profit project and its developer.
  - This project will stay open-source, everyone is more than welcome to submit pull requests. Just don't submit codes for illegal purpose.
  - If the Wechat team formally states that a specific feature has been misused for illegal purpose, it will be removed immediately.

## Preview
<img src="https://github.com/Gh0u1L5/WechatMagician/raw/master/image/sample-1.en.png" width="40%" /> <img src="https://github.com/Gh0u1L5/WechatMagician/raw/master/image/sample-2.en.png" width="40%" />

<img src="https://github.com/Gh0u1L5/WechatMagician/raw/master/image/sample-3.en.png" width="40%" /> <img src="https://github.com/Gh0u1L5/WechatMagician/raw/master/image/sample-4.en.png" width="40%" />

<img src="https://github.com/Gh0u1L5/WechatMagician/raw/master/image/sample-5.en.png" width="40%" /> <img src="https://github.com/Gh0u1L5/WechatMagician/raw/master/image/sample-6.en.png" width="40%" />

<img src="https://github.com/Gh0u1L5/WechatMagician/raw/master/image/sample-7.en.png" width="40%" /> <img src="https://github.com/Gh0u1L5/WechatMagician/raw/master/image/sample-8.en.png" width="40%" />

## Credits
* Thanks @rovo89 for the awesome Xposed framework.
* Thanks @rarnu for the prototype wechat_no_revoke.
