## 组件

组件是ECP的核心组成部分，在ECP中所有的行为和功能都是由组件提供的。

组合式编程的基本思想就是将功能划分为精细的部分，然后将它们组合为更高级的组件，最终将组件组合在一起构造承载功能的对象。

在ECP中，组件就是一个个被声明的集合，它由关键字`comp`声明，包含一系列的属性和函数，并携带一些访问与特性修饰符，以及其包含的组件列表，组件的声明格式如下：

```ecs
[modifiers] comp <ComponentName>[<Type Args Block>][:Contains Components]{body}
```

- `modifiers`：组件的访问与特性修饰符
- `ComponentName`：此组件的名称
- `Type Args Block`：此组件的类型参数块
  > 类型参数块与常见的泛型用途并不一致，在ECP中泛型是基于类型表达式的，请参阅章节 _类型表达式_
- `Contains Components`：此组件包含的组件列表
- `body`：此组件包含的属性和函数

一个基本的组件声明如下所示：

```ecp
comp Person{
  var name: String
  var age: Int
  
  shared val id: Int
  
  init(name: String, age: Int){
    this.name = name
    this.age = age
  }
  
  fun sayHello(){
    println("hello!")
  }
  
  proto fun onWalk(){
    println("walking...")
  }
}
```

### 包含组件

其中有一个`shared`的变量还有一个`proto`的函数。
