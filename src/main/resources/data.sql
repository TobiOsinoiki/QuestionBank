
INSERT INTO question (subject, topic, difficulty, question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES

('JAVA','Variables','EASY','What will be the output?\nint x = 5;\nSystem.out.println(x);','5','0','Error','null','A'),

('JAVA','Variables','EASY','Which keyword is used to declare a constant variable?','final','const','static','var','A'),

('JAVA','Data Types','EASY','Which data type stores decimal values?','int','float','boolean','char','B'),

('JAVA','Operators','EASY','What is the result of 10 % 3?','1','3','0','10','A'),

('JAVA','Control Flow','EASY','What will be printed?\nif(true){System.out.println("Yes");}','No','Yes','Error','Nothing','B'),

('JAVA','Loops','EASY','Which loop executes at least once?','for','while','do-while','none','C'),

('JAVA','Loops','EASY','How many times will this run?\nfor(int i=0;i<3;i++)','2','3','4','Infinite','B'),

('JAVA','OOP','EASY','Which keyword is used to create an object?','class','new','object','this','B'),

('JAVA','OOP','EASY','What is a class?','Blueprint','Variable','Method','Loop','A'),

('JAVA','Arrays','EASY','What is the index of first element in array?','1','0','-1','Depends','B'),

('JAVA','Arrays','EASY','What is array length of int[] a = {1,2,3}?','2','3','4','1','B'),

('JAVA','Strings','EASY','Which method returns string length?','size()','length()','getSize()','len()','B'),

('JAVA','Strings','EASY','What is output?\n"Hello".charAt(1)','H','e','l','o','B'),

('JAVA','Operators','EASY','What does ++ mean?','Add 2','Increment by 1','Multiply','None','B'),

('JAVA','Control Flow','EASY','Which statement is used for multiple conditions?','if','switch','loop','case','B'),

('JAVA','Variables','EASY','What will be the output?\nint a = 10;\nint b = a;\nSystem.out.println(b);','10','0','Error','null','A'),

('JAVA','Data Types','EASY','Which data type is used for true/false values?','int','boolean','char','String','B'),

('JAVA','Operators','EASY','What is output?\nSystem.out.println(5 + 3 * 2);','16','11','13','10','B'),

('JAVA','Operators','EASY','Which operator is used for comparison?','==','=','+=','++','A'),

('JAVA','Control Flow','EASY','What will be printed?\nif(false){System.out.println("Hi");}else{System.out.println("Bye");}','Hi','Bye','Error','Nothing','B'),

('JAVA','Loops','EASY','Which loop is best when iterations are known?','while','do-while','for','switch','C'),

('JAVA','Loops','EASY','What is output?\nint i=1; while(i<=3){System.out.print(i); i++;}','123','321','111','Error','A'),

('JAVA','OOP','EASY','Which keyword refers to current object?','this','self','current','object','A'),

('JAVA','OOP','EASY','What is a method?','Variable','Function inside class','Loop','Object','B'),

('JAVA','Arrays','EASY','What is output?\nint[] a={1,2,3}; System.out.println(a[0]);','1','2','3','Error','A'),

('JAVA','Arrays','EASY','Which symbol is used to declare array?','()','{}','[]','<>','C'),

('JAVA','Strings','EASY','What is output?\n"Java".toUpperCase()','java','JAVA','Error','null','B'),

('JAVA','Strings','EASY','Which method compares strings?','==','equals()','compare()','match()','B'),

('JAVA','Operators','EASY','What is output?\nint x=5; x++; System.out.println(x);','5','6','4','Error','B'),

('JAVA','Control Flow','EASY','Which keyword exits loop?','stop','break','exit','end','B'),

('JAVA','Methods','MEDIUM','What is output?\nint add(int a,int b){return a+b;}','No output','Returns sum','Error','Prints sum','B'),

('JAVA','Methods','MEDIUM','What is method overloading?','Same method name different params','Same method name same params','Different names','None','A'),

('JAVA','OOP','MEDIUM','What is encapsulation?','Hiding data','Showing data','Looping','Inheritance','A'),

('JAVA','OOP','MEDIUM','Which keyword is used for inheritance?','extends','implements','inherits','super','A'),

('JAVA','OOP','MEDIUM','What is polymorphism?','Many forms','Single form','Loop','Object','A'),

('JAVA','Arrays','MEDIUM','What is output?\nint[] a={1,2,3}; System.out.println(a.length);','2','3','1','Error','B'),

('JAVA','Arrays','MEDIUM','Which exception occurs when index is invalid?','NullPointer','ArrayIndexOutOfBounds','IO','Arithmetic','B'),

('JAVA','Strings','MEDIUM','What is output?\nString s=\"Java\"; s.concat(\"Code\"); System.out.println(s);','JavaCode','Java','Error','null','B'),

('JAVA','Strings','MEDIUM','Strings in Java are?','Mutable','Immutable','Flexible','Static','B'),

('JAVA','Exception','MEDIUM','What is output?\ntry{int x=5/0;}catch(Exception e){System.out.println(\"Error\");}','Error','5','0','Crash','A'),

('JAVA','Methods','MEDIUM','What will be the output?\nstatic void test(){System.out.println("Hello");}\npublic static void main(String[] args){test();}','Hello','Error','Nothing','null','A'),

('JAVA','Methods','MEDIUM','What is return type of main method?','void','int','String','double','A'),

('JAVA','OOP','MEDIUM','What is abstraction?','Hiding implementation','Showing details','Looping','Inheritance','A'),

('JAVA','OOP','MEDIUM','Which keyword prevents inheritance?','final','static','const','private','A'),

('JAVA','OOP','MEDIUM','What is output?\nclass A{int x=5;} class B extends A{}\nB obj=new B(); System.out.println(obj.x);','5','0','Error','null','A'),

('JAVA','Collections','MEDIUM','Which collection allows duplicates?','Set','Map','List','None','C'),

('JAVA','Collections','MEDIUM','Which collection is unordered?','List','Set','ArrayList','LinkedList','B'),

('JAVA','Collections','MEDIUM','Which class implements List?','HashSet','ArrayList','HashMap','TreeSet','B'),

('JAVA','Arrays','MEDIUM','What is output?\nint[] a=new int[3]; System.out.println(a[1]);','0','1','Error','null','A'),

('JAVA','Arrays','MEDIUM','Default value of int array?','0','1','null','undefined','A'),

('JAVA','Exception','MEDIUM','Which block always executes?','try','catch','finally','throw','C'),

('JAVA','Exception','MEDIUM','Which keyword is used to throw exception?','throws','throw','error','exception','B'),

('JAVA','Exception','MEDIUM','What is checked exception?','Compile-time','Runtime','Logic','Syntax','A'),

('JAVA','Strings','MEDIUM','What is output?\nString s=\"abc\"; System.out.println(s.replace(\"a\",\"z\"));','zbc','abc','Error','null','A'),

('JAVA','Strings','MEDIUM','What is output?\n\"Java\".substring(1,3)','av','ava','ja','Error','A'),

('JAVA','Operators','MEDIUM','What is output?\nint x=2; int y=3; System.out.println(x*y+x);','8','10','6','5','A'),

('JAVA','Control Flow','MEDIUM','What is output?\nfor(int i=0;i<3;i++){if(i==1) continue; System.out.print(i);} ','02','12','01','23','A'),

('JAVA','Control Flow','MEDIUM','What is output?\nint i=0; while(i<2){System.out.print(i); i++;}','01','10','00','Error','A'),

('JAVA','Methods','MEDIUM','Can static method access instance variables?','Yes','No','Sometimes','Only private','B'),

('JAVA','OOP','MEDIUM','What is constructor?','Special method','Loop','Variable','Object','A'),

('JAVA','OOP','MEDIUM','Constructor name must be?','Same as class','Any name','Main','Static','A'),

('JAVA','Collections','MEDIUM','Which map stores key-value?','List','Set','Map','Queue','C'),

('JAVA','Collections','MEDIUM','Which class implements Map?','HashMap','ArrayList','HashSet','Vector','A'),

('JAVA','Exception','MEDIUM','What is output?\ntry{String s=null; s.length();}catch(Exception e){System.out.println("Error");}','Error','Null','Crash','Nothing','A'),

('JAVA','Exception','MEDIUM','Which exception for null object?','Arithmetic','NullPointer','IO','Index','B'),

('JAVA','File Handling','HARD','Which class is used to read file?','FileReader','Scanner','BufferedReader','All','D'),

('JAVA','File Handling','HARD','Which package handles files?','java.io','java.util','java.lang','java.net','A'),

('JAVA','Multithreading','HARD','Which method starts thread?','run()','start()','execute()','init()','B'),

('JAVA','Multithreading','HARD','Which class is used to create thread?','Thread','Runnable','Both','None','C'),

('JAVA','Multithreading','HARD','What is output?\nThread t=new Thread(); t.run();','Runs normally','New thread','Error','Nothing','A'),

('JAVA','Streams','HARD','Which method is used to filter stream?','map()','filter()','reduce()','collect()','B'),

('JAVA','Streams','HARD','Which is terminal operation?','filter()','map()','collect()','peek()','C'),

('JAVA','Lambda','HARD','Lambda expression is used for?','Functional interface','Class','Loop','Variable','A'),

('JAVA','Lambda','HARD','Which symbol used in lambda?','->','=>','==','::','A'),

('JAVA','JDBC','HARD','Which class connects DB?','DriverManager','Connection','Statement','All','D'),

('JAVA','JDBC','HARD','Which method executes query?','executeQuery()','runQuery()','query()','fetch()','A'),

('JAVA','Memory','HARD','Where are objects stored?','Stack','Heap','Register','Disk','B'),

('JAVA','Memory','HARD','Which handles garbage collection?','JVM','JDK','JRE','Compiler','A'),

('JAVA','OOP','HARD','What is interface?','Abstract type','Class','Object','Method','A'),

('JAVA','OOP','HARD','Can interface have methods?','Yes','No','Only static','Only private','A'),

('JAVA','Control Flow','MEDIUM','What is output?\nint x=3; if(x>2) System.out.println("Yes");','Yes','No','Error','Nothing','A'),

('JAVA','Operators','MEDIUM','What is output?\nSystem.out.println(10/3);','3','3.33','Error','0','A'),

('JAVA','Methods','MEDIUM','What is output?\nstatic int a=5; System.out.println(a);','5','0','Error','null','A'),

('JAVA','OOP','MEDIUM','What is super keyword used for?','Parent class','Child class','Method','Loop','A'),

('JAVA','Collections','MEDIUM','Which list is synchronized?','ArrayList','Vector','LinkedList','Stack','B'),

('JAVA','Collections','MEDIUM','Which is fastest for search?','ArrayList','LinkedList','HashSet','Vector','C'),

('JAVA','Exception','MEDIUM','What is output?\ntry{int x=10;}finally{System.out.println("Done");}','Done','Nothing','Error','Crash','A'),

('JAVA','Strings','MEDIUM','What is output?\nString s=\"A\"+\"B\";','AB','A B','Error','null','A'),

('JAVA','Arrays','MEDIUM','What is output?\nint[] a={}; System.out.println(a.length);','0','Error','1','null','A'),

('JAVA','OOP','MEDIUM','Can constructor be overloaded?','Yes','No','Only once','Never','A'),

('JAVA','Multithreading','HARD','What is thread priority range?','1-10','0-5','1-5','0-10','A'),

('JAVA','Multithreading','HARD','Which method pauses thread?','sleep()','wait()','stop()','pause()','A'),

('JAVA','Streams','HARD','Which converts stream to list?','collect()','map()','filter()','reduce()','A'),

('JAVA','Lambda','HARD','Lambda works with how many methods?','1','2','3','Unlimited','A'),

('JAVA','JDBC','HARD','Which closes connection?','close()','end()','stop()','finish()','A'),

('JAVA','File Handling','HARD','Which writes to file?','FileWriter','FileReader','Scanner','InputStream','A'),

('JAVA','Memory','HARD','Stack stores?','Objects','Methods','Variables','Classes','C'),

('JAVA','Memory','HARD','Heap stores?','Methods','Objects','Variables','Loops','B'),

('JAVA','OOP','HARD','Abstract class can have?','Methods','Variables','Both','None','C'),

('JAVA','OOP','HARD','Can abstract class be instantiated?','Yes','No','Sometimes','Only once','B')

;