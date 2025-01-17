字节码操纵技术
---

* 反射 
* CGLIB
* ASM

ASM 更多例子看本库 reflect 包下的官方例子

### 最为流行的字节码操纵框架包括：
	
	ASM
	AspectJ
	BCEL
	Byte Buddy
	CGLIB 封装ASM API
	Cojen
	Javassist
	Serp


### 我们为什么应该关注字节码操纵呢？

很多常用的 Java 库，如 Spring 和 Hibernate，以及大多数的 JVM 语言甚至我们的 IDE，都用到了字节码操纵框架。另外，它也确实非常有趣，所以这是一项很有价值的技术，掌握它之后，我们就能完成一些靠其他技术很难实现或无法完成的任务。

一个很重要的使用场景就是程序分析。例如，流行的 bug 定位工具 FindBugs 在底层就使用了 ASM 来分析字节码并定位 bug。有一些软件商店会有一定的代码复杂性规则，比如方法中 if/else 语句的最大数量以及方法的最大长度。静态分析工具会分析我们的字节码来确定代码的复杂性。

另外一个常见的使用场景就是类生成功能。例如，ORM 框架一般都会基于我们的类定义使用代理的机制。或者，在考虑实现应用的安全性时，可能会提供一种语法来添加授权的注解。在这样的场景下，都能很好地运用字节码操纵技术。

像 Scala、Groovy和 Grails 这样的 JVM 语言都使用了字节码操纵框架。
考虑这样一种场景，我们需要转换库中的类，这些类我们并没有源码，这样的任务通常会由 Java profiler 来执行。例如，在 New Relic，采用了字节码 instrumentation 技术实现了对方法执行的计时。

借助字节码操纵，我们可以优化或混淆代码，甚至可以引入一些功能，比如为应用添加重要的日志。本文将会关注一个日志样例，这个样例提供使用这些字节码操纵框架的基本工具。

ASM 最初是一个博士研究项目，在2002年开源。它的更新非常活跃，从5.x版本开始支持 Java 8。ASM 包含了一个基于事件的库和一个基于对象的库，分别类似于 SAX 和 DOM XML 解析器。

一个Java类是由很多组件组成的，包括超类、接口、属性、域和方法。在使用ASM时，我们可以将其均视为事件。我们会提供一个 ClassVisitor 实现，通过它来解析类，当解析器遇到每个组件时，ClassVisitor 上对应的 “visitor” 事件处理器方法会被调用（始终按照上述的顺序）。

```java
package com.sun.xml.internal.ws.org.objectweb.asm; 
   public interface ClassVisitor { 
       void visit(int version, int access, String name, String signature, 
                                  String superName, String[] interfaces);
       void visitSource(String source, String debug); 
       void visitOuterClass(String owner, String name, String desc); 
       AnnotationVisitor visitAnnotation(String desc, boolean visible); 
       void visitAttribute(Attribute attr); 
       void visitInnerClass(String name, String outerName, 
                            String innerName, int access); 
       FieldVisitor visitField(int access, String name, String desc, 
                               String signature, Object value); 
       MethodVisitor visitMethod(int access, String name, String desc, 
                                 String signature, String[] exceptions); 
       void visitEnd(); 
   } 
```

ClassReader解析过程 - 经典的访问者设计模式应用之处

![ClassReader解析过程](https://github.com/jxnu-liguobin/cs-summary-reflection/blob/master/src/main/java/cn/edu/jxnu/reflect/ClassReader%E8%A7%A3%E6%9E%90%E8%BF%87%E7%A8%8B.gif)

### ASM 优劣

	它的内存占用很小；
	它的运行通常会非常快；
	在网上，它的文档很丰富；
	所有的操作码都是可用的，所以可以通过它做很多的事情；
	有很多的社区支持；
	它只有一个不足之处，但这是很大的不足：
	我们编写的是字节码，所以需要理解在幕后是如何运行的，这样的话，开发人员学习的成本就会增加。

### ASM 使用

```java
package cn.edu.jxnu.reflect.asm;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * 演示两种方式动态生成类，该类的main方法打印Hello World
 * 
 * 根据生成的字节码构造对象，启动main
 * 
 * ASM的Opcodes类已经定义了各个修饰符的常量
 * 
 */
public class Helloworld extends ClassLoader implements Opcodes {

	@SuppressWarnings("deprecation")
	public static void main(final String args[]) throws Exception {

		long start1 = System.nanoTime();
		// 创建一个Example类的 ClassWriter
		// which inherits from Object
		ClassWriter cw = new ClassWriter(0);
		cw.visit(V1_1, ACC_PUBLIC, "Example", null, "java/lang/Object", null);
		// 创建默认构造方法的MethodVisitor，调用ClassWriter的visitMethod得到MethodVisitor，操纵方法相关的字节码
		MethodVisitor mw = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		// 将this压入栈
		mw.visitVarInsn(ALOAD, 0);
		// 调超类构造
		mw.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
		// return
		mw.visitInsn(RETURN);
		// 此代码最多使用一个堆栈元素和一个局部变量
		mw.visitMaxs(1, 1);
		// 类构造方法结束
		mw.visitEnd();
		// 为‘main’方法创建一个方法编写器
		mw = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		// System的out入栈
		mw.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
		// "Hello World!" 字符串常量入栈
		mw.visitLdcInsn("Hello world!");
		// 调用println方法
		mw.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
		// return
		mw.visitInsn(RETURN);
		// 此代码最多使用两个堆栈元素和两个本地元素。0，0表示自动计算
		mw.visitMaxs(2, 2);
		// 方法结束
		mw.visitEnd();
		// 获取示例类的字节码，并动态加载它。
		byte[] code = cw.toByteArray();
		// 保存到文件
		FileOutputStream fos = new FileOutputStream("D:\\cs-summary-reflection\\src\\main\\java\\cn\\edu\\jxnu\\reflect\\asm\\Example.class");
		fos.write(code);
		fos.close();
		Helloworld loader = new Helloworld();
		Class<?> exampleClass = loader.defineClass("Example", code, 0, code.length);
		// 使用动态生成的类打印“HelloWorld”
		exampleClass.getMethods()[0].invoke(null, new Object[] { null });
		long end1 = System.nanoTime();
		
		// ------------------------------------------------------------------------
		// 与GeneratorAdapter相同的示例(更方便，但更慢)
		// ------------------------------------------------------------------------
		long start2 = System.nanoTime();
		cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		cw.visit(V1_1, ACC_PUBLIC, "Example", null, "java/lang/Object", null);
		// 创建默认构造器的GeneratorAdapter
		Method m = Method.getMethod("void <init> ()");
		GeneratorAdapter mg = new GeneratorAdapter(ACC_PUBLIC, m, null, null, cw);
		mg.loadThis();
		mg.invokeConstructor(Type.getType(Object.class), m);
		mg.returnValue();
		mg.endMethod();
		m = Method.getMethod("void main (String[])");
		mg = new GeneratorAdapter(ACC_PUBLIC + ACC_STATIC, m, null, null, cw);
		mg.getStatic(Type.getType(System.class), "out", Type.getType(PrintStream.class));
		mg.push("Hello world!");
		mg.invokeVirtual(Type.getType(PrintStream.class), Method.getMethod("void println (String)"));
		mg.returnValue();
		mg.endMethod();
		cw.visitEnd();
		code = cw.toByteArray();
		loader = new Helloworld();
		exampleClass = loader.defineClass("Example", code, 0, code.length);
		exampleClass.getMethods()[0].invoke(null, new Object[] { null });
		long end2 = System.nanoTime();
		
		System.out.println("MethodVisitor:"+(end1-start1));
		System.out.println("GeneratorAdapter:"+ (end2-start2));
		//约3000000~4000000ns
		System.out.println("MethodVisitor比GeneratorAdapter多花:"+(double)((end1-start1)-(end2-start2))+"ns");

	}
}

```

```java
/**
 * 反射
 */
public class ReflectTest {
	private String name;
	private String age;

	public ReflectTest(String name, String age) {
		this.setName(name);
		this.setAge(age);
	}

	public static void main(String[] args) {
		try {
			ReflectTest rt = new ReflectTest("李四", "24");
			fun(rt);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void fun(Object obj) throws Exception {
		Field[] fields = obj.getClass().getDeclaredFields();
		System.out.println("替换之前的:");
		for (Field field : fields) {
			System.out.println(field.getName() + "=" + field.get(obj));
			if (field.getType().equals(java.lang.String.class)) {
				field.setAccessible(true); // 必须设置为true才可以修改成员变量
				String org = (String) field.get(obj);
				field.set(obj, org.replace("李", "b"));
			}

		}
		System.out.println("替换之后的：");
		for (Field field : fields) {
			System.out.println(field.getName() + "=" + field.get(obj));
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}
}

public class DumpMethods {

    public DumpMethods(String s) {
        System.out.println(s + "这是构造方法");
    }

    public DumpMethods() {
        // 自定义的构造将屏蔽默认无参构造
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, SecurityException,
            InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        System.out.println(DumpMethods.class.getName());
        System.out.println("请输入完整的类名：");
        Scanner scanner = new Scanner(System.in);
        String className = scanner.nextLine();

        Class strClass = Class.forName(className);
        // 检索带有指定参数的构造方法
        Class[] strArgsClass = new Class[]{};
        Constructor constructor = strClass.getConstructor(strArgsClass);
        // Constructor:public cn.edu.jxnu.reflect.DumpMethods()
        System.out.println("Constructor:" + constructor.toString());
        // 调用默认的构造方法创建实例对象object
        Object object = constructor.newInstance();
        System.out.println("Object" + object.toString());// Objectcn.edu.jxnu.reflect.DumpMethods@16d3586
        // 调用有参构造
        String string = "JavaEE";
        Class[] strArgsClass2 = new Class[]{String.class};
        Constructor constructor2 = strClass.getConstructor(strArgsClass2);
        //JavaEE这是构造方法
        //constructor2:public cn.edu.jxnu.reflect.DumpMethods(java.lang.String)cn.edu.jxnu.reflect.DumpMethods@154617c
        System.out.println("constructor2:" + constructor2.toString() + constructor2.newInstance(string));
        scanner.close();
    }
}
```
### CGLib 使用场景案例

在写 RPC 的时候想到了顺便使用一下CGLib并比较一下 JDK 代理和 CGLIB 的性能。很久不写 Java 了，所以只能贴 Scala 代码了。

- 基于Java 8、Scala 2.12.7、cglib-nodep 3.2.10

- RPC分客户端和服务端，只贴客户端构建代理对象的代码，服务端比较简单

1.创建代理类并实现需要拦截的接口，一般是方法级拦截 MethodInterceptor
```scala
 private[this] var clientClass: Class[T] = ??? //假设现在有一个类需要被代理

 private[this] class ClientCglibProxy extends MethodInterceptor {
    //Scala实现第一个接口或类均使用extends，第二个开始使用with。并且整个extends后面是一个整体
    //执行方法时，实际是去调用远程的方法并获取结果
    @throws[Throwable]
    override def intercept(o: scala.AnyRef, method: Method, objects: Array[AnyRef], methodProxy: MethodProxy): AnyRef = {
      messageHandler.sendProcessor(request) //RPC中这里请求服务端，但是本地使用的时候若仅做拦截作用，可加调用需要拦截的方法
      //pre invoke
      //methodProxy. invokeSuper(o, objects)
      //after invoke
    }
  }
```
2.实例化代理类，设置被代理类的类型
```scala
  //cglib代理构造代理对象
  @throws[Exception]
  private[client] def cglibProxy[T]: T = {
    val daoProxy = new ClientCglibProxy //使用代理类
    val enhancer = new Enhancer
    enhancer.setCallback(daoProxy) //设置回调的类型为上面的代理类
    enhancer.setSuperclass(clientClass) //设置需要被代理的类
    enhancer.create.asInstanceOf[T] //创建并返回，得到代理后的对象
  }
```
3.服务端调用可以使用CGLIB也可以不使用

* 使用 FastClass.create 构造 Class 并获取类的 Method 再执行 invoke 方法进行调用。
* 获取方法时需要所调用方法的参数类型数组以确定调用哪个方法，调用时需要参数数组，表示传入方法参数。这点对于JDK代理也是一样的。

4.总结

CGLIB 在客户端的使用本质是继承被代理类，不能代理final修饰的类和方法，但是 CGLIB 不用有实现的接口是最大的优点。
至于性能，基于 NIO RPC 测试，目前发现差距几乎可以忽略，当然测试是粗略的。不过现在高版本 Java 对 JDK 代理的支持是足够好的也是毋庸置疑的。
感兴趣的可以使用 [dlsRpc](https://github.com/jxnu-liguobin/dlsRpc) 项目下的 benchmark 模块中的 [JmhClientCglib](https://github.com/jxnu-liguobin/dlsRpc/blob/master/dls-benchmark/src/main/java/io/growing/dlsrpc/benchmark/JmhClientCglib.java) 进行测试。

### ASM与反射、CGLib、JDK代理、Spring

* 反射是读取持久堆上存储的类信息。而 ASM 是直接处理 .class 字节码的小工具（工具虽小，但是功能非常强大！）
* 反射只能读取类信息，而 ASM 除了读还能写。
* 反射读取类信息时需要进行类加载处理，而 ASM 则不需要将类加载到内存中。
* 反射相对于 ASM 来说使用方便，想直接操纵 ASM 的话需要有 JVM 指令基础。（想熟练掌握极难，况且普通开发者也用不着。。。）
* CGLib 是一个开源项目，底层依赖 ASM API 操纵字节码 [CGLib](https://github.com/cglib/cglib)
* Spring 的 AO P动态代理分为 JDK 和 CGLib （只在有必要的时候才开启CGLIB）




[@infoq](http://www.infoq.com/cn/articles/Living-Matrix-Bytecode-Manipulation)
[@Cglib及其基本使用](https://www.cnblogs.com/xrq730/p/6661692.html)