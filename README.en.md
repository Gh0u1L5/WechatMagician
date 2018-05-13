# WechatMagician

WechatMagician is a fancy Xposed module designed for Chinese social media application Wechat, to help the users get the ultimate control of their messages and moments. It is based on the __[WechatSpellbook](https://github.com/Gh0u1L5/WechatSpellbook)__ framework. Currently this module supports WeChat 6.5.3+.

## Get Start
Because WechatMagician is based on the __[WechatSpellbook](https://github.com/Gh0u1L5/WechatSpellbook)__ framework, when you clone the repository, don't forget the ```--recursive``` argument.
``` bash
git clone --recursive https://github.com/Gh0u1L5/WechatMagician.git
```

If you have already cloned the project, you can execute the following command to update the submodules.
``` bash
git submodule update --init --recursive
```

Similarly, if you are downloading the zip archieves, you have to also download the codes of WechatSpellbook and express them into the "spellbook" folder.

## Features

#### Chatting
1. Prevent friends from recalling messages (with customizable notification).
2. Forward messages to as many friends as you want.
3. Send more than 9 pictures at once (be default, less than 1000 pictures).
4. Hide useless groups into chatroom hider. (Need manually turn on in settings)
5. Mark friends as secret friends, and hide the chatting history. (Need manually turn on in settings)
6. Mark all the conversations as read in one click.

#### Moments
1. Prevent friends from deleting moments or comments.
2. Block the advertisements posted by Wechat.
3. Retweet other's moments, which can be text, image, video or links.
4. Take screenshot of a single moment.
5. Set blacklist for moments, and say goodbye to ads and PDA couples.

#### Miscellaneous
1. Automatically confirm the login requests from Wechat PC client (take your own risk if you turn on this function).

## QQ Group / Wechat Group
Official QQ Groups:
* Group One: 135955386 (Full)
* Group Two: 157550472

Official Wechat Groups:
1. Add Wechat account "XposedHelper", send the key word "Wechat Magician".
2. Add Wechat account "CSYJZF".

## Design
After learning from the failure of other Wechat modules, this project wants to do a better job in the following aspects:
* __Stability__: Most of those modules crashes for every Wechat update due to the obfuscator used by Wechat.
  - This project wraps each feature into a small "unit"; a single unit can crash safely without ruining the whole module.
  - This project has [a set of APIs](https://github.com/Gh0u1L5/WechatSpellbook/blob/master/src/main/kotlin/com/gh0u1l5/wechatmagician/spellbook/util/ReflectionUtil.kt) to analyze and match the signatures of critical classes / methods.
  - This project picks only [the signatures](https://github.com/Gh0u1L5/WechatSpellbook/tree/master/src/main/kotlin/com/gh0u1l5/wechatmagician/spellbook/mirror/) that exist since Wechat 6.5.3. Even if a signature is broken in the coming Wechat updates, it can be fixed easily.
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
