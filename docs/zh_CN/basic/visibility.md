## 可见性

可见性是对象成员对外部访问的控制修饰符，用于限制成员的访问权限。

访问修饰符有三种：

- `public（默认，留空）`：公共成员，可在对象外部任意上下文访问

  公共成员是默认的，不携带访问修饰符即为公共成员，公共成员可以在任何地方被访问，如下所示：
  ```ecs
  comp Example{
    val name = "abc"
    val age = 12
    
    foo(){
      println("hello world")
    }
  }
  
  val inst = new Example()
  println(inst.name) // abc
  println(inst.age) // 12
  inst.foo() // hello world
  ```
- `internal`：内部成员，仅在此组件和包含此组件的上下文内部可见

  内部成员仅对组件的依赖者可见，从对象外部是无法访问到内部成员的，如下所示：
  ```
  comp Example{
    internal val str = "hello world"
  }
  
  comp Foo: Example{
    fun foo(){
      println(str)
    }
  }
  
  new Foo().foo() // hello world
  new Foo().str // 编译错误，无法访问到内部成员
  ```

- `private`：私有的，本组件内部成员，不可从组件之外任何位置访问

  