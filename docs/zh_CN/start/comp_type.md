## 组件与组合类型

在ECP当中，所有的程序内容都被认为是**组件**，而所有程序的实际承担者都是**对象**，对象即是组件的集合体，对象的几乎所有行为都来自于组件。

### 组件

一个组件的构成有几个部分：
- 组件的特性修饰符
- 组件名称
- 组件所包含的组件表
- 组件内容

一个简单的`Position`组件声明如下所示：

```ecp
comp Position{
  var x: Int
  var y: Int

  init(x: Int = 0, y: Int = 0){
    this.x = x
    this.y = y
  }

  fun move(dx: Int, dy: Int){
    x += dx
    y += dy
  }
}
```

此组件包含了两个Int属性`x`和`y`以及一个函数`move(Int, Int)`，其中的`init`声明是组件的初始化函数，它会在包含此组件的组合被实例化时调用。您可以使用`new`关键字来创建**包含此组件的组合**实例，如下：

```ecs
var pos1 = new Position()
var pos2 = new Position(1, 2)
```

> **但是请注意**，组件本身不可以被实例化，它始终是对象的一部分，但如上所示您仍可以直接对组件使用`new`关键字，但它的语义并非直接实例化组件，在后文段落 *组合* 中会详细说明。

组件还可以包含若干个组件，它的形式如下：

```ecp
comp Motion: Position{
  var velX: Int
  var velY: Int

  fun updatePos(){
    move(velX, velY)
  }
}
```

不同于扩展，组件的包含关系表达的是一种**依赖**关系，组件与组件之间是同级的而不存在上下级关系。在上述的这段代码当中，表达的语义是“组件Motion对组件Position有依赖，Motion组件会包含Position组件”，其中的`move`函数调用是被委托到Position上的，在Motion中并不继承Position的任何功能。

此特性是来自ECP的语言设计的，即所有的功能实际承担者的类型都是扁平化的，而所有的元功能都是通过委托完成的，在章节 *编码风格* 中会解释此特性的意义。

### 可重复组件及组件别名

组件声明前可附加包括访问和特性的广义修饰符，这里需要提及修饰符`repeat`和别名，它们是组件被重复包含必要的特性

`repeat`修饰符会使得组件可重复放入列表内，即对象可以具备若干个此组件，但是可重复组件必须要为其分配别名以便访问，这样做的原因也会在章节 *编码风格* 中阐述。

一个可重复组件的声明与使用的简单形式如下：

```ecs
repeat comp Example{
  ...
}

comp Combine: Example as cmp1, Example as cmp2{
  ...
}
```

### 组合类型

组合类型，即若干个组件组合在一起构造的一个**承担实际功能**的类型，其组成结构与组件基本类似：
- 组合类型的特性修饰符
- 组合名称
- 组合所包含的组件表
- 组合内容

不同的是，在组合类型当中**不能声明任何不来自其组件的可见功能**，即在组合类型中不能声明任何从类型外部可以访问的变量和函数等，例如以下声明因为定义了一个可见变量`z`，所以是**非法的**：

```ecp
combine Entity: Position {
  var z: Int
}
```

以下是一个简单的组合类型声明，假设我们已经给出了组件`Health`：

```ecp
combine Entity: Motion, Health{
  init(x: Int, y: Int){
    this:Position(x, y)
  }
}
```

与组件的init相同，此处的init也会在此组合类型实例化时调用，但组件的初始化函数必须要在包含它的组件或组合类型的初始化函数中调用（任意一个，可为无参初始化函数）。

如果您此前使用过java或者其他传统的面向对象语言，那段声明的实际效果这会有一些反直觉。组合类型`Entity`事实上同时包含了`Motion`，`Health`还有`Position`，即`Position`与`Motion`是平级的，正如前文提到的在Motion中调用的Position的函数，事实上是在Motion中委派给Position的，而非调用虚方法。

但是需要注意，组合类型不可被用于包含组件列表，如下语句是非法的：

```ecp
combine EntityComp(x: Int, y: Int): Entity(x, y){
  ...
}
```

### 表观类型

组件和组合类型都可以被用作变量或者参数的表观类型，即：

```ecs
val entity: Entity
var pos: Position

fun hasten(motion: Motion, hx: Int, hy: Int){
  motion.velX += hx
  motion.velY += hy
}
```

上述的语句都是合法的。

表观类型的作用即确定对某个变量的访问协议，它用于描述这个变量可以访问哪些属性和函数等信息，正如上文程序中所写，`motion.velX`即motion变量的类型为`Motion`，此类型暴露了来自变量motion的属性`velX`和`velY`等信息。同时，类型会同时暴露其包含的组件列表的组件类型，这意味着你可以在`Motion`类型的变量上访问`Positon`中的属性和函数，统称为API。

为满足暴露对象API的稳定性，可以传递给该变量的对象也必须包含了此组件，或者对象的组合类型和变量的类型一致。

### 复合组合类型与匿名类型组合表达式

前文有提及对组件直接使用`new`关键字来创建包含此组件的组合类型实例：

```ecs
var pos = new Position(1, 2)
var mot = new Motion()
```

事实上上述表达式的完整形式如下：

```ecs
var pos: Position = new {{ Position(1, 2) }}()
var mot: Motion = new {{ Motion }}()
```

此表达式即将其中的组件组合为一个匿名的组合类型，此处的两个声明均为单组件组合，对于多个组件的组合形式，其类型表达式与表观类型语法如下所示：

```ecs
var ent: { Motion, Health } = new {{ Motion, Health }}()
```

其中，变量类型`{ Motion, Health }`称为复合组件类型，其表示可对该变量访问的API包含了这些组件暴露的所有API，分配的对象必须包含`Motion`和`Health`两个组件（其中Motion又包含Position，事实上是三个组件），事实上您可以对任何变量使用复合组件类型作表观类型，而非只有匿名组合类型可用。

后方new语句中的`{{ Motion, Health }}`表示的是一个匿名组合类型，它的效果等同于：

```ecp
combine $combine$#id: Motion, Health {}
```