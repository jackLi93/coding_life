##Advaced-java 读书笔记

-  简洁优雅方法的声明：
	-  方法声明的重点在于：输入参数和返回类型，以及可能有方法会产生异常，需要外加抛出一些异常。
	-  另外方法名需要具有可读性，方法参数需要具有可读性，而不是很随意取的一些名字
	-  有的方法可能需要使用到泛型
<pre>
	//抛出异常的例子
	public void write(File file) throws IOException{
	//do something
	}

	public int parse(String str) throws NumberFormatException{
	 if(str==null){
			throw new  IllegalArgumentException("String should not be null");	
		}
		return Integer.parseInt(str);
	}

</pre>

-  设计和编写优雅代码方法：

 	-  1.**单一职责原则**:try to implement the methods in such a way ,that every single method does just one thing and does it well.==>每一个方法只做好一件事足以。
 	-  2.**简短原则：** keep method implementations short -->每一个方法的实现保持简短===>这样做能保持较好的可读性
 	-  3.尽量较少的使用return
 
-  API设计小结：
	
当我们写API给别人调用的时候，尽量满足如下小结：
	-  方法名和参数名尽量可读
	-  输入参数尽量小于6个
	-  保持方法简短和可读
	-  为方法写注释
	-  **避免返回null**
	-  使用好权限关键字，隐藏该隐藏的实现，暴露该暴露的