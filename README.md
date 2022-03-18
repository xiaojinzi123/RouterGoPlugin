## 前言

这个辅助插件不在维护！！！！！！！！！！！！！！！！！！！！！！！！

这是一个 AndroidStudio/Idea 的辅助工具,是一个插件,让你使用组件化方案 
[Component](https://github.com/xiaojinzi123/Component) 的时候,可以像下面一样
![](./imgs/RouterGoPluginPreview.gif) 自由的跳转,不会因为使用组件化而导致需要
搜索字符串那样去查找

## useage

### 手动下载
[点我下载插件](https://github.com/xiaojinzi123/RouterGoPlugin/releases)

### AndroidStudio中下载安装
在 `AndroidStudio` 中搜索插件 `RouterGo` 就可以搜索到,下载安装即可

### 在每个模块的清单文件中. 配置 host 信息

```
<application>
        <meta-data
            android:name="host_component1"
            android:value="component1" />
        ......  
</application>
```

name 必须是 host_为前缀的. name 的后缀我建议使用模块名字. 这样不会和其他模块的 name 冲突
value 是模块的名字
