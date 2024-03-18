## EnComping 语言介绍

EnCompin是一种完全组合式编程语言（Combination Program，CP），其旨在提供一种基于组合的元编程模式，使开发者可以直接以`组件-对象`的模式进行编程。

该语言设计之初即为了将`ECS`架构中的宝贵思想引入到编程语言当中，因此，EnComping是面向对象的编程语言，但它并非扩展式（Extend）的面向对象，而是组合式面向对象，与传统的面向对象语言不同，EnComping当中没有`继承`等概念，组件的`包含`被用于代替其需要实现的功能。

EnComping被设计为可在jvm上工作的编程语言，并且后续会有计划的实现它在native平台，.net和到js等平台的编译运行支持。

## 基本语法

EnComping的程序文件后缀名称为`.ecs`或者`.ecp`，二者有略微的差异，在之后会有阐述。EnComping可直接简称为ECP，下文亦采用ECP代指EnComping

以下是使用ECP编写的一段简单的HelloWorld程序：

```ecs
@MainEntry
fun main(args: Array<String>){
  println("Hello World!")
}
```
