Gradle项目构建工具，使用的Groovy语言。我们自定义一个plugin插件，也需要使用Groovy语言，同是代码需要在buildSrc目录下进行编写。
1.首先我们创建一个Android项目，然后选择File -> New Module-> Java or Kotlin Library，创建一个lib库，库名称就是buildSrc
image.png
image.png
新建buildSrc库会遇到错误'buildSrc' cannot be used as a project name as it is a reserved name原因是这个名字被官方保留了。解决：在settings.gradle中把include ':buildSrc'去掉即可
image.png
image.png
2.先看一线我们在项目中如何引用我们定义的plugin插件。

MyPlugin是Plugin插件名称
myplugin是Plugin的扩展
name具体的变量

apply plugin: 'com.android.application'
apply plugin: 'MyPlugin'//MyPlugin就是MyPlugin.properties的文件名，比如我们改成com.zh.MyPlugin.properties的话，引入方式就是 apply plugin: 'com.zh.MyPlugin'

myplugin{
    name '在调用的时候，换了个新名字，小丽' //实际作用是setUser('在调用的时候，换了个新名字，小丽')
}
android {
    compileSdkVersion 30
    buildToolsVersion "30.0.2"

    defaultConfig {
        applicationId "com.zhboy.gradle_plugin"
        minSdkVersion 21
        targetSdkVersion 30
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}

dependencies {
    implementation fileTree(dir: "libs", include: ["*.jar"])
    implementation 'androidx.appcompat:appcompat:1.2.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.0.4'
    testImplementation 'junit:junit:4.12'
    androidTestImplementation 'androidx.test.ext:junit:1.1.2'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.3.0'

}
3.接下来创建插件类 MyPlugin

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
4.创建扩展类，一些配置的初始化就在这里

package com.zhboy.plugin

/**
 * @author zhou_hao
 * @date 2020/12/10
 * @description: 我的插件对应的扩展
 */
class MyPluginExtension {
    def name = "小明同学"
}
5.创建MyPlugin.properties，作用时外部项目可以找到我们的插件
resources ->META-INF->gradle-plugins是固定的文件命名
image.png
image.png

MyPlugin.properties文件中指定插件plugin的目录即可
implementation-class = com.zhboy.plugin.MyPlugin
6.创建Transform类，android项目打包之前，对编译好的文件进行处理

package com.zhboy.plugin

import com.android.build.api.transform.Format;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils;

/**
 * @author: zhou_hao* @date: 2020/12/10
 * @description: Transform面向切面AOP编程。 的作用是在，android项目打包之前，对编译好的文件进行处理
 * */
class MyPluginTransform extends Transform {

    //返回的任务  名字 会放到任务列表里
    @Override
    String getName() {
        return "myplugin"
    }

    //告诉Transform你要处理那些类型的文件（这些文件都是编译好的）
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        //表示处理编译好的class文件
        return TransformManager.CONTENT_CLASS
    }
    //过滤器，标明你要处理的范围。是整个项目 或者某一个子项目 或者某几个子项目 ，也可以自己去定义Scope
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    //这里需要将打包需要的依赖jar包和我们自己的文件，复制到对应的打包的目录下build-intermediates-transforms-myplugin-debug
    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        //项目打包所依赖的所有Jar包
        def inputs = transformInvocation.inputs
        //项目打包存放需要jar包和class文件的位置
        def outputProvider = transformInvocation.outputProvider
        inputs.each {
            it.jarInputs.each {
                //将项目打包所依赖的所有Jar包，一个一个copy到项目打包存放需要jar包和class文件的位置
                File dest = outputProvider.getContentLocation(it.name, it.contentTypes, it.scopes, Format.JAR)
                FileUtils.copyFile(it.file, dest)
            }
            //将所有class文件，一个一个copy到项目打包存放需要jar包和class文件的位置
            it.directoryInputs.each {
                File dest = outputProvider.getContentLocation(it.name, it.contentTypes, it.scopes, Format.DIRECTORY)
                FileUtils.copyDirectory(it.file,dest)
            }
        }

    }
}
