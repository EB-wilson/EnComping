## 组合

 **组合（Combine）**是ECP当中最重要的概念，是组织若干个组件到具备实际功能的实体的基础。

一个常规组合由`combine`关键字声明，声明语句的格式与组件基本一致，如下所示：

```ecp
[modifiers] combine[<Type Argumrnts>][:Contains Comps][{body}]
```

- `modifiers`：组合类型的访问与特性修饰符
- `ComponentName`：组合类型的名称
- `Type Arguments`：组合类型的类型参数
- `Contains Components`：此组合类型包含的组件列表
- `body`：组合类型的成员体

各部分功能与组件大致一致，但在`body`内**不可声明此组合包含的组件之外的任何对外可见成员**，且组合类型不可以被包含。

下面是一个简单的组合声明形式（假设我们已经给出了组件`Position`和`Health`的定义）：

```ecp
combine Entity(x: Int, y: Int): Position(x, y), Health(100)
```

组合对组件的包含与组件与组件的包含规则是类似的，但在组合当中不允许存在任何**待定**行为，如未确定调用的组件初始化函数，未被隐藏的抽象函数，未分配默认值的共享变量等。

> 抽象函数是抽象组件的表达程序接口的行为，请参阅章节 *抽象组件*。
> 共享变量是在无依赖关系的组件之间传递数据的变量，请参阅 *共享变量*。

### 组合实例化

ECP是一个面向对象的编程语言，即程序逻辑都被封装为了**对象（Object）**，在ECP中产生对象的方式就是实例化一个组合。

可以使用`new`关键字实例化一个组合类型，最简单的实例化语句如下所示：：

```ecs
val ent = new Entity(1, 5)
```

### 组合的初始化函数

在组合当中也具备初始化函数，声明的语法与组件是一致的，在类型被实例化时会根据参数调用对应的初始化函数。

不同于组件中的声明，在组合当中不允许有**待定**的组件初始化调用，即无论使用组合中声明的哪一个初始化函数来进行实例化，它都应该直接或间接的调用了每一个包含的组件中的至少一个初始化函数，例如如下声明将是非法的：

```ecs
comp Position(var x: Int, var y: Int)
comp Motion(var velX: Int, var velY: Int): Position

combine Entity: Motion(0, 0)  //非法
combine Body: Motion{
  init(x: Int, y: Int){       //合法
    this:Motion(0, 0)
    this:Position(x, y)
  }
  
  init{                       //非法
    this:Motion(0, 0)
  }
}
```

在上述代码中，`Enitiy`只调用了`Motion`的初始化函数，却没有对`Position`的初始化函数进行调用，因此它是非法的；而`Body`中，它的含参数的初始化函数调用了所有待定调用的初始化函数，它是合法的，但是它的第二个无参数初始化函数没有调用`Position`的初始化函数，此初始化函数就是非法的。

### 匿名组合

除基础的命名组合，组件还可以以不指名的形式创建组合并获取实例，匿名组合的语法格式如下：

```ecs
new {{ <Components List> }}()[{body}]
```

上述代码里，`Components List`表示此匿名组合包含的组件列表，`body`为此匿名组合的成员体。不从语法内的括号接收任何初始化参数，其各组件的初始化函数直接在组件列表内调用，成员体可选，但在组合中存在待定的行为时，则必须在成员体内部去确定这些行为。

代码形式如下：

```ecs
var x = 10
var y = 15
var velX = 1
var velY = -1

val entity = new {{ Motion(velX, velY), Position(x, y) }}()
```

另一种形式是对组件直接进行`new`

```ecs
new <Component>([arguments])[{body}]
```

其中，`Component`就是实例化的目标组件，这个语句的实际效果是创建一个包含此组件的组合类型，并以传入的参数确定组件的初始化函数，而其后方的成员块条件与前一种形式的条件是完全一样的，即不可具有任何待定的行为，否则必须在成员块内进行确定。

形式如下：
```ecs
var x = 10
var y = 20

val entity = new Motion(0, 0){
  default init{
    this:Position(x, y)
  }
}
```