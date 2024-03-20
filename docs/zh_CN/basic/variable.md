## 变量

变量是一个保存在内存中的数据区域的符号，它也是编程中最基础的元素。

在ECP中，一个变量声明会携带的信息包括变量的**访问与特性修饰符，变量类型以及可能有的初始值**。一个常规的变量的声明语句格式如下：

```
[modifier]<var/val> <variable name>[:Variable Type][=default expression]
```

其中，各部分的含义：
- modifier：特性与访问修饰符
- var/val：此变量是可变量还是不可变量
- variable name：变量的名称
- Variable Type：变量类型
- default expression：变量的初始值，一个表达式

举例来说：

```ecs
var variable1: Int = 0        //普通变量
val variable2 = "var"         //不可变量，变量的类型可被省略，可从初始值推断
const val VARIABLE3 = 0.5f    //常量，仅可使用简单基础类型，其实际效果等价于直接将它的值写在访问此常量的位置
internal var variable4 = 18L  //内部变量，internal为访问修饰符，请参阅章节-可见性
shared var variable5: Any?    //共享变量，组件的共享特征，请参阅章节-组件
```

### 访问与赋值

ECP中的变量访问和赋值语句与大多数编程语言一样，使用`=`号进行赋值，用在表达式中表示访问其中的值，如下：

```ecs
var a = 1
var b = 2
var c = a + b

b = 3
a = 1
c = b

foo(a, b, c) //作为参数访问
```

ECP还支持包装的变量访问，可以同时操作多个变量的值：

```ecs
var a = 1
var b = 2
var c = 3

(a, b) = (b, a)
(a, b, c) = (b, c, a)
```

> 上述的包装变量访问事实上是来自元组（Tuple）的功能，在元组当中会对此形式进行详细介绍。

### 属性

在ECP中，当一个变量被声明在组件中或者单例中，亦或者直接作为顶级元素存在时，如果他不是私有的变量，则它会具备对这个变量的`getter`和`setter`，即这个变量会成为其上级元素的**属性（Property）**，对属性进行访问和赋值时，操作都将被转移给`getter`与`setter`。

当变量处于组件，单例或者顶级上下文之中时，变量会被转化为默认的属性声明，如前文所演示的满足条件的变量均隐含了其属性的声明，如果完全展开变量的属性声明，那么它看起来是这样的：

```ecs
var property: String{
  default = "init"

  get() = default
  set(value){ default = value }
}

val readOnly: String{
  default = "init"

  get() = default
}
```

以上的两个变量声明与如下所示的简单声明是一致的：

```ecs
var property: String = "init"
val readOnly: String = "init"
```

在变量属性声明中，`var`变量具备`getter`和`setter`，而`val`只能具有`getter`。以前文的变量属性声明为例，一个描述属性的变量，在其类型声明之后是一个代码块，块内依据var/val可声明`get()`与`set(value)`。

其中，`get()`函数需要返回一个可分配给变量类型的对象作为对此变量访问获得的值，而`get(value)`函数则是在此变量被赋值时会被调用，传入的值会被提供给那个单一参数，参数类型与变量类型相同。声明中的`default`是一个关键字，它的作用是标记本变量的实际内存区域。

变量的内存区域可能是不存在的，这取决于用户是否为在属性块头部为`default`赋予任何其允许的值，包括null。也只有在为`default`赋值之后，在`getter`和`setter`的声明中才允许使用default关键字去访问变量内存区域。

当属性不声明`default`时，这个属性没有关联的内存区域，对它的访问由`getter`与`setter`进行控制，如下代码演示了一种无内存区域的属性用法：

```ecp
comp Vector2D(var x: Float = 0, var y: Float = 0) {
  var length: Float{
    get() = Math.sqrt(x * x + y * y)
    set(len){
      val scale = len / length
      x *= scale
      y *= scale
    }
  }
  
  val normalized: Vector2D{
    get(){
      val scale = 1 / length
      return new Vector2D(x * scale, y * scale)
    }
  }
}
```

特别的，`default`可以被赋值为`init`关键字，这表明此属性需要在初始化函数中分配内存初始值，就像这样：

```ecp
comp Poiint2D(val x: Int = init, val y: Int = init){
  val id: Int{
    default = init
  
    get(){
      println("point id is accessed, value: $default")
      return default
    }
  }
  
  init {
    id = x + y << 16
  }
}
```
