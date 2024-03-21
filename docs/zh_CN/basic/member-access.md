## 成员访问

在组件当中被声明的属性和函数，称为组件的成员，它们是特定于其组件的行为。

对一个对象访问它的属性和函数使用符号`.`，在代码中，这看起来是这样的（假设组件`Position`内含有属性`x`和`y`还有函数`move(dx: Int, dy: Int)`）：

```ecs
combine Entity: Position(), Health(100)

val ent = new Entity()

println(ent.x)
println(ent.y)

ent.move(1, 4)
```

对对象可以直接访问它包含的组件内的行为，这在大多数情况下都会委派给其来自的组件。

但很多时候我们还需要对对象的组件访问它的成员，此时需要在访问语句上添加对组件的选择，它的形式如下例所示，尽管在这个例子中其实并不需要这么做：

```ecs
combine Entity: Position(), Health(100)

val ent = new Entity()

println(ent:Position.x)
println(ent:Position.y)

ent:Health.kill()
```

### 成员隐藏

正常情况下，对对象直接访问其成员时，这些成员大都来自它包含的组件，对成员的调用会被委托给这个成员的来源组件。例如前文演示的`ent.x`和`ent.y`，它们会被委托给`Position`组件，即第一段演示代码的两个属性访问与第二段演示代码并没有任何区别。

但是，有时候在组件当中会有重名的函数或者属性，例如如下这个多层叠加的组件包含结构：

```ecs
comp Comp1{
  var variable = "var"
  
  fun foo(){
    println("origin")
  }
}

comp Comp2: Comp1{
  var variable = "hello"
  
  fun foo(){
    println(variable)
  }
}

comp Comp3: Comp2{
  fun foo(){
    println(inst.variable)
  }
}

comp Comp4: Comp3{
  var variable = "world"
}
```

上述的几个组件当中有非常多重名的成员声明，每一层包含，重名的公共成员都会同名覆盖上层的同名成员，这种现象被称为**隐藏（Hide）**

例如我们对`new Comp2()`调用`foo()`，它会打印出`hello`，而不是`origin`，这是因为在结构上`Comp2`的`foo()`隐藏了`Comp1`内的`foo()`；同样的，我们对它访问`variable`同样会得到`"hello"`而非`"var"`。

注意上文`Comp3`内出现的`inst`关键字，此关键字指向此组件从属的对象实例。通常来说，在组件内的对成员的访问会首先尝试在组件内查找成员，如果成员存在，那么就相当于对`this`访问该成员，而在ECP中，`this`关键字指向的是对组件的引用，即：如果需要按对象的组件成员隐藏进行成员访问，那么就需要使用`inst`关键字去指向其实例。

在以上的声明下，如下程序会产生不同的效果：

```ecs
new Comp3().foo()
new Comp4().foo()
```

上述的第一段语句会打印出的是`"hello"`，`Comp3`将`foo()`内打印的变量定向到了对对象的`variable`属性访问，但是`Comp3`层次的可见`variable`属性来自`Comp2`，因为其值为`"hello"`从而在第一段语句打印出了`"hello"`。

而第二段语句是对`Comp4`的组合实例访问`foo()`，不同于前者，在`Comp4`内又声明了`variable`隐藏了上层包含的此变量，因此对对象访问该属性会得到`"world"`，因此第二段语句会打印出`"world"`。
