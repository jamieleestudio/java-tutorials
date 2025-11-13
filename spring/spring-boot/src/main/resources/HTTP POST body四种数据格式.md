# application/x-www-form-urlencoded
application/x-www-form-urlencoded 是最常见的 POST 提交数据方式。浏览器原生的 form 表单如果不设置 enctype 属性，最终就会以这种方式进行提交。类似于：

POST http://www.example.com HTTP/1.1
Content-Type: application/x-www-form-urlencoded;charset=utf-8

title=ahweg&sub%5B%5D=1&sub%5B%5D=2&sub%5B%5D=3


当浏览器提交一个表单时，如果表单的 enctype 设置为 application/x-www-form-urlencoded（这是默认的），浏览器会将表单数据转换成一个巨大的查询字符串，按照 名=值 对格式排列，每对之间用 & 分隔。



# multipart/form-data

Multipart 允许客户端在一次 HTTP 请求中发送多个部分（part）数据，每部分数据之间的类型可以不同。Multipart 并不是一种专一的数据类型，它有很多子类型，例如：multipart/mixed，multipart/form-data 等。
通俗来讲，一个 multipart 消息就是一个大包裹，包裹里面有多个不同类型的消息，每一个消息就是一个 part，每个 part 都会声明自己的消息类型（Content-Type）。除了消息类型，part 还可以附加一些元数据。
Multipart 消息的基本语法结构可以在 RFC2046 中找到：

 每个 multipart 消息的 Content-Type 都必须包含一个叫做 boundary 的参数，boundary 声明了各个 part 之间的边界，记为 ${boundary}。实际上，完整的边界定义为：一行由两个 - 加上 ${boundary} 组成的字符串。假设我们在 Content-Type 里面指定的 boundary=example-part-boundary，那么按照协议规定，每个 part 之间的分隔行就是：--example-part-boundary。
