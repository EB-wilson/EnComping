## 编码风格

ECP语言有其类型系统设计上的特殊性，通常来说ECP的程序组织形式是**组件-对象**的，但ECP仍然具备变量/函数顶级声明的能力。

ECP鼓励开发者将大部分主要程序和行为都编写在组件当中，应当考虑尽可能令程序的分解良好，令功能的粒度更小，以更多的组件数量来增加组件组合的灵活性。组件之外，可将函数/变量等定义在`.ecs`文件的顶级上下文中。

### .ecs与.ecp

ECP项目是由许多个程序文件构成的，根据文件类型的不同，可包含一个或若干个组件/组合/函数/变量等声明，在语言介绍当中有提到过两类程序文件，.ecp文件的顶级上下文中只能定义一个组件/类型等结构，而.ecs中则可以定义任意数量的声明语句。

详细来说.ecp文件后缀名是*EnComping Program*的缩写，它被认为是一个独立的程序单元，仅表示一个顶级类型元素，它可以使用的顶级声明包括：

- 组件 - **comp**
- 组合类型 - **combine**
- 组件覆盖 - **override**
- 单例 - **singleton**
- 枚举 - **enum**

在特性说明当中会详细的解释上面出现的许多陌生概念。

我们也建议在项目当中使用.ecp为主，即**一个文件只编写某一个或者一组具体的功能**，以使得项目结构更加直观清晰。

而.ecs文件则是EnComping Script的缩写，此文件会被视为**若干程序元素声明的集合**，即在.ecs当中可以定义任意数量的变量/函数/组件/组合等。.ecs文件可以保存一系列的API以提供方便的使用，通常来说ecs被用于定义实用工具或者存放一些不希望添加到组件结构中的代码。

> 有时候，可能会需要解决一些小微型的需求或者问题，当这些程序放入组件结构中会不太方便时，建议使用ecs和面向过程的写法来编写这些比较小微的目标。**本文仅作为参考，无论何时，都以使代码简洁易读为目标**

### 包与导入

无论.ecp还是.ecs，它们都需要分配一个包名称来避免命名污染，不同的是.ecp要求其包名与文件在编译目录的相对路径强关联，而.ecs文件则不对包名称有强制要求，但是我们依然建议使用其相对路径的关联包路径。

与java相同，例如在项目的ECP代码目录下，有`/example/packageName/HelloWorld.ecp`文件，那么在这个文件的开头必须有如下声明：

```ecp
package example.packageName
```

上述语句表明此文件包含的内容是存在于`example.packageName`包内的，这在导入时会用到。

另外，.ecs的包名一般建议使用和.ecp相同的形式，但是在末尾附加此文件的名称，比如和前文相同的路径但叫做`/example/packageName/HelloWorld.ecs`，此时其包名声明我们建议使用基础包名加上文件名：

```ecs
package example.packageName.HelloWorld
```

在跨文件访问时，若在同一个包内，那么在文件中可以直接访问包内的其他文件内容，否则就需要使用`import`语句进行导入，也同Java一样，import必须在文件的包声明之后和第一个语句声明之前。

值得注意的一点是在.ecs中定义的所有内容，都需要被导入后才能使用，而.ecp则直接导入其包名并定位到文件即可。例如，假设有`/example/HelloWorld.ecs`和`/example/main/Main.ecp`两个不同包的文件，后者访问前者的内容就需要逐一导入或者直接使用通配符：

HelloWorld.ecs:
```ecs
package example.HelloWorld

var hello = "Hello"
var world = "World"

fun printHelloWorld(){
  println(hello)
  println(world)
  println("!")
}
```

Main.ecp:
```ecp
package example.main

import example.HelloWorld.hello
import example.HelloWorld.printHelloWorld
//或者使用通配符 import example.HelloWorld.*

@EntryPoint
comp Main{
  init{
    println(hello)

    printHelloWorld()
  }
}
```

### 单例代码块

单例代码块即`singleton`，它事实上是一个包装非成员函数的工具，你可以直接对单例访问它当中定义的成员变量和成员函数。它的一般形式是这样的：

```ecp
singleton Mathf{
  const val PI = 3.1415926f
  const val HALF_PI = PI/2

  fun sin(deg: Float): Float{...}

  fun cos(deg: Float): Float{...}

  fun tan(deg: Float): Float{...}

  ...
}
```

您可以直接使用它的名称标记来访问它当中定义的内容：

```ecs
println(Mathf.PI)
peintln(Mathf.sin(60))
```

单例代码块有一种特殊的形式，即附着在组件或组合类型中时，它可以省略其名称标识，并使用它附着的类型作为名称，这看起来是这样的：

```ecp
comp Entity{
  singleton {
    fun kill(ent: Entity){...}
  }

  ...
}
```

当你需要访问它时，直接对Entity类型访问其函数即可：

```ecs
Entity.kill(ent)
```

单例的附着还有一种形式，当单例所在的包中存在同名的组件或组合类型时，它也可以附着到那个同名的类型上，但是一个类型有且只能有一个单例附着，多个附着是非法的。

同名附着单例看起来是这样的：

```ecs
package example

comp Entity{
  ...
}

singleton Entity{
  fun kill(ent: Entity){...}
}
```

只要其包路径与名称均相同，那么这个单例就可以附着到那个同名的类型上，上述这段代码和前一个的效果是完全一致的。

事实上，单例就是一种提供静态包装的工具集的方式，同样的，作为包装结构，它也是可以作为.ecp文件的顶级结构存在的。

### 组合类型与组件

组合类型是ECP中的实际的功能承载者，但是通常我们认为组合类型只是包含若干组件的集合，在组合类型当中仅能描述初始化函数，对原型函数的拼装，以及对组件功能的委派，不可声明任何新的对外部可见的成员。

此举是为了保证语义的一致性，我们希望在ECP当中，承担功能的对象的所有行为最终都来自组件，或者是对组件的成员扩展（请参阅 *扩展* ），而不应该来自其他意外的地方。

结合在先前的章节中提到的**匿名组合类型**来看，即便我们需要强调对组件进行`new Comp()`事实上是创建了一个包含此组件的匿名组合的实例，但是在代码风格上，它起到的作用就像是Java中的Object一样，在ECP中就是**万物皆是组件**，只是它们最终都需要被组合后，再构造对象去承担实际的功能。

在此基础上，我们鼓励开发者在非必要的情况下，减少声明组合类型，使用匿名组合的方式来获取他们想要的对象，采取**工厂方法**的模式去创建他们需要的对象会是个比较好的选择。

例如，对于下面这个组合及它的使用：

```ecs
combine Example(x: Int, y: Int): Position(x, y), Health{}
...
var a: Example = new Example(0, 1)
```

我们非常推荐直接写作如下形式：

```ecs
var a: {Position, Health} = new {{ Position(0, 1), Health }}()
```

两个语句中的var类型均可省略，但写明的变量类型与其推断的类型是相同的，上述语句与前一个的实际效果几乎完全一致，但是前者的变量`a`的类型为`Example`，后者则是复合组件类型，前者将只能接收Example的实例而不可接收具备其完全对等行为的其他对象，而后者则可以接收任何包含了它复合的两个组件的对象。

### 语法规则

通常来说，EnComping在保证语言正确的前提下，对语言的具体语法并不做强限制，但通常我们选择按照语法规范的惯例来使程序更有可读性，**规范的最终目的都是为了更简洁更容易阅读，而非信条**。

- **一致的句尾分号**  
  ECP中的句尾分号是不必要的，尽管你仍可以用分号来分割语句，但我们建议完全不写句尾分号，亦或者在项目中完全采用句尾分号，以确保语法的一致性，即您应当在如下风格当中选择一种：
  ```ecp
  comp Example{
    var v1 = "hello"
    var v2 = "world"

    fun print(){
      println(v1 + " " + v2)
    }
  }
  ```
  ```ecp
  comp Example{
    var v1 = "hello";
    var v2 = "world";

    fun print(){
      println(v1 + " " + v2);
    }
  }
  ```

- **命名约定**  
  ECP中使用的命名符号规则大致与java一致，尽量使你的命名符号有意义，减少使用诸如**`s1`,`a`,`lxq`**等令人迷惑的没有经过约定的字母缩写。建议使用ascii编码的字符撰写命名符号，其大小写规范遵从如下表格：

  | 命名符号   | 规则             | 例子                        |
  |:-------|:---------------|:--------------------------|
  | 组件名称   | UpperCamelCase | comp HelloWorld{}         |
  | 组合类型名称 | UpperCamelCase | combine HelloWorld{}      |
  | 包名称    | alllowercase   | package example.main      |
  | 常量名称   | ALL_UPPER_CASE | const val LINE_SEP = "\n" |
  | 常量名称   | ALL_UPPER_CASE | const val LINE_SEP = "\n" |
  | 函数名称   | lowerCamelCase | fun helloWorld()          |
  | 枚举类型   | UpperCamelCase | enum Color{}              |
  | 枚举项    | ALL_UPPER_CASE | WHITE, BLACK, GRAY        |

- **流程控制**  

  在可能的情况下，建议尽可能减少分支语句的嵌套情况，例如使用`return`，`break`和`continue`来减少分支语句的嵌套次数，因为过多的嵌套会使得程序变得难以阅读。减少嵌套次数，例如：
  ```ecs
  fun foo(){
    if(a){
      println("a")

      if(b) println("b")
      else println("c")
    }
  }
  ```
  
  可以反转最外层if语句为：
  ```ecs
  fun foo(){
    if(!a)return

    println("a")

    if(b) println("b")
    else println("c")
  }
  ```

- **缩进**  
  程序的缩进长度应当保持一致（普通缩进，连续缩进等），具体长度不做限制，但建议长度至少是2个空格长度的整倍数，且不应当大于8个空格长度，例如您不应当使用3个空格长度进行缩进或者用长达16个空格长度的缩进。本文所有的演示代码均为2空格长度缩进。