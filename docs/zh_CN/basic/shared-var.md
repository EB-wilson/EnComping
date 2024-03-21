## 共享变量

**共享变量（Shared Variable）** 是在没有依赖关系的组件之间传递数据的首要方式。

共享变量即使用关键字`shared`修饰的成员属性，形式上：

```ecp
comp Example{
  shared var sharedVar: String
}
```
