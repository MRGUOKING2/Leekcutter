# Leekcutter

* Leekcutter是基于Grasscutter构建的Genshin Impact PS，并一直与Grasscutter同步并且作为Grasscutter的备胎。同时，这也是一款更适合中国用户的PS，其指令经过汉化，并会及时引入经过验证的修复代码，即使这条代码并没有通过Grasscutter的Pull Requests。本项目将不会隐去Grasscutter的字样，因为本项目赖以Grasscutter生存，且对Grasscutter保持一定的敬意。

**注意:** 我们一直欢迎您成为该项目的贡献者。在添加您的代码之前，请仔细阅读我们的 [代码规范](https://github.com/Grasscutters/Grasscutter/blob/stable/CONTRIBUTING.md).

## 当前特性

* 登录
* 账号系统
* 战斗
* 好友列表
* 传送系统
* 祈愿系统
* 从控制台生成魔物
* 多人游戏 *部分* 可用
* 物品栏相关 (接收物品/角色, 升级角色/武器等)
* 汉化指令

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
   - [自行构建（推荐，但需补全res）](#构建)
   - [Actions（需手动补全文件，不推荐）](https://github.com/Searchstars/Leekcutter/actions)
   - [Releases（版本落后，非最新构建，但无需补全res等文件）](https://github.com/Searchstars/Leekcutter/releases)
2. 在**leekcutter.jar** 所在目录中创建 `resources` 文件夹并将 `BinOutput` 和 `ExcelBinOutput` 放入其中 *(查看 [wiki](https://github.com/Grasscutters/Grasscutter/wiki) 了解更多)*
3. 通过命令 `java -jar leekcutter.jar` 来运行Grasscutter. **在此之前请确认MongoDB服务运行正常，推荐使用MongoDBCompass来管理数据库**

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
- 一个有编程基础的脑子

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

目前，这是Leekcutter的特色功能。

你可以通过游戏内的account指令创建/删除用户，当然也可以使用网页来创建用户（或者被称为注册），这对于那些需要让其它玩家加入同一个服务器一起游玩的人很有帮助，毕竟你无需再跟玩家们一个个的沟通、交流，并使用account指令创建用户账号了，只需要把注册页面的链接甩给他们让他们自行操作就好了。

若你想通过网页创建用户，那么一般情况下，网页一般在`localhost(server ip)/register.html`下，这也将会随着你在config.json中指定的ip与端口而变动。例如我的服务器地址是1.1.1.1，那么我的注册网页就应在`1.1.1.1/register.html`下。如果你拥有公网ip，你可以直接把这个链接发给你的朋友，让你的朋友在你的服务器中快速、便捷地注册账号（注意：用户名与密码的总长度必须小于50个字符！！！），而无需你的帮助。注册的账号的用户名以及密码将会被以明文形式保存在服务器上，使用文件来存储而非数据库。虽然只要你不手贱乱开共享乱开端口，用户就不能轻易得到它，但这仍并不安全，所以不建议大型公开服务器采用。

账号创建完毕后，在游戏内的登录和往常并不一样，你需要在游戏弹出的登录框的账号栏中填写`用户名:密码`，中间以英文冒号隔开，例如我的用户名是user333，密码是superidol，那么我应该在账号栏中输入：`user333:superidol`，随后在密码栏中随便输点什么，没错，你想输什么就输什么，然后点登录即可。若提示服务器错误，即表示你的密码错了。

为什么游戏内登录窗口的密码栏无法使用？因为游戏内密码栏向服务器发送的数据使用RSA加密，而解密几乎是不可能的，所以目前也就只能这样凑合用用了。详见此[issue](https://github.com/Grasscutters/Grasscutter/issues/68)

## 命令列表

这可能需要您自行查看Grasscutter的README或在服务端/游戏内执行help指令

### 额外功能

当你想传送到某个地点, 只需要在地图中创建标记, 关闭地图后即可到达目标地点上空

# 快速排除问题

* 如果编译未能成功,请检查您的jdk安装，若您安装了多个版本的jdk，请从`PATH`环境变量中删除其它版本的jdk的环境变量，并在cmd窗口中使用`java -version`来确保配置正常
* 我的客户端无法登录/连接, 4206, 其它... - 大部分情况下这是因为您的代理存在问题.如果使用Fiddler请确认Fiddler监听端口不是`8888`
* 启动顺序: MongoDB > Grasscutter > Fiddler Classic > 客户端
* 如果提示MongoDB相关问题，请确认MongoDB运行正常
