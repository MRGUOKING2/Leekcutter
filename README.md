# Leekcutter

* Leekcutter是基于Grasscutter构建的Genshin Impact PS，并一直与Grasscutter同步并且作为Grasscutter的备胎。同时，这也是一款更适合中国用户的PS，其指令经过汉化，并会及时引入经过验证的修复代码，即使这条代码并没有通过Grasscutter的Pull Requests。本项目将不会隐去Grasscutter的字样，因为本项目赖以Grasscutter生存，且对Grasscutter保持一定的敬意。

**注意:** 我们一直欢迎您成为该项目的贡献者。在添加您的代码之前，请仔细阅读我们的 [代码规范](https://github.com/Grasscutters/Grasscutter/blob/stable/CONTRIBUTING.md).

## 当前特性

* 登录
* 战斗
* 好友列表
* 传送系统
* 祈愿系统
* 从控制台生成魔物
* 多人游戏 *部分* 可用
* 物品栏相关 (接收物品/角色, 升级角色/武器等)
* 汉化过的指令

## 快速设置指南

**附:** 加入Grasscutter的 [Discord](https://discord.gg/T5vZU6UyeG) 获取更多帮助！

### 环境需求

* Java SE - 17 (当您没有Oracle账户，可以使用[镜像](https://mirrors.tuna.tsinghua.edu.cn/Adoptium/17/jdk/))

  **注:** 如果您仅仅想要简单地**运行服务端而非编译**, 那么使用 **jre** 便足够了 

* MongoDB (推荐 4.0+)

* Proxy daemon: mitmproxy (推荐使用mitmdump), Fiddler Classic, 等

### 运行

**注:** 如果您从旧版本升级到新版本，最好删除 `config.json` 并启动服务端jar来重新生成它

1. 获取 `leekcutter.jar`
   - [自行构建](#构建)
2. 在**leekcutter.jar** 所在目录中创建 `resources` 文件夹并将 `BinOutput` 和 `ExcelBinOutput` 放入其中 *(查看 [wiki](https://github.com/Grasscutters/Grasscutter/wiki) 了解更多)*
3. 通过命令 `java -jar leekcutter.jar` 来运行Grasscutter. **在此之前请确认MongoDB服务运行正常**

注：MongoDB下载地址：[https://www.mongodb.com/download-center/community](https://www.mongodb.com/download-center/community) 并建议在安装时勾选同时安装MongoDB Compass的勾选框，这将会大大提升你的数据库管理体验。

### 重定向流量

½. 在服务器控制台中 [创建账户](#命令列表).

1. 重定向流量: (选其一)
    - Fiddler Classic: 运行Fiddler Classic, 在设置中开启 `解密https通信` 并将端口切换到除`8888` 以外的任意端口 (工具 -> 选项 -> 连接) 并加载 [此脚本](https://github.lunatic.moe/fiddlerscript).
      
    - [Hosts文件](https://github.com/Grasscutters/Grasscutter/wiki/Running#traffic-route-map)
    
2. 设置代理为 `127.0.0.1:8080` 或其它你所设定的端口

### 构建

Leekcutter 使用 Gradle 来处理依赖及构建.

**依赖:**

- Java SE Development Kits - 17
- Git（其实也可以用Github Desktop代替）

##### Windows

```shell
git clone https://github.com/Searchstars/Leekcutter.git
cd Leekcutter
.\gradlew.bat # Setting up environments
.\gradlew jar # Compile
```

##### Linux

```bash
git clone https://github.com/Searchstars/Leekcutter.git
cd Leekcutter
chmod +x gradlew
./gradlew jar # Compile
```

你可以在项目根目录中找到`leekcutter.jar`

## 账号系统

你可以通过游戏内的account指令创建/删除用户，当然也可以使用网页来创建用户（或者被称为注册），这对于那些需要让其它玩家加入进同一个服务器一起游玩的人很有帮助。

若你想通过网页创建用户，那么一般情况下，网页一般在`localhost(server ip)/register.html`下，例如我的服务器地址是1.1.1.1，那么我的注册网页就应在`1.1.1.1/register.html`下。如果你拥有公网ip，你可以直接把这个链接发给你的朋友，让你的朋友在你的服务器中快速、便捷地注册账号（注意：用户名与密码的总长度必须小于50个字符！！！），而无需你的帮助。在网页注册的账号将会被以明文形式保存在服务器上，虽然用户不能轻易得到它，但这仍并不安全，所以不建议大型公开服务器采用。

账号创建完毕后，在游戏内的登录和往常并不一样，你需要在游戏弹出的登录框的账号栏中填写`用户名:密码`，中间以英文冒号隔开，例如我的用户名是user333，密码是superidol，那么我应该在账号栏中输入：`user333:superidol`，随后在密码栏中随便输点什么，没错，你想输什么就输什么，然后点登录即可。若提示服务器错误，即表示你的密码错了。

## 命令列表

你可能需要在终端中运行 `java -jar leekcutter.jar -handbook` 它将会创建一个 `GM Handbook.txt` 以方便您查阅物品ID等

你可能需要在终端中运行 `java -jar leekcutter.jar -gachamap` 来使得祈愿历史记录系统正常显示物品信息。 这个命令生成一个配置文件到如下文件夹：`GRASSCUTTER_RESOURCE/gcstatic`。 不执行此命令，您的祈愿历史记录中将只会显示数字ID而非物品名称。（目前仅支持自动生成英文记录信息）

在每个玩家的朋友列表中都有一个名为“Server”的虚拟用户，你可以通过发送消息来使用命令。命令也适用于其他聊天室，例如私人/团队聊天。
要在游戏中使用命令，需要添加 `/` 或 `!` 前缀，如 `/pos`

| 命令            | 用法                                         | 权限节点                  | 可用性   | 注释                                       | 别名                                            |
| -------------- | -------------------------------------------- | ------------------------- | -------- | ------------------------------------------ | ----------------------------------------------- |
| account        | account <create\|delete> <用户名> [uid]      |                           | 仅服务端 | 通过指定用户名和uid增删账户                |                                                 |
| broadcast      | broadcast <消息内容>                         | server.broadcast          | 均可使用 | 给所有玩家发送公告                         | b                                               |
| coop           | coop \<uid> <目标uid>                 | server.coop               | 均可使用    | 强制某位玩家进入指定玩家的多人世界                  |                                                 |
| changescene    | changescene <场景ID>                         | player.changescene        | 仅客户端 | 切换到指定场景                             | scene                                           |
| clear          | clear <all\|wp\|art\|mat> [UID]                     | player.clearinv     | 仅客户端 | 删除所有未装备及未解锁的圣遗物(art)或武器(wp)或材料(mat)或者所有(all),包括五星    | clear                                        |
| drop           | drop <物品ID\|物品名称> [数量]               | server.drop               | 仅客户端 | 在指定玩家周围掉落指定物品                 | `d` `dropitem`                                  |
| give           | give [uid] <物品ID\|物品名称> [数量] [等级] [精炼等级]  |                           |          | 给予指定玩家一定数量及等级的物品 (精炼等级仅适用于武器)        | `g` `item` `giveitem`                           |
| givechar       | givechar \<uid> <角色ID> [等级]               | player.givechar           | 均可使用 | 给予指定玩家对应角色                       | givec                                           |
| giveart        | giveart [uid] \<圣遗物ID> \<主属性ID> [\<副属性ID>[,<次数>]]... [等级] | player.giveart            | 均可使用 | 给予玩家指定属性的圣遗物                   | gart                                           |
| giveall        | giveall [uid] [数量]                         | player.giveall            | 均可使用 | 给予指定玩家全部物品                       | givea                                           |
| godmode        | godmode [uid]                                | player.godmode            | 仅客户端 | 保护你不受到任何伤害(依然会被击退)         |                                                 |
| heal           | heal                                         | player.heal               | 仅客户端 | 治疗队伍中所有角色                         | h                                               |
| help           | help [命令]                                  |                           | 均可使用 | 显示帮助或展示指定命令的帮助               |                                                 |
| kick           | kick \<uid>                                   | server.kick               | 均可使用 | 从服务器中踢出指定玩家 (WIP)               | k                                               |
| killall        | killall [uid] [场景ID]                       | server.killall            | 均可使用 | 杀死指定玩家世界中所在或指定场景的全部生物 |                                                 |
| list           | list                                         |                           | 均可使用 | 列出在线玩家                               |                                                 |
| permission     | permission <add\|remove> <用户名> <权限节点> | *                         | 均可使用 | 添加或移除玩家的权限                       |                                                 |
| position       | position                                     |                           | 仅客户端 | 获取当前坐标                               | pos                                             |
| reload         | reload                                       | server.reload             | 均可使用 | 重载服务器配置                             |                                                 |
| resetconst     | resetconst [all]                             | player.resetconstellation | 仅客户端 | 重置当前角色的命座,重新登录即可生效        | resetconstellation                              |
| restart        | restart                                      |                           | 均可使用 | 重启服务端                                 |                                                 |
| say            | say \<uid> <消息>                             | server.sendmessage        | 均可使用 | 作为服务器发送消息给玩家                   | `sendservmsg` `sendservermessage` `sendmessage` |
| setfetterlevel | setfetterlevel <好感等级>                    | player.setfetterlevel     | 仅客户端 | 设置当前角色的好感等级                     | `setfetterlvl` `setfriendship`                  |
| setstats       | setstats <属性> <数值>                       | player.setstats           | 仅客户端 | 直接修改当前角色的面板                     | stats                                           |
| setworldlevel  | setworldlevel <世界等级>                     | player.setworldlevel      | 仅客户端 | 设置世界等级(重新登录即可生效)             | setworldlvl                                     |
| spawn          | spanw <实体ID\|实体名称> [等级] [数量]       | server.spawn              | 仅客户端 | 在你周围生成实体                           |                                                 |
| stop           | stop                                         | server.stop               | 均可使用 | 停止服务器                                 |                                                 |
| talent         | talent <天赋ID> <等级>                       | player.settalent          | 仅客户端 | 设置当前角色的天赋等级                     |                                                 |
| teleport       | teleport [@playerUid] \<x> \<y> \<z> [sceneId] | player.teleport           | 均可使用 | 传送玩家到指定坐标                         | tp                                              |
| tpall          |                                                   | player.tpall              | 仅客户端  | 传送多人世界中所有的玩家到自身地点         |                                                 |
| weather        | weather <天气ID> <气候ID>                    | player.weather            | 仅客户端 | 改变天气                                   | w                                               |

### 额外功能

当你想传送到某个地点, 只需要在地图中创建标记, 关闭地图后即可到达目标地点上空

# 快速排除问题

* 如果编译未能成功,请检查您的jdk安装 (JDK 17并确认jdk处于环境变量`PATH`中，若您安装了多个版本的jdk，请删除其它版本的java的环境变量。
* 我的客户端无法登录/连接, 4206, 其它... - 大部分情况下这是因为您的代理存在问题.如果使用Fiddler请确认Fiddler监听端口不是`8888`
* 启动顺序: MongoDB > Grasscutter > 代理程序 (mitmdump, fiddler等.) > 客户端
* 如果提示MongoDB相关问题，请确认MongoDB运行正常
