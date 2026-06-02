# Java集合框架

## 适用人群
已掌握Java OOP，需要深入理解集合框架原理和最佳实践的开发者。

## 集合框架概览
Java集合框架提供了一套统一的数据结构接口和实现，核心接口：
- `Collection`：单列集合根接口
  - `List`：有序、可重复（ArrayList、LinkedList）
  - `Set`：无序、不可重复（HashSet、TreeSet）
- `Map`：键值对集合（HashMap、LinkedHashMap、TreeMap）

---

## ArrayList vs LinkedList
两者都实现 `List` 接口，但底层数据结构不同，性能特征差异显著。

```java
// ArrayList：基于动态数组，随机访问O(1)，插入删除O(n)
List<String> arrayList = new ArrayList<>();
arrayList.add("Java");
arrayList.add("Spring");
String first = arrayList.get(0); // O(1) 随机访问

// LinkedList：基于双向链表，随机访问O(n)，头尾插入删除O(1)
List<String> linkedList = new LinkedList<>();
linkedList.add("Java");
((LinkedList<String>) linkedList).addFirst("Python"); // O(1)
```

**选择原则：** 频繁随机访问用 ArrayList；频繁头尾插入删除用 LinkedList。

---

## HashMap 原理
HashMap 是最常用的 Map 实现，基于哈希表（数组+链表/红黑树）。

```java
Map<String, Integer> scores = new HashMap<>();
scores.put("Alice", 95);
scores.put("Bob", 87);
scores.put("Charlie", 92);

// 获取值
int aliceScore = scores.get("Alice"); // 95
int defaultScore = scores.getOrDefault("Dave", 0); // 0（键不存在时返回默认值）

// 遍历
for (Map.Entry<String, Integer> entry : scores.entrySet()) {
    System.out.println(entry.getKey() + ": " + entry.getValue());
}
```

**Java 8 优化：** 当链表长度超过8且数组长度超过64时，链表转为红黑树，查询从O(n)优化到O(log n)。

**注意：** HashMap 默认初始容量16，负载因子0.75，超过阈值触发扩容（2倍），扩容代价高，预估数据量时建议指定初始容量：`new HashMap<>(expectedSize / 0.75 + 1)`。

---

## HashSet 与去重
HashSet 基于 HashMap 实现，元素唯一性依赖 `hashCode()` 和 `equals()`。

```java
Set<String> set = new HashSet<>();
set.add("Java");
set.add("Spring");
set.add("Java"); // 重复，不会添加
System.out.println(set.size()); // 2

// 自定义对象去重：必须重写 hashCode 和 equals
public class User {
    private String id;
    private String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        return Objects.equals(id, ((User) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
```

---

## Stream API（Java 8+）
Stream 提供函数式风格的集合操作，代码更简洁。

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

// 过滤偶数，平方后求和
int result = numbers.stream()
    .filter(n -> n % 2 == 0)   // 过滤：2,4,6,8,10
    .map(n -> n * n)            // 映射：4,16,36,64,100
    .reduce(0, Integer::sum);   // 归约：220
System.out.println(result); // 220

// 分组统计
List<String> words = Arrays.asList("Java", "Spring", "Python", "JavaScript", "Go");
Map<Integer, List<String>> groupByLength = words.stream()
    .collect(Collectors.groupingBy(String::length));
// {2=[Go], 4=[Java], 6=[Spring, Python], 10=[JavaScript]}
```

---

## 泛型
泛型在编译期提供类型安全检查，避免强制类型转换。

```java
// 泛型类
public class Box<T> {
    private T value;
    public Box(T value) { this.value = value; }
    public T getValue() { return value; }
}

Box<String> strBox = new Box<>("Hello");
Box<Integer> intBox = new Box<>(42);

// 泛型方法
public static <T extends Comparable<T>> T max(T a, T b) {
    return a.compareTo(b) > 0 ? a : b;
}
System.out.println(max(3, 7));       // 7
System.out.println(max("apple", "banana")); // banana
```

---

## 常见问题
Q: HashMap 线程安全吗？
A: 不安全。多线程环境下使用 `ConcurrentHashMap`（分段锁，高并发性能好）或 `Collections.synchronizedMap()`（全局锁，性能差）。

Q: ArrayList 和数组的区别？
A: 数组长度固定，ArrayList 动态扩容；数组可存基本类型，ArrayList 只能存对象；ArrayList 提供丰富的操作方法。

---

## 学习建议
集合框架是Java开发的基础，建议结合Spring框架学习，Spring大量使用集合框架管理Bean和配置。
