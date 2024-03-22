## 属性

在ECP中，当一个变量被声明在组件中或者单例中，亦或者直接作为顶级元素存在时，如果他不是私有的变量，则它会具备对这个变量的`getter`和`setter`，即这个变量会成为其上级元素的**属性（Property）**，对属性进行访问和赋值时，操作都将被转移给`getter`与`setter`。

当变量处于组件，单例或者顶级上下文之中时，变量会被转化为默认的属性声明，所有满足条件的变量均隐含了其属性的声明，如果完全展开变量的属性声明，那么它看起来是这样的：

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

变量的内存区域可能是不存在的，这取决于用户是否为在属性块头部为`default`赋予任何其允许的值，包括null。也只有在为`default`赋值之后，在`getter`和`setter`的声明中才允许使用`default`关键字去访问变量内存区域。

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
