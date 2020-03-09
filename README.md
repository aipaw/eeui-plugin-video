# 视频播放器

## 安装

```shell script
eeui plugin install https://github.com/aipaw/eeui-plugin-video
```

## 卸载

```shell script
eeui plugin uninstall https://github.com/aipaw/eeui-plugin-video
```

## 组件

### 组件 eeui-video 事件

| 属性 | 作用 |
| --- | --- |
| onStart | 开始播放 |
| onPlaying | 播放中 |
| onPause | 暂停播放 |
| onCompletion | 播放完毕 |

### 组件 eeui-video 属性

| 属性 | 作用 |
| --- | --- |
| src | 播放地址 |
| autoPlay | 是否自动播放 |
| title | 标题 |
| pos | 播放的初始位置（单位毫秒） |

### 组件 eeui-video 方法

| 属性 | 作用 |
| --- | --- |
| play() | 播放 |
| pause() | 暂停 |
| seek(sec) | 播放到某一时间（单位毫秒） |
| fullScreen() | 全屏 |
| quitFullScreen() | 退出全屏 |
| getDuration(callback) | 获取当前播放视频时长（单位毫秒） |


### 组件 DEMO

```html
<template>
    <div>

        <eeui-video ref="video" @onPlaying="start" img="logo.png" style="width:750px;height:400px;background-color: black" title="xxxxx" auto-play="false"   src="http://mp4.vjshi.com/2013-07-25/2013072519392517096.mp4"></eeui-video>

        <div style="flex: 1;align-items: center;justify-content: center">
            <text>{{text}}</text>
            <button text="播放" @click="play"></button>
            <button text="暂停" @click="pause" style="margin-top: 20px"></button>
            <button text="全屏" @click="full" style="margin-top: 20px"></button>
            <button text="seek" @click="seek" style="margin-top: 20px"></button>
        </div>

    </div>
</template>
<script>

    export default{
        components: {},
        data(){
            return {
                text:''
            }
        },
        props: {},
        methods: {
            start(p){
                this.text=p.percent
            },
            play(){
                this.$refs.video.play()
            },
            pause(){
                this.$refs.video.pause()
            },
            full(){
                this.$refs.video.fullScreen()
            },
            seek(){
                this.$refs.video.seek(10)
            },

            onLoad(p){

            }
        },
        created(){

        }
    }
</script>
<style scoped>

</style>

```

## 模块

### 引用模块
```js
const video = app.requireModule("eeui/video");
```

### 获取视频时长

```js
/**
 * 通过视频地址获取视频时长
 * @param url       视频地址
 * @param callback  回调事件，result:{status:状态(success|error), msg:状态描述, url:视频地址, duration:总时长毫秒数}
 */
video.getDuration(url, callback(result))

```
