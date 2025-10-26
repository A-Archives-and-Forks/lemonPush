---
sidebar_position: 2
---
# 配置文件
运行程序后会自动生成一份默认配置文件`lemon.conf`

```
port=14756
folder=./_lemon_
ip=
auto_open_url=open
```

## 字段说明
- port默认为14756，如出现端口冲突需修改监听端口
- ip为空时每次启动需手动选择ip生成二维码，如指定ip则不需要选择ip
- folder是上传文件的所在的文件夹，`./_lemon_`表示程序当前目录的`_lemon_`文件夹
- auto_open_url是自动使用默认浏览器打开链接开关，open为开，close为关

