## 函数

函数是一切可复用代码的基本组成单位，是程序组织的基本单元，在ECP中，所有实际的程序逻辑都是被定义在函数中的。

一个函数的定义格式如下所示：

```ecs
[modifiers]fun <Type Args Block>([arguments])[:Return Type][{body}]
```

其中：

- `modifiers`：函数的访问或者特性修饰符
- `Type Args Block`：函数的类型参数块
  > 类型参数块与常见的泛型用途并不一致，在ECP中泛型是基于类型表达式的，请参阅章节 _类型表达式_
- `arguments`：函数的参数列表
- `Return Type`：函数的返回值类型
- `body`：函数的主体代码块

举例来说，函数的定义如下例所示：

```ecs
//常规无参函数
fun foo1(){
  println("Hello, World!")
}

//常规函数
fun foo2(a: Int, b: Int): Int {
  return a + b
}

//函数的表达式形式，此形式可以省略返回类型声明，自动推断
fun foo3(a: String, b: String) = a + b

//带有参数默认值的函数，带有内部的访问修饰符，请参阅 可见性
internal fun foo4(str: String = "Hello World!"){
  println(str)
}

//带有可变参数的原型函数，proto函数是组件的重要特性，请参阅 组件
proto fun foo5(vararg args: String）
  for(arg in args){
    println(arg)
  }
}
```

### 函数的调用

函数调用的的一般形式如下所示

```ecs
foo1()
val result = foo2(1, 2)
val helloWorld = foo3("Hello", "World!")
foo4()
```

函数的参数默认情况下按形式参数声明的顺序传入，并将函数的返回值传回函数调用处作为表达式的值。如果函数没有声明返回类型，它的类型会被推断为`Unit`，这时函数返回不能被赋值给任何变量或者参数。

其中对`foo4()`函数的调用没有提供参数，在`foo4()`的参数表声明中为参数分配了默认值，即对这个函数调用可以不提供具有默认值的参数，其会使用默认值作为此次调用的传入参数。

函数调用还有一种依次指定名称的参数传入形式，这看起来是这样的：

```ecs
val result = foo2(b = 5, a = 2)
val string = foo3(b = "World!", a = "Hello")
```

这种调用形式可忽略参数的声明顺序，按名称指定参数进行传入，上述代码均颠倒了参数位置，但实际效果是一致的。

### 可变参数

在函数的最后一位参数上，可以使用关键字`vararg`来修饰表示这个参数是可变的，对带有可变参数的函数进行调用，可变参数之前的参数与一般的传递方式一致，在可变参数上可以传递多个类型可分配的值，或者被标记为可变参数的数组：

```ecs
foo5("a", "b", "c", "d", "e")
foo5(*["a", "b", "c", "d", "e"])
foo5(args = "a", "b", "c", "d", "e")
foo5(args = *["a", "b", "c", "d", "e"])
```

在函数内部访问可变参数时，可变参数可以直接视为一个可迭代集合，也可转换为数组。如前文所示的符号`*`所示，数组与可变参数可以使用`*`进行相互转换，但是`vararg`不能用于修饰类型，将数组再转换为可变参数时应当直接传入函数，如下：

```ecs
fun example(vararg args: String){
  for(str in args){
    println(str)
  }
  
  val strs: String[] = *args
  for(i in 0..strs.length - 1){
    println(strs[i])
  }
  
  foo5(args)
  foo5(*strs)
}
```
