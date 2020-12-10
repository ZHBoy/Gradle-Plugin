package com.zhboy.plugin

import com.android.build.api.transform.Format;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils;

import java.util.Set;

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
