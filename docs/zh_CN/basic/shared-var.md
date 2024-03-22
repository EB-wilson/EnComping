## 共享变量

**共享变量（Shared Variable）** 是在没有依赖关系的组件之间传递数据的首要方式。

共享变量即使用关键字`shared`修饰的成员属性，形式上：

```ecp
comp Example{
  shared var sharedVar: String
}
```

共享变量属于组件的待定成员，不可在组件中分配默认值，需要在组件所在的组合当中确认其默认值：

```ecp
combine Entity: Example{
  shared var sharedVar = "hello world"
}
```

共享变量对结构内包含的组件中的的所有同名变量都保持一致性，对任意一个组件内访问或赋值共享变量，则在对象所有组件中获得的变量值都会是一致的。

此特性旨在反转依赖，使此变量无需明确包含组件的下文中的声明，结构内只要此变量被修改，那么所有对此变量的访问都会被更改，换言之，所有组件内的共享变量都是其对象的**观察者**。

例如对于以下程序：

```ecs
comp Comp1{
  shared var status: Int
  
  fun foo1(){
    println(status)
  }
}

comp Comp2{
  shared var status: Int
  
  fun foo2(){
    status = 1
  }
}

combine Entity: Comp1, Comp2{
  shared var status: Int = 0
}

val ent = new Entity()
ent.foo1()           // 0
ent.foo2()
ent.foo1()           // 1
ent.status = 2
ent.foo1()           // 2
ent:Comp1.status = 3
ent.foo1()           // 3
```

### 共享属性

共享变量同样也是属性，共享变量的属性是在其组合当中的成员属性，在组件之中不可声明`getter`与`setter`，需要在组合当中进行声明，如下：

```ecs
comp Comp1{
  shared var status: Int
}

comp Comp2{
  shared var status: Int
}

combine Entity: Comp1, Comp2{
  shared var status: Int{
    default = 0
    
    get() = default
    set(value){
      println("status update!")
      default = value
    }
  }
}

```

尽管无法在组件内声明属性特征，但结合原型函数仍然可以轻松的实现对属性访问的通知模式，请参阅 原型函数。
