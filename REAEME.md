# AI工具工程，用来辅助生成代码

# AI用例
## 生成界面
帮我生成以下界面，界面的图片放到了task目录下，名称是task.png
这是一个徽章的UI，有2排，每排有4个徽章位置，一共8个徽章位置，每个徽章位置可以显示徽章，徽章下面显示一个按钮，
分别表示替换，穿戴，解锁，徽章底部齐平的有一个位置序号，使用资源ic_badge_seat_1到ic_badge_seat_8，
徽章已经穿戴了使用背景test_ic_badge_select，徽章解锁了使用ic_badge_seat_free，徽章没有解锁使用：ic_badge_seat_lock
界面所需要的资源已经放到了drawable-xhdpi文件夹中。
使用自定义View的方式，整个View命名为：BadgeShowView
里面每个子View命名为：BadgeShowItemView。
就是说实现一个自定义View，名字叫：BadgeShowView
里面有8个BadgeShowItemView。
要求BadgeShowIView和BadgeShowItemView的根布局使用androidx.constraintlayout.widget.ConstraintLayout。
使用Kotlin语言实现，生成的代码文件统一放到这个包下:com.example.aitooldemo.test1
生成的资源文件放到按照开发要求放到对应目录下。
没有提供的资源比如果按钮的背景自己生成一个。
生成完之后在TestActivity1进行引用测试。
在这个过程中，需要创建文件，或者其他操作的，我都允许，不需要得到我的确认，你可以直接操作。
所有View的属性，如果能在xml设置的，都在xml的中进行设置，不要在代码中动态设置。
修改完之后，你不需要运行代码，我自己会运行。


请你仔细阅读项目工程下的task/task.txt。帮我实现这里面描述的需求


