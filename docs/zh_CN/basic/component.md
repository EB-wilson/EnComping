## 组件

组件是ECP的核心组成部分，在ECP中所有的行为和功能都是由组件提供的。

组合式编程的基本思想就是将功能划分为精细的部分，然后将它们组合为更高级的组件，最终将组件组合在一起构造承载功能的对象。

在ECP中，组件就是一个个被声明的集合，它由关键字`comp`声明，包含一系列的属性和函数，并携带一些访问与特性修饰符，以及其包含的组件列表，组件的声明格式如下：

```ecs
[modifiers] comp <ComponentName>[<Type Arguments>][:Contains Components]{body}
```

- `modifiers`：组件的访问与特性修饰符
- `ComponentName`：此组件的名称
- `Type Arguments`：此组件的类型参数
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

在述的这段代码中出现了一个`shared`的变量还有一个`proto`的函数，它们是组件当中十分重要的特性，在稍后的段落中会详细解释它们的作用。

### 初始化函数

在上文的样例代码中，有出现这样的声明：

```ecs
init(name: String, age: Int){
  this.name = name
  this.age = age
}
```

这是组件的初始化函数声明，在这个例子上其带有两个参数。

一个组件当中可以具有多个初始化函数声明，在对象实例化时，依据传递的参数不同，会调用合适的初始化方法，在对象实例化时，所有组件都至少应当被调用一次其初始化函数。

特别的，您可以直接将参数包含的纯属性（一定要是**纯属性**，即属性的gettter和setter不可被人为指定）直接作为参数表提供，此时该组件就不能再定义其他的非默认初始化函数了：

```ecp
comp Example(var v1: Int, var v2: String){
  ...
}
```

### 默认初始化函数

在`init`关键字前可附加一个关键字`default`，此时该初始化函数不允许有参数，它看起来是这样的:

```ecs
default init{
  ...
}
```

上述的声明被称为默认初始化函数，这是个可选特性，在一个组件声明当中至多只能存在一个默认初始化函数，此函数会在对象被实例化后，在任何初始化函数被调用完成后被调用，可认为默认初始化函数就是这个组件的主要初始化函数，只要对象被创建那么它都会被调用。

### 包含组件

ECP依赖类型继承来实现代码复用，代替继承的是组件间的**包含（Contains）**，具体来说包含表达的是一种对于组件的依赖，或者说要求关系。譬如有如下几个组件：

```ecs
comp Position(var x: Int, var y: Int){
  fun move(dx: Int, dy: Int){
    x += dx
    y += dy
  }
}

comp Health(val maxHealth = 100){
  var health = maxHealth
  
  fun kill(){
    health = 0
  }
}
```

假设我们需要设计一个组件`Motion`来提供动量来让对象移动，那么我们就会希望一个对象有`Motion`时，就应该有`Position`组件，即：`Motion`依赖`Position`，或者说是需要。

转化为代码的话：

```ecp
comp Motion(var velX = 0, var velY = 0): Position{
  fun updatePos(){
    move(velX, velY)
  }
}

comp Entity(val id: Int): Motion(), Health(){
  ...
}
```

组件`Motion`当中包含了一个组件`Position`，表明Motion对Position的依赖，当Motion包含了Position时，在其中就可以访问来自Position的属性和函数，例如在上述程序中就直接使用了来自Position的函数`move(dx: Int, dy: Int)`。

需要强调，在ECP中的组件和组件之间**只有依赖关系**，所有组件的结构是平级的，包含的所有组件会在组合当中会进行拍扁，请参阅章节 *组合*。

### 结构内调用初始化函数

在一个组合结构当中，无论使用哪一个初始化函数的重载来构造实例，其包含的所有组件都应当被调用过至少一个初始化函数。

观察前文的包含组件列表，ECP约定在包含列表中，对任意组件的包含在使用括弧`(args)`时，尽管可能并未传入任何参数，此语句表示在初始化时传递的调用那个组件的此初始化函数，若没有使用括弧，那么表示对此组件的初始化函数调用为**待定**。

特别的，如果组件只含有无参数初始化函数，那么不带有括弧参数的组件列表引用的会被视为调用那个唯一的无参数初始化函数，而非待定。

我们提到过`init`块被用于设置初始化函数，如果一个组件的初始化函数调用是待定的，那么在包含它的结构中必须在任何init分支中都至少调用了一个初始化函数。包含的结构不一定是直接包含它的组件，在包含此组件的任意结构当中只要声明了对初始化函数的调用即可。

在初始化函数中，对包含的组件调用初始化函数的语法如下：

```ecs
init{
  this:<Component Name>([arguments])
}
```

**上述的语句只能在初始化函数中使用**。

待定初始化的组件应当在其它层级中调用初始化函数，以上述的`Motion`对`Position`的依赖为例，它的初始化函数调用是待定的，而`Entity`又包含了`Motion`，进而包含了待定调用的`Position`，即仍可以在对它的包含中确定初始化：

```ecp
comp CombinedEntity: Entity{
  init(id: Int){
    this:Entity(id)
    this:Position(0, 0)
  }

  init(id: Int, x: Int, y: Int){
    this:Entity(id)
    this:Position(0, 0)
  }
}
```

注意，无论构造对象时使用了哪一个初始化函数，所有组件都应该被**至少调用一个**初始化函数，例如上述代码中，两个初始化函数均调用了所有仍然待定组件的初始化函数，如果您没有在组件声明中完成对初始化函数的待定分配，那么在将它们组合起来时必须在组合中正确分配调用，否则组合就是非法的。
