package com.zhboy.plugin

import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @author zhou_hao
 * @date 2020/12/10
 * @description: 创建一个plugin插件
 */
class MyPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        //创建一个新的扩展 myplugin，会在被调用的地方只用MyPluginExtension的方法
        def extension = project.extensions.create("myplugin", MyPluginExtension)

        //afterEvaluate表示全片代码执行完毕或者说初始化完成之后，才会来执行
        project.afterEvaluate{
            println("my name is ${extension.name}")
        }

        def myPluginTransform = new MyPluginTransform()
        //获取到最外层的扩展 这里对应的就是myplugin
        def baseExtension = project.extensions.getByType(BaseExtension)
        //将myplugin这个扩展插件跟myPluginTransform进行关联
        baseExtension.registerTransform(myPluginTransform)
    }
}