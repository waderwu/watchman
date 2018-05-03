# watchman

### todo

- [ ] 超过阈值通过邮件发到到指定邮箱
- [ ] 从指定邮箱接收相应的指令，然后执行相应的动作

### 模块分析
- modules
- sensors
- service
 - EmailFetch 用来获取邮件内容，只解析了邮件的subject，通过subject执行指令
 - EmailSender
 - MonitorService


### 试用
在build/outputs/apk/debug/ 文件夹下面下载apk文件
