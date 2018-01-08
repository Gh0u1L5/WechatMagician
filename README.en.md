# WechatMagician

WechatMagician is a fancy Xposed module designed for Chinese social media application Wechat, to help the users get the ultimate control of their messages and moments. Currently this module supports WeChat version up to 6.5.16. It has implemented following features:
1. Prevent friends from recalling messages (with customizable notification).
2. Prevent friends from deleting moments or comments.
3. Allow user to set the limit on number of photos to send (default value is 1000).
4. Bypass the limit on number of recipients when retransmitting messages.
5. Add a button "Select All" in "Select Contacts" interface.
6. Retweet other's moments (which can be text, image, video or links).
7. Take screenshot of a single moment.
8. Set blacklist for moments, and say goodbye to ads and PDA couples.
9. Automatically confirm the login requests from Wechat PC client (take your own risk if you turn on this function).

__P.S. If you want to retweet or take screenshots, please long press the blank area under others' avatar.__

## Official QQ Group
* Group One: 135955386 (Full)
* Group Two: 157550472

## Design
After learning from the failure of other Wechat modules, this project wants to do a better job in the following aspects:
* __Stability__: Most of those modules crashes for every Wechat update due to the obfuscator used by Wechat.
  - This project wraps each feature into a small "unit"; a single unit can crash safely without ruining the whole module.
  - This project has [a set of APIs](https://github.com/Gh0u1L5/WechatMagician/blob/master/src/main/kotlin/com/gh0u1l5/wechatmagician/util/PackageUtil.kt) to analyze and match the signatures of critical classes / methods.
  - This project picks only [the signatures](https://github.com/Gh0u1L5/WechatMagician/blob/master/src/main/kotlin/com/gh0u1l5/wechatmagician/backend/WechatPackage.kt) that exist from Wechat 6.5.3 to 6.5.16. Even if a signature is broken in the coming Wechat updates, it can be fixed easily.
  - This project hooks the methods close to system components / platform tools. This sacrifices some speed but ensures some stable break points.
* __Simplicity__: Large modules like WeXposed have many functions that are hardly used by most of the users, but most of the crashes are caused by those functions.
  - If those functions are implemented by different modules, and the users can just install the modules as they need, then our lives would be much more easier.
  - This project aims to "help the users get the ultimate control of their messages and moments"; more specifically, it wants to help the users to prevent recalling / deleting, retransmit messages, retweet moments, etc.
  - However, in the meantime, this project is also __an open-source template for Wechat modules__. With current framework you can easily hook several critical operations in Wechat including UI updates, XML parsing, file I/O, encryption/decryption engine and database operations.
  - If you have developed something that is unrelated to current purpose of this project, you may still get a standalone branch for your contribution.
* __Open Source__: Closed-source will only ruin a non-profit project and its developer.
  - This project will stay open-source, everyone is more than welcome to submit pull requests. Just don't submit codes for illegal purpose.
  - If the Wechat team formally states that a specific feature has been misused for illegal purpose, it will be removed immediately.

## Preview
<img src="https://github.com/Gh0u1L5/WechatMagician/raw/master/image/sample-1.en.png" width="40%" /> <img src="https://github.com/Gh0u1L5/WechatMagician/raw/master/image/sample-2.en.png" width="40%" />

<img src="https://github.com/Gh0u1L5/WechatMagician/raw/master/image/sample-3.en.png" width="40%" /> <img src="https://github.com/Gh0u1L5/WechatMagician/raw/master/image/sample-4.en.png" width="40%" />

<img src="https://github.com/Gh0u1L5/WechatMagician/raw/master/image/sample-5.en.png" width="40%" /> <img src="https://github.com/Gh0u1L5/WechatMagician/raw/master/image/sample-6.en.png" width="40%" />

<img src="https://github.com/Gh0u1L5/WechatMagician/raw/master/image/sample-7.en.png" width="40%" /> <img src="https://github.com/Gh0u1L5/WechatMagician/raw/master/image/sample-8.en.png" width="40%" />

<img src="https://github.com/Gh0u1L5/WechatMagician/raw/master/image/interface-1.en.png" width="40%" /> <img src="https://github.com/Gh0u1L5/WechatMagician/raw/master/image/interface-2.en.png" width="40%" />

<img src="https://github.com/Gh0u1L5/WechatMagician/raw/master/image/interface-3.en.png" width="40%" /> <img src="https://github.com/Gh0u1L5/WechatMagician/raw/master/image/interface-4.en.png" width="40%" />

## Credits
* Thanks @rovo89 for the awesome Xposed framework.
* Thanks @rarnu for the prototype wechat_no_revoke.
