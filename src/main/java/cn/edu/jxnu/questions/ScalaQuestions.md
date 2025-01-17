## Scala面试问题（Scala interview questions）

搬运并补充 [www.playscala.cn](https://www.playscala.cn/article/view?_id=10-5cd54475eeab56146a538620)

### Q1 var，val和def三个关键字之间的区别？

- var是变量声明关键字，类似于Java中的变量，变量值可以更改，但是变量类型不能更改。不可被lazy修饰，可被final修饰（不可覆盖）
- val常量声明关键字。补充：方法的参数也是val，不可再次赋值，但是每次方法执行完val会被释放，下回调用时是新的值，与普通常量在程序初始化后不再改变是有差异的。
- def 关键字用于创建方法（[注意方法和函数的区别](https://tpolecat.github.io/2014/06/09/methods-functions.html)）。
- 还有一个lazy val（惰性val）声明，意思是当需要计算时才使用，避免重复计算。

代码示例：
```scala
var x = 3 //  x是Int类型
x = 4      // 
x = "error" // 类型变化，编译器报错'error: type mismatch'

val y = 3
y = 4        //常量值不可更改，报错 'error: reassignment to val'

def fun(name: String) = "Hey! My name is: " + name
fun("Scala") // "Hey! My name is: Scala"

//注意scala中函数式编程一切都是表达式
lazy val x = {
  println("computing x")
  3
}
val y = {
  println("computing y")
  10
}
x+x  //
y+y  // x 没有计算, 打印结果"computing y" 
```

### Q2 trait（特质）和abstract class（抽象类）的区别？

- 一个类只能集成一个抽象类，但是可以通过with关键字继承多个特质（混入多个特质）；
- 抽象类有带参数的构造函数，特质不行（如 trait t（i：Int）{} ，这种声明是错误的）；
- trait的线性化细节描述见本库Scala语法基础部分（Construction4）。

### Q3 object和class的区别？

- object是类的单例对象，开发人员无需用new关键字实例化。如果对象的名称和类名相同，这个对象就是伴生对象（深入了解请参考问题Q7）

代码示例：
```scala
//声明一个类
class MyClass(number: Int, text: String) {
  def classMethod() = println(text)
}
//声明一个对象
object MyObject{
  def objectMethod()=println("object")
}
new MyClass(3,"text").classMethod() //打印结果test，需要实例化类
Myclass.classMethod()  //无法直接调用类的方法
MyObject.objectMethod() //打印结果object，对象可以直接调用方法
```

### Q4 case class （样本类）是什么？

样本类是一种不可变且可分解类的语法糖，这个语法糖的意思大概是在构建时，自动实现一些功能。样本类具有以下特性：

1. 自动添加与类名一致的构造函数（这个就是前面提到的伴生对象，通过apply方法实现），即构造对象时，不需要new；
2. 样本类中的参数默认添加val关键字，即参数不能修改；
3. 默认实现了toString，equals，hashCode，copy等方法；
4. 样本类可以通过==比较两个对象，并且不在构造方法中定义的属性不会用在比较上。

代码示例：
```scala
//声明一个样本类
case class MyCaseClass(number: Int, text: String, others: List[Int]){
 println(number)
}
//不需要new关键字，创建一个对象
val dto = MyCaseClass(3, "text", List.empty) //打印结果3

//利用样本类默认实现的copy方法
dto.copy(number = 5) //打印结果5

val dto2 = MyCaseClass(3, "text", List.empty)
pringln(dto == dto2) // 返回true，两个不同的引用对象
class MyClass(number: Int, text: String, others: List[Int]) {}
val c1 = new MyClass(1, "txt", List.empty)
val c2 = new MyClass(1, "txt", List.empty)
println(c1 == c2 )// 返回false,两个不同的引用对象
```

### Q5 Java和Scala 异步计算的区别？

这里作者的意思是他大概也不清楚，请阅读这个 [really clean and simple answer on StackOverflow](https://stackoverflow.com/questions/31366277/what-are-the-differences-between-a-scala-future-and-a-java-future/31368177#31368177)，我个人理解还不到位后续补上。

### Q6 unapply 和apply方法的区别， 以及各自使用场景？

- 先讲一个概念——提取器，它实现了构造器相反的效果，构造器从给定的参数创建一个对象，然而提取器却从对象中提取出构造该对象的参数，
scala标准库预定义了一些提取器，如上面提到的样本类中，会自动创建一个伴生对象（包含apply和unapply方法）。
- 为了成为一个提取器，unapply方法需要被伴生对象。
- apply方法是为了自动实现样本类的对象，无需new关键字。

### Q7 伴生对象是什么？

- 前面已经提到过，伴生对象就是与类名相同的对象，伴生对象可以访问类中的私有量，类也可以访问伴生对象中的私有方法，类似于Java类中的静态方法。
伴生对象必须和其对应的类定义在相同的源文件。

代码示例：
```scala
//定义一个类
class MyClass(number: Int, text: String) {

  private val classSecret = 42

  def x = MyClass.objectSecret + "?"  // MyClass.objectSecret->在类中可以访问伴生对象的方法，在类的外部则无法访问
}

//定义一个伴生对象
object MyClass { // 和类名称相同
  private val objectSecret = "42"

  def y(arg: MyClass) = arg.classSecret -1 // arg.classSecret -> 在伴生对象中可以访问类的常量
}

MyClass.objectSecret // 无法访问
MyClass.classSecret // 无法访问

new MyClass(-1, "random").objectSecret // 无法访问
new MyClass(-1, "random").classSecret // 无法访问
```

### Q8 Scala类型系统中Nil, Null, None, Nothing四个类型的区别？

Scala类型图：

![Scala类型系统继承结构图](../scala/scala%E7%B1%BB%E5%9E%8B%E7%B3%BB%E7%BB%9F%E7%BB%93%E6%9E%84.jpg?raw=true)

- Null是一个trait（特质），是所有引用类型AnyRef的一个子类型，null是Null唯一的实例。
- Nothing也是一个trait（特质），是所有类型Any（包括值类型和引用类型）的子类型，它不在有子类型，它也没有实例，实际上为了一个方法抛出异常，通常会设置一个默认返回类型。
- Nil代表一个List空类型，等同List[Nothing]
- None是Option monad的空标识（深入了解请参考问题Q11）

### Q9 Unit类型是什么？

- Unit代表没有任何意义的值类型，类似于java中的void类型，他是AnyVal的子类型，仅有一个实例对象"( )"

### Q10 call-by-value和call-by-name求值策略的区别？

- call-by-value是在调用函数之前计算；
- call-by-name是在需要时计算（懒惰求值、非严格求值）。

示例代码：
```scala
//声明第一个函数
def func(): Int = {
  println("computing stuff....")
  42 // return something
}
//声明第二个函数，scala默认的求值就是call-by-value
def callByValue(x: Int) = {
  println("1st x: " + x)
  println("2nd x: " + x)
}
//声明第三个函数，用=>表示call-by-name求值
def callByName(x: => Int) = {
  println("1st x: " + x)
  println("2nd x: " + x)
}

//开始调用

//call-by-value求值
callByValue(func())   
//输出结果
//computing stuff....  
//1st x: 42  
//2nd x: 42

//call-by-name求值
callByName(func())   
//输出结果
//computing stuff....  
//1st x: 42  
//computing stuff....
//2nd x: 42
```

### Q11 Option类型的定义和使用场景？

- 在Java中，null是一个关键字，不是一个对象，当开发者希望返回一个空对象时，却返回了一个关键字，为了解决这个问题，Scala建议开发者返回值是空值时，
使用Option类型，在Scala中null是Null的唯一对象，会引起异常，Option则可以避免。Option有两个子类型，Some和None（空值），如果有必要获取异常详细的信息，可以使用Either。

代码示例：
```scala
val person: Person = getPersonByIdOnDatabaseUnsafe(id = 4) // 如果没有id=4的person时，返回null对象
println(s"This person age is ${person.age}") //如果是null，抛出异常

val personOpt: Option[Person] = 
getPersonByIdOnDatabaseSafe(id = 4) // 如果没有id=4的person时，返回None类型

personOpt match {
  case Some(p) => println(s"This person age is ${p.age}")
  case None => println("There is no person with that id")
}
```

### Q12 yield如何工作？

- yield用于循环迭代中生成新值，yield是comprehensions的一部分，是多个操作（foreach, map, flatMap, filter or withFilter）的composition语法糖。（深入了解请参考问题Q14）

代码示例：
```scala
//scala命令行
scala> for (i <- 1 to 5) yield i * 2 
res0: scala.collection.immutable.IndexedSeq[Int] = Vector(2, 4, 6, 8, 10)
```

### Q13 解释隐示参数的优先级

在Scala中implicit的功能很强大。当编译器寻找implicits时，如果不注意隐式参数的优先级，可能会引起意外的错误。因此编译器会按顺序查找隐式关键字。顺序如下：
1. 当前类声明的implicits ；
2. 导入包中的 implicits；
3. 外部域（声明在外部域的implicts）；
4. inheritance；
5. package object；
6. implicit scope like companion objects。

一个参考文章：[set of examples can be found here](http://eed3si9n.com/implicit-parameter-precedence-again).

### Q14 comprehension（推导式）的语法糖是什么操作？

- comprehension（推导式）是若干个操作组成的替代语法。如果不用yield关键字，comprehension（推导式）可以被foreach操作替代，或者被map/flatMap，filter代替。

示例代码：
```scala
//三层循环嵌套
for {
  x <- c1
  y <- c2
  z <- c3 if z > 0
} yield {...}

//上面的可转换为
c1.flatMap(x => c2.flatMap(y => c3.withFilter(z => z > 0).map(z => {...})))
```

### Q15 Streams：当使用Scala Steams时需要考虑什么？Scala的Streams内部使用什么技术？

- 还没有理解，暂时不翻译，后续补上。

### Q16 什么是vaule class？

- 开发时经常遇到这个的问题，当你使用integer时，希望它代表一些东西，而不是全部东西，例如，一个integer代表年龄，另一个代表高度。由于上述原因，我们考虑包裹原始类型生成一个新的有意义的类型（如年龄类型和高度类型）。
- [Value classes](https://docs.scala-lang.org/overviews/core/value-classes.html) 允许开发者安全的增加一个新类型，避免运行时对象分配。
有一些 [必须进行分配的情况](https://docs.scala-lang.org/overviews/core/value-classes.html) and 限制，但是基本的思想是：在编译时，通过使用原始类型替换值类实例，删除对象分配。更多细节[More details can be found on its SIP](https://docs.scala-lang.org/sips/completed/value-classes.html).

### Q17 Option ，Try 和 Either 三者的区别？

这三种monads允许我们显示函数没有按预期执行的计算结果。
- Option表示可选值，它的返回类型是Some（代表返回有效数据）或None（代表返回空值）。
- Try类似于Java中的try/catch，如果计算成功，返回Success的实例，如果抛出异常，返回Failure。
- Either可以提供一些计算失败的信息，Either有两种可能返回类型：预期/正确/成功的 和 错误的信息。

代码示例：
```scala
//返回一个Either类型
def personAge(id: Int): Either[String, Int] = {
  val personOpt: Option[Person] = DB.getPersonById(id) //返回Option类型，如果为null返回None，否则返回Some

  personOpt match {
    case None => Left(s"Could not get person with id: $id")  //Left 包含错误或无效值
    case Some(person) => Right(person.age)                    //Right包含正确或有效值
  }
```

### Q18 什么是函数柯里化？

- 柯里化技术是一个接受多个参数的函数转化为接受其中几个参数的函数。经常被用来处理高阶函数。

代码示例：
```scala
def add(a: Int)(b: Int) = a + b

val add2 = add(2)(_)  //_ 表示不只一个的意思，实际是部分应用函数参数，具体参考小红书第二章的习题柯里化与反柯里化实现

scala> add2(3)
res0: Int = 5
```

### Q19 什么是尾递归？

- 正常递归，每一次递归步骤，需要保存信息到堆栈里面，当递归步骤很多时，导致堆栈溢出；
- 尾递归就是为了解决上述问题，在尾递归中所有的计算都是在递归之前调用；
- 编译器可以利用这个属性避免堆栈错误，尾递归的调用可以使信息不插入堆栈，从而优化尾递归；
- 使用 @tailrec 标签可使编译器强制使用尾递归。

代码示例：
```scala
def sum(n: Int): Int = { // 求和计算
  if(n == 0) {
    n
  } else {
    n + sum(n - 1)
  }
}

@tailrec  //告诉编译器
def tailSum(n: Int, acc: Int = 0): Int = {
  if(n == 0) {
    acc
  } else {
    tailSum(n - 1, acc + n)
  }
}

sum(5)
5 + sum(4) // 暂停计算 => 需要添加信息到堆栈
5 + (4 + sum(3))
5 + (4 + (3 + sum(2)))
5 + (4 + (3 + (2 + sum(1))))
5 + (4 + (3 + (2 + 1)))
15

tailSum(5) // tailSum(5, 0) 默认值是0
tailSum(4, 5) // 不需要暂停计算
tailSum(3, 9)
tailSum(2, 12)
tailSum(1, 14)
tailSum(0, 15)
15
```

### Q20 什么是高阶函数？

- 高阶函数指能接受或者返回其他函数的函数，scala中的filter map flatMap函数都能接受其他函数作为参数。




个人总结:

1. monads概念的需要进一步理解
2. Scala Steams使用的内部技术
3. Scala中隐形参数的使用
4. 高阶函数的灵活运用