# Java面向对象编程

## 适用人群
已掌握Java基础语法，准备深入学习面向对象思想的开发者。

## 什么是面向对象
面向对象编程（OOP）是一种以"对象"为核心的编程范式。对象是现实世界事物的抽象，包含属性（数据）和行为（方法）。

Java的三大特性：封装、继承、多态。

---

## 类与对象
类是对象的模板，对象是类的实例。

```java
// 定义一个Student类
public class Student {
    // 属性（成员变量）
    private String name;
    private int age;

    // 构造方法
    public Student(String name, int age) {
        this.name = name;
        this.age = age;
    }

    // 行为（成员方法）
    public void study() {
        System.out.println(name + "正在学习");
    }

    // getter/setter
    public String getName() { return name; }
    public int getAge() { return age; }
}

// 创建对象
Student student = new Student("小明", 20);
student.study(); // 输出：小明正在学习
```

---

## 封装
封装是将数据和操作数据的方法绑定在一起，并对外隐藏内部实现细节。

通过 `private` 修饰属性，通过 `public` 的 getter/setter 方法访问，保护数据安全。

```java
public class BankAccount {
    private double balance; // 余额不能直接访问

    public void deposit(double amount) {
        if (amount > 0) balance += amount;
    }

    public boolean withdraw(double amount) {
        if (amount > 0 && balance >= amount) {
            balance -= amount;
            return true;
        }
        return false;
    }

    public double getBalance() { return balance; }
}
```

---

## 继承
继承允许子类复用父类的属性和方法，使用 `extends` 关键字。

```java
// 父类
public class Animal {
    protected String name;

    public Animal(String name) {
        this.name = name;
    }

    public void eat() {
        System.out.println(name + "在吃东西");
    }
}

// 子类继承父类
public class Dog extends Animal {
    public Dog(String name) {
        super(name); // 调用父类构造方法
    }

    // 子类特有方法
    public void bark() {
        System.out.println(name + "：汪汪汪！");
    }
}

Dog dog = new Dog("旺财");
dog.eat();  // 继承自父类：旺财在吃东西
dog.bark(); // 子类方法：旺财：汪汪汪！
```

---

## 多态
多态是同一个方法在不同对象上有不同的表现形式，通过方法重写（Override）实现。

```java
public class Animal {
    public void makeSound() {
        System.out.println("动物发出声音");
    }
}

public class Cat extends Animal {
    @Override
    public void makeSound() {
        System.out.println("喵喵喵");
    }
}

public class Dog extends Animal {
    @Override
    public void makeSound() {
        System.out.println("汪汪汪");
    }
}

// 多态的体现：父类引用指向子类对象
Animal animal1 = new Cat();
Animal animal2 = new Dog();
animal1.makeSound(); // 喵喵喵
animal2.makeSound(); // 汪汪汪
```

---

## 接口与抽象类
接口定义行为规范，抽象类提供部分实现。

```java
// 接口：定义能力
public interface Flyable {
    void fly(); // 接口方法默认是抽象的
}

// 抽象类：部分实现
public abstract class Shape {
    public abstract double area(); // 抽象方法，子类必须实现

    public void printArea() { // 普通方法，子类可直接使用
        System.out.println("面积：" + area());
    }
}

// 实现接口
public class Bird implements Flyable {
    @Override
    public void fly() {
        System.out.println("鸟儿在飞翔");
    }
}
```

---

## 常见问题
Q: 接口和抽象类的区别？
A: 接口只定义规范（Java 8后可有默认方法），一个类可实现多个接口；抽象类可有具体实现，但只能单继承。优先使用接口定义行为契约。

Q: 什么时候用继承，什么时候用组合？
A: 继承表示"is-a"关系（狗是动物），组合表示"has-a"关系（汽车有引擎）。优先使用组合，继承会增加耦合度。

---

## 学习建议
掌握OOP后，建议学习Java集合框架，它是OOP思想的最佳实践案例。
