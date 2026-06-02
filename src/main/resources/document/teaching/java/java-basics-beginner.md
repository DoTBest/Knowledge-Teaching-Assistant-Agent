# Java基础入门

## 适用人群
完全零基础或刚开始学习编程的初学者。

## 什么是Java
Java是一种面向对象的编程语言，由Sun Microsystems于1995年发布。它的核心理念是"一次编写，到处运行"（Write Once, Run Anywhere），通过JVM（Java虚拟机）实现跨平台。

Java广泛应用于企业级后端开发、Android应用开发、大数据处理等领域。

---

## 变量与数据类型
变量是存储数据的容器，Java是强类型语言，每个变量必须声明类型。

**基本数据类型：**
- `int`：整数，如 `int age = 18;`
- `double`：小数，如 `double price = 9.9;`
- `boolean`：布尔值，如 `boolean isStudent = true;`
- `char`：单个字符，如 `char grade = 'A';`
- `String`：字符串（引用类型），如 `String name = "张三";`

```java
public class VariableDemo {
    public static void main(String[] args) {
        int age = 20;
        double height = 1.75;
        String name = "小明";
        boolean isStudent = true;
        System.out.println(name + "今年" + age + "岁，身高" + height + "米");
    }
}
```

---

## 控制流程
控制流程决定程序的执行顺序。

**if-else 条件判断：**
```java
int score = 85;
if (score >= 90) {
    System.out.println("优秀");
} else if (score >= 60) {
    System.out.println("及格");
} else {
    System.out.println("不及格");
}
```

**for 循环：**
```java
// 打印1到5
for (int i = 1; i <= 5; i++) {
    System.out.println("第" + i + "次循环");
}
```

**while 循环：**
```java
int count = 0;
while (count < 3) {
    System.out.println("执行第" + (count + 1) + "次");
    count++;
}
```

---

## 方法（函数）
方法是代码复用的基本单元，将一段逻辑封装起来，可以反复调用。

```java
public class MethodDemo {
    // 定义一个求两数之和的方法
    public static int add(int a, int b) {
        return a + b;
    }

    public static void main(String[] args) {
        int result = add(3, 5);
        System.out.println("3 + 5 = " + result); // 输出：3 + 5 = 8
    }
}
```

方法由四部分组成：访问修饰符、返回类型、方法名、参数列表。

---

## 数组
数组是存储同类型数据的容器，长度固定。

```java
// 声明并初始化数组
int[] scores = {90, 85, 78, 92, 88};

// 访问元素（下标从0开始）
System.out.println("第一个成绩：" + scores[0]); // 90

// 遍历数组
for (int score : scores) {
    System.out.println(score);
}
```

---

## 常见问题
Q: int 和 Integer 有什么区别？
A: `int` 是基本类型，直接存储数值，效率高；`Integer` 是包装类，是对象，可以为 null，支持泛型。Java会自动装箱/拆箱在两者之间转换。

Q: == 和 equals() 有什么区别？
A: `==` 比较的是内存地址（引用），`equals()` 比较的是内容。对于 String，应始终使用 `equals()` 比较。

---

## 学习建议
完成本章后，建议进入面向对象编程（OOP）的学习，理解类和对象的概念是Java进阶的关键。
